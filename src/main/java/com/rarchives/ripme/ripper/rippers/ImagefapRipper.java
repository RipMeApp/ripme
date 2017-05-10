package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ImagefapRipper extends AbstractHTMLRipper {

    private Document albumDoc = null;
    private boolean isNewAlbumType = false;

    public ImagefapRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imagefap";
    }
    @Override
    public String getDomain() {
        return "imagefap.com";
    }

    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String gid = getGID(url);
        String newURL = "http://www.imagefap.com/gallery.php?";
        if (isNewAlbumType) {
            newURL += "p";
        }
        newURL += "gid=" + gid + "&view=2";
        logger.debug("Changed URL from " + url + " to " + newURL);
        return new URL(newURL);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*imagefap.com/gallery.php\\?pgid=([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/gallery.php\\?gid=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/pictures/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/pictures/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }

        p = Pattern.compile("^.*imagefap.com/gallery/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*imagefap.com/gallery/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected imagefap.com gallery formats: "
                        + "imagefap.com/gallery.php?gid=####... or "
                        + "imagefap.com/pictures/####..."
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextURL = null;
        for (Element a : doc.select("a.link3")) {
            if (a.text().contains("next")) {
                nextURL = "http://imagefap.com/gallery.php" + a.attr("href");
                break;
            }
        }
        if (nextURL == null) {
            throw new IOException("No next page found");
        }
        sleep(1000);
        return Http.url(nextURL).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replaceAll(
                    "http://x.*.fap.to/images/thumb/",
                    "http://fap.to/images/full/");
            image = image.replaceAll("w[0-9]{1,}-h[0-9]{1,}/", "");
            imageURLs.add(image);
            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer for image downloads
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            String title = getFirstPage().title();
            Pattern p = Pattern.compile("^Porn pics of (.*) \\(Page 1\\)$");
            Matcher m = p.matcher(title);
            if (m.matches()) {
                return getHost() + "_" + m.group(1);
            }
        } catch (IOException e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }

}