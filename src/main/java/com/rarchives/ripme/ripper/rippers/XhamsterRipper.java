package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class XhamsterRipper extends AlbumRipper {

    private static final String HOST = "xhamster";

    public XhamsterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://[wmde.]*xhamster\\.com/photos/gallery/[0-9]+.*$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        String nextURL = this.url.toExternalForm();
        while (nextURL != null) {
            logger.info("    Retrieving " + nextURL);
            Document doc = Http.url(nextURL).get();
            for (Element thumb : doc.select("table.iListing div.img img")) {
                if (!thumb.hasAttr("src")) {
                    continue;
                }
                String image = thumb.attr("src");
                // replace thumbnail urls with the urls to the full sized images
                image = image.replaceAll(
                        "https://upt.xhcdn\\.",
                        "http://up.xhamster.");
                image = image.replaceAll("ept\\.xhcdn", "ep.xhamster");
                image = image.replaceAll(
                        "_160\\.",
                        "_1000.");
                // Xhamster has shitty cert management and uses the wrong cert for their ep.xhamster Domain
                // so we change all https requests to http
                image = image.replaceAll(
                            "https://",
                            "http://");
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
            if (isThisATest()) {
                break;
            }
            nextURL = null;
            for (Element element : doc.select("a.last")) {
                nextURL = element.attr("href");
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
        Pattern p = Pattern.compile("^https?://([a-z0-9.]*?)xhamster\\.com/photos/gallery/([0-9]{1,})/.*\\.html");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }
        throw new MalformedURLException(
                "Expected xhamster.com gallery formats: "
                        + "xhamster.com/photos/gallery/#####/xxxxx..html"
                        + " Got: " + url);
    }

}
