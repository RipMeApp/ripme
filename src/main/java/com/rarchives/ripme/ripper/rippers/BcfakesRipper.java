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

public class BcfakesRipper extends AbstractHTMLRipper {

    public BcfakesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "bcfakes";
    }
    @Override
    public String getDomain() {
        return "bcfakes.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^https?://[wm.]*bcfakes.com/celebritylist/([a-zA-Z0-9\\-_]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected bcfakes gallery format: "
                        + "http://www.bcfakes.com/celebritylist/name"
                        + " Got: " + url);
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Elements hrefs = doc.select("a.next");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = "http://www.bcfakes.com" + hrefs.first().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div.ngg-gallery-thumbnail > a > img")) {
            String imageURL = thumb.attr("src");
            imageURL = imageURL.replace("thumbs/thumbs_", "");
            imageURLs.add(imageURL);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}