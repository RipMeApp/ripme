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
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class ImagevenueRipper extends AlbumRipper {

    private static final int IMAGE_SLEEP_TIME = 0;

    private static final String DOMAIN = "imagevenue.com", HOST = "imagevenue";

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool imagevenueThreadPool = new DownloadThreadPool("imagevenue");

    public ImagevenueRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^https?://.*imagevenue.com/galshow.php\\?gal=([a-zA-Z0-9\\-_]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagevenue gallery format: "
                        + "http://...imagevenue.com/galshow.php?gal=gallery_...."
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        String nextUrl = this.url.toExternalForm();
        logger.info("    Retrieving album page " + nextUrl);
        sendUpdate(STATUS.LOADING_RESOURCE, nextUrl);
        Document albumDoc = Http.url(nextUrl).get();
        // Find thumbnails
        Elements thumbs = albumDoc.select("a[target=_blank]");
        if (thumbs.size() == 0) {
            logger.info("No images found at " + nextUrl);
        }
        else {
            // Iterate over images on page
            for (Element thumb : thumbs) {
                if (isStopped()) {
                    break;
                }
                index++;
                ImagevenueImageThread t = new ImagevenueImageThread(new URL(thumb.attr("href")), index);
                imagevenueThreadPool.addThread(t);
                try {
                    Thread.sleep(IMAGE_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting to load next image", e);
                    break;
                }
            }
        }

        imagevenueThreadPool.waitForThreads();
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
    private class ImagevenueImageThread extends Thread {
        private URL url;
        private int index;

        public ImagevenueImageThread(URL url, int index) {
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
                sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
                Document doc = Http.url(this.url).get();
                // Find image
                Elements images = doc.select("a > img");
                if (images.size() == 0) {
                    logger.warn("Image not found at " + this.url);
                    return;
                }
                Element image = images.first();
                String imgsrc = image.attr("src");
                imgsrc = "http://" + this.url.getHost() + "/" + imgsrc;
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