package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class DeviantartRipper extends AlbumRipper {

    private static final String DOMAIN = "deviantart.com",
                                HOST   = "deviantart";

    private static final int SLEEP_TIME = 2000;
    private static final Logger logger = Logger.getLogger(DeviantartRipper.class);

    public DeviantartRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        u = u.replaceAll("\\?.*", "");
        return new URL(u);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        String nextURL = this.url.toExternalForm();

        while (nextURL != null) {

            logger.info("    Retrieving " + nextURL);
            sendUpdate(STATUS.LOADING_RESOURCE, "Retrieving " + nextURL);
            Document doc = Jsoup.connect(nextURL)
                    .userAgent(USER_AGENT)
                    .get();

            for (Element thumb : doc.select("div.zones-container a.thumb")) {
                if (isStopped()) {
                    break;
                }
                Element img = thumb.select("img").get(0);
                if (img.attr("transparent").equals("false")) {
                    continue; // a.thumbs to other albums are invisible
                }
                index++;

                String fullSize = null;
                try {
                    fullSize = thumbToFull(img.attr("src"), true);
                } catch (Exception e) {
                    logger.info("Attempting to get full size image from " + thumb.attr("href"));
                    fullSize = smallToFull(img.attr("src"), thumb.attr("href"));
                    if (fullSize == null) {
                        continue;
                    }
                }
                try {
                    URL fullsizeURL = new URL(fullSize);
                    String imageId = fullSize.substring(fullSize.lastIndexOf('-') + 1);
                    imageId = imageId.substring(0, imageId.indexOf('.'));
                    long imageIdLong = alphaToLong(imageId);
                    addURLToDownload(fullsizeURL, String.format("%010d_", imageIdLong));
                } catch (MalformedURLException e) {
                    logger.error("[!] Invalid thumbnail image: " + fullSize);
                    continue;
                }
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting for page to load", e);
                break;
            }
            nextURL = null;
            for (Element nextButton : doc.select("a.away")) {
                if (nextButton.attr("href").contains("offset=" + index)) {
                    nextURL = this.url.toExternalForm() + "?offset=" + index;
                }
            }
            if (nextURL == null) {
                logger.info("No next button found");
            }
        }
        waitForThreads();
    }

    public static long alphaToLong(String alpha) {
        long result = 0;
        for (int i = 0; i < alpha.length(); i++) {
            result += charToInt(alpha, i);
        }
        return result;
    }

    private static int charToInt(String text, int index) {
        char c = text.charAt(text.length() - index - 1);
        c = Character.toLowerCase(c);
        int number = "0123456789abcdefghijklmnopqrstuvwxyz".indexOf(c);
        number *= Math.pow(36, index);
        return number;
    }

    public static String thumbToFull(String thumb, boolean throwException) throws Exception {
        thumb = thumb.replace("http://th", "http://fc");
        List<String> fields = new ArrayList<String>(Arrays.asList(thumb.split("/")));
        fields.remove(4);
        if (!fields.get(4).equals("f") && throwException) {
            // Not a full-size image
            logger.warn("Can't get full size image from " + thumb);
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

    public String smallToFull(String thumb, String page) {
        try {
            Response resp = Jsoup.connect(page)
                                 .userAgent(USER_AGENT)
                                 .referrer(this.url.toExternalForm())
                                 .method(Method.GET)
                                 .execute();
            Map<String,String> cookies = resp.cookies();
            Elements els = resp.parse().select("a.dev-page-download");
            if (els.size() == 0) {
                throw new IOException("no download page found");
            }
            String fsimage = els.get(0).attr("href");
            String imageId = fsimage.substring(fsimage.lastIndexOf('-') + 1);
            imageId = imageId.substring(0, imageId.indexOf('.'));
            long imageIdLong = alphaToLong(imageId);
            addURLToDownload(new URL(fsimage), String.format("%010d_", imageIdLong), "", page, cookies);
            return null;
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

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com(/gallery)?/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Root gallery
            return m.group(1);
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-]{1,})\\.deviantart\\.com/gallery/([0-9]{1,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Subgallery
            return m.group(1) + "_" + m.group(2);
        }
        throw new MalformedURLException("Expected URL format: http://username.deviantart.com/[/gallery/#####], got: " + url);
    }

    /**
     * Logs into deviant art. Not required to rip NSFW images.
     * @return Map of cookies containing session data.
     */
    @SuppressWarnings("unused")
    private Map<String, String> loginToDeviantart() throws IOException {
        // Populate postData fields
        Map<String,String> postData = new HashMap<String,String>();
        String username = Utils.getConfigString("deviantart.username", null);
        String password = Utils.getConfigString("deviantart.password", null);
        if (username == null || password == null) {
            throw new IOException("could not find username or password in config");
        }
        Response resp = Jsoup.connect("http://www.deviantart.com/")
                             .userAgent(USER_AGENT)
                             .method(Method.GET)
                             .execute();
        for (Element input : resp.parse().select("form#form-login input[type=hidden]")) {
            postData.put(input.attr("name"), input.attr("value"));
        }
        postData.put("username", username);
        postData.put("password", password);
        postData.put("remember_me", "1");

        // Send login request
        resp = Jsoup.connect("https://www.deviantart.com/users/login")
                    .userAgent(USER_AGENT)
                    .data(postData)
                    .cookies(resp.cookies())
                    .method(Method.POST)
                    .execute();

        // Assert we are logged in
        if (resp.hasHeader("Location") && resp.header("Location").contains("password")) {
            // Wrong password
            throw new IOException("Wrong pasword");
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
