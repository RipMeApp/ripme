package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

/**
 *
 * @author losipher
 */
public class EromeRipper extends AbstractHTMLRipper {


    public EromeRipper (URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
            return "erome.com";
    }

    @Override
    public String getHost() {
            return "erome";
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
            try {
                // Attempt to use album title as GID
                Element titleElement = getFirstPage().select("meta[property=og:title]").first();
                String title = titleElement.attr("content");
                title = title.substring(title.lastIndexOf('/') + 1);
                return getHost() + "_" + getGID(url) + "_" + title.trim();
            } catch (IOException e) {
                // Fall back to default album naming convention
                logger.info("Unable to find title at " + url);
            }
            return super.getAlbumTitle(url);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return new URL(url.toExternalForm().replaceAll("https?://erome.com", "https://www.erome.com"));
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> URLs = new ArrayList<>();
        //Pictures
        Elements imgs = doc.select("div.img > img.img-front");
        for (Element img : imgs) {
            String imageURL = img.attr("src");
            imageURL = "https:" + imageURL;
            URLs.add(imageURL);
        }
        //Videos
        Elements vids = doc.select("div.video > video > source");
        for (Element vid : vids) {
            String videoURL = vid.attr("src");
            URLs.add("https:" + videoURL);
        }

        return URLs;
    }

    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url(this.url)
                            .ignoreContentType()
                            .response();

        return resp.parse();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://www.erome.com/a/([a-zA-Z0-9]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        p = Pattern.compile("^https?://erome.com/a/([a-zA-Z0-9]*)/?$");
        m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("erome album not found in " + url + ", expected https://www.erome.com/album");
    }

    public static List<URL> getURLs(URL url) throws IOException{

        Response resp = Http.url(url)
                            .ignoreContentType()
                            .response();

        Document doc = resp.parse();

        List<URL> URLs = new ArrayList<>();
        //Pictures
        Elements imgs = doc.getElementsByTag("img");
        for (Element img : imgs) {
            if (img.hasClass("album-image")) {
                String imageURL = img.attr("src");
                imageURL = "https:" + imageURL;
                URLs.add(new URL(imageURL));
            }
        }
        //Videos
        Elements vids = doc.getElementsByTag("video");
        for (Element vid : vids) {
            if (vid.hasClass("album-video")) {
                Elements source = vid.getElementsByTag("source");
                String videoURL = source.first().attr("src");
                URLs.add(new URL(videoURL));
            }
        }

        return URLs;
    }
}
