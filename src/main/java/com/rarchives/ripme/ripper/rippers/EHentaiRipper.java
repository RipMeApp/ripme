package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

public class EHentaiRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(EHentaiRipper.class);

    // All sleep times are in milliseconds
    private static final int PAGE_SLEEP_TIME = 3000;
    private static final int IMAGE_SLEEP_TIME = 1500;
    private static final int IP_BLOCK_SLEEP_TIME = 60 * 1000;
    private static final Map<String, String> cookies = new HashMap<>();

    static {
        cookies.put("nw", "1");
        cookies.put("tip", "1");
    }

    private String lastURL = null;
    // Current HTML document
    private Document albumDoc = null;

    public EHentaiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "e-hentai";
    }

    @Override
    public String getDomain() {
        return "e-hentai.org";
    }

    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                albumDoc = getPageWithRetries(url);
            }
            Elements elems = albumDoc.select("#gn");
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

        p = Pattern.compile("^https?://e-hentai\\.org/g/([0-9]+)/([a-fA-F0-9]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2);
        }

        throw new MalformedURLException(
                "Expected e-hentai.org gallery format: "
                        + "http://e-hentai.org/g/####/####/"
                        + " Got: " + url);
    }

    private Document getPageWithRetries(URL url) throws IOException {
        Document doc;
        int retries = 3;
        while (true) {
            sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
            logger.info("Retrieving " + url);
            doc = Http.url(url)
                    .referrer(this.url)
                    .cookies(cookies)
                    .get();
            if (doc.toString().contains("IP address will be automatically banned")) {
                if (retries == 0) {
                    throw new IOException("Hit rate limit and maximum number of retries, giving up");
                }
                logger.warn("Hit rate limit while loading " + url + ", sleeping for " + IP_BLOCK_SLEEP_TIME + "ms, " + retries + " retries remaining");
                retries--;
                try {
                    Thread.sleep(IP_BLOCK_SLEEP_TIME);
                } catch (InterruptedException e) {
                    throw new IOException("Interrupted while waiting for rate limit to subside");
                }
            } else {
                return doc;
            }
        }
    }

    public List<String> getTags(Document doc) {
        List<String> tags = new ArrayList<>();
        logger.info("Getting tags");
        for (Element tag : doc.select("td > div > a")) {
            logger.info("Found tag " + tag.text());
            tags.add(tag.text());
        }
        return tags;
    }


    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = getPageWithRetries(this.url);
        }
        this.lastURL = this.url.toExternalForm();
        logger.info("Checking blacklist");
        String blacklistedTag = RipUtils.checkTags(Utils.getConfigStringArray("ehentai.blacklist.tags"), getTags(albumDoc));
        if (blacklistedTag != null) {
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Skipping " + url.toExternalForm() + " as it " +
                    "contains the blacklisted tag \"" + blacklistedTag + "\"");
            return null;
        }
        return albumDoc;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException, URISyntaxException {
        // Check if we've stopped
        if (isStopped()) {
            throw new IOException("Ripping interrupted");
        }
        // Find next page
        Elements hrefs = doc.select(".ptt a");
        if (hrefs.isEmpty()) {
            logger.info("doc: " + doc.html());
            throw new IOException("No navigation links found");
        }
        // Ensure next page is different from the current page
        String nextURL = hrefs.last().attr("href");
        if (nextURL.equals(this.lastURL)) {
            logger.info("lastURL = nextURL : " + nextURL);
            throw new IOException("Reached last page of results");
        }
        // Sleep before loading next page
        sleep(PAGE_SLEEP_TIME);
        // Load next page
        Document nextPage = getPageWithRetries(new URI(nextURL).toURL());
        this.lastURL = nextURL;
        return nextPage;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Elements thumbs = page.select("#gdt > a");
        // Iterate over images on page
        for (Element thumb : thumbs) {
            imageURLs.add(thumb.attr("href"));
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        EHentaiImageThread t = new EHentaiImageThread(url, index, this.workingDir.toPath());
        getCrawlerThreadPool().addThread(t);
        try {
            Thread.sleep(IMAGE_SLEEP_TIME);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting to load next image", e);
        }
    }

    /**
     * Helper class to find and download images found on "image" pages
     * <p>
     * Handles case when site has IP-banned the user.
     */
    private class EHentaiImageThread implements Runnable {
        private final URL url;
        private final int index;
        private final Path workingDir;

        EHentaiImageThread(URL url, int index, Path workingDir) {
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
                Document doc = getPageWithRetries(this.url);

                // Find image
                Elements images = doc.select(".sni > a > img");
                if (images.isEmpty()) {
                    // Attempt to find image elsewise (Issue #41)
                    images = doc.select("img#img");
                    if (images.isEmpty()) {
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
                    String savePath = this.workingDir + "/";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        savePath += String.format("%03d_", index);
                    }
                    savePath += m.group(1);
                    addURLToDownload(new URI(imgsrc).toURL(), Paths.get(savePath));
                } else {
                    // Provide prefix and let the AbstractRipper "guess" the filename
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    addURLToDownload(new URI(imgsrc).toURL(), prefix);
                }
            } catch (IOException | URISyntaxException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}
