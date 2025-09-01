package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class MotherlessRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(MotherlessRipper.class);

    // All sleep times are in milliseconds
    private static final int IMAGE_SLEEP_TIME    = 1000;

    private static final String DOMAIN = "motherless.com",
                                HOST   = "motherless";

    public MotherlessRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        try {
            getGID(url);
        } catch (Exception e) {
            return false;
        }
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    protected Document getFirstPage() throws IOException {
        URL firstURL = this.url;
        String path = this.url.getPath();
        // Check if "All Uploads" (/GMxxxx), Image (/GIxxxx) or Video (/GVxxxx) gallery since there's no "next" after the homepage (/Gxxxx)
        Pattern p = Pattern.compile("[MIV]");
        Matcher m = p.matcher(String.valueOf(path.charAt(2)));
        boolean notHome = m.matches();
        // If it's the homepage go to the "All Uploads" gallery (/Gxxxxx -> /GMxxxxx)
        if (!notHome) {
            StringBuilder newPath = new StringBuilder(path);
            newPath.insert(2, "M");
            firstURL = URI.create("https://" + DOMAIN + newPath).toURL();
            logger.info("Changed URL to " + firstURL);
        }
        return Http.url(firstURL).referrer("https://motherless.com").get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException, URISyntaxException {

        Elements nextPageLink = doc.head().select("link[rel=next]");
        if (nextPageLink.isEmpty()) {
            throw new IOException("Last page reached");
        } else {
            String referrerLink = doc.head().select("link[rel=canonical]").first().attr("href");
            URL nextURL = this.url.toURI().resolve(nextPageLink.first().attr("href")).toURL();
            return Http.url(nextURL).referrer(referrerLink).get();
        }
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> pageURLs = new ArrayList<>();

        for (Element thumb : page.select("div.thumb-container a.img-container")) {
            if (isStopped()) {
                break;
            }
            String thumbURL = thumb.attr("href");
            if (thumbURL.contains("pornmd.com")) {
                continue;
            }

            String url;
            if (!thumbURL.startsWith("http")) {
                url = "https://" + DOMAIN + thumbURL;
            } else {
                url = thumbURL;
            }
            pageURLs.add(url);

            if (isThisATest()) {
                break;
            }
        }

        return pageURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        // Create thread for finding image at "url" page
        MotherlessImageRunnable mit = new MotherlessImageRunnable(url, index);
        getThreadPool().addThread(mit);
        try {
            Thread.sleep(IMAGE_SLEEP_TIME);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting to load next image", e);
        }
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?motherless\\.com/G([MVI]?[A-F0-9]{6,8}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(m.groupCount());
        }
        p = Pattern.compile("^https?://(www\\.)?motherless\\.com/term/(images/|videos/)([a-zA-Z0-9%]+)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(m.groupCount());
        }
        p = Pattern.compile("^https?://(www\\.)?motherless\\.com/g[iv]/([a-zA-Z0-9%\\-_]+)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(m.groupCount());
        }
        throw new MalformedURLException("Expected URL format: https://motherless.com/GIXXXXXXX, got: " + url);
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class MotherlessImageRunnable implements Runnable {
        private final URL url;
        private final int index;

        MotherlessImageRunnable(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                if (isStopped() && !isThisATest()) {
                    return;
                }
                String u = this.url.toExternalForm();
                Document doc = Http.url(u)
                                   .referrer(u)
                                   .get();
                Pattern p = Pattern.compile("^.*__fileurl = '([^']+)';.*$", Pattern.DOTALL);
                Matcher m = p.matcher(doc.outerHtml());
                if (m.matches()) {
                    String file = m.group(1);
                    String prefix = "";
                    if (Utils.getConfigBoolean("download.save_order", true)) {
                        prefix = String.format("%03d_", index);
                    }
                    addURLToDownload(new URI(file).toURL(), prefix);
                } else {
                    logger.warn("[!] could not find '__fileurl' at " + url);
                }
            } catch (IOException | URISyntaxException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }

}
