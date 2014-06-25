package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurAlbum;
import com.rarchives.ripme.ripper.rippers.ImgurRipper.ImgurImage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class IrarchivesRipper extends AbstractJSONRipper {

    public IrarchivesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "irarchives";
    }
    @Override
    public String getDomain() {
        return "i.rarchives.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        String searchTerm = u.substring(u.indexOf("?") + 1);
        searchTerm = searchTerm.replace("%3A", "=");
        if (searchTerm.startsWith("url=")) {
            if (!searchTerm.contains("http")
                    && !searchTerm.contains(":")) {
                searchTerm = searchTerm.replace("url=", "user=");
            }
        }
        searchTerm = searchTerm.replace("user=user=", "user=");
        return new URL("http://i.rarchives.com/search.cgi?" + searchTerm);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        String searchTerm = u.substring(u.indexOf("?") + 1);
        return Utils.filesystemSafe(searchTerm);
    }
    
    @Override
    public JSONObject getFirstPage() throws IOException {
        return Http.url(url)
                   .timeout(60 * 1000)
                   .getJSON();
    }
    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<String>();
        JSONArray posts = json.getJSONArray("posts");
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = (JSONObject) posts.get(i);
            String theUrl = post.getString("url");
            if (theUrl.contains("imgur.com/a/")) {
                ImgurAlbum album = null;
                try {
                    album = ImgurRipper.getImgurAlbum(new URL(theUrl));
                } catch (IOException e) {
                    logger.error("Error loading imgur album " + theUrl, e);
                    sendUpdate(STATUS.DOWNLOAD_ERRORED, "Can't download " + theUrl + " : " + e.getMessage());
                    continue;
                }
                for (ImgurImage image : album.images) {
                    imageURLs.add(image.url.toExternalForm());
                }
            }
            else {
                imageURLs.add(post.getString("imageurl"));
            }
        }
        return imageURLs;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
