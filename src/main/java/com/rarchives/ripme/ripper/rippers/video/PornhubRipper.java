package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class PornhubRipper extends VideoRipper {

    private static final String HOST = "pornhub";

    public PornhubRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*pornhub\\.com/view_video.php\\?viewkey=[a-z0-9]+$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*pornhub\\.com/view_video.php\\?viewkey=([a-z0-9]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected pornhub format:"
                        + "pornhub.com/view_video.php?viewkey=####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        LOGGER.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Http.url(this.url).get();
        String html = doc.body().html();
        Pattern p = Pattern.compile("^.*flashvars_[0-9]+ = (.+});.*$", Pattern.DOTALL);
        Matcher m = p.matcher(html);
        if (m.matches()) {
            String vidUrl = null;
            try {
                JSONObject json = new JSONObject(m.group(1));

                JSONArray mediaDef = (JSONArray) json.get("mediaDefinitions");
                int bestQual = 0;
                for (int i = 0; i < mediaDef.length(); i++) {
                    JSONObject e = (JSONObject) mediaDef.get(i);
                    if (!"upsell".equals(e.getString("format"))) {
                        int quality = Integer.parseInt((String)e.get("quality"));
                        if (quality > bestQual) {
                            bestQual = quality;
                            vidUrl = (String)e.get("videoUrl");
                        }
                    }
                }
                if (vidUrl == null) {
                    throw new IOException("Unable to find encrypted video URL at " + this.url);
                }
                addURLToDownload(new URL(vidUrl), HOST + "_" + bestQual + "p_" + getGID(this.url));
            } catch (JSONException e) {
                LOGGER.error("Error while parsing JSON at " + url, e);
                throw e;
            } catch (Exception e) {
                LOGGER.error("Error while retrieving video URL at " + url, e);
                throw new IOException(e);
            }
        }
        else {
            throw new IOException("Failed to download " + this.url + " : could not find 'flashvars'");
        }
        waitForThreads();
    }
}

