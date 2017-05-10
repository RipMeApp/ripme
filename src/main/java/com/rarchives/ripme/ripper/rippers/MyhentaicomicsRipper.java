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
    public static boolean isTag;

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

        Pattern pat = Pattern.compile("^http://myhentaicomics.com/index.php/tag/([0-9]*)/?([a-zA-Z%0-9+\\?=:]*)?$");
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
    public List<String> getNextAlbumPage(String pageUrl) {
        List<String> albumPagesList = new ArrayList<String>();
        int pageNumber = 1;
        albumPagesList.add("http://myhentaicomics.com/index.php/" + pageUrl.split("\\?")[0] + "?page=" + Integer.toString(pageNumber));
            while (true) {
                String urlToGet = "http://myhentaicomics.com/index.php/" + pageUrl.split("\\?")[0] + "?page=" + Integer.toString(pageNumber);
                Document nextAlbumPage;
                try {
                    logger.info("Grabbing " + urlToGet);
                    nextAlbumPage = Http.url(urlToGet).get();
                } catch(IOException e) {
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


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<String>();
        List<String> pagesToRip;
        // Checks if this is a comic page or a page of albums
        if (doc.toString().contains("class=\"g-item g-album\"")) {
            for (Element elem : doc.select("li.g-album > a")) {
                String link = elem.attr("href");
                logger.info("Grabbing album " + link);
                pagesToRip = getNextAlbumPage(link);
                logger.info(pagesToRip);
                for (String element : pagesToRip) {
                    Document album_doc;
                    try {
                        logger.info("grabbing " + element + " with jsoup");
                        boolean startsWithhttp = element.startsWith("http");
                        if (startsWithhttp == false) {
                            album_doc = Http.url("http://myhentaicomics.com/" + element).get();
                        }
                        else {
                            album_doc = Http.url(element).get();
                        }
                    } catch(IOException e) {
                        logger.warn("Failed to log link in Jsoup");
                        album_doc = null;
                        e.printStackTrace();
                    }
                    for (Element el :album_doc.select("img")) {
                        String imageSource = el.attr("src");
                        // This bool is here so we don't try and download the site logo
                        boolean b = imageSource.startsWith("http");
                        if (b == false) {
                            // We replace thumbs with resizes so we can the full sized images
                            imageSource = imageSource.replace("thumbs", "resizes");
                            String url_string = "http://myhentaicomics.com/" + imageSource;
                            url_string = url_string.replace("%20", "_");
                            url_string = url_string.replace("%27", "");
                            url_string = url_string.replace("%28", "_");
                            url_string = url_string.replace("%29", "_");
                            url_string = url_string.replace("%2C", "_");
                            if (isTag == true) {
                                logger.info("Downloading from a tag or search");
                                try {
                                    addURLToDownload(new URL("http://myhentaicomics.com/" + imageSource), "", url_string.split("/")[6]);
                                }
                                catch(MalformedURLException e) {
                                    logger.warn("Malformed URL");
                                    e.printStackTrace();
                                }
                                result.add("http://myhentaicomics.com/" + imageSource);
                            }
                        }
                    }
                }
            }
        return result;
        }
        else {
        for (Element el : doc.select("img")) {
            String imageSource = el.attr("src");
            // This bool is here so we don't try and download the site logo
            boolean b = imageSource.startsWith("http");
            if (b == false) {
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
