package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class GonewildRipper extends AbstractJSONRipper {

    private static final int count = 50;
    private int startIndex = 0;
    private static String API_DOMAIN;
    private String username;

    public GonewildRipper(URL url) throws IOException {
        super(url);
        API_DOMAIN = Utils.getConfigString("gw.api", "gonewild");
    }

    @Override
    public String getHost() {
        return "gonewild";
    }
    @Override
    public String getDomain() {
        return "gonewild.com";
    }

    @Override
    public boolean canRip(URL url) {
        return getUsernameMatcher(url).matches();
    }

    private Matcher getUsernameMatcher(URL url) {
        Pattern p = Pattern.compile("^.*gonewild(\\.com?/|:)(user/)?([a-zA-Z0-9\\-_]{3,})[/?]?.*$");
        return p.matcher(url.toExternalForm());
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = getUsernameMatcher(url);
        if (m.matches()) {
            this.username = m.group(m.groupCount());
        }
        else {
            throw new MalformedURLException("Expected format: gonewild.com/<user>");
        }
        return username;
    }
    
    @Override
    public JSONObject getFirstPage() throws IOException {
        String gwURL = "http://" + API_DOMAIN + ".rarchives.com/api.cgi"
                + "?method=get_user"
                + "&user=" + username
                + "&count=" + count
                + "&start=" + startIndex;
        JSONObject nextJSON = Http.url(gwURL).getJSON();
        if (nextJSON.has("error")) {
            throw new IOException(nextJSON.getString("error"));
        }
        if (nextJSON.getJSONArray("posts").length() == 0) {
            throw new IOException("No posts found");
        }
        return nextJSON;
    }

    @Override
    public JSONObject getNextPage(JSONObject json) throws IOException {
        startIndex += count;
        sleep(1000);
        return getFirstPage();
    }
    
    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<String>();
        JSONArray posts = json.getJSONArray("posts");
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            JSONArray images = post.getJSONArray("images");
            for (int j = 0; j < images.length(); j++) {
                JSONObject image = images.getJSONObject(j);
                String imagePath = image.getString("path");
                if (imagePath.startsWith("..")) {
                    imagePath = imagePath.substring(2);
                }
                imagePath = "http://" + API_DOMAIN + ".rarchives.com" + imagePath;
                imageURLs.add(imagePath);
            }
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
