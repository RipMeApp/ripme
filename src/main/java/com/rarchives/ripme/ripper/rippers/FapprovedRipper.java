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

public class FapprovedRipper extends AbstractHTMLRipper {

    private int pageIndex = 1;
    private String username = null;

    public FapprovedRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "fapproved";
    }
    @Override
    public String getDomain() {
        return "fapproved.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[w.]*fapproved.com/users/([a-zA-Z0-9\\-_]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            username = m.group(1);
            return username;
        }
        throw new MalformedURLException("Fapproved user not found in " + url + ", expected http://fapproved.com/users/username/images");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL("http://fapproved.com/users/" + getGID(url));
    }

    @Override
    public Document getFirstPage() throws IOException {
        pageIndex = 1;
        String pageURL = getPageURL(pageIndex);
        return Http.url(pageURL)
                   .ignoreContentType()
                   .get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if ((doc.select("div.pagination li.next.disabled").size() != 0)
            || (doc.select("div.pagination").size() == 0)) {
            throw new IOException("No more pages found");
        }
        sleep(1000);
        pageIndex++;
        String pageURL = getPageURL(pageIndex);
        return Http.url(pageURL)
                   .ignoreContentType()
                   .get();
    }

    private String getPageURL(int index) throws IOException {
        if (username == null) {
            username = getGID(this.url);
        }
        return "http://fapproved.com/users/" + username + "/images?page=" + pageIndex;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();
        for (Element image : page.select("div.actual-image img")) {
            String imageURL = image.attr("src");
            if (imageURL.startsWith("//")) {
                imageURL = "http:" + imageURL;
            }
            else if (imageURL.startsWith("/")) {
                imageURL = "http://fapproved.com" + imageURL;
            }
            imageURLs.add(imageURL);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
