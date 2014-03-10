package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public abstract class AbstractRipper 
                extends Observable
                implements RipperInterface, Runnable {

    private static final Logger logger = Logger.getLogger(AbstractRipper.class);

    protected static final String USER_AGENT = 
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:27.0) Gecko/20100101 Firefox/27.0";

    protected URL url;
    protected File workingDir;
    protected DownloadThreadPool threadPool;
    protected Observer observer = null;

    protected Map<URL, File> itemsPending = new HashMap<URL, File>();
    protected Map<URL, File> itemsCompleted = new HashMap<URL, File>();
    protected Map<URL, String> itemsErrored = new HashMap<URL, String>();
    protected boolean completed = true;

    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException;

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
        setWorkingDir(url);
        this.threadPool = new DownloadThreadPool();
    }

    public void setObserver(Observer obs) {
        this.observer = obs;
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL to decide filename.
     * @param url
     *      URL to download
     */
    public void addURLToDownload(URL url) {
        // Use empty prefix and empty subdirectory
        addURLToDownload(url, "", "");
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL (and 'prefix') to decide filename.
     * @param url
     *      URL to download
     * @param prefix
     *      Text to append to saved filename.
     */
    public void addURLToDownload(URL url, String prefix) {
        // Use empty subdirectory
        addURLToDownload(url, prefix, "");
    }

    /**
     * Queues image to be downloaded and saved.
     * @param url
     *      URL of the file
     * @param saveAs
     *      Path of the local file to save the content to.
     */
    public void addURLToDownload(URL url, File saveAs) {
        if (itemsPending.containsKey(url)
                || itemsCompleted.containsKey(url)
                || itemsErrored.containsKey(url)) {
            // Item is already downloaded/downloading, skip it.
            logger.info("Skipping " + url + " -- already attempted: " + Utils.removeCWD(saveAs));
            return;
        }
        itemsPending.put(url, saveAs);
        threadPool.addThread(new DownloadFileThread(url, saveAs, this));
    }

    /**
     * Queues file to be downloaded and saved. With options.
     * @param url
     *      URL to download.
     * @param prefix
     *      Prefix to prepend to the saved filename.
     * @param subdirectory
     *      Sub-directory of the working directory to save the images to.
     */
    public void addURLToDownload(URL url, String prefix, String subdirectory) {
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
            return;
        }
        logger.debug("Downloading " + url + " to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            saveFileAs.getParentFile().mkdirs();
        }
        addURLToDownload(url, saveFileAs);
    }
    
    /**
     * Waits for downloading threads to complete.
     */
    protected void waitForThreads() {
        completed = false;
        threadPool.waitForThreads();
        checkIfComplete();
    }

    /**
     * Notifies observers that source is being retrieved.
     * @param url
     *      URL being retrieved
     */
    public void retrievingSource(URL url) {
        RipStatusMessage msg = new RipStatusMessage(STATUS.LOADING_RESOURCE, url);
        observer.update(this,  msg);
        observer.notifyAll();
    }

    /**
     * Notifies observers that a file download has completed.
     * @param url
     *      URL that was completed.
     * @param saveAs
     *      Where the downloaded file is stored.
     */
    public void downloadCompleted(URL url, File saveAs) {
        if (observer == null) {
            return;
        }
        try {
            String path = Utils.removeCWD(saveAs);
            RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            synchronized(observer) {
                itemsPending.remove(url);
                itemsCompleted.put(url, saveAs);
                observer.update(this, msg);
                observer.notifyAll();
                checkIfComplete();
            }
        } catch (Exception e) {
            logger.error("Exception while updating observer: ", e);
        }
    }

    /**
     * Notifies observers that a file could not be downloaded (includes a reason).
     * @param url
     * @param reason
     */
    public void downloadErrored(URL url, String reason) {
        if (observer == null) {
            return;
        }
        synchronized(observer) {
            itemsPending.remove(url);
            itemsErrored.put(url, reason);
            observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, url + " : " + reason));
            observer.notifyAll();
            checkIfComplete();
        }
    }

    /**
     * Notify observers that a download could not be completed,
     * but was not technically an "error".
     * @param url
     * @param message
     */
    public void downloadProblem(URL url, String message) {
        if (observer == null) {
            return;
        }
        synchronized(observer) {
            itemsPending.remove(url);
            itemsErrored.put(url, message);
            observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_WARN, url + " : " + message));
            observer.notifyAll();
        }
        checkIfComplete();
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    private void checkIfComplete() {
        synchronized (observer) {
            if (!completed && itemsPending.size() == 0) {
                completed = true;
                logger.info("   Rip completed!");
                observer.update(this,
                        new RipStatusMessage(
                                STATUS.RIP_COMPLETE,
                                workingDir));
                observer.notifyAll();
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

    /**
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the workin directory should be saved.
     */
    public void setWorkingDir(URL url) throws IOException {
        String path = Utils.getWorkingDirectory().getCanonicalPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += getHost() + "_" + getGID(this.url) + File.separator;
        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir));
            this.workingDir.mkdirs();
        }
        logger.debug("Set working directory to: " + this.workingDir);
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
        for (Constructor<?> constructor : getRipperConstructors()) {
            try {
                AbstractRipper ripper = (AbstractRipper) constructor.newInstance(url);
                return ripper;
            } catch (Exception e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        throw new Exception("No compatible ripper found");
    }

    /**
     * @return
     *      List of constructors for all eligible Rippers.
     * @throws Exception
     */
    private static List<Constructor<?>> getRipperConstructors() throws Exception {
        List<Constructor<?>> constructors = new ArrayList<Constructor<?>>();
        for (Class<?> clazz : Utils.getClassesForPackage("com.rarchives.ripme.ripper.rippers")) {
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
        synchronized (observer) {
            observer.update(this, new RipStatusMessage(status, message));
            observer.notifyAll();
        }
    }
    
    /**
     * @return
     *      Integer between 0 and 100 defining the progress of the album rip.
     */
    public int getCompletionPercentage() {
        double total = itemsPending.size()  + itemsErrored.size() + itemsCompleted.size();
        return (int) (100 * ( (total - itemsPending.size()) / total));
    }
    
    /**
     * @return
     *      Human-readable information on the status of the current rip.
     */
    public String getStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompletionPercentage())
          .append("% ")
          .append("- Pending: "  ).append(itemsPending.size())
          .append(", Completed: ").append(itemsCompleted.size())
          .append(", Errored: "  ).append(itemsErrored.size());
        return sb.toString();
    }

    /**
     * Rips the album when the thread is invoked.
     */
    public void run() {
        try {
            rip();
        } catch (IOException e) {
            logger.error("Got exception while running ripper:", e);
        }
    }

}