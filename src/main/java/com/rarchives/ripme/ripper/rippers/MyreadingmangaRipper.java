
package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MyreadingmangaRipper extends AbstractHTMLRipper {

    public MyreadingmangaRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "myreadingmanga";
    }

    @Override
    public String getDomain() {
        return "myreadingmanga.info";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://myreadingmanga.info/([a-zA-Z_\\-0-9]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("Expected myreadingmanga.info URL format: "
                + "myreadingmanga.info/title - got " + url + " instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("div * img[data-lazy-src]")) {
            String imageSource = el.attr("data-lazy-src");
            result.add(imageSource);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}