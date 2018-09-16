package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.utils.Http;

public class Tubex6Ripper extends AbstractSingleFileRipper {

    public Tubex6Ripper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "tubex6";
    }

    @Override
    public String getDomain() {
        return "tubex6.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^http://.*tubex6\\.com/(.*)/$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected tubex6.com URL format: " +
                "tubex6.com/NAME - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        result.add(doc.select("source[type=video/mp4]").attr("src"));
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}