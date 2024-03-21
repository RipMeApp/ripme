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

public class FreeComicOnlineRipper extends AbstractHTMLRipper {

    public FreeComicOnlineRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "freecomiconline";
    }

    @Override
    public String getDomain() {
        return "freecomiconline.me";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://freecomiconline.me/comic/([a-zA-Z0-9_\\-]+)/([a-zA-Z0-9_\\-]+)/?$");
	Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("^https://freecomiconline.me/comic/([a-zA-Z0-9_\\-]+)/?$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected freecomiconline URL format: " +
                "freecomiconline.me/TITLE/CHAPTER - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextPage = doc.select("div.select-pagination a").get(1).attr("href");
	String nextUrl = "";
	Pattern p = Pattern.compile("https://freecomiconline.me/comic/([a-zA-Z0-9_\\-]+)/([a-zA-Z0-9_\\-]+)/?$");
        Matcher m = p.matcher(nextPage);
	if(m.matches()){ 
	    nextUrl = m.group(0);
	}
	if(nextUrl.equals("")) throw new IOException("No more pages");
	sleep(500);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select(".wp-manga-chapter-img")) {
            result.add(el.attr("src"));
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
