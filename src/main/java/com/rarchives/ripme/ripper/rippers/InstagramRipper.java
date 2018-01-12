package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Utils;


public class InstagramRipper extends AbstractHTMLRipper {

    private String userID;

    public InstagramRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "instagram";
    }
    @Override
    public String getDomain() {
        return "instagram.com";
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith("instagram.com"));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
       URL san_url = new URL(url.toExternalForm().replaceAll("\\?hl=\\S*", ""));
       logger.info("sanitized URL is " + san_url.toExternalForm());
        return san_url;
    }

    private List<String> getPostsFromSinglePage(Document Doc) {
        List<String> imageURLs = new ArrayList<>();
        JSONArray datas;
        try {
            JSONObject json = getJSONFromPage(Doc);
            if (json.getJSONObject("entry_data").getJSONArray("PostPage")
                    .getJSONObject(0).getJSONObject("graphql").getJSONObject("shortcode_media")
                    .has("edge_sidecar_to_children")) {
                datas = json.getJSONObject("entry_data").getJSONArray("PostPage")
                        .getJSONObject(0).getJSONObject("graphql").getJSONObject("shortcode_media")
                        .getJSONObject("edge_sidecar_to_children").getJSONArray("edges");
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = (JSONObject) datas.get(i);
                    data = data.getJSONObject("node");
                    if (data.has("is_video") && data.getBoolean("is_video")) {
                        imageURLs.add(data.getString("video_url"));
                    } else {
                        imageURLs.add(data.getString("display_url"));
                    }
                }
            } else {
                JSONObject data = json.getJSONObject("entry_data").getJSONArray("PostPage")
                        .getJSONObject(0).getJSONObject("graphql").getJSONObject("shortcode_media");
                if (data.getBoolean("is_video")) {
                    imageURLs.add(data.getString("video_url"));
                } else {
                    imageURLs.add(data.getString("display_url"));
                }
            }
            return imageURLs;
        } catch (IOException e) {
            logger.error("Unable to get JSON from page " + url.toExternalForm());
            return null;
        }
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://instagram.com/([^/]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/([^/]+)/?(?:\\?hl=\\S*)?/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/p/([a-zA-Z0-9_-]+)/\\?taken-by=([^/]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2) + "_" + m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/p/([a-zA-Z0-9_-]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/p/([a-zA-Z0-9_-]+)/?(?:\\?hl=\\S*)?/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/explore/tags/([^/]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("Unable to find user in " + url);
    }

    private JSONObject getJSONFromPage(Document firstPage) throws IOException {
        String jsonText = "";
        try {
            for (Element script : firstPage.select("script[type=text/javascript]")) {
                if (script.data().contains("window._sharedData = ")) {
                    jsonText = script.data().replaceAll("window._sharedData = ", "");
                    jsonText = jsonText.replaceAll("};", "}");
                }
            }
            return new JSONObject(jsonText);
        } catch (JSONException e) {
            throw new IOException("Could not get JSON from page");
        }
    }

    @Override
    public Document getFirstPage() throws IOException {
        userID = getGID(url);
        return Http.url(url).get();
    }

    private String getVideoFromPage(String videoID) {
        try {
            Document doc = Http.url("https://www.instagram.com/p/" + videoID).get();
            return doc.select("meta[property=og:video]").attr("content");
        } catch (IOException e) {
            logger.warn("Unable to get page " + "https://www.instagram.com/p/" + videoID);
        }
        return "";
    }

    private String getOriginalUrl(String imageURL) {
        // Without this regex most images will return a 403 error
        imageURL = imageURL.replaceAll("vp/[a-zA-Z0-9]*/", "");
        imageURL = imageURL.replaceAll("scontent.cdninstagram.com/hphotos-", "igcdn-photos-d-a.akamaihd.net/hphotos-ak-");
        // TODO replace this with a single regex
        imageURL = imageURL.replaceAll("p150x150/", "");
        imageURL = imageURL.replaceAll("p320x320/", "");
        imageURL = imageURL.replaceAll("p480x480/", "");
        imageURL = imageURL.replaceAll("p640x640/", "");
        imageURL = imageURL.replaceAll("p720x720/", "");
        imageURL = imageURL.replaceAll("p1080x1080/", "");
        imageURL = imageURL.replaceAll("p2048x2048/", "");
        imageURL = imageURL.replaceAll("s150x150/", "");
        imageURL = imageURL.replaceAll("s320x320/", "");
        imageURL = imageURL.replaceAll("s480x480/", "");
        imageURL = imageURL.replaceAll("s640x640/", "");
        imageURL = imageURL.replaceAll("s720x720/", "");
        imageURL = imageURL.replaceAll("s1080x1080/", "");
        imageURL = imageURL.replaceAll("s2048x2048/", "");

        
        // Instagram returns cropped images to unauthenticated applications to maintain legacy support. 
        // To retrieve the uncropped image, remove this segment from the URL. 
        // Segment format: cX.Y.W.H - eg: c0.134.1080.1080
        imageURL = imageURL.replaceAll("/c\\d{1,4}\\.\\d{1,4}\\.\\d{1,4}\\.\\d{1,4}", "");

        imageURL = imageURL.replaceAll("\\?ig_cache_key.+$", "");
        return imageURL;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        String nextPageID = "";
        List<String> imageURLs = new ArrayList<>();
        JSONObject json = new JSONObject();
        try {
            json = getJSONFromPage(doc);
        } catch (IOException e) {
            logger.warn("Unable to exact json from page");
        }


        if (!url.toExternalForm().contains("/p/")) {
            JSONArray datas = new JSONArray();
            try {
                JSONArray profilePage = json.getJSONObject("entry_data").getJSONArray("ProfilePage");
                datas = profilePage.getJSONObject(0).getJSONObject("user").getJSONObject("media").getJSONArray("nodes");
            } catch (JSONException e) {
                // Handle hashtag pages
                datas = json.getJSONObject("entry_data").getJSONArray("TagPage").getJSONObject(0)
                        .getJSONObject("tag").getJSONObject("media").getJSONArray("nodes");
            }
            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = (JSONObject) datas.get(i);
                Long epoch = data.getLong("date");
                Instant instant = Instant.ofEpochSecond(epoch);
                String image_date = DateTimeFormatter.ofPattern("yyyy_MM_dd_hh:mm_").format(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
                if (data.getString("__typename").equals("GraphSidecar")) {
                    try {
                        Document slideShowDoc = Http.url(new URL ("https://www.instagram.com/p/" + data.getString("code"))).get();
                        List<String> toAdd = getPostsFromSinglePage(slideShowDoc);
                        for (int slideShowInt=0; slideShowInt<toAdd.size(); slideShowInt++) {
                            addURLToDownload(new URL(toAdd.get(slideShowInt)), image_date + data.getString("code"));
                        }
                    } catch (MalformedURLException e) {
                        logger.error("Unable to download slide show, URL was malformed");
                    } catch (IOException e) {
                        logger.error("Unable to download slide show");
                    }
                }
                try {
                    if (!data.getBoolean("is_video")) {
                        if (imageURLs.size() == 0) {
                            // We add this one item to the array because either wise
                            // the ripper will error out because we returned an empty array
                            imageURLs.add(getOriginalUrl(data.getString("thumbnail_src")));
                        }
                        addURLToDownload(new URL(getOriginalUrl(data.getString("thumbnail_src"))), image_date);
                    } else {
                        if (!Utils.getConfigBoolean("instagram.download_images_only", false)) {
                            addURLToDownload(new URL(getVideoFromPage(data.getString("code"))), image_date);
                        } else {
                            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Skipping video " + data.getString("code"));
                        }
                    }
                } catch (MalformedURLException e) {
                    return imageURLs;
                }

                nextPageID = data.getString("id");

                if (isThisATest()) {
                    break;
                }
            }
            // Rip the next page
            if (!nextPageID.equals("") && !isThisATest()) {
                if (url.toExternalForm().contains("/tags/")) {
                    try {
                        // Sleep for a while to avoid a ban
                        sleep(2500);
                        if (url.toExternalForm().substring(url.toExternalForm().length() - 1).equals("/")) {
                            getURLsFromPage(Http.url(url.toExternalForm() + "?max_id=" + nextPageID).get());
                        } else {
                            getURLsFromPage(Http.url(url.toExternalForm() + "/?max_id=" + nextPageID).get());
                        }

                    } catch (IOException e) {
                        return imageURLs;
                    }

                }
                try {
                    // Sleep for a while to avoid a ban
                    sleep(2500);
                    getURLsFromPage(Http.url("https://www.instagram.com/" + userID + "/?max_id=" + nextPageID).get());
                } catch (IOException e) {
                    return imageURLs;
                }
            } else {
                logger.warn("Can't get net page");
            }
        } else { // We're ripping from a single page
            logger.info("Ripping from single page");
            imageURLs = getPostsFromSinglePage(doc);
        }

        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }

}
