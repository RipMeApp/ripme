package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;

public class ImagefapRipper extends AlbumRipper {

    private static final String DOMAIN = "imagefap.com",
                                HOST   = "imagefap";
    private static final Logger logger = Logger.getLogger(ImagefapRipper.class);

    private Document albumDoc = null;

    public ImagefapRipper(URL url) throws IOException {
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
        String gid = getGID(url);
        URL newURL = new URL("http://www.imagefap.com/gallery.php?gid="
                            + gid + "&view=2");
        logger.debug("Sanitized URL from " + url + " to " + newURL);
        return newURL;
    }
    
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                albumDoc = Jsoup.connect(url.toExternalForm()).get();
            }
            String title = albumDoc.title();
            Pattern p = Pattern.compile("^Porn pics of (.*) \\(Page 1\\)$");
            Matcher m = p.matcher(title);
            if (m.matches()) {
                return m.group(1);
            }
        } catch (IOException e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*imagefap.com/gallery.php\\?gid=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/pictures/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/gallery/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagefap.com gallery formats: "
                        + "imagefap.com/gallery.php?gid=####... or "
                        + "imagefap.com/pictures/####..."
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        logger.info("    Retrieving " + this.url.toExternalForm());
        if (albumDoc == null) {
            albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        }
        while (true) {
            if (isStopped()) {
                break;
            }
            for (Element thumb : albumDoc.select("#gallery img")) {
                if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                    continue;
                }
                String image = thumb.attr("src");
                image = image.replaceAll(
                        "http://x.*.fap.to/images/thumb/",
                        "http://fap.to/images/full/");
                index += 1;
                addURLToDownload(new URL(image), String.format("%03d_", index));
            }
            String nextURL = null;
            for (Element a : albumDoc.select("a.link3")) {
                if (a.text().contains("next")) {
                    nextURL = a.attr("href");
                    nextURL = "http://imagefap.com/gallery.php" + nextURL;
                    break;
                }
            }
            if (nextURL == null) {
                break;
            }
            else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting to load next page", e);
                    throw new IOException(e);
                }
                sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
                albumDoc = Jsoup.connect(nextURL).get();
            }
        }
        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

}