package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class InstagramRipper extends AbstractJSONRipper {

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
        Pattern p = Pattern.compile("^https?://instagram.com/([^/]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Unable to find user in " + url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*instagram\\.com/([a-zA-Z0-9\\-_.]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://instagram.com/" + m.group(1));
        }

        throw new MalformedURLException("Expected username in URL (instagram.com/username and not " + url);
    }

    private String getUserID(URL url) throws IOException {

        Pattern p = Pattern.compile("^https?://instagram\\.com/([^/]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new IOException("Unable to find userID at " + this.url);
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        userID = getUserID(url);

        String jsonText = "";
        try {
            Document firstPage = Http.url("http://instagram.com/" + userID).get();
            for (Element script : firstPage.select("script[type=text/javascript]")) {
                logger.info("Found script");

                if (script.data().contains("window._sharedData = ")) {
                   jsonText = script.data().replaceAll("window._sharedData = ", "");
                   jsonText = jsonText.replaceAll("};", "}");
                }
            }
            logger.debug(jsonText);
            return new JSONObject(jsonText);
        } catch (JSONException e) {
            throw new IOException("Could not get instagram user");
        }
    }

    @Override
    public JSONObject getNextPage(JSONObject json) throws IOException {

        boolean nextPageAvailable;
        try {
            nextPageAvailable = json.getBoolean("more_available");
        } catch (Exception e) {
            throw new IOException("No additional pages found");
        }

        if (nextPageAvailable) {
            JSONArray items         = json.getJSONArray("items");
            JSONObject last_item    = items.getJSONObject(items.length() - 1);
            String nextMaxID        = last_item.getString("id");

            String baseURL = "http://instagram.com/" + userID + "/media/?max_id=" + nextMaxID;
            logger.info("Loading " + baseURL);
            sleep(1000);

            return Http.url(baseURL).getJSON();
        } else {
            throw new IOException("No more images found");
        }
    }

    private String getOriginalUrl(String imageURL) {
        imageURL = imageURL.replaceAll("scontent.cdninstagram.com/hphotos-", "igcdn-photos-d-a.akamaihd.net/hphotos-ak-");
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

    private String getMedia(JSONObject data) {
        String imageURL = "";
        JSONObject mediaObject;
        if (data.has("videos")) {
            mediaObject = data.getJSONObject("videos");
            if (!mediaObject.isNull("standard_resolution")) {
                imageURL = mediaObject.getJSONObject("standard_resolution").getString("url");
            }
        } else if (data.has("images")) {
            mediaObject = data.getJSONObject("images");
            if (!mediaObject.isNull("standard_resolution")) {
                imageURL = mediaObject.getJSONObject("standard_resolution").getString("url");
            }
        }
        return imageURL;
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<>();
        JSONArray profilePage = json.getJSONObject("entry_data").getJSONArray("ProfilePage");
        JSONArray datas = profilePage.getJSONObject(0).getJSONObject("user").getJSONObject("media").getJSONArray("nodes");
        for (int i = 0; i < datas.length(); i++) {
            JSONObject data = (JSONObject) datas.get(i);
            imageURLs.add(getOriginalUrl(data.getString("thumbnail_src")));

//            String dataType = data.getString("type");
//            if (dataType.equals("carousel")) {
//                JSONArray carouselMedias = data.getJSONArray("carousel_media");
//                for (int carouselIndex = 0; carouselIndex < carouselMedias.length(); carouselIndex++) {
//                    JSONObject carouselMedia = (JSONObject) carouselMedias.get(carouselIndex);
//                    String imageURL = getMedia(carouselMedia);
//                    if (!imageURL.equals("")) {
//                        imageURL = getOriginalUrl(imageURL);
//                        imageURLs.add(imageURL);
//                    }
//                }
//            } else {
//                String imageURL = getMedia(data);
//                if (!imageURL.equals("")) {
//                    imageURL = getOriginalUrl(imageURL);
//                    imageURLs.add(imageURL);
//                }
//            }

            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }

}
