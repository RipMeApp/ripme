package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

// Should this file even exist? It does the same thing as abstractHTML ripper

/**'
 * For ripping delicious albums off the interwebz.
 * @deprecated Use AbstractHTMLRipper instead.
 */
@Deprecated
public abstract class AlbumRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(AlbumRipper.class);

    protected AlbumRipper(URL url) throws IOException {
        super(url);
    }

    public abstract boolean canRip(URL url);
    public abstract URL sanitizeURL(URL url) throws MalformedURLException, URISyntaxException;
    public abstract void rip() throws IOException;
    public abstract String getHost();
    public abstract String getGID(URL url) throws MalformedURLException, URISyntaxException;

    protected boolean allowDuplicates() {
        return false;
    }

    /**
     * Sets directory to save all ripped files to.
     * @param url
     *      URL to define how the working directory should be saved.
     * @throws
     *      IOException
     */
    @Override
    public void setWorkingDir(URL url) throws IOException, URISyntaxException {
        Path wd = Utils.getWorkingDirectory();
        // TODO - change to nio
        String path = wd.toAbsolutePath().toString();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        String title;
        if (Utils.getConfigBoolean("album_titles.save", true)) {
            title = getAlbumTitle(this.url);
        } else {
            title = super.getAlbumTitle(this.url);
        }
        logger.debug("Using album title '" + title + "'");

        title = Utils.filesystemSafe(title);
        path += title;
        path = Utils.getOriginalDirectory(path) + File.separator;   // check for case sensitive (unix only)

        this.workingDir = new File(path);
        if (!this.workingDir.exists()) {
            logger.info("[+] Creating directory: " + Utils.removeCWD(this.workingDir.toPath()));
            this.workingDir.mkdirs();
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

}
