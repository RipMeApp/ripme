package com.rarchives.ripme.ripper.rippers;


import com.rarchives.ripme.ripper.AlbumRipper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            u = u.substring(0,  u.indexOf('#'));
        }
        u = u.replace("https?://m\\.newsfilter\\.org", "http://newsfilter.org");
        return new URL(u);
    }

    @Override
    public void rip() throws IOException {
        String gid = getGID(this.url),
                theurl = "http://newsfilter.org/gallery/" + gid;

        Connection.Response resp = null;
        logger.info("Loading " + theurl);
        resp = Jsoup.connect(theurl)
                .timeout(5000)
                .referrer("")
                .userAgent(USER_AGENT)
                .method(Connection.Method.GET)
                .execute();

        Document doc = resp.parse();
        //Element gallery  = doc.getElementById("thegalmain");
        //Elements piclinks = gallery.getElementsByAttributeValue("itemprop","contentURL");
        Pattern pat = Pattern.compile(gid+"/\\d+");
        Elements piclinks = doc.getElementsByAttributeValueMatching("href", pat);
        for (Element picelem : piclinks) {
            String picurl = "http://newsfilter.org"+picelem.attr("href");
            logger.info("Getting to picture page: "+picurl);
            resp = Jsoup.connect(picurl)
                    .timeout(5000)
                    .referrer(theurl)
                    .userAgent(USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();
            Document picdoc = resp.parse();
            String dlurl = picdoc.getElementsByAttributeValue("itemprop","contentURL").first().attr("src");
            addURLToDownload(new URL(dlurl));
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
        throw new MalformedURLException("Expected newsfilter gallery format: "
                                        + "http://newsfilter.org/gallery/galleryid"
                                        + " Got: " + url);
    }

}
