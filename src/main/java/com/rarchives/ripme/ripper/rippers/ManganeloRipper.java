package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ManganeloRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(ManganeloRipper.class);

    public ManganeloRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "manganelo";
    }

    @Override
    public String getDomain() {
        return "manganelo.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://manganelo.com/manga/([\\S]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("https?://manganelo.com/chapter/([\\S]+)/([\\S_\\-]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected manganelo URL format: " +
                "/manganelo.com/manga/ID - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("div.btn-navigation-chap > a.back").first();
        if (elem == null) {
            throw new IOException("No more pages");
        } else {
            return Http.url(elem.attr("href")).get();
        }
    }

    private List<String> getURLsFromChap(String url) {
        List<String> result = new ArrayList<>();
        try {
            Document doc = Http.url(url).get();

            return getURLsFromChap(doc);
        } catch (IOException e) {
            return null;
        }

    }

    private List<String> getURLsFromChap(Document doc) {
        logger.debug("Getting urls from " + doc.location());
        List<String> result = new ArrayList<>();
        for (Element el : doc.select(".vung-doc > img")) {
            result.add(el.attr("src"));
        }
        return result;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        List<String> urlsToGrab = new ArrayList<>();
        if (url.toExternalForm().contains("/manga/")) {
            for (Element el : doc.select("div.chapter-list > div.row > span > a")) {
                urlsToGrab.add(el.attr("href"));
            }
            Collections.reverse(urlsToGrab);

            for (String url : urlsToGrab) {
                result.addAll(getURLsFromChap(url));
            }
        } else if (url.toExternalForm().contains("/chapter/")) {
            result.addAll(getURLsFromChap(doc));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
