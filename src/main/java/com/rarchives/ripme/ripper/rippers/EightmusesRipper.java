package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class EightmusesRipper extends AbstractHTMLRipper {

    private Document albumDoc = null;
    private Map<String,String> cookies = new HashMap<String,String>();

    public EightmusesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "8muses";
    }
    @Override
    public String getDomain() {
        return "8muses.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?8muses\\.com/index/category/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.8muses.com/index/category/albumname, got: " + url);
        }
        return m.group(m.groupCount());
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Element titleElement = getFirstPage().select("meta[name=description]").first();
            String title = titleElement.attr("content");
            title = title.substring(title.lastIndexOf('/') + 1);
            return getHost() + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            Response resp = Http.url(url).response();
            cookies.putAll(resp.cookies());
            albumDoc = resp.parse();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();
        if (page.select(".preview > span").size() > 0) {
            // Page contains subalbums (not images)
            Elements albumElements = page.select("a.preview");
            List<Element> albumsList = albumElements.subList(0, albumElements.size());
            Collections.reverse(albumsList);
            // Iterate over elements in reverse order
            for (Element subalbum : albumsList) {
                String subUrl = subalbum.attr("href");
                subUrl = subUrl.replaceAll("\\.\\./", "");
                if (subUrl.startsWith("//")) {
                    subUrl = "http:";
                }
                else if (!subUrl.startsWith("http://")) {
                    subUrl = "http://www.8muses.com/" + subUrl;
                }
                try {
                    logger.info("Retrieving " + subUrl);
                    sendUpdate(STATUS.LOADING_RESOURCE, subUrl);
                    Document subPage = Http.url(subUrl).get();
                    // Get all images in subalbum, add to list.
                    List<String> subalbumImages = getURLsFromPage(subPage);
                    logger.info("Found " + subalbumImages.size() + " images in subalbum");
                    imageURLs.addAll(subalbumImages);
                } catch (IOException e) {
                    logger.warn("Error while loading subalbum " + subUrl, e);
                    continue;
                }
            }
        }
        else {
            // Page contains images
            for (Element thumb : page.select("div.item .holder img")) {
                if (super.isStopped()) break;
                // Find thumbnail image source
                String image = null;
                if (thumb.hasAttr("data-cfsrc")) {
                    image = thumb.attr("data-cfsrc");
                }
                else {
                    String parentHref = thumb.parent().attr("href");
                    if (parentHref.equals("")) continue;
                    if (parentHref.startsWith("/")) {
                        parentHref = "https://www.8muses.com" + parentHref;
                    }
                    try {
                        logger.info("Retrieving full-size image location from " + parentHref);
                        Thread.sleep(1000);
                        image = getFullSizeImage(parentHref);
                    } catch (IOException e) {
                        logger.error("Failed to get full-size image from " + parentHref);
                        continue;
                    } catch (InterruptedException e) {
                        logger.error("Interrupted while getting full-size image from " + parentHref);
                        continue;
                    }
                }
                if (!image.contains("8muses.com")) {
                    // Not hosted on 8mues.
                    continue;
                }
                // Remove relative directory path naming
                image = image.replaceAll("\\.\\./", "");
                if (image.startsWith("//")) {
                    image = "http:" + image;
                }
                // Convert from thumb URL to full-size
                if (image.contains("-cu_")) {
                    image = image.replaceAll("-cu_[^.]+", "-me");
                }
                image = image.replaceAll(" ", "%20");
                imageURLs.add(image);
                if (isThisATest()) break;
            }
        }
        return imageURLs;
    }

    private String getFullSizeImage(String imageUrl) throws IOException {
        sendUpdate(STATUS.LOADING_RESOURCE, imageUrl);
        Document doc = new Http(imageUrl).get();
        return doc.select("#image").first().attr("src");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
    }

    @Override
    public String getPrefix(int index) {
        return String.format("%03d_", index);
    }
}
