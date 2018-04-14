package com.rarchives.ripme.ripper;

import java.awt.Desktop;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.jsoup.HttpStatusException;

import com.rarchives.ripme.ui.RipStatusComplete;
import com.rarchives.ripme.ui.RipStatusHandler;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

import java.io.File;
import java.util.Scanner;

public abstract class AbstractRipper
                extends Observable
                implements RipperInterface, Runnable {

    protected static final Logger logger = Logger.getLogger(AbstractRipper.class);
    private final String URLHistoryFile = Utils.getURLHistoryFile();

    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";

    protected URL url;
    protected File workingDir;
    DownloadThreadPool threadPool;
    RipStatusHandler observer = null;

    private boolean completed = true;

    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException;
    public boolean hasASAPRipping() { return false; }
    // Everytime addUrlToDownload skips a already downloaded url this increases by 1
    public int alreadyDownloadedUrls = 0;
    private boolean shouldStop = false;
    private boolean thisIsATest = false;

    public void stop() {
        shouldStop = true;
    }
    public boolean isStopped() {
        return shouldStop;
    }
    protected void stopCheck() throws IOException {
        if (shouldStop) {
            throw new IOException("Ripping interrupted");
        }
    }


    /**
     * Adds a URL to the url history file
     * @param downloadedURL URL to check if downloaded
     */
    private void writeDownloadedURL(String downloadedURL) throws IOException {
        downloadedURL = normalizeUrl(downloadedURL);
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(URLHistoryFile);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(downloadedURL);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Normalize a URL
     * @param url URL to check if downloaded
     */
    public String normalizeUrl(String url) {
        return url;
    }
    
    /**
     * Checks to see if Ripme has already downloaded a URL
     * @param url URL to check if downloaded
     * @return 
     *      Returns true if previously downloaded.
     *      Returns false if not yet downloaded.
     */
    private boolean hasDownloadedURL(String url) {
        File file = new File(URLHistoryFile);
        url = normalizeUrl(url);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                final String lineFromFile = scanner.nextLine();
                if (lineFromFile.equals(url)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        return false;
    }


    /**
     * Ensures inheriting ripper can rip this URL, raises exception if not.
     * Otherwise initializes working directory and thread pool.
     *
     * @param url
     *      URL to rip.
     * @throws IOException
     *      If anything goes wrong.
     */
    public AbstractRipper(URL url) throws IOException {
        if (!canRip(url)) {
            throw new MalformedURLException("Unable to rip url: " + url);
        }
        this.url = sanitizeURL(url);
    }

    /**
     * Sets ripper's:
     *      Working directory
     *      Logger (for debugging)
     *      FileAppender
     *      Threadpool
     * @throws IOException 
     *      Always be prepared.
     */
    public void setup() throws IOException {
        setWorkingDir(this.url);
        Logger rootLogger = Logger.getRootLogger();
        FileAppender fa = (FileAppender) rootLogger.getAppender("FILE");
        if (fa != null) {
            fa.setFile(this.workingDir + File.separator + "log.txt");
            fa.activateOptions();
        }

        this.threadPool = new DownloadThreadPool();
    }

    public void setObserver(RipStatusHandler obs) {
        this.observer = obs;
    }

    /**
     * Queues image to be downloaded and saved.
     * @param url
     *      URL of the file
     * @param saveAs
     *      Path of the local file to save the content to.
     * @return True on success, false on failure.
     */
    public abstract boolean addURLToDownload(URL url, File saveAs);

    /**
     * Queues image to be downloaded and saved.
     * @param url
     *      URL of the file
     * @param saveAs
     *      Path of the local file to save the content to.
     * @param referrer
     *      The HTTP referrer to use while downloading this file.
     * @param cookies
     *      The cookies to send to the server while downloading this file.
     * @return
     *      True if downloaded successfully
     *      False if failed to download
     */
    protected abstract boolean addURLToDownload(URL url, File saveAs, String referrer, Map<String, String> cookies);

    /**
     * Queues image to be downloaded and saved.
     * @param url
     *      URL of the file
     * @param prefix
     *      Prefix for the downloaded file
     * @param subdirectory
     *      Path to get to desired directory from working directory
     * @param referrer
     *      The HTTP referrer to use while downloading this file.
     * @param cookies
     *      The cookies to send to the server while downloading this file.
     * @param fileName
     *      The name that file will be written to
     * @return 
     *      True if downloaded successfully
     *      False if failed to download
     */
    protected boolean addURLToDownload(URL url, String prefix, String subdirectory, String referrer, Map<String, String> cookies, String fileName) {
        // Don't re-add the url if it was downloaded in a previous rip
        if (Utils.getConfigBoolean("remember.url_history", true) && !isThisATest()) {
            if (hasDownloadedURL(url.toExternalForm())) {
                sendUpdate(STATUS.DOWNLOAD_WARN, "Already downloaded " + url.toExternalForm());
                alreadyDownloadedUrls += 1;
                return false;
            }
        }
        try {
            stopCheck();
        } catch (IOException e) {
            logger.debug("Ripper has been stopped");
            return false;
        }
        logger.debug("url: " + url + ", prefix: " + prefix + ", subdirectory" + subdirectory + ", referrer: " + referrer + ", cookies: " + cookies + ", fileName: " + fileName);
        String saveAs;
        if (fileName != null) {
            saveAs = fileName;
            // Get the extension of the file
            String extension = url.toExternalForm().substring(url.toExternalForm().lastIndexOf(".") + 1);
            saveAs = saveAs + "." + extension;
        } else {
            saveAs = url.toExternalForm();
            saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        }

        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
        if (saveAs.indexOf(':') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf(':')); }
        File saveFileAs;
        try {
            if (!subdirectory.equals("")) {
                subdirectory = File.separator + subdirectory;
            }
            prefix = Utils.filesystemSanitized(prefix);
            saveFileAs = new File(
                    workingDir.getCanonicalPath()
                    + subdirectory
                    + File.separator
                    + prefix
                    + saveAs);
        } catch (IOException e) {
            logger.error("[!] Error creating save file path for URL '" + url + "':", e);
            return false;
        }
        logger.debug("Downloading " + url + " to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            saveFileAs.getParentFile().mkdirs();
        }
        if (Utils.getConfigBoolean("remember.url_history", true) && !isThisATest()) {
            try {
                writeDownloadedURL(url.toExternalForm() + "\n");
            } catch (IOException e) {
                logger.debug("Unable to write URL history file");
            }
        }
        return addURLToDownload(url, saveFileAs, referrer, cookies);
    }

    /**
     * Queues file to be downloaded and saved. With options.
     * @param url
     *      URL to download.
     * @param prefix
     *      Prefix to prepend to the saved filename.
     * @param subdirectory
     *      Sub-directory of the working directory to save the images to.
     * @return True on success, flase on failure.
     */
    protected boolean addURLToDownload(URL url, String prefix, String subdirectory) {
        return addURLToDownload(url, prefix, subdirectory, null, null, null);
    }

    protected boolean addURLToDownload(URL url, String prefix, String subdirectory, String referrer, Map<String, String> cookies) {
        return addURLToDownload(url, prefix, subdirectory, referrer, cookies, null);
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL (and 'prefix') to decide filename.
     * @param url
     *      URL to download
     * @param prefix
     *      Text to append to saved filename.
     * @return True on success, flase on failure.
     */
    protected boolean addURLToDownload(URL url, String prefix) {
        // Use empty subdirectory
        return addURLToDownload(url, prefix, "");
    }


    /**
     * Waits for downloading threads to complete.
     */
    protected void waitForThreads() {
        logger.debug("Waiting for threads to finish");
        completed = false;
        threadPool.waitForThreads();
        checkIfComplete();
    }

    /**
     * Notifies observers that source is being retrieved.
     * @param url
     *      URL being retrieved
     */
    public void retrievingSource(String url) {
        RipStatusMessage msg = new RipStatusMessage(STATUS.LOADING_RESOURCE, url);
        if (observer != null) {
            observer.update(this, msg);
        }
    }

    /**
     * Notifies observers that a file download has completed.
     * @param url
     *      URL that was completed.
     * @param saveAs
     *      Where the downloaded file is stored.
     */
    public abstract void downloadCompleted(URL url, File saveAs);
    /**
     * Notifies observers that a file could not be downloaded (includes a reason).
     * @param url
     * @param reason
     */
    public abstract void downloadErrored(URL url, String reason);
    /**
     * Notify observers that a download could not be completed,
     * but was not technically an "error".
     * @param url
     * @param file
     */
    public abstract void downloadExists(URL url, File file);

    /**
     * @return Number of files downloaded.
     */
    int getCount() {
        return 1;
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    void checkIfComplete() {
        if (observer == null) {
            logger.debug("observer is null");
            return;
        }

        if (!completed) {
            completed = true;
            logger.info("   Rip completed!");

            RipStatusComplete rsc = new RipStatusComplete(workingDir, getCount());
            RipStatusMessage msg = new RipStatusMessage(STATUS.RIP_COMPLETE, rsc);
            observer.update(this, msg);

            Logger rootLogger = Logger.getRootLogger();
            FileAppender fa = (FileAppender) rootLogger.getAppender("FILE");
            if (fa != null) {
                logger.debug("Changing log file back to 'ripme.log'");
                fa.setFile("ripme.log");
                fa.activateOptions();
            }
            if (Utils.getConfigBoolean("urls_only.save", false)) {
                String urlFile = this.workingDir + File.separator + "urls.txt";
                try {
                    Desktop.getDesktop().open(new File(urlFile));
                } catch (IOException e) {
                    logger.warn("Error while opening " + urlFile, e);
                }
            }
        }
    }

    /**
     * Gets URL
     * @return 
     *      Returns URL that wants to be downloaded.
     */
    public URL getURL() {
        return url;
    }

    /**
     * @return
     *      Path to the directory in which all files
     *      ripped via this ripper will be stored.
     */
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public abstract void setWorkingDir(URL url) throws IOException;

    /**
     * 
     * @param url 
     *      The URL you want to get the title of.
     * @return
     *      host_URLid
     *      e.g. (for a reddit post)
     *      reddit_post_7mg2ur
     * @throws MalformedURLException 
     *      If any of those damned URLs gets malformed.
     */
    public String getAlbumTitle(URL url) throws MalformedURLException {
        return getHost() + "_" + getGID(url);
    }

    /**
     * Finds, instantiates, and returns a compatible ripper for given URL.
     * @param url
     *      URL to rip.
     * @return
     *      Instantiated ripper ready to rip given URL.
     * @throws Exception
     *      If no compatible rippers can be found.
     */
    public static AbstractRipper getRipper(URL url) throws Exception {
        for (Constructor<?> constructor : getRipperConstructors("com.rarchives.ripme.ripper.rippers")) {
            try {
                AlbumRipper ripper = (AlbumRipper) constructor.newInstance(url); // by design: can throw ClassCastException
                logger.debug("Found album ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (Exception e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        for (Constructor<?> constructor : getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            try {
                VideoRipper ripper = (VideoRipper) constructor.newInstance(url); // by design: can throw ClassCastException
                logger.debug("Found video ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (Exception e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        throw new Exception("No compatible ripper found");
    }

    /**
     * @param pkg
     *      The package name.
     * @return
     *      List of constructors for all eligible Rippers.
     * @throws Exception
     */
    public static List<Constructor<?>> getRipperConstructors(String pkg) throws Exception {
        List<Constructor<?>> constructors = new ArrayList<>();
        for (Class<?> clazz : Utils.getClassesForPackage(pkg)) {
            if (AbstractRipper.class.isAssignableFrom(clazz)) {
                constructors.add(clazz.getConstructor(URL.class));
            }
        }
        return constructors;
    }

    /**
     * Sends an update message to the relevant observer(s) on this ripper.
     * @param status 
     * @param message
     */
    public void sendUpdate(STATUS status, Object message) {
        if (observer == null) {
            return;
        }
        observer.update(this, new RipStatusMessage(status, message));
    }
    
    /**
     * Get the completion percentage.
     * @return 
     *      Percentage complete
     */
    public abstract int getCompletionPercentage();
    /**
     * @return 
     *      Text for status
     */
    public abstract String getStatusText();

    /**
     * Rips the album when the thread is invoked.
     */
    public void run() {
        try {
            rip();
        } catch (HttpStatusException e) {
            logger.error("Got exception while running ripper:", e);
            waitForThreads();
            sendUpdate(STATUS.RIP_ERRORED, "HTTP status code " + e.getStatusCode() + " for URL " + e.getUrl());
        } catch (Exception e) {
            logger.error("Got exception while running ripper:", e);
            waitForThreads();
            sendUpdate(STATUS.RIP_ERRORED, e.getMessage());
        } finally {
            cleanup();
        }
    }
    /**
     * Tries to delete any empty directories
     */
    private void cleanup() {
        if (this.workingDir.list().length == 0) {
            // No files, delete the dir
            logger.info("Deleting empty directory " + this.workingDir);
            boolean deleteResult = this.workingDir.delete();
            if (!deleteResult) {
                logger.error("Unable to delete empty directory " +  this.workingDir);
            }
        }
    }
    
    /**
     * Pauses thread for a set amount of time.
     * @param milliseconds
     *      Amount of time (in milliseconds) that the thread gets paused for
     * @return 
     *      True if paused successfully
     *      False if failed to pause/got interrupted.
     */
    protected boolean sleep(int milliseconds) {
        try {
            logger.debug("Sleeping " + milliseconds + "ms");
            Thread.sleep(milliseconds);
            return true;
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to load next page", e);
            return false;
        }
    }

    public void setBytesTotal(int bytes) {
        // Do nothing
    }
    public void setBytesCompleted(int bytes) {
        // Do nothing
    }

    /** Methods for detecting when we're running a test. */
    public void markAsTest() {
        logger.debug("THIS IS A TEST RIP");
        thisIsATest = true;
    }
    protected boolean isThisATest() {
        return thisIsATest;
    }
}
