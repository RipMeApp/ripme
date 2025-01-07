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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HitomiRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(HitomiRipper.class);

    private String galleryId = "";

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
        Pattern p = Pattern.compile("https://hitomi.la/(cg|doujinshi|gamecg|manga)/(.+).html");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            galleryId = m.group(1);
            return m.group(1);
        }
        throw new MalformedURLException("Expected hitomi URL format: " +
                "https://hitomi.la/(cg|doujinshi|gamecg|manga)/ID.html - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        // if we go to /GALLERYID.js we get a nice json array of all images in the gallery
        return Http.url(new URI(url.toExternalForm().replaceAll("hitomi", "ltn.hitomi").replaceAll(".html", ".js")).toURL()).ignoreContentType().get();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String json = doc.text().replaceAll("var galleryinfo =", "");
        JSONArray json_data = new JSONArray(json);
        for (int i = 0; i < json_data.length(); i++) {
            result.add("https://ba.hitomi.la/galleries/" + galleryId + "/" + json_data.getJSONObject(i).getString("name"));
        }

        return result;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            // Attempt to use album title and username as GID
            Document doc = Http.url(url).get();
            return getHost() + "_" + getGID(url) + "_" +
                    doc.select("title").text().replaceAll(" - Read Online - hentai artistcg \\| Hitomi.la", "");
        } catch (IOException e) {
            logger.info("Falling back");
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
