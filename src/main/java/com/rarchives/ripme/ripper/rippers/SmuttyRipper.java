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


public class SmuttyRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "smutty.com",
                                HOST   = "smutty";

    public SmuttyRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "smutty";
    }

    @Override
    public String getDomain() {
        return "smutty.com";
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith(DOMAIN));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        for (Element image : doc.select("a.l > img")) {
            if (isStopped()) {
                break;
            }
            String imageUrl = image.attr("src");

            // Construct direct link to image based on thumbnail
            StringBuilder sb = new StringBuilder();
            String[] fields = imageUrl.split("/");
            for (int i = 0; i < fields.length; i++) {
                if (i == fields.length - 2 && fields[i].equals("m")) {
                    fields[i] = "b";
                }
                sb.append(fields[i]);
                if (i < fields.length - 1) {
                    sb.append("/");
                }
            }
            imageUrl = sb.toString();
            results.add("http:" + imageUrl);
        }
        return results;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("a.next").first();
        if (elem == null) {
            throw new IOException("No more pages");
        }
        String nextPage = elem.attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage.equals("")) {
            throw new IOException("No more pages");
        }
        else {
            return Http.url("https://smutty.com" + nextPage).get();
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://smutty\\.com/h/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^https?://[wm.]*smutty\\.com/search/\\?q=([a-zA-Z0-9\\-_%]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1).replace("%23", "");
        }

        p = Pattern.compile("^https?://smutty.com/user/([a-zA-Z0-9\\-_]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected tag in URL (smutty.com/h/tag and not " + url);
    }

}
