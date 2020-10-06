package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ui.RipStatusMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Element;

public class FlickrRipper extends AbstractHTMLRipper {

    private Document albumDoc = null;
    private final DownloadThreadPool flickrThreadPool;

    // Map from image URL to photo metadata
    private Map<String, JSONObject> downloadedPhotoMetadata = new HashMap<String, JSONObject>();

    private enum UrlType {
        USER,
        PHOTOSET
    }

    private class Album {
        final UrlType type;
        final String id;

        Album(UrlType type, String id) {
            this.type = type;
            this.id = id;
        }
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return flickrThreadPool;
    }

    @Override
    public boolean hasASAPRipping() {
        return true;
    }

    @Override
    protected boolean hasDescriptionSupport() {
        return true;
    }

    public FlickrRipper(URL url) throws IOException {
        super(url);
        flickrThreadPool = new DownloadThreadPool();
    }

    @Override
    public String getHost() {
        return "flickr";
    }
    @Override
    public String getDomain() {
        return "flickr.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String sUrl = url.toExternalForm();
        // Strip out https
        sUrl = sUrl.replace("https://secure.flickr.com", "http://www.flickr.com");
        // For /groups/ links, add a /pool to the end of the URL
        if (sUrl.contains("flickr.com/groups/") && !sUrl.contains("/pool")) {
            if (!sUrl.endsWith("/")) {
                sUrl += "/";
            }
            sUrl += "pool";
        }
        return new URL(sUrl);
    }
    // FLickr is one of those sites what includes a api key in sites javascript
    // TODO let the user provide their own api key
    private String getAPIKey(Document doc) {
        Pattern p;
        Matcher m;
        p = Pattern.compile("root.YUI_config.flickr.api.site_key = \"([a-zA-Z0-9]*)\";");
        for (Element e : doc.select("script")) {
            // You have to use .html here as .text will strip most of the javascript
            m = p.matcher(e.html());
            if (m.find()) {
                LOGGER.info("Found api key:" + m.group(1));
                return m.group(1);
            }
        }
        LOGGER.error("Unable to get api key");
        // A nice error message to tell our users what went wrong
        sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Unable to extract api key from flickr");
        sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Using hardcoded api key");
        return "935649baf09b2cc50628e2b306e4da5d";
    }

    // The flickr api is a monster of weird settings so we just request everything that the webview does
    private String apiURLBuilder(String photoset, int pageNumber, String apiKey) {
        LOGGER.info("https://api.flickr.com/services/rest?extras=can_addmeta," +
                "can_comment,can_download,can_share,contact,count_comments,count_faves,count_views,date_taken," +
                "date_upload,description,icon_urls_deep,isfavorite,ispro,license,media,needs_interstitial,owner_name," +
                "owner_datecreate,path_alias,realname,rotation,safety_level,secret_k,secret_h,url_c,url_f,url_h,url_k," +
                "url_l,url_m,url_n,url_o,url_q,url_s,url_sq,url_t,url_z,visibility,visibility_source,o_dims," +
                "is_marketplace_printable,is_marketplace_licensable,publiceditability&per_page=100&page="+ String.valueOf(pageNumber) + "&" +
                "get_user_info=1&primary_photo_extras=url_c,%20url_h,%20url_k,%20url_l,%20url_m,%20url_n,%20url_o" +
                ",%20url_q,%20url_s,%20url_sq,%20url_t,%20url_z,%20needs_interstitial,%20can_share&jump_to=&" +
                "photoset_id=" + photoset + "&viewerNSID=&method=flickr.photosets.getPhotos&csrf=&" +
                "api_key=" + apiKey + "&format=json&hermes=1&hermesClient=1&reqId=358ed6a0&nojsoncallback=1");
        return "https://api.flickr.com/services/rest?extras=can_addmeta," +
                "can_comment,can_download,can_share,contact,count_comments,count_faves,count_views,date_taken," +
                "date_upload,description,icon_urls_deep,isfavorite,ispro,license,media,needs_interstitial,owner_name," +
                "owner_datecreate,path_alias,realname,rotation,safety_level,secret_k,secret_h,url_c,url_f,url_h,url_k," +
                "url_l,url_m,url_n,url_o,url_q,url_s,url_sq,url_t,url_z,visibility,visibility_source,o_dims," +
                "is_marketplace_printable,is_marketplace_licensable,publiceditability&per_page=100&page="+ String.valueOf(pageNumber) + "&" +
                "get_user_info=1&primary_photo_extras=url_c,%20url_h,%20url_k,%20url_l,%20url_m,%20url_n,%20url_o" +
                ",%20url_q,%20url_s,%20url_sq,%20url_t,%20url_z,%20needs_interstitial,%20can_share&jump_to=&" +
                "photoset_id=" + photoset + "&viewerNSID=&method=flickr.photosets.getPhotos&csrf=&" +
                "api_key=" + apiKey + "&format=json&hermes=1&hermesClient=1&reqId=358ed6a0&nojsoncallback=1";
    }

