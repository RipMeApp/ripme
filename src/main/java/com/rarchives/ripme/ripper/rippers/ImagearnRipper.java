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

public class ImagearnRipper extends AbstractHTMLRipper {

    public ImagearnRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "imagearn";
    }
    @Override
    public String getDomain() {
        return "imagearn.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagearn.com/+gallery.php\\?id=([0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected imagearn.com gallery formats: "
                        + "imagearn.com/gallery.php?id=####..."
                        + " Got: " + url);
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagearn.com/+image.php\\?id=[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // URL points to imagearn *image*, not gallery
            try {
                url = getGalleryFromImage(url);
            } catch (Exception e) {
                LOGGER.error("[!] " + e.getMessage(), e);
            }
        }
        return url;
    }

    private URL getGalleryFromImage(URL url) throws IOException {
        Document doc = Http.url(url).get();
        for (Element link : doc.select("a[href~=^gallery\\.php.*$]")) {
            LOGGER.info("LINK: " + link.toString());
            if (link.hasAttr("href")
                    && link.attr("href").contains("gallery.php")) {
                url = new URL("http://imagearn.com/" + link.attr("href"));
                LOGGER.info("[!] Found gallery from given link: " + url);
                return url;
            }
        }
        throw new IOException("Failed to find gallery at URL " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            Document doc = getFirstPage();
            String title = doc.select("h3 > strong").first().text(); // profile name
            return getHost() + "_" + title + "_" + getGID(url);
        } catch (Exception e) {
            // Fall back to default album naming convention
            LOGGER.warn("Failed to get album title from " + url, e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div#gallery > div > a")) {
            String imageURL = thumb.attr("href");
            try {
                Document imagedoc = new Http("http://imagearn.com/" + imageURL).get();
                String image = imagedoc.select("a.thickbox").first().attr("href");
                imageURLs.add(image);
            } catch (IOException e) {
                LOGGER.warn("Was unable to download page: " + imageURL);
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
        sleep(1000);
    }
}
