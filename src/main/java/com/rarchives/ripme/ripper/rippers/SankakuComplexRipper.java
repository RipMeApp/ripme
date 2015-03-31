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

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class SankakuComplexRipper extends AbstractHTMLRipper {
    private Document albumDoc = null;
    private Map<String,String> cookies = new HashMap<String,String>();

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
                return URLDecoder.decode(m.group(1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MalformedURLException("Cannot decode tag name '" + m.group(1) + "'");
            }
        }
        throw new MalformedURLException("Expected sankakucomplex.com URL format: " +
                        "idol.sankakucomplex.com?...&tags=something... - got " +
                        url + "instead");
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
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        // Image URLs are basically thumbnail URLs with a different domain, a simple
        // path replacement, and a ?xxxxxx post ID at the end (obtainable from the href)
        for (Element thumbSpan : doc.select("div.content > div > span.thumb")) {
            String postId = thumbSpan.attr("id").replaceAll("p", "");
            Element thumb = thumbSpan.getElementsByTag("img").first();
            String image = thumb.attr("abs:src")
                                .replace(".sankakucomplex.com/data/preview",
                                         "s.sankakucomplex.com/data") + "?" + postId;
            imageURLs.add(image);
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        // Mock up the URL of the post page based on the post ID at the end of the URL.
        String postId = url.toExternalForm().replaceAll(".*\\?", "");
        addURLToDownload(url, postId + "_", "", "", null);
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element pagination = doc.select("div.pagination").first();
        if (pagination.hasAttr("next-page-url")) {
            return Http.url(pagination.attr("abs:next-page-url")).cookies(cookies).get();
        } else{
            return null;
        }
    }
}
