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
    private Map<String,String> cookies = new HashMap<>();
    // TODO put up a wiki page on using maps to store titles
    // the map for storing the title of each album when downloading sub albums
    private Map<URL,String> urlTitles = new HashMap<>();

    private Boolean rippingSubalbums = false;

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
        Pattern p = Pattern.compile("^https?://(www\\.)?8muses\\.com/comix/album/([a-zA-Z0-9\\-_]+).*$");
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
            title = title.replace("A huge collection of free porn comics for adults. Read", "");
            title = title.replace("online for free at 8muses.com", "");
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
        List<String> imageURLs = new ArrayList<>();
        // get the first image link on the page and check if the last char in it is a number
        // if it is a number then we're ripping a comic if not it's a subalbum
        String firstImageLink = page.select("div.gallery > a.t-hover").first().attr("href");
        Pattern p = Pattern.compile("/comix/picture/([a-zA-Z0-9\\-_/]*/)?\\d+");
        Matcher m = p.matcher(firstImageLink);
        if (!m.matches()) {
            logger.info("Ripping subalbums");
            // Page contains subalbums (not images)
            Elements albumElements = page.select("div.gallery > a.t-hover");
            List<Element> albumsList = albumElements.subList(0, albumElements.size());
            Collections.reverse(albumsList);
            // Iterate over elements in reverse order
            for (Element subalbum : albumsList) {
                String subUrl = subalbum.attr("href");
                // This if is to skip ads which don't have a href
                if (subUrl != "") {
                    subUrl = subUrl.replaceAll("\\.\\./", "");
                    if (subUrl.startsWith("//")) {
                        subUrl = "https:";
                    }
                    else if (!subUrl.startsWith("http://")) {
                        subUrl = "https://www.8muses.com" + subUrl;
                    }
                    try {
                        logger.info("Retrieving " + subUrl);
                        sendUpdate(STATUS.LOADING_RESOURCE, subUrl);
                        Document subPage = Http.url(subUrl).get();
                        // Get all images in subalbum, add to list.
                        List<String> subalbumImages = getURLsFromPage(subPage);
                        String albumTitle = subPage.select("meta[name=description]").attr("content");
                        albumTitle = albumTitle.replace("A huge collection of free porn comics for adults. Read ", "");
                        albumTitle = albumTitle.replace(" online for free at 8muses.com", "");
                        albumTitle = albumTitle.replace(" ", "_");
                        // albumTitle = albumTitle.replace("Sex and Porn Comics", "");
                        // albumTitle = albumTitle.replace("|", "");
                        // albumTitle = albumTitle.replace("8muses", "");
                        // albumTitle = albumTitle.replaceAll("-", "_");
                        // albumTitle = albumTitle.replaceAll(" ", "_");
                        // albumTitle = albumTitle.replaceAll("___", "_");
                        // albumTitle = albumTitle.replaceAll("__", "_");
                        // // This is here to remove the trailing __ from folder names
                        // albumTitle = albumTitle.replaceAll("__", "");
                        logger.info("Found " + subalbumImages.size() + " images in subalbum");
                        int prefix = 1;
                        for (String image : subalbumImages) {
                            URL imageUrl = new URL(image);
                            // urlTitles.put(imageUrl, albumTitle);
                            addURLToDownload(imageUrl, getPrefix(prefix), albumTitle, this.url.toExternalForm(), cookies);
                            prefix = prefix + 1;
                        }
                        rippingSubalbums = true;
                        imageURLs.addAll(subalbumImages);
                    } catch (IOException e) {
                        logger.warn("Error while loading subalbum " + subUrl, e);
                    }
                }
            }
        }
        else {
            // Page contains images
            for (Element thumb : page.select(".image")) {
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
                        image = getFullSizeImage(parentHref);
                    } catch (IOException e) {
                        logger.error("Failed to get full-size image from " + parentHref);
                        continue;
                    }
                }
                if (!image.contains("8muses.com")) {
                    // Not hosted on 8muses.
                    continue;
                }
                imageURLs.add(image);
                if (isThisATest()) break;
            }
        }
        return imageURLs;
    }

    private String getFullSizeImage(String imageUrl) throws IOException {
        sendUpdate(STATUS.LOADING_RESOURCE, imageUrl);
        logger.info("Getting full sized image from " + imageUrl);
        Document doc = new Http(imageUrl).get(); // Retrieve the webpage  of the image URL
        String imageName = doc.select("input[id=imageName]").attr("value"); // Select the "input" element from the page
        return "https://www.8muses.com/image/fm/" + imageName;
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
