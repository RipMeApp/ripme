package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class ViddmeRipper extends VideoRipper {

    private static final String HOST = "vid";

    public ViddmeRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*vid\\.me/[a-zA-Z0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*vid\\.me/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected vid.me format:"
                        + "vid.me/id"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        LOGGER.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Http.url(this.url).get();
        Elements videos = doc.select("meta[name=twitter:player:stream]");
        if (videos.isEmpty()) {
            throw new IOException("Could not find twitter:player:stream at " + url);
        }
        String vidUrl = videos.first().attr("content");
        vidUrl = vidUrl.replaceAll("&amp;", "&");
        addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}