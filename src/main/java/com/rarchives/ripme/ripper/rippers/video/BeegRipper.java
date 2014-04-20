package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.VideoRipper;

public class BeegRipper extends VideoRipper {

    private static final String HOST = "beeg";
    private static final Logger logger = Logger.getLogger(BeegRipper.class);

    public BeegRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*beeg\\.com/[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }
    
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*beeg\\.com/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected beeg format:"
                        + "beeg.com/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Jsoup.connect(this.url.toExternalForm())
                            .userAgent(USER_AGENT)
                            .get();
        Pattern p = Pattern.compile("^.*var qualityArr = (.*});.*$", Pattern.DOTALL);
        Matcher m = p.matcher(doc.html());
        if (m.matches()) {
            try {
                JSONObject json = new JSONObject(m.group(1));
                String vidUrl = null;
                for (String quality : new String[] {"1080p", "720p", "480p", "240p"}) {
                    if (json.has(quality)) {
                        vidUrl = json.getString(quality);
                        break;
                    }
                }
                if (vidUrl == null) {
                    throw new IOException("Unable to find video URL at " + this.url);
                }
                addURLToDownload(new URL(vidUrl), HOST + "_" + getGID(this.url));
                waitForThreads();
                return;
            } catch (JSONException e) {
                logger.error("Error while parsing JSON at " + url, e);
                throw e;
            }
        }
        throw new IOException("Failed to rip video at " + this.url);
    }
}