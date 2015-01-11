package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class CheebyRipper extends AbstractHTMLRipper {

    private int offset = 0;
    private Map<String, Integer> albumSets = new HashMap<String, Integer>();

    public CheebyRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "cheeby";
    }
    @Override
    public String getDomain() {
        return "cheeby.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[w.]*cheeby.com/u/([a-zA-Z0-9\\-_]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("cheeby user not found in " + url + ", expected http://cheeby.com/u/username");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL("http://cheeby.com/u/" + getGID(url) + "/pics");
    }

    @Override
    public Document getFirstPage() throws IOException {
        String url = this.url + "?limit=10&offset=0";
        return Http.url(url)
                   .get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        sleep(500);
        offset += 1;
        String url = this.url + "?p=" + offset;
        Document nextDoc = Http.url(url).get();
        if (nextDoc.select("div.i a img").size() == 0) {
            throw new IOException("No more images to fetch");
        }
        return nextDoc;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Not implmeneted here
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        // Not implemented here
        return null;
    }

    public List<Image> getImagesFromPage(Document page) {
        List<Image> imageURLs = new ArrayList<Image>();
        for (Element image : page.select("div.i a img")) {
            // Get image URL
            String imageURL = image.attr("src");
            imageURL = imageURL.replace("s.", ".");

            // Get "album" from image link
            String href = image.parent().attr("href");
            while (href.endsWith("/")) {
                href = href.substring(0, href.length() - 2);
            }
            String[] hrefs = href.split("/");
            String prefix = hrefs[hrefs.length - 1];

            // Keep track of how many images are in this album
            int albumSetCount = 0;
            if (albumSets.containsKey(prefix)) {
                albumSetCount = albumSets.get(prefix);
            }
            albumSetCount++;
            albumSets.put(prefix, albumSetCount);

            imageURLs.add(new Image(imageURL, prefix, albumSetCount));

        }
        return imageURLs;
    }
    
    @Override
    public void rip() throws IOException {
        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        Document doc = getFirstPage();
        
        while (doc != null) {
            List<Image> images = getImagesFromPage(doc);

            if (images.size() == 0) {
                throw new IOException("No images found at " + doc.location());
            }
            
            for (Image image : images) {
                if (isStopped()) {
                    break;
                }
                // Don't create subdirectory if "album" only has 1 image
                if (albumSets.get(image.prefix) > 1) {
                    addURLToDownload(new URL(image.url), getPrefix(image.index), image.prefix);
                }
                else {
                    addURLToDownload(new URL(image.url));
                }
            }

            if (isStopped()) {
                break;
            }

            try {
                sendUpdate(STATUS.LOADING_RESOURCE, "next page");
                doc = getNextPage(doc);
            } catch (IOException e) {
                logger.info("Can't get next page: " + e.getMessage());
                break;
            }
        }

        // If they're using a thread pool, wait for it.
        if (getThreadPool() != null) {
            getThreadPool().waitForThreads();
        }
        waitForThreads();

        // Delete empty subdirectories
        for (String prefix : albumSets.keySet()) {
            if (prefix.trim().equals("")) {
                continue;
            }
            File f = new File(this.workingDir, prefix);
            if (f.list() != null && f.list().length == 0) {
                logger.info("Deleting empty directory: " + f.getAbsolutePath());
                f.delete();
            }
        }
    }
    
    private class Image {
        String url, prefix;
        int index;
        public Image(String url, String prefix, int index) {
            this.url = url;
            this.prefix = prefix;
            this.index = index;
        }
    }
}
