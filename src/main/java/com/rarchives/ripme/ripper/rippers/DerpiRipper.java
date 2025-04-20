package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

import org.json.JSONObject;
import org.json.JSONArray;

public class DerpiRipper extends AbstractJSONRipper {

    private URL currUrl;
    private Integer currPage;

    public DerpiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "DerpiBooru";
    }

    @Override
    public String getDomain() {
        return "derpibooru.org";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String u = url.toExternalForm();
        String[] uu = u.split("\\?", 2);
        String newU = uu[0];
        if (newU.substring(newU.length() - 1).equals("/")) {
            newU = newU.substring(0, newU.length() - 1);
        }
        newU += ".json?";
        if (uu.length > 1) {
            newU += uu[1];
        }

        String key = Utils.getConfigString("derpi.key", "");
        if (!key.equals("")) {
            newU += "&key=" + key;
        }

        return new URI(newU).toURL();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        currUrl = url;
        currPage = 1;

        // search
        Pattern p = Pattern.compile("^https?://derpibooru\\.org/search\\.json\\?q=([^&]+).*?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "search_" + m.group(1);
        }

        // tags
        p = Pattern.compile("^https?://derpibooru\\.org/tags/([^.]+)\\.json.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "tags_" + m.group(1);
        }

        // galleries
        p = Pattern.compile("^https?://derpibooru\\.org/galleries/([^/]+)/(\\d+)\\.json.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "galleries_" + m.group(1) + "_" + m.group(2);
        }

        // single image
        p = Pattern.compile("^https?://derpibooru\\.org/(\\d+)\\.json.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "image_" + m.group(1);
        }

        throw new MalformedURLException("Unable to find image in " + url);
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        return Http.url(url).getJSON();
    }

    @Override
    public JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        currPage++;
        String u = currUrl.toExternalForm() + "&page=" + Integer.toString(currPage);
        JSONObject json = Http.url(new URI(u).toURL()).getJSON();
        JSONArray arr;
        if (json.has("images")) {
            arr = json.getJSONArray("images");
        } else if (json.has("search")) {
            arr = json.getJSONArray("search");
        } else {
            throw new IOException("No more images");
        }
        if (arr.length() == 0) {
            throw new IOException("No more images");
        }
        return json;
    }

    private String getImageUrlFromJson(JSONObject json) {
        return "https:" + json.getJSONObject("representations").getString("full");
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<>();

        JSONArray arr = null;
        if (json.has("images")) {
            arr = json.getJSONArray("images");
        } else if (json.has("search")) {
            arr = json.getJSONArray("search");
        }
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++){
                imageURLs.add(this.getImageUrlFromJson(arr.getJSONObject(i)));
            }
        } else {
            imageURLs.add(this.getImageUrlFromJson(json));
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // we don't set an index prefix here as derpibooru already prefixes their images with their unique IDs
        addURLToDownload(url, "");
    }
}
