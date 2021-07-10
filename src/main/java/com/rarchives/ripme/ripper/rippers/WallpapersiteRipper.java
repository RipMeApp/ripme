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

public class WallpapersiteRipper extends AbstractHTMLRipper {

    public WallpapersiteRipper(URL url) throws IOException {
        super(url);
    }

    private String getFullSizedImageFromURL(String imageURL) {
        try {
            return Http.url("https://wallpapersite.com/" + getGID(url) + "/" + imageURL).get().select("span.res-ttl > a.original").attr("href");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getHost() {
        return "wallpapersite";
    }

    @Override
    public String getDomain() {
        return "wallpapersite.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://wallpapersite.com/([a-zA-Z1-9_-]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected wallpapersite URL format: " +
                "www.wallpapersite.com/ID - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("a.ctrl-right") != null) {
            return Http.url(url.toExternalForm() + doc.select("a.ctrl-right").attr("href")).get();
        }
        throw new IOException("No more pages");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element e : doc.select("div#pics-list > p > a")) {
            String imageURL = e.attr("href");
            result.add("https://wallpapersite.com/" + getFullSizedImageFromURL(imageURL));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}