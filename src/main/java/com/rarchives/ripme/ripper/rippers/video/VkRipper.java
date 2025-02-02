package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class VkRipper extends VideoRipper {

    private static final Logger logger = LogManager.getLogger(VkRipper.class);

    private static final String HOST = "vk";

    public VkRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*vk\\.com/video[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*vk\\.com/video([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected vk video URL format:"
                        + "vk.com/videos####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        logger.info("    Retrieving " + this.url);
        String videoURL = getVideoURLAtPage(this.url.toExternalForm());
        addURLToDownload(new URI(videoURL).toURL(), HOST + "_" + getGID(this.url));
        waitForThreads();
    }

    public static String getVideoURLAtPage(String url) throws IOException {
        Document doc = Http.url(url)
                           .userAgent(USER_AGENT)
                           .get();
        String html = doc.outerHtml();
        String videoURL = null;
        for (String quality : new String[] {"1080", "720", "480", "240"}) {
            quality = "url" + quality + "\\\":\\\"";
            if (html.contains(quality)) {
                videoURL = html.substring(html.indexOf(quality) + quality.length());
                videoURL = videoURL.substring(0, videoURL.indexOf("\""));
                videoURL = videoURL.replace("\\", "");
                break;
            }
        }
        if (videoURL == null) {
            throw new IOException("Could not find video URL at " + url);
        }
        return videoURL;
    }
}
