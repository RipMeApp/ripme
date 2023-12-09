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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;

public class NfsfwRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "nfsfw.com",
                                HOST   = "nfsfw";


    private int index = 0;
    private String currentDir = "";
    private List<String> subalbumURLs = new ArrayList<>();
    private Pattern subalbumURLPattern = Pattern.compile(
            "https?://[wm.]*nfsfw.com/gallery/v/[^/]+/(.+)$"
    );

    // threads pool for downloading images from image pages
    private DownloadThreadPool nfsfwThreadPool;

    public NfsfwRipper(URL url) throws IOException {
        super(url);
        nfsfwThreadPool = new DownloadThreadPool("NFSFW");
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        String nextURL = null;
        Elements a = page.select("a.next");
        if (!a.isEmpty()){
            // Get next page of current album
            nextURL = "http://nfsfw.com" + a.first().attr("href");
        } else if (!subalbumURLs.isEmpty()){
            // Get next sub-album
            nextURL = subalbumURLs.remove(0);
            LOGGER.info("Detected subalbum URL at:" + nextURL);
            Matcher m = subalbumURLPattern.matcher(nextURL);
            if (m.matches()) {
                // Set the new save directory and save images with a new index
                this.currentDir = m.group(1);
                this.index = 0;
            } else {
                LOGGER.error("Invalid sub-album URL: " + nextURL);
                nextURL = null;
            }
        }
        // Wait
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting to load next page", e);
        }
        if (nextURL == null){
            throw new IOException("No more pages");
        } else {
            return Http.url(nextURL).get();
        }
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> imagePageURLs = getImagePageURLs(page);

        // Check if any sub-albums are present on this page
        List<String> subalbumURLs = getSubalbumURLs(page);
        this.subalbumURLs.addAll(subalbumURLs);

        return imagePageURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        // if we are now downloading a sub-album, all images in it
        // should be indexed starting from 0
        if (!this.currentDir.equals("")){
            index = ++this.index;
        }
        NfsfwImageThread t = new NfsfwImageThread(url, currentDir, index);
        nfsfwThreadPool.addThread(t);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        // always start on the first page of an album
        // (strip the options after the '?')
        String u = url.toExternalForm();
        if (u.contains("?")) {
            u = u.substring(0, u.indexOf("?"));
            return new URI(u).toURL();
        } else {
            return url;
        }
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("https?://[wm.]*nfsfw.com/gallery/v/(.*)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String group = m.group(1);
            if (group.endsWith("/")) {
                group = group.substring(0, group.length() - 1);
            }
            return group.replaceAll("/", "__");
        }

        throw new MalformedURLException(
                "Expected nfsfw.com gallery format: "
                        + "nfsfw.com/v/albumname"
                        + " Got: " + url);
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return nfsfwThreadPool;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        try {
            final var fstPage = getCachedFirstPage();
            List<String> imageURLs = getImagePageURLs(fstPage);
            List<String> subalbumURLs = getSubalbumURLs(fstPage);
            return imageURLs.isEmpty() && !subalbumURLs.isEmpty();
        } catch (IOException e) {
            LOGGER.error("Unable to load " + url, e);
            return false;
        }
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        return getSubalbumURLs(doc);
    }

    // helper methods

    private List<String> getImagePageURLs(Document page){
        // get image pages
        // NOTE: It might be possible to get the (non-thumbnail) image URL
        // without going to its page first as there seems to be a pattern
        // between the thumb and actual image URLs, but that is outside the
        // scope of the current issue being solved.
        List<String> imagePageURLs = new ArrayList<>();
        for (Element thumb : page.select("td.giItemCell > div > a")) {
            String imagePage = "http://nfsfw.com" + thumb.attr("href");
            imagePageURLs.add(imagePage);
        }
        return imagePageURLs;
    }

    private List<String> getSubalbumURLs(Document page){
        // Check if sub-albums are present on this page
        List<String> subalbumURLs = new ArrayList<>();
        for (Element suba : page.select("td.IMG > a")) {
            String subURL = "http://nfsfw.com" + suba.attr("href");
            subalbumURLs.add(subURL);
        }
        return subalbumURLs;
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class NfsfwImageThread implements Runnable {
        private final URL url;
        private final String subdir;
        private final int index;

        NfsfwImageThread(URL url, String subdir, int index) {
            super();
            this.url = url;
            this.subdir = subdir;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Document doc = Http.url(this.url)
                                   .referrer(this.url)
                                   .get();
                Elements images = doc.select(".gbBlock img");
                if (images.isEmpty()) {
                    LOGGER.error("Failed to find image at " + this.url);
                    return;
                }
                String file = images.first().attr("src");
                if (file.startsWith("/")) {
                    file = "http://nfsfw.com" + file;
                }
                addURLToDownload(new URI(file).toURL(), getPrefix(index), this.subdir);
            } catch (IOException | URISyntaxException e) {
                LOGGER.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}