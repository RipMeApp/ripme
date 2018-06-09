package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class VkRipper extends AlbumRipper {

    private static final String DOMAIN = "vk.com",
                                HOST   = "vk";

    public VkRipper(URL url) throws IOException {
        super(url);
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
    public void rip() throws IOException {
        if (this.url.toExternalForm().contains("/videos")) {
            ripVideos();
        }
        else {
            ripImages();
        }
    }

    private void ripVideos() throws IOException {
        String oid = getGID(this.url).replace("videos", "");
        String u = "http://vk.com/al_video.php";
        Map<String,String> postData = new HashMap<>();
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
        JSONObject json = new JSONObject(jsonStrings[jsonStrings.length - 1]);
        JSONArray videos = json.getJSONArray("all");
        LOGGER.info("Found " + videos.length() + " videos");
        for (int i = 0; i < videos.length(); i++) {
            JSONArray jsonVideo = videos.getJSONArray(i);
            int vidid = jsonVideo.getInt(1);
            String videoURL = com.rarchives.ripme.ripper.rippers.video.VkRipper.getVideoURLAtPage(
                    "http://vk.com/video" + oid + "_" + vidid);
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", i + 1);
            }
            addURLToDownload(new URL(videoURL), prefix);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting to fetch next video URL", e);
                break;
            }
        }
        waitForThreads();
    }

    private void ripImages() throws IOException {
        Map<String,String> photoIDsToURLs = new HashMap<>();
        int offset = 0;
        while (true) {
            LOGGER.info("    Retrieving " + this.url);

            // al=1&offset=80&part=1
            Map<String,String> postData = new HashMap<>();
            postData.put("al", "1");
            postData.put("offset", Integer.toString(offset));
            postData.put("part", "1");
            Document doc = Http.url(this.url)
                               .referrer(this.url)
                               .ignoreContentType()
                               .data(postData)
                               .post();

            String body = doc.toString();
            if (!body.contains("<div")) {
                break;
            }
            body = body.substring(body.indexOf("<div"));
            doc = Jsoup.parseBodyFragment(body);
            List<Element> elements = doc.select("a");
            Set<String> photoIDsToGet = new HashSet<>();
            for (Element a : elements) {
                if (!a.attr("onclick").contains("showPhoto('")) {
                    LOGGER.error("a: " + a);
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
                String url = photoIDsToURLs.get(photoID);
                addURLToDownload(new URL(url));
                if (isStopped() || isThisATest()) {
                    break;
                }
            }

            if (elements.size() < 40 || isStopped() || isThisATest()) {
                break;
            }
            offset += elements.size();
        }
        waitForThreads();
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
        Document doc = Jsoup
                .connect("https://vk.com/al_photos.php")
                .header("Referer", this.url.toExternalForm())
                .ignoreContentType(true)
                .userAgent(USER_AGENT)
                .timeout(5000)
                .data(postData)
                .post();
        String jsonString = doc.toString();
        jsonString = jsonString.substring(jsonString.indexOf("<!json>") + "<!json>".length());
        jsonString = jsonString.substring(0, jsonString.indexOf("<!>"));
        JSONArray json = new JSONArray(jsonString);
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonImage = json.getJSONObject(i);
            for (String key : new String[] {"z_src", "y_src", "x_src"}) {
                if (!jsonImage.has(key)) {
                    continue;
                }
                photoIDsToURLs.put(jsonImage.getString("id"), jsonImage.getString(key));
                break;
            }
        }
        return photoIDsToURLs;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?vk\\.com/(photos|album|videos)-?([a-zA-Z0-9_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected format: http://vk.com/album#### or vk.com/photos####");
        }
        int count = m.groupCount();
        return m.group(count - 1) + m.group(count);
    }

}
