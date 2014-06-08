package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class DrawcrowdRipper extends AlbumRipper {

    private static final String DOMAIN = "drawcrowd.com",
                                HOST   = "drawcrowd";

    public DrawcrowdRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*drawcrowd.com/projects/.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            throw new MalformedURLException("Cannot rip drawcrowd.com/projects/ pages");
        }

        p = Pattern.compile("^.*drawcrowd.com/([a-zA-Z0-9\\-_]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected drawcrowd.com gallery format: "
                        + "drawcrowd.com/username"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        while (true) {
            if (isStopped()) {
                break;
            }
            for (Element thumb : albumDoc.select("div.item.asset img")) {
                String image = thumb.attr("src");
                image = image
                        .replaceAll("/medium/", "/large/")
                        .replaceAll("/small/", "/large/");
                index++;
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(image), prefix);
            }
            Elements loadMore = albumDoc.select("a#load-more");
            if (loadMore.size() == 0) {
                break;
            }
            String nextURL = "http://drawcrowd.com" + loadMore.get(0).attr("href");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to load next page", e);
                throw new IOException(e);
            }
            sendUpdate(STATUS.LOADING_RESOURCE, nextURL);
            albumDoc = Jsoup.connect(nextURL).get();
        }
        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

}