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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class TheyiffgalleryRipper extends AbstractHTMLRipper {

    public TheyiffgalleryRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "theyiffgallery";
    }

    @Override
    public String getDomain() {
        return "theyiffgallery.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://theyiffgallery.com/index\\?/category/(\\d+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected theyiffgallery URL format: " +
                "theyiffgallery.com/index?/category/#### - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextPage = doc.select("span.navPrevNext > a").attr("href");
        if (nextPage != null && !nextPage.isEmpty() && nextPage.contains("start-")) {
            return Http.url("https://theyiffgallery.com/" + nextPage).get();
        }
        throw new IOException("No more pages");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("img.thumbnail")) {
            String imageSource = el.attr("src");
            imageSource = imageSource.replaceAll("_data/i", "");
            imageSource = imageSource.replaceAll("-\\w\\w_\\w\\d+x\\d+", "");
            result.add("https://theyiffgallery.com" + imageSource);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}