package com.rarchives.ripme.ripper.rippers;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.utils.Http;


public class GfycatRipper extends AbstractHTMLRipper {

    private static final String HOST = "gfycat.com";
    String username = "";
    String cursor = "";
    String count = "30";



    public GfycatRipper(URL url) throws IOException {
        super(new URL(url.toExternalForm().split("-")[0].replace("thumbs.", "")));
    }

    @Override
    public String getDomain() {
        return "gfycat.com";
    }

    @Override
    public String getHost() {
        return "gfycat";
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(HOST);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String sUrl = url.toExternalForm();
        sUrl = sUrl.replace("/gifs/detail", "");
        sUrl = sUrl.replace("/amp", "");
        return new URL(sUrl);
    }

    public boolean isProfile() {
        Pattern p = Pattern.compile("^https?://[wm.]*gfycat\\.com/@([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (!isProfile()) {
            return Http.url(url).get();
        } else {
            username = getGID(url);
            return Http.url(new URL("https://api.gfycat.com/v1/users/" +  username + "/gfycats")).ignoreContentType().get();
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(thumbs\\.|[wm\\.]*)gfycat\\.com/@?([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        
        if (m.matches())
            return m.group(2);
        
        throw new MalformedURLException(
                "Expected gfycat.com format: "
                        + "gfycat.com/id or "
                        + "thumbs.gfycat.com/id.gif"
                        + " Got: " + url);
    }

    private String stripHTMLTags(String t) {
        t = t.replaceAll("<html>\n" +
                " <head></head>\n" +
                " <body>", "");
        t = t.replaceAll("</body>\n" +
                "</html>", "");
        t = t.replaceAll("\n", "");
        t = t.replaceAll("=\"\"", "");
        return t;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (cursor.equals("")) {
            throw new IOException("No more pages");
        }
        return Http.url(new URL("https://api.gfycat.com/v1/users/" +  username + "/gfycats?count=" + count + "&cursor=" + cursor)).ignoreContentType().get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        if (isProfile()) {
            JSONObject page = new JSONObject(stripHTMLTags(doc.html()));
            JSONArray content = page.getJSONArray("gfycats");
            for (int i = 0; i < content.length(); i++) {
                result.add(content.getJSONObject(i).getString("mp4Url"));
            }
            cursor = page.getString("cursor");
        } else {
            Elements videos = doc.select("script");
            for (Element el : videos) {
                String json = el.html();
                if (json.startsWith("{")) {
                    JSONObject page = new JSONObject(json);
                    result.add(page.getJSONObject("video").getString("contentUrl"));
                }
            }
        }
        return result;
    }

    /**
     * Helper method for retrieving video URLs.
     * @param url URL to gfycat page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException {
        LOGGER.info("Retrieving " + url.toExternalForm());

        //Sanitize the URL first
        url = new URL(url.toExternalForm().replace("/gifs/detail", ""));

        Document doc = Http.url(url).get();
        Elements videos = doc.select("script");
        for (Element el : videos) {
            String json = el.html();
            if (json.startsWith("{")) {
                JSONObject page = new JSONObject(json);
                return page.getJSONObject("video").getString("contentUrl");
            }
        }
        throw new IOException();
    }
}
