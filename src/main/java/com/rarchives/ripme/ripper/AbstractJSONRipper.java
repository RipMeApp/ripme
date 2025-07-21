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

    private final Set<RipUrlId> itemsPending = Collections.synchronizedSet(new HashSet<>());
    private final Map<RipUrlId, Path> itemsCompleted = Collections.synchronizedMap(new HashMap<>());
    private final Map<RipUrlId, String> itemsErrored = Collections.synchronizedMap(new HashMap<>());

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

    /*
     * ------ Methods copied from AlbumRipper ------
     */

    protected boolean allowDuplicates() {
        return false;
    }

    @Override
    /**
     * Returns total amount of files attempted.
     */
    public int getCount() {
        return itemsCompleted.size() + itemsErrored.size();
    }

    @Override
    /**
     * Queues multiple URLs of single images to download from a single Album URL
     */
    public boolean addURLToDownload(TokenedUrlGetter tug, RipUrlId ripUrlId, Path directory, String filename, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        // Only download one file if this is a test.
        if (super.isThisATest() && (itemsCompleted.size() > 0 || itemsErrored.size() > 0)) {
            stop();
            itemsPending.clear();
            return false;
        }
        if (!allowDuplicates()
                && ( itemsPending.contains(ripUrlId)
                  || itemsCompleted.containsKey(ripUrlId)
                  || itemsErrored.containsKey(ripUrlId) )) {
            // Item is already downloaded/downloading, skip it.
            // TODO print path if in itemsCompleted or itemsErrored
            logger.info("[!] Skipping " + ripUrlId + " -- already attempted: " + Utils.removeCWD(directory));
            return false;
        }
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            Path urlFile = Paths.get(this.workingDir + "/urls.txt");
            URL url = null;
            try {
                url = tug.getTokenedUrl();
            } catch (IOException | URISyntaxException e) {
                logger.error("Unable to get URL for {}", ripUrlId, e);
                itemsErrored.put(ripUrlId, e.getMessage());
                return false;
            }
            if (shouldIgnoreURL(url)) {
                sendUpdate(STATUS.DOWNLOAD_SKIP, "Skipping " + url.toExternalForm() + " - ignored extension");
                return false;
            }
            String text = url.toExternalForm() + System.lineSeparator();
            try {
                Files.write(urlFile, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                itemsCompleted.put(ripUrlId, urlFile);
            } catch (IOException e) {
                logger.error("Error while writing to " + urlFile, e);
            }
            return true;
        }
        else {
            itemsPending.add(ripUrlId);
            DownloadFileThread dft = new DownloadFileThread(tug, ripUrlId, directory, filename, this, getFileExtFromMIME);
            if (referrer != null) {
                dft.setReferrer(referrer);
            }
            if (cookies != null) {
                dft.setCookies(cookies);
            }
            threadPool.addThread(dft);
        }

        return true;
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
     * Cleans up & tells user about successful download
     */
    @Override
    public void downloadCompleted(RipUrlId ripUrlId, Path saveAs) {
        if (observer == null) {
            return;
        }
        try {
            String path = Utils.removeCWD(saveAs);
            RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            itemsPending.remove(ripUrlId);
            itemsCompleted.put(ripUrlId, saveAs);
            observer.update(this, msg);

            checkIfComplete();
        } catch (Exception e) {
            logger.error("Exception while updating observer: ", e);
        }
    }

    /**
     * Cleans up & tells user about failed download.
     */
    @Override
    public void downloadErrored(RipUrlId ripUrlId, String reason) {
        if (observer == null) {
            return;
        }
        itemsPending.remove(ripUrlId);
        itemsErrored.put(ripUrlId, reason);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, ripUrlId + " : " + reason));

        checkIfComplete();
    }

    /**
     * Tells user that a single file in the album they wish to download has
     * already been downloaded in the past.
     */
    @Override
    public void downloadExists(RipUrlId ripUrlId, Path file) {
        if (observer == null) {
            return;
        }

        itemsPending.remove(ripUrlId);
        itemsCompleted.put(ripUrlId, file);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_WARN, url + " already saved as " + file));

        checkIfComplete();
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    @Override
    protected void checkIfComplete() {
        if (observer == null) {
            return;
        }
        if (itemsPending.isEmpty()) {
            notifyComplete();
        }
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
        double total = itemsPending.size()  + itemsErrored.size() + itemsCompleted.size();
        return (int) (100 * ( (total - itemsPending.size()) / total));
    }

    /**
     * @return
     *      Human-readable information on the status of the current rip.
     */
    @Override
    public String getStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompletionPercentage())
          .append("% ")
          .append("- Pending: "  ).append(itemsPending.size())
          .append(", Completed: ").append(itemsCompleted.size())
          .append(", Errored: "  ).append(itemsErrored.size());
        return sb.toString();
    }


}
