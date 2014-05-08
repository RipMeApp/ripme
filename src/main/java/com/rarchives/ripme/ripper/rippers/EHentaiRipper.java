package com.rarchives.ripme.ripper.rippers;

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

public class EHentaiRipper extends AlbumRipper {
    private static final String DOMAIN = "g.e-hentai.org", HOST = "e-hentai";
    private static final Logger logger = Logger.getLogger(EHentaiRipper.class);

    private Document albumDoc = null;

    private URL prevUrl = null;
    private URL cursorUrl = null;
    private Document cursorDoc = null;

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

        p = Pattern.compile("^.*g\\.e-hentai\\.org/g/[0-9]+/[a-fA-F0-9]+)/$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException(
                "Expected g.e-hentai.org gallery format: "
                        + "http://g.e-hentai.org/g/####/####/"
                        + " Got: " + url);
    }

    @Override
    public void rip() throws IOException {
        int index = 0;
        logger.info("    Retrieving " + this.url.toExternalForm());
        if (albumDoc == null) {
            albumDoc = Jsoup.connect(this.url.toExternalForm()).get();
        }
        if (cursorDoc == null) {
            Elements select = albumDoc.select("#gdt > .gdtm");
            Element first = select.first();
            String href = first.select("a").attr("href");
            cursorUrl = new URL(href);
            System.out.println(cursorUrl);
        }

        while (!cursorUrl.equals(prevUrl)) {
            cursorDoc = Jsoup.connect(this.cursorUrl.toExternalForm()).get();

            Elements a = cursorDoc.select(".sni > a");
            Elements img = a.select("img");

            String imgsrc = img.attr("src");
            addURLToDownload(new URL(imgsrc), String.format("%03d_", index));

            String href = a.attr("href");

            prevUrl = cursorUrl;
            cursorUrl = new URL(href);

            index++;
        }

        waitForThreads();
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }
}