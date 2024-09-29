package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONObject;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

/**
 * Simplified ripper, designed for ripping from sites by parsing JSON.
 */
public abstract class AbstractJSONRipper extends QueueingRipper {

    protected AbstractJSONRipper(URL url) throws IOException {
        super(url);
    }

    protected abstract String getDomain();
    @Override
    public abstract String getHost();

    protected abstract JSONObject getFirstPage() throws IOException, URISyntaxException;
    protected JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        throw new IOException("getNextPage not implemented");
    }
    protected abstract List<String> getURLsFromJSON(JSONObject json);
    protected abstract void downloadURL(URL url, int index);
    private DownloadThreadPool getThreadPool() {
        return null;
    }

    protected boolean keepSortOrder() {
        return true;
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(getDomain());
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        return url;
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        int index = 0;
        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        JSONObject json = getFirstPage();

        while (json != null) {
            List<String> imageURLs = getURLsFromJSON(json);

            if (alreadyDownloadedUrls >= Utils.getConfigInteger("history.end_rip_after_already_seen", 1000000000) && !isThisATest()) {
                 sendUpdate(STATUS.DOWNLOAD_COMPLETE, "Already seen the last " + alreadyDownloadedUrls + " images ending rip");
                 break;
            }

            // Remove all but 1 image
            if (isThisATest()) {
                while (imageURLs.size() > 1) {
                    imageURLs.remove(1);
                }
            }

            if (imageURLs.isEmpty() && !hasASAPRipping()) {
                throw new IOException("No images found at " + this.url);
            }

            for (String imageURL : imageURLs) {
                if (isStopped()) {
                    break;
                }

                index += 1;
                logger.debug("Found image url #" + index+ ": " + imageURL);
                downloadURL(new URI(imageURL).toURL(), index);
            }

            if (isStopped() || isThisATest()) {
                break;
            }

            try {
                sendUpdate(STATUS.LOADING_RESOURCE, "next page");
                json = getNextPage(json);
            } catch (IOException | URISyntaxException e) {
                logger.info("Can't get next page: " + e.getMessage());
                break;
            }
        }

        // If they're using a thread pool, wait for it.
        if (getThreadPool() != null) {
            logger.debug("Waiting for threadpool " + getThreadPool().getClass().getName());
            getThreadPool().waitForThreads();
        }
        waitForThreads();
    }

    protected String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", index);
        }
        return prefix;
    }

    /**
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the working directory should be saved.
     * @throws
     *      IOException
     */
    @Override
    public void setWorkingDir(URL url) throws IOException, URISyntaxException {
        Path wd = Utils.getWorkingDirectory();
        String title;
        if (Utils.getConfigBoolean("album_titles.save", true)) {
            title = getAlbumTitle();
        } else {
            title = super.getAlbumTitle();
        }
        logger.debug("Using album title '" + title + "'");

        title = Utils.filesystemSafe(title);
        wd = wd.resolve(title);
        if (!Files.exists(wd)) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(wd));
            Files.createDirectory(wd);
        }
        this.workingDir = wd.toFile();
        logger.info("Set working directory to: {}", this.workingDir);
    }
}
