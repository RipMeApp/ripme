package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rarchives.ripme.App.logger;

public class HentaidudeRipper extends AbstractSingleFileRipper {


    public HentaidudeRipper(URL url) throws IOException {
        super(url);
    }


    @Override
    public String getHost() {
        return "hentaidude";
    }

    @Override
    public String getDomain() {
        return "hentaidude.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://hentaidude\\.com/([a-zA-Z0-9_-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected hqporner URL format: " +
                "hentaidude.com/VIDEO - got " + url + " instead");
    }

    private String getVideoName() {
        try {
            return getGID(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Unable to get video title from " + url.toExternalForm());
            e.printStackTrace();
        }
        return "unknown";
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String videoPageUrl = "https:" + doc.select("div.videoWrapper > iframe").attr("src");
        Pattern p = Pattern.compile("sources\\[.video-source-\\d.\\] = .(https://cdn\\d.hentaidude.com/index.php\\?[a-zA-Z=0-9]+)");
        for (Element el : doc.select("script")) {
            Matcher m = p.matcher(el.html());
            if (m.find()) {
                result.add(m.group(1));
            }
        }
        return result;
    }

    @Override
    public boolean tryResumeDownload() {return true;}

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, "", "", "", null, getVideoName(), "mp4");
    }



}