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
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://instagram.com/([^/]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/([^/]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.instagram.com/p/[a-zA-Z0-9_-]+/\\?taken-by=([^/]+)/?");
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

        Pattern p = Pattern.compile("^.*instagram.com/p/([a-zA-Z0-9\\-_.]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
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

                try {
                    if (!data.getBoolean("is_video")) {
                        if (imageURLs.size() == 0) {
                            // We add this one item to the array because either wise
                            // the ripper will error out because we returned an empty array
                            imageURLs.add(data.getString("thumbnail_src"));
                        }
                        addURLToDownload(new URL(getOriginalUrl(data.getString("thumbnail_src"))), image_date);
                    } else {
                        addURLToDownload(new URL(getVideoFromPage(data.getString("code"))), image_date);
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
            if (!doc.select("meta[property=og:video]").attr("content").equals("")) {
                String videoURL = doc.select("meta[property=og:video]").attr("content");
                // We're ripping a page with a video on it
                imageURLs.add(videoURL);
            } else {
                // We're ripping a picture
                imageURLs.add(doc.select("meta[property=og:image]").attr("content"));
            }

        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }

}
