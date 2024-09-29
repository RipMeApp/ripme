package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public abstract class QueueingRipper extends AbstractRipper {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final Set<URL> itemsPending = Collections.synchronizedSet(new HashSet<>());
    private final Set<URL> itemsCompleted = Collections.synchronizedSet(new HashSet<>());
    private final Map<URL, String> itemsErrored = Collections.synchronizedMap(new HashMap<>());

    public QueueingRipper(URL url) throws IOException {
        super(url);
    }

    /*
     * ------ Methods copied from AlbumRipper. ------
     * This removes AlbumnRipper's usage from this class.
     */
    protected boolean allowDuplicates() {
        return false;
    }

    @Override
    /*
      Returns total amount of files attempted.
     */
    public int getCount() {
        return itemsCompleted.size() + itemsErrored.size();
    }

    @Override
    /*
      Queues multiple URLs of single images to download from a single Album URL
     */
    public boolean addURLToDownload(URL url, Path saveAs, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        // Only download one file if this is a test.
        if (isThisATest() && (itemsCompleted.size() > 0 || itemsErrored.size() > 0)) {
            stop();
            itemsPending.clear();
            return false;
        }
        if (!allowDuplicates()
                && ( itemsPending.contains(url)
                        || itemsCompleted.contains(url)
                        || itemsErrored.containsKey(url) )) {
            // Item is already downloaded/downloading, skip it.
            logger.info("[!] Skipping " + url + " -- already attempted: " + Utils.removeCWD(saveAs));
            return false;
        }
        if (shouldIgnoreURL(url)) {
            sendUpdate(STATUS.DOWNLOAD_SKIP, "Skipping " + url.toExternalForm() + " - ignored extension");
            return false;
        }
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            final var urlFile = Paths.get(this.workingDir + "/urls.txt");
            final var text = url.toExternalForm() + System.lineSeparator();
            try {
                Files.write(urlFile, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                itemsCompleted.add(url);
            } catch (final IOException e) {
                logger.error("Error while writing to " + urlFile, e);
            }
        }
        else {
            addURLToPending(url);
            downloadInBackground(url, saveAs, referrer, cookies, getFileExtFromMIME);
        }

        return true;
    }

    /**
     * Start downloading of the given url in the background, using the internal threadpool.
     * @param url
     * @param saveAs
     * @param referrer
     * @param cookies
     * @param getFileExtFromMIME
     */
    protected void downloadInBackground(URL url, Path saveAs, String referrer, Map<String, String> cookies, Boolean getFileExtFromMIME) {
        final var dft = new DownloadFileThread(url,  saveAs.toFile(),  this, getFileExtFromMIME);
        if (referrer != null) {
            dft.setReferrer(referrer);
        }
        if (cookies != null) {
            dft.setCookies(cookies);
        }
        threadPool.addThread(dft);
    }

    @Override
    public boolean addURLToDownload(URL url, Path saveAs) {
        return addURLToDownload(url, saveAs, null, null, false);
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

    protected void addURLToPending(URL url) {
        if (observer == null) {
            return;
        }
        itemsPending.add(url);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_PROGRESSED, url));
    }

    @Override
    /*
      Cleans up & tells user about successful download
     */
    public void downloadCompleted(URL url, Path saveAs) {
        if (observer == null) {
            return;
        }
        try {
            final var path = Utils.removeCWD(saveAs);
            final var msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            itemsPending.remove(url);
            itemsCompleted.add(url);
            observer.update(this, msg);

            checkIfComplete();
        } catch (final Exception e) {
            logger.error("Exception while updating observer: ", e);
        }
    }

    @Override
    /*
     * Cleans up & tells user about failed download.
     */
    public void downloadErrored(URL url, String reason) {
        if (observer == null) {
            return;
        }
        itemsPending.remove(url);
        itemsErrored.put(url, reason);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, url + " : " + reason));

        checkIfComplete();
    }

    @Override
    /*
      Tells user that a single file in the album they wish to download has
      already been downloaded in the past.
     */
    public void downloadExists(URL url, Path file) {
        if (observer == null) {
            return;
        }

        itemsPending.remove(url);
        itemsCompleted.add(url);
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
            super.checkIfComplete();
        }
    }

    /**
     * @return
     *      Integer between 0 and 100 defining the progress of the album rip.
     */
    @Override
    public int getCompletionPercentage() {
        final double total = itemsPending.size()  + itemsErrored.size() + itemsCompleted.size();
        return (int) (100 * ( (total - itemsPending.size()) / total));
    }

    /**
     * @return
     *      Human-readable information on the status of the current rip.
     */
    @Override
    public String getStatusText() {
        return getCompletionPercentage() +
                "% " +
                "- Pending: " + itemsPending.size() +
                ", Completed: " + itemsCompleted.size() +
                ", Errored: " + itemsErrored.size();
    }

}
