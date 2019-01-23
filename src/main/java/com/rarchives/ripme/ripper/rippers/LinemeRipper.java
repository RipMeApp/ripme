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

public class LinemeRipper extends AbstractHTMLRipper {

    public LinemeRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "store.line";
    }

    @Override
    public String getDomain() {
        return "store.line.me";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://store.line.me/stickershop/product/([0-9]+)/\\S+");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected store.line.me URL format: " +
                "store.line.me/stickershop/product/#### - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("ul > li > div > span")) {
            String f =el.attr("style").replaceAll("background-image:url\\(", "").replaceAll(";compress=true\\);", "");
            result.add(f);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
