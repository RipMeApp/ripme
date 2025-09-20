package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class TumblrRipper extends AlbumRipper {

    private static final Logger logger = LogManager.getLogger(TumblrRipper.class);

    int index = 1;

    private static final String DOMAIN = "tumblr.com",
            HOST = "tumblr",
            IMAGE_PATTERN = "([^\\s]+(\\.(?i)(?:jpg|png|gif|bmp))$)";

    private enum ALBUM_TYPE {
        SUBDOMAIN,
        TAG,
        POST,
        LIKED
    }

    private ALBUM_TYPE albumType;
    private String subdomain, tagName, postNumber;

    private static final String TUMBLR_AUTH_CONFIG_KEY = "tumblr.auth";

    private static boolean useDefaultApiKey = false; // fall-back for bad user-specified key
    private static String API_KEY = null;

    /**
     * Gets the API key.
     * Chooses between default/included keys & user specified ones (from the config file).
     * @return Tumblr API key
     */
    public static String getApiKey() {
        // Use a different api ket for unit tests so we don't get 429 errors
        if (isThisATest()) {
            return "UHpRFx16HFIRgQjtjJKgfVIcwIeb71BYwOQXTMtiCvdSEPjV7N";
        }
        if (API_KEY == null) {
            API_KEY = pickRandomApiKey();
        }

        if (useDefaultApiKey || Utils.getConfigString(TUMBLR_AUTH_CONFIG_KEY, "JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX").equals("JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX")) {
            logger.info("Using api key: " + API_KEY);
            return API_KEY;
        } else {
            String userDefinedAPIKey = Utils.getConfigString(TUMBLR_AUTH_CONFIG_KEY, "JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX");
            logger.info("Using user tumblr.auth api key: " + userDefinedAPIKey);
            return userDefinedAPIKey;
        }
    }

    private static String pickRandomApiKey() {
        final List<String> APIKEYS = Arrays.asList("JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX",
                "FQrwZMCxVnzonv90rgNUJcAk4FpnoS0mYuSuGYqIpM2cFgp9L4",
                "qpdkY6nMknksfvYAhf2xIHp0iNRLkMlcWShxqzXyFJRxIsZ1Zz");
        int genNum = new Random().nextInt(APIKEYS.size());
        logger.info(genNum);
        final String API_KEY = APIKEYS.get(genNum); // Select random API key from APIKEYS
        return API_KEY;
    }

    public TumblrRipper(URL url) throws IOException {
        super(url);
        if (getApiKey() == null) {
            throw new IOException("Could not find tumblr authentication key in configuration");
        }
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    /**
     * Sanitizes URL.
     * @param url URL to be sanitized.
     * @return Sanitized URL
     * @throws MalformedURLException
     */
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String u = url.toExternalForm();
        // Convert <FQDN>.tumblr.com/path to <FQDN>/path if needed
        if (StringUtils.countMatches(u, ".") > 2) {
            url = new URI(u.replace(".tumblr.com", "")).toURL();
            if (isTumblrURL(url)) {
                logger.info("Detected tumblr site: " + url);
            }
            else {
                logger.info("Not a tumblr site: " + url);
            }
        }
        return url;
    }

    private boolean isTumblrURL(URL url) {
        String checkURL = "http://api.tumblr.com/v2/blog/";
        checkURL += url.getHost();
        checkURL += "/info?api_key=" + getApiKey();
        try {
            JSONObject json = Http.url(checkURL)
                    .getJSON();
            int status = json.getJSONObject("meta").getInt("status");
            return status == 200;
        } catch (IOException e) {
            logger.error("Error while checking possible tumblr domain: " + url.getHost(), e);
        }
        return false;
    }

    @Override
    public void rip() throws IOException {
        String[] mediaTypes;

        // If true the rip loop won't be run
        boolean shouldStopRipping = false;

        if (albumType == ALBUM_TYPE.POST) {
            mediaTypes = new String[] { "post" };
        } else {
            mediaTypes = new String[] { "photo", "video", "audio" };
        }

        int offset;
        for (String mediaType : mediaTypes) {
            if (isStopped()) {
                break;
            }

            if (shouldStopRipping) {
                break;
            }

            offset = 0;

            while (true) {
                if (isStopped()) {
                    break;
                }

                if (shouldStopRipping) {
                    break;
                }

                String apiURL = getTumblrApiURL(mediaType, offset);
                logger.info("Retrieving " + apiURL);
                sendUpdate(STATUS.LOADING_RESOURCE, apiURL);

                JSONObject json = null;
                boolean retry = false;

                try {
                    json = Http.url(apiURL).getJSON();
                } catch (IOException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof HttpStatusException) {
                        HttpStatusException status = (HttpStatusException)cause;
                        if (status.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED && !useDefaultApiKey) {
                            retry = true;
                        } else if (status.getStatusCode() == 404) {
                            logger.error("No user or album found!");
                            sendUpdate(STATUS.NO_ALBUM_OR_USER, "Album or user doesn't exist!");
                            shouldStopRipping = true;
                            break;
                        } else if (status.getStatusCode() == 429) {
                            logger.error("Tumblr rate limit has been exceeded");
                            sendUpdate(STATUS.DOWNLOAD_ERRORED,"Tumblr rate limit has been exceeded");
                            shouldStopRipping = true;
                            break;
                        }
                    }
                }

                if (retry) {
                    useDefaultApiKey = true;
                    String apiKey = getApiKey();

                    String message = "401 Unauthorized. Will retry with default Tumblr API key: " + apiKey;
                    logger.info(message);
                    sendUpdate(STATUS.DOWNLOAD_WARN, message);

                    Utils.setConfigString(TUMBLR_AUTH_CONFIG_KEY, apiKey); // save the default key to the config

                    // retry loading the JSON

                    apiURL = getTumblrApiURL(mediaType, offset);
                    logger.info("Retrieving " + apiURL);
                    sendUpdate(STATUS.LOADING_RESOURCE, apiURL);

                    json = Http.url(apiURL).getJSON();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("[!] Interrupted while waiting to load next album:", e);
                    break;
                }

                if (!handleJSON(json)) {
                    // Returns false if an error occurs and we should stop.
                    break;
                }

                offset += 20;
            }

            if (isStopped()) {
                break;
            }
        }

        waitForRipperThreads();
    }

    private boolean handleJSON(JSONObject json) {
        JSONArray posts, photos;
        JSONObject post, photo;
        Pattern p;
        Matcher m;
        p = Pattern.compile(IMAGE_PATTERN);

        String fileLocation;
        URL fileURL;

        Pattern qualP = Pattern.compile("_[0-9]+\\.(jpg|png|gif|bmp)$");
        Matcher qualM;

        if (albumType == ALBUM_TYPE.LIKED) {
            posts = json.getJSONObject("response").getJSONArray("liked_posts");
        } else {
            posts = json.getJSONObject("response").getJSONArray("posts");
        }
        if (posts.length() == 0) {
            logger.info("   Zero posts returned.");
            return false;
        }

        for (int i = 0; i < posts.length(); i++) {
            post = posts.getJSONObject(i);
            String date = post.getString("date");
            if (post.has("photos")) {
                photos = post.getJSONArray("photos");
                for (int j = 0; j < photos.length(); j++) {
                    photo = photos.getJSONObject(j);
                    try {
                        fileLocation = photo.getJSONObject("original_size").getString("url").replaceAll("http:", "https:");
                        qualM = qualP.matcher(fileLocation);
                        fileLocation = qualM.replaceFirst("_1280.$1");
                        fileURL = new URI(fileLocation).toURL();

                        m = p.matcher(fileURL.toString());
                        if (m.matches()) {
                            downloadURL(fileURL, date);
                        } else {
                            URL redirectedURL = Http.url(fileURL).ignoreContentType().response().url();
                            downloadURL(redirectedURL, date);
                        }
                    } catch (Exception e) {
                        logger.error("[!] Error while parsing photo in " + photo, e);
                    }
                }
            } else if (post.has("video_url")) {
                try {
                    fileURL = new URI(post.getString("video_url").replaceAll("http:", "https:")).toURL();
                    downloadURL(fileURL, date);
                } catch (Exception e) {
                    logger.error("[!] Error while parsing video in " + post, e);
                    return true;
                }
            } else if (post.has("audio_url")) {
                try {
                    fileURL = new URI(post.getString("audio_url").replaceAll("http:", "https:")).toURL();
                    downloadURL(fileURL, date);
                } catch (Exception e) {
                    logger.error("[!] Error while parsing audio in " + post, e);
                    return true;
                }
                if (post.has("album_art")) {
                    try {
                        fileURL = new URI(post.getString("album_art").replaceAll("http:", "https:")).toURL();
                        downloadURL(fileURL, date);
                    } catch (Exception e) {
                        logger.error("[!] Error while parsing album art in " + post, e);
                        return true;
                    }
                }
            } else if (post.has("body")) {
                Document d = Jsoup.parse(post.getString("body"));
                if (!d.select("img").attr("src").isEmpty()) {
                    try {
                        String imgSrc = d.select("img").attr("src");
                        // Set maximum quality, tumblr doesn't go any further
                        // If the image is any smaller, it will still get the largest available size
                        qualM = qualP.matcher(imgSrc);
                        imgSrc = qualM.replaceFirst("_1280.$1");
                        downloadURL(new URI(imgSrc).toURL(), date);
                    } catch (MalformedURLException | URISyntaxException e) {
                        logger.error("[!] Error while getting embedded image at " + post, e);
                        return true;
                    }
                }
            }
            if (albumType == ALBUM_TYPE.POST) {
                return false;
            }
        }
        return true;
    }

    private String getTumblrApiURL(String mediaType, int offset) {
        StringBuilder sb = new StringBuilder();
        if (albumType == ALBUM_TYPE.LIKED) {
            sb.append("http://api.tumblr.com/v2/blog/")
                    .append(subdomain)
                    .append("/likes")
                    .append("?api_key=")
                    .append(getApiKey())
                    .append("&offset=")
                    .append(offset);
            return sb.toString();
        }
        if (albumType == ALBUM_TYPE.POST) {
            sb.append("http://api.tumblr.com/v2/blog/")
                    .append(subdomain)
                    .append("/posts?id=")
                    .append(postNumber)
                    .append("&api_key=")
                    .append(getApiKey());
            return sb.toString();
        }
        sb.append("http://api.tumblr.com/v2/blog/")
                .append(subdomain)
                .append("/posts/")
                .append(mediaType)
                .append("?api_key=")
                .append(getApiKey())
                .append("&offset=")
                .append(offset);
        if (albumType == ALBUM_TYPE.TAG) {
            sb.append("&tag=")
                    .append(tagName);
        }

        return sb.toString();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        final String DOMAIN_REGEX = "^https?://([a-zA-Z0-9\\-.]+)";

        Pattern p;
        Matcher m;

        // Tagged URL
        p = Pattern.compile(DOMAIN_REGEX + "/tagged/([a-zA-Z0-9\\-%]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.albumType = ALBUM_TYPE.TAG;
            this.subdomain = m.group(1);
            this.tagName = m.group(2);
            this.tagName = this.tagName.replace('-', '+').replace("_", "%20");
            return this.subdomain + "_tag_" + this.tagName.replace("%20", " ");
        }
        // Post URL
        p = Pattern.compile(DOMAIN_REGEX + "/post/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.albumType = ALBUM_TYPE.POST;
            this.subdomain = m.group(1);
            this.postNumber = m.group(2);
            return this.subdomain + "_post_" + this.postNumber;
        }
        // Subdomain-level URL
        p = Pattern.compile(DOMAIN_REGEX + "/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.albumType = ALBUM_TYPE.SUBDOMAIN;
            this.subdomain = m.group(1);
            return this.subdomain;
        }
        // Likes url
        p = Pattern.compile("https?://([a-z0-9_-]+).tumblr.com/likes");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.albumType = ALBUM_TYPE.LIKED;
            this.subdomain = m.group(1);
            return this.subdomain + "_liked";
        }

        // Likes url different format
        p = Pattern.compile("https://www.tumblr.com/liked/by/([a-z0-9_-]+)");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.albumType = ALBUM_TYPE.LIKED;
            this.subdomain = m.group(1);
            return this.subdomain + "_liked";
        }

        throw new MalformedURLException("Expected format: http://subdomain[.tumblr.com][/tagged/tag|/post/postno]");
    }

    private String getPrefix(int i) {
        String prefix = "";
        if (Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", i);
        }
        return prefix;
    }

    public void downloadURL(URL url, String date) {
        logger.info(albumType);
        if (albumType == ALBUM_TYPE.TAG) {
            addURLToDownload(url, date + " ");
        }
        addURLToDownload(url, getPrefix(index));
        index++;
    }
}
