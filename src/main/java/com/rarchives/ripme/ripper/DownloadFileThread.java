package com.rarchives.ripme.ripper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

/**
 * Thread for downloading files.
 * Includes retry logic, observer notifications, and other goodies.
 */
class DownloadFileThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadFileThread.class);

    private String referrer = "";
    private Map<String,String> cookies = new HashMap<>();

    private URL url;
    private File saveAs;
    private String prettySaveAs;
    private AbstractRipper observer;
    private int retries;
    private Boolean getFileExtFromMIME;

    private final int TIMEOUT;

    public DownloadFileThread(URL url, File saveAs, AbstractRipper observer, Boolean getFileExtFromMIME) {
        super();
        this.url = url;
        this.saveAs = saveAs;
        this.prettySaveAs = Utils.removeCWD(saveAs);
        this.observer = observer;
        this.retries = Utils.getConfigInteger("download.retries", 1);
        this.TIMEOUT = Utils.getConfigInteger("download.timeout", 60000);
        this.getFileExtFromMIME = getFileExtFromMIME;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    public void setCookies(Map<String,String> cookies) {
        this.cookies = cookies;
    }


    /**
     * Attempts to download the file. Retries as needed.
     * Notifies observers upon completion/error/warn.
     */
    public void run() {
        long fileSize = 0;
        int bytesTotal = 0;
        int bytesDownloaded = 0;
        if (saveAs.exists() && observer.tryResumeDownload()) {
            fileSize = saveAs.length();
        }
        try {
            observer.stopCheck();
        } catch (IOException e) {
            observer.downloadErrored(url, "Download interrupted");
            return;
        }
        if (saveAs.exists() && !observer.tryResumeDownload() && !getFileExtFromMIME ||
                Utils.fuzzyExists(new File(saveAs.getParent()), saveAs.getName()) && getFileExtFromMIME && !observer.tryResumeDownload()) {
            if (Utils.getConfigBoolean("file.overwrite", false)) {
                logger.info("[!] Deleting existing file" + prettySaveAs);
                saveAs.delete();
            } else {
                logger.info("[!] Skipping " + url + " -- file already exists: " + prettySaveAs);
                observer.downloadExists(url, saveAs);
                return;
            }
        }
        URL urlToDownload = this.url;
        boolean redirected = false;
        int tries = 0; // Number of attempts to download
        do {
            tries += 1;
            InputStream bis = null; OutputStream fos = null;
            try {
                logger.info("    Downloading file: " + urlToDownload + (tries > 0 ? " Retry #" + tries : ""));
                observer.sendUpdate(STATUS.DOWNLOAD_STARTED, url.toExternalForm());

                // Setup HTTP request
                HttpURLConnection huc;
                if (this.url.toString().startsWith("https")) {
                    huc = (HttpsURLConnection) urlToDownload.openConnection();
                }
                else {
                    huc = (HttpURLConnection) urlToDownload.openConnection();
                }
                huc.setInstanceFollowRedirects(true);
                huc.setConnectTimeout(TIMEOUT);
                huc.setRequestProperty("accept",  "*/*");
                if (!referrer.equals("")) {
                    huc.setRequestProperty("Referer", referrer); // Sic
                }
                huc.setRequestProperty("User-agent", AbstractRipper.USER_AGENT);
                String cookie = "";
                for (String key : cookies.keySet()) {
                    if (!cookie.equals("")) {
                        cookie += "; ";
                    }
                    cookie += key + "=" + cookies.get(key);
                }
                huc.setRequestProperty("Cookie", cookie);
                if (observer.tryResumeDownload()) {
                    if (fileSize != 0) {
                        huc.setRequestProperty("Range", "bytes=" + fileSize + "-");
                    }
                }
                logger.debug("Request properties: " + huc.getRequestProperties());
                huc.connect();

                int statusCode = huc.getResponseCode();
                logger.debug("Status code: " + statusCode);
                if (statusCode != 206 && observer.tryResumeDownload() && saveAs.exists()) {
                    // TODO find a better way to handle servers that don't support resuming downloads then just erroring out
                    throw new IOException("Server doesn't support resuming downloads");
                }
                if (statusCode  / 100 == 3) { // 3xx Redirect
                    if (!redirected) {
                        // Don't increment retries on the first redirect
                        tries--;
                        redirected = true;
                    }
                    String location = huc.getHeaderField("Location");
                    urlToDownload = new URL(location);
                    // Throw exception so download can be retried
                    throw new IOException("Redirect status code " + statusCode + " - redirect to " + location);
                }
                if (statusCode / 100 == 4) { // 4xx errors
                    logger.error("[!] Non-retriable status code " + statusCode + " while downloading from " + url);
                    observer.downloadErrored(url, "Non-retriable status code " + statusCode + " while downloading " + url.toExternalForm());
                    return; // Not retriable, drop out.
                }
                if (statusCode / 100 == 5) { // 5xx errors
                    observer.downloadErrored(url, "Retriable status code " + statusCode + " while downloading " + url.toExternalForm());
                    // Throw exception so download can be retried
                    throw new IOException("Retriable status code " + statusCode);
                }
                if (huc.getContentLength() == 503 && urlToDownload.getHost().endsWith("imgur.com")) {
                    // Imgur image with 503 bytes is "404"
                    logger.error("[!] Imgur image is 404 (503 bytes long): " + url);
                    observer.downloadErrored(url, "Imgur image is 404: " + url.toExternalForm());
                    return;
                }

                // If the ripper is using the bytes progress bar set bytesTotal to huc.getContentLength()
                if (observer.useByteProgessBar()) {
                    bytesTotal = huc.getContentLength();
                    observer.setBytesTotal(bytesTotal);
                    observer.sendUpdate(STATUS.TOTAL_BYTES, bytesTotal);
                    logger.debug("Size of file at " + this.url + " = " + bytesTotal + "b");
                }

                // Save file
                bis = new BufferedInputStream(huc.getInputStream());

                // Check if we should get the file ext from the MIME type
                if (getFileExtFromMIME) {
                    String fileExt = URLConnection.guessContentTypeFromStream(bis);
                    if (fileExt != null) {
                        fileExt = fileExt.replaceAll("image/", "");
                        saveAs = new File(saveAs.toString() + "." + fileExt);
                    } else {
                        logger.error("Was unable to get content type from stream");
                        // Try to get the file type from the magic number
                        byte[] magicBytes = new byte[8];
                        bis.read(magicBytes,0, 5);
                        bis.reset();
                        fileExt = Utils.getEXTFromMagic(magicBytes);
                        if (fileExt != null) {
                            saveAs = new File(saveAs.toString() + "." + fileExt);
                        } else {
                            logger.error("Was unable to get content type using magic number");
                            logger.error("Magic number was: " + Arrays.toString(magicBytes));
                        }
                    }
                }
                // If we're resuming a download we append data to the existing file
                if (statusCode == 206) {
                    fos = new FileOutputStream(saveAs, true);
                } else {
                    fos = new FileOutputStream(saveAs);
                }
                byte[] data = new byte[1024 * 256];
                int bytesRead;
                while ( (bytesRead = bis.read(data)) != -1) {
                    try {
                        observer.stopCheck();
                    } catch (IOException e) {
                        observer.downloadErrored(url, "Download interrupted");
                        return;
                    }
                    fos.write(data, 0, bytesRead);
                    if (observer.useByteProgessBar()) {
                        bytesDownloaded += bytesRead;
                        observer.setBytesCompleted(bytesDownloaded);
                        observer.sendUpdate(STATUS.COMPLETED_BYTES, bytesDownloaded);
                    }
                }
                bis.close();
                fos.close();
                break; // Download successful: break out of infinite loop
            } catch (HttpStatusException hse) {
                logger.debug("HTTP status exception", hse);
                logger.error("[!] HTTP status " + hse.getStatusCode() + " while downloading from " + urlToDownload);
                if (hse.getStatusCode() == 404 && Utils.getConfigBoolean("errors.skip404", false)) {
                    observer.downloadErrored(url, "HTTP status code " + hse.getStatusCode() + " while downloading " + url.toExternalForm());
                    return;
                }
            } catch (IOException e) {
                logger.debug("IOException", e);
                logger.error("[!] Exception while downloading file: " + url + " - " + e.getMessage());
            } finally {
                // Close any open streams
                try {
                    if (bis != null) { bis.close(); }
                } catch (IOException e) { }
                try {
                    if (fos != null) { fos.close(); }
                } catch (IOException e) { }
            }
            if (tries > this.retries) {
                logger.error("[!] Exceeded maximum retries (" + this.retries + ") for URL " + url);
                observer.downloadErrored(url, "Failed to download " + url.toExternalForm());
                return;
            }
        } while (true);
        observer.downloadCompleted(url, saveAs);
        logger.info("[+] Saved " + url + " as " + this.prettySaveAs);
    }

}
