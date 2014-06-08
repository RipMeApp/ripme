package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class ButttoucherRipper extends AlbumRipper {

    private static final String DOMAIN = "butttoucher.com",
                                HOST   = "butttoucher";

    private Document albumDoc = null;

    public ButttoucherRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }
    
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*butttoucher.com/users/([a-zA-Z0-9_\\-]{1,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected butttoucher.com gallery format: "
                        + "butttoucher.com/users/<username>"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url.toExternalForm());
        if (albumDoc == null) {
            albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        }
        int index = 0;
        for (Element thumb : albumDoc.select("div.image-gallery > a > img")) {
            if (!thumb.hasAttr("src")) {
                continue;
            }
            String smallImage = thumb.attr("src");
            String image = smallImage.replace("m.", ".");
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