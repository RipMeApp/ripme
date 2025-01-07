package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class GirlsOfDesireRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(EromeRipper.class);

    public GirlsOfDesireRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "GirlsOfDesire";
    }
    @Override
    public String getDomain() {
        return "girlsofdesire.org";
    }

    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title as GID
            Document doc = getCachedFirstPage();
            Elements elems = doc.select(".albumName");
            return getHost() + "_" + elems.first().text();
        } catch (Exception e) {
            // Fall back to default album naming convention
            logger.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^www\\.girlsofdesire\\.org/galleries/([\\w\\d-]+)/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected girlsofdesire.org gallery format: "
                        + "http://www.girlsofdesire.org/galleries/<name>/"
                        + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("td.vtop > a > img")) {
            String imgSrc = thumb.attr("src");
            imgSrc = imgSrc.replaceAll("_thumb\\.", ".");
            if (imgSrc.startsWith("/")) {
                imgSrc = "http://www.girlsofdesire.org" + imgSrc;
            }
            imageURLs.add(imgSrc);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer when downloading images
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
