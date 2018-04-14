package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;
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
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class DeviantartRipper extends AbstractHTMLRipper {

    private static final int PAGE_SLEEP_TIME  = 3000,
                             IMAGE_SLEEP_TIME = 2000;

    private Map<String,String> cookies = new HashMap<>();
    private Set<String> triedURLs = new HashSet<>();

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

        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9\\-]+)\\.deviantart\\.com/favou?rites/([0-9]+)/*?$");
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
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]+)\\.deviantart\\.com/gallery/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]+)\\.deviantart\\.com/favou?rites/([0-9]+)/.*?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_faves_" + m.group(2);
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]+)\\.deviantart\\.com/favou?rites/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_faves";
        }
        throw new MalformedURLException("Expected URL format: http://username.deviantart.com/[/gallery/#####], got: " + url);
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
    public Document getFirstPage() throws IOException {
        
        //Test to see if there is a login:
        String username = Utils.getConfigString("deviantart.username", new String(Base64.decode("Z3JhYnB5")));
        String password = Utils.getConfigString("deviantart.password", new String(Base64.decode("ZmFrZXJz")));
        
        if(username == null || password == null) {
            logger.debug("No DeviantArt login provided.");
            cookies.put("agegate_state","1"); // Bypasses the age gate
        } else {
            // Attempt Login
            try {
                cookies = loginToDeviantart();
            } catch (IOException e) {
                logger.warn("Failed to login: ", e);
                cookies.put("agegate_state","1"); // Bypasses the age gate
            }
        }
        
        
        return Http.url(this.url)
                   .cookies(cookies)
                   .get();
    }
    
    /**
     * 
     * @param page
     * @param id
     * @return 
     */
    private String jsonToImage(Document page, String id) {
        Elements js = page.select("script[type=\"text/javascript\"]");
        for (Element tag : js) {
            if (tag.html().contains("window.__pageload")) {
                try {
                    String script = tag.html();
                    script = script.substring(script.indexOf("window.__pageload"));
                    if (!script.contains(id)) {
                        continue;
                    }
                    script = script.substring(script.indexOf(id));
                    // first },"src":"url" after id
                    script = script.substring(script.indexOf("},\"src\":\"") + 9, script.indexOf("\",\"type\""));
                    return script.replace("\\/", "/");
                } catch (StringIndexOutOfBoundsException e) {
                    logger.debug("Unable to get json link from " + page.location());
                }
            }
        }
        return null;
    }
    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();

        // Iterate over all thumbnails
        for (Element thumb : page.select("div.zones-container span.thumb")) {
            if (isStopped()) {
                break;
            }
            Element img = thumb.select("img").get(0);
            if (img.attr("transparent").equals("false")) {
                continue; // a.thumbs to other albums are invisible
            }
            // Get full-sized image via helper methods
            String fullSize = null;
            if (thumb.attr("data-super-full-img").contains("//orig")) {
                fullSize = thumb.attr("data-super-full-img");
            } else {
                String spanUrl = thumb.attr("href");
                String fullSize1 = jsonToImage(page,spanUrl.substring(spanUrl.lastIndexOf('-') + 1));
                if (fullSize1 == null || !fullSize1.contains("//orig")) {
                    fullSize = smallToFull(img.attr("src"), spanUrl);
                }
                if (fullSize == null && fullSize1 != null) {
                    fullSize = fullSize1;
                }
            }
            if (fullSize == null) {
                if (thumb.attr("data-super-full-img") != null) {
                    fullSize = thumb.attr("data-super-full-img");
                } else if (thumb.attr("data-super-img") != null) {
                    fullSize = thumb.attr("data-super-img");
                } else {
                    continue;
                }
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
        List<String> textURLs = new ArrayList<>();
        // Iterate over all thumbnails
        for (Element thumb : page.select("div.zones-container span.thumb")) {
            logger.info(thumb.attr("href"));
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
        Elements nextButtons = page.select("link[rel=\"next\"]");
        if (nextButtons.size() == 0) {
            if (page.select("link[rel=\"prev\"]").size() == 0) {
                throw new IOException("No next page found");
            } else {
                throw new IOException("Hit end of pages");
            }
        }
        Element a = nextButtons.first();
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
     * Attempts to download description for image.
     * Comes in handy when people put entire stories in their description.
     * If no description was found, returns null.
     * @param url The URL the description will be retrieved from
     * @param page The gallery page the URL was found on
     * @return A String[] with first object being the description, and the second object being image file name if found.
     */
    @Override
    public String[] getDescription(String url,Document page) {
        if (isThisATest()) {
            return null;
        }
        try {
            // Fetch the image page
            Response resp = Http.url(url)
                                .referrer(this.url)
                                .cookies(cookies)
                                .response();
            cookies.putAll(resp.cookies());

            // Try to find the description
            Document documentz = resp.parse();
            Element ele = documentz.select("div.dev-description").first();
            if (ele == null) {
                throw new IOException("No description found");
            }
            documentz.outputSettings(new Document.OutputSettings().prettyPrint(false));
            ele.select("br").append("\\n");
            ele.select("p").prepend("\\n\\n");
            String fullSize = null;
            Element thumb = page.select("div.zones-container span.thumb[href=\"" + url + "\"]").get(0);
            if (!thumb.attr("data-super-full-img").isEmpty()) {
                fullSize = thumb.attr("data-super-full-img");
                String[] split = fullSize.split("/");
                fullSize = split[split.length - 1];
            } else {
                String spanUrl = thumb.attr("href");
                fullSize = jsonToImage(page,spanUrl.substring(spanUrl.lastIndexOf('-') + 1));
                if (fullSize != null) {
                    String[] split = fullSize.split("/");
                    fullSize = split[split.length - 1];
                }
            }
            if (fullSize == null) {
                return new String[] {Jsoup.clean(ele.html().replaceAll("\\\\n", System.getProperty("line.separator")), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false))};
            }
            fullSize = fullSize.substring(0, fullSize.lastIndexOf("."));
            return new String[] {Jsoup.clean(ele.html().replaceAll("\\\\n", System.getProperty("line.separator")), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false)),fullSize};
            // TODO Make this not make a newline if someone just types \n into the description.
        } catch (IOException ioe) {
                logger.info("Failed to get description at " + url + ": '" + ioe.getMessage() + "'");
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
            if (els.size() > 0) {
                // Large image
                fsimage = els.get(0).attr("src");
                logger.info("Found large-scale: " + fsimage);
                if (fsimage.contains("//orig")) {
                    return fsimage;
                }
            }
            // Try to find the download button
            els = doc.select("a.dev-page-download");
            if (els.size() > 0) {
                // Full-size image
                String downloadLink = els.get(0).attr("href");
                logger.info("Found download button link: " + downloadLink);
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
                    logger.info("Found image download: " + location);
                }
            }
            if (fsimage != null) {
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
        Map<String,String> postData = new HashMap<>();
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
