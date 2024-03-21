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

public class OglafRipper extends AbstractHTMLRipper {

    public OglafRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "oglaf";
    }

    @Override
    public String getDomain() {
        return "oglaf.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("http://oglaf\\.com/([a-zA-Z1-9_-]*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected oglaf URL format: " +
                "oglaf.com/NAME - got " + url + " instead");
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        return getDomain();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("div#nav > a > div#nx").first() == null) {
            throw new IOException("No more pages");
        }
        Element elem = doc.select("div#nav > a > div#nx").first().parent();
        String nextPage = elem.attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage.equals("")) {
            throw new IOException("No more pages");
        }
        else {
            sleep(1000);
            return Http.url("http://oglaf.com" + nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("b > img#strip")) {
                String imageSource = el.select("img").attr("src");
                result.add(imageSource);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
