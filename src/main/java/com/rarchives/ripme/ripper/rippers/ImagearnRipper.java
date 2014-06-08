package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class ImagearnRipper extends AlbumRipper {

    private static final String DOMAIN = "imagearn.com",
                                HOST   = "imagearn";

    public ImagearnRipper(URL url) throws IOException {
        super(url);
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagearn.com/{1,}image.php\\?id=[0-9]{1,}.*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            // URL points to imagearn *image*, not gallery
            try {
                url = getGalleryFromImage(url);
            } catch (Exception e) {
                logger.error("[!] " + e.getMessage(), e);
            }
        }
        return url;
    }

    private URL getGalleryFromImage(URL url) throws IOException {
        Document doc = Jsoup.connect(url.toExternalForm()).get();
        for (Element link : doc.select("a[href~=^gallery\\.php.*$]")) {
            logger.info("LINK: " + link.toString());
            if (link.hasAttr("href")
                    && link.attr("href").contains("gallery.php")) {
                url = new URL("http://imagearn.com/" + link.attr("href"));
                logger.info("[!] Found gallery from given link: " + url);
                return url;
            }
        }
        throw new IOException("Failed to find gallery at URL " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        logger.info("[ ] Retrieving " + this.url.toExternalForm());
        Document doc = Jsoup.connect(url.toExternalForm()).get();
        for (Element thumb : doc.select("img.border")) {
            String image = thumb.attr("src");
            image = image.replaceAll("thumbs[0-9]*\\.imagearn\\.com/", "img.imagearn.com/imags/");
            index += 1;
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(new URL(image), prefix);
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*imagearn.com/{1,}gallery.php\\?id=([0-9]{1,}).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected imagearn.com gallery formats: "
                        + "imagearn.com/gallery.php?id=####..."
                        + " Got: " + url);
    }
}
