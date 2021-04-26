package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ThefappeningblogRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "thefappeningblog.com", HOST = "thefappeningblog";

    public ThefappeningblogRipper(URL url) throws IOException {
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

        try {
            Document doc = Jsoup.connect(url.toString())
                    .userAgent(AbstractRipper.USER_AGENT)
                    .get();
            String title = doc.title();
            title = title.replace(" | #TheFappening", "");
            return title;
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
    public List<String> getURLsFromPage(Document doc) {

        List<String> result = new ArrayList<>();

        for (Element el : doc.select("div.entry-content > p > a")) {
            String url =  el.attr("href");

            if(url.contains("thefappeningblog.com")) {
                result.add(url);
            }
        }
        return result;

    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
