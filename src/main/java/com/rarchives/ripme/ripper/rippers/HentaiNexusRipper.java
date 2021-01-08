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
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class HentaiNexusRipper extends AbstractHTMLRipper {

    private Document firstPage;
    private DownloadThreadPool hentainexusThreadPool = new DownloadThreadPool("hentainexus");
    @Override
    public DownloadThreadPool getThreadPool() {
        return hentainexusThreadPool;
    }

    public HentaiNexusRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hentainexus";
    }

    @Override
    public String getDomain() {
        return "hentainexus.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://hentainexus\\.com/view/([a-zA-Z0-9_\\-%]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected hentainexus.com URL format: " +
                        "hentainexus.com/view/NUMBER - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        if (firstPage == null) {
           firstPage = Http.url(url).get();
        }
        return firstPage;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        Elements thumbs = doc.select("div.is-multiline > div.column > a");
        for (Element el : thumbs) {
           imageURLs.add("https://" + getDomain() + el.attr("href"));
        }
        return imageURLs;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            Document gallery = Http.url(url).get();
            return getHost() + "_" + gallery.select("h1.title").text();
        } catch (IOException e) {
            LOGGER.info("Falling back");
        }

       return super.getAlbumTitle(url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        HentaiNexusImageThread t = new HentaiNexusImageThread(url, index);
        hentainexusThreadPool.addThread(t);
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class HentaiNexusImageThread extends Thread {
        private URL url;
        private int index;

        HentaiNexusImageThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            fetchImage();
        }

        private void fetchImage() {
            try {
                Document doc = Http.url(url).retries(3).get();
                Elements images = doc.select("figure.image > img");
                if (images.isEmpty()) {
                    LOGGER.warn("Image not found at " + this.url);
                    return;
                }
                Element image = images.first();
                String imgsrc = image.attr("src");
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(imgsrc), prefix);
            } catch (IOException e) {
                LOGGER.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}
