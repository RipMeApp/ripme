package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.RipStatusMessage;

/**
 * Simplified ripper, designed for ripping from sites by parsing HTML.
 */
public abstract class AbstractHTMLRipper extends AbstractRipper {
    
    private final Map<URL, File> itemsPending = Collections.synchronizedMap(new HashMap<>());
    private final Map<URL, Path> itemsCompleted = Collections.synchronizedMap(new HashMap<>());
    private final Map<URL, String> itemsErrored = Collections.synchronizedMap(new HashMap<>());
    Document cachedFirstPage;

    protected AbstractHTMLRipper(URL url) throws IOException {
        super(url);
    }

    protected abstract String getDomain();
    public abstract String getHost();

    protected abstract Document getFirstPage() throws IOException;

    protected Document getCachedFirstPage() throws IOException {
        if (cachedFirstPage == null) {
            cachedFirstPage = getFirstPage();
        }
        return cachedFirstPage;
    }

    public Document getNextPage(Document doc) throws IOException {
        return null;
    }
    protected abstract List<String> getURLsFromPage(Document page);
    protected List<String> getDescriptionsFromPage(Document doc) throws IOException {
        throw new IOException("getDescriptionsFromPage not implemented"); // Do I do this or make an abstract function?
    }
    protected abstract void downloadURL(URL url, int index);
    protected DownloadThreadPool getThreadPool() {
        return null;
    }

    protected boolean keepSortOrder() {
        return true;
    }

