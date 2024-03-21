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

public class StickyXXXRipper extends VideoRipper {

    private static final String HOST = "stickyxxx";

    public StickyXXXRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://.*stickyxxx\\.com(/)(.*)/$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://.*stickyxxx\\.com(/)(.*)/$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected stickyxxx format:"
                        + "stickyxxx.com/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        LOGGER.info("Retrieving " + this.url);
        Document doc = Http.url(url).get();
        Elements videos = doc.select(".wp-video > video > source");
        if (videos.isEmpty()) {
            throw new IOException("Could not find Embed code at " + url);
        }
        String vidUrl = videos.attr("src");
        addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}