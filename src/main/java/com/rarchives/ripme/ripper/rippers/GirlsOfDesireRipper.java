package com.rarchives.ripme.ripper.rippers;

import java.io.File;
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

public class GirlsOfDesireRipper extends AlbumRipper {
    // All sleep times are in milliseconds
    private static final int PAGE_SLEEP_TIME     = 3  * 1000;
    private static final int IMAGE_SLEEP_TIME    = 1  * 1000;
    private static final int IP_BLOCK_SLEEP_TIME = 60 * 1000;
    private static final int TIMEOUT             = 5  * 1000;

    private static final String DOMAIN = "girlsofdesire.org", HOST = "GirlsOfDesire";

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool girlsOfDesireThreadPool = new DownloadThreadPool(HOST);

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
                albumDoc = Jsoup.connect(url.toExternalForm())
                                .userAgent(USER_AGENT)
                                .timeout(TIMEOUT)
                                .get();
            }
            Elements elems = albumDoc.select(".albumName");
            return HOST + "_" + elems.get(0).text();
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
        int index = 0, retries = 3;
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
                                .timeout(TIMEOUT)
                                .referrer(this.url.toExternalForm())
                                .get();
            }

            // Check for rate limiting
            // TODO copied from EHentaiRipper - how does this need to work on GirlsOfDesire?
            if (albumDoc.toString().contains("IP address will be automatically banned")) {
                if (retries == 0) {
                    logger.error("Hit rate limit and maximum number of retries, giving up");
                    break;
                }
                logger.warn("Hit rate limit while loading " + nextUrl + ", sleeping for " + IP_BLOCK_SLEEP_TIME + "ms, " + retries + " retries remaining");
                retries--;
                try {
                    Thread.sleep(IP_BLOCK_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for rate limit to subside", e);
                    break;
                }
                albumDoc = null;
                continue;
            }

            // Find thumbnails
            Elements thumbs = albumDoc.select("#box_10 > table a");
            if (thumbs.size() == 0) {
                logger.info("albumDoc: " + albumDoc);
                logger.info("No images found at " + nextUrl);
                break;
            }

            // Iterate over images on page
            for (Element thumb : thumbs) {
                if (isStopped()) {
                    break;
                }
                index++;
                String imgSrc = thumb.attr("href");
                URL imgUrl = new URL(url, imgSrc);
                GirlsOfDesireImageThread t = new GirlsOfDesireImageThread(imgUrl, index, this.workingDir);
                girlsOfDesireThreadPool.addThread(t);
                try {
                    Thread.sleep(IMAGE_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting to load next image", e);
                }
            }

            if (isStopped()) {
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
    private class GirlsOfDesireImageThread extends Thread {
        private URL url;
        private int index;
        private File workingDir;
        private int retries = 3;

        public GirlsOfDesireImageThread(URL url, int index, File workingDir) {
            super();
            this.url = url;
            this.index = index;
            this.workingDir = workingDir;
        }

        @Override
        public void run() {
            fetchImage();
        }

        private void fetchImage() {
            try {
                Document doc = Jsoup.connect(this.url.toExternalForm())
                                    .userAgent(USER_AGENT)
                                    .timeout(TIMEOUT)
                                    .referrer(this.url.toExternalForm())
                                    .get();
                // Check for rate limit
                // TODO copied from EHentaiRipper - how does this need to work on GirlsOfDesire?
                if (doc.toString().contains("IP address will be automatically banned")) {
                    if (this.retries == 0) {
                        logger.error("Rate limited & ran out of retries, skipping image at " + this.url);
                        return;
                    }
                    logger.warn("Hit rate limit. Sleeping for " + IP_BLOCK_SLEEP_TIME + "ms");
                    try {
                        Thread.sleep(IP_BLOCK_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while waiting for rate limit to subside", e);
                        return;
                    }
                    this.retries--;

                    fetchImage(); // Re-attempt to download the image
                    return;
                }

                // Find image
                Elements divs = doc.select("#box_12 > div");
                Element div = divs.get(1);
                Element image = div.select("a > img").first();
                String imgsrc = image.attr("src");
                URL imgUrl = new URL(url, imgsrc);

                logger.info("Found URL " + imgUrl.toExternalForm() + " via " + image);
                Pattern p = Pattern.compile("^http://.*/([\\d]+).jpg$"); // TODO only compile this regex once
                Matcher m = p.matcher(imgsrc);
                if (m.matches()) {
                    // Manually discover filename from URL
                    String savePath = this.workingDir + File.separator;
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        savePath += String.format("%03d_", index);
                    }
                    savePath += m.group(1);
                    addURLToDownload(imgUrl, new File(savePath));
                }
                else {
                    // Provide prefix and let the AbstractRipper "guess" the filename
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    addURLToDownload(imgUrl, prefix);
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}