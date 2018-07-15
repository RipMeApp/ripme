package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.utils.Utils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class LoveromRipper extends AbstractHTMLRipper {

    public LoveromRipper(URL url) throws IOException {
        super(url);
    }

    private int bytesTotal = 1;
    private int bytesCompleted = 1;
    boolean multipart = false;

    @Override
    public String getHost() {
        return "loveroms";
    }

    @Override
    public String getDomain() {
        return "loveroms.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https://www.loveroms.com/download/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/\\d+");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: https://www.loveroms.com/download/CONSOLE/GAME, got: " + url);
        }
        return m.group(1) + "_" + m.group(2);
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }


    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String downloadLink = doc.select("a#start_download_link").attr("href");
        if (downloadLink != null && !downloadLink.isEmpty()) {
            result.add(downloadLink);
        } else {
            multipart = true;
            for (Element el : doc.select("a.multi-file-btn")) {
                result.add(el.attr("href"));
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        if (multipart) {
            addURLToDownload(url, "", "", "", null, null, "7z." + getPrefix(index));
        } else {
            addURLToDownload(url, "", "", "", null, null, "7z");
        }
    }

    @Override
    public String getStatusText() {
        if (multipart) {
            return super.getStatusText();
        }
        return String.valueOf(getCompletionPercentage()) +
                "%  - " +
                Utils.bytesToHumanReadable(bytesCompleted) +
                " / " +
                Utils.bytesToHumanReadable(bytesTotal);
    }

    @Override
    public int getCompletionPercentage() {
        if (multipart) {
            return super.getCompletionPercentage();
        }
        return (int) (100 * (bytesCompleted / (float) bytesTotal));
    }

    @Override
    public void setBytesTotal(int bytes) {
        this.bytesTotal = bytes;
    }

    @Override
    public void setBytesCompleted(int bytes) {
        this.bytesCompleted = bytes;
    }

    @Override
    public boolean useByteProgessBar() {return true;}

    @Override
    public boolean tryResumeDownload() {return true;}

    @Override
    public String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("7z.%03d", index);
        }
        return prefix;
    }
}
