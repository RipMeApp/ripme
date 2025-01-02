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

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

/**
 *
 * @author randomcommitter
 */
public class ErotivRipper extends AbstractHTMLRipper {

    boolean rippingProfile;

    public ErotivRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "erotiv.io";
    }

    @Override
    public String getHost() {
        return "erotiv";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(?:www.)?erotiv.io/e/([0-9]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }

        throw new MalformedURLException("erotiv video not found in " + url + ", expected https://erotiv.io/e/id");
    }

    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url(this.url).ignoreContentType().response();

        return resp.parse();
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        return new URI(url.toExternalForm().replaceAll("https?://www.erotiv.io", "https://erotiv.io")).toURL();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> results = new ArrayList<>();
        for (Element el : doc.select("video[id=\"video-id\"] > source")) {
            if (el.hasAttr("src")) {
                Pattern p = Pattern.compile("/uploads/[0-9]*\\.mp4");
                Matcher m = p.matcher(el.attr("src"));
                if (m.matches()) {
                    results.add("https://erotiv.io" + el.attr("src"));
                }
            }

        }
        return results;

    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

}
