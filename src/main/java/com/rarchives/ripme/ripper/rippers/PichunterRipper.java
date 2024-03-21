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

public class PichunterRipper extends AbstractHTMLRipper {

    public PichunterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "pichunter";
    }

    @Override
    public String getDomain() {
        return "pichunter.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.pichunter.com/(|tags|models|sites)/(\\S*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        p = Pattern.compile("https?://www.pichunter.com/(tags|models|sites)/(\\S*)/photos/\\d+/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        p = Pattern.compile("https?://www.pichunter.com/tags/all/(\\S*)/\\d+/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("https?://www.pichunter.com/gallery/\\d+/(\\S*)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected pichunter URL format: " +
                "pichunter.com/(tags|models|sites)/Name/ - got " + url + " instead");
    }

    private boolean isPhotoSet(URL url) {
        Pattern p = Pattern.compile("https?://www.pichunter.com/gallery/\\d+/(\\S*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // We use comic-nav-next to the find the next page
        Element elem = doc.select("div.paperSpacings > ul > li.arrow").last();
        if (elem != null) {
            String nextPage = elem.select("a").attr("href");
            // Some times this returns a empty string
            // This for stops that
            return Http.url("http://www.pichunter.com" + nextPage).get();
        }
        throw new IOException("No more pages");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        if (!isPhotoSet(url)) {
            for (Element el : doc.select("div.thumbtable > a.thumb > img")) {
                result.add(el.attr("src").replaceAll("_i", "_o"));
            }
        } else {
            for (Element el : doc.select("div.flex-images > figure > a.item > img")) {
                result.add(el.attr("src").replaceAll("_i", "_o"));
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}