package com.rarchives.ripme.ripper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractRipper
                extends Observable
                implements RipperInterface, Runnable {

    protected static final Logger logger = Logger.getLogger(AbstractRipper.class);

    public static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:36.0) Gecko/20100101 Firefox/36.0";

    protected URL url;
    protected File workingDir;
    protected DownloadThreadPool threadPool;
    protected RipStatusHandler observer = null;

    protected boolean completed = true;

    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException;

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
     * @return True on success, flase on failure.
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
     */
    public abstract boolean addURLToDownload(URL url, File saveAs, String referrer, Map<String,String> cookies);

    public boolean addURLToDownload(URL url, String prefix, String subdirectory, String referrer, Map<String,String> cookies) {
        try {
            stopCheck();
        } catch (IOException e) {
            logger.debug("Ripper has been stopped");
            return false;
        }
        logger.debug("url: " + url + ", prefix: " + prefix + ", subdirectory" + subdirectory + ", referrer: " + referrer + ", cookies: " + cookies);
        String saveAs = url.toExternalForm();
        saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
        if (saveAs.indexOf(':') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf(':')); }
        File saveFileAs;
        try {
            if (!subdirectory.equals("")) {
                subdirectory = File.separator + subdirectory;
            }
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
    public boolean addURLToDownload(URL url, String prefix, String subdirectory) {
        return addURLToDownload(url, prefix, subdirectory, null, null);
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
    public boolean addURLToDownload(URL url, String prefix) {
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
    public int getCount() {
        return 1;
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    protected void checkIfComplete() {
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

    public abstract void setWorkingDir(URL url) throws IOException;

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
                AlbumRipper ripper = (AlbumRipper) constructor.newInstance(url);
                logger.debug("Found album ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (InstantiationException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (IllegalAccessException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (IllegalArgumentException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (InvocationTargetException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        for (Constructor<?> constructor : getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            try {
                VideoRipper ripper = (VideoRipper) constructor.newInstance(url);
                logger.debug("Found video ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (InstantiationException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (IllegalAccessException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (IllegalArgumentException e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            } catch (InvocationTargetException e) {
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
        List<Constructor<?>> constructors = new ArrayList<Constructor<?>>();
        for (Class<?> clazz : Utils.getClassesForPackage(pkg)) {
            if (AbstractRipper.class.isAssignableFrom(clazz)) {
                constructors.add( (Constructor<?>) clazz.getConstructor(URL.class) );
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

    public abstract int getCompletionPercentage();

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
        } catch (IOException e) {
            logger.error("Got exception while running ripper:", e);
            waitForThreads();
            sendUpdate(STATUS.RIP_ERRORED, e.getMessage());
        } catch (Exception e) {
            logger.error("Got exception while running ripper:", e);
            waitForThreads();
            sendUpdate(STATUS.RIP_ERRORED, e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void cleanup() {
        if (this.workingDir.list().length == 0) {
            // No files, delete the dir
            logger.info("Deleting empty directory " + this.workingDir);
            boolean deleteResult = this.workingDir.delete();
            if (!deleteResult) {
                logger.error("Unable to delete empty directory " +  this.workingDir);
            }
        }
    }

    public boolean sleep(int milliseconds) {
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
    public boolean isThisATest() {
        return thisIsATest;
    }
}
