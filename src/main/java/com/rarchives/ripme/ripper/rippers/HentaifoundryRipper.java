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
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HentaifoundryRipper extends AbstractHTMLRipper {

    private Map<String,String> cookies = new HashMap<String,String>();
    public HentaifoundryRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hentai-foundry";
    }
    @Override
    public String getDomain() {
        return "hentai-foundry.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^.*hentai-foundry\\.com/pictures/user/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected hentai-foundry.com gallery format: "
                        + "hentai-foundry.com/pictures/user/USERNAME"
                        + " Got: " + url);
    }

    @Override
    public Document getFirstPage() throws IOException {
        Response resp = Http.url("http://www.hentai-foundry.com/").response();
        cookies = resp.cookies();
        resp = Http.url("http://www.hentai-foundry.com/?enterAgree=1&size=1500")
                   .referrer("http://www.hentai-foundry.com/")
                   .cookies(cookies)
                   .response();
        cookies.putAll(resp.cookies());
        sleep(500);
        resp = Http.url(url)
                   .referrer("http://www.hentai-foundry.com/")
                   .cookies(cookies)
                   .response();
        cookies.putAll(resp.cookies());
        return resp.parse();
    }
    
    @Override
    public Document getNextPage(Document doc) throws IOException {
        if (doc.select("li.next.hidden").size() > 0) {
            // Last page
            throw new IOException("No more pages");
        }
        Elements els = doc.select("li.next > a");
        Element first = els.first();
        String nextURL = first.attr("href");
        nextURL = "http://www.hentai-foundry.com" + nextURL;
        return Http.url(nextURL)
                   .referrer(url)
                   .cookies(cookies)
                   .get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<String>();
        Pattern imgRegex = Pattern.compile(".*/user/([a-zA-Z0-9\\-_]+)/(\\d+)/.*");
        for (Element thumb : doc.select("td > a:first-child")) {
            if (isStopped()) {
                break;
            }
            Matcher imgMatcher = imgRegex.matcher(thumb.attr("href"));
            if (!imgMatcher.matches()) {
                logger.info("Couldn't find user & image ID in " + thumb.attr("href"));
                continue;
            }
            String user = imgMatcher.group(1),
                imageId = imgMatcher.group(2);
            String image = "http://pictures.hentai-foundry.com//";
            image += user.toLowerCase().charAt(0);
            image += "/" + user + "/" + imageId + ".jpg";
            imageURLs.add(image);
        }
        return imageURLs;
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

}
