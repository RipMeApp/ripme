package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class FlickrRipper extends AbstractHTMLRipper {

    private int page = 1;
    private Set<String> attempted = new HashSet<>();
    private Document albumDoc = null;
    private final DownloadThreadPool flickrThreadPool;
    @Override
    public DownloadThreadPool getThreadPool() {
        return flickrThreadPool;
    }

    public FlickrRipper(URL url) throws IOException {
        super(url);
        flickrThreadPool = new DownloadThreadPool();
    }

    @Override
    public String getHost() {
        return "flickr";
    }
    @Override
    public String getDomain() {
        return "flickr.com";
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        String sUrl = url.toExternalForm();
        // Strip out https
        sUrl = sUrl.replace("https://secure.flickr.com", "http://www.flickr.com");
        // For /groups/ links, add a /pool to the end of the URL
        if (sUrl.contains("flickr.com/groups/") && !sUrl.contains("/pool")) {
            if (!sUrl.endsWith("/")) {
                sUrl += "/";
            }
            sUrl += "pool";
        }
        return new URL(sUrl);
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        if (!url.toExternalForm().contains("/sets/")) {
            return super.getAlbumTitle(url);
        }
        try {
            // Attempt to use album title as GID
            Document doc = getFirstPage();
            String user = url.toExternalForm();
            user = user.substring(user.indexOf("/photos/") + "/photos/".length());
            user = user.substring(0, user.indexOf("/"));
            String title = doc.select("meta[name=description]").get(0).attr("content");
            if (!title.equals("")) {
                return getHost() + "_" + user + "_" + title;
            }
        } catch (Exception e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        // Root:  https://www.flickr.com/photos/115858035@N04/
        // Album: https://www.flickr.com/photos/115858035@N04/sets/72157644042355643/

        final String domainRegex = "https?://[wm.]*flickr.com";
        final String userRegex = "[a-zA-Z0-9@]+";
        // Album
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ")/sets/([0-9]+)/?.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }

        // User page
        p = Pattern.compile("^" + domainRegex + "/photos/(" + userRegex + ").*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        // Groups page
        p = Pattern.compile("^" + domainRegex + "/groups/(" + userRegex + ").*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "groups-" + m.group(1);
        }
        throw new MalformedURLException(
                "Expected flickr.com URL formats: "
                        + "flickr.com/photos/username or "
                        + "flickr.com/photos/username/sets/albumid"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (isThisATest()) {
            return null;
        }
        // Find how many pages there are
        int lastPage = 0;
        for (Element apage : doc.select("a[data-track^=page-]")) {
            String lastPageStr = apage.attr("data-track").replace("page-", "");
            lastPage = Integer.parseInt(lastPageStr);
        }
        // If we're at the last page, stop.
        if (page >= lastPage) {
            throw new IOException("No more pages");
        }
        // Load the next page
        page++;
        albumDoc = null;
        String nextURL = this.url.toExternalForm();
        if (!nextURL.endsWith("/")) {
            nextURL += "/";
        }
        nextURL += "page" + page + "/";
        // Wait a bit
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting to load next page " + nextURL);
        }
        return Http.url(nextURL).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : page.select("a[data-track=photo-click]")) {
            /* TODO find a way to persist the image title
            String imageTitle = null;
            if (thumb.hasAttr("title")) {
                imageTitle = thumb.attr("title");
            }
            */
            String imagePage = thumb.attr("href");
            if (imagePage.startsWith("/")) {
                imagePage = "http://www.flickr.com" + imagePage;
            }
            if (imagePage.contains("/in/")) {
                imagePage = imagePage.substring(0, imagePage.indexOf("/in/") + 1);
            }
            if (!imagePage.endsWith("/")) {
                imagePage += "/";
            }
            imagePage += "sizes/o/";

            // Check for duplicates
            if (attempted.contains(imagePage)) {
                continue;
            }
            attempted.add(imagePage);
            imageURLs.add(imagePage);
            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Add image page to threadpool to grab the image & download it
        FlickrImageThread mit = new FlickrImageThread(url, index);
        flickrThreadPool.addThread(mit);
    }

    /**
     * Login to Flickr.
     * @return Cookies for logged-in session
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private Map<String,String> signinToFlickr() throws IOException {
        Response resp = Jsoup.connect("http://www.flickr.com/signin/")
                            .userAgent(USER_AGENT)
                            .followRedirects(true)
                            .method(Method.GET)
                            .execute();
        Document doc = resp.parse();
        Map<String,String> postData = new HashMap<>();
        for (Element input : doc.select("input[type=hidden]")) {
            postData.put(input.attr("name"),  input.attr("value"));
        }
        postData.put("passwd_raw",  "");
        postData.put(".save",   "");
        postData.put("login",   new String(Base64.decode("bGVmYWtlZGVmYWtl")));
        postData.put("passwd",  new String(Base64.decode("MUZha2V5ZmFrZQ==")));
        String action = doc.select("form[method=post]").get(0).attr("action");
        resp = Jsoup.connect(action)
                    .cookies(resp.cookies())
                    .data(postData)
                    .method(Method.POST)
                    .execute();
        return resp.cookies();
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class FlickrImageThread extends Thread {
        private URL    url;
        private int    index;

        FlickrImageThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Document doc = getLargestImagePageDocument(this.url);
                Elements fullsizeImages = doc.select("div#allsizes-photo img");
                if (fullsizeImages.isEmpty()) {
                    LOGGER.error("Could not find flickr image at " + doc.location() + " - missing 'div#allsizes-photo img'");
                }
                else {
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    synchronized (flickrThreadPool) {
                        addURLToDownload(new URL(fullsizeImages.first().attr("src")), prefix);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }

        private Document getLargestImagePageDocument(URL url) throws IOException {
            // Get current page
            Document doc = Http.url(url).get();
            // Look for larger image page
            String largestImagePage = this.url.toExternalForm();
            for (Element olSize : doc.select("ol.sizes-list > li > ol > li")) {
                Elements ola = olSize.select("a");
                if (ola.isEmpty()) {
                    largestImagePage = this.url.toExternalForm();
                }
                else {
                    String candImage = ola.get(0).attr("href");
                    if (candImage.startsWith("/")) {
                        candImage = "http://www.flickr.com" + candImage;
                    }
                    largestImagePage = candImage;
                }
            }
            if (!largestImagePage.equals(this.url.toExternalForm())) {
                // Found larger image page, get it.
                doc = Http.url(largestImagePage).get();
            }
            return doc;
        }
    }
}