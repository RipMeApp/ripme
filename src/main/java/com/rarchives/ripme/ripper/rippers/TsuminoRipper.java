package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;
import org.json.JSONArray;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;


import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Element;

public class TsuminoRipper extends AbstractHTMLRipper {
    private Map<String,String> cookies = new HashMap<>();

    public TsuminoRipper(URL url) throws IOException {
        super(url);
    }

    public List<String> getTags(Document doc) {
        List<String> tags = new ArrayList<>();
        LOGGER.info("Getting tags");
        for (Element tag : doc.select("div#Tag > a")) {
            LOGGER.info("Found tag " + tag.text());
            tags.add(tag.text().toLowerCase());
        }
        return tags;
    }

    private ArrayList<String> getPageUrls(int numPages) {
        // Get the api access token from the index page
        ArrayList<String> urlsToReturn = new ArrayList<String>();

        try {
            LOGGER.debug("Getting index");
            cookies.put("ASP.NET_SessionId","c4rbzccf0dvy3e0cloolmlkq");
//   IMPORTANT NOTE: If you request "https://www.tsumino.com/Read/Index/" + getAlbumID() the server was temp ban you! Always request /Read/Index/ID?page=1 like the web ui does
            Document page = Http.url(new URL("https://www.tsumino.com/Read/Index/" + getAlbumID() + "?page=1")).referrer("https://www.tsumino.com/Read/Index/" + getAlbumID()).cookies(cookies).get();
            LOGGER.info(page);
            String endPoint = page.select("div#image-container").attr("data-cdn");
            for (int i = 1; i <= numPages; i++) {
                urlsToReturn.add(endPoint.replaceAll("\\[PAGE\\]", String.valueOf(i)));
            }
        } catch (IOException e) {
            LOGGER.info(e);
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_ERRORED, "Unable to download album, please compete the captcha at http://www.tsumino.com/Read/Auth/"
                    + getAlbumID() + " and try again");
            return null;
        }
        return urlsToReturn;
    }

    @Override
    public String getHost() {
        return "tsumino";
    }

    @Override
    public String getDomain() {
        return "tsumino.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www.tsumino.com/Book/Info/([0-9]+)/([a-zA-Z0-9_-]*)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "_" + m.group(2);
        }
        p = Pattern.compile("https?://www.tsumino.com/Book/Info/([0-9]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("https?://www.tsumino.com/entry/([0-9]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected tsumino URL format: " +
                "tsumino.com/entry/ID - got " + url + " instead");
    }

    private String getAlbumID() {
        Pattern p = Pattern.compile("https?://www.tsumino.com/Book/Info/([0-9]+)/\\S*");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("https?://www.tsumino.com/entry/([0-9]+)/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public Document getFirstPage() throws IOException {
        Connection.Response resp = Http.url(url).response();
        cookies.putAll(resp.cookies());
        Document doc =  resp.parse();
        String blacklistedTag = RipUtils.checkTags(Utils.getConfigStringArray("tsumino.blacklist.tags"), getTags(doc));
        if (blacklistedTag != null) {
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Skipping " + url.toExternalForm() + " as it " +
                    "contains the blacklisted tag \"" + blacklistedTag + "\"");
            return null;
        }
        return doc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {

        return getPageUrls(Integer.parseInt(doc.select("div#Pages").text()));
    }

    @Override
    public void downloadURL(URL url, int index) {
        sleep(1000);
        /*
        There is no way to tell if an image returned from tsumino.com is a png to jpg. The content-type header is always
        "image/jpeg" even when the image is a png. The file ext is not included in the url.
         */
        addURLToDownload(url, getPrefix(index), "", null, null, null, null, true);
    }
}
