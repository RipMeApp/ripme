package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;

public class MotherlessRipper extends AlbumRipper {

    private static final String DOMAIN = "motherless.com",
                                HOST   = "motherless";
    private static final Logger logger = Logger.getLogger(MotherlessRipper.class);

    private DownloadThreadPool motherlessThreadPool;

    public MotherlessRipper(URL url) throws IOException {
        super(url);
        motherlessThreadPool = new DownloadThreadPool();
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String gid = getGID(url);
        URL newURL = new URL("http://motherless.com/G" + gid);
        logger.debug("Sanitized URL from " + url + " to " + newURL);
        return newURL;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?motherless\\.com/G([MVI][A-F0-9]{6,8}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        System.err.println(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://motherless.com/GIXXXXXXX, got: " + url);
        }
        return m.group(m.groupCount());
    }

    @Override
    public void rip() throws IOException {
        int index = 0, page = 1;
        String nextURL = this.url.toExternalForm();
        while (nextURL != null) {
            logger.info("    Retrieving " + nextURL);
            Document doc = Jsoup.connect(nextURL)
                    .userAgent(USER_AGENT)
                    .get();
            for (Element thumb : doc.select("div.thumb a.img-container")) {
                URL url = new URL("http://" + DOMAIN + thumb.attr("href"));
                index += 1;
                // Create thread for finding image at "url" page
                MotherlessImageThread mit = new MotherlessImageThread(url, index);
                motherlessThreadPool.addThread(mit);
            }
            // Next page
            nextURL = null;
            page++;
            if (doc.html().contains("?page=" + page)) {
                nextURL = this.url.toExternalForm() + "?page=" + page;
            }
        }
        motherlessThreadPool.waitForThreads();
        waitForThreads();
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class MotherlessImageThread extends Thread {
        private URL url;
        private int index;

        public MotherlessImageThread(URL url, int index) {
            super();
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Document doc = Jsoup.connect(this.url.toExternalForm())
                                    .userAgent(USER_AGENT)
                                    .get();
                Pattern p = Pattern.compile("^.*__fileurl = '([^']{1,})';.*$", Pattern.DOTALL);
                Matcher m = p.matcher(doc.outerHtml());
                if (m.matches()) {
                    String file = m.group(1);
                    addURLToDownload(new URL(file), String.format("%03d_", index));
                } else {
                    logger.warn("[!] could not find '__fileurl' at " + url);
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }

}
