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

public class RajceRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "rajce.idnes.cz";
    private static final String HOST = "rajce.idnes";

    public RajceRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([^.]+)\\.rajce\\.idnes\\.cz/(([^/]+)/.*)?$");
        Matcher m = p.matcher(url.toExternalForm());

        if (!m.matches()) {
            throw new MalformedURLException("Unsupported URL format: " + url.toExternalForm());
        }

        String user = m.group(1);
        String album = m.group(3);

        if (album == null) {
            throw new MalformedURLException("Unsupported URL format (not an album): " + url.toExternalForm());
        }

        return user + "/" + album;
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        return super.getNextPage(doc);
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> result = new ArrayList<>();
        for (Element el : page.select("a.photoThumb")) {
            result.add(el.attr("href"));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}
