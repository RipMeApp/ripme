package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;

public class NatalieMuRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(NatalieMuRipper.class);

    public int news_id = 0;

    public NatalieMuRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        String host = this.url.getHost();
        host = host.substring(0, host.lastIndexOf('.'));
        if (host.contains(".")) {
            // Host has subdomain (www)
            host = host.substring(host.lastIndexOf('.') + 1);
        }
        String board = this.url.toExternalForm().split("/")[3];
        return host + "_" + board;
    }

    @Override
    public boolean canRip(URL url) {
        //urls like:
        // http://cdn2.natalie.mu/music/gallery/show/news_id/xxxxxx/image_id/xxxxxx
        // http://cdn2.natalie.mu/music/news/140411
        return  url.toExternalForm().contains("natalie.mu")     // Most chans
             && (url.toExternalForm().contains("/news_id/")
             || url.toExternalForm().contains("/news/")); // 4chan, archive.moe
    }

    /**
     * For example the achrives are all known. (Check 4chan-x)
     * Should be based on the software the specific chan uses.
     * FoolFuuka uses the same (url) layout as 4chan
     * */
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        String u = url.toExternalForm();
        if (u.contains("/news_id/")) {
            p = Pattern.compile("/news_id/([0-9]+)/");
            m = p.matcher(u);
            if (m.find()) {
                return m.group(1);
            }
        } else if (u.contains("/news/")) {
            p = Pattern.compile("/news/([0-9]+)/?");
            m = p.matcher(u);
            if (m.find()) {
                return m.group(1);
            }
        }

        throw new MalformedURLException(
                "Expected natalie.mu URL formats: "
                        + "http://natalie.mu/music/news/xxxxxx or http://natalie.mu/music/gallery/show/news_id/xxxxxx/image_id/yyyyyy"
                        + " Got: " + u);
    }

    @Override
    public String getDomain() {
        return this.url.getHost();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Pattern p; Matcher m;
        //select all album thumbnails
        for (Element span : page.select(".NA_articleGallery span")) {
            if (!span.hasAttr("style")) {
                continue;
            }
            String style = span.attr("style").trim();

            p = Pattern.compile("background-image: url\\((.*list_thumb_inbox.*)\\);", Pattern.CASE_INSENSITIVE);
            m = p.matcher(style);
            if (m.find()) {
                String imgUrl = m.group(1);
                if (imgUrl.startsWith("//")) {
                    imgUrl = "http:" + imgUrl;
                }
                if (imgUrl.startsWith("/")) {
                    imgUrl = "http://" + this.url.getHost() + imgUrl;
                }
                //convert thumbnail url into fullsize url
                imgUrl = imgUrl.replace("list_thumb_inbox","xlarge");
                // Don't download the same URL twice
                if (imageURLs.contains(imgUrl)) {
                    logger.debug("Already attempted: " + imgUrl);
                    continue;
                }
                imageURLs.add(imgUrl);
                if (isThisATest()) {
                    break;
                }
            }

            if (isStopped()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toString(), null);
    }
}
