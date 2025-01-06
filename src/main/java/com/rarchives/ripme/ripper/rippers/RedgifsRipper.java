package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;

public class RedgifsRipper extends AbstractJSONRipper {

    private static final String HOST = "redgifs.com";
    private static final String HOST_2 = "gifdeliverynetwork.com";
    private static final String GIFS_DETAIL_ENDPOINT = "https://api.redgifs.com/v2/gifs/%s";
    private static final String USERS_SEARCH_ENDPOINT = "https://api.redgifs.com/v2/users/%s/search";
    private static final String GALLERY_ENDPOINT = "https://api.redgifs.com/v2/gallery/%s";
    private static final String SEARCH_ENDPOINT = "https://api.redgifs.com/v2/search/%s";
    private static final String TAGS_ENDPOINT = "https://api.redgifs.com/v2/gifs/search";
    private static final String TEMPORARY_AUTH_ENDPOINT = "https://api.redgifs.com/v2/auth/temporary";
    private static final Pattern PROFILE_PATTERN = Pattern
            .compile("^https?://[a-zA-Z0-9.]*redgifs\\.com/users/([a-zA-Z0-9_.-]+).*$");
    private static final Pattern SEARCH_PATTERN = Pattern.compile(
            "^https?:\\/\\/[a-zA-Z0-9.]*redgifs\\.com\\/search(?:\\/[a-zA-Z]+)?\\?.*?query=([a-zA-Z0-9-_+%]+).*$");
    private static final Pattern TAGS_PATTERN = Pattern
            .compile("^https?:\\/\\/[a-zA-Z0-9.]*redgifs\\.com\\/gifs\\/([a-zA-Z0-9_.,-]+).*$");
    private static final Pattern SINGLETON_PATTERN = Pattern
            .compile("^https?://[a-zA-Z0-9.]*redgifs\\.com/watch/([a-zA-Z0-9_-]+).*$");

    /**
     * Keep a single auth token for the complete lifecycle of the app.
     * This should prevent fetching of multiple tokens.
     */
    private static String authToken = "";

    String username = "";
    int count = 40;
    int currentPage = 1;
    int maxPages = 1;

    public RedgifsRipper(URL url) throws IOException, URISyntaxException {
        super(new URI(url.toExternalForm().replace("thumbs.", "")).toURL());
    }

