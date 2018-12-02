package com.rarchives.ripme.ripper.rippers;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.rarchives.ripme.utils.Http;


public class GfycatRipper extends AbstractSingleFileRipper {

    private static final String HOST = "gfycat.com";

    public GfycatRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "gfycat.com";
    }

    @Override
    public String getHost() {
        return "gfycat";
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(HOST);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        url = new URL(url.toExternalForm().replace("/gifs/detail", ""));
        
        return url;
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[wm.]*gfycat\\.com/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected gfycat.com format:"
                        + "gfycat.com/id"
                        + " Got: " + url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Elements videos = doc.select("source");
        String vidUrl = videos.first().attr("src");
        if (vidUrl.startsWith("//")) {
            vidUrl = "http:" + vidUrl;
        }
        result.add(vidUrl);
        return result;
    }

    /**
     * Helper method for retrieving video URLs.
     * @param url URL to gfycat page
     * @return URL to video
     * @throws IOException
     */
    public static String getVideoURL(URL url) throws IOException {
        LOGGER.info("Retrieving " + url.toExternalForm());

        //Sanitize the URL first
        url = new URL(url.toExternalForm().replace("/gifs/detail", ""));

        Document doc = Http.url(url).get();
        Elements videos = doc.select("source");
        if (videos.isEmpty()) {
            throw new IOException("Could not find source at " + url);
        }
        String vidUrl = videos.first().attr("src");
        if (vidUrl.startsWith("//")) {
            vidUrl = "http:" + vidUrl;
        }
        return vidUrl;
    }
}