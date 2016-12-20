package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;

public class NewsfilterRipper extends AlbumRipper {
    private static final String HOST = "newsfilter";

    public NewsfilterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        //http://newsfilter.org/gallery/he-doubted-she-would-fuck-on-cam-happy-to-be-proven-wrong-216799
        Pattern p = Pattern.compile("^https?://([wm]+\\.)?newsfilter\\.org/gallery/.+$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String u = url.toExternalForm();
        if (u.indexOf('#') >= 0) {
            u = u.substring(0, u.indexOf('#'));
        }
        u = u.replace("https?://m\\.newsfilter\\.org", "http://newsfilter.org");
        return new URL(u);
    }

    @Override
    public void rip() throws IOException {
        String gid = getGID(this.url);
        String theurl = "http://newsfilter.org/gallery/" + gid;
        logger.info("Loading " + theurl);

        Connection.Response resp = Jsoup.connect(theurl)
            .timeout(5000)
            .referrer("")
            .userAgent(USER_AGENT)
            .method(Connection.Method.GET)
            .execute();
        Document doc = resp.parse();

        Elements thumbnails = doc.select("#galleryImages .inner-block img");
        for (Element thumb : thumbnails) {
            String thumbUrl = thumb.attr("src");
            String picUrl = thumbUrl.replace("thumbs/", "");
            addURLToDownload(new URL(picUrl));
        }

        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://([wm]+\\.)?newsfilter\\.org/gallery/([^/]+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        throw new MalformedURLException(
            "Expected newsfilter gallery format: http://newsfilter.org/gallery/galleryid" +
            " Got: " + url);
    }
}