    @Override
    public String getDomain() {
        return "redgifs.com";
    }

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
        return PROFILE_PATTERN.matcher(url.toExternalForm());
    }

    public Matcher isSearch() {
        return SEARCH_PATTERN.matcher(url.toExternalForm());
    }

    public Matcher isTags() {
        return TAGS_PATTERN.matcher(url.toExternalForm());
    }

    public Matcher isSingleton() {
        return SINGLETON_PATTERN.matcher(url.toExternalForm());
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        try {
            if (authToken == null || authToken.isBlank()) {
                fetchAuthToken();
            }

            if (isSingleton().matches()) {
                maxPages = 1;
                String gifDetailsURL = String.format(GIFS_DETAIL_ENDPOINT, getGID(url));
                return Http.url(gifDetailsURL).header("Authorization", "Bearer " + authToken).getJSON();
            } else if (isSearch().matches() || isTags().matches()) {
                var json = Http.url(getSearchOrTagsURL()).header("Authorization", "Bearer " + authToken).getJSON();
                maxPages = json.getInt("pages");
                return json;
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
            throw new IOException("Failed to build first page url", e);
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
            var sText = m.group(1);
            if (sText == null || sText.isBlank()) {
                throw new MalformedURLException(
                        String.format("Expected redgifs.com/search?query=searchtext\n Got %s", url));
            }
            sText = URLDecoder.decode(sText, StandardCharsets.UTF_8);
            sText = sText.replaceAll("[^A-Za-z0-9_-]", "-");
            return sText;
        }
        m = isTags();
        if (m.matches()) {
            var sText = m.group(1);
            if (sText == null || sText.isBlank()) {
                throw new MalformedURLException(String.format("Expected redgifs.com/gifs/searchtags\n Got %s", url));
            }
            sText = URLDecoder.decode(sText, StandardCharsets.UTF_8);
            var list = Arrays.asList(sText.split(","));
            if (list.size() > 1) {
                LOGGER.warn("Url with multiple tags found. \nThey will be sorted alphabetically for folder name.");
            }
            Collections.sort(list);
            var gid = list.stream().reduce("", (acc, val) -> acc.concat("_" + val));
            gid = gid.replaceAll("[^A-Za-z0-9_-]", "-");
            return gid;
        }
        m = isSingleton();
        if (m.matches()) {
            return m.group(1).split("-")[0];
        }
        throw new MalformedURLException(
                "Expected redgifs.com format: "
                        + "redgifs.com/watch/id or "
                        + "redgifs.com/users/id or "
                        + "redgifs.com/gifs/id or "
                        + "redgifs.com/search?query=text"
                        + " Got: " + url);
    }

    @Override
    public JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        if (currentPage == maxPages || isSingleton().matches()) {
            return null;
        }
        currentPage++;
        if (isSearch().matches() || isTags().matches()) {
            var json = Http.url(getSearchOrTagsURL()).header("Authorization", "Bearer " + authToken).getJSON();
            // Handle rare maxPages change during a rip
            maxPages = json.getInt("pages");
            return json;
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
        if (isProfile().matches() || isSearch().matches() || isTags().matches()) {
            var gifs = json.getJSONArray("gifs");
            for (var gif : gifs) {
                if (((JSONObject) gif).isNull("gallery")) {
                    var hdURL = ((JSONObject) gif).getJSONObject("urls").getString("hd");
                    result.add(hdURL);
                } else {
                    var galleryID = ((JSONObject) gif).getString("gallery");
                    var gifID = ((JSONObject) gif).getString("id");
                    result.addAll(getURLsForGallery(galleryID, gifID));
                }
            }
        } else {
            var gif = json.getJSONObject("gif");
            if (gif.isNull("gallery")) {
                String hdURL = gif.getJSONObject("urls").getString("hd");
                result.add(hdURL);
            } else {
                var galleryID = gif.getString("gallery");
                var gifID = gif.getString("id");
                result.addAll(getURLsForGallery(galleryID, gifID));
            }
        }
        return result;
    }

    /**
     * Get all images for a gif url with multiple images
     *
     * @param galleryID gallery id
     * @param gifID     gif id with multiple images for logging
     * @return List<String>
     */
    private static List<String> getURLsForGallery(String galleryID, String gifID) {
        List<String> list = new ArrayList<>();
        if (galleryID == null || galleryID.isBlank()) {
            return list;
        }
        try {
            var json = Http.url(String.format(GALLERY_ENDPOINT, galleryID))
                    .header("Authorization", "Bearer " + authToken).getJSON();
            for (var gif : json.getJSONArray("gifs")) {
                var hdURL = ((JSONObject) gif).getJSONObject("urls").getString("hd");
                list.add(hdURL);
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Error fetching gallery %s for gif %s", galleryID, gifID), e);
        }
        return list;
    }

    /**
     * Static helper method for retrieving video URLs for usage in RipUtils.
     * Most of the code is lifted from getFirstPage and getURLsFromJSON
     *
     * @param url URL to redgif page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException, URISyntaxException {
        LOGGER.info("Retrieving " + url.toExternalForm());
        var m = SINGLETON_PATTERN.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new IOException(String.format("Cannot fetch redgif url %s", url.toExternalForm()));
        }
        if (authToken == null || authToken.isBlank()) {
            fetchAuthToken();
        }
        var gid = m.group(1).split("-")[0];
        var gifDetailsURL = String.format(GIFS_DETAIL_ENDPOINT, gid);
        var json = Http.url(gifDetailsURL).header("Authorization", "Bearer " + authToken).getJSON();
        var gif = json.getJSONObject("gif");
        if (!gif.isNull("gallery")) {
            // TODO check how to handle a image gallery
            throw new IOException(String.format("Multiple images found for url %s", url));
        }
        return gif.getJSONObject("urls").getString("hd");
    }

    /**
     * Fetch a temorary auth token for the rip
     *
     * @throws IOException
     */
    private static void fetchAuthToken() throws IOException {
        var json = Http.url(TEMPORARY_AUTH_ENDPOINT).getJSON();
        var token = json.getString("token");
        authToken = token;
        LOGGER.info("Incase of redgif 401 errors, please restart the app to refresh the auth token");
    }

    /**
     * Map browser url query params to search or tags endpoint query params and
     * return the complete url.
     *
     * Search text for search url comes from the query params, whereas search text
     * for tags url comes from the path.
     *
     * Tab type for search url comes from the path whereas, tab type for tags url
     * comes from query params.
     *
     * @return Search or tags endpoint url
     */
    private URL getSearchOrTagsURL() throws IOException, URISyntaxException {
        URIBuilder uri;
        Map<String, String> endpointQueryParams = new HashMap<>();
        var browserURLQueryParams = new URIBuilder(url.toString()).getQueryParams();
        for (var qp : browserURLQueryParams) {
            var name = qp.getName();
            var value = qp.getValue();
            switch (name) {
                case "query":
                    endpointQueryParams.put("query", URLDecoder.decode(value, StandardCharsets.UTF_8));
                    break;
                case "tab":
                    switch (value) {
                        case "gifs" -> endpointQueryParams.put("type", "g");
                        case "images" -> endpointQueryParams.put("type", "i");
                        default -> LOGGER.warn(String.format("Unsupported tab for tags url %s", value));
                    }
                    break;
                case "verified":
                    if (value != null && value.equals("1")) {
                        if (isTags().matches()) {
                            endpointQueryParams.put("verified", "y");
                        } else {
                            endpointQueryParams.put("verified", "yes");
                        }
                    }
                    break;
                case "order":
                    endpointQueryParams.put("order", value);
                    break;
                case "viewMode":
                    break;
                default:
                    LOGGER.warn(String.format("Unexpected query param %s for search url. Skipping.", name));
            }
        }

        // Build the search or tags url and add missing query params if any
        if (isTags().matches()) {
            var subpaths = url.getPath().split("/");
            if (subpaths.length != 0) {
                endpointQueryParams.put("search_text", subpaths[subpaths.length - 1]);
            } else {
                throw new IOException("Failed to get search tags for url");
            }
            // Check if it is the main tags page with all gifs, images, creator etc
            if (!endpointQueryParams.containsKey("type")) {
                LOGGER.warn("No tab selected, defaulting to gifs");
                endpointQueryParams.put("type", "g");
            }
            uri = new URIBuilder(TAGS_ENDPOINT);
        } else {
            var tabType = "gifs";
            var subpaths = url.getPath().split("/");
            if (subpaths.length != 0) {
                switch (subpaths[subpaths.length - 1]) {
                    case "gifs" -> tabType = "gifs";
                    case "images" -> tabType = "images";
                    case "search" -> LOGGER.warn("No tab selected, defaulting to gifs");
                    default -> LOGGER.warn(String.format("Unsupported search tab %s, defaulting to gifs",
                            subpaths[subpaths.length - 1]));
                }
            }
            uri = new URIBuilder(String.format(SEARCH_ENDPOINT, tabType));
        }

        endpointQueryParams.put("page", Integer.toString(currentPage));
        endpointQueryParams.put("count", Integer.toString(count));
        endpointQueryParams.forEach((k, v) -> uri.addParameter(k, v));

        return uri.build().toURL();
    }
}
