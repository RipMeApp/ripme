package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelxRipper extends AbstractHTMLRipper {

    public ModelxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "modelx";
    }

    @Override
    public String getDomain() {
        return "modelx.org";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*modelx.org/.*/(.+)$");
        Matcher m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("Expected URL format: http://www.modelx.org/[category (one or more)]/xxxxx got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> result = new ArrayList<>();

        for (Element el : page.select(".gallery-icon > a")) {
            result.add(el.attr("href"));
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
