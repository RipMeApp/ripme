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

public class XhamsterRipper extends AbstractHTMLRipper {

    public XhamsterRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "xhamster";
    }

    @Override
    public String getDomain() {
        return "xhamster.com";
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String URLToReturn = url.toExternalForm();
        URLToReturn = URLToReturn.replaceAll("m.xhamster.com", "xhamster.com");
        URLToReturn = URLToReturn.replaceAll("\\w\\w.xhamster.com", "xhamster.com");
        URL san_url = new URL(URLToReturn.replaceAll("xhamster.com", "m.xhamster.com"));
        LOGGER.info("sanitized URL is " + san_url.toExternalForm());
        return san_url;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[\\w\\w.]*xhamster\\.com/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected xhamster.com gallery formats: "
                        + "xhamster.com/photos/gallery/xxxxx-#####"
                        + " Got: " + url);
    }


    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://([\\w\\w]*\\.)?xhamster\\.com/photos/gallery/.*?(\\d+)$");
        Matcher m = p.matcher(url.toExternalForm());
        return m.matches();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("a.next").first() != null) {
            if (doc.select("a.next").first().attr("href").startsWith("http")) {
                return Http.url(doc.select("a.next").first().attr("href")).get();
            }
        }
        throw new IOException("No more pages");

    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element thumb : doc.select("div.picture_view > div.pictures_block > div.items > div.item-container > a > div.thumb_container > div.img > img")) {
            String image = thumb.attr("src");
            // replace thumbnail urls with the urls to the full sized images
            image = image.replaceAll(
                    "https://upt.xhcdn\\.",
                    "http://up.xhamster.");
            image = image.replaceAll("ept\\.xhcdn", "ep.xhamster");
            image = image.replaceAll(
                    "_160\\.",
                    "_1000.");
            // Xhamster has bad cert management and uses invalid certs for some cdns, so we change all our requests to http
            image = image.replaceAll("https", "http");
            result.add(image);
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title and username as GID
            Document doc = getFirstPage();
            Element user = doc.select("a.author").first();
            String username = user.text();
            String path = url.getPath();
            Pattern p = Pattern.compile("^/photos/gallery/(.*)$");
            Matcher m = p.matcher(path);
            if (m.matches() && !username.isEmpty()) {
                return getHost() + "_" + username + "_" + m.group(1);
            }
        } catch (IOException e) {
            // Fall back to default album naming convention
        }
        return super.getAlbumTitle(url);
    }
}