package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

// Should this file even exist? It does the same thing as abstractHTML ripper

/**'
 * For ripping delicious albums off the interwebz.
 * @deprecated Use AbstractHTMLRipper instead.
 */
@Deprecated
public abstract class AlbumRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(AlbumRipper.class);

    protected AlbumRipper(URL url) throws IOException {
        super(url);
    }

    public abstract boolean canRip(URL url);
    public abstract URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException;
    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException, URISyntaxException;

    protected boolean allowDuplicates() {
        return false;
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
            if (AbstractRipper.shouldIgnoreExtension(url)) {
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
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the working directory should be saved.
     * @throws
     *      IOException
     */
    @Override
    public void setWorkingDir(URL url) throws IOException, URISyntaxException {
        Path wd = Utils.getWorkingDirectory();
        // TODO - change to nio
        String path = wd.toAbsolutePath().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        String title;
        if (Utils.getConfigBoolean("album_titles.save", true)) {
            title = getAlbumTitle(this.url);
        } else {
            title = super.getAlbumTitle(this.url);
        }
        logger.debug("Using album title '" + title + "'");

        title = Utils.filesystemSafe(title);
        path += title;
        path = Utils.getOriginalDirectory(path) + File.separator;   // check for case sensitive (unix only)

        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir.toPath()));
            this.workingDir.mkdirs();
        }
        logger.debug("Set working directory to: " + this.workingDir);
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
