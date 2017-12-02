package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.nodes.Document;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

/**
 * Simplified ripper, designed for ripping from sites by parsing HTML.
 */
public abstract class AbstractHTMLRipper extends AlbumRipper {

    protected AbstractHTMLRipper(URL url) throws IOException {
        super(url);
    }

    protected abstract String getDomain();
    public abstract String getHost();

    protected abstract Document getFirstPage() throws IOException;
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
    public boolean hasASAPRipping() {return false;}
    protected String[] getDescription(String url, Document page) throws IOException {
        throw new IOException("getDescription not implemented"); // Do I do this or make an abstract function?
    }
    protected int descSleepTime() {
        return 100;
    }
    @Override
    public void rip() throws IOException {
        int index = 0;
        int textindex = 0;
        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        Document doc = getFirstPage();

        while (doc != null) {
            List<String> imageURLs;
            if (!hasASAPRipping()) {
                imageURLs = getURLsFromPage(doc);
                // Remove all but 1 image
                if (isThisATest()) {
                    while (imageURLs.size() > 1) {
                        imageURLs.remove(1);
                    }
                }

                if (imageURLs.size() == 0) {
                    throw new IOException("No images found at " + doc.location());
                }

                for (String imageURL : imageURLs) {
                    index += 1;
                    logger.debug("Found image url #" + index + ": " + imageURL);
                    downloadURL(new URL(imageURL), index);
                    if (isStopped()) {
                        break;
                    }
                }
            }
            if (hasDescriptionSupport() && Utils.getConfigBoolean("descriptions.save", false)) {
                logger.debug("Fetching description(s) from " + doc.location());
                List<String> textURLs = getDescriptionsFromPage(doc);
                if (textURLs.size() > 0) {
                    logger.debug("Found description link(s) from " + doc.location());
                    for (String textURL : textURLs) {
                        if (isStopped()) {
                            break;
                        }
                        textindex += 1;
                        logger.debug("Getting description from " + textURL);
                        String[] tempDesc = getDescription(textURL,doc);
                        if (tempDesc != null) {
                            if (Utils.getConfigBoolean("file.overwrite", false) || !(new File(
                                    workingDir.getCanonicalPath()
                                            + ""
                                            + File.separator
                                            + getPrefix(index)
                                            + (tempDesc.length > 1 ? tempDesc[1] : fileNameFromURL(new URL(textURL)))
                                            + ".txt").exists())) {
                                logger.debug("Got description from " + textURL);
                                saveText(new URL(textURL), "", tempDesc[0], textindex, (tempDesc.length > 1 ? tempDesc[1] : fileNameFromURL(new URL(textURL))));
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

        // If they're using a thread pool, wait for it.
        if (getThreadPool() != null) {
            logger.debug("Waiting for threadpool " + getThreadPool().getClass().getName());
            getThreadPool().waitForThreads();
        }
        waitForThreads();
    }
    private String fileNameFromURL(URL url) {
        String saveAs = url.toExternalForm();
        if (saveAs.substring(saveAs.length() - 1) == "/") { saveAs = saveAs.substring(0,saveAs.length() - 1) ;}
        saveAs = saveAs.substring(saveAs.lastIndexOf('/')+1);
        if (saveAs.indexOf('?') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('?')); }
        if (saveAs.indexOf('#') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('#')); }
        if (saveAs.indexOf('&') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf('&')); }
        if (saveAs.indexOf(':') >= 0) { saveAs = saveAs.substring(0, saveAs.indexOf(':')); }
        return saveAs;
    }
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
            // TODO Get prefix working again, probably requires reworking a lot of stuff! (Might be fixed now)
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
            logger.info("[+] Creating directory: " + Utils.removeCWD(saveFileAs.getParent()));
            saveFileAs.getParentFile().mkdirs();
        }
        return true;
    }
    protected String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", index);
        }
        return prefix;
    }
}
