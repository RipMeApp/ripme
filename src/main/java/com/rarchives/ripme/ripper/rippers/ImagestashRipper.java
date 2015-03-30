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

public class ImagestashRipper extends AbstractJSONRipper {

    private int page = 1;

    public ImagestashRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imagestash";
    }
    @Override
    public String getDomain() {
        return "imagestash.org";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagestash.org/tag/([a-zA-Z0-9\\-_]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected imagestash.org tag formats: "
                        + "imagestash.org/tag/tagname"
                        + " Got: " + url);
    }
    
    @Override
    public JSONObject getFirstPage() throws IOException {
        String baseURL = "https://imagestash.org/images?tags="
                       + getGID(url)
                       + "&page=" + page;
        return Http.url(baseURL).getJSON();
    }
    
    @Override
    public JSONObject getNextPage(JSONObject json) throws IOException {
        int count  = json.getInt("count"),
            offset = json.getInt("offset"),
            total  = json.getInt("total");
        if (count + offset >= total || json.getJSONArray("images").length() == 0) {
            throw new IOException("No more images");
        }
        sleep(1000);
        page++;
        return getFirstPage();
    }
    
    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<String>();
        JSONArray images = json.getJSONArray("images");
        for (int i = 0; i < images.length(); i++) {
            JSONObject image = images.getJSONObject(i);
            String imageURL = image.getString("src");
            if (imageURL.startsWith("/")) {
                imageURL = "https://imagestash.org" + imageURL;
            }
            imageURLs.add(imageURL);
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
