package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DeviantartRipper extends AbstractJSONRipper {
    String requestID;
    String galleryID;
    String username;
    String baseApiUrl = "https://www.deviantart.com/dapi/v1/gallery/";
    String csrf;
    Map<String, String> pageCookies = new HashMap<>();

    private static final int PAGE_SLEEP_TIME  = 3000,
                             IMAGE_SLEEP_TIME = 2000;

    private Map<String,String> cookies = new HashMap<>();
    private Set<String> triedURLs = new HashSet<>();

    public DeviantartRipper(URL url) throws IOException {
        super(url);
    }

    String loginCookies = "auth=__0f9158aaec09f417b235%3B%221ff79836392a515d154216d919eae573%22;" +
            "auth_secure=__41d14dd0da101f411bb0%3B%2281cf2cf9477776162a1172543aae85ce%22;" +
            "userinfo=__bf84ac233bfa8ae642e8%3B%7B%22username%22%3A%22grabpy%22%2C%22uniqueid%22%3A%22a0a876aa37dbd4b30e1c80406ee9c280%22%2C%22vd%22%3A%22BbHUXZ%2CBbHUXZ%2CA%2CU%2CA%2C%2CB%2CA%2CB%2CBbHUXZ%2CBbHUdj%2CL%2CL%2CA%2CBbHUdj%2C13%2CA%2CB%2CA%2C%2CA%2CA%2CB%2CA%2CA%2C%2CA%22%2C%22attr%22%3A56%7D";

    @Override
    public String getHost() {
        return "deviantart";
    }

    @Override
    public String getDomain() {
        return "deviantart.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.contains("/gallery/")) {
            return url;
        }

        if (!u.endsWith("/gallery/") && !u.endsWith("/gallery")) {
            if (!u.endsWith("/")) {
                u += "/gallery/";
            } else {
                u += "gallery/";
            }
        }


        Pattern p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)/favou?rites/([0-9]+)/*?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            String subdir = "/";
            if (u.contains("catpath=scraps")) {
                subdir = "scraps";
            }
            u = u.replaceAll("\\?.*", "?catpath=" + subdir);
        }
        return new URL(u);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)(/gallery)?/?(\\?.*)?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Root gallery
            if (url.toExternalForm().contains("catpath=scraps")) {
                return m.group(1) + "_scraps";
            }
            else {
                return m.group(1);
            }
        }
        p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)/gallery/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)/favou?rites/([0-9]+)/.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_faves_" + m.group(2);
        }
        p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)/favou?rites/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_faves";
        }
        throw new MalformedURLException("Expected URL format: http://www.deviantart.com/username[/gallery/#####], got: " + url);
    }

    private String getUsernameFromURL(String u) {
        Pattern p = Pattern.compile("^https?://www\\.deviantart\\.com/([a-zA-Z0-9\\-]+)/gallery/?(\\S+)?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        return null;

    }

    private String getFullsizedNSFWImage(String pageURL) {
        try {
            Document doc = Http.url(pageURL).cookies(cookies).get();
            String imageToReturn = "";
            String[] d = doc.select("img").attr("srcset").split(",");

            String s = d[d.length -1].split(" ")[0];
            LOGGER.info("2:" + s);

            if (s == null || s.equals("")) {
                LOGGER.error("Could not find full sized image at " + pageURL);
            }
            return s;
        } catch (IOException e) {
            LOGGER.error("Could not find full sized image at " + pageURL);
            return null;
        }
    }

    /**
     * Gets first page.
     * Will determine if login is supplied,
     * if there is a login, then login and add that login cookies.
     * Otherwise, just bypass the age gate with an anonymous flag.
     * @return
     * @throws IOException 
     */
    @Override
    public JSONObject getFirstPage() throws IOException {
        
        // Base64 da login
        // username: Z3JhYnB5
        // password: ZmFrZXJz


        cookies = getDACookies();
            if (cookies.isEmpty()) {
                LOGGER.warn("Failed to get login cookies");
                cookies.put("agegate_state","1"); // Bypasses the age gate
            }
        cookies.put("agegate_state", "1");
            
        Response res = Http.url(this.url)
                   .cookies(cookies)
                   .response();
        Document page = res.parse();

        JSONObject firstPageJSON = getFirstPageJSON(page);
        requestID = firstPageJSON.getJSONObject("dapx").getString("requestid");
        galleryID = getGalleryID(page);
        username = getUsernameFromURL(url.toExternalForm());
        csrf = firstPageJSON.getString("csrf");
        pageCookies = res.cookies();

        return requestPage(0, galleryID, username, requestID, csrf, pageCookies);
    }

    private JSONObject requestPage(int offset, String galleryID, String username, String requestID, String csfr, Map<String, String> c) {
        LOGGER.debug("offset: " + Integer.toString(offset));
        LOGGER.debug("galleryID: " + galleryID);
        LOGGER.debug("username: " + username);
        LOGGER.debug("requestID: " + requestID);
        String url = baseApiUrl + galleryID + "?iid=" + requestID;
        try {
            Document doc = Http.url(url).cookies(c).data("username", username).data("offset", Integer.toString(offset))
                    .data("limit", "24").data("_csrf", csfr).data("id", requestID)
                    .ignoreContentType().post();
            return new JSONObject(doc.body().text());
        } catch (IOException e) {
            LOGGER.error("Got error trying to get page: " + e.getMessage());
            e.printStackTrace();
            return null;
        }


    }

    private JSONObject getFirstPageJSON(Document doc) {
        for (Element js : doc.select("script")) {
            if (js.html().contains("requestid")) {
                String json = js.html().replaceAll("window.__initial_body_data=", "").replaceAll("\\);", "")
                        .replaceAll(";__wake\\(.+", "");
                LOGGER.info("json: " + json);
                JSONObject j = new JSONObject(json);
                return j;
            }
        }
        return null;
    }

    public String getGalleryID(Document doc) {
        // If the url contains catpath we return 0 as the DA api will provide all galery images if you sent the
        // gallery id to 0
        if (url.toExternalForm().contains("catpath=")) {
            return "0";
        }
        Pattern p = Pattern.compile("^https?://www\\.deviantart\\.com/[a-zA-Z0-9\\-]+/gallery/([0-9]+)/?\\S+");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        for (Element el : doc.select("input[name=set]")) {
            try {
                String galleryID = el.attr("value");
                return galleryID;
            } catch (NullPointerException e) {
                continue;
            }
        }
        LOGGER.error("Could not find gallery ID");
        return null;
    }

    public String getUsername(Document doc) {
        return doc.select("meta[property=og:title]").attr("content")
                .replaceAll("'s DeviantArt gallery", "").replaceAll("'s DeviantArt Gallery", "");
    }
    

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> imageURLs = new ArrayList<>();
        LOGGER.info(json);
        JSONArray results = json.getJSONObject("content").getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            Document doc = Jsoup.parseBodyFragment(results.getJSONObject(i).getString("html"));
            if (doc.html().contains("ismature")) {
                LOGGER.info("Downloading nsfw image");
                String nsfwImage = getFullsizedNSFWImage(doc.select("span").attr("href"));
                if (nsfwImage != null && nsfwImage.startsWith("http")) {
                    imageURLs.add(nsfwImage);
                }
            }
            try {
                String imageURL = doc.select("span").first().attr("data-super-full-img");
                if (!imageURL.isEmpty() && imageURL.startsWith("http")) {
                    imageURLs.add(imageURL);
                }
            } catch (NullPointerException e) {
               LOGGER.info(i + " does not contain any images");
            }

        }
        return imageURLs;
    }


    @Override
    public JSONObject getNextPage(JSONObject page) throws IOException {
        boolean hasMore = page.getJSONObject("content").getBoolean("has_more");
        if (hasMore) {
            return requestPage(page.getJSONObject("content").getInt("next_offset"), galleryID, username, requestID, csrf, pageCookies);
        }

        throw new IOException("No more pages");
    }

    @Override
    public boolean keepSortOrder() {
         // Don't keep sort order (do not add prefixes).
         // Causes file duplication, as outlined in https://github.com/4pr0n/ripme/issues/113
        return false;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
        sleep(IMAGE_SLEEP_TIME);
    }

    /**
     * Tries to get full size image from thumbnail URL
     * @param thumb Thumbnail URL
     * @param throwException Whether or not to throw exception when full size image isn't found
     * @return Full-size image URL
     * @throws Exception If it can't find the full-size URL
     */
    private static String thumbToFull(String thumb, boolean throwException) throws Exception {
        thumb = thumb.replace("http://th", "http://fc");
        List<String> fields = new ArrayList<>(Arrays.asList(thumb.split("/")));
        fields.remove(4);
        if (!fields.get(4).equals("f") && throwException) {
            // Not a full-size image
            throw new Exception("Can't get full size image from " + thumb);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                result.append("/");
            }
            result.append(fields.get(i));
        }
        return result.toString();
    }



    /**
     * If largest resolution for image at 'thumb' is found, starts downloading
     * and returns null.
     * If it finds a larger resolution on another page, returns the image URL.
     * @param thumb Thumbnail URL
     * @param page Page the thumbnail is retrieved from
     * @return Highest-resolution version of the image based on thumbnail URL and the page.
     */
    private String smallToFull(String thumb, String page) {
        try {
            // Fetch the image page
            Response resp = Http.url(page)
                                .referrer(this.url)
                                .cookies(cookies)
                                .response();
            cookies.putAll(resp.cookies());
            Document doc = resp.parse();
            Elements els = doc.select("img.dev-content-full");
            String fsimage = null;
            // Get the largest resolution image on the page
            if (!els.isEmpty()) {
                // Large image
                fsimage = els.get(0).attr("src");
                LOGGER.info("Found large-scale: " + fsimage);
                if (fsimage.contains("//orig")) {
                    return fsimage;
                }
            }
            // Try to find the download button
            els = doc.select("a.dev-page-download");
            if (!els.isEmpty()) {
                // Full-size image
                String downloadLink = els.get(0).attr("href");
                LOGGER.info("Found download button link: " + downloadLink);
                HttpURLConnection con = (HttpURLConnection) new URL(downloadLink).openConnection();
                con.setRequestProperty("Referer",this.url.toString());
                String cookieString = "";
                for (Map.Entry<String, String> entry : cookies.entrySet()) {
                    cookieString = cookieString + entry.getKey() + "=" + entry.getValue() + "; ";
                }
                cookieString = cookieString.substring(0,cookieString.length() - 1);
                con.setRequestProperty("Cookie",cookieString);
                con.setRequestProperty("User-Agent", USER_AGENT);
                con.setInstanceFollowRedirects(true);
                con.connect();
                int code = con.getResponseCode();
                String location = con.getURL().toString();
                con.disconnect();
                if (location.contains("//orig")) {
                    fsimage = location;
                    LOGGER.info("Found image download: " + location);
                }
            }
            if (fsimage != null) {
                return fsimage;
            }
            throw new IOException("No download page found");
        } catch (IOException ioe) {
            try {
                LOGGER.info("Failed to get full size download image at " + page + " : '" + ioe.getMessage() + "'");
                String lessThanFull = thumbToFull(thumb, false);
                LOGGER.info("Falling back to less-than-full-size image " + lessThanFull);
                return lessThanFull;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Returns DA cookies.
     * @return Map of cookies containing session data.
     */
    private Map<String, String> getDACookies() {
        return RipUtils.getCookiesFromString(Utils.getConfigString("deviantart.cookies", loginCookies));
    }
}