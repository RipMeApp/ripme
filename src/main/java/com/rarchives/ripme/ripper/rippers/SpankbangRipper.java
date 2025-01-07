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
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;

public class SpankbangRipper extends AbstractSingleFileRipper {

    private static final Logger logger = LogManager.getLogger(SpankbangRipper.class);

    private static final String HOST = "spankbang";

    public SpankbangRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "spankbang.com";
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Elements videos = doc.select(".video-js > source");
        if (videos.isEmpty()) {
            logger.error("Could not find Embed code at " + url);
            return null;
        }
        result.add(videos.attr("src"));
        return result;
    }


    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://.*spankbang\\.com/(.*)/video/.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://.*spankbang\\.com/(.*)/video/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected spankbang format:"
                        + "spankbang.com/####/video/"
                        + " Got: " + url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
