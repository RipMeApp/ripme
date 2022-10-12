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


public class HentaiimageRipper extends AbstractHTMLRipper {


    public HentaiimageRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public String getDomain() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public boolean canRip(URL url) {
        try {
            getGID(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://(?:\\w\\w\\.)?hentai-(image|comic).com/image/([a-zA-Z0-9_-]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected hitomi URL format: " +
                "https://hentai-image.com/image/ID - got " + url + " instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("div.icon-overlay > a > img")) {
            result.add(el.attr("src"));
        }
        return result;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {

        for (Element el : doc.select("div[id=paginator] > span")) {
            if (el.select("a").text().equals("next>")) {
                return Http.url("https://" + getDomain() + el.select("a").attr("href")).get();
            }
        }

        throw new IOException("No more pages");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
