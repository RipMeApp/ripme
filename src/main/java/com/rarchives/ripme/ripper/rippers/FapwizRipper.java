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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class FapwizRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(FapwizRipper.class);

    private static final Pattern CATEGORY_PATTERN = Pattern.compile("https?://fapwiz.com/category/([a-zA-Z0-9_-]+)/?$");

    private static final Pattern USER_PATTERN = Pattern.compile("https?://fapwiz.com/([a-zA-Z0-9_-]+)/?$");

    // Note that the last part of the pattern can contain unicode emoji which
    // get encoded as %-encoded UTF-8 bytes in the URL, so we allow % characters.
    private static final Pattern POST_PATTERN = Pattern
            .compile("https?://fapwiz.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_%-]+)/?$");

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
            return "post_" + m.group(1) + "_" + m.group(2);
        }

        throw new MalformedURLException("Expected fapwiz URL format: " +
                "fapwiz.com/USER or fapwiz.com/USER/POST or " +
                "fapwiz.com/CATEGORY - got " + url + " instead");
    }

    void processUserOrCategoryPage(Document doc, List<String> results) {
        // The category page looks a lot like the structure of a user page,
        // so processUserPage is written to be compatible with both.
        doc.select(".post-items-holder img").forEach(e -> {
            String imgSrc = e.attr("src");

            // Skip the user profile picture thumbnail insets
            if (imgSrc.endsWith("-thumbnail-icon.jpg")) {
                return;
            }

            // Replace -thumbnail.jpg with .mp4
            String videoSrc = imgSrc.replace("-thumbnail.jpg", ".mp4");
            results.add(videoSrc);
        });
    }

    void processCategoryPage(Document doc, List<String> results) {
        logger.info("Processing category page: " + url);
        processUserOrCategoryPage(doc, results);
    }

    void processUserPage(Document doc, List<String> results) {
        logger.info("Processing user page: " + url);
        processUserOrCategoryPage(doc, results);
    }

    void processPostPage(Document doc, List<String> results) {
        logger.info("Processing post page: " + url);
        doc.select("video source").forEach(video -> {
            results.add(video.attr("src"));
        });
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        Matcher m;

        m = CATEGORY_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            processCategoryPage(doc, results);
        }

        m = USER_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            processUserPage(doc, results);
        }

        m = POST_PATTERN.matcher(url.toExternalForm());
        if (m.matches()) {
            processPostPage(doc, results);
        }

        return results;
    }

    private Document getDocument(String url, int retries) throws IOException {
        return Http.url(url).userAgent(USER_AGENT).retries(retries).get();
    }

    private Document getDocument(String url) throws IOException {
        return getDocument(url, 1);
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        logger.info("Getting next page for url: " + url);
        Elements next = page.select("a.next");
        if (!next.isEmpty()) {
            String href = next.attr("href");
            logger.info("Found next page: " + href);
            return getDocument(href);
        } else {
            logger.info("No more pages");
            throw new IOException("No more pages.");
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(2000);
        addURLToDownload(url, getPrefix(index));
    }
}
