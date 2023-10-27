package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedgifsRipper extends AbstractHTMLRipper {

    private static final String HOST = "redgifs.com";
    private static final String HOST_2 = "gifdeliverynetwork.com";
    String username = "";
    String cursor = "";
    String count = "100";

    String searchText = "";
    int searchCount = 150;
    int searchStart = 0;

    public RedgifsRipper(URL url) throws IOException, URISyntaxException {
        super(new URI(url.toExternalForm().replace("thumbs.", "")).toURL());
    }

    @Override
    public String getDomain() { return "redgifs.com"; }

    @Override
    public String getHost() {
        return "redgifs";
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(HOST) || url.getHost().endsWith(HOST_2);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String sUrl = url.toExternalForm();
        sUrl = sUrl.replace("/gifs/detail", "");
        sUrl = sUrl.replace("/amp", "");
        sUrl = sUrl.replace("gifdeliverynetwork.com", "redgifs.com/watch");
        return new URI(sUrl).toURL();
    }

    public Matcher isProfile() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/users/([a-zA-Z0-9_.-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    public Matcher isSearch() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/gifs/browse/([a-zA-Z0-9_.-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    public Matcher isSingleton() {
        Pattern p = Pattern.compile("^https?://[wm.]*redgifs\\.com/watch/([a-zA-Z0-9_-]+).*$");
        return p.matcher(url.toExternalForm());
    }

    @Override
    public Document getFirstPage() throws IOException {
        try {
            if (!isProfile().matches() && !isSearch().matches()) {
                return Http.url(url).get();
            } else if (isSearch().matches()) {
                searchText = getGID(url).replace("-", " ");
                return Http.url(
                        new URI("https://api.redgifs.com/v1/gfycats/search?search_text=" + searchText + "&count=" + searchCount + "&start=" + searchStart * searchCount).toURL()).ignoreContentType().get();
            } else {
                username = getGID(url);
                return Http.url(new URI("https://api.redgifs.com/v1/users/" + username + "/gfycats?count=" + count).toURL())
                        .ignoreContentType().get();
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {

        Matcher m = isProfile();
        if (m.matches()) {
            return m.group(1);
        }
        m = isSearch();
        if (m.matches()) {
            return m.group(1);
        }
        m = isSingleton();
        if (m.matches()) {
            return m.group(1).split("-")[0];
        }
        throw new MalformedURLException(
                "Expected redgifs.com format: "
                        + "redgifs.com/id or "
                        + "thumbs.redgifs.com/id.gif"
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
    public Document getNextPage(Document doc) throws IOException, URISyntaxException {
        if (isSearch().matches()) {
            Document d = Http.url(
                    new URI("https://api.redgifs.com/v1/gfycats/search?search_text=" + searchText
                                    + "&count=" + searchCount + "&start=" + searchCount*++searchStart).toURL())
                       .ignoreContentType().get();
            return (hasURLs(d).isEmpty()) ? null : d;
        } else {
            if (cursor.equals("") || cursor.equals("null")) {
                return null;
            } else {
                Document d =  Http.url(new URI("https://api.redgifs.com/v1/users/" +  username + "/gfycats?count=" + count + "&cursor=" + cursor).toURL()).ignoreContentType().get();
                return (hasURLs(d).isEmpty()) ? null : d;
            }
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        if (isProfile().matches() || isSearch().matches()) {
            result = hasURLs(doc);
        } else {
            Elements videos = doc.select("script");
            for (Element el : videos) {
                String json = el.html();
                if (json.startsWith("{")) {
                    JSONObject page = new JSONObject(json);
                    result.add(page.getJSONObject("video").getString("contentUrl")
                            .replace("-mobile", ""));
                }
            }
        }
        return result;
    }

    /**
     * Helper method for retrieving URLs.
     * @param doc Document of the URL page to look through
     * @return List of URLs to download
     */
    public List<String> hasURLs(Document doc) {
        List<String> result = new ArrayList<>();
        JSONObject page = new JSONObject(stripHTMLTags(doc.html()));
        JSONArray content = page.getJSONArray("gfycats");
        for (int i = 0; i < content.length(); i++) {
            result.add(content.getJSONObject(i).getString("mp4Url"));
        }
        cursor = page.get("cursor").toString();
        return result;
    }

    /**
     * Helper method for retrieving video URLs.
     * @param url URL to gfycat page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException, URISyntaxException {
        LOGGER.info("Retrieving " + url.toExternalForm());

        //Sanitize the URL first
        url = new URI(url.toExternalForm().replace("/gifs/detail", "")).toURL();

        Document doc = Http.url(url).get();
        Elements videos = doc.select("script");
        for (Element el : videos) {
            String json = el.html();
            if (json.startsWith("{")) {
                JSONObject page = new JSONObject(json);
                String mobileUrl = page.getJSONObject("video").getString("contentUrl");
                return mobileUrl.replace("-mobile", "");
            }
        }
        throw new IOException();
    }
}
