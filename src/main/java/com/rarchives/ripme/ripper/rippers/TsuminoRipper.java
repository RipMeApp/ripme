package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class TsuminoRipper extends AbstractHTMLRipper {
    private Map<String,String> cookies = new HashMap<>();

    public TsuminoRipper(URL url) throws IOException {
        super(url);
    }

    private JSONArray getPageUrls() {
        String postURL = "http://www.tsumino.com/Read/Load";
        try {
            // This sessionId will expire and need to be replaced
            cookies.put("ASP.NET_SessionId","c4rbzccf0dvy3e0cloolmlkq");
            logger.info(cookies);
            Document doc = Jsoup.connect(postURL).data("q", getAlbumID()).userAgent(USER_AGENT).cookies(cookies).referrer("http://www.tsumino.com/Read/View/" + getAlbumID()).post();
            String jsonInfo = doc.html().replaceAll("<html>","").replaceAll("<head></head>", "").replaceAll("<body>", "").replaceAll("</body>", "")
                    .replaceAll("</html>", "").replaceAll("\n", "");
            logger.info(jsonInfo);
            JSONObject json = new JSONObject(jsonInfo);
            logger.info(json.getJSONArray("reader_page_urls"));
            return json.getJSONArray("reader_page_urls");
        } catch (IOException e) {
            logger.info(e);
            return null;
        }
    }

    @Override
    public String getHost() {
        return "tsumino";
    }

    @Override
    public String getDomain() {
        return "tsumino.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.tsumino.com/Book/Info/([0-9]+)/([a-zA-Z0-9_-]*)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        throw new MalformedURLException("Expected tsumino URL format: " +
                "tsumino.com/Book/Info/ID/TITLE - got " + url + " instead");
    }

    private String getAlbumID() {
        Pattern p = Pattern.compile("https?://www.tsumino.com/Book/Info/([0-9]+)/\\S*");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public Document getFirstPage() throws IOException {
        Connection.Response resp = Http.url(url).response();
        cookies.putAll(resp.cookies());
        // We need to perform a get on http://www.tsumino.com/Read/View/albumID/1 or else the
        //www.tsumino.com/Read/Load endpoint 404s
        resp = Http.url("http://www.tsumino.com/Book/Info/" + getAlbumID()).response();
        cookies.putAll(resp.cookies());
        return resp.parse();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        JSONArray imageIds = getPageUrls();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < imageIds.length(); i++) {
            result.add("http://www.tsumino.com/Image/Object?name=" + URLEncoder.encode(imageIds.getString(i)));
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(1000);
        addURLToDownload(url, getPrefix(index));
    }
}
