package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class PornhubRipper extends VideoRipper {

    private static final Logger logger = LogManager.getLogger(PornhubRipper.class);

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
    public void rip() throws IOException, URISyntaxException {
        String vidUrl = "";
        logger.info("    Retrieving " + this.url.toExternalForm());
        Document doc = Http.url(this.url).get();
        String html = doc.body().html();
        html = StringEscapeUtils.unescapeJavaScript(html);
        html = html.substring(html.indexOf("var ra"));
        html = html.substring(0, html.indexOf('\n'));
        html = html.replaceAll("\\/\\*([\\S\\s]+?)\\*\\/", ""); // Delete JS comments from the String

        String varName;
        String varValue;
        int nextEqual;
        int nextSemicolonSpace;
        HashMap<String, String> vars = new HashMap<>();
        HashMap<String, String> qualityMap = new HashMap<>();
        ArrayList<String> urlArray = new ArrayList<>();

        for (int i = 0; i < 4; i++) { // Max. 4 loops for 240p, 480p, 720p, 1080p

            // Put every of the (unsorted) variables with their corresponding values in a HashMap
            while (html.startsWith("var ra")) {
                nextEqual = html.indexOf('=');
                nextSemicolonSpace = html.indexOf(';');
                varName = html.substring(4,nextEqual);
                varValue = html.substring(nextEqual + 1, nextSemicolonSpace);
                // Remove """ and " + " from varValue
                varValue = varValue.replaceAll("\"", "");
                varValue = varValue.replaceAll(" \\+ ", "");
                vars.put(varName, varValue);
                html = html.substring(nextSemicolonSpace + 1);
            }

            // put every variable's name in an ArrayList
            if (html.startsWith("var quality")) {
                int next = 3;
                nextEqual = html.indexOf('=');
                urlArray.add(html.substring(12, nextEqual - 1)); // Get numeric value of the video's resolution to compare it later
                html = html.substring(nextEqual + 1);
                while (html.startsWith("ra")) {
                    nextSemicolonSpace = html.indexOf(' ');
                    if (nextSemicolonSpace > html.indexOf(';')) {
                        nextSemicolonSpace = html.indexOf(';');
                        next = 1;
                    }
                    varName = html.substring(0, nextSemicolonSpace);
                    urlArray.add(varName);
                    html = html.substring(nextSemicolonSpace + next);
                }
            }

            // Put together vidURL by matching the variable's names with the corresponding value from the vars-Map
            for (int k = 1; k < urlArray.size(); k++) {
                varName = urlArray.get(k);
                Iterator<Map.Entry<String, String>> iterator = vars.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    if (varName.equals(entry.getKey())) {
                        vidUrl = vidUrl + entry.getValue();
                        iterator.remove();
                        break;
                    }
                }
            }

            qualityMap.put(urlArray.get(0), vidUrl);
            vidUrl = ""; // Delete content of vidURL
            urlArray.clear();

            // Delete "flashvars" because it's not needed
            if (html.startsWith("flashvars")) {
                nextSemicolonSpace = html.indexOf(';');
                html = html.substring(nextSemicolonSpace + 1);
            }
        }

        // Get URL of highest quality version
        int bestQuality = 0;
        int currentQuality;
        for (Map.Entry<String, String> entry : qualityMap.entrySet()) {
            currentQuality = Integer.parseInt(entry.getKey());
            if (currentQuality > bestQuality) {
                bestQuality = currentQuality;
                vidUrl = entry.getValue();
            }
        }

        if (vidUrl.equals("")) {
            throw new IOException("Unable to find encrypted video URL at " + this.url);
        }
        addURLToDownload(new URI(vidUrl).toURL(), HOST + "_" + bestQuality + "p_" + getGID(this.url));

        waitForThreads();
    }
}
