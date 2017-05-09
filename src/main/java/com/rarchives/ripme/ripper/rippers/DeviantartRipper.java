package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
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

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class DeviantartRipper extends AbstractHTMLRipper {

    private static final int PAGE_SLEEP_TIME  = 3000,
                             IMAGE_SLEEP_TIME = 2000;

    private Map<String,String> cookies = new HashMap<String,String>();
    private Set<String> triedURLs = new HashSet<String>();

    public DeviantartRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "deviantart";
    }
    @Override
    public String getDomain() {
        return "deviantart.com";
    }
    @Override
    public boolean hasDescriptionSupport() {
        return true;
    }
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();

        if (u.replace("/", "").endsWith(".deviantart.com")) {
            // Root user page, get all albums
            if (!u.endsWith("/")) {
                u += "/";
            }
            u += "gallery/?";
        }

        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com/favou?rites/([0-9]+)/*?$");
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
        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9\\-]+)\\.deviantart\\.com(/gallery)?/?(\\?.*)?$");
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
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com/gallery/([0-9]{1,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com/favou?rites/([0-9]+)/.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_faves_" + m.group(2);
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com/favou?rites/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_faves";
        }
        throw new MalformedURLException("Expected URL format: http://username.deviantart.com/[/gallery/#####], got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        // Login
        try {
            cookies = loginToDeviantart();
        } catch (Exception e) {
            logger.warn("Failed to login: ", e);
        }
        return Http.url(this.url)
                   .cookies(cookies)
                   .get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();

        // Iterate over all thumbnails
        for (Element thumb : page.select("div.zones-container a.thumb")) {
            if (isStopped()) {
                break;
            }
            Element img = thumb.select("img").get(0);
            if (img.attr("transparent").equals("false")) {
                continue; // a.thumbs to other albums are invisible
            }

            // Get full-sized image via helper methods
            String fullSize = null;
            try {
                fullSize = thumbToFull(img.attr("src"), true);
            } catch (Exception e) {
                logger.info("Attempting to get full size image from " + thumb.attr("href"));
                fullSize = smallToFull(img.attr("src"), thumb.attr("href"));
            }
            if (fullSize == null) {
                continue;
            }
            if (triedURLs.contains(fullSize)) {
                logger.warn("Already tried to download " + fullSize);
                continue;
            }
            triedURLs.add(fullSize);
            imageURLs.add(fullSize);

            if (isThisATest()) {
                // Only need one image for a test
                break;
            }
        }
        return imageURLs;
    }
    @Override
    public List<String> getDescriptionsFromPage(Document page) {
        List<String> textURLs = new ArrayList<String>();

        // Iterate over all thumbnails
        for (Element thumb : page.select("div.zones-container a.thumb")) {
            if (isStopped()) {
                break;
            }
            Element img = thumb.select("img").get(0);
            if (img.attr("transparent").equals("false")) {
                continue; // a.thumbs to other albums are invisible
            }
            textURLs.add(thumb.attr("href"));
        }
        return textURLs;
    }
    @Override
    public Document getNextPage(Document page) throws IOException {
        if (isThisATest()) {
            return null;
        }
        Elements nextButtons = page.select("li.next > a");
        if (nextButtons.size() == 0) {
            throw new IOException("No next page found");
        }
        Element a = nextButtons.first();
        if (a.hasClass("disabled")) {
            throw new IOException("Hit end of pages");
        }
        String nextPage = a.attr("href");
        if (nextPage.startsWith("/")) {
            nextPage = "http://" + this.url.getHost() + nextPage;
        }
        if (!sleep(PAGE_SLEEP_TIME)) {
            throw new IOException("Interrupted while waiting to load next page: " + nextPage);
        }
        logger.info("Found next page: " + nextPage);
        return Http.url(nextPage)
                   .cookies(cookies)
                   .get();
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
    public static String thumbToFull(String thumb, boolean throwException) throws Exception {
        thumb = thumb.replace("http://th", "http://fc");
        List<String> fields = new ArrayList<String>(Arrays.asList(thumb.split("/")));
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
     * Attempts to download description for image.
     * Comes in handy when people put entire stories in their description.
     * If no description was found, returns null.
     * @param page The page the description will be retrieved from
     * @return The description
     */
    @Override
    public String getDescription(String page) {
        if (isThisATest()) {
            return null;
        }
        try {
            // Fetch the image page
            Response resp = Http.url(page)
                                .referrer(this.url)
                                .cookies(cookies)
                                .response();
            cookies.putAll(resp.cookies());

            // Try to find the description
            Elements els = resp.parse().select("div[class=dev-description]");
            if (els.size() == 0) {
                throw new IOException("No description found");
            }
            Document documentz = resp.parse();
            Element ele = documentz.select("div[class=dev-description]").get(0);
            documentz.outputSettings(new Document.OutputSettings().prettyPrint(false));
            ele.select("br").append("\\n");
            ele.select("p").prepend("\\n\\n");
            return Jsoup.clean(ele.html().replaceAll("\\\\n", System.getProperty("line.separator")), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
            // TODO Make this not make a newline if someone just types \n into the description.
        } catch (IOException ioe) {
                logger.info("Failed to get description " + page + " : '" + ioe.getMessage() + "'");
                return null;
        }
    }

    /**
     * If largest resolution for image at 'thumb' is found, starts downloading
     * and returns null.
     * If it finds a larger resolution on another page, returns the image URL.
     * @param thumb Thumbnail URL
     * @param page Page the thumbnail is retrieved from
     * @return Highest-resolution version of the image based on thumbnail URL and the page.
     */
    public String smallToFull(String thumb, String page) {
        try {
            // Fetch the image page
            Response resp = Http.url(page)
                                .referrer(this.url)
                                .cookies(cookies)
                                .response();
            cookies.putAll(resp.cookies());

            // Try to find the download button
            Document doc = resp.parse();
            Elements els = doc.select("a.dev-page-download");
            if (els.size() > 0) {
                // Full-size image
                String fsimage = els.get(0).attr("href");
                logger.info("Found download page: " + fsimage);
                return fsimage;
            }

            // Get the largest resolution image on the page
            els = doc.select("img.dev-content-full");
            if (els.size() > 0) {
                // Large image
                String fsimage = els.get(0).attr("src");
                logger.info("Found large-scale: " + fsimage);
                return fsimage;
            }
            throw new IOException("No download page found");
        } catch (IOException ioe) {
            try {
                logger.info("Failed to get full size download image at " + page + " : '" + ioe.getMessage() + "'");
                String lessThanFull = thumbToFull(thumb, false);
                logger.info("Falling back to less-than-full-size image " + lessThanFull);
                return lessThanFull;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Logs into deviant art. Required to rip full-size NSFW content.
     * @return Map of cookies containing session data.
     */
    private Map<String, String> loginToDeviantart() throws IOException {
        // Populate postData fields
        Map<String,String> postData = new HashMap<String,String>();
        String username = Utils.getConfigString("deviantart.username", new String(Base64.decode("Z3JhYnB5")));
        String password = Utils.getConfigString("deviantart.password", new String(Base64.decode("ZmFrZXJz")));
        if (username == null || password == null) {
            throw new IOException("could not find username or password in config");
        }
        Response resp = Http.url("http://www.deviantart.com/")
                            .response();
        for (Element input : resp.parse().select("form#form-login input[type=hidden]")) {
            postData.put(input.attr("name"), input.attr("value"));
        }
        postData.put("username", username);
        postData.put("password", password);
        postData.put("remember_me", "1");

        // Send login request
        resp = Http.url("https://www.deviantart.com/users/login")
                    .userAgent(USER_AGENT)
                    .data(postData)
                    .cookies(resp.cookies())
                    .method(Method.POST)
                    .response();

        // Assert we are logged in
        if (resp.hasHeader("Location") && resp.header("Location").contains("password")) {
            // Wrong password
            throw new IOException("Wrong password");
        }
        if (resp.url().toExternalForm().contains("bad_form")) {
            throw new IOException("Login form was incorrectly submitted");
        }
        if (resp.cookie("auth_secure") == null ||
            resp.cookie("auth") == null) {
            throw new IOException("No auth_secure or auth cookies received");
        }
        // We are logged in, save the cookies
        return resp.cookies();
    }
}
