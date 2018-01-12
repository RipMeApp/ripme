package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MyhentaicomicsRipper extends AbstractHTMLRipper {
    private static boolean isTag;

    public MyhentaicomicsRipper(URL url) throws IOException {
    super(url);
    }

    @Override
    public String getHost() {
        return "myhentaicomics";
    }

    @Override
    public String getDomain() {
        return "myhentaicomics.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://myhentaicomics.com/index.php/([a-zA-Z0-9-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isTag = false;
            return m.group(1);
        }

        Pattern pa = Pattern.compile("^https?://myhentaicomics.com/index.php/search\\?q=([a-zA-Z0-9-]*)([a-zA-Z0-9=&]*)?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            isTag = true;
            return ma.group(1);
        }

        Pattern pat = Pattern.compile("^https?://myhentaicomics.com/index.php/tag/([0-9]*)/?([a-zA-Z%0-9+?=:]*)?$");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            isTag = true;
            return mat.group(1);
        }

        throw new MalformedURLException("Expected myhentaicomics.com URL format: " +
                        "myhentaicomics.com/index.php/albumName - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextUrl = "";
        Element elem = doc.select("a.ui-icon-right").first();
            String nextPage = elem.attr("href");
            Pattern p = Pattern.compile("/index.php/[a-zA-Z0-9_-]*\\?page=\\d");
            Matcher m = p.matcher(nextPage);
            if (m.matches()) {
                nextUrl = "http://myhentaicomics.com" + m.group(0);
                }
            if (nextUrl == "") {
                throw new IOException("No more pages");
            }
            // Sleep for half a sec to avoid getting IP banned
            sleep(500);
            return Http.url(nextUrl).get();
        }

    // This replaces getNextPage when downloading from searchs and tags
    private List<String> getNextAlbumPage(String pageUrl) {
        List<String> albumPagesList = new ArrayList<>();
        int pageNumber = 1;
        albumPagesList.add("http://myhentaicomics.com/index.php/" + pageUrl.split("\\?")[0] + "?page=" + Integer.toString(pageNumber));
            while (true) {
                String urlToGet = "http://myhentaicomics.com/index.php/" + pageUrl.split("\\?")[0] + "?page=" + Integer.toString(pageNumber);
                Document nextAlbumPage;
                try {
                    logger.info("Grabbing " + urlToGet);
                    nextAlbumPage = Http.url(urlToGet).get();
                } catch (IOException e) {
                    logger.warn("Failed to log link in Jsoup");
                    nextAlbumPage = null;
                    e.printStackTrace();
                }
                Element elem = nextAlbumPage.select("a.ui-icon-right").first();
                String nextPage = elem.attr("href");
                pageNumber = pageNumber + 1;
                if (nextPage == "") {
                    logger.info("Got " + pageNumber + " pages");
                    break;
                }
                else {
                    logger.info(nextPage);
                    albumPagesList.add(nextPage);
                    logger.info("Adding " + nextPage);
                }
            }
            return albumPagesList;
        }

    private List<String> getAlbumsFromPage(String url) {
        List<String> pagesToRip;
        List<String> result = new ArrayList<>();
        logger.info("Running getAlbumsFromPage");
        Document doc;
        try {
            doc = Http.url("http://myhentaicomics.com" + url).get();
        } catch (IOException e) {
            logger.warn("Failed to log link in Jsoup");
            doc = null;
            e.printStackTrace();
        }
        // This for goes over every album on the page
        for (Element elem : doc.select("li.g-album > a")) {
            String link = elem.attr("href");
            logger.info("Grabbing album " + link);
            pagesToRip = getNextAlbumPage(link);
            logger.info(pagesToRip);
            for (String element : pagesToRip) {
                Document album_doc;
                try {
                    logger.info("grabbing " + element + " with jsoup");
                    boolean startsWithHttp = element.startsWith("http://");
                    if (!startsWithHttp) {
                        album_doc = Http.url("http://myhentaicomics.com/" + element).get();
                    }
                    else {
                        album_doc = Http.url(element).get();
                    }
                } catch (IOException e) {
                    logger.warn("Failed to log link in Jsoup");
                    album_doc = null;
                    e.printStackTrace();
                }
                for (Element el :album_doc.select("img")) {
                    String imageSource = el.attr("src");
                    // This bool is here so we don't try and download the site logo
                    if (!imageSource.startsWith("http://")) {
                        // We replace thumbs with resizes so we can the full sized images
                        imageSource = imageSource.replace("thumbs", "resizes");
                        String url_string = "http://myhentaicomics.com/" + imageSource;
                        url_string = url_string.replace("%20", "_");
                        url_string = url_string.replace("%27", "");
                        url_string = url_string.replace("%28", "_");
                        url_string = url_string.replace("%29", "_");
                        url_string = url_string.replace("%2C", "_");
                        if (isTag) {
                            logger.info("Downloading from a tag or search");
                            try {
                                sleep(500);
                                result.add("http://myhentaicomics.com/" + imageSource);
                                addURLToDownload(new URL("http://myhentaicomics.com/" + imageSource), "", url_string.split("/")[6]);
                            }
                            catch (MalformedURLException e) {
                                logger.warn("Malformed URL");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<String> getListOfPages(Document doc) {
        List<String> pages = new ArrayList<>();
        // Get the link from the last button
        String nextPageUrl = doc.select("a.ui-icon-right").last().attr("href");
        Pattern pat = Pattern.compile("/index\\.php/tag/[0-9]*/[a-zA-Z0-9_\\-:+]*\\?page=(\\d+)");
        Matcher mat = pat.matcher(nextPageUrl);
        if (mat.matches()) {
            logger.debug("Getting pages from a tag");
            String base_link = mat.group(0).replaceAll("\\?page=\\d+", "");
            logger.debug("base_link is " + base_link);
            int numOfPages = Integer.parseInt(mat.group(1));
            for (int x = 1; x != numOfPages +1; x++) {
                logger.debug("running loop");
                String link = base_link + "?page=" + Integer.toString(x);
                pages.add(link);
            }
        } else {
            Pattern pa = Pattern.compile("/index\\.php/search\\?q=[a-zA-Z0-9_\\-:]*&page=(\\d+)");
            Matcher ma = pa.matcher(nextPageUrl);
            if (ma.matches()) {
                logger.debug("Getting pages from a search");
                String base_link = ma.group(0).replaceAll("page=\\d+", "");
                logger.debug("base_link is " + base_link);
                int numOfPages = Integer.parseInt(ma.group(1));
                for (int x = 1; x != numOfPages +1; x++) {
                    logger.debug("running loop");
                    String link = base_link + "page=" + Integer.toString(x);
                    logger.debug(link);
                    pages.add(link);
                }
            }
        }
        return pages;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        // Checks if this is a comic page or a page of albums
        // If true the page is a page of albums
        if (doc.toString().contains("class=\"g-item g-album\"")) {
            // This if checks that there is more than 1 page
            if (doc.select("a.ui-icon-right").last().attr("href") != "") {
                // There is more than one page so we call getListOfPages
                List<String> pagesToRip = getListOfPages(doc);
                logger.debug("Pages to rip = " + pagesToRip);
                for (String url : pagesToRip) {
                    logger.debug("Getting albums from " + url);
                    result = getAlbumsFromPage(url);
                }
            } else {
                logger.debug("There is only one page on this page of albums");
                // There is only 1 page so we call getAlbumsFromPage and pass it the page url
                result = getAlbumsFromPage(doc.select("div.g-description > a").attr("href"));
            }
            return result;
        }
        else {
        for (Element el : doc.select("img")) {
            String imageSource = el.attr("src");
            // This bool is here so we don't try and download the site logo
            if (!imageSource.startsWith("http://") && !imageSource.startsWith("https://")) {
            // We replace thumbs with resizes so we can the full sized images
            imageSource = imageSource.replace("thumbs", "resizes");
            result.add("http://myhentaicomics.com/" + imageSource);
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
