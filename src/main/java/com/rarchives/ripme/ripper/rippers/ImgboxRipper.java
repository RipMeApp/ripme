package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class ImgboxRipper extends AlbumRipper {

    private static final String DOMAIN = "imgbox.com",
                                HOST   = "imgbox";
    
    public ImgboxRipper(URL url) throws IOException {
        super(url);
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
        logger.info("    Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm())
                .userAgent(USER_AGENT)
                .get();
        Elements images = doc.select("div.boxed-content > a > img");
        if (images.size() == 0) {
            logger.error("No images found at " + this.url);
            throw new IOException("No images found at " + this.url);
        }
        int index = 0;
        for (Element image : images) {
            index++;
            String imageUrl = image.attr("src").replace("s.imgbox.com", "i.imgbox.com");
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(new URL(imageUrl), prefix);
        }

        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*imgbox\\.com/g/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected imgbox.com URL format: " +
                        "imgbox.com/g/albumid - got " + url + "instead");
    }
}
