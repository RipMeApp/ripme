package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BooruRipper extends AbstractHTMLRipper {
    private static final Logger logger = LogManager.getLogger(BooruRipper.class);

    private static Pattern gidPattern = null;

    public BooruRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        if (url.toExternalForm().contains("xbooru") || url.toExternalForm().contains("gelbooru")) {
            return true;
        }
        return false;

    }

    @Override
    public String getHost() {
        logger.info(url.toExternalForm().split("/")[2]);
        return url.toExternalForm().split("/")[2].split("\\.")[0];
    }

    @Override
    public String getDomain() {
        return url.toExternalForm().split("/")[2];
    }

    private String getPage(int num) throws MalformedURLException {
        return "http://" + getHost() + ".com/index.php?page=dapi&s=post&q=index&pid=" + num + "&tags=" + getTerm(url);

    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(getPage(0)).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        int offset = Integer.parseInt(doc.getElementsByTag("posts").first().attr("offset"));
        int num = Integer.parseInt(doc.getElementsByTag("posts").first().attr("count"));

        if (offset + 100 > num) {
            return null;
        }

        return Http.url(getPage(offset / 100 + 1)).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> res = new ArrayList<>(100);
        for (Element e : page.getElementsByTag("post")) {
            res.add(e.absUrl("file_url") + "#" + e.attr("id"));
        }
        return res;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, Utils.getConfigBoolean("download.save_order", true) ? url.getRef() + "-" : "");
    }

    private String getTerm(URL url) throws MalformedURLException {
        if (gidPattern == null) {
            gidPattern = Pattern.compile("^https?://(www\\.)?(x|gel)booru\\.com/(index.php)?.*([?&]tags=([a-zA-Z0-9$_.+!*'(),%-]+))(&|(#.*)?$)");
        }

        Matcher m = gidPattern.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(4);
        }

        throw new MalformedURLException("Expected xbooru.com URL format: " + getHost() + ".com/index.php?tags=searchterm - got " + url + " instead");
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        try {
            // Get the search term and make it filesystem safe
            String term = getTerm(url).replaceAll("&tags=", "");
            return Utils.filesystemSafe(term);
        } catch (Exception ex) {
            logger.error("Error getting GID from URL: " + url, ex);
        }

        throw new MalformedURLException("Expected xbooru.com URL format: " + getHost() + ".com/index.php?tags=searchterm - got " + url + " instead");
    }
}