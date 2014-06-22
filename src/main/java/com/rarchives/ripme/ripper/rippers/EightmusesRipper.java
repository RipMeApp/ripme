package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class EightmusesRipper extends AlbumRipper {

    private static final String DOMAIN = "8muses.com",
                                HOST   = "8muses";

    private Document albumDoc = null;

    public EightmusesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(DOMAIN);
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }
    
    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        try {
            // Attempt to use album title as GID
            if (albumDoc == null) {
                albumDoc = Http.url(url).get();
            }
            Element titleElement = albumDoc.select("meta[name=description]").first();
            String title = titleElement.attr("content");
            title = title.substring(title.lastIndexOf('/') + 1);
            return HOST + "_" + title.trim();
        } catch (IOException e) {
            // Fall back to default album naming convention
            logger.info("Unable to find title at " + url);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public void rip() throws IOException {
        ripAlbum(this.url.toExternalForm(), this.workingDir);
        waitForThreads();
    }
    
    private void ripAlbum(String url, File subdir) throws IOException {
        logger.info("    Retrieving " + url);
        sendUpdate(STATUS.LOADING_RESOURCE, url);
        if (albumDoc == null) {
            albumDoc = Http.url(url).get();
        }

        int index = 0; // Both album index and image index
        if (albumDoc.select(".preview > span").size() > 0) {
            // Page contains subalbums (not images)
            for (Element subalbum : albumDoc.select("a.preview")) {
                ripSubalbumFromPreview(subalbum, subdir, ++index);
            }
        }
        else {
            // Page contains images
            for (Element thumb : albumDoc.select("img")) {
                downloadImage(thumb, subdir, ++index);
            }
        }
    }
    
    /**
     * @param subalbum Anchor element of a subalbum
     * @throws IOException
     */
    private void ripSubalbumFromPreview(Element subalbum, File subdir, int index) throws IOException {
        // Find + sanitize URL from Element
        String subUrl = subalbum.attr("href");
        subUrl = subUrl.replaceAll("\\.\\./", "");
        if (subUrl.startsWith("//")) {
            subUrl = "http:";
        }
        else if (!subUrl.startsWith("http://")) {
            subUrl = "http://www.8muses.com/" + subUrl;
        }
        // Prepend image index if enabled
        // Get album title
        String subTitle = subalbum.attr("alt");
        if (subTitle.equals("")) {
            subTitle = getGID(new URL(subUrl));
        }
        subTitle = Utils.filesystemSafe(subTitle);
        // Create path to subdirectory
        File subDir = new File(subdir.getAbsolutePath() + File.separator + subTitle);
        if (!subDir.exists()) {
            subDir.mkdirs();
        }
        albumDoc = null;
        ripAlbum(subUrl, subDir);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.warn("Interrupted whiel waiting to load next album");
        }
    }

    private void downloadImage(Element thumb, File subdir, int index) {
        // Find thumbnail image source
        String image = null;
        if (thumb.hasAttr("data-cfsrc")) {
            image = thumb.attr("data-cfsrc");
        }
        else if (thumb.hasAttr("src")) {
            image = thumb.attr("src");
        }
        else {
            logger.warn("Thumb does not havedata-cfsrc or src: " + thumb);
            return;
        }
        // Remove relative directory path naming
        image = image.replaceAll("\\.\\./", "");
        if (image.startsWith("//")) {
            image = "http:" + image;
        }
        // Convert from thumb URL to full-size
        if (image.contains("-cu_")) {
            image = image.replaceAll("-cu_[^.]+", "-me");
        }
        // Set download path
        try {
            URL imageURL = new URL(image);
            String saveAs = subdir.getAbsolutePath() + File.separator;
            if (Utils.getConfigBoolean("download.save_order", true)) {
                // Append image index
                saveAs += String.format("%03d_", index);
            }
            // Append image title
            saveAs += Utils.filesystemSafe(thumb.attr("title"));
            // Append extension
            saveAs += image.substring(image.lastIndexOf('.'));
            File saveFile = new File(saveAs);
            // Download
            addURLToDownload(imageURL, saveFile, thumb.baseUri(), null);
        } catch (IOException e) {
            logger.error("Failed to download image at " + image, e);
            sendUpdate(STATUS.DOWNLOAD_ERRORED, "Failed to download image at " + image);
        }
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(www\\.)?8muses\\.com/index/category/([a-zA-Z0-9\\-_]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()) {
            throw new MalformedURLException("Expected URL format: http://www.8muses.com/index/category/albumname, got: " + url);
        }
        return m.group(m.groupCount());
    }

}
