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

public class NsfwAlbumRipper extends AbstractHTMLRipper {
    private static final String HOST = "nsfwalbum";
    private static final String DOMAIN = "nsfwalbum.com";

    public NsfwAlbumRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern pattern = Pattern.compile("(?!https:\\/\\/nsfwalbum.com\\/album\\/)\\d+");
        Matcher matcher = pattern.matcher(url.toExternalForm());

        if (matcher.find()) {
            return matcher.group();
        }

        throw new MalformedURLException(
                "Expected nsfwalbum.com URL format nsfwalbum.com/album/albumid - got " + url + " instead.");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<String>();

        Elements imgs = doc.select(".album img");

        System.out.println(imgs.size() + " elements (thumbnails) found.");

        for (Element img : imgs) {
            String thumbURL = img.attr("data-src");
            String fullResURL = null;

            if (thumbURL.contains("imgspice.com")) {
                fullResURL = thumbURL.replace("_t.jpg", ".jpg");
            } else if (thumbURL.contains("imagetwist.com")) {
                fullResURL = thumbURL.replace("/th/", "/i/");
            } else if (thumbURL.contains("pixhost.com")) {
                fullResURL = thumbURL.replace("https://t", "https://img");
                fullResURL = fullResURL.replace("/thumbs/", "/images/");
            } else if (thumbURL.contains("imx.to")) {
                fullResURL = thumbURL.replace("/t/", "/i/");
            }

            if (fullResURL != null)
                results.add(fullResURL);
        }

        return results;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
