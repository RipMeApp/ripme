package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;

public class XvideosRipper extends AbstractSingleFileRipper {

    private static final Logger logger = LogManager.getLogger(XvideosRipper.class);

    private static final String HOST = "xvideos";

    private static final Pattern videoPattern = Pattern.compile("^https?://[wm.]*xvideos\\.com/video\\.([^/]*)(.*)$");
    private static final Pattern albumPattern = Pattern.compile("^https?://[wm.]*xvideos\\.com/(profiles|amateurs)/([a-zA-Z0-9_-]+)/photos/(\\d+)/([a-zA-Z0-9_-]+)$");

    public XvideosRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return HOST + ".com";
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = videoPattern;
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        p = albumPattern;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = videoPattern;
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = albumPattern;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(3);
        }

        throw new MalformedURLException(
                "Expected xvideo format:"
                        + "xvideos.com/video####"
                        + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        Pattern p = videoPattern;
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            Elements scripts = doc.select("script");
            for (Element e : scripts) {
                if (e.html().contains("html5player.setVideoUrlHigh")) {
                    logger.info("Found the right script");
                    String[] lines = e.html().split("\n");
                    for (String line : lines) {
                        if (line.contains("html5player.setVideoUrlHigh")) {
                            String videoURL = line.strip().replaceAll("\t", "").replaceAll("html5player.setVideoUrlHigh\\(", "").replaceAll("\'", "").replaceAll("\\);", "");
                            results.add(videoURL);
                        }
                    }
                }
            }
        } else {
            for (Element e : doc.select("div.thumb > a")) {
                results.add(e.attr("href"));
                if (isThisATest()) {
                    break;
                }
            }
        }
        return results;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        Pattern p;
        Matcher m;

        p = videoPattern;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return getHost() + "_" + m.group(1) + "_" + m.group(2);
        }

        p = albumPattern;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return getHost() + "_" + m.group(1) + "_" + m.group(2) + "_" + m.group(4) + "_" + m.group(3);
        }

        return super.getAlbumTitle(url);
    }
}
