package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DanbooruRipper extends AbstractJSONRipper {

    private JSONObject image_details;
    private String currentgid;

    public DanbooruRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "danbooru_donmai";
    }

    @Override
    public boolean canRip(URL url) {
        if (url.toExternalForm().startsWith("https://danbooru.donmai.us/posts/") || url.toExternalForm().startsWith("https://danbooru.donmai.us/post/show/")) {
            return true;
        }
        return false;

    }

    @Override
    public String getHost() {
        return "danbooru.donmai.us";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?:\\/\\/danbooru.donmai.us\\/posts/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            currentgid = m.group(1);
            return currentgid;
        }

        p = Pattern.compile("^https?:\\/\\/danbooru.donmai.us\\/post/show/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            currentgid = m.group(1);
            return currentgid;
        }

        throw new MalformedURLException("Expected danbooru.donmai.us/posts/123456 URL format.");
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        if (url.toExternalForm().startsWith("https://danbooru.donmai.us/post/show/")) {
            url = new URL("https://danbooru.donmai.us/posts/" + currentgid);
        }
        String plainJsonResponse = Jsoup.connect(url.toExternalForm() + ".json").ignoreContentType(true).execute().body();
        image_details = new JSONObject(plainJsonResponse);
        return image_details;
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> urls = new ArrayList<>();
        urls.add(image_details.getString("file_url"));

        return urls;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }
}
