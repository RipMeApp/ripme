package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.util.HashMap;

public class AerisdiesRipper extends AbstractHTMLRipper {

    private Document albumDoc = null;
    private Map<String,String> cookies = new HashMap<>();


    public AerisdiesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "aerisdies";
    }
    @Override
    public String getDomain() {
        return "aerisdies.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www.aerisdies.com/html/lb/[a-z]*_(\\d+)_\\d\\.html");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.aerisdies.com/html/lb/albumDIG, got: " + url);
        }
        return m.group(1);
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            String title = getFirstPage().select("div > div > span[id=albumname] > a").first().text();
            return getHost() + "_" + getGID(url) + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            Response resp = Http.url(url).response();
            cookies.putAll(resp.cookies());
            albumDoc = resp.parse();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Elements albumElements = page.select("div.imgbox > a > img");
            for (Element imageBox : albumElements) {
                String imageUrl = imageBox.attr("src");
                imageUrl = imageUrl.replaceAll("thumbnails", "images");
                imageUrl = imageUrl.replaceAll("../../", "");
                imageUrl = imageUrl.replaceAll("gif", "jpg");
                imageURLs.add("http://www.aerisdies.com/" + imageUrl);
            }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
    }

    @Override
    public String getPrefix(int index) {
        return String.format("%03d_", index);
    }
}
