package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class ChanRipper extends AlbumRipper {

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

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
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
    public void rip() throws IOException {
        Set<String> attempted = new HashSet<String>();
        int index = 0;
        Pattern p; Matcher m;
        logger.info("Retrieving " + this.url);
        Document doc = getDocument(this.url);
        for (Element link : doc.select("a")) {
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
                if (attempted.contains(image)) {
                    logger.debug("Already attempted: " + image);
                    continue;
                }
                index += 1;
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(image), prefix);
                attempted.add(image);
            }
        }
        waitForThreads();
    }

}