package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

/**
 * https://github.com/500px/api-documentation
 * http://500px.com/tsyganov/stories/80675/galya ("blog")
 * http://500px.com/tsyganov/stories ("blogs") - get HTML, parse stories
 * http://500px.com/tsyganov/favorites
 * http://500px.com/tsyganov (photos)
 * https://api.500px.com/v1/photo
 *  ?rpp=100
 *  &feature=user
 *  &image_size=3
 *  &page=3
 *  &sort=created_at
 *  &include_states=false
 *  &user_id=1913159
 *  &consumer_key=XPm2br2zGBq6TOfd2xbDIHYoLnt3cLxr1HYryGCv
 *
 */
public class FivehundredpxRipper extends AbstractJSONRipper {

    private int page = 1;
    private String baseURL = "https://api.500px.com/v1";
    private static final String CONSUMER_KEY = "XPm2br2zGBq6TOfd2xbDIHYoLnt3cLxr1HYryGCv";

    public FivehundredpxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "500px";
    }
    @Override
    public String getDomain() {
        return "500px.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        // http://500px.com/tsyganov/stories/80675/galya ("blog")
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/stories/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1),
                   blogid   = m.group(2);
            baseURL += "/blogs/" + blogid
                     + "?feature=user"
                     + "&username=" + username
                     + "&image_size=5"
                     + "&rpp=100";
            return username + "_stories_" + blogid;
        }

        // http://500px.com/tsyganov/stories ("blogs")
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/stories/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1);
            baseURL += "/blogs"
                     + "?feature=user"
                     + "&username=" + username
                     + "&rpp=100";
            return username + "_stories";
        }

        // http://500px.com/tsyganov/favorites
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/favorites/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1);
            baseURL += "/photos"
                     + "?feature=user_favorites"
                     + "&username=" + username
                     + "&rpp=100"
                     + "&image_size=5";
            return username + "_faves";
        }

        // http://500px.com/tsyganov/galleries
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/galleries/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1);
            String userID;
            try {
                userID = getUserID(username);
            } catch (IOException e) {
                throw new MalformedURLException("Unable to get User ID from username (" + username + ")");
            }
            baseURL += "/users/" + userID + "/galleries"
                     + "?rpp=100";
            return username + "_galleries";
        }

        // https://500px.com/getesmart86/galleries/olga
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/galleries/([a-zA-Z0-9\\-_]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1);
            String subgallery = m.group(2);
            String userID;
            try {
                userID = getUserID(username);
            } catch (IOException e) {
                throw new MalformedURLException("Unable to get User ID from username (" + username + ")");
            }
            baseURL += "/users/" + userID + "/galleries/" + subgallery + "/items"
                     + "?rpp=100"
                     + "&image_size=5";
            return username + "_galleries_" + subgallery;
        }

        // http://500px.com/tsyganov (photos)
        p = Pattern.compile("^.*500px.com/([a-zA-Z0-9\\-_]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String username = m.group(1);
            baseURL += "/photos"
                     + "?feature=user"
                     + "&username=" + username
                     + "&rpp=100"
                     + "&image_size=5";
            return username;
        }

        throw new MalformedURLException(
                "Expected 500px.com gallery formats: "
                + "/stories/###  /stories  /favorites  /"
                + " Got: " + url);
    }

    /** Convert username to UserID. */
    private String getUserID(String username) throws IOException {
        LOGGER.info("Fetching user ID for " + username);
        JSONObject json = new Http("https://api.500px.com/v1/" +
                    "users/show" +
                    "?username=" + username +
                    "&consumer_key=" + CONSUMER_KEY)
                .getJSON();
        return Long.toString(json.getJSONObject("user").getLong("id"));
    }

    @Override
    public JSONObject getFirstPage() throws IOException, URISyntaxException {
        URL apiURL = new URI(baseURL + "&consumer_key=" + CONSUMER_KEY).toURL();
        LOGGER.debug("apiURL: " + apiURL);
        JSONObject json = Http.url(apiURL).getJSON();

        if (baseURL.contains("/galleries?")) {
            // We're in the root /galleries folder, need to get all images from all galleries.
            JSONObject result = new JSONObject();
            result.put("photos", new JSONArray());
            // Iterate over every gallery
            JSONArray jsonGalleries = json.getJSONArray("galleries");
            for (int i = 0; i < jsonGalleries.length(); i++) {
                if (i > 0) {
                    sleep(500);
                }
                JSONObject jsonGallery = jsonGalleries.getJSONObject(i);
                long galleryID = jsonGallery.getLong("id");
                String userID = Long.toString(jsonGallery.getLong("user_id"));
                String blogURL = "https://api.500px.com/v1/users/" + userID + "/galleries/" + galleryID + "/items"
                     + "?rpp=100"
                     + "&image_size=5"
                     + "&consumer_key=" + CONSUMER_KEY;
                LOGGER.info("Loading " + blogURL);
                sendUpdate(STATUS.LOADING_RESOURCE, "Gallery ID " + galleryID + " for userID " + userID);
                JSONObject thisJSON = Http.url(blogURL).getJSON();
                JSONArray thisPhotos = thisJSON.getJSONArray("photos");
                // Iterate over every image in this story
                for (int j = 0; j < thisPhotos.length(); j++) {
                    result.getJSONArray("photos").put(thisPhotos.getJSONObject(j));
                }
            }
            return result;
        }
        else if (baseURL.contains("/blogs?")) {
            // List of stories to return
            JSONObject result = new JSONObject();
            result.put("photos", new JSONArray());

            // Iterate over every story
            JSONArray jsonBlogs = json.getJSONArray("blog_posts");
            for (int i = 0; i < jsonBlogs.length(); i++) {
                if (i > 0) {
                    sleep(500);
                }
                JSONObject jsonBlog = jsonBlogs.getJSONObject(i);
                int blogid = jsonBlog.getInt("id");
                String username = jsonBlog.getJSONObject("user").getString("username");
                String blogURL = "https://api.500px.com/v1/blogs/" + blogid
                     + "?feature=user"
                     + "&username=" + username
                     + "&rpp=100"
                     + "&image_size=5"
                     + "&consumer_key=" + CONSUMER_KEY;
                LOGGER.info("Loading " + blogURL);
                sendUpdate(STATUS.LOADING_RESOURCE, "Story ID " + blogid + " for user " + username);
                JSONObject thisJSON = Http.url(blogURL).getJSON();
                JSONArray thisPhotos = thisJSON.getJSONArray("photos");
                // Iterate over every image in this story
                for (int j = 0; j < thisPhotos.length(); j++) {
                    result.getJSONArray("photos").put(thisPhotos.getJSONObject(j));
                }
            }
            return result;
        }
        return json;
    }

    @Override
    public JSONObject getNextPage(JSONObject json) throws IOException, URISyntaxException {
        if (isThisATest()) {
            return null;
        }
        // Check previous JSON to see if we hit the last page
        if (!json.has("current_page")
         || !json.has("total_pages")) {
            throw new IOException("No more pages");
        }
        int currentPage = json.getInt("current_page"),
            totalPages  = json.getInt("total_pages");
        if (currentPage == totalPages) {
            throw new IOException("No more results");
        }

        sleep(500);
        ++page;
        URL apiURL = new URI(baseURL
                             + "&page=" + page
                             + "&consumer_key=" + CONSUMER_KEY).toURL();
        return Http.url(apiURL).getJSON();
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<>();
        JSONArray photos = json.getJSONArray("photos");
        for (int i = 0; i < photos.length(); i++) {
            if (super.isStopped()) {
                break;
            }
            JSONObject photo = photos.getJSONObject(i);
            String imageURL = null;
            String rawUrl = "https://500px.com" + photo.getString("url");
            Document doc;
            Elements images = new Elements();
            try {
                LOGGER.debug("Loading " + rawUrl);
                super.retrievingSource(rawUrl);
                doc = Http.url(rawUrl).get();
                images = doc.select("div#preload img");
            }
            catch (IOException e) {
                LOGGER.error("Error fetching full-size image from " + rawUrl, e);
            }
            if (!images.isEmpty()) {
                imageURL = images.first().attr("src");
                LOGGER.debug("Found full-size non-watermarked image: " + imageURL);
            }
            else {
                LOGGER.debug("Falling back to image_url from API response");
                imageURL = photo.getString("image_url");
                imageURL = imageURL.replaceAll("/4\\.", "/5.");
                // See if there's larger images
                for (String imageSize : new String[] { "2048" } ) {
                    String fsURL = imageURL.replaceAll("/5\\.", "/" + imageSize + ".");
                    sleep(10);
                    if (urlExists(fsURL)) {
                        LOGGER.info("Found larger image at " + fsURL);
                        imageURL = fsURL;
                        break;
                    }
                }
            }
            imageURLs.add(imageURL);
            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }

    private boolean urlExists(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() != 200) {
                throw new IOException("Couldn't find full-size image at " + url);
            }
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }

    @Override
    public boolean keepSortOrder() {
        return false;
    }

    @Override
    public void downloadURL(URL url, int index) {
        String u = url.toExternalForm();
        String[] fields = u.split("/");
        String prefix = "/" + getPrefix(index) + fields[fields.length - 3];
        Path saveAs = Paths.get(getWorkingDir() + prefix + ".jpg");
        addURLToDownload(url,  saveAs,  "", null, false);
    }

}
