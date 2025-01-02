package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class VidbleRipper extends AbstractHTMLRipper {

    public VidbleRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "vidble";
    }
    @Override
    public String getDomain() {
        return "vidble.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*vidble.com/album/([a-zA-Z0-9_\\-]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected vidble.com album format: "
                        + "vidble.com/album/####"
                        + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        return getURLsFromPageStatic(doc);
    }

    private static List<String> getURLsFromPageStatic(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        Elements els = doc.select("#ContentPlaceHolder1_divContent");
        Elements imgs = els.select("img");
        for (Element img : imgs) {
            String src = img.absUrl("src");
            src = src.replaceAll("_[a-zA-Z]{3,5}", "");

            if (!src.equals("")) {
                imageURLs.add(src);
            }
        }
        return imageURLs;
   }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    public static List<URL> getURLsFromPage(URL url) throws IOException, URISyntaxException {
        List<URL> urls = new ArrayList<>();
        Document doc = Http.url(url).get();
        for (String stringURL : getURLsFromPageStatic(doc)) {
            urls.add(new URI(stringURL).toURL());
        }
        return urls;
    }
}
