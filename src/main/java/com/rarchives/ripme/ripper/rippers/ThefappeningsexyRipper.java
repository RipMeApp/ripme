package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ThefappeningsexyRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "thefappening.sexy", HOST = "thefappening";

    public ThefappeningsexyRipper(URL url) throws IOException {
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
    public String getGID(URL url) throws MalformedURLException{

        try {
            Document doc = Jsoup.connect(url.toString())
                    .userAgent(AbstractRipper.USER_AGENT)
                    .get();
            String title = doc.title();
            return title.replace(" | The Fappening: Back At It Again!", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "missed";
        }
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Elements nextPageLink = doc.select("div.navigationBar a[rel=next]");
        if (nextPageLink.isEmpty()){
            throw new IOException("No more pages");
        } else {
            URL nextURL = new URL(this.url, nextPageLink.first().attr("href"));
            return Http.url(nextURL).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {

        List<String> result = new ArrayList<>();

        for (Element el : doc.select("img.thumbnail")) {
            String url =  el.attr("src");
            String correctedURL = url.replace("_data/i/", "").replace("-th","");
            result.add("https://thefappening.sexy/albums/" + correctedURL);
        }
        return result;

    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}   
