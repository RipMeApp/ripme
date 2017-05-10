package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class YoupornRipper extends VideoRipper {

    private static final String HOST = "youporn";

    public YoupornRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*youporn\\.com/watch/[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*youporn\\.com/watch/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected youporn format:"
                        + "youporn.com/watch/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url);
        Document doc = Http.url(this.url).get();
        Elements videos = doc.select("video");
        if (videos.size() == 0) {
            throw new IOException("Could not find Embed code at " + url);
        }
        Element video = videos.get(0);
        String vidUrl = video.attr("src");
        addURLToDownload(new URL(vidUrl), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}