package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

/**
 *
 * @author losipher
 */
public class EromeRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(EromeRipper.class);

    boolean rippingProfile;
    private HashMap<String, String> cookies = new HashMap<>();

    public EromeRipper (URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "erome.com";
    }

    @Override
    public String getHost() {
        return "erome";
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toString(), this.cookies);
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums() {
        Pattern pa = Pattern.compile("https?://www.erome.com/([a-zA-Z0-9_\\-?=]*)/?");
        Matcher ma = pa.matcher(url.toExternalForm());
        return ma.matches();
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select("div#albums > div.album > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getAlbumTitle() throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title as GID
            Element titleElement = getCachedFirstPage().select("meta[property=og:title]").first();
            String title = titleElement.attr("content");
            title = title.substring(title.lastIndexOf('/') + 1);
            return getHost() + "_" + getGID(url) + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        } catch (NullPointerException e) {
            return getHost() + "_" + getGID(url);
        }
        return super.getAlbumTitle();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        return new URI(url.toExternalForm().replaceAll("https?://erome.com", "https://www.erome.com")).toURL();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        return getMediaFromPage(doc);
    }

    @Override
    public Document getFirstPage() throws IOException {
        this.setAuthCookie();
        Response resp = Http.url(this.url)
                .cookies(cookies)
                .ignoreContentType()
                .response();

        return resp.parse();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www.erome.com/[ai]/([a-zA-Z0-9]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://www.erome.com/([a-zA-Z0-9_\\-?=]+)/?$");
        m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("erome album not found in " + url + ", expected https://www.erome.com/album");
    }

    private List<String> getMediaFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        for (Element el : doc.select("img.img-front")) {
            if (el.hasAttr("data-src")) {
                //to add images that are not loaded( as all images are lasyloaded as we scroll).
                results.add(el.attr("data-src"));
            } else if (el.hasAttr("src")) {
                if (el.attr("src").startsWith("https:")) {
                    results.add(el.attr("src"));
                } else {
                    results.add("https:" + el.attr("src"));
                }
            }
        }
        for (Element el : doc.select("source[label=HD]")) {
            if (el.attr("src").startsWith("https:")) {
                results.add(el.attr("src"));
            }
            else {
                results.add("https:" + el.attr("src"));
            }
        }
        for (Element el : doc.select("source[label=SD]")) {
            if (el.attr("src").startsWith("https:")) {
                results.add(el.attr("src"));
            }
            else {
                results.add("https:" + el.attr("src"));
            }
        }

        if (results.size() == 0) {
            if (cookies.isEmpty()) {
                logger.warn("You might try setting erome.laravel_session manually " +
                        "if you think this page definitely contains media.");
            }
        }

        return results;
    }

    private void setAuthCookie() {
        String sessionId = Utils.getConfigString("erome.laravel_session", null);
        if (sessionId != null) {
            cookies.put("laravel_session", sessionId);
        }
    }

}
