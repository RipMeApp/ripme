package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class DirectlinksRipper extends AbstractHTMLRipper {

    public DirectlinksRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        String host = url.toExternalForm().split("/")[2];
        return host;
    }

    @Override
    public String getDomain() {
        String host = url.toExternalForm().split("/")[2];
        return host;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern pat = Pattern.compile("https?://[www\\.]?\\S+\\.(mp4|gif|png|jpg|jpeg|webm)/?$");
        Matcher mat = pat.matcher(url.toExternalForm());

        return mat.matches();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        return url.toExternalForm();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<String>();
            result.add(url.toExternalForm());
            return result;
        }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).ignoreContentType().get();
    }
}
