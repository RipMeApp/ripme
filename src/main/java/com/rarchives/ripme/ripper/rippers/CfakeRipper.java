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

public class CfakeRipper extends AbstractHTMLRipper {
    public CfakeRipper(URL url) throws IOException {
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
        Pattern p = Pattern.compile("https?://cfake\\.com/images/celebrity/([a-zA-Z1-9_-]*)/\\d+/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected cfake URL format: " +
                "cfake.com/images/celebrity/MODEL/ID - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("div#wrapper_path div#content_path div#num_page").last();
        if (elem == null) {
            throw new IOException("No more pages (cannot find nav)");
        }

        Element nextAnchor = elem.select("a").first();
        if (nextAnchor == null) {
            throw new IOException("No more pages (cannot find anchor)");
        }

        Elements nextSpans = nextAnchor.select("span");
        if (nextSpans.isEmpty()) {
            // This is the expected case that we're done iterating.
            throw new IOException("No more pages (last page)");
        }

        // Use the nextAnchor (parent of the span) for the URL
        String nextPage = nextAnchor.attr("href");

        // Sometimes this returns an empty string; this stops that
        if (nextPage.equals("")) {
            return null;
        } else {
            return Http.url("https://cfake.com" + nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("div#media_content .responsive .gallery > a img")) {
            // Convert found src value e.g. /medias/thumbs/2025/17358722979850276d_cfake.jpg
            // to photo src value e.g.
            // https://cfake.com/medias/photos/2025/17358722979850276d_cfake.jpg
            String imageSource = el.attr("src");
            imageSource = imageSource.replace("thumbs", "photos");
            result.add("https://cfake.com" + imageSource);
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
