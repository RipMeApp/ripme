package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;

public class ErofusRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(ErofusRipper.class);

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
        logger.info(page);
        List<String> imageURLs = new ArrayList<>();
        if (pageContainsImages(page)) {
            logger.info("Page contains images");
            ripAlbum(page);
        } else {
            // This contains the thumbnails of all images on the page
            Elements pageImages = page.select("a.a-click");
            for (Element pageLink : pageImages) {
                if (super.isStopped()) break;
                if (pageLink.attr("href").contains("comics")) {
                    String subUrl = "https://erofus.com" + pageLink.attr("href");
                    try {
                        logger.info("Retrieving " + subUrl);
                        sendUpdate(RipStatusMessage.STATUS.LOADING_RESOURCE, subUrl);
                        Document subPage = Http.url(subUrl).get();
                        List<String> subalbumImages = getURLsFromPage(subPage);
                    } catch (IOException e) {
                        logger.warn("Error while loading subalbum " + subUrl, e);
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
                addURLToDownload(new URI(image).toURL(), opts);
            } catch (MalformedURLException | URISyntaxException e) {
                logger.info(e.getMessage());
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
