package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class ImagestashRipper extends AlbumRipper {

    private static final String DOMAIN = "imagestash.org",
                                HOST   = "imagestash";

    public ImagestashRipper(URL url) throws IOException {
        super(url);
    }

    public boolean canRip(URL url) {
        return url.getHost().equals(DOMAIN);
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        // Given URL: https://imagestash.org/tag/everydayuncensor
        // GID:       "everydayuncensor"
        // JSON URL:  https://imagestash.org/images?tags=everydayuncensor&page=1
        String baseURL = "https://imagestash.org/images?tags=" + getGID(this.url);
        int page = 0, index = 0;
        while (true) {
            page++;
            String nextURL = baseURL + "&page=" + page;
            logger.info("[ ] Retrieving " + nextURL);
            sendUpdate(STATUS.LOADING_RESOURCE, nextURL);
            String jsonText = Jsoup.connect(nextURL)
                                   .ignoreContentType(true)
                                   .userAgent(USER_AGENT)
                                   .method(Method.GET)
                                   .execute()
                                   .body();
            logger.info(jsonText);
            JSONObject json = new JSONObject(jsonText);
            JSONArray images = json.getJSONArray("images");
            for (int i = 0; i < images.length(); i++) {
                JSONObject image = images.getJSONObject(i);
                String imageURL = image.getString("src");
                if (imageURL.startsWith("/")) {
                    imageURL = "http://imagestash.org" + imageURL;
                }
                index += 1;
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(imageURL), prefix);
            }
            // Check if there are more images to fetch
            int count  = json.getInt("count"),
                offset = json.getInt("offset"),
                total  = json.getInt("total");
            if (count + offset >= total || images.length() == 0) {
                break;
            }
            // Wait a bit
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting to load next page", e);
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
        Pattern p = Pattern.compile("^.*imagestash.org/tag/([a-zA-Z0-9\\-_]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected imagestash.org tag formats: "
                        + "imagestash.org/tag/tagname"
                        + " Got: " + url);
    }
}
