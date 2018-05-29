package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;
import com.sun.org.apache.xpath.internal.operations.Bool;

public abstract class VideoRipper extends AbstractRipper {

    private int bytesTotal = 1,
                 bytesCompleted = 1;

    protected VideoRipper(URL url) throws IOException {
        super(url);
    }

    public abstract boolean canRip(URL url);
    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException;

    @Override
    public void setBytesTotal(int bytes) {
        this.bytesTotal = bytes;
    }
    @Override
    public void setBytesCompleted(int bytes) {
        this.bytesCompleted = bytes;
    }

    @Override
    public String getAlbumTitle(URL url) {
        return "videos";
    }

    @Override
    public boolean addURLToDownload(URL url, File saveAs) {
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            String urlFile = this.workingDir + File.separator + "urls.txt";

            try (FileWriter fw = new FileWriter(urlFile, true)) {
                fw.write(url.toExternalForm());
                fw.write("\n");

                RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, urlFile);
                observer.update(this, msg);
            } catch (IOException e) {
                logger.error("Error while writing to " + urlFile, e);
                return false;
            }
        }
        else {
            if (isThisATest()) {
                // Tests shouldn't download the whole video
                // Just change this.url to the download URL so the test knows we found it.
                logger.debug("Test rip, found URL: " + url);
                this.url = url;
                return true;
            }
            threadPool.addThread(new DownloadVideoThread(url, saveAs, this));
        }
        return true;
    }

    @Override
    public boolean addURLToDownload(URL url, File saveAs, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        return addURLToDownload(url, saveAs);
    }

    
    /**
     * Creates & sets working directory based on URL.
     * @param url
     *      Target URL
     */
    @Override
    public void setWorkingDir(URL url) throws IOException {
        String path = Utils.getWorkingDirectory().getCanonicalPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "videos" + File.separator;
        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir));
            this.workingDir.mkdirs();
        }
        logger.debug("Set working directory to: " + this.workingDir);
    }
    
    /**
     * @return
     *      Returns % of video done downloading.
     */
    @Override
    public int getCompletionPercentage() {
        return (int) (100 * (bytesCompleted / (float) bytesTotal));
    }
    
    /**
     * Runs if download successfully completed.
     * @param url
     *      Target URL
     * @param saveAs
     *      Path to file, including filename.
     */
    @Override
    public void downloadCompleted(URL url, File saveAs) {
        if (observer == null) {
            return;
        }
        try {
            String path = Utils.removeCWD(saveAs);
            RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            observer.update(this, msg);

            checkIfComplete();
        } catch (Exception e) {
            logger.error("Exception while updating observer: ", e);
        }
    }
    
    /**
     * Runs if the download errored somewhere.
     * @param url
     *      Target URL
     * @param reason
     *      Reason why the download failed.
     */
    @Override
    public void downloadErrored(URL url, String reason) {
        if (observer == null) {
            return;
        }
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, url + " : " + reason));
        checkIfComplete();
    }
    
    
    /**
     * Runs if user tries to redownload an already existing File.
     * @param url
     *      Target URL
     * @param file
     *      Existing file
     */
    @Override
    public void downloadExists(URL url, File file) {
        if (observer == null) {
            return;
        }
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_WARN, url + " already saved as " + file));
        checkIfComplete();
    }

    /**
     * Gets the status and changes it to a human-readable form.
     * @return 
     *      Status of current download.
     */
    @Override
    public String getStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCompletionPercentage())
          .append("% ")
          .append(" - ")
          .append(Utils.bytesToHumanReadable(bytesCompleted))
          .append(" / ")
          .append(Utils.bytesToHumanReadable(bytesTotal));
        return sb.toString();
    }

    @Override
    /**
     * Sanitizes URL.
     * Usually just returns itself.
     */
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    @Override
    protected void checkIfComplete() {
        if (observer == null) {
            return;
        }
        if (bytesCompleted >= bytesTotal) {
            super.checkIfComplete();
        }
    }
}
