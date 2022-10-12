package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErofusRipper extends AbstractHTMLRipper {

    public ErofusRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean hasASAPRipping() {
        return true;
    }

    @Override
    public String getHost() {
        return "erofus";
    }

    @Override
    public String getDomain() {
        return "erofus.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https://www.erofus.com/comics/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.8muses.com/index/category/albumname, got: " + url);
        }
        return m.group(m.groupCount());
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        LOGGER.info(page);
        List<String> imageURLs = new ArrayList<>();
        int x = 1;
        if (pageContainsImages(page)) {
            LOGGER.info("Page contains images");
            ripAlbum(page);
        } else {
            // This contains the thumbnails of all images on the page
            Elements pageImages = page.select("a.a-click");
            for (Element pageLink : pageImages) {
                if (super.isStopped()) break;
                if (pageLink.attr("href").contains("comics")) {
                    String subUrl = "https://erofus.com" + pageLink.attr("href");
                    try {
                        LOGGER.info("Retrieving " + subUrl);
                        sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, subUrl);
                        Document subPage = Http.url(subUrl).get();
                        List<String> subalbumImages = getURLsFromPage(subPage);
                    } catch (IOException e) {
                        LOGGER.warn("Error while loading subalbum " + subUrl, e);
                    }
                }
                if (isThisATest()) break;
            }
        }


        return imageURLs;
    }

    public void ripAlbum(Document page) {
        int x = 1;
        Elements thumbs = page.select("a.a-click > div.thumbnail > img");
        for (Element thumb : thumbs) {
            String image = "https://www.erofus.com" + thumb.attr("src").replaceAll("thumb", "medium");
            try {
                Map<String,String> opts = new HashMap<String, String>();
                opts.put("subdirectory", page.title().replaceAll(" \\| Erofus - Sex and Porn Comics", "").replaceAll(" ", "_"));
                opts.put("prefix", getPrefix(x));
                addURLToDownload(new URL(image), opts);
            } catch (MalformedURLException e) {
                LOGGER.info(e.getMessage());
            }
            x++;
        }
    }

    private boolean pageContainsImages(Document page) {
        Elements pageImages = page.select("a.a-click");
        for (Element pageLink : pageImages) {
            if (pageLink.attr("href").contains("/pic/")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}