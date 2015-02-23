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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class NudeGalsRipper extends AbstractHTMLRipper {
    // Current HTML document
    private Document albumDoc = null;

    public NudeGalsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "Nude-Gals";
    }

    @Override
    public String getDomain() {
        return "nude-gals.com";
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            Document doc = getFirstPage();
            Elements elems = doc.select("#left_col > #grid_title > .right");

            String girl = elems.get(3).text();
            String magazine = elems.get(2).text();
            String title = elems.get(0).text();

            return getHost() + "_" + girl + "-" + magazine + "-" + title;
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

        p = Pattern.compile("^.*nude-gals\\.com\\/photoshoot\\.php\\?photoshoot_id=(\\d+)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected nude-gals.com gallery format: "
                        + "nude-gals.com/photoshoot.php?phtoshoot_id=####"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();

        Elements thumbs = doc.select("#grid_container .grid > .grid_box");
        for (Element thumb : thumbs) {
            String link = thumb.select("a").get(1).attr("href");
            String imgSrc = "http://nude-gals.com/" + link;
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