package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class FapwizRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(FapwizRipper.class);

    private static final Pattern CATEGORY_PATTERN =
        Pattern.compile("https?://fapwiz.com/category/([a-zA-Z1-9_-]+)/?$");

    private static final Pattern USER_PATTERN =
        Pattern.compile("https?://fapwiz.com/([a-zA-Z1-9_-]+)/?$");

    private static final Pattern POST_PATTERN =
        Pattern.compile("https?://fapwiz.com/([a-zA-Z1-9_-]+)/([a-zA-Z1-9_-]+)/?$");

    public FapwizRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "fapwiz";
    }

    @Override
    public String getDomain() {
        return "fapwiz.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m;

        m = CATEGORY_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            return "category_" + m.group(1);
        }

        m = USER_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            return "user_" + m.group(1);
        }

        m = POST_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }

        throw new MalformedURLException("Expected fapwiz URL format: " +
                "fapwiz.com/NAME - got " + url + " instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();

        Matcher m;

        m = CATEGORY_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            // structure of user page - does it work for category pages too?
            doc.select(".post-items-holder img").forEach(e -> {
                String imgSrc = e.attr("src");
                // Replace -thumbnail.jpg with .mp4
                String videoSrc = imgSrc.replace("-thumbnail.jpg", ".mp4");
                result.add(videoSrc);
            });
        }

        m = USER_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            // structure of user page
            doc.select(".post-items-holder img").forEach(e -> {
                String imgSrc = e.attr("src");
                // Replace -thumbnail.jpg with .mp4
                String videoSrc = imgSrc.replace("-thumbnail.jpg", ".mp4");
                result.add(videoSrc);
            });
        }

        m = POST_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            doc.select("video source").forEach(video -> {
                result.add(video.attr("src"));
            });
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(2000);
        addURLToDownload(url, getPrefix(index));
    }
}
