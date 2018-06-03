package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class LusciousRipper extends AbstractHTMLRipper {

    public LusciousRipper(URL url) throws IOException {
    super(url);
    }

    @Override
    public String getDomain() {
        return "luscious.net";
    }

    @Override
    public String getHost() {
        return "luscious";
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        Document page = Http.url(url).get();
        URL firstUrl = new URL("https://luscious.net" +  page.select("div > div.album_cover_item > a").first().attr("href"));
        LOGGER.info("First page is " + "https://luscious.net" +  page.select("div > div.album_cover_item > a").first().attr("href"));
        return Http.url(firstUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<>();
        Elements urlElements = page.select("img#single_picture");
        for (Element e : urlElements) {
            urls.add(e.attr("src"));
        }
        // This is here for pages with mp4s instead of images
        String video_image = "";
        video_image = page.select("div > video > source").attr("src");
        if (!video_image.equals("")) {
            urls.add(video_image);
        }
        return urls;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextPageUrl = "https://luscious.net" + doc.select("a.image_link[rel=next]").attr("href");
        // The more_like_this is here so we don't try to download the page that comes after the end of an album
        if (nextPageUrl == "https://luscious.net" ||
        nextPageUrl.contains("more_like_this")) {
            throw new IOException("No more pages");
        }

        return Http.url(nextPageUrl).get();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern
                .compile("^https?://luscious\\.net/albums/([-_.0-9a-zA-Z]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected luscious.net URL format: "
                + "luscious.net/albums/albumname  - got " + url
                + " instead");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}
