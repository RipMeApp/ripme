/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class EroShareRipper extends AbstractHTMLRipper {

    public EroShareRipper (URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
            return "eroshare.com";
    }

    @Override
    public String getHost() {
            return "eroshare";
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url);
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
    public List<String> getURLsFromPage(Document doc) {
        List<String> URLs = new ArrayList<String>();
        //Pictures
        Elements imgs = doc.getElementsByTag("img");
        for (Element img : imgs) {
            if (img.hasClass("album-image")) {
                String imageURL = img.attr("src");
                imageURL = "https:" + imageURL;
                URLs.add(imageURL);
            }
        }
        //Videos
        Elements vids = doc.getElementsByTag("video");
        for (Element vid : vids) {
            if (vid.hasClass("album-video")) {
                Elements source = vid.getElementsByTag("source");
                String videoURL = source.first().attr("src");
                URLs.add(videoURL);
            }
        }

        return URLs;
    }

    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url(this.url)
                            .ignoreContentType()
                            .response();

        Document doc = resp.parse();

        return doc;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://[w.]*eroshare.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("eroshare album not found in " + url + ", expected https://eroshare.com/album");
    }

    public static List<URL> getURLs(URL url) throws IOException{

        Response resp = Http.url(url)
                            .ignoreContentType()
                            .response();

        Document doc = resp.parse();

        List<URL> URLs = new ArrayList<URL>();
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
