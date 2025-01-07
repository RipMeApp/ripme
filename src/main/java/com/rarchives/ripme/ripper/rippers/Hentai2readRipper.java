package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class Hentai2readRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(Hentai2readRipper.class);

    String lastPage;

    public Hentai2readRipper(URL url) throws IOException {
        super(url);
    }

        @Override
        public String getHost() {
            return "hentai2read";
        }

        @Override
        public String getDomain() {
            return "hentai2read.com";
        }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        logger.info("Page contains albums");
        Pattern pat = Pattern.compile("https?://hentai2read\\.com/([a-zA-Z0-9_-]*)/?");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select(".nav-chapters > li > div.media > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://hentai2read\\.com/([a-zA-Z0-9_-]*)/(\\d+)?/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        throw new MalformedURLException("Expected hentai2read.com URL format: " +
                            "hentai2read.com/COMICID - got " + url + " instead");
    }

        @Override
        public Document getFirstPage() throws IOException {
            String thumbnailLink;
            try {
                // If the page contains albums we want to load the main page
                if (pageContainsAlbums(url)) {
                    return Http.url(url).get();
                }
                Document tempDoc;
                tempDoc = Http.url(url).get();
                // Get the thumbnail page so we can rip all images without loading every page in the comic
                thumbnailLink = tempDoc.select("div.col-xs-12 > div.reader-controls > div.controls-block > button > a").attr("href");
                if (!thumbnailLink.equals("")) {
                    return Http.url(thumbnailLink).get();
                } else {
                    return Http.url(tempDoc.select("a[data-original-title=Thumbnails").attr("href")).get();
                }
            } catch (IOException e) {
                throw new IOException("Unable to get first page");
            }
        }

        @Override
        public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
            try {
                return getHost() + "_" + getGID(url);
            } catch (Exception e) {
                // Fall back to default album naming convention
                logger.warn("Failed to get album title from " + url, e);
            }
            return super.getAlbumTitle(url);
        }

        @Override
        public List<String> getURLsFromPage(Document doc) {
            List<String> result = new ArrayList<String>();
            for (Element el : doc.select("div.block-content > div > div.img-container > a > img.img-responsive")) {
                String imageURL = "https:" + el.attr("src");
                imageURL = imageURL.replace("hentaicdn.com", "static.hentaicdn.com");
                imageURL = imageURL.replace("thumbnails/", "");
                imageURL = imageURL.replace("tmb", "");
                result.add(imageURL);
            }
                return result;
        }

        @Override
        public Document getNextPage(Document doc) throws IOException {
            // Find next page
            String nextUrl = "";
            Element elem = doc.select("div.bg-white > ul.pagination > li > a").last();
            if (elem == null) {
                throw new IOException("No more pages");
            }
            nextUrl = elem.attr("href");
            // We use the global lastPage to check if we've already ripped this page
            // and is so we quit as there are no more pages
            if (nextUrl.equals(lastPage)) {
                throw new IOException("No more pages");
            }
            lastPage = nextUrl;
            // Sleep for half a sec to avoid getting IP banned
            sleep(500);
            return Http.url(nextUrl).get();
        }

        @Override
        public void downloadURL(URL url, int index) {
            addURLToDownload(url, getPrefix(index));
        }
    }
