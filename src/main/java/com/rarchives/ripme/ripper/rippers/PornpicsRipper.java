package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PornpicsRipper extends AbstractHTMLRipper {

    public PornpicsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "pornpics";
    }

    @Override
    public String getDomain() {
        return "pornpics.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.pornpics.com/galleries/([a-zA-Z0-9_-]*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected pornpics URL format: " +
                "www.pornpics.com/galleries/ID - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

//    @Override
//    public Document getNextPage(Document doc) throws IOException {
//        // Find next page
//        String nextUrl = "";
//        // We use comic-nav-next to the find the next page
//        Element elem = doc.select("td > div.next > a").first();
//        if (elem == null) {
//            throw new IOException("No more pages");
//        }
//        String nextPage = elem.attr("href");
//        // Some times this returns a empty string
//        // This for stops that
//        if (nextPage == "") {
//            return null;
//        }
//        else {
//            return Http.url("http://cfake.com" + nextPage).get();
//        }
//    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("a.rel-link")) {
            result.add(el.attr("href"));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
