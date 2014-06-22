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

import com.rarchives.ripme.ripper.AbstractSinglePageRipper;
import com.rarchives.ripme.utils.Http;

public class ChanRipper extends AbstractSinglePageRipper {

    public ChanRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        String host = this.url.getHost();
        host = host.substring(0, host.lastIndexOf('.'));
        if (host.contains(".")) {
            // Host has subdomain (www)
            host = host.substring(host.lastIndexOf('.') + 1);
        }
        String board = this.url.toExternalForm().split("/")[3];
        return host + "_" + board;
    }

    @Override
    public boolean canRip(URL url) {
        // TODO Whitelist?
        if (url.getHost().equals("anon-ib.com")) {
            return true;
        }
        return url.getHost().contains("chan") &&
                ( url.toExternalForm().contains("/res/")      // Most chans
               || url.toExternalForm().contains("/thread/")); // 4chan
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        String u = url.toExternalForm();
        if (u.contains("/res/")) {
            p = Pattern.compile("^.*(chan|anon-ib).*\\.[a-z]{2,3}/[a-zA-Z0-9]+/res/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(2);
            }
        }
        else if (u.contains("/thread/")) {
            p = Pattern.compile("^.*chan.*\\.[a-z]{2,3}/[a-zA-Z0-9]+/thread/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }
        }

        throw new MalformedURLException(
                "Expected *chan URL formats: "
                        + "*chan.com/@/res/####.html"
                        + " Got: " + u);
    }

    @Override
    public String getDomain() {
        return this.url.getHost();
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(this.url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();
        Pattern p; Matcher m;
        for (Element link : page.select("a")) {
            if (!link.hasAttr("href")) { 
                continue;
            }
            if (!link.attr("href").contains("/src/")
             && !link.attr("href").contains("4cdn.org")) {
                logger.debug("Skipping link that does not contain /src/: " + link.attr("href"));
                continue;
            }
            if (link.attr("href").contains("=http")
             || link.attr("href").contains("http://imgops.com/")) {
                logger.debug("Skipping link that contains '=http' or 'imgops.com': " + link.attr("href"));
                continue;
            }
            p = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|webm)$", Pattern.CASE_INSENSITIVE);
            m = p.matcher(link.attr("href"));
            if (m.matches()) {
                String image = link.attr("href");
                if (image.startsWith("//")) {
                    image = "http:" + image;
                }
                if (image.startsWith("/")) {
                    image = "http://" + this.url.getHost() + image;
                }
                // Don't download the same URL twice
                if (imageURLs.contains(image)) {
                    logger.debug("Already attempted: " + image);
                    continue;
                }
                imageURLs.add(image);
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}