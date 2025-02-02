package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class DynastyscansRipper extends AbstractHTMLRipper {

    public DynastyscansRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "dynasty-scans";
    }

    @Override
    public String getDomain() {
        return "dynasty-scans.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://dynasty-scans.com/chapters/([\\S]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected dynasty-scans URL format: " +
                "dynasty-scans.com/chapters/ID - got " + url + " instead");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Element elem = doc.select("a[id=next_link]").first();
        if (elem == null || elem.attr("href").equals("#")) {
            throw new IOException("No more pages");
        }
        return Http.url("https://dynasty-scans.com" + elem.attr("href")).get();

    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String jsonText = null;
        for (Element script : doc.select("script")) {
            if (script.data().contains("var pages")) {
                jsonText = script.data().replaceAll("var pages = ", "");
                jsonText = jsonText.replaceAll("//<!\\[CDATA\\[", "");
                jsonText = jsonText.replaceAll("//]]>", "");
            }
        }
        JSONArray imageArray = new JSONArray(jsonText);
        for (int i = 0; i < imageArray.length(); i++) {
            result.add("https://dynasty-scans.com" + imageArray.getJSONObject(i).getString("image"));
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
