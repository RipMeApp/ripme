package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NhentaiRipper extends AbstractHTMLRipper {

    // All sleep times are in milliseconds
    private static final int IMAGE_SLEEP_TIME = 1500;

    private String albumTitle;
    private Document firstPage;

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool nhentaiThreadPool = new DownloadThreadPool("nhentai");

    @Override
    public DownloadThreadPool getThreadPool() {
        return nhentaiThreadPool;
    }

    public NhentaiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "nhentai.net";
    }

    @Override
    public String getHost() {
        return "nhentai";
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        if (firstPage == null) {
            try {
                firstPage = Http.url(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String title = firstPage.select("#info > h1").text();
        if (title == null) {
            return getAlbumTitle(url);
        }
        return title;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // Ex: https://nhentai.net/g/159174/
        Pattern p = Pattern.compile("^https?://nhentai\\.net/g/(\\d+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Return the text contained between () in the regex - 159174 in this case
            return m.group(1);
        }
        throw new MalformedURLException("Expected nhentai.net URL format: " +
                "nhentai.net/g/albumid - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (firstPage == null) {
            firstPage = Http.url(url).get();
        }
        return firstPage;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();
        Elements thumbs = page.select(".gallerythumb");
        for (Element el : thumbs) {
            String imageUrl = el.attr("href");
            imageURLs.add("https://nhentai.net" + imageUrl);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        NHentaiImageThread t = new NHentaiImageThread(url, index, this.workingDir);
        nhentaiThreadPool.addThread(t);
        try {
            Thread.sleep(IMAGE_SLEEP_TIME);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting to load next image", e);
        }
    }

    private class NHentaiImageThread extends Thread {

        private URL url;
        private int index;
        private File workingDir;

        NHentaiImageThread(URL url, int index, File workingDir) {
            super();
            this.url = url;
            this.index = index;
            this.workingDir = workingDir;
        }

        @Override
        public void run() {
            fetchImage();
        }

        private void fetchImage() {
            try {
                //Document doc = getPageWithRetries(this.url);
                Document doc = Http.url(this.url).get();

                // Find image
                Elements images = doc.select("#image-container > a > img");
                if (images.size() == 0) {
                    // Attempt to find image elsewise (Issue #41)
                    images = doc.select("img#img");
                    if (images.size() == 0) {
                        logger.warn("Image not found at " + this.url);
                        return;
                    }
                }
                Element image = images.first();
                String imgsrc = image.attr("src");
                logger.info("Found URL " + imgsrc + " via " + images.get(0));

                Pattern p = Pattern.compile("^https?://i.nhentai.net/galleries/\\d+/(.+)$");
                Matcher m = p.matcher(imgsrc);
                if (m.matches()) {
                    // Manually discover filename from URL
                    String savePath = this.workingDir + File.separator;
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        savePath += String.format("%03d_", index);
                    }
                    savePath += m.group(1);
                    addURLToDownload(new URL(imgsrc), new File(savePath));
                } else {
                    // Provide prefix and let the AbstractRipper "guess" the filename
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    addURLToDownload(new URL(imgsrc), prefix);
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }

    }
}
