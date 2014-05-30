package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Utils;

public class EHentaiRipper extends AlbumRipper {
    private static final String DOMAIN = "g.e-hentai.org", HOST = "e-hentai";
    private static final Logger logger = Logger.getLogger(EHentaiRipper.class);

    private Document albumDoc = null;

    public EHentaiRipper(URL url) throws IOException {
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
                albumDoc = Jsoup.connect(url.toExternalForm()).get();
            }
            Elements elems = albumDoc.select("#gn");
            return HOST + "_" + elems.get(0).text();
        } catch (Exception e) {
            // Fall back to default album naming convention
            e.printStackTrace();
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        System.out.println(url);

        p = Pattern.compile("^.*g\\.e-hentai\\.org/g/([0-9]+)/([a-fA-F0-9]+)/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2);
        }

        throw new MalformedURLException(
                "Expected g.e-hentai.org gallery format: "
                        + "http://g.e-hentai.org/g/####/####/"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        if (albumDoc == null) {
            logger.info("    Retrieving " + this.url.toExternalForm());
            albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        }
        Elements select = albumDoc.select("#gdt > .gdtm");
        Element first = select.first();
        String href = first.select("a").attr("href");
        if (href.equals("")) {
            throw new IOException("Could not find 'href' inside elements under #gdt > .gdtm > a");
        }
        URL cursorUrl = new URL(href), prevUrl = null;

        while (!cursorUrl.equals(prevUrl)) {
            prevUrl = cursorUrl;
            Document cursorDoc = Jsoup.connect(cursorUrl.toExternalForm())
                                      .userAgent(USER_AGENT)
                                      .get();

            Elements a = cursorDoc.select(".sni > a");
            Elements images = a.select("img");
            if (images.size() == 0) {
                logger.info("cursorDoc: " + cursorDoc.toString());
                logger.error("No images found at " + cursorUrl);
                break;
            }

            String imgsrc = images.get(0).attr("src");
            if (imgsrc.equals("")) {
                logger.warn("Image URL is empty via " + images.get(0));
                continue;
            }
            logger.info("Found URL " + imgsrc + " via " + images.get(0));
            Pattern p = Pattern.compile("^http://.*/ehg/image.php.*&n=([^&]+).*$");
            Matcher m = p.matcher(imgsrc);
            if (m.matches()) {
                // Manually discover filename from URL
                String savePath = this.workingDir + File.separator;
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    savePath += String.format("%03d_", index + 1);
                }
                savePath += m.group(1);
                addURLToDownload(new URL(imgsrc), new File(savePath));
            }
            else {
                // Provide prefix and let the AbstractRipper "guess" the filename
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index + 1);
                }
                addURLToDownload(new URL(imgsrc), prefix);
            }

            String nextUrl = a.attr("href");
            if (nextUrl.equals("")) {
                logger.warn("Next page URL is empty, via " + a);
                break;
            }
            cursorUrl = new URL(nextUrl);

            index++;
        }

        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }
}