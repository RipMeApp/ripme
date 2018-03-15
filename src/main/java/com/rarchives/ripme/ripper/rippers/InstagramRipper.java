package com.rarchives.ripme.ripper.rippers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
    String nextPageID = "";
    private String qHash;
    private  boolean rippingTag = false;
    private String tagName;

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

    @Override
    public String normalizeUrl(String url) {
        // Remove the date sig from the url
        return url.replaceAll("/[A-Z0-9]{8}/", "/");
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
            rippingTag = true;
            tagName = m.group(1);
            return m.group(1);
        }

        throw new MalformedURLException("Unable to find user in " + url);
    }

    private String stripHTMLTags(String t) {
        t = t.replaceAll("<html>\n" +
                " <head></head>\n" +
                " <body>", "");
        t.replaceAll("</body>\n" +
                "</html>", "");
        return t;
    }


    private JSONObject getJSONFromPage(Document firstPage) throws IOException {
        // Check if this page is HTML + JSON or jsut json
        if (!firstPage.html().contains("window._sharedData =")) {
            return new JSONObject(stripHTMLTags(firstPage.html()));
        }
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
        Document p = Http.url(url).get();
        // Get the query hash so we can download the next page
        qHash = getQHash(p);
        return p;
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
        List<String> imageURLs = new ArrayList<>();
        JSONObject json = new JSONObject();
        try {
            json = getJSONFromPage(doc);
        } catch (IOException e) {
            logger.warn("Unable to exact json from page");
        }


        if (!url.toExternalForm().contains("/p/")) {
            JSONArray datas = new JSONArray();
            if (!rippingTag) {
                // This first try only works on data from the first page
                try {
                    JSONArray profilePage = json.getJSONObject("entry_data").getJSONArray("ProfilePage");
                    userID = profilePage.getJSONObject(0).getString("logging_page_id").replaceAll("profilePage_", "");
                    datas = profilePage.getJSONObject(0).getJSONObject("graphql").getJSONObject("user")
                            .getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges");
                } catch (JSONException e) {
                    datas = json.getJSONObject("data").getJSONObject("user")
                            .getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges");
                }
            } else {
                try {
                    JSONArray tagPage = json.getJSONObject("entry_data").getJSONArray("TagPage");
                    datas = tagPage.getJSONObject(0).getJSONObject("graphql").getJSONObject("hashtag")
                            .getJSONObject("edge_hashtag_to_media").getJSONArray("edges");
                } catch (JSONException e) {
                    datas = json.getJSONObject("data").getJSONObject("hashtag").getJSONObject("edge_hashtag_to_media")
                            .getJSONArray("edges");
                }
            }
            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = (JSONObject) datas.get(i);
                data = data.getJSONObject("node");
                Long epoch = data.getLong("taken_at_timestamp");
                Instant instant = Instant.ofEpochSecond(epoch);
                String image_date = DateTimeFormatter.ofPattern("yyyy_MM_dd_hh:mm_").format(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
                // It looks like tag pages don't have the __typename key
                if (!rippingTag) {
                    if (data.getString("__typename").equals("GraphSidecar")) {
                        try {
                            Document slideShowDoc = Http.url(new URL("https://www.instagram.com/p/" + data.getString("shortcode"))).get();
                            List<String> toAdd = getPostsFromSinglePage(slideShowDoc);
                            for (int slideShowInt = 0; slideShowInt < toAdd.size(); slideShowInt++) {
                                addURLToDownload(new URL(toAdd.get(slideShowInt)), image_date + data.getString("shortcode"));
                            }
                        } catch (MalformedURLException e) {
                            logger.error("Unable to download slide show, URL was malformed");
                        } catch (IOException e) {
                            logger.error("Unable to download slide show");
                        }
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
                            addURLToDownload(new URL(getVideoFromPage(data.getString("shortcode"))), image_date);
                        } else {
                            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Skipping video " + data.getString("shortcode"));
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

        } else { // We're ripping from a single page
            logger.info("Ripping from single page");
            imageURLs = getPostsFromSinglePage(doc);
        }

        return imageURLs;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Document toreturn;
        if (!nextPageID.equals("") && !isThisATest()) {
            if (rippingTag) {
                try {
                    sleep(2500);
                     toreturn = Http.url("https://www.instagram.com/graphql/query/?query_hash=" + qHash +
                                     "&variables={\"tag_name\":\"" + tagName + "\",\"first\":4,\"after\":\"" + nextPageID + "\"}").ignoreContentType().get();
                    // Sleep for a while to avoid a ban
                    logger.info(toreturn.html());
                    return toreturn;

                } catch (IOException e) {
                    throw new IOException("No more pages");
                }

            }
            try {
                // Sleep for a while to avoid a ban
                sleep(2500);
                toreturn = Http.url("https://www.instagram.com/graphql/query/?query_hash=" + qHash + "&variables=" +
                        "{\"id\":\"" + userID + "\",\"first\":100,\"after\":\"" + nextPageID + "\"}").ignoreContentType().get();
                if (!pageHasImages(toreturn)) {
                    throw new IOException("No more pages");
                }
                return toreturn;
            } catch (IOException e) {
                return null;
            }
        } else {
            throw new IOException("No more pages");
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }

    private boolean pageHasImages(Document doc) {
        JSONObject json = new JSONObject(stripHTMLTags(doc.html()));
        int numberOfImages = json.getJSONObject("data").getJSONObject("user")
                .getJSONObject("edge_owner_to_timeline_media").getJSONArray("edges").length();
        if (numberOfImages == 0) {
            return false;
        }
        return true;
    }

    private String getQHash(Document doc) {
        String jsFileURL = "https://www.instagram.com" + doc.select("link[rel=preload]").attr("href");
        StringBuilder sb = new StringBuilder();
        Document jsPage;
        try {
            // We can't use Jsoup here because it won't download a non-html file larger than a MB
            // even if you set maxBodySize to 0
            URLConnection connection = new URL(jsFileURL).openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();

        } catch (MalformedURLException e) {
            logger.info("Unable to get query_hash, " + jsFileURL + " is a malformed URL");
            return null;
        } catch (IOException e) {
            logger.info("Unable to get query_hash");
            logger.info(e.getMessage());
            return null;
        }
        if (!rippingTag) {
            Pattern jsP = Pattern.compile("o},queryId:.([a-zA-Z0-9]+).");
            Matcher m = jsP.matcher(sb.toString());
            if (m.find()) {
                return m.group(1);
            }
        } else {
            Pattern jsP = Pattern.compile("return e.tagMedia.byTagName.get\\(t\\).pagination},queryId:.([a-zA-Z0-9]+).");
            Matcher m = jsP.matcher(sb.toString());
            if (m.find()) {
                return m.group(1);
            }
        }
        logger.info("Could not find query_hash on " + jsFileURL);
        return null;

    }

}
