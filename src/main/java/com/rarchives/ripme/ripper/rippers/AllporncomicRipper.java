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

public class AllporncomicRipper extends AbstractHTMLRipper {

    public AllporncomicRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "allporncomic";
    }

    @Override
    public String getDomain() {
        return "allporncomic.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://allporncomic.com/porncomic/([a-zA-Z0-9_\\-]+)/([a-zA-Z0-9_\\-]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("^https?://allporncomic.com/porncomic/([a-zA-Z0-9_\\-]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected allporncomic URL format: " +
                "allporncomic.com/TITLE/CHAPTER - got " + url + " instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select(".wp-manga-chapter-img")) {
            result.add(el.attr("data-src"));
        }
        return result;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("^https?://allporncomic.com/porncomic/([a-zA-Z0-9_\\-]+)/?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        return ma.matches();
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select(".wp-manga-chapter > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
