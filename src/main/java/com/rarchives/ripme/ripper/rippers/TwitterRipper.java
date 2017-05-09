package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class TwitterRipper extends AlbumRipper {

    private static final String DOMAIN = "twitter.com",
            HOST = "twitter";

    private static final int MAX_REQUESTS = Utils.getConfigInteger("twitter.max_requests", 10);
    private static final int WAIT_TIME = 2000;

    // Base 64 of consumer key : consumer secret
    private String authKey;
    private String accessToken;

    private enum ALBUM_TYPE {
        ACCOUNT,
        SEARCH
    }

    private ALBUM_TYPE albumType;
    private String searchText, accountName;

    public TwitterRipper(URL url) throws IOException {
        super(url);
        authKey = Utils.getConfigString("twitter.auth", null);
        if (authKey == null) {
            throw new IOException("Could not find twitter authentication key in configuration");
        }
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        // https://twitter.com/search?q=from%3Apurrbunny%20filter%3Aimages&src=typd
        Pattern p = Pattern.compile("^https?://(m\\.)?twitter\\.com/search\\?q=([a-zA-Z0-9%\\-_]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            albumType = ALBUM_TYPE.SEARCH;
            searchText = m.group(2);
            return url;
        }
        p = Pattern.compile("^https?://(m\\.)?twitter\\.com/([a-zA-Z0-9\\-_]{1,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            albumType = ALBUM_TYPE.ACCOUNT;
            accountName = m.group(2);
            return url;
        }
        throw new MalformedURLException("Expected username or search string in url: " + url);
    }

    private void getAccessToken() throws IOException {
        Document doc = Http.url("https://api.twitter.com/oauth2/token")
                .ignoreContentType()
                .header("Authorization", "Basic " + authKey)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("User-agent", "ripe and zipe")
                .data("grant_type", "client_credentials")
                .post();
        String body = doc.body().html().replaceAll("&quot;", "\"");
        try {
            JSONObject json = new JSONObject(body);
            accessToken = json.getString("access_token");
            return;
        } catch (JSONException e) {
            // Fall through
            throw new IOException("Failure while parsing JSON: " + body, e);
        }
    }

    private void checkRateLimits(String resource, String api) throws IOException {
        Document doc = Http.url("https://api.twitter.com/1.1/application/rate_limit_status.json?resources=" + resource)
                .ignoreContentType()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("User-agent", "ripe and zipe")
                .get();
        String body = doc.body().html().replaceAll("&quot;", "\"");
        try {
            JSONObject json = new JSONObject(body);
            JSONObject stats = json.getJSONObject("resources")
                    .getJSONObject(resource)
                    .getJSONObject(api);
            int remaining = stats.getInt("remaining");
            logger.info("    Twitter " + resource + " calls remaining: " + remaining);
            if (remaining < 20) {
                logger.error("Twitter API calls exhausted: " + stats.toString());
                throw new IOException("Less than 20 API calls remaining; not enough to rip.");
            }
        } catch (JSONException e) {
            logger.error("JSONException: ", e);
            throw new IOException("Error while parsing JSON: " + body, e);
        }
    }

    private String getApiURL(Long maxID) {
        StringBuilder req = new StringBuilder();
        switch (albumType) {
            case ACCOUNT:
                req.append("https://api.twitter.com/1.1/statuses/user_timeline.json")
                        .append("?screen_name=" + this.accountName)
                        .append("&include_entities=true")
                        .append("&exclude_replies=true")
                        .append("&trim_user=true")
                        .append("&include_rts=false")
                        .append("&count=" + 200);
                break;
            case SEARCH:
                req.append("https://api.twitter.com/1.1/search/tweets.json")
                        .append("?q=" + this.searchText)
                        .append("&include_entities=true")
                        .append("&result_type=recent")
                        .append("&count=100");
                break;
        }
        if (maxID > 0) {
            req.append("&max_id=" + Long.toString(maxID));
        }
        return req.toString();
    }

    private List<JSONObject> getTweets(String url) throws IOException {
        List<JSONObject> tweets = new ArrayList<JSONObject>();
        logger.info("    Retrieving " + url);
        Document doc = Http.url(url)
                .ignoreContentType()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("User-agent", "ripe and zipe")
                .get();
        String body = doc.body().html().replaceAll("&quot;", "\"");
        Object jsonObj = new JSONTokener(body).nextValue();
        JSONArray statuses;
        if (jsonObj instanceof JSONObject) {
            JSONObject json = (JSONObject) jsonObj;
            if (json.has("errors")) {
                String msg = json.getJSONObject("errors").getString("message");
                throw new IOException("Twitter responded with errors: " + msg);
            }
            statuses = json.getJSONArray("statuses");
        } else {
            statuses = (JSONArray) jsonObj;
        }
        for (int i = 0; i < statuses.length(); i++) {
            tweets.add((JSONObject) statuses.get(i));
        }
        return tweets;
    }

    private int parseTweet(JSONObject tweet) throws MalformedURLException {
        int parsedCount = 0;
        if (!tweet.has("extended_entities")) {
            logger.error("XXX Tweet doesn't have entitites");
            return 0;
        }

        JSONObject entities = tweet.getJSONObject("extended_entities");

        if (entities.has("media")) {
            JSONArray medias = entities.getJSONArray("media");
            String url;
            JSONObject media;

            for (int i = 0; i < medias.length(); i++) {
                media = (JSONObject) medias.get(i);
                url = media.getString("media_url");
                if (media.getString("type").equals("video")) {
                    JSONArray variants = media.getJSONObject("video_info").getJSONArray("variants");
                    for (int j = 0; j < medias.length(); j++) {
                        JSONObject variant = (JSONObject) variants.get(i);
                        if (variant.has("bitrate") && variant.getInt("bitrate") == 832000) {
                            addURLToDownload(new URL(variant.getString("url")));
                            parsedCount++;
                            break;
                        }
                    }
                } else if (media.getString("type").equals("photo")) {
                    if (url.contains(".twimg.com/")) {
                        url += ":orig";
                        addURLToDownload(new URL(url));
                        parsedCount++;
                    } else {
                        logger.debug("Unexpected media_url: " + url);
                    }
                }
            }
        }


        return parsedCount;
    }

    @Override
    public void rip() throws IOException {
        getAccessToken();

        switch (albumType) {
            case ACCOUNT:
                checkRateLimits("statuses", "/statuses/user_timeline");
                break;
            case SEARCH:
                checkRateLimits("search", "/search/tweets");
                break;
        }

        Long lastMaxID = 0L;
        int parsedCount = 0;
        for (int i = 0; i < MAX_REQUESTS; i++) {
            List<JSONObject> tweets = getTweets(getApiURL(lastMaxID - 1));
            if (tweets.size() == 0) {
                logger.info("   No more tweets found.");
                break;
            }
            logger.debug("Twitter response #" + (i + 1) + " Tweets:\n" + tweets);
            if (tweets.size() == 1 &&
                    lastMaxID.equals(tweets.get(0).getString("id_str"))
                    ) {
                logger.info("   No more tweet found.");
                break;
            }

            for (JSONObject tweet : tweets) {
                lastMaxID = tweet.getLong("id");
                parsedCount += parseTweet(tweet);

                if (isStopped() || (isThisATest() && parsedCount > 0)) {
                    break;
                }
            }

            if (isStopped() || (isThisATest() && parsedCount > 0)) {
                break;
            }

            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load more results", e);
                break;
            }
        }

        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        switch (albumType) {
            case ACCOUNT:
                return "account_" + accountName;
            case SEARCH:
                StringBuilder gid = new StringBuilder();
                for (int i = 0; i < searchText.length(); i++) {
                    char c = searchText.charAt(i);
                    // Ignore URL-encoded chars
                    if (c == '%') {
                        gid.append('_');
                        i += 2;
                        continue;
                        // Ignore non-alphanumeric chars
                    } else if (
                            (c >= 'a' && c <= 'z')
                                    || (c >= 'A' && c <= 'Z')
                                    || (c >= '0' && c <= '9')
                            ) {
                        gid.append(c);
                    }
                }
                return "search_" + gid.toString();
        }
        throw new MalformedURLException("Could not decide type of URL (search/account): " + url);
    }

}
