package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

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

    public void addURLToDownload(URL url) {
        addURLToDownload(url, "");
    }

    public void addURLToDownload(URL url, String prefix) {
        String saveAs = url.toExternalForm();
        saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
        File saveFileAs;
        try {
            saveFileAs = new File(workingDir.getCanonicalPath() + File.separator + prefix + saveAs);
        } catch (IOException e) {
            logger.error("Error creating save file path for URL '" + url + "':", e);
            return;
        }
        logger.debug("Downloading " + url + " to " + saveFileAs);
        addURLToDownload(url, saveFileAs);
    }
    /**
     * Add image to be downloaded and saved.
     * @param url
     *      URL of the file
     * @param saveAs
     *      Path of the local file to save the content to.
     */
    public void addURLToDownload(URL url, File saveAs) {
        threadPool.addThread(new DownloadFileThread(url, saveAs));
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
            logger.info("Creating working directory(s): " + this.workingDir);
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
        throw new Exception("No compatible ripper found");
    }
}