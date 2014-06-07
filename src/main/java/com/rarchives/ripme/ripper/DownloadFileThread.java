package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.HttpStatusException;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

/**
 * Thread for downloading files.
 * Includes retry logic, observer notifications, and other goodies.
 */
public class DownloadFileThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadFileThread.class);

    private String referrer = "";
    private Map<String,String> cookies = new HashMap<String,String>();

    private URL url;
    private File saveAs;
    private String prettySaveAs;
    private AbstractRipper observer;
    private int retries;

    private final int TIMEOUT;
    private final int MAX_BODY_SIZE;

    public DownloadFileThread(URL url, File saveAs, AbstractRipper observer) {
        super();
        this.url = url;
        this.saveAs = saveAs;
        this.prettySaveAs = Utils.removeCWD(saveAs);
        this.observer = observer;
        this.retries = Utils.getConfigInteger("download.retries", 1);
        this.TIMEOUT = Utils.getConfigInteger("download.timeout", 60000);
        this.MAX_BODY_SIZE = Utils.getConfigInteger("download.max_bytes", 1024 * 1024 * 100);
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
        try {
            observer.stopCheck();
        } catch (IOException e) {
            observer.downloadErrored(url, "Download interrupted");
            return;
        }
        if (saveAs.exists()) {
            if (Utils.getConfigBoolean("file.overwrite", false)) {
                logger.info("[!] Deleting existing file" + prettySaveAs);
                saveAs.delete();
            } else {
                logger.info("[!] Skipping " + url + " -- file already exists: " + prettySaveAs);
                observer.downloadProblem(url, "File already exists: " + prettySaveAs);
                return;
            }
        }

        int tries = 0; // Number of attempts to download
        do {
            try {
                logger.info("    Downloading file: " + url + (tries > 0 ? " Retry #" + tries : ""));
                observer.sendUpdate(STATUS.DOWNLOAD_STARTED, url.toExternalForm());
                tries += 1;
                Response response;
                response = Jsoup.connect(url.toExternalForm())
                                .ignoreContentType(true)
                                .userAgent(AbstractRipper.USER_AGENT)
                                .header("accept", "*/*")
                                .timeout(TIMEOUT)
                                .maxBodySize(MAX_BODY_SIZE)
                                .cookies(cookies)
                                .referrer(referrer)
                                .execute();
                if (response.statusCode() != 200) {
                    logger.error("[!] Non-OK status code " + response.statusCode() + " while downloading from " + url);
                    observer.downloadErrored(url, "Non-OK status code " + response.statusCode() + " while downloading " + url.toExternalForm());
                    return;
                }
                byte[] bytes = response.bodyAsBytes();
                if (bytes.length == 503 && url.getHost().endsWith("imgur.com")) {
                    // Imgur image with 503 bytes is "404"
                    logger.error("[!] Imgur image is 404 (503 bytes long): " + url);
                    observer.downloadErrored(url, "Imgur image is 404: " + url.toExternalForm());
                    return;
                }
                FileOutputStream out = new FileOutputStream(saveAs);
                out.write(response.bodyAsBytes());
                out.close();
                break; // Download successful: break out of infinite loop
            } catch (HttpStatusException hse) {
                logger.error("[!] HTTP status " + hse.getStatusCode() + " while downloading from " + url);
                observer.downloadErrored(url, "HTTP status code " + hse.getStatusCode() + " while downloading " + url.toExternalForm());
                if (hse.getStatusCode() == 404 && Utils.getConfigBoolean("errors.skip404", false)) {
                    return;
                }
            } catch (IOException e) {
                logger.error("[!] Exception while downloading file: " + url + " - " + e.getMessage(), e);
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
