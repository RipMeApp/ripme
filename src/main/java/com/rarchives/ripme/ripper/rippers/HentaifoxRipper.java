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

public class HentaifoxRipper extends AbstractHTMLRipper {

    public HentaifoxRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hentaifox";
    }

    @Override
    public String getDomain() {
        return "hentaifox.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://hentaifox.com/gallery/([\\d]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected hentaifox URL format: " +
                "https://hentaifox.com/gallery/ID - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        LOGGER.info(doc);
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("div.preview_thumb > a > img")) {
                String imageSource = "https:" + el.attr("data-src").replaceAll("t\\.jpg", ".jpg");
                result.add(imageSource);
            }
        return result;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            Document doc = getFirstPage();
            String title = doc.select("div.info > h1").first().text();
            return getHost() + "_" + title + "_" + getGID(url);
        } catch (Exception e) {
            // Fall back to default album naming convention
            LOGGER.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
