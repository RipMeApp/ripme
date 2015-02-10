package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class ModelmayhemRipper extends AlbumRipper {

    private static final String DOMAIN = "modelmayhem.com",
                                HOST   = "modelmayhem";

    public ModelmayhemRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith(DOMAIN));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        Map<String,String> cookies  = null,
                           postData = new HashMap<String,String>();
        String gid = getGID(this.url),
               ref = "http://www.modelmayhem.com/" + gid;

        Response resp = null;
        String theurl = "http://www.modelmayhem.com/" + gid;
        logger.info("Loading " + theurl);
        resp = Jsoup.connect(theurl)
                    .timeout(5000)
                    .referrer("")
                    .userAgent(USER_AGENT)
                    .method(Method.GET)
                    .execute();
        cookies = resp.cookies();
        
        resp = Jsoup.connect("http://www.modelmayhem.com/includes/js/auth.php")
                .cookies(cookies)
                .ignoreContentType(true)
                .referrer(ref)
                .userAgent(USER_AGENT)
                .method(Method.GET)
                .execute();
        String authText = resp.parse().html();
        String mmservice = authText.substring(authText.indexOf("token = '") + 9);
        mmservice = mmservice.substring(0, mmservice.indexOf("'"));

        cookies.putAll(resp.cookies());

        cookies.put("worksafe", "0");
        theurl = "http://www.modelmayhem.com/services/photo_viewer/albums/" + gid;
        postData.put("MMSERVICE", mmservice);
        resp = Jsoup.connect(theurl)
                    .data(postData)
                    .cookies(cookies)
                    .referrer(ref)
                    .userAgent(USER_AGENT)
                    .method(Method.POST)
                    .execute();
        cookies.putAll(resp.cookies());

        theurl = "http://www.modelmayhem.com/services/photo_viewer/pictures/" + gid + "/0/0/1/0";
        this.sendUpdate(STATUS.LOADING_RESOURCE, theurl);
        logger.info("Loading " + theurl);
        resp = Jsoup.connect(theurl)
                    .data(postData)
                    .cookies(cookies)
                    .referrer(ref)
                    .userAgent(USER_AGENT)
                    .method(Method.POST)
                    .execute();

        Document doc = resp.parse();
        String jsonText = doc.body().html();
        jsonText = jsonText.replace("&quot;",  "\"");
        System.err.println(jsonText);
        JSONObject json = new JSONObject(jsonText);
        JSONArray pictures = json.getJSONArray("pictures");
        for (int i = 0; i < pictures.length(); i++) {
            JSONObject picture = pictures.getJSONObject(i);
            String bigImage = picture.getString("big_image");
            if (bigImage.trim().equals("")) {
                logger.info("Got empty image for " + picture.toString(2));
                continue;
            }
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", i + 1);
            }
            addURLToDownload(new URL(bigImage), prefix);

            if (isThisATest()) {
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
        Pattern p = Pattern.compile("^https?://[w.]*modelmayhem.com.*/([0-9]+)/?.*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Modelmayhem user ID not found in " + url + ", expected http://modelmayhem.com/userid");
    }

}
