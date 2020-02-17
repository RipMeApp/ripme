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

public class ByteRipper extends AbstractJSONRipper {

    private String userId;
    private String cursor;
    private static final String BASE_URL = "https://api.byte.co/";
    private static final String AUTH_HEADER = "2GKVHKTO7ZHMVI7NMKLA7AJWNI";

    public ByteRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "byte";
    }

    @Override
    public String getDomain() {
        return "byte.co";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://byte.co/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected byte.co username, got: " + url);
    }

    private String getUserId(String username) throws IOException {
        JSONObject json = new Http(BASE_URL + "account/prefix/" + username)
            .header("Authorization", AUTH_HEADER)
            .getJSON();
        JSONArray accounts = json.getJSONObject("data").getJSONArray("accounts");
        LOGGER.debug(accounts);
        if (accounts.length() == 0) {
            throw new IOException("Username does not exist.");
        }
        userId = accounts.getJSONObject(0).getString("id");
        return userId;
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        getUserId("aurahack");

        LOGGER.debug(BASE_URL + "account/id/" + userId + "/posts");
        JSONObject json = new Http(BASE_URL + "account/id/" + userId + "/posts")
            .header("Authorization", AUTH_HEADER)
            .getJSON();
        LOGGER.debug(json);
        return json;
    }

    @Override
    public JSONObject getNextPage(JSONObject prevPage) throws IOException {
        if (isThisATest()) {
            return null;
        }
        sleep(1500);

        if (!prevPage.getJSONObject("data").has("cursor")) {
            return null;
        }

        cursor = prevPage.getJSONObject("data").getString("cursor");

        JSONObject json = new Http(BASE_URL + "account/id/" + userId + "/posts?cursor=" + cursor)
            .header("Authorization", AUTH_HEADER)
            .getJSON();
        return json;
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> videoUrls = new ArrayList<>();
        JSONArray videos = json.getJSONObject("data").getJSONArray("posts");
        for (int i = 0; i < videos.length(); i++) {
            LOGGER.debug("Video");
            LOGGER.debug(i);
            JSONObject video = videos.getJSONObject(i);
            videoUrls.add(video.getString("videoSrc"));
        }
        return videoUrls;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
