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

            for (Element thumb : doc.select("div.zones-container a.thumb img")) {
                if (thumb.attr("transparent").equals("false")) {
                    continue; // a.thumbs to other albums are invisible
                }

                String fullSizePage = thumbToFull(thumb.attr("src"));
                try {
                    URL fullsizePageURL = new URL(fullSizePage);
                    index++;
                    addURLToDownload(fullsizePageURL, String.format("%03d_", index));
                } catch (MalformedURLException e) {
                    logger.error("[!] Invalid thumbnail image: " + thumbToFull(fullSizePage));
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
    
    public static String thumbToFull(String thumb) {
        thumb = thumb.replace("http://th", "http://fc");
        List<String> fields = new ArrayList<String>(Arrays.asList(thumb.split("/")));
        fields.remove(4);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                result.append("/");
            }
            result.append(fields.get(i));
        }
        return result.toString();
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
