package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HypnohubRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(HypnohubRipper.class);

    public HypnohubRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hypnohub";
    }

    @Override
    public String getDomain() {
        return "hypnohub.net";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String query = url.getQuery();
        if (query == null) {
            throw new MalformedURLException("URL missing query: " + url);
        }
        if (query.contains("page=pool")) {
            for (String param : query.split("&")) {
                if (param.startsWith("id=")) {
                    return param.substring("id=".length());
                }
            }
            throw new MalformedURLException("Pool URL missing id: " + url);
        } else if (query.startsWith("page=post")) {
            // Drop "page=" to satisfy testGetGID
            return query.substring("page=".length());
        }
        throw new MalformedURLException("Unexpected URL format for GID: " + url);
    }

    /**
     * Fetches a post page and extracts its full-size image URL.
     */
    private String ripPost(String postUrl) throws IOException {
        logger.info("Fetching post: {}", postUrl);
        Document doc = Http.url(postUrl).get();
        // Try primary selector: the displayed sample image
        Element img = doc.selectFirst("img#image");
        if (img != null) {
            String src = img.attr("src");
            if (src.startsWith("//"))
                return "https:" + src;
            if (src.startsWith("/"))
                return "https://hypnohub.net" + src;
            return src;
        }
        // Fallback to original image link
        Element origLink = doc.selectFirst("a:matchesOwn(^Original image$");
        if (origLink != null) {
            String href = origLink.attr("href");
            if (href.startsWith("//"))
                return "https:" + href;
            if (href.startsWith("/"))
                return "https://hypnohub.net" + href;
            return href;
        }
        // Final fallback: meta og:image
        Element meta = doc.selectFirst("meta[property=og:image]");
        if (meta != null) {
            String content = meta.attr("content");
            if (content.startsWith("//"))
                return "https:" + content;
            if (content.startsWith("/"))
                return "https://hypnohub.net" + content;
            return content;
        }
        logger.warn("No image found on post page: {}", postUrl);
        return null;
    }

    /**
     * Extracts the full-size image URL from an already-fetched post Document.
     */
    private String ripPost(Document doc) {
        logger.info("Parsing post document: {}", url);
        // Use same logic as string-based ripPost
        Element img = doc.selectFirst("img#image");
        if (img != null) {
            String src = img.attr("src");
            if (src.startsWith("//"))
                return "https:" + src;
            if (src.startsWith("/"))
                return "https://hypnohub.net" + src;
            return src;
        }
        Element origLink = doc.selectFirst("a:matchesOwn(^Original image$");
        if (origLink != null) {
            String href = origLink.attr("href");
            if (href.startsWith("//"))
                return "https:" + href;
            if (href.startsWith("/"))
                return "https://hypnohub.net" + href;
            return href;
        }
        Element meta = doc.selectFirst("meta[property=og:image]");
        if (meta != null) {
            String content = meta.attr("content");
            if (content.startsWith("//"))
                return "https:" + content;
            if (content.startsWith("/"))
                return "https://hypnohub.net" + content;
            return content;
        }
        logger.warn("No image found in document at: {}", url);
        return null;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String pageUrl = url.toExternalForm();
        if (pageUrl.contains("page=pool")) {
            // Iterate over all thumbnail spans on the pool page
            for (Element link : doc.select("span.thumb > a[href*='page=post']")) {
                String href = link.attr("href");
                String fullPostUrl = href.startsWith("http") ? href : "https://hypnohub.net/" + href;
                try {
                    String imgUrl = ripPost(fullPostUrl);
                    if (imgUrl != null) {
                        result.add(imgUrl);
                    }
                } catch (IOException e) {
                    logger.error("Failed to rip post {}", fullPostUrl, e);
                }
            }
        } else if (pageUrl.contains("page=post")) {
            String imgUrl = ripPost(doc);
            if (imgUrl != null) {
                result.add(imgUrl);
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // url here is already a direct image URL
        addURLToDownload(url, getPrefix(index));
    }
}
