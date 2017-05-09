package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class E621Ripper extends AbstractHTMLRipper {
    public static final int POOL_IMAGES_PER_PAGE = 24;

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
        if (url.getPath().startsWith("/pool/show/")) {
            return Http.url("https://e621.net/pool/show/" + getTerm(url)).get();
        } else {
            return Http.url("https://e621.net/post/index/1/" + getTerm(url)).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        Elements elements = page.select("#post-list .thumb a,#pool-show .thumb a");
        List<String> res = new ArrayList<String>(elements.size());

        if (page.getElementById("pool-show") != null) {
            int index = 0;

            Element e = page.getElementById("paginator");
            if (e != null) {
                e = e.getElementsByClass("current").first();
                if (e != null) {
                    index = (Integer.parseInt(e.text()) - 1) * POOL_IMAGES_PER_PAGE;
                }
            }

            for (Element e_ : elements) {
                res.add(e_.absUrl("href") + "#" + ++index);
            }

        } else {
            for (Element e : elements) {
                res.add(e.absUrl("href") + "#" + e.child(0).attr("id").substring(1));
            }
        }

        return res;
    }

    @Override
    public Document getNextPage(Document page) throws IOException {
        for (Element e : page.select("#paginator a")) {
            if (e.attr("rel").equals("next")) {
                return Http.url(e.absUrl("href")).get();
            }
        }

        return null;
    }

    @Override
    public void downloadURL(final URL url, int index) {
        e621ThreadPool.addThread(new Thread(new Runnable() {
            public void run() {
                try {
                    Document page = Http.url(url).get();
                    Element e = page.getElementById("image");

                    if (e != null) {
                        addURLToDownload(new URL(e.absUrl("src")), Utils.getConfigBoolean("download.save_order", true) ? url.getRef() + "-" : "");
                    } else if ((e = page.select(".content object>param[name=\"movie\"]").first()) != null) {
                        addURLToDownload(new URL(e.absUrl("value")), Utils.getConfigBoolean("download.save_order", true) ? url.getRef() + "-" : "");
                    } else {
                        Logger.getLogger(E621Ripper.class.getName()).log(Level.WARNING, "Unsupported media type - please report to program author: " + url.toString());
                    }

                } catch (IOException ex) {
                    Logger.getLogger(E621Ripper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
    }

    private String getTerm(URL url) throws MalformedURLException {
        String query = url.getQuery();

        if (query != null) {
            return Utils.parseUrlQuery(query, "tags");
        }

        if (query == null) {
            if ((query = url.getPath()).startsWith("/post/index/")) {
                query = query.substring(12);

                int pos = query.indexOf('/');
                if (pos == -1) {
                    return null;
                }

                // skip page number
                query = query.substring(pos + 1);

                if (query.endsWith("/")) {
                    query = query.substring(0, query.length() - 1);
                }

                try {
                    return URLDecoder.decode(query, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Shouldn't happen since UTF-8 is required to be supported
                    throw new RuntimeException(e);
                }

            } else if (query.startsWith("/pool/show/")) {
                query = query.substring(11);

                if (query.endsWith("/")) {
                    query = query.substring(0, query.length() - 1);
                }

                return query;
            }
        }

        return null;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String prefix = "";
        if (url.getPath().startsWith("/pool/show/")) {
            prefix = "pool_";
        } else {
            prefix = "term_";
        }

        return Utils.filesystemSafe(prefix + getTerm(url));
    }
}
