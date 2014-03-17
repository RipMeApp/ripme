package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;
import java.net.SocketTimeoutException;

public class RedditRipper extends AbstractRipper {

    public RedditRipper(URL url) throws IOException {
        super(url);
    }

    private static final String HOST   = "reddit";
    private static final String DOMAIN = "reddit.com";

    private static final Logger logger = Logger.getLogger(RedditRipper.class);
    private static final int SLEEP_TIME = 2000;

    //private static final String USER_AGENT = "ripme by /u/4_pr0n github.com/4pr0n/ripme";
    
    private long lastRequestTime = 0;

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        // Strip '/u/' from URL
        u = u.replaceAll("reddit\\.com/u/", "reddit.com/user/");
        return new URL(u);
    }

    private URL getJsonURL(URL url) throws MalformedURLException {
        // Append ".json" to URL in appropriate location.
        String result = url.getProtocol() + "://" + url.getHost() + url.getPath() + ".json";
        if (url.getQuery() != null) {
            result += "?" + url.getQuery();
        }
        return new URL(result);
    }

    @Override
    public void rip() throws IOException {
        URL jsonURL = getJsonURL(this.url);
        while (true) {
            jsonURL = getAndParseAndReturnNext(jsonURL);
            if (jsonURL == null) {
                break;
            }
        }
        waitForThreads();
    }
    
    
    
    private URL getAndParseAndReturnNext(URL url) throws IOException {
        JSONArray jsonArray = getJsonArrayFromURL(url), children;
        JSONObject json, data;
        URL nextURL = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            if (!json.has("data")) {
                continue;
            }
            data = json.getJSONObject("data");
            if (!data.has("children")) {
                continue;
            }
            children = data.getJSONArray("children");
            for (int j = 0; j < children.length(); j++) {
                parseJsonChild(children.getJSONObject(j));
            }
            if (data.has("after") && !data.isNull("after")) {
                String nextURLString = Utils.stripURLParameter(url.toExternalForm(), "after");
                if (nextURLString.contains("?")) {
                    nextURLString = nextURLString.concat("&after=" + data.getString("after"));
                }
                else {
                    nextURLString = nextURLString.concat("?after=" + data.getString("after"));
                }
                nextURL = new URL(nextURLString);
            }
        }
        return nextURL;
    }
    
    private JSONArray getJsonArrayFromURL(URL url) throws IOException {
        // Wait 2 seconds before the next request
        long timeDiff = System.currentTimeMillis() - lastRequestTime;
        if (timeDiff < SLEEP_TIME) {
            try {
                Thread.sleep(timeDiff);
            } catch (InterruptedException e) {
                logger.warn("[!] Interrupted while waiting to load next page", e);
                return new JSONArray();
            }
        }
        lastRequestTime = System.currentTimeMillis();

        int attempts = 0;
        Document doc = null;
        logger.info("    Retrieving " + url);
        while(doc == null && attempts++ < 3) {
            try {
                doc= Jsoup.connect(url.toExternalForm())
                                        .ignoreContentType(true)
                                        .userAgent(USER_AGENT)
                                        .get();
            } catch(SocketTimeoutException ex) {
                if(attempts >= 3) throw ex;
                logger.warn(String.format("[!] Connection timed out (attempt %d)", attempts));
            }
        }
        
        String jsonString = doc.body().html().replaceAll("&quot;", "\"");

        Object jsonObj = new JSONTokener(jsonString).nextValue();
        JSONArray jsonArray = new JSONArray();
        if (jsonObj instanceof JSONObject) {
            jsonArray.put( (JSONObject) jsonObj);
        } else if (jsonObj instanceof JSONArray){
            jsonArray = (JSONArray) jsonObj;
        } else {
            logger.warn("[!] Unable to parse child: " + jsonString);
        }
        return jsonArray;
    }

    private void parseJsonChild(JSONObject child) {
        String kind = child.getString("kind");
        JSONObject data = child.getJSONObject("data");
        if (kind.equals("t1")) {
            // Comment
            handleBody(data.getString("body"), data.getString("id"));
        }
        else if (kind.equals("t3")) {
            // post
            if (data.getBoolean("is_self")) {
                // TODO Parse self text
                handleBody(data.getString("selftext"), data.getString("id"));
            } else {
                // Get link
                handleURL(data.getString("url"), data.getString("id"));
            }
            if (data.has("replies") && data.get("replies") instanceof JSONObject) {
                JSONArray replies = data.getJSONObject("replies")
                                        .getJSONObject("data")
                                        .getJSONArray("children");
                for (int i = 0; i < replies.length(); i++) {
                    parseJsonChild(replies.getJSONObject(i));
                }
            }
        }
    }

    public void handleBody(String body, String id) {
        Pattern p = RipUtils.getURLRegex();
        Matcher m = p.matcher(body);
        while (m.find()) {
            handleURL(m.group(1), id);
        }
    }

    public void handleURL(String theUrl, String id) {
        URL originalURL;
        try {
            originalURL = new URL(theUrl);
        } catch (MalformedURLException e) {
            return;
        }

        List<URL> urls = RipUtils.getFilesFromURL(originalURL);
        if (urls.size() == 1) {
            addURLToDownload(urls.get(0), id + "-");
        } else if (urls.size() > 1) {
            for (int i = 0; i < urls.size(); i++) {
                addURLToDownload(urls.get(i), id + String.format("-%03d-", i + 1));
            }
        }
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // User
        Pattern p = Pattern.compile("^https?://[a-zA-Z0-9\\.]{0,4}reddit\\.com/(user|u)/([a-zA-Z0-9_\\-]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "user_" + m.group(m.groupCount());
        }

        // Post
        p = Pattern.compile("^https?://[a-zA-Z0-9\\.]{0,4}reddit\\.com/.*comments/([a-zA-Z0-9]{1,8}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "post_" + m.group(m.groupCount());
        }

        // Subreddit
        p = Pattern.compile("^https?://[a-zA-Z0-9\\.]{0,4}reddit\\.com/r/([a-zA-Z0-9_]{1,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "sub_" + m.group(m.groupCount());
        }

        throw new MalformedURLException("Only accepts user pages, subreddits, or post, can't understand " + url);
    }

}
