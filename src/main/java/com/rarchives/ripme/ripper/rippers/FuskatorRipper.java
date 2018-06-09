package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class FuskatorRipper extends AbstractHTMLRipper {

    public FuskatorRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "fuskator";
    }
    @Override
    public String getDomain() {
        return "fuskator.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.contains("/thumbs/")) {
            u = u.replace("/thumbs/", "/full/");
        }
        return new URL(u);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*fuskator.com/full/([a-zA-Z0-9\\-]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected fuskator.com gallery formats: "
                        + "fuskator.com/full/id/..."
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        String html = doc.html();
        // Get "baseUrl"
        String baseUrl = Utils.between(html, "unescape('", "'").get(0);
        try {
            baseUrl = URLDecoder.decode(baseUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Error while decoding " + baseUrl, e);
        }
        if (baseUrl.startsWith("//")) {
            baseUrl = "http:" + baseUrl;
        }
        // Iterate over images
        for (String filename : Utils.between(html, "+'", "'")) {
            imageURLs.add(baseUrl + filename);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
