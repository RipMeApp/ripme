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

public class FitnakedgirlsRipper extends AbstractHTMLRipper {

    public FitnakedgirlsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "fitnakedgirls";
    }

    @Override
    public String getDomain() {
        return "fitnakedgirls.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        p = Pattern.compile("^.*fitnakedgirls\\.com/gallery/(.+)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected fitnakedgirls.com gallery format: " + "fitnakedgirls.com/gallery/####" + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();

        Elements imgs = doc.select("div[class*=wp-tiles-tile-bg] > img");
        for (Element img : imgs) {
            String imgSrc = img.attr("src");
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