package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public abstract class VideoRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(VideoRipper.class);

    private int bytesTotal = 1;
    private int bytesCompleted = 1;

    protected VideoRipper(URL url) throws IOException {
        super(url);
    }

    public abstract void rip() throws IOException, URISyntaxException;

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
    protected boolean useByteProgessBar() {
        return true;
    }

    @Override
    public boolean addURLToDownload(TokenedUrlGetter tug, RipUrlId ripUrlId, Path directory, String filename, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            String urlFile = this.workingDir + "/urls.txt";
            URL url = null;
            try {
                url = tug.getTokenedUrl();
            } catch (IOException | URISyntaxException e) {
                logger.error("Unable to get URL for {}", ripUrlId, e);
                return false;
            }
            if (AbstractRipper.shouldIgnoreExtension(url)) {
                sendUpdate(STATUS.DOWNLOAD_SKIP, "Skipping " + url.toExternalForm() + " - ignored extension");
                return false;
            }

            try (FileWriter fw = new FileWriter(urlFile, true)) {
                fw.write(url.toExternalForm());
                fw.write("\n");

                RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, urlFile);
                observer.update(this, msg);
            } catch (IOException e) {
                logger.error("Error while writing to " + urlFile, e);
                return false;
            }
            return true;
        } else {
            if (isThisATest()) {
                // Tests shouldn't download the whole video
                // Just change this.url to the download URL so the test knows we found it.
                logger.debug("Test rip, found URL: " + url);
                try {
                    this.url = tug.getTokenedUrl();
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }

            itemsPending.add(ripUrlId);
            threadPool.addThread(new DownloadFileThread(tug, ripUrlId, directory, filename, this, getFileExtFromMIME));
        }
        return true;
    }

    /**
     * Creates & sets working directory based on URL.
     *
     * @param url Target URL
     */
    @Override
    public void setWorkingDir(URL url) throws IOException {
        Path wd = Utils.getWorkingDirectory();
        // TODO - change to nio
        String path = wd.toAbsolutePath().toString();

        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }

        path += "videos" + File.separator;
        workingDir = new File(path);

        if (!workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(workingDir.toPath()));
            workingDir.mkdirs();
        }

        logger.debug("Set working directory to: " + workingDir);
    }

    /**
     * @return Returns % of video done downloading.
     */
    @Override
    public int getCompletionPercentage() {
        return (int) (100 * (bytesCompleted / (float) bytesTotal));
    }

    /**
     * Gets the status and changes it to a human-readable form.
     *
     * @return Status of current download.
     */
    @Override
    public String getStatusText() {
        return String.valueOf(getCompletionPercentage()) +
                "%  - " +
                Utils.bytesToHumanReadable(bytesCompleted) +
                " / " +
                Utils.bytesToHumanReadable(bytesTotal);
    }

    /**
     * Sanitizes URL.
     * Usually just returns itself.
     */
    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        return url;
    }

}
