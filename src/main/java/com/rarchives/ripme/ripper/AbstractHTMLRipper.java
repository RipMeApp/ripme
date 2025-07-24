package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

/**
 * Simplified ripper, designed for ripping from sites by parsing HTML.
 */
public abstract class AbstractHTMLRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(AbstractHTMLRipper.class);

    Document cachedFirstPage;

    protected AbstractHTMLRipper(URL url) throws IOException {
        super(url);
        if(Utils.getConfigBoolean("ssl.verify.off",false)){
            Http.SSLVerifyOff();
        }else {
            Http.undoSSLVerifyOff();
        }
    }

    protected abstract String getDomain();
    public abstract String getHost();

    protected Document getFirstPage() throws IOException, URISyntaxException {
        return Http.url(url).get();
    }

    protected Document getCachedFirstPage() throws IOException, URISyntaxException {
        if (cachedFirstPage == null) {
            cachedFirstPage = getFirstPage();
        }
        return cachedFirstPage;
    }

    public Document getNextPage(Document doc) throws IOException, URISyntaxException {
        return null;
    }

    protected abstract List<String> getURLsFromPage(Document page) throws UnsupportedEncodingException, URISyntaxException;

    protected List<String> getDescriptionsFromPage(Document doc) throws IOException {
        throw new IOException("getDescriptionsFromPage not implemented"); // Do I do this or make an abstract function?
    }

    protected abstract void downloadURL(URL url, int index);

    protected boolean keepSortOrder() {
        return true;
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(getDomain());
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException {
        return url;
    }
    protected boolean hasDescriptionSupport() {
        return false;
    }

    protected String[] getDescription(String url, Document page) throws IOException {
        throw new IOException("getDescription not implemented"); // Do I do this or make an abstract function?
    }
    protected int descSleepTime() {
        return 100;
    }

    protected List<String> getAlbumsToQueue(Document doc) {
        return null;
    }

    // If a page has Queue support then it has no images we want to download, just a list of urls we want to add to
    // the queue
    protected boolean hasQueueSupport() {
        return false;
    }

    // Takes a url and checks if it is for a page of albums
    protected boolean pageContainsAlbums(URL url) {
        return false;
    }

    @Override
    public void rip() throws IOException, URISyntaxException {
        int imageIndex = 0;
        int textindex = 0;
        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        var doc = getCachedFirstPage();

        if (hasQueueSupport() && pageContainsAlbums(this.url)) {
            List<String> urls = getAlbumsToQueue(doc);
            for (String url : urls) {
                MainWindow.addUrlToQueue(url);
            }

            // We set doc to null here so the while loop below this doesn't fire
            doc = null;
            logger.debug("Adding items from " + this.url + " to queue");
        }

        List<String> doclocation = new ArrayList<>();

        logger.info("Got doc location " + doc.location());

        while (doc != null) {

            logger.info("Processing a doc...");

            // catch if we saw a doc location already, save the ones seen in a list
            if (doclocation.contains(doc.location())) {
                logger.info("Already processed location " + doc.location() + " breaking");
                break;
            }
            doclocation.add(doc.location());

            if (alreadyDownloadedUrls >= Utils.getConfigInteger("history.end_rip_after_already_seen", 1000000000) && !isThisATest()) {
                sendUpdate(STATUS.DOWNLOAD_COMPLETE_HISTORY, "Already seen the last " + alreadyDownloadedUrls + " images ending rip");
                break;
            }

            logger.info("retrieving urls from doc");

            List<String> imageURLs = getURLsFromPage(doc);
            // If hasASAPRipping() returns true then the ripper will handle downloading the files
            // if not it's done in the following block of code
            if (!hasASAPRipping()) {
                // Remove all but 1 image
                if (isThisATest()) {
                    while (imageURLs.size() > 1) {
                        imageURLs.remove(1);
                    }
                }

                if (imageURLs.isEmpty()) {
                    throw new IOException("No images found at " + doc.location());
                }

                for (String imageURL : imageURLs) {
                    imageIndex += 1;
                    logger.debug("Found image url #" + imageIndex + ": '" + imageURL + "'");
                    downloadURL(new URI(imageURL).toURL(), imageIndex);
                    if (isStopped() || isThisATest()) {
                        break;
                    }
                }
            }
            if (hasDescriptionSupport() && Utils.getConfigBoolean("descriptions.save", false)) {
                logger.debug("Fetching description(s) from " + doc.location());
                List<String> textURLs = getDescriptionsFromPage(doc);
                if (!textURLs.isEmpty()) {
                    logger.debug("Found description link(s) from " + doc.location());
                    for (String textURL : textURLs) {
                        if (isStopped() || isThisATest()) {
                            break;
                        }

                        textindex += 1;
                        logger.debug("Getting description from " + textURL);
                        String[] tempDesc = getDescription(textURL,doc);

                        if (tempDesc != null) {
                            URL url = new URI(textURL).toURL();
                            String filename = fileNameFromURL(url);

                            boolean fileExists = new File(
                                workingDir.getCanonicalPath()
                                        + ""
                                        + File.separator
                                        + getPrefix(imageIndex)
                                        + (tempDesc.length > 1 ? tempDesc[1] : filename)
                                        + ".txt").exists();

                            if (Utils.getConfigBoolean("file.overwrite", false) || !fileExists) {
                                logger.debug("Got description from " + textURL);
                                saveText(url, "", tempDesc[0], textindex, (tempDesc.length > 1 ? tempDesc[1] : filename));
                                sleep(descSleepTime());
                            } else {
                                logger.debug("Description from " + textURL + " already exists.");
                            }
                        }

                    }
                }
            }

            if (isStopped() || isThisATest()) {
                break;
            }

            try {
                sendUpdate(STATUS.LOADING_RESOURCE, "next page");
                doc = getNextPage(doc);
            } catch (IOException e) {
                logger.info("Can't get next page: " + e.getMessage());
                break;
            }
        }

        logger.info("All items queued; total items: {}; url: {}", imageIndex, url);

        // Final total item count is now known
        setItemsTotal(imageIndex);

        if (getCrawlerThreadPool() != null) {
            logger.debug("Waiting for crawler threadpool: {}", url);
            getCrawlerThreadPool().waitForThreads(imageIndex, shouldStop, url);
        }

        waitForRipperThreads();
    }

    /**
     * Gets the file name from the URL
     * @param url
     *      URL that you want to get the filename from
     * @return
     *      Filename of the URL
     */
    private String fileNameFromURL(URL url) {
        String saveAs = url.toExternalForm();
        if (saveAs.substring(saveAs.length() - 1).equals("/")) { saveAs = saveAs.substring(0,saveAs.length() - 1) ;}
        saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
        if (saveAs.indexOf(':') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf(':')); }
        return saveAs;
    }
    /**
     *
     * @param url
     *      Target URL
     * @param subdirectory
     *      Path to subdirectory where you want to save it
     * @param text
     *      Text you want to save
     * @param index
     *      Index in something like an album
     * @return
     *      True if ripped successfully
     *      False if failed
     */
    public boolean saveText(URL url, String subdirectory, String text, int index) {
        String saveAs = fileNameFromURL(url);
        return saveText(url,subdirectory,text,index,saveAs);
    }
    private boolean saveText(URL url, String subdirectory, String text, int index, String fileName) {
        // Not the best for some cases, like FurAffinity. Overridden there.
        try {
            stopCheck();
        } catch (IOException e) {
            return false;
        }
        File saveFileAs;
        try {
            if (!subdirectory.equals("")) { // Not sure about this part
                subdirectory = File.separator + subdirectory;
            }
            saveFileAs = new File(
                    workingDir.getCanonicalPath()
                    + subdirectory
                    + File.separator
                    + getPrefix(index)
                    + fileName
                    + ".txt");
            // Write the file
            FileOutputStream out = (new FileOutputStream(saveFileAs));
            out.write(text.getBytes());
            out.close();
        } catch (IOException e) {
            logger.error("[!] Error creating save file path for description '" + url + "':", e);
            return false;
        }
        logger.debug("Downloading " + url + "'s description to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            logger.info("[+] Creating directory: " + saveFileAs.getParent());
            saveFileAs.getParentFile().mkdirs();
        }
        return true;
    }

    /**
     * Gets prefix based on where in the index it is
     * @param index
     *      The index in question
     * @return
     *      Returns prefix for a file. (?)
     */
    protected String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", index);
        }
        return prefix;
    }

    /*
     * ------ Methods copied from AlbumRipper. ------
     * This removes AlbumnRipper's usage from this class.
     */

    protected boolean allowDuplicates() {
        return false;
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL to decide filename.
     * @param url
     *      URL to download
     * @return
     *      True on success
     */
    protected boolean addURLToDownload(URL url) {
        // Use empty prefix and empty subdirectory
        return addURLToDownload(url, "", "");
    }

    /**
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the working directory should be saved.
     */
    @Override
    public void setWorkingDir(URL url) throws IOException, URISyntaxException {
        Path wd = Utils.getWorkingDirectory();
        // TODO - change to nio
        String path = wd.toAbsolutePath().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        String title = getAlbumTitle(this.url);
        logger.debug("Using album title '" + title + "'");

        title = Utils.filesystemSafe(title);
        path += title;
        path = Utils.getOriginalDirectory(path) + File.separator;   // check for case sensitive (unix only)

        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir.toPath()));
            if (!this.workingDir.mkdirs()) {
                throw new IOException("Failed creating dir: \"" + this.workingDir + "\"");
            }
        }
        logger.debug("Set working directory to: " + this.workingDir);
    }

    /**
     * @return
     *      Integer between 0 and 100 defining the progress of the album rip.
     */
    @Override
    public int getCompletionPercentage() {
        double total = itemsPending.size()  + itemsErrored.size() + itemsCompleted.size();
        return (int) (100 * ( (total - itemsPending.size()) / total));
    }

    @Override
    public int getPendingCount() {
        DownloadThreadPool threadPool = getRipperThreadPool();
        if (threadPool != null) {
            return threadPool.getPendingThreadCount();
        }
        return itemsPending.size();
    }

}
