package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangadexRipper extends AbstractJSONRipper {
    private final String chapterApiEndPoint = "https://mangadex.org/api/chapter/";
    private final String mangaApiEndPoint = "https://mangadex.org/api/manga/";
    private boolean isSingleChapter;

    public MangadexRipper(URL url) throws IOException {
        super(url);
    }

    private String getImageUrl(String chapterHash, String imageName, String server) {
        return server + chapterHash + "/" + imageName;
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
        String mangaID = getMangaID(url.toExternalForm());
        if (capID != null) {
            isSingleChapter = true;
            return capID;
        } else if (mangaID != null) {
            isSingleChapter = false;
            return mangaID;
        }
        throw new MalformedURLException("Unable to get chapter ID from" + url);
    }

    private String getChapterID(String url) {
        Pattern p = Pattern.compile("https://(www\\.)?mangadex\\.org/chapter/([\\d]+)/([\\d+]?)");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            return m.group(3);
        }
        return null;
    }

    private String getMangaID(String url) {
        Pattern p = Pattern.compile("https://mangadex.org/title/([\\d]+)/(.+)");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }


    @Override
    public JSONObject getFirstPage() throws IOException, URISyntaxException {
        // Get the chapter ID
        String chapterID = getChapterID(url.toExternalForm());
        String mangaID = getMangaID(url.toExternalForm());
        if (mangaID != null) {
            return Http.url(new URI(mangaApiEndPoint + mangaID).toURL()).getJSON();
        } else
            return Http.url(new URI(chapterApiEndPoint + chapterID).toURL()).getJSON();
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        if (isSingleChapter) {
            List<String> assetURLs = new ArrayList<>();
            JSONArray currentObject;
            String chapterHash;
            // Server is the cdn hosting the images.
            String server;
            chapterHash = json.getString("hash");
            server = json.getString("server");
            for (int i = 0; i < json.getJSONArray("page_array").length(); i++) {
                currentObject = json.getJSONArray("page_array");

                assetURLs.add(getImageUrl(chapterHash, currentObject.getString(i), server));
            }
            return assetURLs;
        }
        JSONObject chaptersJSON = (JSONObject) json.get("chapter");
        JSONObject temp;
        Iterator<String> keys = chaptersJSON.keys();
        HashMap<Double, String> chapterIDs = new HashMap<>();
        while (keys.hasNext()) {
            String keyValue = keys.next();
            temp = (JSONObject) chaptersJSON.get(keyValue);
            if (temp.getString("lang_name").equals("English")) {
                chapterIDs.put(temp.getDouble("chapter"), keyValue);
            }

        }

        List<String> assetURLs = new ArrayList<>();
        JSONArray currentObject;
        String chapterHash;
        // Server is the cdn hosting the images.
        String server;
        JSONObject chapterJSON = null;
        TreeMap<Double, String> treeMap = new TreeMap<>(chapterIDs);
        for (Double aDouble : treeMap.keySet()) {
            double key = (double) aDouble;
            try {
                chapterJSON = Http.url(new URI(chapterApiEndPoint + treeMap.get(key)).toURL()).getJSON();
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, "chapter " + key);
            chapterHash = chapterJSON.getString("hash");
            server = chapterJSON.getString("server");
            for (int i = 0; i < chapterJSON.getJSONArray("page_array").length(); i++) {
                currentObject = chapterJSON.getJSONArray("page_array");

                assetURLs.add(getImageUrl(chapterHash, currentObject.getString(i), server));
            }
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