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
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Http;

public class PhotobucketRipper extends AlbumRipper {

    private static final String DOMAIN = "photobucket.com",
                                HOST   = "photobucket";

    private Response pageResponse = null;

    public PhotobucketRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        LOGGER.info(url);
        String u = url.toExternalForm();
        if (u.contains("?")) {
            u = u.substring(0, u.indexOf("?"));
            return new URL(u);
        }
        else {
            return url;
        }
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (pageResponse == null) {
                pageResponse = Http.url(url).response();
            }
            Document albumDoc = pageResponse.parse();
            Elements els = albumDoc.select("div.libraryTitle > h1");
            if (els.isEmpty()) {
                throw new IOException("Could not find libraryTitle at " + url);
            }
            return els.get(0).text();
        } catch (IOException e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        // http://s844.photobucket.com/user/SpazzySpizzy/library/Lady%20Gaga?sort=3&page=1
        p = Pattern.compile("^https?://[a-zA-Z0-9]+\\.photobucket\\.com/user/([a-zA-Z0-9_\\-]+)/library.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected photobucket.com gallery formats: "
                        + "http://x###.photobucket.com/username/library/..."
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        List<String> subalbums = ripAlbumAndGetSubalbums(this.url.toExternalForm());

        List<String> subsToRip = new ArrayList<>(),
                    rippedSubs = new ArrayList<>();

        for (String sub : subalbums) {
            subsToRip.add(sub);
        }

        while (!subsToRip.isEmpty() && !isStopped()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            String nextSub = subsToRip.remove(0);
            rippedSubs.add(nextSub);
            LOGGER.info("Attempting to rip next subalbum: " + nextSub);
            try {
                pageResponse = null;
                subalbums = ripAlbumAndGetSubalbums(nextSub);
            } catch (IOException e) {
                LOGGER.error("Error while ripping " + nextSub, e);
                break;
            }
            for (String subalbum : subalbums) {
                if (!subsToRip.contains(subalbum) && !rippedSubs.contains(subalbum)) {
                    subsToRip.add(subalbum);
                }
            }
        }
        waitForThreads();
    }

    private List<String> ripAlbumAndGetSubalbums(String theUrl) throws IOException {
        int filesIndex = 0,
            filesTotal = 0,
            pageIndex = 0;
        String currentAlbumPath = null,
               url = null;

        while (pageIndex == 0 || filesIndex < filesTotal) {
            if (isStopped()) {
                break;
            }
            pageIndex++;
            if (pageIndex > 1 || pageResponse == null) {
                url = theUrl + String.format("?sort=3&page=%d", pageIndex);
                LOGGER.info("    Retrieving " + url);
                pageResponse = Http.url(url).response();
            }
            Document albumDoc = pageResponse.parse();
            // Retrieve JSON from request
            String jsonString = null;
            for (Element script : albumDoc.select("script[type=text/javascript]")) {
                String data = script.data();
                // Ensure this chunk of javascript contains the album info
                if (!data.contains("libraryAlbumsPageCollectionData")) {
                    continue;
                }
                // Grab the JSON
                Pattern p; Matcher m;
                p = Pattern.compile("^.*collectionData: (\\{.*}).*$", Pattern.DOTALL);
                m = p.matcher(data);
                if (m.matches()) {
                    jsonString = m.group(1);
                    break;
                }
            }
            if (jsonString == null) {
                LOGGER.error("Unable to find JSON data at URL: " + url);
                break;
            }
            JSONObject json = new JSONObject(jsonString);
            JSONObject items = json.getJSONObject("items");
            JSONArray objects = items.getJSONArray("objects");
            filesTotal = items.getInt("total");
            currentAlbumPath = json.getString("currentAlbumPath");
            for (int i = 0; i < objects.length(); i++) {
                JSONObject object = objects.getJSONObject(i);
                String image = object.getString("fullsizeUrl");
                filesIndex += 1;
                addURLToDownload(new URL(image),
                        "",
                        object.getString("location").replaceAll(" ", "_"),
                        albumDoc.location(),
                        pageResponse.cookies());
            }
        }
        // Get subalbums
        if (url != null) {
            return getSubAlbums(url, currentAlbumPath);
        } else {
            return new ArrayList<>();
        }
    }

    private List<String> getSubAlbums(String url, String currentAlbumPath) {
        List<String> result = new ArrayList<>();
        String subdomain = url.substring(url.indexOf("://")+3);
        subdomain = subdomain.substring(0, subdomain.indexOf("."));
        String apiUrl = "http://" + subdomain + ".photobucket.com/component/Albums-SubalbumList"
                + "?deferCollapsed=true"
                + "&albumPath=" + currentAlbumPath // %2Falbums%2Fab10%2FSpazzySpizzy"
                + "&json=1";
        try {
            LOGGER.info("Loading " + apiUrl);
            JSONObject json = Http.url(apiUrl).getJSON();
            JSONArray subalbums = json.getJSONObject("body").getJSONArray("subAlbums");
            for (int i = 0; i < subalbums.length(); i++) {
                String suburl =
                        "http://"
                        + subdomain
                        + ".photobucket.com"
                        + subalbums.getJSONObject(i).getString("path");
                suburl = suburl.replace(" ", "%20");
                result.add(suburl);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to get subalbums from " + apiUrl, e);
        }
        return result;
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

}