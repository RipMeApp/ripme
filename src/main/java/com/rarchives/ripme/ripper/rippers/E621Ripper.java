package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class E621Ripper extends AbstractHTMLRipper {
    private static final Logger logger = Logger.getLogger(E621Ripper.class);

    private static Pattern gidPattern = null;
    private static Pattern gidPattern2 = null;
    private static Pattern gidPatternPool = null;

    private DownloadThreadPool e621ThreadPool = new DownloadThreadPool("e621");

    public E621Ripper(URL url) throws IOException {
        super(url);
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
        if (url.getPath().startsWith("/pool/show/"))
            return Http.url("https://e621.net/pool/show/" + getTerm(url)).get();
        else
            return Http.url("https://e621.net/post/index/1/" + getTerm(url)).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        Elements elements = page.select("div > span.thumb > a");
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
        if (!page.select("a.next_page").isEmpty()) {
            return Http.url(page.select("a.next_page").attr("abs:href")).get();
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
        if (gidPattern == null)
            gidPattern = Pattern.compile(
                    "^https?://(www\\.)?e621\\.net/post/index/[^/]+/([a-zA-Z0-9$_.+!*'():,%\\-]+)(/.*)?(#.*)?$");
        if (gidPatternPool == null)
            gidPatternPool = Pattern.compile(
                    "^https?://(www\\.)?e621\\.net/pool/show/([a-zA-Z0-9$_.+!*'(),%:\\-]+)(\\?.*)?(/.*)?(#.*)?$");

        Matcher m = gidPattern.matcher(url.toExternalForm());
        if (m.matches()) {
            LOGGER.info(m.group(2));
            return m.group(2);
        }

        m = gidPatternPool.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(2);
        }

        throw new MalformedURLException(
                "Expected e621.net URL format: e621.net/post/index/1/searchterm - got " + url + " instead");
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String prefix = "";
        if (url.getPath().startsWith("/pool/show/")) {
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

    public class E621FileThread extends Thread {

        private URL url;
        private String index;

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
            Document page = Http.url(imageURL).retries(3).get();
            Elements video = page.select("video > source");
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
            }

        }

    }
}
