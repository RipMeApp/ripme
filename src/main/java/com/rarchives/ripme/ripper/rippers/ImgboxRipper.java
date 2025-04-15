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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class ImgboxRipper extends AbstractHTMLRipper {

    public ImgboxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imgbox";
    }
    @Override
    public String getDomain() {
        return "imgbox.com";
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

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div.boxed-content > a > img")) {
            String image = thumb.attr("src").replaceAll("thumbs", "images");
            image = image.replace("_b", "_o");
            image = image.replaceAll("\\d-s", "i");
            imageURLs.add(image);
        }
        return imageURLs;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
