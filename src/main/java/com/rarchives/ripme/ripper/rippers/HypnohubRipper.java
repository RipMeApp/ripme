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

public class HypnohubRipper extends AbstractHTMLRipper {

    public HypnohubRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hypnohub";
    }

    @Override
    public String getDomain() {
        return "hypnohub.net";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://hypnohub.net/\\S+/show/([\\d]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("https?://hypnohub.net/\\S+/show/([\\d]+)/([\\S]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        throw new MalformedURLException("Expected cfake URL format: " +
                "hypnohub.net/pool/show/ID - got " + url + " instead");
    }

    private String ripPost(String url) throws IOException {
        LOGGER.info(url);
        Document doc = Http.url(url).get();
        return "https:" +  doc.select("img.image").attr("src");

    }

    private String ripPost(Document doc) {
        LOGGER.info(url);
        return "https:" +  doc.select("img.image").attr("src");

    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        if (url.toExternalForm().contains("/pool")) {
            for (Element el : doc.select("ul[id=post-list-posts] > li > div > a.thumb")) {
                try {
                    result.add(ripPost("https://hypnohub.net" + el.attr("href")));
                } catch (IOException e) {
                    return result;
                }
            }
        } else if (url.toExternalForm().contains("/post")) {
            result.add(ripPost(doc));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
