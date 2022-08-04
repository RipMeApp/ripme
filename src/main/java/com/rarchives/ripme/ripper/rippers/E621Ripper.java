package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class E621Ripper extends AbstractHTMLRipper {
    private static final Logger logger = LogManager.getLogger(E621Ripper.class);

    private static Pattern gidPattern = null;
    private static Pattern gidPattern2 = null;
    private static Pattern gidPatternPool = null;

    private static Pattern gidPatternNew = null;
    private static Pattern gidPatternPoolNew = null;

    private DownloadThreadPool e621ThreadPool = new DownloadThreadPool("e621");

    private Map<String, String> cookies = new HashMap<String, String>();
    private String userAgent = USER_AGENT;

    public E621Ripper(URL url) throws IOException {
        super(url);
    }

    private void loadConfig() {
        String cookiesString = Utils.getConfigString("e621.cookies", "");
        if(!cookiesString.equals("")) {
            cookies = RipUtils.getCookiesFromString(cookiesString);
            if(cookies.containsKey("cf_clearance"))
                sendUpdate(STATUS.DOWNLOAD_WARN, "Using CloudFlare captcha cookies, make sure to update them and set your browser's useragent in config!");
            if(cookies.containsKey("remember"))
                sendUpdate(STATUS.DOWNLOAD_WARN, "Logging in using auth cookie.");
        }
        userAgent = Utils.getConfigString("e621.useragent", USER_AGENT);
        
    }

    private void warnAboutBlacklist(Document page) {
        if(!page.select("div.hidden-posts-notice").isEmpty()) 
            sendUpdate(STATUS.DOWNLOAD_WARN, "Some posts are blacklisted. Consider logging in. Search for \"e621\" in this wiki page: https://github.com/RipMeApp/ripme/wiki/Config-options");
    }

    private Document getDocument(String url, int retries) throws IOException {
        return Http.url(url).userAgent(userAgent).retries(retries).cookies(cookies).get();
    }

    private Document getDocument(String url) throws IOException {
        return getDocument(url, 1);
    }

    @Override
    public DownloadThreadPool getThreadPool() {
        return e621ThreadPool;
    }

    @Override
    public String getDomain() {
        return "e621.net";
    }

    @Override
    public String getHost() {
        return "e621";
    }

    @Override
    public Document getFirstPage() throws IOException {
        loadConfig();
        Document page;
        if (url.getPath().startsWith("/pool"))
            page = getDocument("https://e621.net/pools/" + getTerm(url));
        else
            page = getDocument("https://e621.net/posts?tags=" + getTerm(url));
        
        warnAboutBlacklist(page);
        return page;
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        Elements elements = page.select("article > a");
        List<String> res = new ArrayList<>();

        for (Element e : elements) {
            if (!e.attr("href").isEmpty()) {
                res.add(e.attr("abs:href"));
            }
        }

        return res;
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        warnAboutBlacklist(page);
        if (!page.select("a#paginator-next").isEmpty()) {
            return getDocument(page.select("a#paginator-next").attr("abs:href"));
        } else {
            throw new IOException("No more pages.");
        }
    }

    @Override
    public void downloadURL(final URL url, int index) {
        // addURLToDownload(url, getPrefix(index));
        e621ThreadPool.addThread(new E621FileThread(url, getPrefix(index)));
    }

    private String getTerm(URL url) throws MalformedURLException {
        // old url style => new url style:
        // /post/index/1/<tags> => /posts?tags=<tags>
        // /pool/show/<id> => /pools/id
        if (gidPattern == null)
            gidPattern = Pattern.compile(
                    "^https?://(www\\.)?e621\\.net/post/index/[^/]+/([a-zA-Z0-9$_.+!*'():,%\\-]+)(/.*)?(#.*)?$");
        if (gidPatternPool == null)
            gidPatternPool = Pattern.compile(
                    "^https?://(www\\.)?e621\\.net/pool/show/([a-zA-Z0-9$_.+!*'(),%:\\-]+)(\\?.*)?(/.*)?(#.*)?$");
        if (gidPatternNew == null)
            gidPatternNew = Pattern.compile("^https?://(www\\.)?e621\\.net/posts\\?([\\S]*?)tags=([a-zA-Z0-9$_.+!*'(),%:\\-]+)(\\&[\\S]+)?");
        if (gidPatternPoolNew == null)
            gidPatternPoolNew = Pattern.compile("^https?://(www\\.)?e621\\.net/pools/([\\d]+)(\\?[\\S]*)?");

        Matcher m = gidPattern.matcher(url.toExternalForm());
        if (m.matches()) {
            LOGGER.info(m.group(2));
            return m.group(2);
        }

        m = gidPatternPool.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

        m = gidPatternNew.matcher(url.toExternalForm());
        if (m.matches()) {
            LOGGER.info(m.group(3));
            return m.group(3);
        }

        m = gidPatternPoolNew.matcher(url.toExternalForm());
        if (m.matches()) {
            LOGGER.info(m.group(2));
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected e621.net URL format: e621.net/posts?tags=searchterm - got " + url + " instead");
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String prefix = "";
        if (url.getPath().startsWith("/pool")) {
            prefix = "pool_";
        }
        return Utils.filesystemSafe(prefix + getTerm(url));
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        if (gidPattern2 == null)
            gidPattern2 = Pattern.compile(
                    "^https?://(www\\.)?e621\\.net/post/search\\?tags=([a-zA-Z0-9$_.+!*'():,%-]+)(/.*)?(#.*)?$");

        Matcher m = gidPattern2.matcher(url.toExternalForm());
        if (m.matches())
            return new URL("https://e621.net/post/index/1/" + m.group(2).replace("+", "%20"));

        return url;
    }

    public class E621FileThread implements Runnable {

        private final URL url;
        private final String index;

        public E621FileThread(URL url, String index) {
            this.url = url;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                String fullSizedImage = getFullSizedImage(url);
                if (fullSizedImage != null && !fullSizedImage.equals("")) {
                    addURLToDownload(new URL(fullSizedImage), index);
                }
            } catch (IOException e) {
                logger.error("Unable to get full sized image from " + url);
            }
        }

        private String getFullSizedImage(URL imageURL) throws IOException {
            Document page = getDocument(imageURL.toExternalForm(), 3);
            /*Elements video = page.select("video > source");
            Elements flash = page.select("embed");
            Elements image = page.select("a#highres");
            if (video.size() > 0) {
                return video.attr("src");
            } else if (flash.size() > 0) {
                return flash.attr("src");
            } else if (image.size() > 0) {
                return image.attr("href");
            } else {
                throw new IOException();
            }*/

            if (!page.select("div#image-download-link > a").isEmpty()) {
                return page.select("div#image-download-link > a").attr("abs:href");
            } else {
                if(!page.select("#blacklist-box").isEmpty())
                    sendUpdate(RipStatusMessage.STATUS.RIP_ERRORED, "Cannot download image - blocked by blacklist. Consider logging in. Search for \"e621\" in this wiki page: https://github.com/RipMeApp/ripme/wiki/Config-options");
                throw new IOException();
            }
        }

    }
}
