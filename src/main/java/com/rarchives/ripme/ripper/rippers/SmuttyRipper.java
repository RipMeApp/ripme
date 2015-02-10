package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;

/**
 * Appears to be broken as of 2015-02-11.
 * Generating large image from thumbnail requires replacing "/m/" with something else:
 * -> Sometimes "/b/"
 * -> Sometimes "/p/"
 * No way to know without loading the image page.
 */
public class SmuttyRipper extends AlbumRipper {

    private static final String DOMAIN = "smutty.com",
                                HOST   = "smutty";

    public SmuttyRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return (url.getHost().endsWith(DOMAIN));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        int page = 0;
        String url, tag = getGID(this.url);
        boolean hasNextPage = true;
        while (hasNextPage) {
            if (isStopped()) {
                break;
            }
            page++;
            url = "http://smutty.com/h/" + tag + "/?q=%23" + tag + "&page=" + page + "&sort=date&lazy=1";
            this.sendUpdate(STATUS.LOADING_RESOURCE, url);
            logger.info("    Retrieving " + url);
            Document doc;
            try {
                doc = Http.url(url)
                          .ignoreContentType()
                          .get();
            } catch (IOException e) {
                if (e.toString().contains("Status=404")) {
                    logger.info("No more pages to load");
                } else {
                    logger.warn("Exception while loading " + url, e);
                }
                break;
            }
            for (Element image : doc.select("a.l > img")) {
                if (isStopped()) {
                    break;
                }
                String imageUrl = image.attr("src");

                // Construct direct link to image based on thumbnail
                StringBuilder sb = new StringBuilder();
                String[] fields = imageUrl.split("/");
                for (int i = 0; i < fields.length; i++) {
                    if (i == fields.length - 2 && fields[i].equals("m")) {
                        fields[i] = "b";
                    }
                    sb.append(fields[i]);
                    if (i < fields.length - 1) {
                        sb.append("/");
                    }
                }
                imageUrl = sb.toString();
                addURLToDownload(new URL(imageUrl));
            }
            if (doc.select("#next").size() == 0) {
                break; // No more pages
            }
            // Wait before loading next page
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("[!] Interrupted while waiting to load next album:", e);
                break;
            }
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://smutty\\.com/h/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^https?://[wm.]*smutty\\.com/search/\\?q=([a-zA-Z0-9\\-_%]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1).replace("%23", "");
        }
        throw new MalformedURLException("Expected tag in URL (smutty.com/h/tag and not " + url);
    }

}
