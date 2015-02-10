package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

public class GifyoRipper extends AbstractHTMLRipper {

    private int page = 0;
    private Map<String,String> cookies = new HashMap<String,String>();

    public GifyoRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "gifyo";
    }
    @Override
    public String getDomain() {
        return "gifyo.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[w.]*gifyo.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Gifyo user not found in " + url + ", expected http://gifyo.com/username");
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL("http://gifyo.com/" + getGID(url) + "/");
    }
    
    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url(this.url)
                            .ignoreContentType()
                            .response();
        cookies = resp.cookies();

        Document doc = resp.parse();
        if (doc.html().contains("profile is private")) {
            sendUpdate(STATUS.RIP_ERRORED, "User has private profile");
            throw new IOException("User has private profile");
        }
        return doc;
    }
    
    @Override
    public Document getNextPage(Document doc) throws IOException {
        page++;
        Map<String,String> postData = new HashMap<String,String>();
        postData.put("cmd", "refreshData");
        postData.put("view", "gif");
        postData.put("layout", "grid");
        postData.put("page", Integer.toString(page));
        Response resp = Http.url(this.url)
                            .ignoreContentType()
                            .data(postData)
                            .cookies(cookies)
                            .method(Method.POST)
                            .response();
        cookies.putAll(resp.cookies());
        Document nextDoc = resp.parse();
        if (nextDoc.select("div.gif img").size() == 0) {
            throw new IOException("No more images found");
        }
        sleep(2000);
        return nextDoc;
    }
    
    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        for (Element image : doc.select("img.profile_gif")) {
            String imageUrl = image.attr("data-animated");
            if (imageUrl.startsWith("//")) {
                imageUrl = "http:" + imageUrl;
            }
            imageUrl = imageUrl.replace("/medium/", "/large/");
            imageUrl = imageUrl.replace("_s.gif", ".gif");
            imageURLs.add(imageUrl);
        }
        logger.debug("Found " + imageURLs.size() + " images");
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
    }
}
