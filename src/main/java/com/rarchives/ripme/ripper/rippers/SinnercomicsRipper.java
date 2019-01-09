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

public class SinnercomicsRipper extends AbstractHTMLRipper {

    private static final String HOST   = "sinnercomics",
                                DOMAIN = "sinnercomics.com";

    private static final int SLEEP_TIME = 500;

    enum RIP_TYPE {
        HOMEPAGE,
        PINUP,
        COMIC
    }

    private RIP_TYPE ripType;
    private Integer pageNum;

    public SinnercomicsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String normalizeUrl(String url) {
        // Remove the comments hashtag
        return url.replaceAll("/#(comments|disqus_thread)", "/");
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String cleanUrl = normalizeUrl(url.toExternalForm());
        Pattern p;
        Matcher m;

        p = Pattern.compile("^https?://sinnercomics\\.com/comic/([a-zA-Z0-9-]*)/?$");
        m = p.matcher(cleanUrl);
        if (m.matches()) {
            // Comic
            this.ripType = RIP_TYPE.COMIC;
            return m.group(1).replaceAll("-page-\\d+", "");
        }

        p = Pattern.compile("^https?://sinnercomics\\.com(?:/page/([0-9]+))?/?$");
        m = p.matcher(cleanUrl);
        if (m.matches()) {
            // Homepage
            this.ripType = RIP_TYPE.HOMEPAGE;
            if (m.group(1) != null) {
                this.pageNum = Integer.valueOf(m.group(1));
            } else {
                this.pageNum = 1;
            }
            return "homepage";
        }

        p = Pattern.compile("^https?://sinnercomics\\.com/([a-zA-Z0-9-]+)(?:/#comments)?/?$");
        m = p.matcher(cleanUrl);
        if (m.matches()) {
            // Pinup image
            this.ripType = RIP_TYPE.PINUP;
            return m.group(1);
        }

        throw new MalformedURLException("Expected sinnercomics.com URL format: " +
                        "/pinupName or /comic/albumName or /page/number  - got " + cleanUrl + " instead");
    }

    @Override
    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        try {
            getGID(url);
        } catch (MalformedURLException e) {
            // Can't get GID, can't rip it.
            return false;
        }
        return true;
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextUrl = null;

        switch (this.ripType) {
            case PINUP:
                throw new IOException("No next page on a pinup");

            case COMIC:
                // We use comic-nav-next to the find the next page
                Element elem = doc.select("a.comic-nav-next").first();
                if (elem == null) {
                    throw new IOException("No more pages");
                }
                nextUrl = elem.attr("href");
                break;

            default: // case HOMEPAGE:
                this.pageNum++;
                nextUrl = "https://sinnercomics.com/page/" + String.valueOf(this.pageNum);
                break;
        }

        // Wait to avoid IP bans
        sleep(SLEEP_TIME);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();

        switch (this.ripType) {
            case COMIC:
                // comic pages only contain one image, determined by a meta tag
                for (Element el : doc.select("meta[property=og:image]")) {
                    String imageSource = el.attr("content");
                    imageSource = imageSource.replace(" alt=", "");
                    result.add(imageSource);
                }
                break;
            default:
                for (Element el : doc.select(".entry p img")) {
                    // These filters match the full size images but might match ads too...
                    result.add(el.attr("src"));
                }
                break;
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}
