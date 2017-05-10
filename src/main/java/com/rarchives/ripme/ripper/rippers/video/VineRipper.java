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

public class VineRipper extends VideoRipper {

    private static final String HOST = "vine";

    public VineRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        // https://vine.co/v/hiqQrP0eUZx
        Pattern p = Pattern.compile("^https?://[wm.]*vine\\.co/v/[a-zA-Z0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*vine\\.co/v/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected vine format:"
                        + "vine.co/v/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Http.url(this.url).get();
        Elements props = doc.select("meta[property=twitter:player:stream]");
        if (props.size() == 0) {
            throw new IOException("Could not find meta property 'twitter:player:stream' at " + url);
        }
        String vidUrl = props.get(0).attr("content");
        addURLToDownload(new URL(vidUrl), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}