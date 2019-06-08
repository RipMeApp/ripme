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


public class fantastiRipper extends AbstractHTMLRipper {


    private Document albumDoc = null;
    private boolean isNewAlbumType = false;


    public fantastiRipper(URL url) throws IOException {
        super(url);
    }


    @Override
    public String getHost() {
        return "fantasti";
    }
    @Override
    public String getDomain() {
        return "fantasti.cc";
    }


    /**
     * Reformat given URL into the desired format (all images on single page)
     */
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        String gid = getGID(url);
        String newURL = "https://fantasti.cc/images/";
        if (isNewAlbumType) {
            newURL += "p";
        }
        newURL += "gid=" + gid + "&view=2";
        LOGGER.debug("Changed URL from " + url + " to " + newURL);
        return new URL(newURL);
    }


    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;


        p = Pattern.compile("^.*https://fantasti.cc/images\\?pgid=([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }
        p = Pattern.compile("^.*https://fantasti.cc/images\\?gid=([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }


        p = Pattern.compile("^.*fantasti.cc.com/pictures/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*fantasti.cc/pictures/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }


        p = Pattern.compile("^.*fantasti.cc/images/([0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        p = Pattern.compile("^.*fantasti.cc/images/([a-f0-9]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            isNewAlbumType = true;
            return m.group(1);
        }


        throw new MalformedURLException(
                "Expected fantasti.cc gallery formats: "
                        + "fantasti.cc/images/gid=####... or "
                        + "fantasti.cc/pictures/####..."
                        + " Got: " + url);
    }


    @Override
    public Document getFirstPage() throws IOException {
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }
        return albumDoc;
    }


    @Override
    public Document getNextPage(Document doc) throws IOException {
        String nextURL = null;
        for (Element a : doc.select("a.link3")) {
            if (a.text().contains("next")) {
                nextURL = "https://fantasti.cc/images/" + a.attr("href");
                break;
            }
        }
        if (nextURL == null) {
            throw new IOException("No next page found");
        }
        sleep(1000);
        return Http.url(nextURL).get();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("#gallery img")) {
            if (!thumb.hasAttr("src") || !thumb.hasAttr("width")) {
                continue;
            }
            String image = getFullSizedImage("https://fantasti.cc" + thumb.parent().attr("href"));
            imageURLs.add(image);
            if (isThisATest()) {
                break;
            }
        }
        return imageURLs;
    }


    @Override
    public void downloadURL(URL url, int index) {
        // Send referrer for image downloads
        addURLToDownload(url, getPrefix(index), "", this.url.toExternalForm(), null);
    }


    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            String title = getFirstPage().title();
            title = title.replace("Porn Pics & Porn GIFs", "");
            title = title.replace(" ", "_");
            String toReturn = getHost() + "_" + title + "_" + getGID(url);
            return toReturn.replaceAll("__", "_");
        } catch (IOException e) {
            return super.getAlbumTitle(url);
        }
    }


    private String getFullSizedImage(String pageURL) {
        try {
            Document doc = Http.url(pageURL).get();
            return doc.select("img#mainPhoto").attr("src");
        } catch (IOException e) {
            return null;
        }
    }


}
