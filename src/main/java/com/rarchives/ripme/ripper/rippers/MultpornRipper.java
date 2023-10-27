package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultpornRipper extends AbstractHTMLRipper {

    public MultpornRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "multporn.net";
    }

    @Override
    public String getHost() {
        return "multporn";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException, URISyntaxException {
        Pattern p = Pattern.compile("^https?://multporn\\.net/node/(\\d+)/.*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        try {
            String nodeHref = Http.url(url).get().select(".simple-mode-switcher").attr("href");
            p = Pattern.compile("/node/(\\d+)/.*");
            m = p.matcher(nodeHref);
            if (m.matches()) {
                this.url = new URI("https://multporn.net" + nodeHref).toURL();
                return m.group(1);
            }
        }catch (Exception ignored){};

        throw new MalformedURLException("Expected multporn.net URL format: " +
                "multporn.net/comics/comicid / multporn.net/node/id/* - got " + url + " instead");
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Elements thumbs = page.select(".mfp-gallery-image .mfp-item");
        for (Element el : thumbs) {
            imageURLs.add(el.attr("href"));
        }
        return imageURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
