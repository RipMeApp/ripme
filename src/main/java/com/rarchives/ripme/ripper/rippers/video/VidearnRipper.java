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
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class VidearnRipper extends VideoRipper {

    private static final Logger logger = LogManager.getLogger(VidearnRipper.class);

    private static final String HOST = "videarn";

    public VidearnRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*videarn\\.com/[a-zA-Z0-9\\-]+/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*videarn\\.com/[a-zA-Z0-9\\-]+/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected videarn format:"
                        + "videarn.com/.../####-..."
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        logger.info("Retrieving " + this.url);
        Document doc = Http.url(url).get();
        List<String> mp4s = Utils.between(doc.html(), "file:\"", "\"");
        if (mp4s.isEmpty()) {
            throw new IOException("Could not find files at " + url);
        }
        String vidUrl = mp4s.get(0);
        addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}
