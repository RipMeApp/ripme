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

import com.rarchives.ripme.ripper.AbstractRipper;

public class ImgurRipper extends AbstractRipper {

    private static final String DOMAIN = "imgur.com",
                                HOST   = "imgur";
    private static final Logger logger = Logger.getLogger(ImgurRipper.class);
    
    static enum ALBUM_TYPE {
        ALBUM,
        USER,
        USER_ALBUM,
        SERIES_OF_IMAGES
    };
    private ALBUM_TYPE albumType;

    public ImgurRipper(URL url) throws IOException {
        super(url);
    }

    public void processURL(URL url, String prefix) {
       logger.debug("Found URL: " + url);
       addURLToDownload(url, prefix);
    }

    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
           return false;
        }
        try {
            getGID(url);
        } catch (Exception e) {
            // Can't get GID, can't rip it.
            return false;
        }
        return true;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.indexOf('#') >= 0) {
            u = u.substring(0,  u.indexOf('#'));
        }
        return new URL(u);
    }

    @Override
    public void rip() throws IOException {
        switch (albumType) {
        case ALBUM:
            this.url = new URL(this.url.toExternalForm() + "/noscript");
            // Fall-through
        case USER_ALBUM:
            ripAlbum(this.url);
            break;

        case SERIES_OF_IMAGES:
            // TODO Get all images
            break;

        case USER:
            // TODO Get all albums by user
            break;
        }
        threadPool.waitForThreads();
    }

    private void ripAlbum(URL url) throws IOException {
        int index = 0;
        logger.info("[ ] Retrieving " + url.toExternalForm());
        Document doc = Jsoup.connect(url.toExternalForm()).get();
        for (Element thumb : doc.select("div.image")) {
            String image;
            if (thumb.select("a.zoom").size() > 0) {
                // Clickably full-size
                image = "http:" + thumb.select("a").attr("href");
            } else if (thumb.select("img").size() > 0) {
                image = "http:" + thumb.select("img").attr("src");
            } else {
                // Unable to find image in this div
                logger.error("[!] Unable to find image in div: " + thumb.toString());
                continue;
            }
            index += 1;
            processURL(new URL(image), String.format("%03d_", index));
        }
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(m\\.)?imgur\\.com/a/([a-zA-Z0-9]{5,8}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur album
            albumType = ALBUM_TYPE.ALBUM;
            String gid = m.group(m.groupCount());
            this.url = new URL("http://imgur.com/a/" + gid);
            return gid;
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-])\\.imgur\\.com/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Root imgur account
            albumType = ALBUM_TYPE.USER;
            return m.group(m.groupCount());
        }
        p = Pattern.compile("^https?://([a-zA-Z0-9\\-])\\.imgur\\.com/([a-zA-Z0-9])?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Imgur account album
            albumType = ALBUM_TYPE.USER_ALBUM;
            return m.group();
        }
        p = Pattern.compile("^https?://(i\\.)?imgur\\.com/([a-zA-Z0-9,]{5,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Series of imgur images
            albumType = ALBUM_TYPE.SERIES_OF_IMAGES;
            return m.group();
        }
        throw new MalformedURLException("Unexpected URL format: " + url.toExternalForm());
    }

}
