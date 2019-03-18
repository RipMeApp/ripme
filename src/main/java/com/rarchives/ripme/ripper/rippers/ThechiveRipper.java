package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ThechiveRipper extends AbstractHTMLRipper {
    private Pattern p1 = Pattern.compile("^https?://thechive.com/[0-9]*/[0-9]*/[0-9]*/([a-zA-Z0-9_\\-]*)/?$");
    private Pattern imagePattern = Pattern.compile("<img\\s(?:.|\\n)+?>");

    // i.thechive.com specific variables.
    private Pattern p2 = Pattern.compile("^https?://i.thechive.com/([0-9a-zA-Z_]+)");
    private String jsonUrl = "https://i.thechive.com/rest/uploads";
    private Map<String, String> cookies = new HashMap<>();
    private String nextSeed = "";
    private String username = "";

    public ThechiveRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        Matcher m1 = p1.matcher(url.toExternalForm());
        if (m1.matches()) {
            return "thechive";
        } else {
            return "i.thechive"; // for suitable album title.
        }
    }

    @Override
    public String getDomain() {
        return "thechive.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {

        Matcher m1 = p1.matcher(url.toExternalForm());
        if (m1.matches()) {
            return m1.group(1);
        }

        Matcher m2 = p2.matcher(url.toExternalForm());
        if (m2.matches()) {
            username = m2.group(1);
            return username;
        }

        throw new MalformedURLException("Expected thechive.com URL format: "
                + "thechive.com/YEAR/MONTH/DAY/POSTTITLE/ OR i.thechive.com/username, got " + url + " instead.");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result;
        Matcher matcher = p1.matcher(url.toExternalForm());

        if (matcher.matches()) {
            result = getUrlsFromThechive(doc);
        } else {
            result = getUrlsFromIDotThechive();
        }
        return result;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Matcher matcher = p1.matcher(url.toExternalForm());

        if (matcher.matches()) {
            // for pattern p1.
            return null;
        } else {
            if (nextSeed == null) {
                throw new IOException("No more pages.");
            }
        }

        // check if next json has elements.
        JSONArray imgList;
        try {
            Response response = Http.url(jsonUrl).data("seed", nextSeed).data("queryType", "by-username")
                    .data("username", username).ignoreContentType().cookies(cookies).response();
            cookies = response.cookies();
            JSONObject json = new JSONObject(response.body());
            imgList = json.getJSONArray("uploads");
        } catch (Exception e) {
            throw new IOException("Error fetching next page.", e);
        }

        if (imgList != null && imgList.length() > 0) {
            return new Document(url.toString()); // empty document.
        } else {
            return null;
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private List<String> getUrlsFromThechive(Document doc) {
        List<String> result = new ArrayList<>();
        Elements scripts = doc.getElementsByTag("script");

        for (Element script : scripts) {
            String data = script.data();

            if (!data.contains("CHIVE_GALLERY_ITEMS")) {
                continue;
            }

            /*
             * We add all the <img/> tags in a single StringBuilder and parse as HTML for
             * easy sorting of img/ gifs.
             */
            StringBuilder allImgTags = new StringBuilder();
            Matcher matcher = imagePattern.matcher(data);
            while (matcher.find()) {
                allImgTags.append(matcher.group(0).replaceAll("\\\\", ""));
            }

            // Now we parse and sort links.
            Document imgDoc = Jsoup.parse(allImgTags.toString());
            Elements imgs = imgDoc.getElementsByTag("img");
            for (Element img : imgs) {
                if (img.hasAttr("data-gifsrc")) {
                    // result.add(img.attr("data-gifsrc"));
                    result.add(img.attr("data-gifsrc"));
                } else {
                    // result.add(img.attr("src"));
                    result.add(img.attr("src"));
                }
            }
        }

        // strip all GET parameters from the links( such as quality).
        result.replaceAll(s -> s.substring(0, s.indexOf("?")));

        return result;
    }

    private List<String> getUrlsFromIDotThechive() {
        // check for pattern p2.
        List<String> result = new ArrayList<>();
        try {
            Response response = Http.url(jsonUrl).data("seed", nextSeed).data("queryType", "by-username")
                    .data("username", username).ignoreContentType().cookies(cookies).response();
            cookies = response.cookies();
            JSONObject json = new JSONObject(response.body());
            JSONArray imgList = json.getJSONArray("uploads");
            nextSeed = null; // if no more images, nextSeed stays null
            for (int i = 0; i < imgList.length(); i++) {
                JSONObject img = imgList.getJSONObject(i);
                if (img.getString("mediaType").equals("gif")) {
                    result.add("https:" + img.getString("mediaUrlOverlay"));
                } else {
                    result.add("https:" + img.getString("mediaGifFrameUrl"));
                }
                nextSeed = img.getString("activityId");
            }
        } catch (IOException e) {
            LOGGER.error("Unable to fetch JSON data for url: " + url);
        } catch (JSONException e) {
            LOGGER.error("JSON error while parsing data for url: " + url);
        }
        return result;
    }

}
