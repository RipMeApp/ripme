package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class GfycatRipper extends VideoRipper {

    private static final String HOST = "gfycat.com";

    public GfycatRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(HOST);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*gfycat\\.com/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected gfycat.com format:"
                        + "gfycat.com/id"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        String vidUrl = getVideoURL(this.url);
        addURLToDownload(new URL(vidUrl), "gfycat_" + getGID(this.url));
        waitForThreads();
    }

    /**
     * Helper method for retrieving video URLs.
     * @param url URL to gfycat page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException {
        logger.info("Retrieving " + url.toExternalForm());
        Document doc = Http.url(url).get();
        Elements videos = doc.select("source#mp4Source");
        if (videos.size() == 0) {
            throw new IOException("Could not find source#mp4source at " + url);
        }
        String vidUrl = videos.first().attr("src");
        if (vidUrl.startsWith("//")) {
            vidUrl = "http:" + vidUrl;
        }
        return vidUrl;
    }
}