package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ui.History;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangadexRipper extends AbstractJSONRipper {
    private String chapterApiEndPoint = "https://mangadex.org/api/chapter/";
    private String mangaApiEndPoint = "https://mangadex.org/api/manga/";
    private boolean isSingleChapter;
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
        String mangaID = getMangaID(url.toExternalForm());
        if (capID != null) {
            isSingleChapter=true;
            return capID;
        }
        else
            if(mangaID!=null){
                isSingleChapter=false;
                return mangaID;
            }
        throw new MalformedURLException("Unable to get chapter ID from" + url);
    }

    private String getChapterID(String url) {
        Pattern p = Pattern.compile("https://mangadex.org/chapter/([\\d]+)/([\\d+]?)");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
    private String getMangaID(String url){
        Pattern p = Pattern.compile("https://mangadex.org/title/([\\d]+)/(.+)");
        Matcher m = p.matcher(url);
        if(m.matches()){
            return m.group(1);
        }
        return null;
    }


    @Override
    public JSONObject getFirstPage() throws IOException {
        // Get the chapter ID
        String chapterID = getChapterID(url.toExternalForm());
        String mangaID = getMangaID(url.toExternalForm());
        if(mangaID!=null){
            return Http.url(new URL(mangaApiEndPoint+mangaID)).getJSON();
        }
        else
            return Http.url(new URL(chapterApiEndPoint + chapterID)).getJSON();
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        if(isSingleChapter){
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
        HashMap<Double,String> chapterIDs = new HashMap<>();
        while (keys.hasNext()) {
            String keyValue = (String) keys.next();
            temp=(JSONObject)chaptersJSON.get(keyValue);
            if(temp.getString("lang_name").equals("English")) {
                chapterIDs.put(temp.getDouble("chapter"),keyValue);
            }

        }

        List<String> assetURLs = new ArrayList<>();
        JSONArray currentObject;
        String chapterHash;
        // Server is the cdn hosting the images.
        String server;
        JSONObject chapterJSON=null;
        TreeMap<Double,String> treeMap = new TreeMap<>(chapterIDs);
        Iterator it = treeMap.keySet().iterator();
        while(it.hasNext()) {
            double key =(double) it.next();
            try {
                chapterJSON = Http.url(new URL(chapterApiEndPoint + treeMap.get(key))).getJSON();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE,"chapter "+key);
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