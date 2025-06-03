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

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import com.rarchives.ripme.utils.Http;

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
        try {
            String gid = getGID(url);
            String apiUrl = "https://spankbang.com/api/videos/" + gid;

            logger.info("Fetching video info from API: " + apiUrl);

            Document apiDoc = Http.url(apiUrl)
                    .ignoreContentType()
                    .get();

            String json = apiDoc.text();
            Pattern pattern = Pattern.compile("\"video_url\"\\s*:\\s*\"(https:[^\"]+)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String videoUrl = matcher.group(1).replace("\\/", "/");
                logger.info("Found video URL: " + videoUrl);
                result.add(videoUrl);
            } else {
                logger.error("Could not extract video URL from API response");
            }

        } catch (Exception e) {
            logger.error("Failed to get video URL for Spankbang video", e);
        }
        return result;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        return url.toExternalForm().matches("^https?://(?:www\\.)?spankbang\\.com/[a-zA-Z0-9]+(?:/video/.*)?$");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(?:www\\.)?spankbang\\.com/([a-zA-Z0-9]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.find()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected Spankbang URL format: spankbang.com/abcd/video/... but got: " + url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
