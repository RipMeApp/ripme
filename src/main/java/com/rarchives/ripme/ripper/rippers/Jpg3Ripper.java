package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Jpg3Ripper extends AbstractHTMLRipper {

    public Jpg3Ripper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "jpg3.su";
    }

    @Override
    public String getHost() {
        return "jpg3";
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<>();

        for (Element el : page.select(".image-container > img")) {
            urls.add(el.attr("src").replaceAll("\\.md", ""));
        }

        return urls;
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        u = u.replaceAll("https?://jpg3.su/a/([^/]+)/?.*", "https://jpg3.su/a/$1");
        LOGGER.debug("Changed URL from " + url + " to " + u);
        return new URL(u);
    }

    @Override
    public Document getNextPage(Document page) throws IOException, URISyntaxException {
        String href = page.select("[data-pagination='next']").attr("href");
        if (!href.isEmpty()) {
            return Http.url(href).get();
        } else {
            return null;
        }
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        return url.toString().split("/")[url.toString().split("/").length - 1];
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
