package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CyberdropRipper extends AbstractHTMLRipper {

    public CyberdropRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "cyberdrop";
    }

    @Override
    public String getDomain() {
        return "cyberdrop.me";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://cyberdrop\\.me/a/([a-zA-Z0-9]+).*?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected cyberdrop.me URL format: " +
                "https://cyberdrop.me/a/xxxxxxxx - got " + url + "instead");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        ArrayList<String> urls = new ArrayList<>();
        for (Element element: page.getElementsByClass("image")) {
                urls.add(element.attr("href"));
        }
        return urls;
    }
}