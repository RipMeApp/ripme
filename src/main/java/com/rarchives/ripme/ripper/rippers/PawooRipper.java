package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class PawooRipper extends MastodonRipper {
    public PawooRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "pawoo";
    }

    @Override
    public String getDomain() {
        return "pawoo.net";
    }


    @Override
    // Pawoo uses a custom theme that has different navigation links
    public Document getNextPage(Document doc) throws IOException {
        Elements hrefs = doc.select(".pagination a[rel=\"next\"]");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = hrefs.last().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }
}
