package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractRipper;

public class ChanRipper extends AbstractRipper {

    private static final Logger logger = Logger.getLogger(ChanRipper.class);

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
        return url.getHost().contains("chan") && url.toExternalForm().contains("/res/");
    }

    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*chan.*\\.[a-z]{2,3}/[a-z]+/res/([0-9]+)(\\.html|\\.php)?.*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected *chan URL formats: "
                        + "*chan.com/@/res/####.html"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        Set<String> attempted = new HashSet<String>();
        int index = 0;
        Pattern p; Matcher m;
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm())
                            .userAgent(USER_AGENT)
                            .get();
        for (Element link : doc.select("a")) {
            if (!link.hasAttr("href")) { 
                continue;
            }
            if (!link.attr("href").contains("/src/")) {
                logger.debug("Skipping link that does not contain /src/: " + link.attr("href"));
                continue;
            }
            System.err.println("URL=" + link.attr("href"));
            p = Pattern.compile("^.*\\.(jpg|jpeg|png|gif)$", Pattern.CASE_INSENSITIVE);
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
                addURLToDownload(new URL(image), String.format("%03d_", index));
                attempted.add(image);
            }
        }
        waitForThreads();
    }

}