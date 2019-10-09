package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MastodonRipper extends AbstractHTMLRipper {
    private Map<String, String> itemIDs = Collections.synchronizedMap(new HashMap<String, String>());

    public MastodonRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "mastodon";
    }

    @Override
    public String getDomain() {
        return "mastodon.social";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(" + getDomain() + ")/@([a-zA-Z0-9_-]+)(/media/?)?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Return the text contained between () in the regex
            return m.group(1) + "@" + m.group(2);
        }
        throw new MalformedURLException(
                "Expected " + getDomain() + " URL format: " +
                getDomain() + "/@username - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        Pattern p = Pattern.compile("^/@[a-zA-Z0-9_-]+/media/?$");
        Matcher m = p.matcher(url.getPath());
        if (m.matches()) {
            return Http.url(url).get();
        }
        return Http.url(url.toExternalForm().replaceAll("/$", "") + "/media").get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Elements hrefs = doc.select(".h-entry + .entry > a.load-more.load-gap");
        if (hrefs.isEmpty()) {
            throw new IOException("No more pages");
        }
        String nextUrl = hrefs.last().attr("href");
        sleep(500);
        return Http.url(nextUrl).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<String>();
        for (Element el : doc.select("[data-component=\"MediaGallery\"]")) {
            String props = el.attr("data-props");
            JSONObject obj = new JSONObject(props);
            JSONArray arr = obj.getJSONArray("media");
            for (int i = 0; i < arr.length(); i++) {
                String url = arr.getJSONObject(i).getString("url");
                result.add(url);
                String id = arr.getJSONObject(i).getString("id");
                itemIDs.put(url, id);
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, itemIDs.get(url.toString()) + "_");
    }
}
