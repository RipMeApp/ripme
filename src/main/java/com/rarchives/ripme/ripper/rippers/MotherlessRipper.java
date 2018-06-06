package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class MotherlessRipper extends AlbumRipper {

    private static final String DOMAIN = "motherless.com",
                                HOST   = "motherless";

    private DownloadThreadPool motherlessThreadPool;

    public MotherlessRipper(URL url) throws IOException {
        super(url);
        motherlessThreadPool = new DownloadThreadPool();
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
        throw new MalformedURLException("Expected URL format: http://motherless.com/GIXXXXXXX, got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0, page = 1;
        String nextURL = this.url.toExternalForm();
        while (nextURL != null) {
            if (isStopped()) {
                break;
            }
            LOGGER.info("Retrieving " + nextURL);
            sendUpdate(STATUS.LOADING_RESOURCE, nextURL);
            Document doc = Http.url(nextURL)
                               .referrer("http://motherless.com")
                               .get();
            for (Element thumb : doc.select("div.thumb a.img-container")) {
                if (isStopped()) {
                    break;
                }
                String thumbURL = thumb.attr("href");
                if (thumbURL.contains("pornmd.com")) {
                    continue;
                }
                URL url;
                if (!thumbURL.startsWith("http")) {
                    url = new URL("http://" + DOMAIN + thumbURL);
                }
                else {
                    url = new URL(thumbURL);
                }
                index += 1;

                // Create thread for finding image at "url" page
                MotherlessImageThread mit = new MotherlessImageThread(url, index);
                motherlessThreadPool.addThread(mit);

                if (isThisATest()) {
                    break;
                }
            }
            if (isThisATest()) {
                break;
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

        MotherlessImageThread(URL url, int index) {
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
                    addURLToDownload(new URL(file), prefix);
                } else {
                    LOGGER.warn("[!] could not find '__fileurl' at " + url);
                }
            } catch (IOException e) {
                LOGGER.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }

}
