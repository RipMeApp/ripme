package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class ImagefapRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(ImagefapRipper.class);

    private int callsMade = 0;
    private long startTime = System.nanoTime();

    private static final int RETRY_LIMIT = 10;
    private static final int HTTP_RETRY_LIMIT = 3;
    private static final int RATE_LIMIT_HOUR = 1000;

    // All sleep times are in milliseconds
    private static final int PAGE_SLEEP_TIME = 60 * 60 * 1000 / RATE_LIMIT_HOUR;
    private static final int IMAGE_SLEEP_TIME = 60 * 60 * 1000 / RATE_LIMIT_HOUR;
    // Timeout when blocked = 1 hours. Retry every retry within the hour mark + 1 time after the hour mark.
    private static final int IP_BLOCK_SLEEP_TIME = (int) Math.round((double) 60 / (RETRY_LIMIT - 1) * 60 * 1000);

    public ImagefapRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imagefap";
    }
    @Override
    public String getDomain() {
        return "imagefap.com";
    }

    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String gid = getGID(url);
        String newURL = "https://www.imagefap.com/pictures/" + gid + "/random-string";
        logger.debug("Changed URL from " + url + " to " + newURL);
        return new URI(newURL).toURL();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        // Old format (I suspect no longer supported)
        p = Pattern.compile("^.*imagefap.com/gallery.php\\?pgid=([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/gallery.php\\?gid=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/gallery/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        // most recent format
        p = Pattern.compile("^.*imagefap.com/pictures/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagefap.com gallery formats: "
                        + "imagefap.com/gallery.php?gid=####... or "
                        + "imagefap.com/pictures/####..."
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {

        Document firstPage = getPageWithRetries(url);

        sendUpdate(STATUS.LOADING_RESOURCE, "Loading first page...");

        return firstPage;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException, URISyntaxException {
        String nextURL = null;
        for (Element a : doc.select("a.link3")) {
            if (a.text().contains("next")) {
                nextURL = this.sanitizeURL(this.url) + a.attr("href");
                break;
            }
        }
        if (nextURL == null) {
            throw new IOException("No next page found");
        }
        // Sleep before fetching next page.
        sleep(PAGE_SLEEP_TIME);

        sendUpdate(STATUS.LOADING_RESOURCE, "Loading next page URL: " + nextURL);
        logger.info("Attempting to load next page URL: " + nextURL);

        // Load next page
        Document nextPage = getPageWithRetries(new URI(nextURL).toURL());

        return nextPage;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {

        List<String> imageURLs = new ArrayList<>();

        logger.debug("Trying to get URLs from document... ");

        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = getFullSizedImage("https://www.imagefap.com" + thumb.parent().attr("href"));

            if (image == null) {
                for (int i = 0; i < HTTP_RETRY_LIMIT; i++) {
                    image = getFullSizedImage("https://www.imagefap.com" + thumb.parent().attr("href"));
                    if (image != null) {
                        break;
                    }
                    sleep(PAGE_SLEEP_TIME);
                }
                if (image == null)
                    throw new RuntimeException("Unable to extract image URL from single image page! Unable to continue");
            }

            logger.debug("Adding imageURL: '" + image + "'");

            imageURLs.add(image);
            if (isThisATest()) {
                break;
            }
        }
        logger.debug("Adding " + imageURLs.size() + " URLs to download");

        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer for image downloads
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title as GID
            String title = getCachedFirstPage().title();
            title = title.replace("Porn Pics & Porn GIFs", "");
            title = title.replace(" ", "_");
            String toReturn = getHost() + "_" + title + "_" + getGID(url);
            return toReturn.replaceAll("__", "_");
        } catch (IOException e) {
            return super.getAlbumTitle(url);
        }
    }

    private String getFullSizedImage(String pageURL) {
        try {
            // Sleep before fetching image.
            sleep(IMAGE_SLEEP_TIME);

            Document doc = getPageWithRetries(new URI(pageURL).toURL());

            String framedPhotoUrl = doc.select("img#mainPhoto").attr("data-src");

            // we use a no query param version of the URL to reduce failure rate because of some query params that change between the li elements and the mainPhotoURL
            String noQueryPhotoUrl = framedPhotoUrl.split("\\?")[0];

            logger.debug("noQueryPhotoUrl: " + noQueryPhotoUrl);

            // we look for a li > a element who's framed attribute starts with the noQueryPhotoUrl (only reference in the page to the full URL)
            Elements selectedItem = doc.select("ul.thumbs > li > a[framed^='"+noQueryPhotoUrl+"']");

            // the fullsize URL is in the href attribute
            String fullSizedUrl = selectedItem.attr("href");

            if("".equals(fullSizedUrl))
                throw new IOException("JSoup full URL extraction failed from '" + selectedItem.html() + "'");

            logger.debug("fullSizedUrl: " + fullSizedUrl);

            return fullSizedUrl;

        } catch (IOException | URISyntaxException e) {
            logger.debug("Unable to get full size image URL from page: " + pageURL + " because: " +  e.getMessage());
            return null;
        }
    }

    /**
     * Attempts to get page, checks for IP ban, waits.
     * @param url
     * @return Page document
     * @throws IOException If page loading errors, or if retries are exhausted
     */
    private Document getPageWithRetries(URL url) throws IOException {
        Document doc = null;
        int retries = RETRY_LIMIT;
        while (true) {

            sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());

            // For debugging rate limit checker. Useful to track wheter the timeout should be altered or not.
            callsMade++;
            checkRateLimit();

            logger.info("Retrieving " + url);

            boolean httpCallThrottled = false;
            int httpAttempts = 0;

            // we attempt the http call, knowing it can fail for network reasons
            while(true) {
                httpAttempts++;
                try {
                    doc = Http.url(url).get();
                } catch(IOException e) {

                    logger.info("Retrieving " + url + " error: " + e.getMessage());

                    if(e.getMessage().contains("404"))
                        throw new IOException("Gallery/Page not found!");

                    if(httpAttempts < HTTP_RETRY_LIMIT) {
                        sendUpdate(STATUS.DOWNLOAD_WARN, "HTTP call failed: " + e.getMessage() + " retrying " + httpAttempts + " / " + HTTP_RETRY_LIMIT);

                        // we sleep for a few seconds
                        sleep(PAGE_SLEEP_TIME);
                        continue;
                    } else {
                        sendUpdate(STATUS.DOWNLOAD_WARN, "HTTP call failed too many times: " + e.getMessage() + " treating this as a throttle");
                        httpCallThrottled = true;
                    }
                }
                // no errors, we exit
                break;
            }

            if (httpCallThrottled || (doc != null && doc.toString().contains("Your IP made too many requests to our servers and we need to check that you are a real human being"))) {
                if (retries == 0) {
                    throw new IOException("Hit rate limit and maximum number of retries, giving up");
                }
                String message = "Probably hit rate limit while loading " + url + ", sleeping for " + IP_BLOCK_SLEEP_TIME + "ms, " + retries + " retries remaining";
                logger.warn(message);
                sendUpdate(STATUS.DOWNLOAD_WARN, message);
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

    /**
     * Used for debugging the rate limit issue.
     * This in order to prevent hitting the rate limit altoghether by remaining under the limit threshold.
     * @return Long duration
     */
    private long checkRateLimit() {
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;

        int rateLimitMinute = 100;
        int rateLimitFiveMinutes = 200;
        int rateLimitHour = RATE_LIMIT_HOUR;        // Request allowed every 3.6 seconds.

        if(duration / 1000 < 60){
            logger.debug("Rate limit: " + (rateLimitMinute - callsMade) + " calls remaining for first minute mark.");
        } else if(duration / 1000 <  300){
            logger.debug("Rate limit: " + (rateLimitFiveMinutes - callsMade) + " calls remaining for first 5 minute mark.");
        } else if(duration / 1000 <  3600){
            logger.debug("Rate limit: " + (rateLimitHour - callsMade) + " calls remaining for first hour mark.");
        }

        return duration;
    }


}
