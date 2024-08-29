package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;

import com.rarchives.ripme.ripper.AbstractJSONRipper;

public class RedgifsRipper extends AbstractJSONRipper {

    private static final String HOST = "redgifs.com";
    private static final String HOST_2 = "gifdeliverynetwork.com";
    private static final String GIFS_DETAIL_ENDPOINT = "https://api.redgifs.com/v2/gifs/%s";
    private static final String USERS_SEARCH_ENDPOINT = "https://api.redgifs.com/v2/users/%s/search";
    private static final String TEMPORARY_AUTH_ENDPOINT = "https://api.redgifs.com/v2/auth/temporary";
    String username = "";
    String authToken = "";
    // TODO remove
    String cursor = "";
    int count = 40;
    int currentPage = 1;
    int maxPages = 1;

    // TODO remove with search
    String searchText = "";
    int searchCount = 150;
    int searchStart = 0;

    public RedgifsRipper(URL url) throws IOException, URISyntaxException {
        super(new URI(url.toExternalForm().replace("thumbs.", "")).toURL());
    }

    @Override
    public String getDomain() { return "redgifs.com"; }

    @Override
    public String getHost() {
        return "redgifs";
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(HOST) || url.getHost().endsWith(HOST_2);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String sUrl = url.toExternalForm();
        sUrl = sUrl.replace("/gifs/detail", "");
        sUrl = sUrl.replace("/amp", "");
        sUrl = sUrl.replace("gifdeliverynetwork.com", "redgifs.com/watch");
        return new URI(sUrl).toURL();
    }

    public Matcher isProfile() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/users/([a-zA-Z0-9_.-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    public Matcher isSearch() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/gifs/browse/([a-zA-Z0-9_.-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    public Matcher isSingleton() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/watch/([a-zA-Z0-9_-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        try {
            if (authToken == null || authToken.equals("")){
                fetchAuthToken();
            }

            if (isSingleton().matches()) {
                maxPages = 1;
                String gifDetailsURL = String.format(GIFS_DETAIL_ENDPOINT, getGID(url));
                return Http.url(gifDetailsURL).header("Authorization", "Bearer " + authToken).getJSON();
            } else if (isSearch().matches()) {
                // TODO fix search
                // TODO remove 
                throw new IOException("TODO remove");
            } else {
                username = getGID(url);
                var uri = new URIBuilder(String.format(USERS_SEARCH_ENDPOINT, username));
                uri.addParameter("order", "new");
                uri.addParameter("count", Integer.toString(count));
                uri.addParameter("page", Integer.toString(currentPage));
                var json = Http.url(uri.build().toURL()).header("Authorization", "Bearer " + authToken).getJSON();
                maxPages = json.getInt("pages");
                return json;
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {

        Matcher m = isProfile();
        if (m.matches()) {
            return m.group(1);
        }
        m = isSearch();
        if (m.matches()) {
            return m.group(1);
        }
        m = isSingleton();
        if (m.matches()) {
            return m.group(1).split("-")[0];
        }
        throw new MalformedURLException(
                "Expected redgifs.com format: "
                        + "redgifs.com/id or "
                        + "thumbs.redgifs.com/id.gif"
                        + " Got: " + url);
    }

    // TODO remove
    private String stripHTMLTags(String t) {
        t = t.replaceAll("<html>\n" +
                                 " <head></head>\n" +
                                 " <body>", "");
        t = t.replaceAll("</body>\n" +
                                 "</html>", "");
        t = t.replaceAll("\n", "");
        t = t.replaceAll("=\"\"", "");
        return t;
    }

    @Override
    public JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        if (currentPage == maxPages || isSingleton().matches()){
            return null;
        }
        currentPage++;
        if (isSearch().matches()) {
            // TODO search
            // TODO remove
            throw new IOException("// TODO remove");
        } else if (isProfile().matches()) {
                var uri = new URIBuilder(String.format(USERS_SEARCH_ENDPOINT, getGID(url)));
                uri.addParameter("order", "new");
                uri.addParameter("count", Integer.toString(count));
                uri.addParameter("page", Integer.toString(currentPage));
                var json = Http.url(uri.build().toURL()).header("Authorization", "Bearer " + authToken).getJSON();
                // Handle rare maxPages change during a rip
                maxPages = json.getInt("pages");
                return json;
        } else {
            return null;
        }
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> result = new ArrayList<>();
        if (isProfile().matches() || isSearch().matches()) {
            // TODO check json keys for search
            var gifs = json.getJSONArray("gifs");
            for (var gif : gifs){
                var hdURL = ((JSONObject)gif).getJSONObject("urls").getString("hd");
                result.add(hdURL);
            }
        } else {
            String hdURL = json.getJSONObject("gif").getJSONObject("urls").getString("hd");
            result.add(hdURL);
        }
        return result;
    }

    // TODO delete
    /**
     * Helper method for retrieving URLs.
     * @param doc Document of the URL page to look through
     * @return List of URLs to download
     */
    public List<String> hasURLs(Document doc) {
        List<String> result = new ArrayList<>();
        JSONObject page = new JSONObject(stripHTMLTags(doc.html()));
        JSONArray content = page.getJSONArray("gfycats");
        for (int i = 0; i < content.length(); i++) {
            result.add(content.getJSONObject(i).getString("mp4Url"));
        }
        cursor = page.get("cursor").toString();
        return result;
    }

    // TODO delete
    /**
     * Helper method for retrieving video URLs.
     * @param url URL to gfycat page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException, URISyntaxException {
        LOGGER.info("Retrieving " + url.toExternalForm());

        //Sanitize the URL first
        url = new URI(url.toExternalForm().replace("/gifs/detail", "")).toURL();

        Document doc = Http.url(url).get();
        Elements videos = doc.select("script");
        for (Element el : videos) {
            String json = el.html();
            if (json.startsWith("{")) {
                JSONObject page = new JSONObject(json);
                String mobileUrl = page.getJSONObject("video").getString("contentUrl");
                return mobileUrl.replace("-mobile", "");
            }
        }
        throw new IOException();
    }

    
    /** 
     * Fetch a temorary auth token for the rip
     * @throws IOException
     */
    private void fetchAuthToken() throws  IOException{
        var json = Http.url(TEMPORARY_AUTH_ENDPOINT).getJSON();
        var token = json.getString("token");
        authToken = token;
    }
}
