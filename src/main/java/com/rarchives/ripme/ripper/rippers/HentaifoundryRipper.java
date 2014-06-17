package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public class HentaifoundryRipper extends AlbumRipper {

    private static final String DOMAIN = "hentai-foundry.com",
                                HOST   = "hentai-foundry";

    public HentaifoundryRipper(URL url) throws IOException {
        super(url);
    }

    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    @Override
    public void rip() throws IOException {
        Pattern imgRegex = Pattern.compile(".*/user/([a-zA-Z0-9\\-_]+)/(\\d+)/.*");
        String nextURL = this.url.toExternalForm();
        int index = 0;
        
        // Get cookies
        Response resp = Jsoup.connect("http://www.hentai-foundry.com/")
                             .execute();
        Map<String,String> cookies = resp.cookies();
        resp = Jsoup.connect("http://www.hentai-foundry.com/?enterAgree=1&size=1500")
                    .referrer("http://www.hentai-foundry.com/")
                    .cookies(cookies)
                    .method(Method.GET)
                    .execute();
        cookies = resp.cookies();
        logger.info("cookies: " + cookies);
        
        // Iterate over every page
        while (true) {
            if (isStopped()) {
                break;
            }
            sendUpdate(STATUS.LOADING_RESOURCE, nextURL);
            Document doc = Jsoup.connect(nextURL)
                                .userAgent(USER_AGENT)
                                .timeout(5000)
                                .cookies(cookies)
                                .referrer(this.url.toExternalForm())
                                .get();
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
                logger.info("user: " + user + "; imageId: " + imageId + "; image: " + image);
                image += user.toLowerCase().charAt(0);
                image += "/" + user + "/" + imageId + ".jpg";
                index += 1;
                String prefix = "";
                if (Utils.getConfigBoolean("download.save_order", true)) {
                    prefix = String.format("%03d_", index);
                }
                addURLToDownload(new URL(image), prefix);
            }
            
            if (doc.select("li.next.hidden").size() > 0) {
                // Last page
                break;
            }
            Elements els = doc.select("li.next > a");
            logger.info("li.next > a : " + els);
            Element first = els.first();
            logger.info("li.next > a .first() : " + first);
            nextURL = first.attr("href");
            logger.info("first().attr(href) : " + nextURL);
            nextURL = "http://www.hentai-foundry.com" + nextURL;
        }
        waitForThreads();
    }

    @Override
    public String getHost() {
        return HOST;
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
}
