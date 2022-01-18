package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DanbooruRipper extends AbstractJSONRipper {
    private static final Logger logger = Logger.getLogger(DanbooruRipper.class);

    private static final String DOMAIN = "danbooru.donmai.us",
            HOST = "danbooru";

    private Pattern gidPattern = null;

    private int currentPageNum = 1;

    public DanbooruRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    private String getPage(int num) throws MalformedURLException {
        return "https://" + getDomain() + "/posts.json?page=" + num + "&tags=" + getTag(url);
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        String newCompatibleJSON = "{ resources:" + Http.url(getPage(1)).getJSONArray() + " }";

        return new JSONObject(newCompatibleJSON);
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        currentPageNum++;

        JSONArray resourcesJSONArray = Http.url(getPage(currentPageNum)).getJSONArray();

        int resourcesJSONArrayLength = resourcesJSONArray.length();

        if (resourcesJSONArrayLength == 0) {
            currentPageNum = 0;
            throw new IOException("No more images in the next page");
        }

        String newCompatibleJSON = "{ resources:" + resourcesJSONArray + " }";

        return new JSONObject(newCompatibleJSON);
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> res = new ArrayList<>(100);
        JSONArray jsonArray = json.getJSONArray("resources");
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).has("file_url")) {
                res.add(jsonArray.getJSONObject(i).getString("file_url"));
            }
        }
        return res;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        try {
            return Utils.filesystemSafe(new URI(getTag(url).replaceAll("([?&])tags=", "")).getPath());
        } catch (URISyntaxException ex) {
            logger.error(ex);
        }

        throw new MalformedURLException("Expected booru URL format: " + getDomain() + "/posts?tags=searchterm - got " + url + " instead");
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private String getTag(URL url) throws MalformedURLException {
        gidPattern = Pattern.compile("https?://danbooru.donmai.us/(posts)?.*([?&]tags=([a-zA-Z0-9$_.+!*'(),%-]+))(&|(#.*)?$)");
        Matcher m = gidPattern.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(3);
        }

        throw new MalformedURLException("Expected danbooru URL format: " + getDomain() + "/posts?tags=searchterm - got " + url + " instead");
    }

}
