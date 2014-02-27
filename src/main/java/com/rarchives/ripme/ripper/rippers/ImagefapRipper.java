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

import com.rarchives.ripme.ripper.AbstractRipper;

public class ImagefapRipper extends AbstractRipper {

    private static final String DOMAIN = "imagefap.com",
                                HOST   = "imagefap";
    private static final Logger logger = Logger.getLogger(ImagefapRipper.class);

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
        logger.debug("GID=" + gid);
        URL newURL = new URL("http://www.imagefap.com/gallery.php?gid="
                            + gid + "&view=2");
        logger.debug("Sanitized URL from " + url + " to " + newURL);
        return newURL;
    }

    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagefap.com/gallery.php\\?gid=([0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/pictures/([0-9]{1,}).*$");
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
        logger.debug("Retrieving " + this.url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm()).get();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replaceAll(
                    "http://x.*.fap.to/images/thumb/",
                    "http://fap.to/images/full/");
            processURL(new URL(image));
        }
    }

    public void processURL(URL url) {
        logger.info("Found " + url);
    }

    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        return true;
    }

}