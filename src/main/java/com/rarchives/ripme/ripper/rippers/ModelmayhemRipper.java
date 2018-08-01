package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ModelmayhemRipper extends AbstractHTMLRipper {

    private Map<String,String> cookies = new HashMap<>();

    public ModelmayhemRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "modelmayhem";
    }

    @Override
    public String getDomain() {
        return "modelmayhem.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www\\.modelmayhem\\.com/portfolio/(\\d+)/viewall");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected modelmayhem URL format: " +
                "modelmayhem.com/portfolio/ID/viewall - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // Bypass NSFW filter
        cookies.put("worksafe", "0");
        // "url" is an instance field of the superclass
        return Http.url(url).cookies(cookies).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("tr.a_pics > td > div > a")) {
            String image_URL = el.select("img").attr("src").replaceAll("_m", "");
            if (image_URL.contains("http")) {
                result.add(image_URL);
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
