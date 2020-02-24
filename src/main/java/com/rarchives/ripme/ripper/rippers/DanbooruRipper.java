package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DanbooruRipper extends AbstractJSONRipper {

    private String current_id;
    private String current_tag;
    private int page_num = 2;

    public DanbooruRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "danbooru_donmai";
    }

    @Override
    public boolean canRip(URL url) {
        String[] urls_regex = {
                "^https?://danbooru.donmai.us/posts/.*$",
                "^https?://danbooru.donmai.us/post/show/.*$",
                "^https?://danbooru.donmai.us/posts\\?tags=.*$"
        };

        for (String s : urls_regex) {
            if (url.toExternalForm().matches(s)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public String getHost() {
        return "danbooru.donmai.us";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        if (url.toExternalForm().matches("^https?://danbooru.donmai.us/posts\\?tags=.*$")) {
            Pattern p = Pattern.compile("^https?://danbooru.donmai.us/posts\\?tags=(.*)");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                current_tag = m.group(1);
                return current_tag;
            }
        } else {
            final List<Pattern> url_patterns = new ArrayList<>();
            url_patterns.add(Pattern.compile("^https?://danbooru.donmai.us/posts/([0-9]+).*$"));
            url_patterns.add(Pattern.compile("^https?://danbooru.donmai.us/post/show/([0-9]+).*$"));
            for (Pattern url_pattern: url_patterns) {
                Matcher m = url_pattern.matcher(url.toExternalForm());
                if (m.matches()) {
                    current_id = m.group(1);
                    return current_id;
                }
            }
        }

        throw new MalformedURLException("Expected danbooru.donmai.us/posts/123456 URL format.");
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        if (url.toExternalForm().matches("^https?://danbooru.donmai.us/posts\\?tags=.*$")) {
            Http httpClient = new Http("https://danbooru.donmai.us/posts.json?tags=" + current_tag);
            httpClient.ignoreContentType();
            String r_body = httpClient.get().body().text();
            String json_new = "{ posts:" + r_body + "}";
            return new JSONObject(json_new);
        } else {
            if (url.toExternalForm().matches("^https?://danbooru.donmai.us/post/show/([0-9]+).*$")) {
                url = new URL("https://danbooru.donmai.us/posts/" + current_id);
            }
            Http httpClient = new Http(url.toExternalForm() + ".json");
            return httpClient.getJSON();
        }
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        if (url.toExternalForm().matches("^https?://danbooru.donmai.us/posts\\?tags=.*$")) {
            JSONArray jsonArray = doc.getJSONArray("posts");
            Http httpClient = new Http("https://danbooru.donmai.us/posts.json?page=" + Integer.toString(page_num) + "&tags=" + current_tag);
            httpClient.ignoreContentType();
            String r_body = httpClient.get().body().text();
            String json_new = "{ posts:" + r_body + "}";
            JSONObject json_new_obj = new JSONObject(json_new);
            if (json_new_obj.getJSONArray("posts").length() == 0) {
                throw new IOException("No more images");
            }
            page_num++;
            return json_new_obj;
        } else {
            throw new IOException("No more images.");
        }
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> urls = new ArrayList<>();
        if (url.toExternalForm().matches("^https?://danbooru.donmai.us/posts\\?tags=.*$")) {
            JSONArray jsonArray = json.getJSONArray("posts");
            for (int i = 0; i < jsonArray.length(); i++) {
                if (!jsonArray.getJSONObject(i).getBoolean("is_banned")) {
                    urls.add(jsonArray.getJSONObject(i).getString("file_url"));
                }
            }
            return urls;
        } else {
            urls.add(json.getString("file_url"));
            return urls;
        }

    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }
}
