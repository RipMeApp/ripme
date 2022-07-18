package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ElitebabesRipper extends AbstractHTMLRipper {

    private static final String HOST = "elitebabes";
    private static final String DOMAIN = "elitebabes.com";

    public ElitebabesRipper(final URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(final URL url) throws MalformedURLException {
        final Pattern albumRegex =
                Pattern.compile("^https?://(?:www.)?elitebabes\\.com/([a-z\\d-]+)/?$");
        final Matcher matcher = albumRegex.matcher(url.toExternalForm());
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    @Override
    protected Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    protected List<String> getURLsFromPage(final Document document) {
        final List<String> result = new ArrayList<>();
        final Elements lists = document.getElementsByClass("list-gallery");
        for (final Element list : lists) {
            for (final Element listElement : list.children()) {
                // Skipping Ads
                if (listElement.classNames().contains("w300")) {
                    continue;
                }
                String link;
                final Element anchor = listElement.child(0);
                final String datasrc = anchor.attr("data-srcset");
                if (datasrc.isEmpty()) {
                    link = anchor.absUrl("href");
                } else {
                    link = datasrc.split(", ")[0].split(" ")[0];
                }
                result.add(link);
            }
        }
        return result;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(final URL url) {
        try {
            final Document page = Http.url(url).get();
            return page.getElementsByClass("list-gallery").isEmpty();
        } catch (final IOException ignored) {
            return false;
        }
    }

    @Override
    public List<String> getAlbumsToQueue(Document document) {
        do {
            final Elements lists = document.getElementsByClass("gallery-a");
            lists.addAll(document.getElementsByClass("list-category"));
            for (final Element list : lists) {
                // Skipping Ads
                if (list.classNames().contains("clip-a")) {
                    continue;
                }
                for (final Element listElement : list.children()) {
                    // Skipping videos since we don't have a compatible ripper
                    if (listElement.classNames().contains("vid")) {
                        continue;
                    }
                    final String link = listElement.child(0).absUrl("href");
                    // Adding directly to main window to avoid getting stuck-like experience
                    MainWindow.addUrlToQueue(link);
                }
            }
            document = getNextAlbumPage(document);
        } while (Objects.nonNull(document));
        // Returning empty list as albums are already added to queue
        return Collections.emptyList();
    }

    @Override
    protected void downloadURL(final URL url, final int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private Document getNextAlbumPage(final Document document) {
        final Elements pageList = document.getElementsByClass("m-pagination__item");
        if (pageList.size() > 0) {
            boolean foundCurrent = false;
            for (final Element page : pageList) {
                if (foundCurrent) {
                    final String link = page.absUrl("href");
                    try {
                        return Http.url(link).get();
                    } catch (final IOException ignored) {
                    }
                }
                if (page.classNames().contains("m-pagination__item--current")) {
                    foundCurrent = true;
                }
            }
        } else {
            try {
                final String link =
                        document.getElementsByClass("next").get(0).child(0).absUrl("href");
                return Http.url(link).get();
            } catch (final Exception ignored) {
            }
        }
        return null;
    }

    @Override
    public String getAlbumTitle(final URL url) throws MalformedURLException {
        try {
            final Document document = Http.url(url).get();
            final Element header = document.getElementsByClass("header-inline").get(0);
            return "Elitebabes_" + header.ownText();
        } catch (final MalformedURLException e) {
            throw e;
        } catch (final Exception e) {
            return super.getAlbumTitle(url);
        }
    }
}
