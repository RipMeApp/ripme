package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LusciousRipper extends AbstractHTMLRipper {
    private static String albumid;

    private static final Pattern p = Pattern.compile("^https?://(?:www\\.)?(?:members\\.||legacy\\.||old\\.)?luscious\\.net/albums/([-_.0-9a-zA-Z]+).*$");

    public LusciousRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException{
        String URLToReturn = url.toExternalForm();
        URLToReturn = URLToReturn.replaceAll("https?://(?:www\\.)?luscious\\.", "https://old.luscious.");
        URL san_url = new URI(URLToReturn).toURL();
        LOGGER.info("sanitized URL is " + san_url.toExternalForm());
        return san_url;
    }

    @Override
    public String getDomain() {
        return "luscious.net";
    }

    @Override
    public String getHost() {
        return "luscious";
    }

    @Override
    public List<String> getURLsFromPage(Document page) { // gets urls for all pages through the api
        List<String> urls = new ArrayList<>();
        int totalPages = 1;

        for (int i = 1; i <= totalPages; i++) {
            String APIStringWOVariables = "https://apicdn.luscious.net/graphql/nobatch/?operationName=PictureListInsideAlbum&query=%2520query%2520PictureListInsideAlbum%28%2524input%253A%2520PictureListInput%21%29%2520%257B%2520picture%2520%257B%2520list%28input%253A%2520%2524input%29%2520%257B%2520info%2520%257B%2520...FacetCollectionInfo%2520%257D%2520items%2520%257B%2520__typename%2520id%2520title%2520description%2520created%2520like_status%2520number_of_comments%2520number_of_favorites%2520moderation_status%2520width%2520height%2520resolution%2520aspect_ratio%2520url_to_original%2520url_to_video%2520is_animated%2520position%2520permissions%2520url%2520tags%2520%257B%2520category%2520text%2520url%2520%257D%2520thumbnails%2520%257B%2520width%2520height%2520size%2520url%2520%257D%2520%257D%2520%257D%2520%257D%2520%257D%2520fragment%2520FacetCollectionInfo%2520on%2520FacetCollectionInfo%2520%257B%2520page%2520has_next_page%2520has_previous_page%2520total_items%2520total_pages%2520items_per_page%2520url_complete%2520%257D%2520&variables=";
            Connection con = Http.url(APIStringWOVariables + encodeVariablesPartOfURL(i, albumid)).method(Connection.Method.GET).retries(5).connection();
            con.ignoreHttpErrors(true);
            con.ignoreContentType(true);
            con.userAgent("Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/119.0");
            Connection.Response res;
            try {
                res = con.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String body = res.body();

            JSONObject jsonObject = new JSONObject(body);

            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject picture = data.getJSONObject("picture");
            JSONObject list = picture.getJSONObject("list");
            JSONArray items = list.getJSONArray("items");
            JSONObject info = list.getJSONObject("info");
            totalPages = info.getInt("total_pages");

            for (int j = 0; j < items.length(); j++) {
                JSONObject item = items.getJSONObject(j);
                String urlToOriginal = item.getString("url_to_original");
                urls.add(urlToOriginal);
            }
        }

        return urls;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            albumid = m.group(1).split("_")[m.group(1).split("_").length - 1];
            return m.group(1);
        }
        throw new MalformedURLException("Expected luscious.net URL format: "
                + "luscious.net/albums/albumname \n members.luscious.net/albums/albumname  - got " + url + " instead.");
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }

    public static String encodeVariablesPartOfURL(int page, String albumId) {
        try {
            String json = "{\"input\":{\"filters\":[{\"name\":\"album_id\",\"value\":\"" + albumId + "\"}],\"display\":\"rating_all_time\",\"items_per_page\":50,\"page\":" + page + "}}";

            return URLEncoder.encode(json, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not encode variables");
        }
    }
}
