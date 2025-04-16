package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class BatoRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(BatoRipper.class);

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
    public boolean pageContainsAlbums() {
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
    public String getAlbumTitle() throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title as GID
            return getHost() + "_" + getGID(url) + "_" + getCachedFirstPage().select("title").first().text().replaceAll(" ", "_");
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle();
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

    public String scanForImageList(Pattern p, String scriptData) {
        for (String line : scriptData.split("\n")) {
            Matcher m = p.matcher(line.strip());
            if (m.matches()) {
                return m.group(1);
            }
        }
        return "[]";
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element script : doc.select("script")) {
            if (script.data().contains("imgHttps")) {
                String s = script.data();
                logger.info("Script data: " + s);

                Pattern p = Pattern.compile(".*imgHttps = (\\[\"[^\\];]*\"\\]);.*");
                Matcher m = p.matcher(s);
                String json = scanForImageList(p, s);

                logger.info("JSON: " + json);

                JSONArray images = new JSONArray(json);
                for (int i = 0; i < images.length(); i++) {
                    result.add(images.getString(i));
                }
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(500);
        addURLToDownload(url, getPrefix(index));
    }
}
