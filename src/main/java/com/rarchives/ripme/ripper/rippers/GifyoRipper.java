package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class GifyoRipper extends AlbumRipper {

    private static final String DOMAIN = "gifyo.com",
                                HOST   = "gifyo";

    public GifyoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith(DOMAIN));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://gifyo\\.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://gifyo.com/" + m.group(1) + "/");
        }
        throw new MalformedURLException("Expected username in URL (gifyo.com/username/ and not " + url);
    }
    @Override
    public void rip() throws IOException {
        int page = 0;
        Map<String,String> cookies = new HashMap<String,String>();
        while (true) {
            this.sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm() + " (page #" + page + ")");
            logger.info("    Retrieving " + this.url + "(page #" + page + ")");
            Response resp = null;
            if (page == 0) {
                resp = Http.url(this.url)
                           .ignoreContentType()
                           .response();
                cookies = resp.cookies();
            }
            else {
                Map<String,String> postData = new HashMap<String,String>();
                postData.put("cmd", "refreshData");
                postData.put("view", "gif");
                postData.put("layout", "grid");
                postData.put("page", Integer.toString(page));
                resp = Http.url(this.url)
                           .ignoreContentType()
                           .data(postData)
                           .cookies(cookies)
                           .method(Method.POST)
                           .response();
                cookies.putAll(resp.cookies());
            }
            Document doc = resp.parse();
            Elements images = doc.select("div.gif img");
            logger.info("Found " + images.size() + " images");
            for (Element image : images) {
                String imageUrl = image.attr("src");
                if (imageUrl.startsWith("//")) {
                    imageUrl = "http:" + imageUrl;
                }
                imageUrl = imageUrl.replace("/medium/", "/large/");
                imageUrl = imageUrl.replace("_s.gif", ".gif");
                addURLToDownload(new URL(imageUrl));
            }
            if (images.size() == 0) {
                if (doc.html().contains("profile is private")) {
                    sendUpdate(STATUS.RIP_ERRORED, "User has private profile");
                    throw new IOException("User has private profile");
                }
                else {
                    logger.info("Page " + page + " has 0 images");
                }
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next album:", e);
                break;
            }
            page++;
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[w.]*gifyo.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Gifyo user not found in " + url + ", expected http://gifyo.com/username");
    }

}
