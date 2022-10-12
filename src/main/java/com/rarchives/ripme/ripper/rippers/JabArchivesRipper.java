package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JabArchivesRipper extends AbstractHTMLRipper {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private Map<String, String> itemPrefixes = Collections.synchronizedMap(new HashMap<String, String>());

    public JabArchivesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "jabarchives";
    }

    @Override
    public String getDomain() {
        return "jabarchives.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(?:www\\.)?jabarchives.com/main/view/([a-zA-Z0-9_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Return the text contained between () in the regex
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected javarchives.com URL format: " +
                "jabarchives.com/main/view/albumname - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Elements hrefs = doc.select("a[title=\"Next page\"]");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = "https://jabarchives.com" + hrefs.first().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    protected String getSlug(String input) {
        // Get a URL/file-safe version of a string
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<String>();
        for (Element el : doc.select("#contentMain img")) {
            String url = "https://jabarchives.com" + el.attr("src").replace("thumb", "large");
            result.add(url);

            String title = el.parent().attr("title");
            itemPrefixes.put(url, getSlug(title) + "_");
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, itemPrefixes.get(url.toString()));
    }
}
