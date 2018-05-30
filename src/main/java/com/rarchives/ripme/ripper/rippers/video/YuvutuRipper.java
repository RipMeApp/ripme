package com.rarchives.ripme.ripper.rippers.video;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.utils.Http;

public class YuvutuRipper extends VideoRipper {

    private static final String HOST = "yuvutu";

    public YuvutuRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^http://www\\.yuvutu\\.com/video/[0-9]+/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^http://www\\.yuvutu\\.com/video/[0-9]+/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected yuvutu format:"
                        + "yuvutu.com/video/####"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        logger.info("Retrieving " + this.url);
        Document doc = Http.url(url).get();
        Element iframe = doc.select("iframe").first();
        String iframeSrc = iframe.attr("src");
        if (iframeSrc != null) {
            doc = Http.url("http://www.yuvutu.com" + iframeSrc).get();
        } else {
            throw new IOException("Could not find iframe code at " + url);
        }
        Elements script = doc.select("script");
        if (script.isEmpty()) {
            throw new IOException("Could not find script code at " + url);
        }
        Pattern p = Pattern.compile("file: \"(.*?)\"");
        
        for (Element element : script) {
            Matcher m = p.matcher(element.data());
            if (m.find()){
                String vidUrl = m.group(1);
                addURLToDownload(new URL(vidUrl), HOST + "_" + getGID(this.url));
            }
        }
        waitForThreads();
    }
}
