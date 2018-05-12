package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImagebamRipper extends AbstractHTMLRipper {

    // Current HTML document
    private Document albumDoc = null;

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool imagebamThreadPool = new DownloadThreadPool("imagebam");
    @Override
    public DownloadThreadPool getThreadPool() {
        return imagebamThreadPool;
    }

    public ImagebamRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imagebam";
    }
    @Override
    public String getDomain() {
        return "imagebam.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^https?://[wm.]*imagebam.com/gallery/([a-zA-Z0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagebam gallery format: "
                        + "http://www.imagebam.com/gallery/galleryid"
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
        // Find next page
        Elements hrefs = doc.select("a.pagination_current + a.pagination_link");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = "http://www.imagebam.com" + hrefs.first().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div > a[target=_blank]:not(.footera)")) {
            imageURLs.add(thumb.attr("href"));
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        ImagebamImageThread t = new ImagebamImageThread(url, index);
        imagebamThreadPool.addThread(t);
        sleep(500);
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Elements elems = getFirstPage().select("legend");
            String title = elems.first().text();
            logger.info("Title text: '" + title + "'");
            Pattern p = Pattern.compile("^(.*)\\s\\d* image.*$");
            Matcher m = p.matcher(title);
            if (m.matches()) {
                return getHost() + "_" + getGID(url) + " (" + m.group(1).trim() + ")";
            }
            logger.info("Doesn't match " + p.pattern());
        } catch (Exception e) {
            // Fall back to default album naming convention
            logger.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    /**
     * Helper class to find and download images found on "image" pages
     *
     * Handles case when site has IP-banned the user.
     */
    private class ImagebamImageThread extends Thread {
        private URL url; //link to "image page"
        private int index; //index in album

        ImagebamImageThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            fetchImage();
        }
        
        /**
         * Rips useful image from "image page"
         */
        private void fetchImage() {
            try {
                Document doc = Http.url(url).get();
                // Find image
                Elements metaTags = doc.getElementsByTag("meta");
                
                String imgsrc = "";//initialize, so no NullPointerExceptions should ever happen.
                
                for (Element metaTag: metaTags) {
                    //the direct link to the image seems to always be linked in the <meta> part of the html.
                    if (metaTag.attr("property").equals("og:image")) {
                        imgsrc = metaTag.attr("content");
                        logger.info("Found URL " + imgsrc);
                        break;//only one (useful) image possible for an "image page".
                    }
                }
               
                //for debug, or something goes wrong.
                if (imgsrc.isEmpty()) {
                    logger.warn("Image not found at " + this.url);
                    return;
                }
               
                // Provide prefix and let the AbstractRipper "guess" the filename
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                
                addURLToDownload(new URL(imgsrc), prefix);
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}
