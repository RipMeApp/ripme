package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

// TODO: Probably want to add queue support for cases like this:
// http://s732.photobucket.com/user/doublesix66/library/WARZONE?sort=3&page=1
public class PhotobucketRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(PhotobucketRipper.class);

    private static final String DOMAIN = "photobucket.com",
                                HOST   = "photobucket";
    private static final int ITEMS_PER_PAGE = 24;
    private static final int WAIT_BEFORE_NEXT_PAGE = 2000;

    private final class AlbumMetadata {
        private final String baseURL;
        private final String location;
        private final int sortOrder;
        // cookies for the current page of this album
        private Map<String, String> cookies;
        private Document currPage;
        private int numPages;
        private int pageIndex = 1;

        private AlbumMetadata(JSONObject data) {
            this.baseURL = data.getString("url");
            this.location = data.getString("location")
                                .replace(" ", "_");
            this.sortOrder = data.getInt("sortOrder");
        }

        private String getCurrPageURL(){
            return baseURL + String.format("?sort=%d&page=%d",
                                       sortOrder, pageIndex);
        }
    }

    private final Pattern collDataPattern;
    private final Pattern pbURLPattern;

    // all albums including sub-albums to rip
    private List<AlbumMetadata> albums;
    // the album currently being ripped
    private AlbumMetadata currAlbum;
    // a new index per album downloaded
    private int index = 0;

    public PhotobucketRipper(URL url) throws IOException {
        super(url);
        this.collDataPattern = Pattern.compile(
                "^.*collectionData: (\\{.*}).*$", Pattern.DOTALL
        );
        this.pbURLPattern = Pattern.compile(
                "^https?://([a-zA-Z0-9]+)\\.photobucket\\.com/user/" +
                "([a-zA-Z0-9_\\-]+)/library/([^?]*).*$"
        );
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        logger.info(url);
        String u = url.toExternalForm();
        if (u.contains("?")) {
            // strip options from URL
            u = u.substring(0, u.indexOf("?"));
        }
        if (!u.endsWith("/")) {
            // append trailing slash
            u = u + "/";
        }
        return new URI(u).toURL();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException, URISyntaxException {
        Matcher m;

        URL sanitized = sanitizeURL(url);

        // http://s844.photobucket.com/user/SpazzySpizzy/library/Lady%20Gaga?sort=3&page=1
        m = pbURLPattern.matcher(sanitized.toExternalForm());
        if (m.matches()) {
            // the username is not really a unique GID, because the same user
            // can have multiple albums, but on the other hand, using HOST_GID
            // as save directory means we can group ripped albums of the same
            // user.
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected photobucket.com gallery formats: "
                        + "http://x###.photobucket.com/username/library/..."
                        + " Got: " + url);
    }



    // Page iteration



    @Override
    public Document getFirstPage() throws IOException {
        if (this.currAlbum == null) {
            this.albums = getAlbumMetadata(this.url.toExternalForm());
            logger.info("Detected " + albums.size() + " albums in total");
        }
        this.currAlbum = this.albums.remove(0);
        // NOTE: Why not just get media count in the metadata json?
        //
        // Because that data might not reflect what the user sees on the page
        // and can lead to iterating more pages than there actually are.
        //
        // An example:
        // Metadata JSON -> AlbumStats: 146 images + 0 videos -> 146 items/7 pages
        // http://s1255.photobucket.com/api/user/mimajki/album/Movie%20gifs/get?subAlbums=48&json=1
        // Actual item count when looking at the album url: 131 items/6 pages
        // http://s1255.photobucket.com/user/mimajki/library/Movie%20gifs?sort=6&page=1
        Connection.Response resp = Http.url(currAlbum.getCurrPageURL()).response();
        this.currAlbum.cookies = resp.cookies();
        this.currAlbum.currPage = resp.parse();
        JSONObject collectionData = getCollectionData(currAlbum.currPage);
        int totalNumItems = collectionData.getInt("total");
        this.currAlbum.numPages = (int) Math.ceil(
                (double)totalNumItems / (double)ITEMS_PER_PAGE);
        this.index = 0;
        return currAlbum.currPage;
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        this.currAlbum.pageIndex++;
        boolean endOfAlbum = currAlbum.pageIndex > currAlbum.numPages;
        boolean noMoreSubalbums = albums.isEmpty();
        if (endOfAlbum && noMoreSubalbums){
            throw new IOException("No more pages");
        }
        try {
            Thread.sleep(WAIT_BEFORE_NEXT_PAGE);
        } catch (InterruptedException e) {
            logger.info("Interrupted while waiting before getting next page");
        }
        if (endOfAlbum){
            logger.info("Turning to next album " + albums.get(0).baseURL);
            return getFirstPage();
        } else {
            logger.info("Turning to page " + currAlbum.pageIndex +
                    " of album " + currAlbum.baseURL);
            Connection.Response resp = Http.url(currAlbum.getCurrPageURL()).response();
            currAlbum.cookies = resp.cookies();
            currAlbum.currPage = resp.parse();
            return currAlbum.currPage;
        }
    }



    // Media parsing



    @Override
    protected List<String> getURLsFromPage(Document page) {
        JSONObject collectionData = getCollectionData(page);
        if (collectionData == null) {
            logger.error("Unable to find JSON data at URL: " + page.location());
            // probably better than returning null, as the ripper will display
            // that nothing was found instead of a NullPointerException
            return new ArrayList<>();
        } else {
            return getImageURLs(collectionData);
        }
    }

    private JSONObject getCollectionData(Document page){
        // Retrieve JSON from a script tag in the returned document
        for (Element script : page.select("script[type=text/javascript]")) {
            String data = script.data();
            // Ensure this chunk of javascript contains the album info
            if (data.contains("libraryAlbumsPageCollectionData")) {
                Matcher m = collDataPattern.matcher(data);
                if (m.matches()) {
                    // Grab the JSON
                    return new JSONObject(m.group(1));
                }
            }
        }
        return null;
    }

    private List<String> getImageURLs(JSONObject collectionData){
        List<String> results = new ArrayList<>();
        JSONObject items = collectionData.getJSONObject("items");
        JSONArray objects = items.getJSONArray("objects");
        for (int i = 0; i < objects.length(); i++) {
            JSONObject object = objects.getJSONObject(i);
            String imgURL = object.getString("fullsizeUrl");
            results.add(imgURL);
        }
        return results;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(++this.index), currAlbum.location,
                currAlbum.currPage.location(), currAlbum.cookies);
    }



    // helper methods (for album metadata retrieval)



    private List<AlbumMetadata> getAlbumMetadata(String albumURL)
            throws IOException {
        JSONObject data = getAlbumMetadataJSON(albumURL);
        List<AlbumMetadata> metadata = new ArrayList<>();
        metadata.add(new AlbumMetadata(data));
        if (!data.getString("location").equals("")) {
            // if the location were to equal "", then we are at the profile
            // page of a user. Ripping all sub-albums here would mean ripping
            // all albums of a user (Not supported, only rip items in a users
            // personal bucket).
            for (JSONObject sub : getSubAlbumJSONs(data)){
                metadata.add(new AlbumMetadata(sub));
            }
        }
        logger.info("Succesfully retrieved and parsed metadata");
        return metadata;
    }

    private JSONObject getAlbumMetadataJSON(String albumURL)
            throws IOException {
        String subdomain, user, albumTitle;
        Matcher m = pbURLPattern.matcher(albumURL);
        if (!m.matches()){
            throw new MalformedURLException("invalid URL " + albumURL);
        }
        subdomain = m.group(1);
        user = m.group(2);
        albumTitle = m.group(3);
        if (albumTitle.endsWith("/")){
            albumTitle = albumTitle.substring(0, albumTitle.length() - 1);
        }
        String apiURL = String.format("http://%s.photobucket.com/api/user/" +
                        "%s/album/%s/get?subAlbums=%d&json=1",
                subdomain, user, albumTitle, ITEMS_PER_PAGE);
        logger.info("Loading " + apiURL);
        JSONObject data = Http.url(apiURL).getJSON().getJSONObject("data");
        if (data.has("subAlbums")) {
            int count = data.getInt("subAlbumCount");
            if (count > ITEMS_PER_PAGE) {
                apiURL = String.format("http://%s.photobucket.com/api/user/" +
                                "%s/album/%s/get?subAlbums=%d&json=1",
                        subdomain, user, albumTitle, count);
                data = Http.url(apiURL).getJSON().getJSONObject("data");
            }
        }
        return data;
    }

    private List<JSONObject> getSubAlbumJSONs(JSONObject data) {
        List<JSONObject> subalbumJSONs = new ArrayList<>();
        if (data.has("subAlbums")) {
            JSONArray subalbums = data.getJSONArray("subAlbums");
            for (int idx = 0; idx < subalbums.length(); idx++) {
                JSONObject subalbumJSON = subalbums.getJSONObject(idx);
                subalbumJSONs.add(subalbumJSON);
            }
        }
        return subalbumJSONs;
    }
}
