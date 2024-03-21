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

public class YuvutuRipper extends AbstractHTMLRipper {
    
    private static final String DOMAIN = "yuvutu.com",
                                HOST   = "yuvutu";

    public YuvutuRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }
    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^http://www\\.yuvutu\\.com/modules\\.php\\?name=YuGallery&action=view&set_id=([0-9]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }
    
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^http://www\\.yuvutu\\.com/modules\\.php\\?name=YuGallery&action=view&set_id=([0-9]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected yuvutu.com URL format: " +
                        "yuvutu.com/modules.php?name=YuGallery&action=view&set_id=albumid - got " + url + "instead");
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div#galleria > a > img")) {
            String image = thumb.attr("src");
            imageURLs.add(image);
        }
        return imageURLs;
    }
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
