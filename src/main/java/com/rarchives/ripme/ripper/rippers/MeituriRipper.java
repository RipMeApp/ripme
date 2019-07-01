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
        return "meituri";
    }

    @Override
    public String getDomain() {
        return "meituri.com";
    }

    // To use in getting URLs
    String albumID = "";

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // without escape
        // ^https?://[w.]*meituri\.com/a/([0-9]+)/$
        // https://www.meituri.com/a/14449/
        // group 1 is 14449
        Pattern p = Pattern.compile("^https?://[w.]*meituri\\.com/a/([0-9]+)/$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            albumID = m.group(1);
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected meituri.com URL format: " + "meituri.com/a/albumid/ - got " + url + "instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        // Get number of images from the page
        // Then generate links according to that
        String numOfImages = "";
        // A very ugly way of getting "图片数量： 55P" from paragraphs
        // 3rd p in div.tuji
        int n = 0;
        for (Element para : doc.select("div.tuji > p")) {
            // 图片数量： 55P
            if (n == 2) {
                numOfImages = para.toString();
            }
            n++;
        }
        // ["<p>图片数量：", "55P</p>"]
        String[] splitNumOfImages = numOfImages.split(" ");
        // "55P</p>" -> "55" -> 55
        int actualNumOfImages = Integer.parseInt(splitNumOfImages[1].replace("P</p>", ""));

        // Base URL: http://ii.hywly.com/a/1/albumid/imgnum.jpg
        String baseURL = "http://ii.hywly.com/a/1/" + albumID + "/";

        // Loop through and add images to the URL list
        for (int i = 1; i <= actualNumOfImages; i++) {
            imageURLs.add(baseURL + i + ".jpg");
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
