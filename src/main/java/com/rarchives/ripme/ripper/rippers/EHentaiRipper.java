package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class EHentaiRipper extends AlbumRipper {
    // All sleep times are in milliseconds
    private static final int PAGE_SLEEP_TIME     = 3 * 1000;
    private static final int IMAGE_SLEEP_TIME    = 1 * 1000;
    private static final int IP_BLOCK_SLEEP_TIME = 60 * 1000;

    private static final String DOMAIN = "g.e-hentai.org", HOST = "e-hentai";
    private static final Logger logger = Logger.getLogger(EHentaiRipper.class);

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool ehentaiThreadPool = new DownloadThreadPool("ehentai");

    // Current HTML document
    private Document albumDoc = null;

    public EHentaiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.contains("nw=session")) {
            return url;
        } 
        else if (u.contains("?")) {
            u += "&nw=session";
        } 
        else {
            u += "?nw=session";
        }
        return new URL(u);
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
            Elements elems = albumDoc.select("#gn");
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

        p = Pattern.compile("^.*g\\.e-hentai\\.org/g/([0-9]+)/([a-fA-F0-9]+)/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2);
        }

        throw new MalformedURLException(
                "Expected g.e-hentai.org gallery format: "
                        + "http://g.e-hentai.org/g/####/####/"
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
                                .timeout(5000)
                                .referrer(this.url.toExternalForm())
                                .get();
            }
            // Check for rate limiting
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
            Elements thumbs = albumDoc.select("#gdt > .gdtm a");
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
                EHentaiImageThread t = new EHentaiImageThread(new URL(thumb.attr("href")), index, this.workingDir);
                ehentaiThreadPool.addThread(t);
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
            Elements hrefs = albumDoc.select(".ptt a");
            if (hrefs.size() == 0) {
                logger.info("No navigation links found at " + nextUrl);
                break;
            }
            // Ensure next page is different from the current page
            String lastUrl = nextUrl;
            nextUrl = hrefs.last().attr("href");
            if (lastUrl.equals(nextUrl)) {
                break; // We're on the last page
            }

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
    private class EHentaiImageThread extends Thread {
        private URL url;
        private int index;
        private File workingDir;
        private int retries = 3;

        public EHentaiImageThread(URL url, int index, File workingDir) {
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
                                    .timeout(5000)
                                    .referrer(this.url.toExternalForm())
                                    .get();
                // Check for rate limit
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
                Elements images = doc.select(".sni > a > img");
                if (images.size() == 0) {
                    // Attempt to find image elsewise (Issue #41)
                    images = doc.select("img#img");
                    if (images.size() == 0) {
                        logger.warn("Image not found at " + this.url);
                        return;
                    }
                }
                Element image = images.first();
                String imgsrc = image.attr("src");
                logger.info("Found URL " + imgsrc + " via " + images.get(0));
                Pattern p = Pattern.compile("^http://.*/ehg/image.php.*&n=([^&]+).*$");
                Matcher m = p.matcher(imgsrc);
                if (m.matches()) {
                    // Manually discover filename from URL
                    String savePath = this.workingDir + File.separator;
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        savePath += String.format("%03d_", index);
                    }
                    savePath += m.group(1);
                    addURLToDownload(new URL(imgsrc), new File(savePath));
                }
                else {
                    // Provide prefix and let the AbstractRipper "guess" the filename
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    addURLToDownload(new URL(imgsrc), prefix);
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}