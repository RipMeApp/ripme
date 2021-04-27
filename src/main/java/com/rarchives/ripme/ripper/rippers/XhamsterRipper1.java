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
import com.rarchives.ripme.utils.Http;


// WARNING
// This ripper changes all requests to use the MOBILE version of the site
// If you're changing anything be sure to use the mobile sites html/css or you're just wasting your time!
// WARNING

public class XhamsterRipper1 extends AbstractHTMLRipper {

    public XhamsterRipper1(URL url) throws IOException {
        super(url);
    }

    private int index = 1;

    @Override public boolean hasASAPRipping() {
        return true;
    }

    @Override
    public String getHost() {
        return "xhamster7";
    }

    @Override
    public String getDomain() {
        return "xhamster7.desi";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        if (isVideoUrl(url)) {
            return url;
        }
        String URLToReturn = url.toExternalForm();
        URLToReturn = URLToReturn.replaceAll("https?://\\w?\\w?\\.?xhamster\\.", "https://m.xhamster.");
        URLToReturn = URLToReturn.replaceAll("https?://xhamster7\\.", "https://m.xhamster7.");
        URL san_url = new URL(URLToReturn);
        LOGGER.info("sanitized URL is " + san_url.toExternalForm());
        return san_url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[\\w\\w.]*xhamster7?\\.com/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^https?://[\\w\\w.]*xhamster7?\\.com/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "user_" + m.group(1);
        }
        p = Pattern.compile("^https?://.*xhamster7?\\.com/(movies|videos)/(.*)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

            throw new MalformedURLException(
                "Expected xhamster.com gallery formats: "
                        + "xhamster.com/photos/gallery/xxxxx-#####"
                        + " Got: " + url);
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        LOGGER.info("getting albums");
        for (Element elem : doc.select("div.item-container > a.item")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        LOGGER.info(doc.html());
        return urlsToAddToQueue;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern p = Pattern.compile("^https?://[\\w\\w.]*xhamster\\.com/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        Matcher m = p.matcher(url.toExternalForm());
        LOGGER.info("Checking if page has albums");
        LOGGER.info(m.matches());
        return m.matches();
    }


    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://([\\w\\w]*\\.)?xhamster7?\\.(com|one|desi)/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("^https?://[\\w\\w.]*xhamster7?\\.(com|one|desi)/users/([a-zA-Z0-9_-]+)/(photos|videos)(/\\d+)?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("^https?://.*xhamster7?\\.(com|one|desi)/(movies|videos)/.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        return false;
    }

    private boolean isVideoUrl(URL url) {
        Pattern p = Pattern.compile("^https?://.*xhamster7?\\.(com|one|desi)/(movies|videos)/.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("a[data-page=next]").first() != null) {
            String nextPageUrl = doc.select("a[data-page=next]").first().attr("href");
            if (nextPageUrl.startsWith("http")) {
                nextPageUrl = nextPageUrl.replaceAll("https?://\\w?\\w?\\.?xhamster\\.", "https://m.xhamster.");
                nextPageUrl = nextPageUrl.replaceAll("https?://xhamster7\\.", "https://m.xhamster7.");
                return Http.url(nextPageUrl).get();
            }
        }
        throw new IOException("No more pages");

    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        LOGGER.debug("Checking for urls");
        List<String> result = new ArrayList<>();
        if (!isVideoUrl(url)) {
          for (Element page : doc.select("div.picture_view > div.pictures_block > div.items > div.item-container > a.item")) {
              // Make sure we don't waste time running the loop if the ripper has been stopped
              if (isStopped()) {
                  break;
              }
              String pageWithImageUrl = page.attr("href");
              try {
                  // This works around some redirect fuckery xhamster likes to do where visiting m.xhamster.com sends to
                  // the page chamster.com but displays the mobile site from m.xhamster.com
                  pageWithImageUrl = pageWithImageUrl.replaceAll("://xhamster\\.", "://m.xhamster.");
                  pageWithImageUrl = pageWithImageUrl.replaceAll("://xhamster7\\.", "://m.xhamster.");
                  String image = Http.url(new URL(pageWithImageUrl)).get().select("a > img#photoCurr").attr("src");
                  downloadFile(image);
              } catch (IOException e) {
                  LOGGER.error("Was unable to load page " + pageWithImageUrl);
              }
          }
        } else {
            String imgUrl = doc.select("div.player-container > a").attr("href");
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
            addURLToDownload(new URL(url), getPrefix(index));
            index = index + 1;
        } catch (MalformedURLException e) {
            LOGGER.error("The url \"" + url + "\" is malformed");
        }
    }
    
    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title and username as GID
            Document doc = getFirstPage();
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
