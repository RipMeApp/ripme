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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

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
        return (url.getHost().endsWith("instagram.com")
             || url.getHost().endsWith("statigr.am")
             || url.getHost().endsWith("iconosquare.com/user"));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://iconosquare.com/user/([a-zA-Z0-9\\-_.]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Unable to find user in " + url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://instagram\\.com/p/([a-zA-Z0-9\\-_.]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Link to photo, not the user account
            try {
                url = getUserPageFromImage(url);
            } catch (Exception e) {
                logger.error("[!] Failed to get user page from " + url, e);
                throw new MalformedURLException("Failed to retrieve user page from " + url);
            }
        }
        p = Pattern.compile("^.*instagram\\.com/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/user/" + m.group(1));
        }
        p = Pattern.compile("^.*iconosquare\\.com/user/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/user/" + m.group(1));
        }
        p = Pattern.compile("^.*statigr\\.am/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/user/" + m.group(1));
        }
        throw new MalformedURLException("Expected username in URL (instagram.com/username and not " + url);
    }
    
    private URL getUserPageFromImage(URL url) throws IOException {
        Document doc = Http.url(url).get();
        for (Element element : doc.select("meta[property='og:description']")) {
            String content = element.attr("content");
            if (content.endsWith("'s photo on Instagram")) {
                return new URL("http://iconosquare/" + content.substring(0, content.indexOf("'")));
            }
        }
        throw new MalformedURLException("Expected username in URL (instagram.com/username and not " + url);
    }
    
    private String getUserID(URL url) throws IOException {
        this.sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
        Document doc = Http.url(url).get();
        for (Element element : doc.select("input[id=user_public]")) {
            return element.attr("value");
        }
        throw new IOException("Unable to find userID at " + this.url);
    }
    
    @Override
    public JSONObject getFirstPage() throws IOException {
        userID = getUserID(url);
        String baseURL = "http://iconosquare.com/controller_nl.php?action=getPhotoUserPublic&user_id="
                        + userID;
        logger.info("Loading " + baseURL);
        try {
            JSONObject result = Http.url(baseURL).getJSON();
            return result;
        } catch (JSONException e) {
            throw new IOException("Could not get instagram user via iconosquare", e);
        }
    }

    @Override
    public JSONObject getNextPage(JSONObject json) throws IOException {
        if (isThisATest()) {
            return null;
        }
        JSONObject pagination = json.getJSONObject("pagination");
        String nextMaxID = "";
        JSONArray datas = json.getJSONArray("data");
        for (int i = 0; i < datas.length(); i++) {
            JSONObject data = datas.getJSONObject(i);
            if (data.has("id")) {
                nextMaxID = data.getString("id");
            }
        }
        if (nextMaxID.equals("")) {
            if (!pagination.has("next_max_id")) {
                throw new IOException("No next_max_id found, stopping");
            }
            nextMaxID = pagination.getString("next_max_id");
        }
        String baseURL = "http://iconosquare.com/controller_nl.php?action=getPhotoUserPublic&user_id="
                        + userID
                        + "&max_id=" + nextMaxID;
        logger.info("Loading " + baseURL);
        sleep(1000);
        JSONObject nextJSON = Http.url(baseURL).getJSON();
        datas = nextJSON.getJSONArray("data");
        if (datas.length() == 0) {
            throw new IOException("No more images found");
        }
        return nextJSON;
    }
    
    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<String>();
        JSONArray datas = json.getJSONArray("data");
        for (int i = 0; i < datas.length(); i++) {
            JSONObject data = (JSONObject) datas.get(i);
            String imageURL;
            if (data.has("videos")) {
                imageURL = data.getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
            } else if (data.has("images")) {
                imageURL = data.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
            } else {
                continue;
            }
            imageURL = imageURL.replaceAll("scontent.cdninstagram.com/hphotos-", "igcdn-photos-d-a.akamaihd.net/hphotos-ak-");
            imageURL = imageURL.replaceAll("s640x640/", "");
            imageURLs.add(imageURL);
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
