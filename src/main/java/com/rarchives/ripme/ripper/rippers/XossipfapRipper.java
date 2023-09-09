package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class XossipfapRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "xossipfap.net", HOST = "xossipfap";

    public XossipfapRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {

        try {
            Document doc = Jsoup.connect(url.toString())
                    .userAgent(AbstractRipper.USER_AGENT)
                    .get();
            String title = doc.title();
            return title;
        } catch (Exception e) {
            e.printStackTrace();
            return "missed";
        }
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        Elements nextPageLink = doc.select("a.pagination_next");
        if (nextPageLink.isEmpty()){
            throw new IOException("No more pages");
        } else {

            String nxtPageURL = "";

            for(Element link : nextPageLink) {

                String partURL = link.attr("href");
                if (!partURL.contains("forum")) {
                    nxtPageURL = partURL;
                    break;
                }

            }
            URL nextURL = new URL(this.url, nxtPageURL);
            LOGGER.info("Downloading xosspipurl " + nextURL);
            return Http.url(nextURL).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {

        List<String> result = new ArrayList<>();

        for (Element el : doc.select("img.mycode_img")) {
            String url =  el.attr("src");
            url = url.replace("/th/","/i/");
            url = url.replace("_t.",".");
            if(url.contains("http://iceimg.net")) {
                url = url.replace("http://iceimg.net", "https://prcf.imgbig.xyz");
                url = url.replace("/small/","/big/");
                url = url.replace("small_","");
            }
            if(url.contains("http://imgfrost.net")) {
                url = url.replace("http://imgfrost.net", "https://prcf.imgbig.xyz");
                url = url.replace("/small/","/big/");
                url = url.replace("small_","");
            }
            if(url.contains("imgadult.com")) {
                url = url.replace("/small/","/big/");
                url = url.replace("/small-medium/","/big/");
            }
            if(url.contains("t38.pixhost.to")) {
                url = url.replace("https://t38.pixhost.to/thumbs/", "https://img38.pixhost.to/images/");
            }
            if(url.contains("t36.pixhost.to")) {
                url = url.replace("https://t36.pixhost.to/thumbs/", "https://img36.pixhost.to/images/");
            }
            if(url.contains("t44.pixhost.to")) {
                url = url.replace("https://t44.pixhost.to/thumbs/", "https://img44.pixhost.to/images/");
            }
            if(url.contains("t37.pixhost.to")) {
                url = url.replace("https://t37.pixhost.to/thumbs/", "https://img37.pixhost.to/images/");
            }
            if(url.contains("t41.pixhost.to")) {
                url = url.replace("https://t41.pixhost.to/thumbs/", "https://img41.pixhost.to/images/");
            }
            if(url.contains("t40.pixhost.to")) {
                url = url.replace("https://t40.pixhost.to/thumbs/", "https://img40.pixhost.to/images/");
            }
            if(url.contains("t33.pixhost.to")) {
                url = url.replace("https://t33.pixhost.to/thumbs/", "https://img33.pixhost.to/images/");
            }
            if(url.contains("t39.pixhost.to")) {
                url = url.replace("https://t39.pixhost.to/thumbs/", "https://img39.pixhost.to/images/");
            }
            if(url.contains("t34.pixhost.to")) {
                url = url.replace("https://t34.pixhost.to/thumbs/", "https://img34.pixhost.to/images/");
            }
            if(url.contains("imgbox.com")) {
                url = url.replace("thumbs", "images");
                url = url.replace(".jpg", "_o.jpg");
            }

            result.add(url);
        }
        return result;

    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }

    @Override
    public void setWorkingDir(URL url) throws IOException {
        String path = Utils.getWorkingDirectory().getCanonicalPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        String title;
        if (Utils.getConfigBoolean("album_titles.save", true)) {
            title = getAlbumTitle(this.url);
        } else {
            title = super.getAlbumTitle(this.url);
        }
        //title = HOST;
        LOGGER.debug("Using album title '" + title + "'");

        path += title;
        path = Utils.getOriginalDirectory(path) + File.separator;   // check for case sensitive (unix only)

        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            LOGGER.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir));
            this.workingDir.mkdirs();
        }
        LOGGER.debug("Set working directory to: " + this.workingDir);
    }
}
