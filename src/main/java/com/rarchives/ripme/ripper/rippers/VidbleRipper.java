package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class VidbleRipper extends AbstractHTMLRipper {

    public VidbleRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "vidble";
    }
    @Override
    public String getDomain() {
        return "vidble.com";
    }
    
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*vidble.com/album/([a-zA-Z0-9_\\-]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected vidble.com album format: "
                        + "vidble.com/album/####"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        return getURLsFromPageStatic(doc);
    }

    private static List<String> getURLsFromPageStatic(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        Elements els = doc.select("#ContentPlaceHolder1_thumbs");
        String thumbs = els.first().attr("value");
        for (String thumb : thumbs.split(",")) {
            if (thumb.trim().equals("") || thumb.contains("reddit.com")) {
                continue;
            }
            thumb = thumb.replaceAll("_[a-zA-Z]{3,5}", "");
            imageURLs.add("http://vidble.com/" + thumb);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    public static List<URL> getURLsFromPage(URL url) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        Document doc = Http.url(url).get();
        for (String stringURL : getURLsFromPageStatic(doc)) {
            urls.add(new URL(stringURL));
        }
        return urls;
    }
}