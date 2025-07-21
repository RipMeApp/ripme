package com.rarchives.ripme.ripper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.net.ssl.HttpsURLConnection;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;

/**
 * Thread for downloading files.
 * Includes retry logic, observer notifications, and other goodies.
 */
class DownloadVideoThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(DownloadVideoThread.class);

    private final TokenedUrlGetter tokenedUrlGetter; // Some URLs may be valid for a limited time. This should get a fresh url
    private final RipUrlId ripUrlId;
    private final Path directory;
    private String filename;
    private final AbstractRipper observer;
    private final int retries;

    public DownloadVideoThread(TokenedUrlGetter tug, RipUrlId ripUrlId, Path directory, String filename, AbstractRipper observer) {
        super();
        this.tokenedUrlGetter = tug;
        this.ripUrlId = ripUrlId;
        this.directory = directory;
        this.filename = filename;
        this.observer = observer;
        this.retries = Utils.getConfigInteger("download.retries", 1);
    }

    /**
     * Attempts to download the file. Retries as needed.
     * Notifies observers upon completion/error/warn.
     */
    @Override
    public void run() {
        if (observer.isStopped()) {
            // TODO create status for gracefully-stopped download
            observer.downloadErrored(ripUrlId, "Download interrupted");
            return;
        }
        URL url = null;
        try {
            url = tokenedUrlGetter.getTokenedUrl();
        } catch (HttpStatusException e) {
            observer.downloadErrored(ripUrlId, "Failed to get URL for " + ripUrlId);
            logger.error("[!] Failed to get URL for " + ripUrlId);
            return; // do not retry
        } catch (IOException | URISyntaxException e) {
            logger.error("[!] Failed to get URL for " + ripUrlId, e);
            observer.downloadErrored(ripUrlId, "Failed to get URL for " + ripUrlId);
            return; // do not retry
        }
        if (filename == null) {
            // Strip token query parameters
            filename = Path.of(url.getPath()).getFileName().toString();
        }
        if (AbstractRipper.shouldIgnoreExtension(url)) {
            observer.sendUpdate(STATUS.DOWNLOAD_SKIP, "Skipping " + url.toExternalForm() + " - ignored extension");
            return;
        }
        if (!Files.exists(directory)) {
            logger.info("[+] Creating directory: " + directory);
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                logger.error("Error creating directory", e);
                observer.downloadErrored(ripUrlId, "Error creating directory: " + directory + " ; " +  e.getMessage());
                return;
            }
        }
        Path saveAs = directory.resolve(filename);
        String prettySaveAs = Utils.removeCWD(saveAs);

        if (Files.exists(saveAs)) {
            if (Utils.getConfigBoolean("file.overwrite", false)) {
                logger.info("[!] Deleting existing file" + prettySaveAs);
                try {
                    Files.delete(saveAs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("[!] Skipping " + url + " -- file already exists: " + prettySaveAs);
                observer.downloadExists(ripUrlId, saveAs);
                return;
            }
        }

        int bytesTotal, bytesDownloaded = 0;
        try {
            bytesTotal = getTotalBytes(url);
        } catch (IOException e) {
            logger.error("Failed to get file size at " + url, e);
            observer.downloadErrored(ripUrlId, "Failed to get file size of " + url);
            return;
        }
        observer.setBytesTotal(bytesTotal);
        observer.sendUpdate(STATUS.TOTAL_BYTES, bytesTotal);
        logger.debug("Size of file at " + url + " = " + bytesTotal + "b");

        int tries = 0; // Number of attempts to download
        do {
            InputStream bis = null; OutputStream fos = null;
            byte[] data = new byte[1024 * 256];
            int bytesRead;
            try {
                logger.info("    Downloading file: " + url + (tries > 0 ? " Try #" + tries+1 : ""));
                observer.sendUpdate(STATUS.DOWNLOAD_STARTED, url.toExternalForm());

                // Setup HTTP request
                HttpURLConnection huc;
                if (url.getProtocol().equals("https")) {
                    huc = (HttpsURLConnection) url.openConnection();
                }
                else {
                    huc = (HttpURLConnection) url.openConnection();
                }
                huc.setInstanceFollowRedirects(true);
                huc.setConnectTimeout(0); // Never timeout
                huc.setRequestProperty("accept",  "*/*");
                huc.setRequestProperty("Referer", url.toExternalForm()); // Sic
                huc.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
                tries += 1;
                logger.debug("Request properties: " + huc.getRequestProperties().toString());
                huc.connect();
                // Check status code
                bis = new BufferedInputStream(huc.getInputStream());
                fos = Files.newOutputStream(saveAs);
                while ( (bytesRead = bis.read(data)) != -1) {
                    if (observer.isPanicked()) {
                        observer.downloadErrored(ripUrlId, "Download interrupted");
                        return;
                    }
                    fos.write(data, 0, bytesRead);
                    observer.sendUpdate(STATUS.CHUNK_BYTES, bytesRead);
                    bytesDownloaded += bytesRead;
                    observer.setBytesCompleted(bytesDownloaded);
                    observer.sendUpdate(STATUS.COMPLETED_BYTES, bytesDownloaded);
                }
                bis.close();
                fos.close();
                break; // Download successful: break out of infinite loop
            } catch (IOException e) {
                logger.error("[!] Exception while downloading file: " + url + " - " + e.getMessage(), e);
            } finally {
                // Close any open streams
                try {
                    if (bis != null) { bis.close(); }
                } catch (IOException ignored) { }
                try {
                    if (fos != null) { fos.close(); }
                } catch (IOException ignored) { }
            }
            if (tries > this.retries) {
                logger.error("[!] Exceeded maximum retries (" + this.retries + ") for URL " + url);
                observer.downloadErrored(ripUrlId, "Failed to download " + url.toExternalForm());
                return;
            }

            // get fresh URL for the next attempt
            try {
                url = tokenedUrlGetter.getTokenedUrl();
            } catch (HttpStatusException e) {
                observer.downloadErrored(ripUrlId, "Failed to get URL for " + ripUrlId);
                logger.error("[!] Failed to get URL for " + ripUrlId);
                return; // do not retry
            } catch (IOException | URISyntaxException e) {
                logger.error("[!] Failed to get URL for " + ripUrlId, e);
                observer.downloadErrored(ripUrlId, "Failed to get URL for " + ripUrlId);
                return; // do not retry
            }

        } while (true);
        observer.downloadCompleted(ripUrlId, saveAs);
        logger.info("[+] Saved " + url + " as " + prettySaveAs);
    }

    /**
     * @param url
     *      Target URL
     * @return 
     *      Returns connection length
     */
    private int getTotalBytes(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.setRequestProperty("accept",  "*/*");
        conn.setRequestProperty("Referer", url.toExternalForm()); // Sic
        conn.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
        return conn.getContentLength();
    }

}