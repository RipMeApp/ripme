package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class DribbbleRipper extends AbstractHTMLRipper {

    public DribbbleRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "dribbble";
    }
    @Override
    public String getDomain() {
        return "dribbble.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*dribbble\\.com/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected dribbble.com URL format: " +
                "dribbble.com/albumid - got " + url + "instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Elements hrefs = doc.select("a.next_page");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = "https://www.dribbble.com" + hrefs.first().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }
    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div.shot-thumbnail-base > figure > img")) {
            String srcset = thumb.attr("data-srcset");
            String imageURL = getLargestImageURL(srcset);
            if (imageURL != null) {
                imageURLs.add(imageURL);
            }
        }
        return imageURLs;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private String getLargestImageURL(String srcset) {
        int maxWidth = 0;
        String largestURL = null;

        String[] imageURLs = srcset.split(", ");
        for (String imageURL : imageURLs) {
            try {
                String[] parts = imageURL.trim().split(" ");
                String url = parts[0];
                String size = parts[1];
                int width = Integer.parseInt(size.replace("w", ""));

                if (width > maxWidth) {
                    maxWidth = width;
                    largestURL = url;
                }
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                continue;
            }
        }
        return largestURL;
    }
}