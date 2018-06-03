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
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class NfsfwRipper extends AlbumRipper {

    private static final String DOMAIN = "nfsfw.com",
                                HOST   = "nfsfw";

    private Document albumDoc = null;

    private DownloadThreadPool nfsfwThreadPool;

    public NfsfwRipper(URL url) throws IOException {
        super(url);
        nfsfwThreadPool = new DownloadThreadPool("NFSFW");
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
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                albumDoc = Http.url(url).get();
            }
            String title = albumDoc.select("h2").first().text().trim();
            return "nfsfw_" + Utils.filesystemSafe(title);
        } catch (Exception e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("https?://[wm.]*nfsfw.com/gallery/v/([a-zA-Z0-9\\-_]+).*");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected nfsfw.com gallery format: "
                        + "nfsfw.com/v/albumname"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        List<Pair> subAlbums = new ArrayList<>();
        int index = 0;
        subAlbums.add(new Pair(this.url.toExternalForm(), ""));
        while (!subAlbums.isEmpty()) {
            if (isStopped()) {
                break;
            }
            Pair nextAlbum = subAlbums.remove(0);
            String nextURL = nextAlbum.first;
            String nextSubalbum = nextAlbum.second;
            sendUpdate(STATUS.LOADING_RESOURCE, nextURL);
            LOGGER.info("    Retrieving " + nextURL);
            if (albumDoc == null) {
                albumDoc = Http.url(nextURL).get();
            }
            // Subalbums
            for (Element suba : albumDoc.select("td.IMG > a")) {
                if (isStopped() || isThisATest()) {
                    break;
                }
                String subURL = "http://nfsfw.com" + suba.attr("href");
                String subdir = subURL;
                while (subdir.endsWith("/")) {
                    subdir = subdir.substring(0, subdir.length() - 1);
                }
                subdir = subdir.substring(subdir.lastIndexOf("/") + 1);
                subAlbums.add(new Pair(subURL, subdir));
            }
            // Images
            for (Element thumb : albumDoc.select("td.giItemCell > div > a")) {
                if (isStopped()) {
                    break;
                }
                String imagePage = "http://nfsfw.com" + thumb.attr("href");
                try {
                    NfsfwImageThread t = new NfsfwImageThread(new URL(imagePage), nextSubalbum, ++index);
                    nfsfwThreadPool.addThread(t);
                    if (isThisATest()) {
                        break;
                    }
                } catch (MalformedURLException mue) {
                    LOGGER.warn("Invalid URL: " + imagePage);
                }
            }
            if (isThisATest()) {
                break;
            }
            // Get next page
            for (Element a : albumDoc.select("a.next")) {
                subAlbums.add(0, new Pair("http://nfsfw.com" + a.attr("href"), ""));
                break;
            }
            // Insert next page at the top
            albumDoc = null;
            // Wait
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting to load next page", e);
                throw new IOException(e);
            }
        }
        nfsfwThreadPool.waitForThreads();
        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    /**
     * Helper class to find and download images found on "image" pages
     */
    private class NfsfwImageThread extends Thread {
        private URL url;
        private String subdir;
        private int index;

        NfsfwImageThread(URL url, String subdir, int index) {
            super();
            this.url = url;
            this.subdir = subdir;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Document doc = Http.url(this.url)
                                   .referrer(this.url)
                                   .get();
                Elements images = doc.select(".gbBlock img");
                if (images.isEmpty()) {
                    LOGGER.error("Failed to find image at " + this.url);
                    return;
                }
                String file = images.first().attr("src");
                if (file.startsWith("/")) {
                    file = "http://nfsfw.com" + file;
                }
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(file), prefix, this.subdir);
            } catch (IOException e) {
                LOGGER.error("[!] Exception while loading/parsing " + this.url, e);
            }
        }
    }

    private class Pair {
        String first;
        String second;
        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }
}