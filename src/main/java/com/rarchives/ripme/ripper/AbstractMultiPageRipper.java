package com.rarchives.ripme.ripper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.nodes.Document;

import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

public abstract class AbstractMultiPageRipper extends AlbumRipper {

    public AbstractMultiPageRipper(URL url) throws IOException {
        super(url);
    }

    public abstract String getDomain();
    public abstract String getHost();

    public abstract Document getFirstPage() throws IOException;
    public abstract Document getNextPage(Document doc) throws IOException;
    public abstract List<String> getURLsFromPage(Document page);
    public abstract void downloadURL(URL url, int index);

    public boolean keepSortOrder() {
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

    @Override
    public void rip() throws IOException {
        int index = 0;
        logger.info("Retrieving " + this.url);
        sendUpdate(STATUS.LOADING_RESOURCE, this.url.toExternalForm());
        Document doc = getFirstPage();

        while (doc != null) {
            List<String> imageURLs = getURLsFromPage(doc);

            if (imageURLs.size() == 0) {
                throw new IOException("No images found at " + this.url);
            }

            for (String imageURL : imageURLs) {
                if (isStopped()) {
                    logger.info("Interrupted");
                    break;
                }
                index += 1;
                downloadURL(new URL(imageURL), index);
            }
            try {
                doc = getNextPage(doc);
            } catch (IOException e) {
                logger.info("Can't get next page: " + e.getMessage());
                break;
            }
        }
        waitForThreads();
    }

    public String getPrefix(int index) {
        String prefix = "";
        if (keepSortOrder() && Utils.getConfigBoolean("download.save_order", true)) {
            prefix = String.format("%03d_", index);
        }
        return prefix;
    }
}