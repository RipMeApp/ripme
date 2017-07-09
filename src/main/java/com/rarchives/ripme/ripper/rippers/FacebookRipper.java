package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class FacebookRipper extends AbstractJSONRipper {

    private String pageID;
    private String access_token = "";
    private String rootGraphURL = "https://graph.facebook.com/v2.9/";

    public FacebookRipper(URL url) throws IOException {
        super(url);

        //TODO: Check validity of token
        access_token = Utils.getConfigString("facebook.token", null);
        if (access_token == null) {
            throw new IOException("Could not find a Facebook access token in app configuration");
        }
    }

    @Override
    public String getHost() {
        return "facebook";
    }

    @Override
    public String getDomain() {
        return "facebook.com";
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith("facebook.com"));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?facebook.com/([^/]+)");
        Matcher m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(2);
        }

        throw new MalformedURLException("Unable to find page in " + url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*facebook\\.com/([a-zA-Z0-9\\-_.]+).*$");
        Matcher m = p.matcher(url.toExternalForm().replace("/pg/", "/"));

        if (m.matches()) {
            sendUpdate(STATUS.LOADING_RESOURCE,  "found username: " + m.group(1));
            return new URL("https://www.facebook.com/" + m.group(1));
        }

        throw new MalformedURLException("Expected format www.facebook.com/page and not " + url);
    }

    private String buildGraphRequestURL(String endpoint) {
        return rootGraphURL + endpoint + "?access_token=" + access_token + "&pretty=0";
    }

    private String buildGraphRequestURL(String endpoint, String edge) {
        return rootGraphURL + endpoint + "/" + edge + "?access_token=" + access_token + "&pretty=0";
    }

    private String buildGraphRequestURLWithFields(String endpoint, String fields) {
        return rootGraphURL + endpoint + "/?fields=" + fields + "&access_token=" + access_token + "&pretty=0";
    }

    private String getPageID(String page) throws IOException {
        String idURL = buildGraphRequestURL(page);
        JSONObject result = null;

        try {
            result = Http.url(idURL).getJSON();
            logger.debug(result.toString());

            return result.getString("id");
        } 
        catch (Exception e) {
            throw new IOException("Ensure you have entered a Page not User. Unable to find Page at " + this.url);
        }
    }

    private int GetErrorCode(JSONObject json) {
        if (json.has("error")) {
            return json.getJSONObject("error").getInt("code");
        }
        else {
            return -1;
        }
    }

    private ArrayList<FacebookAlbum> getAlbums() throws IOException {
        ArrayList<FacebookAlbum> albumsList = new ArrayList<FacebookAlbum>();        

        logger.info("Retrieving albums");

        try {
            Boolean hasNext = false;
            String next = null;
            
            do {
                JSONObject result = (hasNext ? Http.url(next).getJSON() : Http.url(buildGraphRequestURL(pageID, "albums")).getJSON());
                JSONArray data = null;

                 if (JSONObject.getNames(result)[0].equals("data")) { 
                    data = result.getJSONArray("data");
                    hasNext = result.getJSONObject("paging").has("next");
                    if (hasNext) next = result.getJSONObject("paging").getString("next"); 
                }
                else {
                    throw new IOException("Could not retrieve albums for page " + pageID);
                }

                for (int i = 0; i < data.length(); i++) {
                    albumsList.add(new FacebookAlbum(data.getJSONObject(i).getString("id"), data.getJSONObject(i).getString("name")));
                }
            } while (hasNext);
        }
        catch (Exception e) {
            throw new IOException("Could not retrieve albums for page " + pageID);
        }

        return albumsList;
    }

    private ArrayList<String> getPhotosFromAlbum(String albumID) throws IOException {
	    ArrayList<String> photoIDS = new ArrayList<String>();     
        ArrayList<String> photoURLS = new ArrayList<String>();        

        logger.info("Retrieving photos from album");
        logger.debug("Retrieving photo ids");

        try {
            Boolean hasNext = false;
            String next = null;

            do {
                JSONObject result = (hasNext ? Http.url(next).getJSON() : Http.url(buildGraphRequestURL(albumID, "photos")).getJSON());
                JSONArray data = null;

                if (JSONObject.getNames(result)[0].equals("data")) {
                    data = result.getJSONArray("data");

                    if (data.length() == 0) {
                        sendUpdate(STATUS.DOWNLOAD_ERRORED, "Empty data set returned for album " + albumID);
                        return photoURLS;
                    }

                    hasNext = result.getJSONObject("paging").has("next");
                    if (hasNext) next = result.getJSONObject("paging").getString("next"); 
                }
                else {
                    throw new IOException("Could not retrieve photo ids for page " + pageID);
                }

                for (int i = 0; i < data.length(); i++) {
                    photoIDS.add(data.getJSONObject(i).getString("id"));
                }
            } while (hasNext);
        }
        catch (Exception e) {
            throw new IOException("Could not retrieve photo ids for page " + pageID);
        }

        logger.debug("Retrieving photo urls");

        for (String pID : photoIDS) {
            try {            
                String result = Http.url(buildGraphRequestURLWithFields(pID, "images")).getJSON().getJSONArray("images").getJSONObject(0).getString("source");
                photoURLS.add(result);
                if (photoURLS.size() % 50 == 0) sendUpdate(STATUS.LOADING_RESOURCE, "photo data...");
            }
            catch (Exception e) {
                throw new IOException("Could not retrieve photo urls for page " + pageID);
            }
        }

        return photoURLS;
    }

    @Override
    public void rip() throws IOException {
        pageID = getPageID(getGID(url));

        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());

        ArrayList<FacebookAlbum> albums = getAlbums();

        if (albums.size() == 0) {
            throw new IOException("No image albums found at " + this.url);
        }
            
        ArrayList<String> photos = new ArrayList<String>();

        sendUpdate(STATUS.LOADING_RESOURCE, "album data");

        for (int i = 0; i < albums.size(); i++) {
            photos.addAll(getPhotosFromAlbum(albums.get(i).getID()));
         
            for (String url : photos) {
                addURLToDownload(new URL(url), "", albums.get(i).getName().replaceAll("[\\\\/:*?\"<>|\\.]", "_"));
            }

            photos.clear();
        }

        // If they're using a thread pool, wait for it.
        if (getThreadPool() != null) {
            logger.debug("Waiting for threadpool " + getThreadPool().getClass().getName());
            getThreadPool().waitForThreads();
        }

        waitForThreads();
    }

	@Override
    public void downloadURL(URL url, int index) { }

    @Override
    public JSONObject getFirstPage() throws IOException { return null; }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) { return null; }
}

class FacebookAlbum {
    private String id;
    private String name;

    FacebookAlbum(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public String getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}