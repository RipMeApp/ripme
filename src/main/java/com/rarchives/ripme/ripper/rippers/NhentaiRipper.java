package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NhentaiRipper extends AbstractHTMLRipper {

    private String albumTitle;
    private Document firstPage;

    // Thread pool for finding direct image links from "image" pages (html)
    private DownloadThreadPool nhentaiThreadPool = new DownloadThreadPool("nhentai");

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("^https?://nhentai\\.net/tag/([a-zA-Z0-9_\\-]+)/?");
        Matcher ma = pa.matcher(url.toExternalForm());
        return ma.matches();
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select("a.cover")) {
            urlsToAddToQueue.add("https://" + getDomain() + elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return nhentaiThreadPool;
    }

    public NhentaiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "nhentai.net";
    }

    @Override
    public String getHost() {
        return "nhentai";
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        if (firstPage == null) {
            try {
                firstPage = Http.url(url).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String title = firstPage.select("#info > h1").text();
        if (title == null) {
            return getAlbumTitle(url);
        }
        return "nhentai" + title;
    }

    public List<String> getTags(Document doc) {
        List<String> tags = new ArrayList<>();
        for (Element tag : doc.select("a.tag")) {
            String tagString = tag.attr("href").replaceAll("/tag/", "").replaceAll("/", "");
            LOGGER.info("Found tag: " + tagString);
            tags.add(tagString);
        }
        return tags;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        // Ex: https://nhentai.net/g/159174/
        Pattern p = Pattern.compile("^https?://nhentai\\.net/g/(\\d+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // Return the text contained between () in the regex - 159174 in this case
            return m.group(1);
        }
        throw new MalformedURLException("Expected nhentai.net URL format: " +
                "nhentai.net/g/albumid - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (firstPage == null) {
            firstPage = Http.url(url).get();
        }

        String blacklistedTag = RipUtils.checkTags(Utils.getConfigStringArray("nhentai.blacklist.tags"), getTags(firstPage));
        if (blacklistedTag != null) {
            sendUpdate(RipStatusMessage.STATUS.DOWNLOAD_WARN, "Skipping " + url.toExternalForm() + " as it " +
                    "contains the blacklisted tag \"" + blacklistedTag + "\"");
            return null;
        }
        return firstPage;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Elements thumbs = page.select("a.gallerythumb > img");
        for (Element el : thumbs) {
            imageURLs.add(el.attr("data-src").replaceAll("://t", "://i").replaceAll("t\\.", "."));
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }


}
