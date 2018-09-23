package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TumblrRipper extends AlbumRipper {

    private static final String DOMAIN = "tumblr.com",
            HOST   = "tumblr",
            IMAGE_PATTERN = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

    private enum ALBUM_TYPE {
        SUBDOMAIN,
        TAG,
        POST
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
        if (API_KEY == null) {
            API_KEY = pickRandomApiKey();
        }

        if (useDefaultApiKey || Utils.getConfigString(TUMBLR_AUTH_CONFIG_KEY, "JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX").equals("JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX")) {
            LOGGER.info("Using api key: " + API_KEY);
            return API_KEY;
        } else {
            String userDefinedAPIKey = Utils.getConfigString(TUMBLR_AUTH_CONFIG_KEY, "JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX");
            LOGGER.info("Using user tumblr.auth api key: " + userDefinedAPIKey);
            return userDefinedAPIKey;
        }

    }

    private static String pickRandomApiKey() {
        final List<String> APIKEYS = Arrays.asList("JFNLu3CbINQjRdUvZibXW9VpSEVYYtiPJ86o8YmvgLZIoKyuNX",
                "FQrwZMCxVnzonv90rgNUJcAk4FpnoS0mYuSuGYqIpM2cFgp9L4",
                "qpdkY6nMknksfvYAhf2xIHp0iNRLkMlcWShxqzXyFJRxIsZ1Zz");
        int genNum = new Random().nextInt(APIKEYS.size());
        LOGGER.info(genNum);
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
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        // Convert <FQDN>.tumblr.com/path to <FQDN>/path if needed
        if (StringUtils.countMatches(u, ".") > 2) {
            url = new URL(u.replace(".tumblr.com", ""));
            if (isTumblrURL(url)) {
                LOGGER.info("Detected tumblr site: " + url);
            }
            else {
                LOGGER.info("Not a tumblr site: " + url);
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
            LOGGER.error("Error while checking possible tumblr domain: " + url.getHost(), e);
        }
        return false;
    }

    @Override
    public void rip() throws IOException {
        String[] mediaTypes;
        boolean exceededRateLimit = false;
        if (albumType == ALBUM_TYPE.POST) {
            mediaTypes = new String[] { "post" };
        } else {
            mediaTypes = new String[] { "photo", "video" };
        }
        int offset;
        for (String mediaType : mediaTypes) {
            if (isStopped()) {
                break;
            }

            if (exceededRateLimit) {
                break;
            }
            offset = 0;
            while (true) {
                if (isStopped()) {
                    break;
                }

                if (exceededRateLimit) {
                    break;
                }


                String apiURL = getTumblrApiURL(mediaType, offset);
                LOGGER.info("Retrieving " + apiURL);
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
                        } else if (status.getStatusCode() == 429) {
                            LOGGER.error("Tumblr rate limit has been exceeded");
                            sendUpdate(STATUS.DOWNLOAD_ERRORED,"Tumblr rate limit has been exceeded");
                            exceededRateLimit = true;
                            break;
                        }
                    }
                }

                if (retry) {
                    useDefaultApiKey = true;
                    String apiKey = getApiKey();

                    String message = "401 Unauthorized. Will retry with default Tumblr API key: " + apiKey;
                    LOGGER.info(message);
                    sendUpdate(STATUS.DOWNLOAD_WARN, message);

                    Utils.setConfigString(TUMBLR_AUTH_CONFIG_KEY, apiKey); // save the default key to the config

                    // retry loading the JSON

                    apiURL = getTumblrApiURL(mediaType, offset);
                    LOGGER.info("Retrieving " + apiURL);
                    sendUpdate(STATUS.LOADING_RESOURCE, apiURL);

                    json = Http.url(apiURL).getJSON();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("[!] Interrupted while waiting to load next album:", e);
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
        waitForThreads();
    }

    private boolean handleJSON(JSONObject json) {
        JSONArray posts, photos;
        JSONObject post, photo;
        Pattern p;
        Matcher m;
        p = Pattern.compile(IMAGE_PATTERN);

        URL fileURL;

        posts = json.getJSONObject("response").getJSONArray("posts");
        if (posts.length() == 0) {
            LOGGER.info("   Zero posts returned.");
            return false;
        }

        for (int i = 0; i < posts.length(); i++) {
            post = posts.getJSONObject(i);
            if (post.has("photos")) {
                photos = post.getJSONArray("photos");
                for (int j = 0; j < photos.length(); j++) {
                    photo = photos.getJSONObject(j);
                    try {
                        fileURL = new URL(photo.getJSONObject("original_size").getString("url").replaceAll("http:", "https:"));

                        m = p.matcher(fileURL.toString());
                        if (m.matches()) {
                            addURLToDownload(fileURL);
                        } else {
                            URL redirectedURL = Http.url(fileURL).ignoreContentType().response().url();
                            addURLToDownload(redirectedURL);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[!] Error while parsing photo in " + photo, e);
                    }
                }
            } else if (post.has("video_url")) {
                try {
                    fileURL = new URL(post.getString("video_url").replaceAll("http:", "https:"));
                    addURLToDownload(fileURL);
                } catch (Exception e) {
                    LOGGER.error("[!] Error while parsing video in " + post, e);
                    return true;
                }
            } else if (post.has("body")) {
                Document d = Jsoup.parse(post.getString("body"));
                if (!d.select("img").attr("src").isEmpty()) {
                    try {
                        addURLToDownload(new URL(d.select("img").attr("src")));
                    } catch (MalformedURLException e) {
                        LOGGER.error("[!] Error while getting embedded image at " + post, e);
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
        throw new MalformedURLException("Expected format: http://subdomain[.tumblr.com][/tagged/tag|/post/postno]");
    }

}
