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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;

public class TwodgalleriesRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(TwodgalleriesRipper.class);

    private int offset = 0;
    private Map<String,String> cookies = new HashMap<>();

    public TwodgalleriesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "2dgalleries";
    }
    @Override
    public String getDomain() {
        return "2dgalleries.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p; Matcher m;

        p = Pattern.compile("^.*2dgalleries.com/artist/([a-zA-Z0-9\\-]+).*$");
        m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException(
                "Expected 2dgalleries.com album format: "
                        + "2dgalleries.com/artist/..."
                        + " Got: " + url);
    }

    private String getURL(String userid, int offset) {
        return "http://en.2dgalleries.com/artist/" + userid
                      + "?timespan=4"
                      + "&order=1"
                      + "&catid=2"
                      + "&offset=" + offset
                      + "&ajx=1&pager=1";
    }

    @Override
    public Document getFirstPage() throws IOException {
        try {
            login();
        } catch (IOException e) {
            logger.error("Failed to login", e);
        }
        String url = getURL(getGID(this.url), offset);
        return Http.url(url)
                   .cookies(cookies)
                   .get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        offset += 24;
        String url = getURL(getGID(this.url), offset);
        sleep(500);
        Document nextDoc = Http.url(url)
                               .cookies(cookies)
                               .get();
        if (nextDoc.select("div.hcaption > img").isEmpty()) {
            throw new IOException("No more images to retrieve");
        }
        return nextDoc;
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("div.hcaption > img")) {
            String image = thumb.attr("src");
            image = image.replace("/200H/", "/");
            if (image.startsWith("//")) {
                image = "http:" + image;
            } else if (image.startsWith("/")) {
                image = "http://en.2dgalleries.com" + image;
            }
            imageURLs.add(image);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    private void login() throws IOException {
        Response resp = Http.url(this.url).response();
        cookies = resp.cookies();
        String ctoken = resp.parse().select("form > input[name=ctoken]").first().attr("value");

        Map<String,String> postdata = new HashMap<>();
        postdata.put("user[login]", new String(Base64.decode("cmlwbWU=")));
        postdata.put("user[password]", new String(Base64.decode("cmlwcGVy")));
        postdata.put("rememberme", "1");
        postdata.put("ctoken", ctoken);

        resp = Http.url("http://en.2dgalleries.com/account/login")
                   .referrer("http://en.2dgalleries.com/")
                   .cookies(cookies)
                   .data(postdata)
                   .method(Method.POST)
                   .response();
        cookies = resp.cookies();
    }
}
