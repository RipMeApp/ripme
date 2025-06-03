package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpankbangPlaylistRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(SpankbangPlaylistRipper.class);

    public SpankbangPlaylistRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "spankbang-playlist";
    }

    @Override
    public String getDomain() {
        return "spankbang.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(?:www\.)?spankbang\.com/([a-zA-Z0-9]+)/playlist/.*");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.find()) {
            return m.group(1);
        }
        throw new MalformedURLException("Invalid Spankbang playlist URL: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        int page = 1;

        while (true) {
            String pageUrl = url.toExternalForm();
            if (page > 1) {
                pageUrl = pageUrl.replaceAll("\?p=\d+", "") + "?p=" + page;
            }

            logger.info("Fetching playlist page: " + pageUrl);

            try {
                Document pageDoc = Http.url(pageUrl).get();

                Elements links = pageDoc.select("a[href^="/"][href*="/video/"]");
                int foundOnPage = 0;

                for (var el : links) {
                    String href = el.attr("href");
                    if (href.matches("^/[a-zA-Z0-9]+/video/.*")) {
                        String videoUrl = "https://spankbang.com" + href;
                        if (!results.contains(videoUrl)) {
                            results.add(videoUrl);
                            foundOnPage++;
                            logger.debug("Found video link: " + videoUrl);
                        }
                    }
                }

                if (foundOnPage == 0) {
                    logger.info("No more videos found on page " + page + "; ending pagination.");
                    break;
                }

            } catch (IOException e) {
                logger.warn("Failed to fetch page " + page + ": " + e.getMessage());
                break;
            }

            page++;
        }

        return results;
    }

    @Override
    public void downloadURL(URL videoPageUrl, int index) {
        try {
            SpankbangRipper videoRipper = new SpankbangRipper(videoPageUrl);
            Document doc = Http.url(videoPageUrl).get();
            List<String> directVideoUrls = videoRipper.getURLsFromPage(doc);
            for (String directUrl : directVideoUrls) {
                addURLToDownload(new URL(directUrl), getPrefix(index));
            }
        } catch (Exception e) {
            logger.error("Failed to rip video from page: " + videoPageUrl, e);
        }
    }
}
