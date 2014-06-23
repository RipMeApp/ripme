package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractSinglePageRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class GirlsOfDesireRipper extends AbstractSinglePageRipper {
    // All sleep times are in milliseconds
    private static final int IMAGE_SLEEP_TIME    = 100;

    // Current HTML document
    private Document albumDoc = null;

    public GirlsOfDesireRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "GirlsOfDesire";
    }
    @Override
    public String getDomain() {
        return "girlsofdesire.org";
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Document doc = getFirstPage();
            Elements elems = doc.select(".albumName");
            return getHost() + "_" + elems.first().text();
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
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }
    
    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        for (Element thumb : doc.select("td.vtop > a > img")) {
            String imgSrc = thumb.attr("src");
            imgSrc = imgSrc.replaceAll("_thumb\\.", ".");
            if (imgSrc.startsWith("/")) {
                imgSrc = "http://www.girlsofdesire.org" + imgSrc;
            }
            imageURLs.add(imgSrc);
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
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
}