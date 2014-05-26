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
import com.rarchives.ripme.utils.Utils;

public class KinkyshareRipper extends AlbumRipper {

    private static final String HOST   = "kinkyshare";
    private static final Logger logger = Logger.getLogger(KinkyshareRipper.class);

    public KinkyshareRipper(URL url) throws IOException {
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

        p = Pattern.compile("^.*kinkyshare.com/c/([0-9]+)/?.*$");
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
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        for (Element thumb : albumDoc.select(".thumbnail > a > img")) {
            if (!thumb.hasAttr("src")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replaceAll(
                    "/thumbs/",
                    "/images/");
            if (image.startsWith("/")) {
                image = "http://kinkyshare.com" + image;
            }
            index += 1; 
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(new URL(image), prefix);
        }
        waitForThreads();
    }

    public boolean canRip(URL url) {
        if (!url.toExternalForm().contains("kinkyshare.com/c/")) {
            return false;
        }
        return true;
    }

}