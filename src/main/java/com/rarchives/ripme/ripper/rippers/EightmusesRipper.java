package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class EightmusesRipper extends AlbumRipper {

    private static final String DOMAIN = "8muses.com",
                                HOST   = "8muses";
    private static final Logger logger = Logger.getLogger(EightmusesRipper.class);

    public EightmusesRipper(URL url) throws IOException {
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
        Response resp = Jsoup.connect(this.url.toExternalForm())
                            .userAgent(USER_AGENT)
                            .execute();
        Document doc = resp.parse();
        int index = 0;
        for (Element thumb : doc.select("img")) {
            if (!thumb.hasAttr("data-cfsrc")) {
                continue;
            }
            String image = thumb.attr("data-cfsrc");
            if (image.contains("-cu_")) {
                image = image.replaceAll("-cu_[^.]+", "-me");
            }
            if (image.startsWith("//")) {
                image = "http:" + image;
            }
            index += 1;
            URL imageURL = new URL(image);
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(imageURL, prefix);
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?8muses\\.com/index/category/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.8muses.com/index/category/albumname, got: " + url);
        }
        return m.group(m.groupCount());
    }

}
