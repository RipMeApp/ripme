/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
    public boolean canRip(URL url) {
        Pattern p = Pattern.compile("^https?://eroshae.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return true;
        }

        Pattern pa = Pattern.compile("^https?://eroshae.com/u/([a-zA-Z0-9\\-_]+)/?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return true;
        }

        Pattern p_eroshare = Pattern.compile("^https?://eroshare.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m_eroshare = p_eroshare.matcher(url.toExternalForm());
        if (m_eroshare.matches()) {
            return true;
        }

        Pattern p_eroshare_profile = Pattern.compile("^https?://eroshare.com/u/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m_eroshare_profile = p_eroshare_profile.matcher(url.toExternalForm());
        return m_eroshare_profile.matches();
    }

    private boolean is_profile(URL url) {
        Pattern pa = Pattern.compile("^https?://eroshae.com/u/([a-zA-Z0-9\\-_]+)/?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        return ma.matches();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextUrl = "";
        Element elem = doc.select("li.next > a").first();
        if (elem == null) {
            throw new IOException("No more pages");
        }
            nextUrl = elem.attr("href");
            if (nextUrl.equals("")) {
                throw new IOException("No more pages");
            }
            return Http.url("eroshae.com" + nextUrl).get();
        }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        if (!is_profile(url)) {
            try {
                // Attempt to use album title as GID
                Element titleElement = getCachedFirstPage().select("meta[property=og:title]").first();
                String title = titleElement.attr("content");
                title = title.substring(title.lastIndexOf('/') + 1);
                return getHost() + "_" + getGID(url) + "_" + title.trim();
            } catch (IOException e) {
                // Fall back to default album naming convention
                LOGGER.info("Unable to find title at " + url);
            }
            return super.getAlbumTitle(url);
        }
        return url.toExternalForm().split("/u/")[1];
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> URLs = new ArrayList<>();
        //Pictures
        Elements imgs = doc.getElementsByTag("img");
        for (Element img : imgs) {
            if (img.hasClass("album-image")) {
                String imageURL = img.attr("src");
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
        // Profile videos
        Elements links = doc.select("div.item-container > a.item");
        for (Element link : links) {
            Document video_page;
            try {
                video_page = Http.url("eroshae.com" + link.attr("href")).get();
            } catch (IOException e) {
                LOGGER.warn("Failed to log link in Jsoup");
                video_page = null;
                e.printStackTrace();
            }
            Elements profile_vids = video_page.getElementsByTag("video");
            for (Element vid : profile_vids) {
                if (vid.hasClass("album-video")) {
                    Elements source = vid.getElementsByTag("source");
                    String videoURL = source.first().attr("src");
                    URLs.add(videoURL);
                }
            }
        }

        return URLs;
    }

    @Override
    public Document getFirstPage() throws IOException {
        String urlToDownload = this.url.toExternalForm();
        Response resp = Http.url(urlToDownload.replace("eroshare.com", "eroshae.com"))
                            .ignoreContentType()
                            .response();

        return resp.parse();
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://eroshae.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        Pattern p_eroshare = Pattern.compile("^https?://eroshare.com/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m_eroshare = p_eroshare.matcher(url.toExternalForm());
        if (m_eroshare.matches()) {
            return m_eroshare.group(1);
        }

        Pattern p_eroshare_profile = Pattern.compile("^https?://eroshare.com/u/([a-zA-Z0-9\\-_]+)/?$");
        Matcher m_eroshare_profile = p_eroshare_profile.matcher(url.toExternalForm());
        if (m_eroshare_profile.matches()) {
            return m_eroshare_profile.group(1) + "_profile";
        }

        Pattern pa = Pattern.compile("^https?://eroshae.com/u/([a-zA-Z0-9\\-_]+)/?$");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return m.group(1) + "_profile";
        }

        throw new MalformedURLException("eroshare album not found in " + url + ", expected https://eroshare.com/album or eroshae.com/album");
    }

    public static List<URL> getURLs(URL url) throws IOException, URISyntaxException {

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
                URLs.add(new URI(imageURL).toURL());
            }
        }
        //Videos
        Elements vids = doc.getElementsByTag("video");
        for (Element vid : vids) {
            if (vid.hasClass("album-video")) {
                Elements source = vid.getElementsByTag("source");
                String videoURL = source.first().attr("src");
                URLs.add(new URI(videoURL).toURL());
            }
        }

        return URLs;
    }
}
