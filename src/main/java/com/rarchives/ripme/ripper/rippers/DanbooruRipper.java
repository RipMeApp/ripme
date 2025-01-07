package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DanbooruRipper extends AbstractJSONRipper {

    private static final Logger logger = LogManager.getLogger(DanbooruRipper.class);

    private static final String DOMAIN = "danbooru.donmai.us",
            HOST = "danbooru";
    private final OkHttpClient client;

    private Pattern gidPattern = null;

    private int currentPageNum = 1;

    public DanbooruRipper(URL url) throws IOException {
        super(url);
        this.client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    private String getPage(int num) throws MalformedURLException {
        return "https://" + getDomain() + "/posts.json?page=" + num + "&tags=" + getTag(url);
    }

    @Override
    protected JSONObject getFirstPage() throws MalformedURLException {
        return getCurrentPage();
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        return getCurrentPage();
    }

    @Nullable
    private JSONObject getCurrentPage() throws MalformedURLException {
        Request request = new Request.Builder()
                .url(getPage(currentPageNum))
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1")
                .header("Accept", "application/json,text/javascript,*/*;q=0.01")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Referer", "https://danbooru.donmai.us/")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Connection", "keep-alive")
                .build();
        Response response = null;
        currentPageNum++;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            JSONArray jsonArray = new JSONArray(responseData);
            if(!jsonArray.isEmpty()){
                String newCompatibleJSON = "{ \"resources\":" + jsonArray + " }";
                return new JSONObject(newCompatibleJSON);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(response !=null) {
                response.body().close();
            }
        }
        return null;
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> res = new ArrayList<>(100);
        JSONArray jsonArray = json.getJSONArray("resources");
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.getJSONObject(i).has("file_url")) {
                res.add(jsonArray.getJSONObject(i).getString("file_url"));
            }
        }
        return res;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        try {
            return Utils.filesystemSafe(new URI(getTag(url).replaceAll("([?&])tags=", "")).getPath());
        } catch (URISyntaxException ex) {
            logger.error(ex);
        }

        throw new MalformedURLException("Expected booru URL format: " + getDomain() + "/posts?tags=searchterm - got " + url + " instead");
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private String getTag(URL url) throws MalformedURLException {
        gidPattern = Pattern.compile("https?://danbooru.donmai.us/(posts)?.*([?&]tags=([^&]*)(?:&z=([0-9]+))?$)");
        Matcher m = gidPattern.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(3);
        }

        throw new MalformedURLException("Expected danbooru URL format: " + getDomain() + "/posts?tags=searchterm - got " + url + " instead");
    }
}
