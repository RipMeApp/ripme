package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

/**
 * Simplified ripper, designed for ripping from sites by parsing JSON.
 */
public abstract class AbstractJSONRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(AbstractJSONRipper.class);

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
        int imageIndex = 0;
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

                imageIndex += 1;
                logger.debug("Found image url #{} of album {}: {}", imageIndex, this.url, imageURL);
                setItemsTotal(Math.max(getItemsTotal(), imageIndex));
                downloadURL(new URI(imageURL).toURL(), imageIndex);
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

        logger.info("All items queued; total items: {}; url: {}", imageIndex, url);

        // Final total item count is now known
        setItemsTotal(imageIndex);

        if (getCrawlerThreadPool() != null) {
            logger.debug("Waiting for crawler threadpool: {}", url);
            getCrawlerThreadPool().waitForThreads(imageIndex, shouldStop, url);
        }

        waitForRipperThreads();
    }

    protected String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", index);
        }
        return prefix;
    }

    /*
     * ------ Methods copied from AlbumRipper ------
     */

    protected boolean allowDuplicates() {
        return false;
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL to decide filename.
     * @param url
     *      URL to download
     * @return
     *      True on success
     */
    protected boolean addURLToDownload(URL url) {
        // Use empty prefix and empty subdirectory
        return addURLToDownload(url, "", "");
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
            title = getAlbumTitle(this.url);
        } else {
            title = super.getAlbumTitle(this.url);
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

    /**
     * @return
     *      Integer between 0 and 100 defining the progress of the album rip.
     */
    @Override
    public int getCompletionPercentage() {
        double total = getTotalCount();
        if (total == 0) {
            return 0;
        }
        return (int) (100 * ( (itemsCompleted.size() + itemsErrored.size()) / total));
    }

    @Override
    public int getPendingCount() {
        DownloadThreadPool threadPool = getRipperThreadPool();
        if (threadPool != null) {
            return threadPool.getPendingThreadCount();
        }
        return itemsPending.size();
    }

}
