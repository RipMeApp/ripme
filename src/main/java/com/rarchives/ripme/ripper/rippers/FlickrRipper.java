package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

public class FlickrRipper extends AbstractHTMLRipper {

    private int page = 1;
    private Set<String> attempted = new HashSet<>();
    private Document albumDoc = null;
    private final DownloadThreadPool flickrThreadPool;
    @Override
    public DownloadThreadPool getThreadPool() {
        return flickrThreadPool;
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

    private String getAPIKey(Document doc) {
//        Pattern p; Matcher m;
//        p = Pattern.compile("root.YUI_config.flickr.api.site_key = \"(\\S*)\"");
//        m = p.matcher(doc.body().html());
//        logger.info(doc.body().html());
//        return m.group(1);
        return "cd26dd7e6f904bbf63c4d1f9f013e76a";
    }

    private String apiURLBuilder(String photoset, String pageNumber, String apiKey) {
        logger.info("https://api.flickr.com/services/rest?extras=can_addmeta," +
                "can_comment,can_download,can_share,contact,count_comments,count_faves,count_views,date_taken," +
                "date_upload,icon_urls_deep,isfavorite,ispro,license,media,needs_interstitial,owner_name," +
                "owner_datecreate,path_alias,realname,rotation,safety_level,secret_k,secret_h,url_c,url_f,url_h,url_k," +
                "url_l,url_m,url_n,url_o,url_q,url_s,url_sq,url_t,url_z,visibility,visibility_source,o_dims," +
                "is_marketplace_printable,is_marketplace_licensable,publiceditability&per_page=100&page="+ pageNumber + "&" +
                "get_user_info=1&primary_photo_extras=url_c,%20url_h,%20url_k,%20url_l,%20url_m,%20url_n,%20url_o" +
                ",%20url_q,%20url_s,%20url_sq,%20url_t,%20url_z,%20needs_interstitial,%20can_share&jump_to=&" +
                "photoset_id=" + photoset + "&viewerNSID=&method=flickr.photosets.getPhotos&csrf=&" +
                "api_key=" + apiKey + "&format=json&hermes=1&hermesClient=1&reqId=358ed6a0&nojsoncallback=1");
        return "https://api.flickr.com/services/rest?extras=can_addmeta," +
                "can_comment,can_download,can_share,contact,count_comments,count_faves,count_views,date_taken," +
                "date_upload,icon_urls_deep,isfavorite,ispro,license,media,needs_interstitial,owner_name," +
                "owner_datecreate,path_alias,realname,rotation,safety_level,secret_k,secret_h,url_c,url_f,url_h,url_k," +
                "url_l,url_m,url_n,url_o,url_q,url_s,url_sq,url_t,url_z,visibility,visibility_source,o_dims," +
                "is_marketplace_printable,is_marketplace_licensable,publiceditability&per_page=100&page="+ pageNumber + "&" +
                "get_user_info=1&primary_photo_extras=url_c,%20url_h,%20url_k,%20url_l,%20url_m,%20url_n,%20url_o" +
                ",%20url_q,%20url_s,%20url_sq,%20url_t,%20url_z,%20needs_interstitial,%20can_share&jump_to=&" +
                "photoset_id=" + photoset + "&viewerNSID=&method=flickr.photosets.getPhotos&csrf=&" +
                "api_key=" + apiKey + "&format=json&hermes=1&hermesClient=1&reqId=358ed6a0&nojsoncallback=1";
    }

    private JSONObject getJSON(String page, String apiKey) {
        URL pageURL = null;
        String apiURL = null;
        try {
             apiURL = apiURLBuilder(getPhotosetID(url.toExternalForm()), page, apiKey);
            pageURL = new URL(apiURL);
        }  catch (MalformedURLException e) {
            logger.error("Unable to get api link " + apiURL + " is malformed");
        }
        try {
            logger.info(Http.url(pageURL).ignoreContentType().get().text());
            return new JSONObject(Http.url(pageURL).ignoreContentType().get().text());
        } catch (IOException e) {
            logger.error("Unable to get api link " + apiURL + " is malformed");
            return null;
        }
    }

    private String getPhotosetID(String url) {
        Pattern p; Matcher m;

        // Root:  https://www.flickr.com/photos/115858035@N04/
        // Album: https://www.flickr.com/photos/115858035@N04/sets/72157644042355643/

        final String domainRegex = "https?://[wm.]*flickr.com";
        final String userRegex = "[a-zA-Z0-9@]+";
        // Album
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ")/sets/([0-9]+)/?.*$");
        m = p.matcher(url);
        if (m.matches()) {
            return m.group(2);
        }

        // User page
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ").*$");
        m = p.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }

        // Groups page
        p = Pattern.compile("^" + domainRegex + "/groups/(" + userRegex + ").*$");
        m = p.matcher(url);
        if (m.matches()) {
            return "groups-" + m.group(1);
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
        final String userRegex = "[a-zA-Z0-9@]+";
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

//    @Override
//    public Document getNextPage(Document doc) throws IOException {
//        if (isThisATest()) {
//            return null;
//        }
//        // Find how many pages there are
//        int lastPage = 0;
//        for (Element apage : doc.select("a[data-track^=page-]")) {
//            String lastPageStr = apage.attr("data-track").replace("page-", "");
//            lastPage = Integer.parseInt(lastPageStr);
//        }
//        // If we're at the last page, stop.
//        if (page >= lastPage) {
//            throw new IOException("No more pages");
//        }
//        // Load the next page
//        page++;
//        albumDoc = null;
//        String nextURL = this.url.toExternalForm();
//        if (!nextURL.endsWith("/")) {
//            nextURL += "/";
//        }
//        nextURL += "page" + page + "/";
//        // Wait a bit
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            throw new IOException("Interrupted while waiting to load next page " + nextURL);
//        }
//        return Http.url(nextURL).get();
//    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();

        int x = 1;
        while (true) {
            JSONObject jsonData = getJSON(String.valueOf(x), getAPIKey(doc));
            if (jsonData.has("stat") && jsonData.getString("stat") == "fail") {
                break;
            } else {
                JSONArray pictures = jsonData.getJSONObject("photoset").getJSONArray("photo");
                for (int i = 0; i < pictures.length(); i++) {
                    JSONObject data = (JSONObject) pictures.get(i);
                    // flickr has a real funny way listing the image sizes, so we have to loop over all these until we
                    // find one that works
                    List<String> imageSizes = Arrays.asList("k", "h", "l", "n", "c", "z", "t");
                    for ( String imageSize : imageSizes) {
                        try {
                            addURLToDownload(new URL(data.getString("url_" + imageSize)));
                            logger.info("Adding picture " + data.getString("url_" + imageSize));
                            break;
                        } catch (org.json.JSONException ignore) {

                        } catch (MalformedURLException e) {}
                    }
                }
            }
        }

        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}