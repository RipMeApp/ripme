package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;

public class ImagefapRipper extends AbstractRipper {

    private static final String HOST = "imagefap.com";

    private String gid;

    public ImagefapRipper(URL url) throws IOException {
        super(url);
        this.gid = getGID(url);
    }

    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    public void sanitizeURL() throws MalformedURLException {
        this.url = new URL("http://www.imagefap.com/gallery.php?gid="
                            + this.gid + "&view=2");
    }

    private static String getGID(URL url) throws MalformedURLException {
        String gid = null;
        Pattern p = Pattern.compile("^.*imagefap.com/gallery.php?gid=([0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            gid = m.group(1);
        } else {
            p = Pattern.compile("^.*imagefap.com/pictures/([0-9]{1,}).*$");
            m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                gid = m.group(1);
            }
        }
        if (gid == null) {
            throw new MalformedURLException(
                    "Expected imagefap.com gallery formats:"
                            + "imagefap.com/gallery.php?gid=####... or"
                            + "imagefap.com/pictures/####...");
        }
        return gid;
    }

    @Override
    public void setWorkingDir() throws IOException {
        String path = Utils.getWorkingDirectory().getCanonicalPath();
        path += this.gid + File.separator;
        this.workingDir = new File(path);
    }

    @Override
    public void rip() throws IOException {
        System.err.println("Connecting to " + this.url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm()).get();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replaceAll("http://x.*.fap.to/images/thumb/",
                    "http://fap.to/images/full/");
            processURL(image);
            System.err.println(image);
        }
    }

    public void processURL(String url) {

    }

    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(HOST)) {
            return false;
        }
        return true;
    }

}
