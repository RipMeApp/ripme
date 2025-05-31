package com.rarchives.ripme.ripper.rippers;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.iff;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.title;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.specialized.DivTag;

public class RedditRipper extends AlbumRipper {

    private static final Logger logger = LogManager.getLogger(RedditRipper.class);

    public RedditRipper(URL url) throws IOException {
        super(url);
    }

    private static final String HOST   = "reddit";
    private static final String DOMAIN = "reddit.com";

    private static final String REDDIT_USER_AGENT = "RipMe:github.com/laziassdev/ripme:" + UpdateUtils.getThisJarVersion() + " (by /u/metaprime and /u/ineedmorealts)";

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
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        String u = url.toExternalForm();
        // Strip '/u/' from URL
        u = u.replaceAll("reddit\\.com/u/", "reddit.com/user/");
        return new URI(u).toURL();
    }

    private URL getJsonURL(URL url) throws MalformedURLException, URISyntaxException {
        // Convert gallery to post link and append ".json"
        Pattern p = Pattern.compile("^https?://[a-zA-Z0-9.]{0,4}reddit\\.com/gallery/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URI("https://reddit.com/" +m.group(m.groupCount())+ ".json").toURL();
        }

        // Append ".json" to URL in appropriate location.
        String result = url.getProtocol() + "://" + url.getHost() + url.getPath() + ".json";
        if (url.getQuery() != null) {
            result += "?" + url.getQuery();
        }
        return new URI(result).toURL();
    }

    @Override
    public void rip() throws IOException {
        try {
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
        } catch (URISyntaxException e) {
            new IOException(e.getMessage());
        }
        waitForThreads();
    }



    private URL getAndParseAndReturnNext(URL url) throws IOException, URISyntaxException {
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
                try {
                    parseJsonChild(children.getJSONObject(j));

                    if (children.getJSONObject(j).getString("kind").equals("t3") &&
                            children.getJSONObject(j).getJSONObject("data").getBoolean("is_self")
                    ) {
                        URL selfPostURL = new URI(children.getJSONObject(j).getJSONObject("data").getString("url")).toURL();
                        System.out.println(selfPostURL.toExternalForm());
                        saveText(getJsonArrayFromURL(getJsonURL(selfPostURL)));
                    }
                } catch (Exception e) {
                    logger.debug("at index " + i + ", for this data: "  + data.toString() + e);
                }
            }
            if (data.has("after") && !data.isNull("after")) {
                String nextURLString = Utils.stripURLParameter(url.toExternalForm(), "after");
                if (nextURLString.contains("?")) {
                    nextURLString = nextURLString.concat("&after=" + data.getString("after"));
                }
                else {
                    nextURLString = nextURLString.concat("?after=" + data.getString("after"));
                }
                nextURL = new URI(nextURLString).toURL();
            }
        }

        // Wait to avoid rate-limiting against reddit's API
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while sleeping", e);
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
                logger.warn("[!] Interrupted while waiting to load next page", e);
                return new JSONArray();
            }
        }
        lastRequestTime = System.currentTimeMillis();

        String jsonString = Http.getWith429Retry(url, 5, 15, REDDIT_USER_AGENT);

        Object jsonObj = new JSONTokener(jsonString).nextValue();
        JSONArray jsonArray = new JSONArray();
        if (jsonObj instanceof JSONObject) {
            jsonArray.put(jsonObj);
        } else if (jsonObj instanceof JSONArray) {
            jsonArray = (JSONArray) jsonObj;
        } else {
            logger.warn("[!] Unable to parse JSON: " + jsonString);
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

    private void saveText(JSONArray jsonArray) throws JSONException {
        Path saveFileAs;

        JSONObject selfPost = jsonArray.getJSONObject(0).getJSONObject("data")
                .getJSONArray("children").getJSONObject(0).getJSONObject("data");
        JSONArray comments = jsonArray.getJSONObject(1).getJSONObject("data")
                .getJSONArray("children");

        if (selfPost.getString("selftext").equals("")) { return; }

        final String title = selfPost.getString("title");
        final String id = selfPost.getString("id");
        final String author = selfPost.getString("author");
        final String creationDate = new Date((long) selfPost.getInt("created") * 1000).toString();
        final String subreddit = selfPost.getString("subreddit");
        final String selfText = selfPost.getString("selftext_html");
        final String permalink = selfPost.getString("url");

        String html = TagCreator.html(
                head(
                        title(title),
                        style(rawHtml(HTML_STYLING))
                ),
                body(
                        div(
                                h1(title),
                                a(subreddit).withHref("https://www.reddit.com/r/" + subreddit),
                                a("Original").withHref(permalink),
                                br()
                        ).withClass("thing"),
                        div(
                                div(
                                        span(
                                                a(author).withHref("https://www.reddit.com/u/" + author)
                                        ).withClass("author op")
                                ).withClass("thing oppost")
                                        .withText(creationDate)
                                        .with(rawHtml(Jsoup.parse(selfText).text()))
                        ).withClass("flex")
                ).with(getComments(comments, author)),
                script(rawHtml(HTML_SCRIPT))
        ).renderFormatted();

        try {
            saveFileAs = Utils.getPath(workingDir
                    + "/"
                    + id + "_" + Utils.filesystemSafe(title)
                    + ".html");
            OutputStream out = Files.newOutputStream(saveFileAs);
            out.write(html.getBytes());
            out.close();
        } catch (IOException e) {
            logger.error("[!] Error creating save file path for description '" + url + "':", e);
            return;
        }

        logger.debug("Downloading " + url + "'s self post to " + saveFileAs);
        super.retrievingSource(permalink);
        if (!Files.exists(saveFileAs.getParent())) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            try {
                Files.createDirectory(saveFileAs.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ContainerTag<DivTag> getComments(JSONArray comments, String author) {
        ContainerTag<DivTag> commentsDiv = div().withId("comments");

        for (int i = 0; i < comments.length(); i++) {
            JSONObject data = comments.getJSONObject(i).getJSONObject("data");

            try {
                ContainerTag<DivTag>  commentDiv =
                    div(
                            span(data.getString("author")).withClasses("author", iff(data.getString("author").equals(author), "op")),
                            a(new Date((long) data.getInt("created") * 1000).toString()).withHref("#" + data.getString("name"))
                    ).withClass("thing comment").withId(data.getString("name"))
                            .with(rawHtml(Jsoup.parse(data.getString("body_html")).text()));
                getNestedComments(data, commentDiv, author);
                commentsDiv.with(commentDiv);
            } catch (Exception e) {
                logger.debug("at index " + i + ", for this data: "  + data.toString() + e);
            }
        }
        return commentsDiv;
    }

    private ContainerTag<DivTag> getNestedComments(JSONObject data, ContainerTag<DivTag> parentDiv, String author) {
        if (data.has("replies") && data.get("replies") instanceof JSONObject) {
            JSONArray commentChildren = data.getJSONObject("replies").getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < commentChildren.length(); i++) {
                JSONObject nestedComment = commentChildren
                        .getJSONObject(i).getJSONObject("data");

                String nestedCommentAuthor = nestedComment.optString("author");
                if (!nestedCommentAuthor.isBlank()) {
                    ContainerTag<DivTag> childDiv =
                            div(
                                    div(
                                            span(nestedCommentAuthor).withClasses("author", iff(nestedCommentAuthor.equals(author), "op")),
                                            a(new Date((long) nestedComment.getInt("created") * 1000).toString()).withHref("#" + nestedComment.getString("name"))
                                    ).withClass("comment").withId(nestedComment.getString("name"))
                                            .with(rawHtml(Jsoup.parse(nestedComment.getString("body_html")).text()))
                            ).withClass("child");

                    parentDiv.with(getNestedComments(nestedComment, childDiv, author));
                }
            }
        }
        return parentDiv;
    }

    private URL parseRedditVideoMPD(String vidURL) {
        org.jsoup.nodes.Document doc;
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
            return new URI(vidURL + "/" + baseURL).toURL();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void handleURL(String theUrl, String id, String title) {
        URL originalURL;
        try {
            originalURL = new URI(theUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
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
                String savePath = this.workingDir + "/";
                savePath += id + "-" + m.group(1) + Utils.filesystemSafe(title) + ".jpg";
                addURLToDownload(urls.get(0), Utils.getPath(savePath));
            } else if (url.contains("v.redd.it")) {
                String savePath = this.workingDir + "/";
                savePath += id + "-" + url.split("/")[3] + Utils.filesystemSafe(title) + ".mp4";
                URL urlToDownload = parseRedditVideoMPD(urls.get(0).toExternalForm());
                if (urlToDownload != null) {
                    logger.info("url: " + urlToDownload + " file: " + savePath);
                    addURLToDownload(urlToDownload, Utils.getPath(savePath));
                }
            } else {
                if (url.contains("redgifs.com")) {
                    // redgifs.com rate limits us if we download too fast,
                    // and redgifs.com is a pretty common link on Reddit.
                    sleep(3000);
                }
                addURLToDownload(urls.get(0), Utils.filesystemSafe(id + title), "", theUrl, null);
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
            		mediaURL = new URI(media.getJSONObject("s").getString("gif").replaceAll("&amp;", "&")).toURL();
            	} else {
            		mediaURL = new URI(media.getJSONObject("s").getString("u").replaceAll("&amp;", "&")).toURL();
            	}
                addURLToDownload(mediaURL, prefix, subdirectory);
            } catch (MalformedURLException | JSONException | URISyntaxException e) {
                logger.error("[!] Unable to parse gallery JSON:\ngallery_data:\n" + data +"\nmedia_metadata:\n" + metadata);
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

    private static final String HTML_STYLING = " .author { font-weight: bold; } .op { color: blue; } .comment { border: 0px; margin: 0 0 25px; padding-left: 5px; } .child { margin: 2px 0 0 20px; border-left: 2px dashed #AAF; } .collapsed { background: darkgrey; margin-bottom: 0; } .collapsed > div { display: none; } .md { max-width: 840px; padding-right: 1em; } h1 { margin: 0; } body { position: relative; background-color: #eeeeec; color: #00000a; font-weight: 400; font-style: normal; font-variant: normal; font-family: Helvetica,Arial,sans-serif; line-height: 1.4 } blockquote { margin: 5px 5px 5px 15px; padding: 1px 1px 1px 15px; max-width: 60em; border: 1px solid #ccc; border-width: 0 0 0 1px; } pre { white-space: pre-wrap; } img, video { max-width: 60vw; max-height: 90vh; object-fit: contain; } .thing { overflow: hidden; margin: 0 5px 3px 40px; border: 1px solid #e0e0e0; background-color: #fcfcfb; } :target > .md { border: 5px solid blue; } .post { margin-bottom: 20px; margin-top: 20px; } .gold { background: goldenrod; } .silver { background: silver; } .platinum { background: aqua; } .deleted { background: #faa; } .md.deleted { background: inherit; border: 5px solid #faa; } .oppost { background-color: #EEF; } blockquote > p { margin: 0; } #related { max-height: 20em; overflow-y: scroll; background-color: #F4FFF4; } #related h3 { position: sticky; top: 0; background-color: white; } .flex { display: flex; flex-flow: wrap; flex-direction: row-reverse; justify-content: flex-end; } ";
    private static final String HTML_SCRIPT = "document.addEventListener('mousedown', function(e) { var t = e.target; if (t.className == 'author') { t = t.parentElement; } if (t.classList.contains('comment')) { t.classList.toggle('collapsed'); e.preventDefault(); e.stopPropagation(); return false; } });";

}
