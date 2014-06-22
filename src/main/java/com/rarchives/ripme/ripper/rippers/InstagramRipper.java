package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class InstagramRipper extends AlbumRipper {

    private static final String DOMAIN = "instagram.com",
                                HOST   = "instagram";

    public InstagramRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith(DOMAIN)
             || url.getHost().endsWith("statigr.am")
             || url.getHost().endsWith("iconosquare.com"));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://instagram\\.com/p/([a-zA-Z0-9\\-_.]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Link to photo, not the user account
            try {
                url = getUserPageFromImage(url);
            } catch (Exception e) {
                logger.error("[!] Failed to get user page from " + url, e);
                throw new MalformedURLException("Failed to retrieve user page from " + url);
            }
        }
        p = Pattern.compile("^.*instagram\\.com/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/" + m.group(1));
        }
        p = Pattern.compile("^.*iconosquare\\.com/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/" + m.group(1));
        }
        p = Pattern.compile("^.*statigr\\.am/([a-zA-Z0-9\\-_.]{3,}).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return new URL("http://iconosquare.com/" + m.group(1));
        }
        throw new MalformedURLException("Expected username in URL (instagram.com/username and not " + url);
    }
    
    private URL getUserPageFromImage(URL url) throws IOException {
        Document doc = Http.url(url).get();
        for (Element element : doc.select("meta[property='og:description']")) {
            String content = element.attr("content");
            if (content.endsWith("'s photo on Instagram")) {
                return new URL("http://iconosquare/" + content.substring(0, content.indexOf("'")));
            }
        }
        throw new MalformedURLException("Expected username in URL (instagram.com/username and not " + url);
    }
    
    private String getUserID(URL url) throws IOException {
        logger.info("Retrieving " + url);
        this.sendUpdate(STATUS.LOADING_RESOURCE, url.toExternalForm());
        Document doc = Http.url(url).get();
        for (Element element : doc.select("input[id=user_public]")) {
            return element.attr("value");
        }
        throw new IOException("Unable to find userID at " + this.url);
    }

    @Override
    public void rip() throws IOException {
        String userID = getUserID(this.url);
        String baseURL = "http://iconosquare.com/controller_nl.php?action=getPhotoUserPublic&user_id=" + userID;
        String params = "";
        while (true) {
            String url = baseURL + params;
            this.sendUpdate(STATUS.LOADING_RESOURCE, url);
            logger.info("    Retrieving " + url);
            JSONObject json = Http.url(url).getJSON();
            JSONArray datas = json.getJSONArray("data");
            String nextMaxID = "";
            if (datas.length() == 0) {
                break;
            }
            for (int i = 0; i < datas.length(); i++) {
                JSONObject data = (JSONObject) datas.get(i);
                if (data.has("id")) {
                    nextMaxID = data.getString("id");
                }
                String imageUrl;
                if (data.has("videos")) {
                    imageUrl = data.getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
                } else if (data.has("images")) {
                    imageUrl = data.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
                } else {
                    continue;
                }
                addURLToDownload(new URL(imageUrl));
            }
            JSONObject pagination = json.getJSONObject("pagination");
            if (nextMaxID.equals("")) {
                if (!pagination.has("next_max_id")) {
                    break;
                } else {
                    nextMaxID = pagination.getString("next_max_id");
                }
            }
            params = "&max_id=" + nextMaxID;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next album:", e);
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
        Pattern p = Pattern.compile("^https?://iconosquare.com/([a-zA-Z0-9\\-_.]{3,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Unable to find user in " + url);
    }

}
