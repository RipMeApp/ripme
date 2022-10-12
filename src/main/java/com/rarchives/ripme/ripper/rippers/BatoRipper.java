package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class BatoRipper extends AbstractHTMLRipper {

    public BatoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "bato";
    }

    @Override
    public String getDomain() {
        return "bato.to";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://bato.to/chapter/([\\d]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        // As this is just for quick queue support it does matter what this if returns
        p = Pattern.compile("https?://bato.to/series/([\\d]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "";
        }
        throw new MalformedURLException("Expected bato.to URL format: " +
                "bato.to/chapter/ID - got " + url + " instead");
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern p = Pattern.compile("https?://bato.to/series/([\\d]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select("div.main > div > a")) {
            urlsToAddToQueue.add("https://" + getDomain() + elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            return getHost() + "_" + getGID(url) + "_" + getCachedFirstPage().select("title").first().text().replaceAll(" ", "_");
        } catch (IOException e) {
            // Fall back to default album naming convention
            LOGGER.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("https?://bato.to/series/([\\d]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }

        p = Pattern.compile("https?://bato.to/chapter/([\\d]+)/?");
        m = p.matcher(url.toExternalForm());
        return m.matches();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element script : doc.select("script")) {
            if (script.data().contains("var images = ")) {
                String s = script.data();
                s = s.replaceAll("var seriesId = \\d+;", "");
                s = s.replaceAll("var chapterId = \\d+;", "");
                s = s.replaceAll("var pages = \\d+;", "");
                s = s.replaceAll("var page = \\d+;", "");
                s = s.replaceAll("var prevCha = null;", "");
                s = s.replaceAll("var nextCha = \\.*;", "");
                String json = s.replaceAll("var images = ", "").replaceAll(";", "");
                JSONObject images = new JSONObject(json);
                for (int i = 1; i < images.length() +1; i++) {
                    result.add(images.getString(Integer.toString(i)));
                }

            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
