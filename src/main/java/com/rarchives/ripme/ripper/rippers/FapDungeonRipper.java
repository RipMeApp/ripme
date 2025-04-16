package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class FapDungeonRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(FapDungeonRipper.class);

    private static final String HOST = "fapdungeon";

    private static final Pattern pagePattern = Pattern
            .compile("^https?://[wm\\.]*fapdungeon\\.com/([a-zA-Z0-9_-]+)/(.+)/?$");

    public FapDungeonRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return HOST + ".com";
    }

    @Override
    public boolean canRip(URL url) {
        Matcher m = pagePattern.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = pagePattern.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected fapdungeon format:"
                        + "fapdungeon.com/category/albumname/"
                        + " Got: " + url);
    }

    public String returnLargestImgUrlFromSrcAndSrcset(String src, String sourceSet) {
        String[] parts = sourceSet.split(",");
        logger.info("While ripping url: " + this.url + " img src: " + src + " has sourceSet: " + sourceSet);

        Optional<String> largestImgUrl = Optional.empty();
        int maxWidthSoFar = 0;

        for (String part : parts) {
            String[] subParts = part.strip().split(" ");
            if (subParts.length == 2) {
                String imgUrlPart = subParts[0].strip();

                // parse the integer out of values like "1080w"
                String widthStringPart = subParts[1].strip();
                String widthNumberString = widthStringPart.substring(0, widthStringPart.length() - 1);
                int width = Integer.parseInt(widthNumberString);

                if (width > maxWidthSoFar) {
                    logger.info("Found larger image: " + part);
                    largestImgUrl = Optional.of(imgUrlPart);
                    maxWidthSoFar = width;
                }
            }
        }

        if (largestImgUrl.isPresent()) {
            String imgUrl = largestImgUrl.get();
            logger.info("For img src " + src + " with srcset, using largestImgUrl: " + imgUrl);
            return imgUrl;
        } else {
            logger.info("Using img src: " + src);
            return src;
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();

        Matcher m = pagePattern.matcher(url.toExternalForm());
        if (m.matches()) {
            Element content = doc.select("div.entry-content").get(0);

            // Debug this selector on the page itself in Dev Tools with
            // Array.from(document.querySelectorAll("div.entry-content img")).map((x) =>
            // x['srcset'])
            Elements pictures = content.select("img");
            for (Element e : pictures) {
                String imageSrc = e.attr("src"); // fallback on <img src="..."> value
                String imageSourceSet = e.attr("srcset"); // get the largest resolution from this srcset
                String imageUrl = returnLargestImgUrlFromSrcAndSrcset(imageSrc, imageSourceSet);
                results.add(imageUrl);
            }

            // Debug this selector on the page itself in Dev Tools with
            // Array.from(document.querySelectorAll("div.entry-content video
            // source")).map((x) => x['src'])
            Elements videos = content.select("video source");
            for (Element e : videos) {
                results.add(e.attr("src"));
            }
        }

        return results;
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(1000);
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getAlbumTitle() throws MalformedURLException, URISyntaxException {
        Matcher m = pagePattern.matcher(url.toExternalForm());
        if (m.matches()) {
            return getHost() + "_" + m.group(1) + "_" + m.group(2);
        } else {
            return super.getAlbumTitle();
        }
    }
}
