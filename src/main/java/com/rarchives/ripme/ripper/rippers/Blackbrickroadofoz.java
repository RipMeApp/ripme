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

public class Blackbrickroadofoz extends AbstractHTMLRipper {

    public Blackbrickroadofoz(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "cfake";
    }

    @Override
    public String getDomain() {
        return "cfake.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.blackbrickroadofoz.com/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected blackbrickroadofoz URL format: " +
                "www.blackbrickroadofoz.com/comic/PAGE - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("nav.cc-nav > a.cc-next").first();
        if (elem == null) {
            throw new IOException("No more pages");
        }
        String nextPage = elem.attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage == "") {
            throw new IOException("No more pages");
        }
        else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Element elem = doc.select("img[id=cc-comic]").first();
        result.add(elem.attr("src"));

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
