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

public class PorncomixinfoRipper extends AbstractHTMLRipper {

    public PorncomixinfoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "porncomixinfo";
    }

    @Override
    public String getDomain() {
        return "porncomixinfo.net";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://porncomixinfo.net/chapter/([a-zA-Z1-9_-]*)/([a-zA-Z1-9_-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected porncomixinfo URL format: " +
                "porncomixinfo.net/chapter/CHAP/ID - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextUrl = "";
        // We use comic-nav-next to the find the next page
        Element elem = doc.select("a.next_page").first();
        if (elem == null) {
            throw new IOException("No more pages");
        }
        String nextPage = elem.attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage.equals("")) {
            return null;
        }
        else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("img.wp-manga-chapter-img")) { {
                String imageSource = el.attr("src");
                result.add(imageSource);
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
