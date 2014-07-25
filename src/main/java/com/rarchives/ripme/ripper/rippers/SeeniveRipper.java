package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class SeeniveRipper extends AlbumRipper {

    private static final String DOMAIN = "seenive.com",
                                HOST   = "seenive";

    private DownloadThreadPool seeniveThreadPool;

    public SeeniveRipper(URL url) throws IOException {
        super(url);
        seeniveThreadPool = new DownloadThreadPool();
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        String baseURL = this.url.toExternalForm();
        logger.info("    Retrieving " + baseURL);
        Document doc = Http.url(baseURL)
                           .referrer(baseURL)
                           .get();
        while (true) {
            if (isStopped()) {
                break;
            }
            String lastID = null;
            for (Element element : doc.select("a.facebox")) {
                if (isStopped()) {
                    break;
                }
                String card = element.attr("href"); // "/v/<video_id>"
                URL videoURL = new URL("https://seenive.com" + card);
                SeeniveImageThread vit = new SeeniveImageThread(videoURL);
                seeniveThreadPool.addThread(vit);
                lastID = card.substring(card.lastIndexOf('/') + 1);
            }
            if (lastID == null) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next page", e);
                break;
            }

            logger.info("[ ] Retrieving " + baseURL + "/next/" + lastID);
            JSONObject json = Http.url(baseURL + "/next/" + lastID)
                                    .referrer(baseURL)
                                    .getJSON();
            String html = json.getString("Html");
            if (html.equals("")) {
                break;
            }
            doc = Jsoup.parse(html);
        }
        seeniveThreadPool.waitForThreads();
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?seenive\\.com/u/([a-zA-Z0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: https://seenive.com/u/USERID");
        }
        return m.group(m.groupCount());
    }
    
    /**
     * Helper class to find and download images found on "image" pages
     */
    private class SeeniveImageThread extends Thread {
        private URL url;

        public SeeniveImageThread(URL url) {
            super();
            this.url = url;
        }

        @Override
        public void run() {
            try {
                Document doc = Http.url(this.url).get();
                logger.info("[ ] Retreiving video page " + this.url);
                sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
                for (Element element : doc.select("source")) {
                    String video = element.attr("src");
                    if (video.contains("redirect?url=")) {
                        video = video.substring("redirect?url=".length() + 1);
                        video = URLDecoder.decode(video, "UTF-8");
                    }
                    synchronized (threadPool) {
                        addURLToDownload(new URL(video));
                    }
                    break;
                }
            } catch (IOException e) {
                logger.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }
}
