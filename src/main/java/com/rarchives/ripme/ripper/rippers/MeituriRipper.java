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
import com.rarchives.ripme.utils.Http;

public class MeituriRipper extends AbstractHTMLRipper {
    public MeituriRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "tujigu";
    }

    @Override
    public String getDomain() {
        return "tujigu.com";
    }

    // To use in getting URLs
    String albumID = "";

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // without escape
        // ^https?://[w.]*tujigu\.com/a/([0-9]+)/([0-9]+\.html)*$
        // https://www.tujigu.com/a/14449/
        // also matches https://www.tujigu.com/a/14449/3.html etc.
        // group 1 is 14449
        Pattern p = Pattern.compile("^https?://[w.]*tujigu\\.com/a/([0-9]+)/([0-9]+\\.html)*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            albumID = m.group(1);
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected tujigu.com URL format: " + "tujigu.com/a/albumid/ - got " + url + "instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        // Get number of images from the page
        // Then generate links according to that
        int numOfImages = 1;
        Pattern p = Pattern.compile("^<p>图片数量： ([0-9]+)P</p>$");
        for (Element para : doc.select("div.tuji > p")) {
            // <p>图片数量： 55P</p>
            Matcher m = p.matcher(para.toString());
            if (m.matches()) {
                // 55
                numOfImages = Integer.parseInt(m.group(1));
            }
        }

        // Base URL: http://ii.hywly.com/a/1/albumid/imgnum.jpg
        String baseURL = "https://tjg.hywly.com/a/1/" + albumID + "/";

        // Loop through and add images to the URL list
        for (int i = 1; i <= numOfImages; i++) {
            imageURLs.add(baseURL + i + ".jpg");
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
