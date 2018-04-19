package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HitomiRipper extends AbstractHTMLRipper {

    String galleryId = "";

    public HitomiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hitomi";
    }

    @Override
    public String getDomain() {
        return "hitomi.la";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://hitomi.la/galleries/([\\d]+).html");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            galleryId = m.group(1);
            return m.group(1);
        }
        throw new MalformedURLException("Expected hitomi URL format: " +
                "https://hitomi.la/galleries/ID.html - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // if we go to /GALLERYID.js we get a nice json array of all images in the gallery
        return Http.url(new URL(url.toExternalForm().replaceAll(".html", ".js"))).ignoreContentType().get();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String json = doc.text().replaceAll("var galleryinfo =", "");
        logger.info(json);
        JSONArray json_data = new JSONArray(json);
        for (int i = 0; i < json_data.length(); i++) {
            result.add("https://0a.hitomi.la/galleries/" + galleryId + "/" + json_data.getJSONObject(i).getString("name"));
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
