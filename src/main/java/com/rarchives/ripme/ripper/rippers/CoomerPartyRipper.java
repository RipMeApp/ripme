package com.rarchives.ripme.ripper.rippers;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ripper.AbstractRipper;

/**
 * <a href="https://coomer.su/api/schema">See this link for the API schema</a>.
 */
public class CoomerPartyRipper extends AbstractJSONRipper {

    private static final Logger logger = LogManager.getLogger(CoomerPartyRipper.class);

    private static final String IMG_URL_BASE = "https://c3.coomer.su/data";
    private static final String VID_URL_BASE = "https://c1.coomer.su/data";
    private static final Pattern IMG_PATTERN = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|apng|webp|tif|tiff)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VID_PATTERN = Pattern.compile("^.*\\.(webm|mp4|m4v)$", Pattern.CASE_INSENSITIVE);

    // just so we can return a JSONObject from getFirstPage
    private static final String KEY_WRAPPER_JSON_ARRAY = "array";

    private static final String KEY_FILE = "file";
    private static final String KEY_PATH = "path";
    private static final String KEY_ATTACHMENTS = "attachments";

    // Posts Request Endpoint
    private static final String POSTS_ENDPOINT = "https://coomer.su/api/v1/%s/user/%s?o=%d";

    // Pagination is strictly 50 posts per page, per API schema.
    private Integer pageCount = 0;
    private static final Integer postCount = 50;

    // "Service" of the page to be ripped: Onlyfans, Fansly, Candfans
    private final String service;

    // Username of the page to be ripped
    private final String user;

    private final int maxDownloads = Utils.getConfigInteger("maxdownloads", -1);
    private int downloadCounter = 0;
    private int queuedDownloadCounter = 0;



    public CoomerPartyRipper(URL url) throws IOException {
        super(url);
        List<String> pathElements = Arrays.stream(url.getPath().split("/"))
                .filter(element -> !element.isBlank())
                .collect(Collectors.toList());

        service = pathElements.get(0);
        user = pathElements.get(2);

        if (service == null || user == null || service.isBlank() || user.isBlank()) {
            logger.warn("service=" + service + ", user=" + user);
            throw new MalformedURLException("Invalid coomer.party URL: " + url);
        }
        logger.debug("Parsed service=" + service + " and user=" + user + " from " + url);
    }

    @Override
    protected String getDomain() {
        return "coomer.party";
    }

    @Override
    public String getHost() {
        return "coomer.party";
    }

    @Override
    public boolean canRip(URL url) {
        String host = url.getHost();
        return host.endsWith("coomer.party") || host.endsWith("coomer.su");
    }

    @Override
    public String getGID(URL url) {
        return Utils.filesystemSafe(String.format("%s_%s", service, user));
    }

    private JSONObject getJsonPostsForOffset(Integer offset) throws IOException {
        String apiUrl = String.format(POSTS_ENDPOINT, service, user, offset);

        String jsonArrayString = Http.url(apiUrl)
                .ignoreContentType()
                .response()
                .body();
        JSONArray jsonArray = new JSONArray(jsonArrayString);

        // Ideally we'd just return the JSONArray from here, but we have to wrap it in a JSONObject
        JSONObject wrapperObject = new JSONObject();
        wrapperObject.put(KEY_WRAPPER_JSON_ARRAY, jsonArray);
        return wrapperObject;
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        JSONObject page = getJsonPostsForOffset(0);
        JSONArray posts = page.getJSONArray(KEY_WRAPPER_JSON_ARRAY);
        if (posts.length() == 0) {
            logger.warn("No posts returned for " + getURL());
            return null; // graceful exit
        }
        return page;
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        if (maxDownloads > 0 && queuedDownloadCounter >= maxDownloads) {
            logger.info("Completed {} of max {} downloads.", downloadCounter, maxDownloads);
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_COMPLETE_HISTORY, "Reached maxdownloads limit of " + maxDownloads + ". Stopping.");
            return null;
        }
        pageCount++;
        Integer offset = postCount * pageCount;
        return getJsonPostsForOffset(offset);
    }


    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        JSONArray posts = json.getJSONArray(KEY_WRAPPER_JSON_ARRAY);
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < posts.length(); i++) {
            if (maxDownloads > 0 && queuedDownloadCounter >= maxDownloads) {
                logger.info("Reached maxDownloads (" + maxDownloads + "), stopping URL collection");
                break;
            }

            int initialSize = urls.size();

            JSONObject post = posts.getJSONObject(i);
            pullFileUrl(post, urls);
            pullAttachmentUrls(post, urls);

            // Increment queued counter only by how many new URLs we added
            int newUrls = urls.size() - initialSize;
            queuedDownloadCounter += newUrls;
        }
        logger.debug("Pulled " + urls.size() + " URLs from " + posts.length() + " posts");
        return urls;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        try {
            URL resolvedUrl = Http.followRedirectsWithRetry(url, 5, 5, AbstractRipper.USER_AGENT);
            addURLToDownload(resolvedUrl, getPrefix(index));
        } catch (IOException e) {
            logger.error("Failed to resolve or download redirect URL {}: {}", url, e.getMessage());
        }
    }

    @Override
    public void downloadCompleted(URL url, java.nio.file.Path saveAs) {
        super.downloadCompleted(url, saveAs);
        downloadCounter++;

        if (maxDownloads > 0 && downloadCounter >= maxDownloads) {
            logger.info("Completed {} of max {} downloads. Stopping rip.", downloadCounter, maxDownloads);
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_COMPLETE_HISTORY, "Reached maxdownloads limit of " + maxDownloads + ". Stopping.");

        }
    }

    private void pullFileUrl(JSONObject post, ArrayList<String> results) {
        try {
            if (!post.has(KEY_FILE)) {
                logger.warn("Post missing 'file' key, skipping");
                return;
            }

            JSONObject file = post.getJSONObject(KEY_FILE);
            if (!file.has(KEY_PATH)) {
                logger.warn("File object missing 'path' key, skipping");
                return;
            }

            String path = file.getString(KEY_PATH);
            if (isImage(path)) {
                results.add(IMG_URL_BASE + path);
            } else if (isVideo(path)) {
                results.add(VID_URL_BASE + path);
            } else {
                logger.warn("Unrecognized media type for path: " + path);
            }
        } catch (JSONException e) {
            logger.error("Error parsing file URL: " + e.getMessage());
        }
    }

    private void pullAttachmentUrls(JSONObject post, ArrayList<String> results) {
        try {
            JSONArray attachments = post.getJSONArray(KEY_ATTACHMENTS);
            for (int i = 0; i < attachments.length(); i++) {
                JSONObject attachment = attachments.getJSONObject(i);
                pullFileUrl(attachment, results);
            }
        } catch (JSONException e) {
             /* No-op */
            logger.error("Unable to Parse AttachmentURL " + e.getMessage());
        }
    }

    private boolean isImage(String path) {
        Matcher matcher = IMG_PATTERN.matcher(path);
        return matcher.matches();
    }

    private boolean isVideo(String path) {
        Matcher matcher = VID_PATTERN.matcher(path);
        return matcher.matches();
    }
}
