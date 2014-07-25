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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class TwodgalleriesRipper extends AbstractHTMLRipper {

    private int offset = 0;

    public TwodgalleriesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "2dgalleries";
    }
    @Override
    public String getDomain() {
        return "2dgalleries.com";
    }
    
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*2dgalleries.com/browse/profile\\?id=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected 2dgalleries.com album format: "
                        + "2dgalleries.com/browse/profile?id=####"
                        + " Got: " + url);
    }

    private String getURL(String userid, int offset) {
        return "http://en.2dgalleries.com/browse/user-artworks?uid=" + userid
                      + "&offset=" + offset
                      + "&ajx=1&pager=1&hr=1&pid=" + userid;
    }

    @Override
    public Document getFirstPage() throws IOException {
        String url = getURL(getGID(this.url), offset);
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        offset += 3;
        String url = getURL(getGID(this.url), offset);
        sleep(500);
        Document nextDoc = Http.url(url).get();
        if (nextDoc.select(".noartwork").size() > 0) {
            throw new IOException("No more images to retrieve");
        }
        return nextDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        for (Element thumb : doc.select("img")) {
            String image = thumb.attr("src");
            image = image.replace("/200H/", "/");
            if (image.startsWith("//")) {
                image = "http:" + image;
            } else if (image.startsWith("/")) {
                image = "http://en.2dgalleries.com" + image;
            }
            imageURLs.add(image);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}