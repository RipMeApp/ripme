package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MyhentaicomicsRipper extends AbstractHTMLRipper {
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
            return m.group(1);
        }

        Pattern pa = Pattern.compile("^https?://myhentaicomics.com/index.php/search\\?q=([a-zA-Z0-9-]*)([a-zA-Z0-9=&]*)?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return ma.group(1);
        }

        Pattern pat = Pattern.compile("^https?://myhentaicomics.com/index.php/tag/([0-9]*)/?([a-zA-Z%0-9+?=:]*)?$");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return mat.group(1);
        }

        throw new MalformedURLException("Expected myhentaicomics.com URL format: " +
                        "myhentaicomics.com/index.php/albumName - got " + url + " instead");
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("^https?://myhentaicomics.com/index.php/search\\?q=([a-zA-Z0-9-]*)([a-zA-Z0-9=&]*)?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return true;
        }

        Pattern pat = Pattern.compile("^https?://myhentaicomics.com/index.php/tag/([0-9]*)/?([a-zA-Z%0-9+?=:]*)?$");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select(".g-album > a")) {
            urlsToAddToQueue.add(getDomain() + elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        return super.getFirstPage();
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
                nextUrl = "https://myhentaicomics.com" + m.group(0);
                }
            if (nextUrl.equals("")) {
                throw new IOException("No more pages");
            }
            // Sleep for half a sec to avoid getting IP banned
            sleep(500);
            return Http.url(nextUrl).get();
        }



    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("img")) {
            String imageSource = el.attr("src");
            // This bool is here so we don't try and download the site logo
            if (!imageSource.startsWith("http://") && !imageSource.startsWith("https://")) {
            // We replace thumbs with resizes so we can the full sized images
            imageSource = imageSource.replace("thumbs", "resizes");
            result.add("https://myhentaicomics.com" + imageSource);
                }
            }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }


}
