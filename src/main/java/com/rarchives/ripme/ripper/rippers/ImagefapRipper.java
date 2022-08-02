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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class ImagefapRipper extends AbstractHTMLRipper {

    private boolean isNewAlbumType = false;

    private int callsMade = 0;
    private long startTime = System.nanoTime();

    private static final int RETRY_LIMIT = 10;
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
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String gid = getGID(url);
        String newURL = "https://www.imagefap.com/gallery.php?";
        if (isNewAlbumType) {
            newURL += "p";
        }
        newURL += "gid=" + gid + "&view=2";
        LOGGER.debug("Changed URL from " + url + " to " + newURL);
        return new URL(newURL);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*imagefap.com/gallery.php\\?pgid=([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/gallery.php\\?gid=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/pictures/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/pictures/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/gallery/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/gallery/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
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
        return getPageWithRetries(url);
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextURL = null;
        for (Element a : doc.select("a.link3")) {
            if (a.text().contains("next")) {
                nextURL = "https://imagefap.com/gallery.php" + a.attr("href");
                break;
            }
        }
        if (nextURL == null) {
            throw new IOException("No next page found");
        }
        // Sleep before fetching next page.
        sleep(PAGE_SLEEP_TIME);

        // Load next page
        Document nextPage = getPageWithRetries(new URL(nextURL));

        return nextPage;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = getFullSizedImage("https://www.imagefap.com" + thumb.parent().attr("href"));
            imageURLs.add(image);
            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer for image downloads
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
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

            Document doc = getPageWithRetries(new URL(pageURL));
            return doc.select("img#mainPhoto").attr("src");
        } catch (IOException e) {
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
        Document doc;
        int retries = RETRY_LIMIT;
        while (true) {
            sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());

            // For debugging rate limit checker. Useful to track wheter the timeout should be altered or not.
            callsMade++;
            checkRateLimit();

            LOGGER.info("Retrieving " + url);
            doc = Http.url(url)
                      .get();


            if (doc.toString().contains("Your IP made too many requests to our servers and we need to check that you are a real human being")) {
                if (retries == 0) {
                    throw new IOException("Hit rate limit and maximum number of retries, giving up");
                }
                String message = "Hit rate limit while loading " + url + ", sleeping for " + IP_BLOCK_SLEEP_TIME + "ms, " + retries + " retries remaining";
                LOGGER.warn(message);
                sendUpdate(STATUS.DOWNLOAD_WARN, message);
                retries--;
                try {
                    Thread.sleep(IP_BLOCK_SLEEP_TIME);
                } catch (InterruptedException e) {
                    throw new IOException("Interrupted while waiting for rate limit to subside");
                }
            }
            else {
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
            LOGGER.debug("Rate limit: " + (rateLimitMinute - callsMade) + " calls remaining for first minute mark.");
        } else if(duration / 1000 <  300){
            LOGGER.debug("Rate limit: " + (rateLimitFiveMinutes - callsMade) + " calls remaining for first 5 minute mark.");
        } else if(duration / 1000 <  3600){
            LOGGER.debug("Rate limit: " + (RATE_LIMIT_HOUR - callsMade) + " calls remaining for first hour mark.");
        }

        return duration;
    }

}
