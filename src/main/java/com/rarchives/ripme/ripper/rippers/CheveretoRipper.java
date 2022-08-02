package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class CheveretoRipper extends AbstractHTMLRipper {
    private static final Map<String, String> CONSENT_COOKIE;
    static {
        CONSENT_COOKIE = new TreeMap<String, String>();
        CONSENT_COOKIE.put("AGREE_CONSENT", "1");
    }

    public CheveretoRipper(URL url) throws IOException {
        super(url);
    }

    private static List<String> explicit_domains = Arrays.asList("tag-fox.com", "kenzato.uk");

    @Override
    public String getHost() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public String getDomain() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public boolean canRip(URL url) {
        String url_name = url.toExternalForm();
        if (explicit_domains.contains(url_name.split("/")[2])) {
                return true;
        }
        return false;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Element titleElement = getCachedFirstPage().select("meta[property=og:title]").first();
            String title = titleElement.attr("content");
            title = title.substring(title.lastIndexOf('/') + 1);
            return getHost() + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            LOGGER.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("(?:https?://)?(?:www\\.)?[a-z1-9-]*\\.[a-z1-9]*(?:[a-zA-Z1-9]*)/album/([a-zA-Z1-9]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected chevereto URL format: " +
                        "site.domain/album/albumName or site.domain/username/albums- got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).cookies(CONSENT_COOKIE).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextUrl = "";
        // We use comic-nav-next to the find the next page
        Element elem = doc.select("li.pagination-next > a").first();
            if (elem == null) {
                throw new IOException("No more pages");
            }
            String nextPage = elem.attr("href");
            // Some times this returns a empty string
            // This for stops that
            if (nextPage == "") {
                return null;
            } else {
                return Http.url(nextPage).cookies(CONSENT_COOKIE).get();
            }
        }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
            for (Element el : doc.select("a.image-container > img")) {
                String imageSource = el.attr("src");
                // We remove the .md from images so we download the full size image
                // not the medium ones
                imageSource = imageSource.replace(".md", "");
                result.add(imageSource);
            }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
