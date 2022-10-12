package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.AbstractSingleFileRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DuckmoviesRipper extends AbstractSingleFileRipper {
    public DuckmoviesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("https?://[a-zA-Z0-9]+.[a-zA-Z]+/(models|category)/([a-zA-Z0-9_-])+/?");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return true;
        }
        pa = Pattern.compile("https?://[a-zA-Z0-9]+.[a-zA-Z]+/(models|category)/([a-zA-Z0-9_-])+/page/\\d+/?");
        ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select(".post > li > div > div > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }


    private static List<String> explicit_domains = Arrays.asList(
            "vidporntube.fun",
            "pornbj.fun",
            "iwantporn.fun",
            "neoporn.fun",
            "yayporn.fun",
            "freshporn.co",
            "palapaja.stream",
            "freshporn.co",
            "pornvidx.fun",
            "palapaja.com"
            );

    @Override
    public String getHost() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public String getDomain() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public boolean canRip(URL url) {
        String url_name = url.toExternalForm();
        return explicit_domains.contains(url_name.split("/")[2]);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        String duckMoviesUrl = doc.select("iframe").attr("src");
        try {
            Document duckDoc = Http.url(new URL(duckMoviesUrl)).get();
            String videoURL = duckDoc.select("source").attr("src");
            // remove any white spaces so we can download the movie without a 400 error
            videoURL = videoURL.replaceAll(" ", "%20");
            results.add(videoURL);
        } catch (MalformedURLException e) {
            LOGGER.error(duckMoviesUrl + " is not a valid url");
        } catch (IOException e) {
            LOGGER.error("Unable to load page " + duckMoviesUrl);
            e.printStackTrace();
        }
        return results;
    }


    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://[a-zA-Z0-9]+\\.[a-zA-Z]+/([a-zA-Z0-9\\-_]+)/?");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("https?://[a-zA-Z0-9]+.[a-zA-Z]+/(category|models)/([a-zA-Z0-9_-])+/?");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("https?://[a-zA-Z0-9]+.[a-zA-Z]+/(category|models)/([a-zA-Z0-9_-])+/page/\\d+");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected duckmovies format:"
                        + "domain.tld/Video-title"
                        + " Got: " + url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, "", "", null, null, null);
    }

    @Override
    public boolean tryResumeDownload() {return true;}
}
