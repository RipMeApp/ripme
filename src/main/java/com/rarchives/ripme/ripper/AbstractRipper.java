package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.rippers.ImagearnRipper;
import com.rarchives.ripme.ripper.rippers.ImagefapRipper;
import com.rarchives.ripme.ripper.rippers.ImgurRipper;
import com.rarchives.ripme.utils.Utils;

public abstract class AbstractRipper implements RipperInterface {

    private static final Logger logger = Logger.getLogger(AbstractRipper.class);

    protected URL url;
    protected File workingDir;
    protected DownloadThreadPool threadPool;

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
        threadPool.addThread(new DownloadFileThread(url, saveAs));
    }

    public void addURLToDownload(URL url, String prefix, String subdirectory) {
        String saveAs = url.toExternalForm();
        saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
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

    public URL getURL() {
        return url;
    }

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
        // I know what you're thinking. I'm disappointed too.
        try {
            AbstractRipper r = new ImagefapRipper(url);
            return r;
        } catch (IOException e) { }
        try {
            AbstractRipper r = new ImgurRipper(url);
            return r;
        } catch (IOException e) { }
        try {
            AbstractRipper r = new ImagearnRipper(url);
            return r;
        } catch (IOException e) { }
        throw new Exception("No compatible ripper found");
    }
}