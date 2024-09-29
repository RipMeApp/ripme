package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class SankakuComplexRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(SankakuComplexRipper.class);

    private Map<String,String> cookies = new HashMap<>();

    public SankakuComplexRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "sankakucomplex";
    }

    @Override
    public String getDomain() {
        return "sankakucomplex.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9]+\\.)?sankakucomplex\\.com/.*tags=([^&]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            try {
                return URLDecoder.decode(m.group(1) + "_" + m.group(2), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MalformedURLException("Cannot decode tag name '" + m.group(1) + "'");
            }
        }
        throw new MalformedURLException("Expected sankakucomplex.com URL format: " +
                        "idol.sankakucomplex.com?...&tags=something... - got " +
                        url + "instead");
    }

    public String getSubDomain(URL url){
        Pattern p = Pattern.compile("^https?://([a-zA-Z0-9]+\\.)?sankakucomplex\\.com/.*tags=([^&]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            try {
                return URLDecoder.decode(m.group(1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }
        return null;

    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).collectCookiesInto(cookies).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        // Image URLs are basically thumbnail URLs with a different domain, a simple
        // path replacement, and a ?xxxxxx post ID at the end (obtainable from the href)
        for (Element thumbSpan : doc.select("div.content > div > span.thumb > a")) {
            String postLink = thumbSpan.attr("href");
                try {
                    String subDomain = getSubDomain(url);
                    String siteURL = "https://" + subDomain + "sankakucomplex.com";
                    // Get the page the full sized image is on
                    Document subPage = Http.url(siteURL + postLink).get();
                    logger.info("Checking page " + siteURL + postLink);
                    imageURLs.add("https:" + subPage.select("div[id=stats] > ul > li > a[id=highres]").attr("href"));
                } catch (IOException e) {
                    logger.warn("Error while loading page " + postLink, e);
                }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(8000);
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element pagination = doc.select("div.pagination").first();
        if (pagination.hasAttr("next-page-url")) {
            String nextPage = pagination.attr("abs:next-page-url");
            // Only logged in users can see past page 25
            // Trying to rip page 26 will throw a no images found error
            if (!nextPage.contains("page=26")) {
                logger.info("Getting next page: " + pagination.attr("abs:next-page-url"));
                return Http.url(pagination.attr("abs:next-page-url")).cookies(cookies).get();
            }
        }
        throw new IOException("No more pages");
    }
}
