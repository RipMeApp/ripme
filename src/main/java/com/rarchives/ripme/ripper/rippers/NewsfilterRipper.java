package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsfilterRipper extends AbstractHTMLRipper {

    private static final String HOST = "newsfilter";
    private static final String DOMAIN = "newsfilter.org";

    public NewsfilterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.indexOf('#') >= 0) {
            u = u.substring(0, u.indexOf('#'));
        }
        u = u.replace("https?://m\\.newsfilter\\.org", "http://newsfilter.org");
        return new URL(u);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([wm]+\\.)?newsfilter\\.org/gallery/([^/]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        throw new MalformedURLException(
                "Expected newsfilter gallery format: http://newsfilter.org/gallery/galleryid" +
                        " Got: " + url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    protected Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> imgURLs = new ArrayList<>();
        Elements thumbnails = page.select("#galleryImages .inner-block img");
        for (Element thumb : thumbnails) {
            String thumbUrl = thumb.attr("src");
            String picUrl = thumbUrl.replace("thumbs/", "");
            // use HTTP instead of HTTPS (less headaches)
            imgURLs.add(picUrl.replaceFirst("https://", "http://"));
        }
        return imgURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
