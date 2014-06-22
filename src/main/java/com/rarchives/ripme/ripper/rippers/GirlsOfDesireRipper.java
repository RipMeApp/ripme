package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class GirlsOfDesireRipper extends AlbumRipper {
    // All sleep times are in milliseconds
    private static final int IMAGE_SLEEP_TIME    = 100;

    private static final String DOMAIN = "girlsofdesire.org", HOST = "GirlsOfDesire";

    // Current HTML document
    private Document albumDoc = null;

    public GirlsOfDesireRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                logger.info("    Retrieving " + url.toExternalForm());
                sendUpdate(STATUS.LOADING_RESOURCE, url.toString());
                albumDoc = Http.url(url).get();
            }
            Elements elems = albumDoc.select(".albumName");
            return HOST + "_" + elems.first().text();
        } catch (Exception e) {
            // Fall back to default album naming convention
            logger.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^www\\.girlsofdesire\\.org\\/galleries\\/([\\w\\d-]+)\\/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected girlsofdesire.org gallery format: "
                        + "http://www.girlsofdesire.org/galleries/<name>/"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        String nextUrl = this.url.toExternalForm();

        if (albumDoc == null) {
            logger.info("    Retrieving album page " + nextUrl);
            sendUpdate(STATUS.LOADING_RESOURCE, nextUrl);
            albumDoc = Http.url(nextUrl).get();
        }

        // Find thumbnails
        Elements thumbs = albumDoc.select("td.vtop > a > img");
        if (thumbs.size() == 0) {
            logger.info("No images found at " + nextUrl);
        }

        // Iterate over images on page
        for (Element thumb : thumbs) {
            if (isStopped()) {
                break;
            }
            // Convert thumbnail to full-size image
            String imgSrc = thumb.attr("src");
            imgSrc = imgSrc.replaceAll("_thumb\\.", ".");
            URL imgUrl = new URL(url, imgSrc);

            addURLToDownload(imgUrl, "", "", this.url.toExternalForm(), null);

            try {
                Thread.sleep(IMAGE_SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting to load next image", e);
            }
        }

        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }
}