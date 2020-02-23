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
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PixivRipper extends AbstractJSONRipper {

    public static final String PIXIV_USERNAME = Utils.getConfigString("pixiv.username", null);
    public static final String PIXIV_PASSWORD = Utils.getConfigString("pixiv.password", null);

    // From https://github.com/upbit/pixivpy/blob/master/pixivpy3/api.py
    private static final String CLIENT_ID = "MOBrBDS8blbauoSck0ZfDbtuzpyT";
    private static final String CLIENT_SECRET = "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj";
    private static final String HASH_SECRET = "28c1fdd170a5204386cb1313c7077b34f83e4aaf4aa829ce78c231e05b0bae2c";

    private String auth_time = Utils.getConfigString("pixiv.auth_time", Long.toString(3601L));
    private String access_token = Utils.getConfigString("pixiv.access_token", null);
    private String user_id = Utils.getConfigString("pixiv.user_id", null);
    private String refresh_token = Utils.getConfigString("pixiv.refresh_token", null);

    private String api_endpoint = "https://app-api.pixiv.net/v1/";

    private String current_id;
    private int current_offset = 0;

    private Map<String, String> options = new HashMap<String, String>();

    public PixivRipper(URL url) throws IOException {
        super(url);
        try {
            auth();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
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
    public boolean canRip(URL url) {
        String[] urls = {
                "https://www.pixiv.net/en/artworks/",
                "http://www.pixiv.net/en/artworks/",
                "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=",
                "http://www.pixiv.net/member_illust.php?mode=medium&illust_id=",
                "https://www.pixiv.net/member.php?id=",
                "http://www.pixiv.net/member.php?id=",
                "https://www.pixiv.net/en/users/",
                "http://www.pixiv.net/en/users/"
        };

        for (String s : urls) {
            if (url.toExternalForm().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        final List<Pattern> url_patterns = new ArrayList<>();
        url_patterns.add(Pattern.compile("^https?://www.pixiv.net/en/artworks/([0-9]+).*$"));
        url_patterns.add(Pattern.compile("^https?://www.pixiv.net/member_illust.php\\?mode=medium&illust_id=([0-9]+).*$"));
        url_patterns.add(Pattern.compile("^https?://www.pixiv.net/member.php\\?id=([0-9]+).*$"));
        url_patterns.add(Pattern.compile("^https?://www.pixiv.net/en/users/([0-9]+).*$"));

        for (Pattern url_pattern: url_patterns) {
            Matcher m = url_pattern.matcher(url.toExternalForm());
            if (m.matches()) {
                current_id = m.group(1);
                return current_id;
            }
        }
        throw new MalformedURLException("Expected pixiv.net/en/artworks/123456 URL format");
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        JSONObject jsonObj = null;
        Http httpClient = new Http(api_endpoint);

        if (url.toExternalForm().matches("^https?://www.pixiv.net/en/artworks/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member_illust.php\\?mode=medium&illust_id=.*$")) {

            if (url.toExternalForm().matches("^https?://www.pixiv.net/member_illust.php\\?mode=medium&illust_id=.*$")) {
                url = new URL("https://www.pixiv.net/en/artworks/" + current_id);
            }
            httpClient = new Http(api_endpoint + "illust/detail");
            httpClient.data("illust_id", getGID(new URL(url.toExternalForm())));
        } else if (url.toExternalForm().matches("^https?://www.pixiv.net/en/users/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member.php\\?id=.*$")) {
            if (url.toExternalForm().matches("^https?://www.pixiv.net/member.php\\?id=.*$")) {
                url = new URL("https://www.pixiv.net/en/users/" + current_id);
            }
            httpClient = new Http(api_endpoint + "user/illusts");
            httpClient.data("user_id", getGID(new URL(url.toExternalForm())));
            httpClient.data("type", "illust");
        }

        httpClient.header("Authorization", "Bearer " + access_token);
        jsonObj = httpClient.getJSON();

        return jsonObj;
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        if (url.toExternalForm().matches("^https?://www.pixiv.net/en/artworks/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member_illust.php\\?mode=medium&illust_id=.*$")) {
            throw new IOException("No more images");
        } else if (url.toExternalForm().matches("^https?://www.pixiv.net/en/users/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member.php\\?id=.*$")) {
            if (doc.isNull("next_url")) {
                throw new IOException("No more images");
            } else {
                Pattern p = Pattern.compile("offset=([0-9]+).*$");
                Matcher m = p.matcher(doc.getString("next_url"));
                if (m.find()) {
                    current_offset =  Integer.parseInt(m.group(1));
                }
            }
            Http httpClient = new Http(api_endpoint + "user/illusts");
            httpClient.header("Authorization", "Bearer " + access_token);
            httpClient.data("user_id", getGID(new URL(url.toExternalForm())));
            httpClient.data("type", "illust");
            httpClient.data("offset", Integer.toString(current_offset));
            return httpClient.getJSON();
        } else {
            throw new IOException("No more images");
        }
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> urls = new ArrayList<String>();
        options.put("referrer", url.toExternalForm());
        if (url.toExternalForm().matches("^https?://www.pixiv.net/en/artworks/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member_illust.php\\?mode=medium&illust_id=.*$")) {
             JSONArray meta_pages = json.getJSONObject("illust").getJSONArray("meta_pages");
            if (meta_pages.length() != 0) {
                for (int i = 0; i < meta_pages.length(); i++) {
                    String illust_urls = meta_pages.getJSONObject(i).getJSONObject("image_urls").getString("original");
                    urls.add(illust_urls);
                    try {
                        addURLToDownload(new URL(illust_urls), options);
                    } catch (MalformedURLException e) {
                        LOGGER.info("Invalid url.");
                    }
                }
            } else {
                String illust_urls = json.getJSONObject("illust").getJSONObject("meta_single_page").getString("original_image_url");
                urls.add(illust_urls);
                try {
                    addURLToDownload(new URL(illust_urls), options);
                } catch (MalformedURLException e) {
                    LOGGER.info("Invalid url.");
                }
            }
        } else if (url.toExternalForm().matches("^https?://www.pixiv.net/en/users/.*$") || url.toExternalForm().matches("^https?://www.pixiv.net/member.php\\?id=.*$")) {
            JSONArray illusts = json.getJSONArray("illusts");
            for (int i = 0; i < illusts.length(); i++) {
                JSONObject illust = illusts.getJSONObject(i);
                JSONArray meta_pages = illust.getJSONArray("meta_pages");
                if (meta_pages.length() != 0) {
                    for (int x = 0; x < meta_pages.length(); x++) {
                        String illust_url = meta_pages.getJSONObject(x).getJSONObject("image_urls").getString("original");
                        urls.add(illust_url);
                        options.put("subdirectory", "illust_" + illust.getInt("id"));
                        try {
                            addURLToDownload(new URL(illust_url), options);
                        } catch (MalformedURLException e) {
                            LOGGER.info("Invalid url.");
                        }

                    }
                } else {
                    options.remove("subdirectory");
                    String illust_url = illust.getJSONObject("meta_single_page").getString("original_image_url");
                    urls.add(illust_url);
                    try {
                        addURLToDownload(new URL(illust_url), options);
                    } catch (MalformedURLException e) {
                        LOGGER.info("Invalid url.");
                    }

                }
            }
        }
        return urls;
    }

    @Override
    protected void downloadURL(URL url, int index) { }


    private void auth() throws Exception {
        long session_time = Instant.now().getEpochSecond() - Long.parseLong(auth_time, 10);
        if ((access_token == null) || (user_id == null) || (refresh_token == null) || (refresh_token == null) || session_time >= 3600L) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u-MM-d'T'kk':'mm':'ss'+00:00'");
            ZonedDateTime currentTime = ZonedDateTime.now(ZoneOffset.UTC);
            String localtime = currentTime.format(formatter);
            URL auth_url = new URL("https://oauth.secure.pixiv.net/auth/token");

            HttpURLConnection httpClient = (HttpURLConnection) auth_url.openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setRequestProperty("User-Agent", "PixivAndroidApp/5.0.64 (Android 6.0)");
            httpClient.setRequestProperty("X-Client-Time", localtime);
            httpClient.setRequestProperty("X-Client-Hash", hexdigest(new String((localtime + HASH_SECRET).getBytes(), "UTF-8")));

            List<BasicNameValuePair> httpData = new ArrayList<BasicNameValuePair>();
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
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

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
        return;


    }

    private String getQuery(List<BasicNameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (BasicNameValuePair pair : params)
        {
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

    public String hexdigest(String message) throws Exception{
        String hd;
        MessageDigest md5 = MessageDigest.getInstance( "MD5" );
        md5.update( message.getBytes() );
        BigInteger hash = new BigInteger( 1, md5.digest() );
        hd = hash.toString(16); // BigInteger strips leading 0's
        while ( hd.length() < 32 ) { hd = "0" + hd; } // pad with leading 0's
        return hd;
    }
}