    @Override
    public boolean canRip(URL url) {
        return url.getHost().endsWith(getDomain());
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
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
    public void rip() throws IOException {
        int index = 0;
        int textindex = 0;
        LOGGER.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        var doc = getCachedFirstPage();

        if (hasQueueSupport() && pageContainsAlbums(this.url)) {
            List<String> urls = getAlbumsToQueue(doc);
            for (String url : urls) {
                MainWindow.addUrlToQueue(url);
            }

            // We set doc to null here so the while loop below this doesn't fire
            doc = null;
            LOGGER.debug("Adding items from " + this.url + " to queue");
        }

        List<String> doclocation = new ArrayList<>();
        while (doc != null) {

            // catch if we saw a doc location already, save the ones seen in a list
            if (doclocation.contains(doc.location())) {
                break;
            }
            doclocation.add(doc.location());

            if (alreadyDownloadedUrls >= Utils.getConfigInteger("history.end_rip_after_already_seen", 1000000000) && !isThisATest()) {
                sendUpdate(STATUS.DOWNLOAD_COMPLETE_HISTORY, "Already seen the last " + alreadyDownloadedUrls + " images ending rip");
                break;
            }
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
                    index += 1;
                    LOGGER.debug("Found image url #" + index + ": " + imageURL);
                    downloadURL(new URL(imageURL), index);
                    if (isStopped() || isThisATest()) {
                        break;
                    }
                }
            }
            if (hasDescriptionSupport() && Utils.getConfigBoolean("descriptions.save", false)) {
                LOGGER.debug("Fetching description(s) from " + doc.location());
                List<String> textURLs = getDescriptionsFromPage(doc);
                if (!textURLs.isEmpty()) {
                    LOGGER.debug("Found description link(s) from " + doc.location());
                    for (String textURL : textURLs) {
                        if (isStopped() || isThisATest()) {
                            break;
                        }
                        textindex += 1;
                        LOGGER.debug("Getting description from " + textURL);
                        String[] tempDesc = getDescription(textURL,doc);
                        if (tempDesc != null) {
                            if (Utils.getConfigBoolean("file.overwrite", false) || !(new File(
                                    workingDir.getCanonicalPath()
                                            + ""
                                            + File.separator
                                            + getPrefix(index)
                                            + (tempDesc.length > 1 ? tempDesc[1] : fileNameFromURL(new URL(textURL)))
                                            + ".txt").exists())) {
                                LOGGER.debug("Got description from " + textURL);
                                saveText(new URL(textURL), "", tempDesc[0], textindex, (tempDesc.length > 1 ? tempDesc[1] : fileNameFromURL(new URL(textURL))));
                                sleep(descSleepTime());
                            } else {
                                LOGGER.debug("Description from " + textURL + " already exists.");
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
                LOGGER.info("Can't get next page: " + e.getMessage());
                break;
            }
        }

        // If they're using a thread pool, wait for it.
        if (getThreadPool() != null) {
            LOGGER.debug("Waiting for threadpool " + getThreadPool().getClass().getName());
            getThreadPool().waitForThreads();
        }
        waitForThreads();
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
            LOGGER.error("[!] Error creating save file path for description '" + url + "':", e);
            return false;
        }
        LOGGER.debug("Downloading " + url + "'s description to " + saveFileAs);
        if (!saveFileAs.getParentFile().exists()) {
            LOGGER.info("[+] Creating directory: " + saveFileAs.getParent());
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

    @Override
    /*
      Returns total amount of files attempted.
     */
    public int getCount() {
        return itemsCompleted.size() + itemsErrored.size();
    }

    @Override
    /*
      Queues multiple URLs of single images to download from a single Album URL
     */
    public boolean addURLToDownload(URL url, Path saveAs, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        // Only download one file if this is a test.
        if (isThisATest() && (itemsCompleted.size() > 0 || itemsErrored.size() > 0)) {
            stop();
            itemsPending.clear();
            return false;
        }
        if (!allowDuplicates()
                && ( itemsPending.containsKey(url)
                  || itemsCompleted.containsKey(url)
                  || itemsErrored.containsKey(url) )) {
            // Item is already downloaded/downloading, skip it.
            LOGGER.info("[!] Skipping " + url + " -- already attempted: " + Utils.removeCWD(saveAs));
            return false;
        }
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            Path urlFile = Paths.get(this.workingDir + "/urls.txt");
            String text = url.toExternalForm() + System.lineSeparator();
            try {
                Files.write(urlFile, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                itemsCompleted.put(url, urlFile);
            } catch (IOException e) {
                LOGGER.error("Error while writing to " + urlFile, e);
            }
        }
        else {
            itemsPending.put(url, saveAs.toFile());
            DownloadFileThread dft = new DownloadFileThread(url,  saveAs.toFile(),  this, getFileExtFromMIME);
            if (referrer != null) {
                dft.setReferrer(referrer);
            }
            if (cookies != null) {
                dft.setCookies(cookies);
            }
            threadPool.addThread(dft);
        }

        return true;
    }

    @Override
    public boolean addURLToDownload(URL url, Path saveAs) {
        return addURLToDownload(url, saveAs, null, null, false);
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

    @Override
    /*
      Cleans up & tells user about successful download
     */
    public void downloadCompleted(URL url, Path saveAs) {
        if (observer == null) {
            return;
        }
        try {
            String path = Utils.removeCWD(saveAs);
            RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            itemsPending.remove(url);
            itemsCompleted.put(url, saveAs);
            observer.update(this, msg);

            checkIfComplete();
        } catch (Exception e) {
            LOGGER.error("Exception while updating observer: ", e);
        }
    }

    @Override
    /*
     * Cleans up & tells user about failed download.
     */
    public void downloadErrored(URL url, String reason) {
        if (observer == null) {
            return;
        }
        itemsPending.remove(url);
        itemsErrored.put(url, reason);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, url + " : " + reason));

        checkIfComplete();
    }

    @Override
    /*
      Tells user that a single file in the album they wish to download has
      already been downloaded in the past.
     */
    public void downloadExists(URL url, Path file) {
        if (observer == null) {
            return;
        }

        itemsPending.remove(url);
        itemsCompleted.put(url, file);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_WARN, url + " already saved as " + file));

        checkIfComplete();
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    @Override
    protected void checkIfComplete() {
        if (observer == null) {
            return;
        }
        if (itemsPending.isEmpty()) {
            super.checkIfComplete();
        }
    }

    /**
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the working directory should be saved.
     */
    @Override
    public void setWorkingDir(URL url) throws IOException {
        Path wd = Utils.getWorkingDirectory();
        // TODO - change to nio
        String path = wd.toAbsolutePath().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        String title = getAlbumTitle(this.url);
        LOGGER.debug("Using album title '" + title + "'");

        title = Utils.filesystemSafe(title);
        path += title;
        path = Utils.getOriginalDirectory(path) + File.separator;   // check for case sensitive (unix only)

        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            LOGGER.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir.toPath()));
            if (!this.workingDir.mkdirs()) {
                throw new IOException("Failed creating dir: \"" + this.workingDir + "\"");
            }
        }
        LOGGER.debug("Set working directory to: " + this.workingDir);
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

    /**
     * @return
     *      Human-readable information on the status of the current rip.
     */
    @Override
    public String getStatusText() {
        return getCompletionPercentage() +
                "% " +
                "- Pending: " + itemsPending.size() +
                ", Completed: " + itemsCompleted.size() +
                ", Errored: " + itemsErrored.size();
    }


}
