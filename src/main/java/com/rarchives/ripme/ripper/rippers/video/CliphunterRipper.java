package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;

public class CliphunterRipper extends VideoRipper {

    private static final String HOST = "cliphunter";
    private static final String decryptString="{'$':':','&':'.','(':'=','-':'-','_':'_','^':'&','a':'h','c':'c','b':'b','e':'v','d':'e','g':'f','f':'o','i':'d','m':'a','l':'n','n':'m','q':'t','p':'u','r':'s','w':'w','v':'p','y':'l','x':'r','z':'i','=':'/','?':'?'}";
    private static final JSONObject decryptDict = new JSONObject(decryptString);

    public CliphunterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wm.]*cliphunter\\.com/w/[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*cliphunter\\.com/w/([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected cliphunter format:"
                        + "cliphunter.com/w/####..."
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        LOGGER.info("Retrieving " + this.url);
        String html = Http.url(url).get().html();
        String jsonString = html.substring(html.indexOf("var flashVars = {d: '") + 21);
        jsonString = jsonString.substring(0, jsonString.indexOf("'"));
        JSONObject json    = new JSONObject(new String(Base64.decode(jsonString)));
        JSONObject jsonURL = new JSONObject(new String(Base64.decode(json.getString("url"))));
        String encryptedURL = jsonURL.getJSONObject("u").getString("l");
        String vidURL = "";
        for (char c : encryptedURL.toCharArray()) {
            if (decryptDict.has(Character.toString(c))) {
                vidURL += decryptDict.getString(Character.toString(c));
            }
            else {
                vidURL += c;
            }
        }
        addURLToDownload(new URI(vidURL).toURL(), HOST + "_" + getGID(this.url));
        waitForThreads();
    }
}