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

        p = Pattern.compile("^https?://(\\w+\\.)?fitnakedgirls\\.com/photos/gallery/(.+)$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected fitnakedgirls.com gallery format: " + "fitnakedgirls.com/gallery/####" + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();

        Elements imgs = doc.select(".entry-inner img");
        for (Element img : imgs) {
            String imgSrc = img.attr("data-src");
            if (imgSrc.strip().isEmpty()) {
                imgSrc = img.attr("src");
                if (imgSrc.strip().isEmpty()) {
                    continue;
                }
            }
            imageURLs.add(imgSrc);
        }

        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
         // site is slow and goes down easily so don't overwhelm it
        sleep(1000);

        // Send referrer when downloading images
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }
}
