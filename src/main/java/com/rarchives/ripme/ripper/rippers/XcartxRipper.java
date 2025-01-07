package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XcartxRipper extends AbstractHTMLRipper {
    private Map<String,String> cookies = new HashMap<>();


    public XcartxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "xcartx";
    }
    @Override
    public String getDomain() {
        return "xcartx.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://xcartx.com/([a-zA-Z0-9_\\-]+).html");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected URL format: http://xcartx.com/comic, got: " + url);

    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Elements imageElements = page.select("div.f-desc img");
        for (Element image : imageElements) {
            String imageUrl = image.attr("data-src");

            imageURLs.add("https://" + getDomain() + imageUrl);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
    }

    @Override
    public String getPrefix(int index) {
        return String.format("%03d_", index);
    }

}
