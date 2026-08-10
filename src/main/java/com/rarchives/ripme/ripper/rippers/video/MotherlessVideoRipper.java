package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class MotherlessVideoRipper extends VideoRipper {

    private static final Logger logger = LogManager.getLogger(MotherlessVideoRipper.class);

    private static final String HOST = "motherless";

    public MotherlessVideoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*motherless\\.com/[A-Z0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*motherless\\.com/([A-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected motherless format:"
                        + "motherless.com/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        logger.info("    Retrieving " + this.url);
        String html = Http.url(this.url).get().toString();
        if (html.contains("__fileurl = '")) {
            logger.error("WTF");
        }
        List<String> vidUrls = Utils.between(html, "__fileurl = '", "';");
        if (vidUrls.isEmpty()) {
            throw new IOException("Could not find video URL at " + url);
        }
        String vidUrl = vidUrls.get(0);
        addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + getGID(this.url));
        waitForRipperThreads();
    }
}
