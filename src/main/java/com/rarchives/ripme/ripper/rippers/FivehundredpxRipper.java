package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

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

    @Override
    public JSONObject getFirstPage() throws IOException {
        URL apiURL = new URL(baseURL + "&consumer_key=" + CONSUMER_KEY);
        logger.debug("apiURL: " + apiURL);
        JSONObject json = Http.url(apiURL).getJSON();
        if (baseURL.contains("/blogs?")) {
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
                logger.info("Loading " + blogURL);
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
    public JSONObject getNextPage(JSONObject json) throws IOException {
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
        URL apiURL = new URL(baseURL
                             + "&page=" + page
                             + "&consumer_key=" + CONSUMER_KEY);
        return Http.url(apiURL).getJSON();
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<String>();
        JSONArray photos = json.getJSONArray("photos");
        for (int i = 0; i < photos.length(); i++) {
            JSONObject photo = photos.getJSONObject(i);
            String imageURL = photo.getString("image_url");
            imageURL = imageURL.replaceAll("/4\\.", "/5.");
            // See if there's larger images
            for (String imageSize : new String[] { "2048" } ) {
                String fsURL = imageURL.replaceAll("/5\\.", "/" + imageSize + ".");
                sleep(10);
                if (urlExists(fsURL)) {
                    logger.info("Found larger image at " + fsURL);
                    imageURL = fsURL;
                    break;
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
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() != 200) {
                throw new IOException("Couldn't find full-size image at " + url);
            }
            return true;
        } catch (IOException e) {
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
        String prefix = getPrefix(index) + fields[fields.length - 3];
        File saveAs = new File(getWorkingDir() + File.separator + prefix + ".jpg");
        addURLToDownload(url,  saveAs,  "", null);
    }

}
