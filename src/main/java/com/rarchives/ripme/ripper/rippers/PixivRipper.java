package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PixivRipper extends AbstractJSONRipper {
    public static final String PIXIV_USERNAME = Utils.getConfigString("pixiv.username", null);
    public static final String PIXIV_PASSWORD = Utils.getConfigString("pixiv.password", null);

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
    private static String auth_time = Utils.getConfigString("pixiv.auth_time", Long.toString(3601L));
    private static String access_token = Utils.getConfigString("pixiv.access_token", null);
    private static String user_id = Utils.getConfigString("pixiv.user_id", null);

    // From https://github.com/upbit/pixivpy/blob/master/pixivpy3/api.py
    private static final String CLIENT_ID = "MOBrBDS8blbauoSck0ZfDbtuzpyT";
    private static final String CLIENT_SECRET = "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj";
    private static final String HASH_SECRET = "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c";
    private static String refresh_token = Utils.getConfigString("pixiv.refresh_token", null);

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
        try {
            auth();
        } catch (Exception e) {
            throw new MalformedURLException(e.getMessage());
        }
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
        jsonObj = httpClient.getJSON();

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
        }
        return new ArrayList<>();
    }

    private void auth() throws Exception {
        long session_time = Instant.now().getEpochSecond() - Long.parseLong(auth_time, 10);
        if ((access_token == null) || (user_id == null) || (refresh_token == null) || session_time >= 3600L) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u-MM-d'T'kk':'mm':'ss'+00:00'");
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
            String localtime = currentTime.format(formatter);
            URL auth_url = new URL("https://oauth.secure.pixiv.net/auth/token");

            HttpURLConnection httpClient = (HttpURLConnection) auth_url.openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("User-Agent", "PixivAndroidApp/5.0.64 (Android 6.0)");
            httpClient.setRequestProperty("X-Client-Time", localtime);
            httpClient.setRequestProperty("X-Client-Hash", hexdigest(new String((localtime + HASH_SECRET).getBytes(), StandardCharsets.UTF_8)));

            List<BasicNameValuePair> httpData = new ArrayList<>();
            httpData.add(new BasicNameValuePair("get_secure_url", Integer.toString(1)));
            httpData.add(new BasicNameValuePair("client_id", CLIENT_ID));
            httpData.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));

            if ((PIXIV_USERNAME != null) && (PIXIV_PASSWORD != null)) {
                httpData.add(new BasicNameValuePair("grant_type", "password"));
                httpData.add(new BasicNameValuePair("username", PIXIV_USERNAME));
                httpData.add(new BasicNameValuePair("password", PIXIV_PASSWORD));
            } else {
                throw new Exception("Pixiv username or password not provided.");
            }

            httpClient.setDoOutput(true);
            OutputStream os = httpClient.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

            writer.write(getQuery(httpData));
            writer.flush();
            writer.close();


            httpClient.connect();

            BufferedReader br;
            if (200 <= httpClient.getResponseCode() && httpClient.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader(httpClient.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(httpClient.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String jsonOutput;

            while ((jsonOutput = br.readLine()) != null) {
                sb.append(jsonOutput);
            }

            JSONObject jsonObj = new JSONObject(sb.toString());

            access_token = jsonObj.getJSONObject("response").getString("access_token");
            user_id = jsonObj.getJSONObject("response").getJSONObject("user").getString("id");
            refresh_token = jsonObj.getJSONObject("response").getString("refresh_token");

            // Need to reduce login api calls, so store the tokens.
            Utils.setConfigString("pixiv.auth_time", Long.toString(Instant.now().getEpochSecond()));
            Utils.setConfigString("pixiv.access_token", access_token);
            Utils.setConfigString("pixiv.user_id", user_id);
            Utils.setConfigString("pixiv.refresh_token", refresh_token);
            Utils.saveConfig();

        }
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

    public String hexdigest(String message) throws Exception {
        StringBuilder hd;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(message.getBytes());
        BigInteger hash = new BigInteger(1, md5.digest());
        hd = new StringBuilder(hash.toString(16)); // BigInteger strips leading 0's
        while (hd.length() < 32) {
            hd.insert(0, "0");
        } // pad with leading 0's
        return hd.toString();
    }

    private enum RIP_TYPE {
        USER,
        IMAGE
    }
}
