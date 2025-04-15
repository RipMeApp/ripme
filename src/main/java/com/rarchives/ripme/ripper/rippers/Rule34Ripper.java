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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class Rule34Ripper extends AbstractHTMLRipper {

    public Rule34Ripper(URL url) throws IOException {
        super(url);
    }

    private String apiUrl;
    private int pageNumber = 0;

    @Override
    public String getHost() {
        return "rule34";
    }

    @Override
    public String getDomain() {
        return "rule34.xxx";
    }

    @Override
    public boolean canRip(URL url){
        Pattern p = Pattern.compile("https?://rule34.xxx/index.php\\?page=post&s=list&tags=([\\S]+)");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://rule34.xxx/index.php\\?page=post&s=list&tags=([\\S]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected rule34.xxx URL format: " +
                "rule34.xxx/index.php?page=post&s=list&tags=TAG - got " + url + " instead");
    }

    public URL getAPIUrl() throws MalformedURLException, URISyntaxException {
        URL urlToReturn = new URI("https://rule34.xxx/index.php?page=dapi&s=post&q=index&limit=100&tags=" + getGID(url)).toURL();
        return urlToReturn;
    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        apiUrl = getAPIUrl().toExternalForm();
        // "url" is an instance field of the superclass
        return Http.url(getAPIUrl()).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.html().contains("Search error: API limited due to abuse")) {
            throw new IOException("No more pages");
        }
        pageNumber += 1;
        String nextPage = apiUrl + "&pid=" + pageNumber;
        return Http.url(nextPage).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("posts > post")) {
            String imageSource = el.select("post").attr("file_url");
            result.add(imageSource);

        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
