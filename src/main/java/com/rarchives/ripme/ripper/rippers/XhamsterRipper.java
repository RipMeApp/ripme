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

public class XhamsterRipper extends AbstractRipper {

    private static final String DOMAIN = "xhamster.com",
                                HOST   = "xhamster";
    private static final Logger logger = Logger.getLogger(XhamsterRipper.class);

    public XhamsterRipper(URL url) throws IOException {
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
        int index = 0;
        String nextURL = this.url.toExternalForm();
        while (nextURL != null) {
            logger.info("    Retrieving " + nextURL);
            Document doc = Jsoup.connect(nextURL).get();
            for (Element thumb : doc.select("table.iListing div.img img")) {
                if (!thumb.hasAttr("src")) {
                    continue;
                }
                String image = thumb.attr("src");
                image = image.replaceAll(
                        "http://p[0-9]*\\.",
                        "http://up.");
                image = image.replaceAll(
                        "_160\\.",
                        "_1000.");
                index += 1;
                addURLToDownload(new URL(image), String.format("%03d_", index));
            }
            nextURL = null;
            for (Element element : doc.select("a.last")) {
                nextURL = element.attr("href");
                break;
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([a-z0-9.]*?)xhamster\\.com/photos/gallery/([0-9]{1,})/.*\\.html");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        throw new MalformedURLException(
                "Expected xhamster.com gallery formats: "
                        + "xhamster.com/photos/gallery/#####/xxxxx..html"
                        + " Got: " + url);
    }

}
