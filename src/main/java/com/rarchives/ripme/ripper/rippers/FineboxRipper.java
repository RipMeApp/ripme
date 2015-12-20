package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import org.jsoup.select.Elements;

public class FineboxRipper extends AlbumRipper {

    private static final String DOMAIN = "finebox.co",
                                DOMAIN_OLD = "vinebox.co",
                                HOST = "finebox";

    public FineboxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN) || url.getHost().endsWith(DOMAIN_OLD);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL("http://"+DOMAIN+"/u/" + getGID(url));
    }

    @Override
    public void rip() throws IOException {
        int page = 0;
        Document doc;
        Boolean hasPagesLeft = true;
        while (hasPagesLeft) {
            page++;
            String urlPaged = this.url.toExternalForm() + "?page=" + page;
            logger.info("Retrieving " + urlPaged);
            sendUpdate(STATUS.LOADING_RESOURCE, urlPaged);
            try {
                doc = Http.url(this.url).get();
            } catch (HttpStatusException e) {
                logger.debug("Hit end of pages at page " + page, e);
                break;
            }
            Elements videos = doc.select("video");
            for (Element element : videos) {
                String videourl = element.select("source").attr("src");
                if (!videourl.startsWith("http")) {
                    videourl = "http://" + DOMAIN + videourl;
                }
                logger.info("URL to download: " + videourl);
                if (!addURLToDownload(new URL(videourl))) {
                   hasPagesLeft = false;
                   break;
                }
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
        Pattern p = Pattern.compile("^https?://(www\\.)?(v|f)inebox\\.co/u/([a-zA-Z0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: http://"+DOMAIN+"/u/USERNAME");
        }
        return m.group(m.groupCount());
    }

}
