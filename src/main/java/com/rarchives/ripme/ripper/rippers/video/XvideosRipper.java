package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class XvideosRipper extends VideoRipper {

    private static final String HOST = "xvideos";

    public XvideosRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*xvideos\\.com/video[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*xvideos\\.com/video([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected xvideo format:"
                        + "xvideos.com/video####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url);
        Document doc = Http.url(this.url).get();
        Elements scripts = doc.select("script");
        for (Element e : scripts) {
            if (e.html().contains("html5player.setVideoUrlHigh")) {
                logger.info("Found the right script");
                String[] lines = e.html().split("\n");
                for (String line: lines) {
                    if (line.contains("html5player.setVideoUrlHigh")) {
                        String videoURL = line.replaceAll("\t", "").replaceAll("html5player.setVideoUrlHigh\\(", "").replaceAll("\'", "").replaceAll("\\);", "");
                        addURLToDownload(new URL(videoURL), HOST + "_" + getGID(this.url));
                        waitForThreads();
                        return;
                    }
                }
            }
        }
        throw new IOException("Unable to find video url at " + this.url.toExternalForm());
    }
}