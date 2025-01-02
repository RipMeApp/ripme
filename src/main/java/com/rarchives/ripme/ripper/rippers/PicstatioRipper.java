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

public class PicstatioRipper extends AbstractHTMLRipper {

    public PicstatioRipper(URL url) throws IOException {
        super(url);
    }

    private String getFullSizedImageFromURL(String fileName) {
        try {
            LOGGER.info("https://www.picstatio.com/wallpaper/" + fileName + "/download");
            return Http.url("https://www.picstatio.com/wallpaper/" + fileName + "/download").get().select("p.text-center > span > a").attr("href");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getHost() {
        return "picstatio";
    }

    @Override
    public String getDomain() {
        return "picstatio.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.picstatio.com/([a-zA-Z1-9_-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected picstatio URL format: " +
                "www.picstatio.com//ID - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("a.next_page") != null) {
            return Http.url("https://www.picstatio.com" + doc.select("a.next_page").attr("href")).get();
        }
        throw new IOException("No more pages");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element e : doc.select("img.img")) {
            String imageName = e.parent().attr("href");
            LOGGER.info(getFullSizedImageFromURL(imageName.split("/")[2]));
            result.add(getFullSizedImageFromURL(imageName.split("/")[2]));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}