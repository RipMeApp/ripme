package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.rippers.ripperhelpers.ChanSite;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.utils.RipUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ChanRipper extends AbstractHTMLRipper {

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

    private static List<ChanSite> bakedin_explicit_domains = Arrays.asList(
            new ChanSite("boards.4chan.org",   Arrays.asList("4cdn.org", "is.4chan.org", "is2.4chan.org", "is3.4chan.org")),
            new ChanSite("boards.4channel.org",   Arrays.asList("4cdn.org", "is.4chan.org", "is2.4chan.org", "is3.4chan.org")),
            new ChanSite("4archive.org",  "imgur.com"),
            new ChanSite("archive.4plebs.org", "img.4plebs.org"),
            new ChanSite("yuki.la", "ii.yuki.la"),
            new ChanSite("55chan.org"),
            new ChanSite("desuchan.net"),
            new ChanSite("boards.420chan.org"),
            new ChanSite("7chan.org"),
            new ChanSite("desuarchive.org", "desu-usergeneratedcontent.xyz"),
            new ChanSite("8ch.net", "media.8ch.net"),
            new ChanSite("thebarchive.com"),
            new ChanSite("archiveofsins.com"),
            new ChanSite("archive.nyafuu.org"),
            new ChanSite("rbt.asia")
        );
    private static List<ChanSite> user_give_explicit_domains = getChansFromConfig(Utils.getConfigString("chans.chan_sites", null));
    private static List<ChanSite> explicit_domains = new ArrayList<>();

    /**
     * reads a string in the format of site1[cdn|cdn2|cdn3], site2[cdn]
     */
    public static List<ChanSite> getChansFromConfig(String rawChanString) {
        List<ChanSite> userChans = new ArrayList<>();
        if (rawChanString != null) {
            String[] listOfChans = rawChanString.split(",");
            for (String chanInfo : listOfChans) {
                // If this is true we're parsing a chan with cdns
                if (chanInfo.contains("[")) {
                    String siteUrl = chanInfo.split("\\[")[0];
                    String[] cdns = chanInfo.replaceAll(siteUrl + "\\[", "").replaceAll("]", "").split("\\|");
                    LOGGER.debug("site url: " + siteUrl);
                    LOGGER.debug("cdn: " + Arrays.toString(cdns));
                    userChans.add(new ChanSite(siteUrl, Arrays.asList(cdns)));
                } else {
                    // We're parsing a site without cdns
                    LOGGER.debug("site: " + chanInfo);
                    userChans.add(new ChanSite(chanInfo));
                }
            }
            return userChans;
        }
        return null;
    }

    private static List<String> url_piece_blacklist = Arrays.asList(
        "=http",
        "http://imgops.com/",
        "iqdb.org",
        "saucenao.com"
        );

    private ChanSite chanSite;
    private boolean generalChanSite = true;

    public ChanRipper(URL url) throws IOException {
        super(url);
        for (ChanSite _chanSite : explicit_domains) {
            LOGGER.info(_chanSite.domains);
            if (_chanSite.domains.contains(url.getHost())) {
                chanSite = _chanSite;
                generalChanSite = false;
            }
        }
        if (chanSite == null) {
            chanSite = new ChanSite(Arrays.asList(url.getHost()));
        }
    }

    @Override
    public String getHost() {
        String host = this.url.getHost();
        host = host.substring(0, host.lastIndexOf('.'));
        if (host.contains(".")) {
            // Host has subdomain (www)
            host = host.substring(host.lastIndexOf('.') + 1);
        }
        String board = this.url.toExternalForm().split("/")[3];
        return host + "_" + board;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Document doc = getCachedFirstPage();
            try {
                String subject = doc.select(".post.op > .postinfo > .subject").first().text();
                return getHost() + "_" + getGID(url) + "_" + subject;
            } catch (NullPointerException e) {
                LOGGER.warn("Failed to get thread title from " + url);
            }
        } catch (Exception e) {
            // Fall back to default album naming convention
            LOGGER.warn("Failed to get album title from " + url, e);
        }
        // Fall back on the GID
        return getHost() + "_" + getGID(url);
    }

    @Override
    public boolean canRip(URL url) {
        explicit_domains.addAll(bakedin_explicit_domains);
        if (user_give_explicit_domains != null) {
            explicit_domains.addAll(user_give_explicit_domains);
        }
        for (ChanSite _chanSite : explicit_domains) {
            if (_chanSite.domains.contains(url.getHost())) {
                return true;
            }
        }

        return false;
    }

    /**
     * For example the archives are all known. (Check 4chan-x)
     * Should be based on the software the specific chan uses.
     * FoolFuuka uses the same (url) layout as 4chan
     *
     * @param url
     * @return
     *      The thread id in string form
     * @throws java.net.MalformedURLException */
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        String u = url.toExternalForm();
        if (u.contains("/thread/") || u.contains("/res/") || u.contains("yuki.la") || u.contains("55chan.org")) {
            p = Pattern.compile("^.*\\.[a-z]{1,4}/[a-zA-Z0-9]+/(thread|res)/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(2);
            }

            // Drawchan is weird, has drawchan.net/dc/dw/res/####.html
            p = Pattern.compile("^.*\\.[a-z]{1,3}/[a-zA-Z0-9]+/[a-zA-Z0-9]+/res/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }
            // xchan
            p = Pattern.compile("^.*\\.[a-z]{1,3}/board/[a-zA-Z0-9]+/thread/([0-9]+)/?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }

            // yuki.la
            p = Pattern.compile("https?://yuki.la/[a-zA-Z0-9]+/([0-9]+)");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }

            //55chan.org
            p = Pattern.compile("https?://55chan.org/[a-z0-9]+/(res|thread)/[0-9]+.html");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }
        }

        throw new MalformedURLException(
                "Expected *chan URL formats: "
                        + ".*/@/(res|thread)/####.html"
                        + " Got: " + u);
    }

    @Override
    public String getDomain() {
        return this.url.getHost();
    }

    @Override
    public Document getFirstPage() throws IOException {

        Document firstPage = getPageWithRetries(url);

        sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, "Loading first page...");

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

        sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, "Loading next page URL: " + nextURL);
        LOGGER.info("Attempting to load next page URL: " + nextURL);

        // Load next page
        Document nextPage = getPageWithRetries(new URI(nextURL).toURL());

        return nextPage;
    }


    private boolean isURLBlacklisted(String url) {
        for (String blacklist_item : url_piece_blacklist) {
            if (url.contains(blacklist_item)) {
                LOGGER.debug("Skipping link that contains '"+blacklist_item+"': " + url);
                return true;
            }
        }
        return false;
    }
    @Override
    public List<String> getURLsFromPage(Document page) throws URISyntaxException {
        List<String> imageURLs = new ArrayList<>();
        Pattern p; Matcher m;
        for (Element link : page.select("a")) {
            if (!link.hasAttr("href")) {
                continue;
            }
            String href = link.attr("href").trim();

            if (isURLBlacklisted(href)) {
                continue;
            }
            //Check all blacklist items
            Boolean self_hosted = false;
            if (!generalChanSite) {
                for (String cdnDomain : chanSite.cdnDomains) {
                    if (href.contains(cdnDomain)) {
                        self_hosted = true;
                    }
                }
            }

            if (self_hosted || generalChanSite) {
                p = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|apng|webp|tif|tiff|webm|mp4)$", Pattern.CASE_INSENSITIVE);
                m = p.matcher(href);
                if (m.matches()) {
                    if (href.startsWith("//")) {
                        href = "http:" + href;
                    }
                    if (href.startsWith("/")) {
                        href = "http://" + this.url.getHost() + href;
                    }
                    // Don't download the same URL twice
                    if (imageURLs.contains(href)) {
                        LOGGER.debug("Already attempted: " + href);
                        continue;
                    }
                    imageURLs.add(href);
                    if (isThisATest()) {
                        break;
                    }
                }
            } else {
                //Copied code from RedditRipper, getFilesFromURL should also implement stuff like flickr albums
                URL originalURL;
                try {
                    originalURL = new URI(href).toURL();
                } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                    continue;
                }

                List<URL> urls = RipUtils.getFilesFromURL(originalURL);
                for (URL imageurl : urls) {
                    imageURLs.add(imageurl.toString());
                }
            }

            if (isStopped()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
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

            sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, url.toExternalForm());

            // For debugging rate limit checker. Useful to track wheter the timeout should be altered or not.
            callsMade++;
            checkRateLimit();

            LOGGER.info("Retrieving " + url);

            boolean httpCallThrottled = false;
            int httpAttempts = 0;

            // we attempt the http call, knowing it can fail for network reasons
            while(true) {
                httpAttempts++;
                try {
                    doc = Http.url(url).get();
                } catch(IOException e) {

                    LOGGER.info("Retrieving " + url + " error: " + e.getMessage());

                    if(e.getMessage().contains("404"))
                        throw new IOException("Gallery/Page not found!");

                    if(httpAttempts < HTTP_RETRY_LIMIT) {
                        sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "HTTP call failed: " + e.getMessage() + " retrying " + httpAttempts + " / " + HTTP_RETRY_LIMIT);

                        // we sleep for a few seconds
                        sleep(PAGE_SLEEP_TIME);
                        continue;
                    } else {
                        sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "HTTP call failed too many times: " + e.getMessage() + " treating this as a throttle");
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
                LOGGER.warn(message);
                sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, message);
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
            LOGGER.debug("Rate limit: " + (rateLimitMinute - callsMade) + " calls remaining for first minute mark.");
        } else if(duration / 1000 <  300){
            LOGGER.debug("Rate limit: " + (rateLimitFiveMinutes - callsMade) + " calls remaining for first 5 minute mark.");
        } else if(duration / 1000 <  3600){
            LOGGER.debug("Rate limit: " + (RATE_LIMIT_HOUR - callsMade) + " calls remaining for first hour mark.");
        }

        return duration;
    }


}
