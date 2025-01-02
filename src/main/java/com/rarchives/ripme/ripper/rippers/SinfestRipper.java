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

public class SinfestRipper extends AbstractHTMLRipper {

    public SinfestRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "sinfest";
    }

    @Override
    public String getDomain() {
        return "sinfest.net";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://sinfest.net/view.php\\?date=([0-9-]*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected sinfest URL format: " +
                "sinfest.net/view.php?date=XXXX-XX-XX/ - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("td.style5 > a > img").last();
        LOGGER.info(elem.parent().attr("href"));
        if (elem == null || elem.parent().attr("href").equals("view.php?date=")) {
            throw new IOException("No more pages");
        }
        String nextPage = elem.parent().attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage.equals("")) {
            return null;
        }
        else {
            return Http.url("http://sinfest.net/" + nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Element elem = doc.select("tbody > tr > td > img").last();
        result.add("http://sinfest.net/" + elem.attr("src"));
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
