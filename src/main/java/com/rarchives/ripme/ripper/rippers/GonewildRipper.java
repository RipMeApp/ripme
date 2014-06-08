package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class GonewildRipper extends AlbumRipper {

    private static final String HOST   = "gonewild";
    private static final int SLEEP_TIME = 1000;
    
    private static String API_DOMAIN;
    private String username;

    public GonewildRipper(URL url) throws IOException {
        super(url);
        API_DOMAIN = Utils.getConfigString("gw.api", "gonewild");
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
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        int start = 0,
            count = 50;
        String baseGwURL = "http://" + API_DOMAIN + ".rarchives.com/api.cgi"
                + "?method=get_user"
                + "&user=" + username
                + "&count=" + count;
        String gwURL, jsonString, imagePath;
        JSONArray posts, images;
        JSONObject json, post, image;
        while (true) {
            logger.info("    Retrieving posts by " + username);
            gwURL = baseGwURL
                    + "&start=" + start;
            start += count;
            jsonString = Jsoup.connect(gwURL)
                    .ignoreContentType(true)
                    .execute()
                    .body();
            json = new JSONObject(jsonString);
            if (json.has("error")) {
                logger.error("Error while retrieving user posts:" + json.getString("error"));
                break;
            }
            posts = json.getJSONArray("posts");
            if (posts.length() == 0) {
                break; // No more posts to get
            }
            for (int i = 0; i < posts.length(); i++) {
                post = (JSONObject) posts.get(i);
                images = post.getJSONArray("images");
                for (int j = 0; j < images.length(); j++) {
                    image = (JSONObject) images.get(j);
                    imagePath = image.getString("path");
                    if (imagePath.startsWith("..")) {
                        imagePath = imagePath.substring(2);
                    }
                    imagePath = "http://" + API_DOMAIN + ".rarchives.com" + imagePath;
                    logger.info("   Found file: " + imagePath);
                    addURLToDownload(new URL(imagePath));
                }
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load more posts", e);
                break;
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = getUsernameMatcher(url);
        if (m.matches()) {
            this.username = m.group(m.groupCount());
        }
        return username;
    }
}
