package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class ImagebamRipper extends AlbumRipper {

    private static final int IMAGE_SLEEP_TIME = 250,
                             PAGE_SLEEP_TIME  = 3000;

    private static final String DOMAIN = "imagebam.com", HOST = "imagebam";

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool imagebamThreadPool = new DownloadThreadPool("imagebam");

    // Current HTML document
    private Document albumDoc = null;

    public ImagebamRipper(URL url) throws IOException {
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
                albumDoc = Jsoup.connect(url.toExternalForm())
                                .userAgent(USER_AGENT)
                                .timeout(5000)
                                .get();
            }
            Elements elems = albumDoc.select("legend");
            String title = elems.first().text();
            logger.info("Title text: '" + title + "'");
            Pattern p = Pattern.compile("^(.*)\\s\\d* image.*$");
            Matcher m = p.matcher(title);
            if (m.matches()) {
                logger.info("matches!");
                return HOST + "_" + getGID(url) + " (" + m.group(1).trim() + ")";
            }
            logger.info("Doesn't match " + p.pattern());
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

        p = Pattern.compile("^https?://[wm.]*imagebam.com/gallery/([a-zA-Z0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagebam gallery format: "
                        + "http://www.imagebam.com/gallery/galleryid"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        String nextUrl = this.url.toExternalForm();
        while (true) {
            if (isStopped()) {
                break;
            }
            if (albumDoc == null) {
                logger.info("    Retrieving album page " + nextUrl);
                sendUpdate(STATUS.LOADING_RESOURCE, nextUrl);
                albumDoc = Jsoup.connect(nextUrl)
                                .userAgent(USER_AGENT)
                                .timeout(5000)
                                .referrer(this.url.toExternalForm())
                                .get();
            }
            // Find thumbnails
            Elements thumbs = albumDoc.select("div > a[target=_blank]:not(.footera)");
            if (thumbs.size() == 0) {
                logger.info("No images found at " + nextUrl);
                break;
            }
            // Iterate over images on page
            for (Element thumb : thumbs) {
                if (isStopped()) {
                    break;
                }
                index++;
                ImagebamImageThread t = new ImagebamImageThread(new URL(thumb.attr("href")), index);
                imagebamThreadPool.addThread(t);
                try {
                    Thread.sleep(IMAGE_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting to load next image", e);
                }
            }

            if (isStopped()) {
                break;
            }
            // Find next page
            Elements hrefs = albumDoc.select("a.pagination_current + a.pagination_link");
            if (hrefs.size() == 0) {
                logger.info("No more pages found at " + nextUrl);
                break;
            }
            nextUrl = "http://www.imagebam.com" + hrefs.first().attr("href");
            logger.info("Found next page: " + nextUrl);

            // Reset albumDoc so we fetch the page next time
            albumDoc = null;

            // Sleep before loading next page
            try {
                Thread.sleep(PAGE_SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to load next page", e);
                break;
            }
        }

        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    /**
     * Helper class to find and download images found on "image" pages
     * 
     * Handles case when site has IP-banned the user.
     */
    private class ImagebamImageThread extends Thread {
        private URL url;
        private int index;

        public ImagebamImageThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            fetchImage();
        }
        
        private void fetchImage() {
            try {
                Document doc = Jsoup.connect(this.url.toExternalForm())
                                    .userAgent(USER_AGENT)
                                    .cookie("nw", "1")
                                    .timeout(5000)
                                    .referrer(this.url.toExternalForm())
                                    .get();
                // Find image
                Elements images = doc.select("td > img");
                if (images.size() == 0) {
                    logger.warn("Image not found at " + this.url);
                    return;
                }
                Element image = images.first();
                String imgsrc = image.attr("src");
                logger.info("Found URL " + imgsrc + " via " + images.get(0));
                // Provide prefix and let the AbstractRipper "guess" the filename
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(imgsrc), prefix);
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}