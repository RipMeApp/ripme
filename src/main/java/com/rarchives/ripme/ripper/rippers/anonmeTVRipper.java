package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class anonmeTVRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "anonme.tv", HOST = "anonme";

    public anonmeTVRipper(URL url) throws IOException {
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
            title = title.replaceAll("anonme_\\/*[a-zA-Z0-9]*\\/ - ", "");
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
    public List<String> getURLsFromPage(Document doc) {

        List<String> result = new ArrayList<>();

        for (Element el : doc.select("div.file.multifile > a")) {
            String url = el.attr("href");
            String correctedURL = "https://anonme.tv" + url;
            result.add(correctedURL);
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
        title = title.replaceAll("anonme_\\/*[a-zA-Z0-9]*\\/ - ", "");
        title = title.replaceAll("[/\\\\?%*:.|\"<>]","");
        LOGGER.info("Using album title '" + title + "'");

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