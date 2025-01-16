package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class NudeGalsRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(NudeGalsRipper.class);

    private static final Pattern ALBUM_PATTERN = Pattern.compile("^.*nude-gals\\.com/photoshoot\\.php\\?photoshoot_id=(\\d+)$");
    private static final Pattern VIDEO_PATTERN = Pattern.compile("^.*nude-gals\\.com/video\\.php\\?video_id=(\\d+)$");

    public NudeGalsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "Nude-Gals";
    }

    @Override
    public String getDomain() {
        return "nude-gals.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = ALBUM_PATTERN;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            logger.info("Found nude-gals photo album page");
            return "album_" + m.group(1);
        }

        p = VIDEO_PATTERN;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            logger.info("Found nude-gals video page");
            return "video_" + m.group(1);
        }

        throw new MalformedURLException(
                "Expected nude-gals.com gallery format: "
                        + "nude-gals.com/photoshoot.php?phtoshoot_id=####"
                        + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> urlsToDownload = new ArrayList<>();

        Pattern p;
        Matcher m;

        p = ALBUM_PATTERN;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            logger.info("Ripping nude-gals photo album");
            Elements thumbs = doc.select("img.thumbnail");
            for (Element thumb : thumbs) {
                String link = thumb.attr("src").strip().replaceAll("thumbs/th_", "");
                String imgSrc = "http://nude-gals.com/" + link;
                imgSrc = imgSrc.replaceAll(" ", "%20");
                urlsToDownload.add(imgSrc);
            }
        }

        p = VIDEO_PATTERN;
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            logger.info("Ripping nude-gals video");
            Elements thumbs = doc.select("video source");
            for (Element thumb : thumbs) {
                String link = thumb.attr("src").strip();
                String videoSrc = "http://nude-gals.com/" + link;
                videoSrc = videoSrc.replaceAll(" ", "%20");
                urlsToDownload.add(videoSrc);
            }
        }

        return urlsToDownload;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer when downloading images
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
