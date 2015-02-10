package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

/**
 * Appears to be broken as of 2015-02-11.
 * Looks like supertangas changed their site completely.
 */
public class SupertangasRipper extends AlbumRipper {

    private static final String DOMAIN = "supertangas.com",
                                HOST   = "supertangas";

    public SupertangasRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        int page = 0;
        String baseURL = "http://www.supertangas.com/fotos/?level=search&exact=1&searchterms=" + this.getGID(this.url);
        Document doc;
        while (true) {
            page++;
            String theURL = baseURL;
            if (page > 1) {
                theURL += "&plog_page=" + page;
            }
            try {
                logger.info("    Retrieving " + theURL);
                sendUpdate(STATUS.LOADING_RESOURCE, theURL);
                doc = Http.url(theURL).get();
            } catch (HttpStatusException e) {
                logger.debug("Hit end of pages at page " + page, e);
                break;
            }
            Elements images = doc.select("li.thumbnail a");
            if (images.size() == 0) {
                break;
            }
            for (Element imageElement : images) {
                String image = imageElement.attr("href");
                image = image.replaceAll("\\/fotos\\/", "/fotos/images/");
                addURLToDownload(new URL(image));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next page", e);
                break;
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // http://www.supertangas.com/fotos/?level=search&exact=1&searchterms=Tahiticora%20(France)
        Pattern p = Pattern.compile("^https?://[w.]*supertangas\\.com/fotos/\\?.*&searchterms=([a-zA-Z0-9%()+]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: http://supertangas.com/fotos/?level=search&exact=1&searchterms=...");
        }
        return m.group(m.groupCount());
    }

}
