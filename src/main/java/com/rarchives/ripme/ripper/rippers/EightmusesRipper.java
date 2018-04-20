package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.utils.Utils;
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
    public boolean hasASAPRipping() {
        return true;
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
        Pattern p = Pattern.compile("^https?://(www\\.)?8muses\\.com/(comix|comics)/album/([a-zA-Z0-9\\-_]+).*$");
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
        int x = 1;
        // This contains the thumbnails of all images on the page
        Elements pageImages = page.getElementsByClass("c-tile");
        for (Element thumb : pageImages) {
            // If true this link is a sub album
            if (thumb.attr("href").contains("/comics/album/")) {
                String subUrl = "https://www.8muses.com" + thumb.attr("href");
                try {
                    logger.info("Retrieving " + subUrl);
                    sendUpdate(STATUS.LOADING_RESOURCE, subUrl);
                    Document subPage = Http.url(subUrl).get();
                    // If the page below this one has images this line will download them
                    List<String> subalbumImages = getURLsFromPage(subPage);
                    logger.info("Found " + subalbumImages.size() + " images in subalbum");
                } catch (IOException e) {
                    logger.warn("Error while loading subalbum " + subUrl, e);
                }

            } else if (thumb.attr("href").contains("/comics/picture/")) {
                logger.info("This page is a album");
                logger.info("Ripping image");
                if (super.isStopped()) break;
                // Find thumbnail image source
                String image = null;
                if (thumb.hasAttr("data-cfsrc")) {
                    image = thumb.attr("data-cfsrc");
                }
                else {
                    String imageHref = thumb.attr("href");
                    if (imageHref.equals("")) continue;
                    if (imageHref.startsWith("/")) {
                        imageHref = "https://www.8muses.com" + imageHref;
                    }
                    try {
                        logger.info("Retrieving full-size image location from " + imageHref);
                        image = getFullSizeImage(imageHref);
                        URL imageUrl = new URL(image);
                        if (Utils.getConfigBoolean("8muses.use_short_names", false)) {
                            addURLToDownload(imageUrl, getPrefixShort(x), getSubdir(page.select("title").text()), this.url.toExternalForm(), cookies, "");
                        } else {
                            addURLToDownload(imageUrl, getPrefixLong(x), getSubdir(page.select("title").text()), this.url.toExternalForm(), cookies);
                        }
                        // X is our page index
                        x++;

                    } catch (IOException e) {
                        logger.error("Failed to get full-size image from " + imageHref);
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

    private String getTitle(String albumTitle) {
        albumTitle = albumTitle.replace("A huge collection of free porn comics for adults. Read ", "");
        albumTitle = albumTitle.replace(" online for free at 8muses.com", "");
        albumTitle = albumTitle.replace(" ", "_");
        return albumTitle;
    }

    private String getSubdir(String rawHref) {
        logger.info("Raw title: " + rawHref);
        String title = rawHref;
        title = title.replaceAll("8muses - Sex and Porn Comics", "");
        title = title.replaceAll("\t\t", "");
        title = title.replaceAll("\n", "");
        title = title.replaceAll("\\| ", "");
        title = title.replace(" ", "-");
        logger.info(title);
        return title;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
    }

    public String getPrefixLong(int index) {
        return String.format("%03d_", index);
    }

    public String getPrefixShort(int index) {
        return String.format("%03d", index);
    }
}
