package com.rarchives.ripme.ripper.rippers;

import com.google.common.base.Strings;
import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class PixivRipper extends AbstractJSONRipper {

    private static final String[] urlsRegex = {
            "^https?://www.pixiv.net(?:/en)?/artworks/.*$",
            "^https?://www.pixiv.net/member_illust.php\\?(?:mode=(?:small|medium|large)&)?illust_id=.*$",
            "^https?://www.pixiv.net/member.php\\?id=.*$",
            "^https?://www.pixiv.net(?:/en)?/users/.*$",
    };

    private static final List<Pattern> url_patterns = Arrays.asList(
            Pattern.compile("^https?://www.pixiv.net(?:/en)?/artworks/([0-9]+).*$"),
            Pattern.compile("^https?://www.pixiv.net/member_illust.php\\?(?:mode=(?:small|medium|large)&)?illust_id=([0-9]+).*$"),
            Pattern.compile("^https?://www.pixiv.net/member.php\\?id=([0-9]+).*$"),
            Pattern.compile("^https?://www.pixiv.net(?:/en)?/users/([0-9]+).*$"));

    private static String access_token = Utils.getConfigString("pixiv.access_token", null);
    private static String refresh_token = Utils.getConfigString("pixiv.refresh_token", null);

    // From https://github.com/upbit/pixivpy/blob/master/pixivpy3/api.py
    private static final String CLIENT_ID = "MOBrBDS8blbauoSck0ZfDbtuzpyT";
    private static final String CLIENT_SECRET = "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj";

    private static String api_endpoint = "https://app-api.pixiv.net/v1/";
    private RIP_TYPE ripType;
    private String current_id;
    private int current_offset;
    private Map<String, String> options = new HashMap<>();

    public PixivRipper(URL url) throws Exception {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        for (String s : urlsRegex) {
            if (url.toExternalForm().matches(s)) {
                if (url.toExternalForm().matches(urlsRegex[0] + "|" + urlsRegex[1])) {
                    ripType = RIP_TYPE.IMAGE;
                } else if (url.toExternalForm().matches(urlsRegex[3] + "|" + urlsRegex[2])) {
                    ripType = RIP_TYPE.USER;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasASAPRipping() {
        return true;
    }

    @Override
    protected String getDomain() {
        return "pixiv";
    }

    @Override
    public String getHost() {
        return "pixiv.net";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        for (Pattern url_pattern : url_patterns) {
            Matcher m = url_pattern.matcher(url.toExternalForm());
            if (m.matches()) {
                current_id = m.group(1);
                return current_id;
            }
        }
        throw new MalformedURLException("Invalid pixiv.net URL format.");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        if (url.toExternalForm().matches(urlsRegex[1])) {
            Pattern p = Pattern.compile("^https?://www.pixiv.net/member_illust.php\\?(?:mode=(?:small|medium|large)&)?illust_id=([0-9]+).*$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return new URL("https://www.pixiv.net/en/artworks/" + m.group(1));
            }
        }
        if (url.toExternalForm().matches(urlsRegex[2])) {
            Pattern p = Pattern.compile("^https?://www.pixiv.net/member.php\\?id=([0-9]+).*$");
            Matcher m = p.matcher(url.toExternalForm());
            if (m.matches()) {
                return new URL("https://www.pixiv.net/en/users/" + m.group(1));
            }
        }
        return url;
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        if (Strings.isNullOrEmpty(access_token) || Strings.isNullOrEmpty(refresh_token)) {
            throw new IOException("Pixiv access token or refresh token not specified.");
        }
        JSONObject jsonObj;
        Http httpClient;
        switch (ripType) {
            case IMAGE:
                httpClient = new Http(api_endpoint + "illust/detail");
                httpClient.data("illust_id", getGID(new URL(url.toExternalForm())));
                break;
            case USER:
                httpClient = new Http(api_endpoint + "user/illusts");
                httpClient.data("user_id", getGID(new URL(url.toExternalForm())));
                httpClient.data("type", "illust");
                break;
            default:
                throw new MalformedURLException("URL is not valid.");
        }

        httpClient.header("Authorization", "Bearer " + access_token);
        try {
            jsonObj = httpClient.getJSON();
        } catch (IOException e) {
            try {
                //attempt to obtain a new auth token.
                refreshToken(refresh_token);
            } catch (Exception e2) {
                throw new IOException("Unable to authenticate to pixiv.");
            }
            jsonObj = httpClient.getJSON();
        }


        return jsonObj;
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        if (ripType == RIP_TYPE.USER) {
            if (doc.isNull("next_url")) {
                throw new IOException("No more images");
            } else {
                Pattern p = Pattern.compile("offset=([0-9]+).*$");
                Matcher m = p.matcher(doc.getString("next_url"));
                if (m.find()) {
                    current_offset = Integer.parseInt(m.group(1));
                }
            }
            Http httpClient = new Http(api_endpoint + "user/illusts");
            httpClient.header("Authorization", "Bearer " + access_token);
            httpClient.data("user_id", getGID(new URL(url.toExternalForm())));
            httpClient.data("type", "illust");
            httpClient.data("offset", Integer.toString(current_offset));
            return httpClient.getJSON();
        }
        throw new IOException("No more images");
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        options.put("referrer", url.toExternalForm());
        String illust_url;
        JSONArray meta_pages;
        JSONObject illust;
        switch (ripType) {
            case IMAGE:
                illust = json.getJSONObject("illust");
                meta_pages = illust.getJSONArray("meta_pages");
                if (meta_pages.length() != 0) {
                    for (int i = 0; i < meta_pages.length(); i++) {
                        illust_url = meta_pages.getJSONObject(i).getJSONObject("image_urls").getString("original");
                        try {
                            addURLToDownload(new URL(illust_url), options);
                        } catch (MalformedURLException e) {
                            LOGGER.error("Invalid url.");
                        }
                    }
                } else {
                    illust_url = json.getJSONObject("illust").getJSONObject("meta_single_page").getString("original_image_url");
                    try {
                        addURLToDownload(new URL(illust_url), options);
                    } catch (MalformedURLException e) {
                        LOGGER.error("Invalid url.");
                    }
                }
                break;
            case USER:
                JSONArray illusts = json.getJSONArray("illusts");
                for (int i = 0; i < illusts.length(); i++) {
                    illust = illusts.getJSONObject(i);
                    meta_pages = illust.getJSONArray("meta_pages");
                    if (meta_pages.length() != 0) {
                        for (int x = 0; x < meta_pages.length(); x++) {
                            illust_url = meta_pages.getJSONObject(x).getJSONObject("image_urls").getString("original");
                            options.put("subdirectory", "illust_" + illust.getInt("id"));
                            try {
                                addURLToDownload(new URL(illust_url), options);
                            } catch (MalformedURLException e) {
                                LOGGER.error("Invalid url.");
                            }
                        }
                    } else {
                        options.remove("subdirectory");
                        illust_url = illust.getJSONObject("meta_single_page").getString("original_image_url");
                        try {
                            addURLToDownload(new URL(illust_url), options);
                        } catch (MalformedURLException e) {
                            LOGGER.error("Invalid url.");
                        }
                    }
                }
                break;
            default:
                break;
        }
        return new ArrayList<>();
    }

    private void refreshToken(String token) throws Exception {
        Http http = new Http("https://oauth.secure.pixiv.net/auth/token");
        http.data("client_id", CLIENT_ID);
        http.data("client_secret", CLIENT_SECRET);
        http.data("grant_type", "refresh_token");
        http.data("include_policy", "true");
        http.data("refresh_token", token);
        http.header("User-Agent", "PixivAndroidApp/5.0.234 (Android 11; Pixel 5)");
        JSONObject response = http.postJSON();
        access_token = response.getString("access_token");
        Utils.setConfigString("pixiv.access_token", response.getString("access_token"));
    }
    @Override
    protected void downloadURL(URL url, int index) {
    }

    private String getQuery(List<BasicNameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (BasicNameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private enum RIP_TYPE {
        USER,
        IMAGE
    }
}
