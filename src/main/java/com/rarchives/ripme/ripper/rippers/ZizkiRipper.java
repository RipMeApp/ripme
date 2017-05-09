package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class ZizkiRipper extends AbstractHTMLRipper {

    private Document albumDoc = null;
    private Map<String,String> cookies = new HashMap<String,String>();

    public ZizkiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "zizki";
    }
    @Override
    public String getDomain() {
        return "zizki.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?zizki\\.com/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.zizki.com/author/albumname, got: " + url);
        }
        return m.group(m.groupCount());
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Element titleElement = getFirstPage().select("meta[name=description]").first();
            String title = titleElement.attr("content");
            title = title.substring(title.lastIndexOf('/') + 1);

            Element authorSpan = getFirstPage().select("span[class=creator]").first();
            String author = authorSpan.select("a").first().text();
            logger.debug("Author: " + author);
            return getHost() + "_" + author + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            Response resp = Http.url(url).response();
            cookies.putAll(resp.cookies());
            albumDoc = resp.parse();
        }
        return albumDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<String>();
        // Page contains images
        logger.info("Look for images.");
        for (Element thumb : page.select("img")) {
            logger.info("Img");
            if (super.isStopped()) break;
            // Find thumbnail image source
            String image = null;
            String img_type = null;
            String src = null;
            if (thumb.hasAttr("typeof")) {
                img_type = thumb.attr("typeof");
                if (img_type.equals("foaf:Image")) {
                  logger.debug("Found image with " + img_type);
                  if (thumb.parent() != null &&
                      thumb.parent().parent() != null &&
                      thumb.parent().parent().attr("class") != null &&
                      thumb.parent().parent().attr("class").equals("aimage-center")
                     )
                  {
                     src = thumb.attr("src");
                     logger.debug("Found url with " + src);
                     if (!src.contains("zizki.com")) {
                       continue;
                     } else {
                       imageURLs.add(src.replace("/styles/medium/public/","/styles/large/public/"));
                     }
                   }
                }
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), cookies);
    }

    @Override
    public String getPrefix(int index) {
        return String.format("%03d_", index);
    }
}
