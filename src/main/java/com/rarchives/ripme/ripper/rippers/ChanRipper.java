package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.rippers.ripperhelpers.ChanSite;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.RipUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ChanRipper extends AbstractHTMLRipper {
    private static List<ChanSite> explicit_domains = Arrays.asList(
        new ChanSite(Arrays.asList("boards.4chan.org"),   Arrays.asList("4cdn.org", "is.4chan.org", "is2.4chan.org", "is3.4chan.org")),
        new ChanSite(Arrays.asList("4archive.org"),       Arrays.asList("imgur.com")),
        new ChanSite(Arrays.asList("archive.4plebs.org"), Arrays.asList("img.4plebs.org"))
        );

    private static List<String> url_piece_blacklist = Arrays.asList(
        "=http",
        "http://imgops.com/",
        "iqdb.org",
        "saucenao.com"
        );

    private ChanSite chanSite;
    private Boolean generalChanSite = true;

    public ChanRipper(URL url) throws IOException {
        super(url);
        for (ChanSite _chanSite : explicit_domains) {
            if (_chanSite.domains.contains(url.getHost())) {
                chanSite = _chanSite;
                generalChanSite = false;
            }
        }
        if (chanSite == null) {
            chanSite = new ChanSite(Arrays.asList(url.getHost()));
        }
    }

    @Override
    public String getHost() {
        String host = this.url.getHost();
        host = host.substring(0, host.lastIndexOf('.'));
        if (host.contains(".")) {
            // Host has subdomain (www)
            host = host.substring(host.lastIndexOf('.') + 1);
        }
        String board = this.url.toExternalForm().split("/")[3];
        return host + "_" + board;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            Document doc = getFirstPage();
            try {
                String subject = doc.select(".post.op > .postinfo > .subject").first().text();
                return getHost() + "_" + getGID(url) + "_" + subject;
            } catch (NullPointerException e) {
                LOGGER.warn("Failed to get thread title from " + url);
            }
        } catch (Exception e) {
            // Fall back to default album naming convention
            LOGGER.warn("Failed to get album title from " + url, e);
        }
        // Fall back on the GID
        return getHost() + "_" + getGID(url);
    }

    @Override
    public boolean canRip(URL url) {
        for (ChanSite _chanSite : explicit_domains) {
            if (_chanSite.domains.contains(url.getHost())) {
                return true;
            }
        }

        if (url.toExternalForm().contains("desuchan.net") && url.toExternalForm().contains("/res/")) {
            return true;
        }
        if (url.toExternalForm().contains("boards.420chan.org") && url.toExternalForm().contains("/res/")) {
            return true;
        }
        if (url.toExternalForm().contains("7chan.org") && url.toExternalForm().contains("/res/")) {
            return true;
        }
        if (url.toExternalForm().contains("xchan.pw") && url.toExternalForm().contains("/board/")) {
            return true;
        }
        if (url.toExternalForm().contains("desuarchive.org")) {
            return true;
        }
        if (url.toExternalForm().contains("8ch.net") && url.toExternalForm().contains("/res/")) {
            return true;
        }
        return false;
    }

    /**
     * For example the archives are all known. (Check 4chan-x)
     * Should be based on the software the specific chan uses.
     * FoolFuuka uses the same (url) layout as 4chan
     *
     * @param url
     * @return
     *      The thread id in string form
     * @throws java.net.MalformedURLException */
    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p;
        Matcher m;

        String u = url.toExternalForm();
        if (u.contains("/thread/") || u.contains("/res/")) {
            p = Pattern.compile("^.*\\.[a-z]{1,3}/[a-zA-Z0-9]+/(thread|res)/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(2);
            }

            // Drawchan is weird, has drawchan.net/dc/dw/res/####.html
            p = Pattern.compile("^.*\\.[a-z]{1,3}/[a-zA-Z0-9]+/[a-zA-Z0-9]+/res/([0-9]+)(\\.html|\\.php)?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }
            // xchan
            p = Pattern.compile("^.*\\.[a-z]{1,3}/board/[a-zA-Z0-9]+/thread/([0-9]+)/?.*$");
            m = p.matcher(u);
            if (m.matches()) {
                return m.group(1);
            }
        }

        throw new MalformedURLException(
                "Expected *chan URL formats: "
                        + ".*/@/(res|thread)/####.html"
                        + " Got: " + u);
    }

    @Override
    public String getDomain() {
        return this.url.getHost();
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(this.url).get();
    }

    private boolean isURLBlacklisted(String url) {
        for (String blacklist_item : url_piece_blacklist) {
            if (url.contains(blacklist_item)) {
                LOGGER.debug("Skipping link that contains '"+blacklist_item+"': " + url);
                return true;
            }
        }
        return false;
    }
    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> imageURLs = new ArrayList<>();
        Pattern p; Matcher m;
        for (Element link : page.select("a")) {
            if (!link.hasAttr("href")) {
                continue;
            }
            String href = link.attr("href").trim();

            if (isURLBlacklisted(href)) {
                continue;
            }
            //Check all blacklist items
            Boolean self_hosted = false;
            if (!generalChanSite) {
                for (String cdnDomain : chanSite.cdnDomains) {
                    if (href.contains(cdnDomain)) {
                        self_hosted = true;
                    }
                }
            }

            if (self_hosted || generalChanSite) {
                p = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|apng|webp|tif|tiff|webm)$", Pattern.CASE_INSENSITIVE);
                m = p.matcher(href);
                if (m.matches()) {
                    if (href.startsWith("//")) {
                        href = "http:" + href;
                    }
                    if (href.startsWith("/")) {
                        href = "http://" + this.url.getHost() + href;
                    }
                    // Don't download the same URL twice
                    if (imageURLs.contains(href)) {
                        LOGGER.debug("Already attempted: " + href);
                        continue;
                    }
                    imageURLs.add(href);
                    if (isThisATest()) {
                        break;
                    }
                }
            } else {
                //Copied code from RedditRipper, getFilesFromURL should also implement stuff like flickr albums
                URL originalURL;
                try {
                    originalURL = new URL(href);
                } catch (MalformedURLException e) {
                    continue;
                }

                List<URL> urls = RipUtils.getFilesFromURL(originalURL);
                for (URL imageurl : urls) {
                    imageURLs.add(imageurl.toString());
                }
            }

            if (isStopped()) {
                break;
            }
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
