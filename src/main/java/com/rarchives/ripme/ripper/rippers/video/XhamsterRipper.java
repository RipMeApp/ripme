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

public class XhamsterRipper extends VideoRipper {

    private static final String HOST = "xhamster";

    public XhamsterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://.*xhamster\\.com/movies/[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://.*xhamster\\.com/movies/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected xhamster format:"
                        + "xhamster.com/movies/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("Retrieving " + this.url);
        Document doc = Http.url(url).get();
        Elements videos = doc.select("a.mp4Thumb");
        if (videos.size() == 0) {
            throw new IOException("Could not find Embed code at " + url);
        }
        String vidUrl = videos.attr("href");
        addURLToDownload(new URL(vidUrl), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}