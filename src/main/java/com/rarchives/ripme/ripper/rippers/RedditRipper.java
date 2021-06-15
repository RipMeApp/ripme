package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ui.RipStatusMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

public class RedditRipper extends AlbumRipper {

    public RedditRipper(URL url) throws IOException {
        super(url);
    }

    private static final String HOST   = "reddit";
    private static final String DOMAIN = "reddit.com";

    private static final String REDDIT_USER_AGENT = "RipMe:github.com/RipMeApp/ripme:" + UpdateUtils.getThisJarVersion() + " (by /u/metaprime and /u/ineedmorealts)";

    private static final int SLEEP_TIME = 2000;

    //private static final String USER_AGENT = "ripme by /u/4_pr0n github.com/4pr0n/ripme";

    private long lastRequestTime = 0;

    private Boolean shouldAddURL() {
        return (alreadyDownloadedUrls >= Utils.getConfigInteger("history.end_rip_after_already_seen", 1000000000) && !isThisATest());
    }

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
        // Convert gallery to post link and append ".json"
        Pattern p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/gallery/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("https://reddit.com/" +m.group(m.groupCount())+ ".json");
        }

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
            if (shouldAddURL()) {
                sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_COMPLETE_HISTORY, "Already seen the last " + alreadyDownloadedUrls + " images ending rip");
                break;
            }
            jsonURL = getAndParseAndReturnNext(jsonURL);
            if (jsonURL == null || isThisATest() || isStopped()) {
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

        // Wait to avoid rate-limiting against reddit's API
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while sleeping", e);
        }
        return nextURL;
    }

    /**
     * Gets a representation of the specified reddit page as a JSONArray using the reddit API
     * @param url The url of the desired page
     * @return A JSONArray object representation of the desired page
     * @throws IOException If no response is received from the url
     */
    private JSONArray getJsonArrayFromURL(URL url) throws IOException {
        // Wait 2 seconds before the next request
        long timeDiff = System.currentTimeMillis() - lastRequestTime;
        if (timeDiff < SLEEP_TIME) {
            try {
                Thread.sleep(timeDiff);
            } catch (InterruptedException e) {
                LOGGER.warn("[!] Interrupted while waiting to load next page", e);
                return new JSONArray();
            }
        }
        lastRequestTime = System.currentTimeMillis();

        String jsonString = Http.url(url)
                                .ignoreContentType()
                                .userAgent(REDDIT_USER_AGENT)
                                .response()
                                .body();

        Object jsonObj = new JSONTokener(jsonString).nextValue();
        JSONArray jsonArray = new JSONArray();
        if (jsonObj instanceof JSONObject) {
            jsonArray.put(jsonObj);
        } else if (jsonObj instanceof JSONArray) {
            jsonArray = (JSONArray) jsonObj;
        } else {
            LOGGER.warn("[!] Unable to parse JSON: " + jsonString);
        }
        return jsonArray;
    }

    /**
     * Turns child JSONObject's into usable URLs and hands them off for further processing
     * Performs filtering checks based on the reddit.
     * Only called from getAndParseAndReturnNext() while parsing the JSONArray returned from reddit's API
     * @param child The child to process
     */
    private void parseJsonChild(JSONObject child) {
        String kind = child.getString("kind");
        JSONObject data = child.getJSONObject("data");

        //Upvote filtering
        if (Utils.getConfigBoolean("reddit.rip_by_upvote", false)){
            int score = data.getInt("score");
            int maxScore = Utils.getConfigInteger("reddit.max_upvotes", Integer.MAX_VALUE);
            int minScore = Utils.getConfigInteger("reddit.min_upvotes", Integer.MIN_VALUE);

            if (score > maxScore || score < minScore) {

                String message = "Skipping post with score outside specified range of " + minScore + " to " + maxScore;
                sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, message);
                return; //Outside specified range, do not download
            }
        }

        if (kind.equals("t1")) {
            // Comment
            handleBody(data.getString("body"), data.getString("id"), "");
        }
        else if (kind.equals("t3")) {
            // post
            if (data.getBoolean("is_self")) {
                // TODO Parse self text
                handleBody(data.getString("selftext"), data.getString("id"), data.getString("title"));
            } else if (!data.isNull("gallery_data") && !data.isNull("media_metadata")) {
                handleGallery(data.getJSONObject("gallery_data").getJSONArray("items"), data.getJSONObject("media_metadata"), data.getString("id"), data.getString("title"));
            } else {
                // Get link
                handleURL(data.getString("url"), data.getString("id"), data.getString("title"));
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

    private void handleBody(String body, String id, String title) {
        Pattern p = RipUtils.getURLRegex();
        Matcher m = p.matcher(body);
        while (m.find()) {
            String url = m.group(1);
            while (url.endsWith(")")) {
                url = url.substring(0, url.length() - 1);
            }
            handleURL(url, id, title);
        }
    }

    private URL parseRedditVideoMPD(String vidURL) {
        org.jsoup.nodes.Document doc = null;
        try {
            doc = Http.url(vidURL + "/DASHPlaylist.mpd").ignoreContentType().get();
            int largestHeight = 0;
            String baseURL = null;
            // Loops over all the videos and finds the one with the largest height and sets baseURL to the base url of that video
            for (org.jsoup.nodes.Element e : doc.select("MPD > Period > AdaptationSet > Representation")) {
                String height = e.attr("height");
                if (height.equals("")) {
                    height = "0";
                }
                if (largestHeight < Integer.parseInt(height)) {
                    largestHeight = Integer.parseInt(height);
                    baseURL = doc.select("MPD > Period > AdaptationSet > Representation[height=" + height + "]").select("BaseURL").text();
                }
            }
            return new URL(vidURL + "/" + baseURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void handleURL(String theUrl, String id, String title) {
        URL originalURL;
        try {
            originalURL = new URL(theUrl);
        } catch (MalformedURLException e) {
            return;
        }
        String subdirectory = "";
        if (Utils.getConfigBoolean("reddit.use_sub_dirs", true)) {
            if (Utils.getConfigBoolean("album_titles.save", true)) {
                subdirectory = title;
                title = "-" + title + "-";
            } else {
                title = "";
            }
        }

        List<URL> urls = RipUtils.getFilesFromURL(originalURL);
        if (urls.size() == 1) {
            String url = urls.get(0).toExternalForm();
            Pattern p = Pattern.compile("https?://i.reddituploads.com/([a-zA-Z0-9]+)\\?.*");
            Matcher m = p.matcher(url);
            if (m.matches()) {
                // It's from reddituploads. Assume .jpg extension.
                String savePath = this.workingDir + File.separator;
                savePath += id + "-" + m.group(1) + title + ".jpg";
                addURLToDownload(urls.get(0), new File(savePath));
            }
            if (url.contains("v.redd.it")) {
                String savePath = this.workingDir + File.separator;
                savePath += id + "-" + url.split("/")[3] + title + ".mp4";
                URL urlToDownload = parseRedditVideoMPD(urls.get(0).toExternalForm());
                if (urlToDownload != null) {
                    LOGGER.info("url: " + urlToDownload + " file: " + savePath);
                    addURLToDownload(urlToDownload, new File(savePath));
                }
            }
            else {
                addURLToDownload(urls.get(0), id + title, "", theUrl, null);
            }
        } else if (urls.size() > 1) {
            for (int i = 0; i < urls.size(); i++) {
                String prefix = id + "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix += String.format("%03d-", i + 1);
                }
                addURLToDownload(urls.get(i), prefix, subdirectory, theUrl, null);
            }
        }
    }

    private void handleGallery(JSONArray data, JSONObject metadata, String id, String title){
        //TODO handle captions and caption urls
        String subdirectory = "";
        if (Utils.getConfigBoolean("reddit.use_sub_dirs", true)) {
            if (Utils.getConfigBoolean("album_titles.save", true)) {
                subdirectory = title;
                title = "-" + title + "-";
            }
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject media = metadata.getJSONObject(data.getJSONObject(i).getString("media_id"));
            String prefix = id + "-";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                //announcement says up to 20 (https://www.reddit.com/r/announcements/comments/hrrh23/now_you_can_make_posts_with_multiple_images/)
                prefix += String.format("%02d-", i + 1);
            }
            try {
                URL mediaURL;
            	if (!media.getJSONObject("s").isNull("gif")) {
            		mediaURL = new URL(media.getJSONObject("s").getString("gif").replaceAll("&amp;", "&"));
            	} else {
            		mediaURL = new URL(media.getJSONObject("s").getString("u").replaceAll("&amp;", "&"));
            	}
                addURLToDownload(mediaURL, prefix, subdirectory);
            } catch (MalformedURLException | JSONException e) {
                LOGGER.error("[!] Unable to parse gallery JSON:\ngallery_data:\n" + data +"\nmedia_metadata:\n" + metadata);
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
        Pattern p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/(user|u)/([a-zA-Z0-9_\\-]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "user_" + m.group(m.groupCount());
        }

        // Post
        p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/.*comments/([a-zA-Z0-9]{1,8}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "post_" + m.group(m.groupCount());
        }

        // Gallery
        p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/gallery/([a-zA-Z0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "post_" + m.group(m.groupCount());
        }

        // Subreddit
        p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/r/([a-zA-Z0-9_]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return "sub_" + m.group(m.groupCount());
        }

        throw new MalformedURLException("Only accepts user pages, subreddits, post, or gallery can't understand " + url);
    }

}
