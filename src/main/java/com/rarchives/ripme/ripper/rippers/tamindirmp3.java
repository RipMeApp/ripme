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

public class tamindirmp3 extends AbstractHTMLRipper {

    public tamindirmp3(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "tamindir";
    }
    @Override
    public String getDomain() {
        return "tamindir.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[server48.]*tamindir\\.com/files/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected tamindir.com URL format: " +
                        "tamindir.com/files/albumid - got " + url + "instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> music = new ArrayList<>();
        for (Element el : doc.select("mp3")) {
            music.add(el.attr("src"));
        }
        return music;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
