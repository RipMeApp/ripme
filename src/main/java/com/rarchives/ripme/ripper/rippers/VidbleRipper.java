package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class VidbleRipper extends AlbumRipper {

    private static final String DOMAIN = "vidble.com",
                                HOST   = "vidble";
    private static final Logger logger = Logger.getLogger(VidbleRipper.class);

    private Document albumDoc = null;

    public VidbleRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }
    
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*vidble.com/album/([a-zA-Z0-9_\\-]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected vidble.com album format: "
                        + "vidble.com/album/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url.toExternalForm());
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        if (albumDoc == null) {
            albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        }
        Elements els = albumDoc.select("#ContentPlaceHolder1_thumbs");
        if (els.size() == 0) {
            throw new IOException("No thumbnails found at " + this.url);
        }
        int index = 0;
        String thumbs = els.get(0).attr("value");
        for (String thumb : thumbs.split(",")) {
            if (thumb.trim().equals("")) {
                continue;
            }
            thumb = thumb.replaceAll("_[a-zA-Z]{3,5}", "");
            String image = "http://vidble.com/" + thumb;
            index += 1;
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(new URL(image), prefix);
        }
        waitForThreads();
    }

    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        return true;
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

}