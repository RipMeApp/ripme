package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class TeenplanetRipper extends AlbumRipper {

    private static final String DOMAIN = "teenplanet.org",
                                HOST   = "teenplanet";

    private Document albumDoc = null;

    public TeenplanetRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                albumDoc = Http.url(url).get();
            }
            Elements elems = albumDoc.select("div.header > h2");
            return HOST + "_" + elems.get(0).text();
        } catch (Exception e) {
            // Fall back to default album naming convention
            e.printStackTrace();
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*teenplanet.org/galleries/([a-zA-Z0-9\\-]+).html$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected teenplanet.org gallery format: "
                        + "teenplanet.org/galleries/....html"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        LOGGER.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        for (Element thumb : albumDoc.select("#galleryImages > a > img")) {
            if (!thumb.hasAttr("src")) {
                continue;
            }
            String image = thumb.attr("src");
            image = image.replace(
                    "/thumbs/",
                    "/");
            index += 1;
            String prefix = "";
            if (Utils.getConfigBoolean("download.save_order", true)) {
                prefix = String.format("%03d_", index);
            }
            addURLToDownload(new URL(image), prefix);
            if (isThisATest()) {
                break;
            }
        }
        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

}