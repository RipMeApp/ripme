package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractRipper;

public class VineboxRipper extends AbstractRipper {

    private static final String DOMAIN = "vinebox.co",
                                HOST   = "vinebox";
    private static final Logger logger = Logger.getLogger(VineboxRipper.class);

    public VineboxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL("http://vinebox.co/u/" + getGID(url));
    }

    @Override
    public void rip() throws IOException {
        int page = 0;
        Document doc;
        while (true) {
            page++;
            String urlPaged = this.url.toExternalForm() + "?page=" + page;
            logger.info("    Retrieving " + urlPaged);
            try {
                doc = Jsoup.connect(urlPaged).get();
            } catch (HttpStatusException e) {
                logger.debug("Hit end of pages at page " + page, e);
                break;
            }
            for (Element element : doc.select("video")) {
                addURLToDownload(new URL(element.attr("src")));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next page", e);
                break;
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?vinebox\\.co/u/([a-zA-Z0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: http://vinebox.co/u/USERNAME");
        }
        return m.group(m.groupCount());
    }

}
