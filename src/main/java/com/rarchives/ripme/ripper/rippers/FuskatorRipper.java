package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class FuskatorRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(FuskatorRipper.class);

    private String jsonurl = "https://fuskator.com/ajax/gal.aspx";
    private String xAuthUrl = "https://fuskator.com/ajax/auth.aspx";
    private String xAuthToken;
    private Map<String, String> cookies;

    public FuskatorRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "fuskator";
    }

    @Override
    public String getDomain() {
        return "fuskator.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String u = url.toExternalForm();
        if (u.contains("/thumbs/")) {
            u = u.replace("/thumbs/", "/full/");
        }
        if (u.contains("/expanded/")) {
            u = u.replaceAll("/expanded/", "/full/");
        }
        return new URI(u).toURL();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*fuskator.com/full/([a-zA-Z0-9\\-~]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected fuskator.com gallery formats: " + "fuskator.com/full/id/..." + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        // return Http.url(url).get();
        Response res = Http.url(url).response();
        cookies = res.cookies();
        return res.parse();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        JSONObject json;

        try {
            getXAuthToken();
            if (xAuthToken == null || xAuthToken.isEmpty()) {
                throw new IOException("No xAuthToken found.");
            }

            // All good. Fetch JSON data from jsonUrl.
            json = Http.url(jsonurl).cookies(cookies).data("X-Auth", xAuthToken).data("hash", getGID(url))
                    .data("_", Long.toString(System.currentTimeMillis())).getJSON();
        } catch (IOException e) {
            logger.error("Couldnt fetch images.", e.getCause());
            return imageURLs;
        }

        JSONArray imageArray = json.getJSONArray("images");
        for (int i = 0; i < imageArray.length(); i++) {
            imageURLs.add("https:" + imageArray.getJSONObject(i).getString("imageUrl"));
        }

        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private void getXAuthToken() throws IOException {
        if (cookies == null || cookies.isEmpty()) {
            throw new IOException("Null cookies or no cookies found.");
        }
        Response res = Http.url(xAuthUrl).cookies(cookies).method(Method.POST).response();
        xAuthToken = res.body();
    }
}
