package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangadexRipper extends AbstractJSONRipper {
    private String chapterApiEndPoint = "https://mangadex.org/api/chapter/";

    private String getImageUrl(String chapterHash, String imageName, String server) {
        return server + chapterHash + "/" + imageName;
    }

    public MangadexRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "mangadex";
    }
    @Override
    public String getDomain() {
        return "mangadex.org";
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith("mangadex.org"));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String capID = getChapterID(url.toExternalForm());
        if (capID != null) {
            return capID;
        }
        throw new MalformedURLException("Unable to get chapter ID from" + url);
    }

    private String getChapterID(String url) {
        Pattern p = Pattern.compile("https://mangadex.org/chapter/([\\d]+)/?");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        // Get the chapter ID
        String chapterID = getChapterID(url.toExternalForm());
        return Http.url(new URL(chapterApiEndPoint + chapterID)).getJSON();
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> assetURLs = new ArrayList<>();
        JSONArray currentObject;

        String chapterHash = json.getString("hash");
        // Server is the cdn hosting the images.
        String server = json.getString("server");

        for (int i = 0; i < json.getJSONArray("page_array").length(); i++) {
            currentObject = json.getJSONArray("page_array");

            assetURLs.add(getImageUrl(chapterHash, currentObject.getString(i), server));
        }

        return assetURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        // mangadex does not like rippers one bit, so we wait a good long while between requests
        sleep(1000);
        addURLToDownload(url, getPrefix(index));
    }

}
