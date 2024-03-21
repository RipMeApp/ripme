package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import com.rarchives.ripme.ripper.AbstractJSONRipper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class VkRipper extends AbstractJSONRipper {

    private static final String DOMAIN = "vk.com",
                                HOST   = "vk";

    enum RipType { VIDEO, IMAGE }

    private RipType RIP_TYPE;
    private String oid;
    private int offset = 0;

    public VkRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    protected String getDomain() {
        return DOMAIN;
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        if (RIP_TYPE == RipType.VIDEO) {
            oid = getGID(this.url).replace("videos", "");
            String u = "http://vk.com/al_video.php";
            Map<String, String> postData = new HashMap<>();
            postData.put("al", "1");
            postData.put("act", "load_videos_silent");
            postData.put("offset", "0");
            postData.put("oid", oid);
            Document doc = Http.url(u)
                    .referrer(this.url)
                    .ignoreContentType()
                    .data(postData)
                    .post();
            String[] jsonStrings = doc.toString().split("<!>");
            return new JSONObject(jsonStrings[jsonStrings.length - 1]);
        } else {
            return getPage();
        }
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        if (isStopped() || isThisATest()) {
            return null;
        }
        return getPage();
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject page) {
        List<String> pageURLs = new ArrayList<>();
        if (RIP_TYPE == RipType.VIDEO) {
            JSONArray videos = page.getJSONArray("all");
            LOGGER.info("Found " + videos.length() + " videos");

            for (int i = 0; i < videos.length(); i++) {
                JSONArray jsonVideo = videos.getJSONArray(i);
                int vidid = jsonVideo.getInt(1);
                String videoURL;
                try {
                    videoURL = com.rarchives.ripme.ripper.rippers.video.VkRipper.getVideoURLAtPage(
                            "http://vk.com/video" + oid + "_" + vidid);
                } catch (IOException e) {
                    LOGGER.error("Error while ripping video id: " + vidid);
                    return pageURLs;
                }
                pageURLs.add(videoURL);
            }
        } else {
            Iterator<String> keys = page.keys();
            while (keys.hasNext()) {
                pageURLs.add(page.getString(keys.next()));
            }
        }
        return pageURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        if (RIP_TYPE == RipType.VIDEO) {
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index + 1);
            }
            addURLToDownload(url, prefix);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting to fetch next video URL", e);
            }
        } else {
            addURLToDownload(url);
        }
    }

    @Override
    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        // Ignore /video pages (but not /videos pages)
        String u = url.toExternalForm();
        return !u.contains("/video") || u.contains("videos");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        if (this.url.toExternalForm().contains("/videos")) {
            RIP_TYPE = RipType.VIDEO;
            JSONObject json = getFirstPage();
            List<String> URLs = getURLsFromJSON(json);
            for (int index = 0; index < URLs.size(); index ++) {
                downloadURL(new URI(URLs.get(index)).toURL(), index);
            }
            waitForThreads();
        }
        else {
            RIP_TYPE = RipType.IMAGE;
        }
        super.rip();
    }

    private Map<String,String> getPhotoIDsToURLs(String photoID) throws IOException {
        Map<String,String> photoIDsToURLs = new HashMap<>();
        Map<String,String> postData = new HashMap<>();
        // act=show&al=1&list=album45506334_172415053&module=photos&photo=45506334_304658196
        postData.put("list", getGID(this.url));
        postData.put("act", "show");
        postData.put("al", "1");
        postData.put("module", "photos");
        postData.put("photo", photoID);
        Response res = Jsoup.connect("https://vk.com/al_photos.php")
                .header("Referer", this.url.toExternalForm())
                .header("Accept", "*/*")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Requested-With", "XMLHttpRequest")
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .timeout(5000)
                .data(postData)
                .method(Method.POST)
                .execute();
        String jsonString = res.body();
        JSONObject json = new JSONObject(jsonString);
        JSONObject photoObject = findJSONObjectContainingPhotoId(photoID, json);
        String bestSourceUrl = getBestSourceUrl(photoObject);

        if (bestSourceUrl != null) {
            photoIDsToURLs.put(photoID, bestSourceUrl);
        } else {
            LOGGER.error("Could not find image source for " + photoID);
        }

        return photoIDsToURLs;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?:\\/\\/(?:www\\.)?vk\\.com\\/((?:photos|album|videos)-?(?:[a-zA-Z0-9_]+).*$)");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: http://vk.com/album#### or vk.com/photos####");
        }
        return m.group(1);
    }


    /**
    * Finds the nested JSON object with entry "id": "photoID" recursively.
    * @param photoID The photoId string to be found with "id" as the key.
    * @param json Object of type JSONObject or JSONArray.
    * @return JSONObject with id as the photoID or null.
    */
    public JSONObject findJSONObjectContainingPhotoId(String photoID, Object json) {
        // Termination condition
        if (json instanceof JSONObject && ((JSONObject) json).has("id")
                && ((JSONObject) json).optString("id").equals(photoID)) {
            return ((JSONObject) json);
        }

        if (json instanceof JSONObject) {
            // Iterate through every key:value pair in the json.
            Iterator<String> iterator = ((JSONObject) json).keys();
            while (iterator.hasNext()) {
                Object o = ((JSONObject) json).get(iterator.next());
                JSONObject responseJson = findJSONObjectContainingPhotoId(photoID, o);
                if (responseJson != null) {
                    return responseJson;
                }
            }

        }

        if (json instanceof JSONArray) {
            // Iterate through every array value in the json
            for (Object o : (JSONArray) json) {
                if (o instanceof JSONObject || o instanceof JSONArray) {
                    JSONObject responseJson = findJSONObjectContainingPhotoId(photoID, o);
                    if (responseJson != null) {
                        return responseJson;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Find the best source url( with highest resolution).
     * @param json JSONObject containing src urls.
     * @return Url string for the image src or null.
     */
    public String getBestSourceUrl(JSONObject json) {
        String bestSourceKey = null;
        int bestSourceResolution = 0;
        Iterator<String> iterator = json.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            Object o = json.get(key);
            // JSON contains source urls in the below format. Check VkRipperTest.java for sample json.
            // {...,
            // "x_src":"src-url",
            // "x_": ["incomplete-url", width, height],
            // ...}
            if (o instanceof JSONArray && ((JSONArray) o).length() == 3
                    && !((JSONArray) o).optString(0).equals("") && ((JSONArray) o).optInt(1) != 0
                    && ((JSONArray) o).optInt(2) != 0 && json.has(key + "src")) {
                if (((JSONArray) o).optInt(1) * ((JSONArray) o).optInt(2) >= bestSourceResolution) {
                    bestSourceResolution = ((JSONArray) o).optInt(1) * ((JSONArray) o).optInt(2);
                    bestSourceKey = key;
                }
            }
        }
        
        // In case no suitable source has been found, we fall back to the older way.
        if(bestSourceKey == null) {
            for (String key : new String[] {"z_src", "y_src", "x_src", "w_src"}) {
                if(!json.has(key)) {
                    continue;
                }
                return json.getString(key);
            }
        }else {
            return json.getString(bestSourceKey + "src");
        }
        
        return null;
    }
    
    /**
     * Common function to get the next page( containing next batch of images).
     * @return JSONObject containing entries of "imgId": "src"
     * @throws IOException
     */
    private JSONObject getPage() throws IOException {
        Map<String, String> photoIDsToURLs = new HashMap<>();
        Map<String, String> postData = new HashMap<>();

        LOGGER.info("Retrieving " + this.url + " from offset " + offset);
        postData.put("al", "1");
        postData.put("offset", Integer.toString(offset));
        postData.put("part", "1");
        Document doc =
                Http.url(this.url).referrer(this.url).ignoreContentType().data(postData).post();
        String body = doc.toString();
        if (!body.contains("<div")) {
            return null;
        }
        body = body.substring(body.indexOf("<div"));
        body = StringEscapeUtils.unescapeJavaScript(body);
        doc = Jsoup.parseBodyFragment(body);
        List<Element> elements = doc.select("a");
        Set<String> photoIDsToGet = new HashSet<>();
        for (Element a : elements) {
            if (!a.attr("onclick").contains("showPhoto('")) {
                continue;
            }
            String photoID = a.attr("onclick");
            photoID = photoID.substring(photoID.indexOf("showPhoto('") + "showPhoto('".length());
            photoID = photoID.substring(0, photoID.indexOf("'"));
            if (!photoIDsToGet.contains(photoID)) {
                photoIDsToGet.add(photoID);
            }
        }
        for (String photoID : photoIDsToGet) {
            if (!photoIDsToURLs.containsKey(photoID)) {
                try {
                    photoIDsToURLs.putAll(getPhotoIDsToURLs(photoID));
                } catch (IOException e) {
                    LOGGER.error("Exception while retrieving photo id " + photoID, e);
                    continue;
                }
            }
            if (!photoIDsToURLs.containsKey(photoID)) {
                LOGGER.error("Could not find URL for photo ID: " + photoID);
                continue;
            }
            if (isStopped() || isThisATest()) {
                break;
            }
        }

        offset += elements.size();
        // Slight hack to make this into effectively a JSON ripper
        return new JSONObject(photoIDsToURLs);
    }
}