    private JSONObject getJSON(int pageNumber, String apiKey) {
        URL pageURL = null;
        String apiURL = null;
        try {
            apiURL = apiURLBuilder(getPhotosetID(url.toExternalForm()), pageNumber, apiKey);
            pageURL = new URL(apiURL);
        }  catch (MalformedURLException e) {
            LOGGER.error("Unable to get api link " + apiURL + " is malformed");
        }
        try {
            return Http.url(pageURL).getJSON();
        } catch (IOException e) {
            LOGGER.error("Unable to get api link " + apiURL + " is malformed");
            return null;
        }
    }

    private String getPhotosetID(String url) {
        Pattern p; Matcher m;

        // Root:  https://www.flickr.com/photos/115858035@N04/
        // Album: https://www.flickr.com/photos/115858035@N04/sets/72157644042355643/

        final String domainRegex = "https?://[wm.]*flickr.com";
        final String userRegex = "[a-zA-Z0-9@_-]+";
        // Album
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ")/(sets|albums)/([0-9]+)/?.*$");
        m = p.matcher(url);
        if (m.matches()) {
            return m.group(3);
        }
        return null;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        if (!url.toExternalForm().contains("/sets/")) {
            return super.getAlbumTitle(url);
        }
        try {
            // Attempt to use album title as GID
            Document doc = getFirstPage();
            String user = url.toExternalForm();
            user = user.substring(user.indexOf("/photos/") + "/photos/".length());
            user = user.substring(0, user.indexOf("/"));
            String title = doc.select("meta[name=description]").get(0).attr("content");
            if (!title.equals("")) {
                return getHost() + "_" + user + "_" + title;
            }
        } catch (Exception e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        // Root:  https://www.flickr.com/photos/115858035@N04/
        // Album: https://www.flickr.com/photos/115858035@N04/sets/72157644042355643/

        final String domainRegex = "https?://[wm.]*flickr.com";
        final String userRegex = "[a-zA-Z0-9@_-]+";
        // Album
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ")/sets/([0-9]+)/?.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }

        // User page
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ").*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        // Groups page
        p = Pattern.compile("^" + domainRegex + "/groups/(" + userRegex + ").*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "groups-" + m.group(1);
        }
        throw new MalformedURLException(
                "Expected flickr.com URL formats: "
                        + "flickr.com/photos/username or "
                        + "flickr.com/photos/username/sets/albumid"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        String apiKey = getAPIKey(doc);
        int x = 1;
        int photoIndex = 0;
        while (true) {
            JSONObject jsonData = getJSON(x, apiKey);
            if (jsonData.has("stat") && jsonData.getString("stat").equals("fail")) {
                break;
            } else {
                int totalPages = jsonData.getJSONObject("photoset").getInt("pages");
                LOGGER.info(jsonData);
                JSONArray pictures = jsonData.getJSONObject("photoset").getJSONArray("photo");
                for (int i = 0; i < pictures.length(); i++) {
                    LOGGER.info(i);
                    JSONObject data = (JSONObject) pictures.get(i);
                    try {
                        URL imageUrl = getLargestImageURL(data.getString("id"), apiKey);
                        addURLToDownload(imageUrl, getPrefix(photoIndex));

                        downloadedPhotoMetadata.put(imageUrl.toExternalForm(), data);

                        photoIndex++;
                    } catch (MalformedURLException e) {
                        LOGGER.error("Flickr MalformedURLException: " + e.getMessage());
                    }
                }
                if (x >= totalPages) {
                    // The rips done
                    break;
                }
                // We have more pages to download so we rerun the loop
                x++;

            }
        }

        return imageURLs;
    }

    @Override
    protected List<String> getDescriptionsFromPage(Document doc) {
        return new ArrayList<String>(downloadedPhotoMetadata.keySet());
    }

    @Override
    protected String[] getDescription(String url, Document page) {
        JSONObject photoMetadata = downloadedPhotoMetadata.get(url);

        if (photoMetadata == null) {
            return null;
        }

        String description = photoMetadata.getString("title") + "\n" + photoMetadata.getJSONObject("description").getString("_content");

        return new String[] { description };
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private URL getLargestImageURL(String imageID, String apiKey) throws MalformedURLException {
        TreeMap<Integer, String> imageURLMap = new TreeMap<>();

        try {
            URL imageAPIURL = new URL("https://www.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=" + apiKey + "&photo_id=" + imageID + "&format=json&nojsoncallback=1");
            JSONArray imageSizes = Http.url(imageAPIURL).getJSON().getJSONObject("sizes").getJSONArray("size");
            for (int i = 0; i < imageSizes.length(); i++) {
                JSONObject imageInfo = imageSizes.getJSONObject(i);
                imageURLMap.put(imageInfo.getInt("width") * imageInfo.getInt("height"), imageInfo.getString("source"));
            }
        } catch (org.json.JSONException e) {
            LOGGER.error("Error in parsing of Flickr API: " + e.getMessage());
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL returned by API");
        } catch (IOException e) {
            LOGGER.error("IOException while looking at image sizes: " + e.getMessage());
        }

        Map.Entry<Integer, String> entry = imageURLMap.lastEntry();

        if (entry == null) {
            return null;
        }

        return new URL(entry.getValue());
    }
}
