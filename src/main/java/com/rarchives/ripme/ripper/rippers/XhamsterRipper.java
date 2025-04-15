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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

// WARNING
// This ripper changes all requests to use the MOBILE version of the site
// If you're changing anything be sure to use the mobile sites html/css or you're just wasting your time!
// WARNING

public class XhamsterRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(XhamsterRipper.class);

    private int index = 1;

    public XhamsterRipper(URL url) throws IOException {
        super(url);
    }

    @Override public boolean hasASAPRipping() {
        return true;
    }

    @Override
    public String getHost() {
        return "xhamster";
    }

    @Override
    public String getDomain() {
        return "xhamster.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        if (isVideoUrl(url)) {
            return url;
        }
        String URLToReturn = url.toExternalForm();
        URLToReturn = URLToReturn.replaceAll("https?://\\w?\\w?\\.?xhamster([^<]*)\\.", "https://m.xhamster$1.");
        URL san_url = new URI(URLToReturn).toURL();
        logger.info("sanitized URL is " + san_url.toExternalForm());
        return san_url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([\\w\\w]*\\.)?xhamster([^<]*)\\.(com|desi)/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(4);
        }
        p = Pattern.compile("^https?://[\\w\\w.]*xhamster([^<]*)\\.(com|desi)/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "user_" + m.group(1);
        }
        p = Pattern.compile("^https?://.*xhamster([^<]*)\\.(com|desi)/(movies|videos)/(.*$)");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(4);
        }

        throw new MalformedURLException(
                "Expected xhamster.com gallery formats: "
                        + "xhamster.com/photos/gallery/xxxxx-#####"
                        + " Got: " + url);
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        logger.info("getting albums");
        for (Element elem : doc.select("div.item-container > a.item")) {
            urlsToAddToQueue.add(elem.attr("href"));
            if (isStopped() || isThisATest()) {
                break;
            }
        }
        logger.info(doc.html());
        return urlsToAddToQueue;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern p = Pattern.compile("^https?://[\\w\\w.]*xhamster([^<]*)\\.(com|desi)/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        Matcher m = p.matcher(url.toExternalForm());
        logger.info("Checking if page has albums");
        logger.info(m.matches());
        return m.matches();
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://([\\w\\w]*\\.)?xhamster([^<]*)\\.(com|desi)/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("^https?://[\\w\\w.]*xhamster([^<]*)\\.(com|desi)/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("^https?://.*xhamster([^<]*)\\.(com|desi)/(movies|videos)/(.*$)");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        return false;
    }

    private boolean isVideoUrl(URL url) {
        Pattern p = Pattern.compile("^https?://.*xhamster([^<]*)\\.(com|desi)/(movies|videos)/(.*$)");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("a.prev-next-list-link").first() != null) {
            String nextPageUrl = doc.select("a.prev-next-list-link--next").first().attr("href");
            if (nextPageUrl.startsWith("http")) {
                nextPageUrl = nextPageUrl.replaceAll("https?://\\w?\\w?\\.?xhamster([^<]*)\\.", "https://m.xhamster$1.");
                return Http.url(nextPageUrl).get();
            }
        }
        throw new IOException("No more pages");

    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        return super.getFirstPage();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        logger.debug("Checking for urls");
        List<String> result = new ArrayList<>();
        if (!isVideoUrl(url)) {
            if (!doc.select("div.picture_view > div.pictures_block > div.items > div.item-container > a.item").isEmpty()) {
                // Old HTML structure is still present at some places
                for (Element page : doc.select(".clearfix > div > a.slided")) {
                    // Make sure we don't waste time running the loop if the ripper has been stopped
                    if (isStopped()) {
                        break;
                    }
                    String pageWithImageUrl = page.attr("href");
                    try {
                        // This works around some redirect fuckery xhamster likes to do where visiting m.xhamster.com sends to
                        // the page chamster.com but displays the mobile site from m.xhamster.com
                        pageWithImageUrl = pageWithImageUrl.replaceAll("://xhamster([^<]*)\\.", "://m.xhamster$1.");
                        String image = Http.url(new URI(pageWithImageUrl).toURL()).get().select("a > img#photoCurr").attr("src");
                        result.add(image);
                        downloadFile(image);
                    } catch (IOException | URISyntaxException e) {
                        logger.error("Was unable to load page " + pageWithImageUrl);
              }
              if (isStopped() || isThisATest()) {
                  break;
                    }
                }
            } else {
                // New HTML structure
                for (Element page : doc.select("div#photo-slider > div#photo_slider > a")) {
                    // Make sure we don't waste time running the loop if the ripper has been stopped
                    if (isStopped()) {
                        break;
                    }
                    String image = page.attr("href");
                    // This works around some redirect fuckery xhamster likes to do where visiting m.xhamster.com sends to
                    // the page chamster.com but displays the mobile site from m.xhamster.com
                    image = image.replaceAll("://xhamster([^<]*)\\.", "://m.xhamster$1.");
                    result.add(image);
                    downloadFile(image);
                }
            }
        } else {
            String imgUrl = doc.select("div.player-container > a").attr("href");
            result.add(imgUrl);
            downloadFile(imgUrl);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private void downloadFile(String url) {
        try {
            addURLToDownload(new URI(url).toURL(), getPrefix(index));
            index = index + 1;
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error("The url \"" + url + "\" is malformed");
        }
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title and username as GID
            Document doc = getCachedFirstPage();
            Element user = doc.select("a.author").first();
            String username = user.text();
            String path = url.getPath();
            Pattern p = Pattern.compile("^/photos/gallery/(.*)$");
            Matcher m = p.matcher(path);
            if (m.matches() && !username.isEmpty()) {
                return getHost() + "_" + username + "_" + m.group(1);
            }
        } catch (IOException | NullPointerException e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }
}
