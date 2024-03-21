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

public class ShesFreakyRipper extends AbstractHTMLRipper {

    public ShesFreakyRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "shesfreaky";
    }

    @Override
    public String getDomain() {
        return "shesfreaky.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*shesfreaky\\.com/gallery/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected shesfreaky.com URL format: "
                + "shesfreaky.com/gallery/... - got " + url + "instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("a[data-lightbox=\"gallery\"]")) {
            String image = thumb.attr("href");
            imageURLs.add("https:" + image);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
