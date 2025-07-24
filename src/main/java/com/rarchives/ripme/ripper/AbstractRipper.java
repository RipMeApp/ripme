package com.rarchives.ripme.ripper;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;

import com.rarchives.ripme.App;
import com.rarchives.ripme.ui.RipStatusComplete;
import com.rarchives.ripme.ui.RipStatusHandler;
import com.rarchives.ripme.ui.RipStatusMessage;
import com.rarchives.ripme.ui.RipStatusMessage.STATUS;
import com.rarchives.ripme.utils.Utils;

// Suppress warning for specifically Observable. Hopefully no other deprecations
// get hidden by this suppression.
// The reason for this is that the deprecation is due to insufficiently powerful
// design. However, it's good enough for us and getting rid of the warning means
// adding our own Observer pattern implementation that is essentially a copy-
// paste of the one in the JDK that has been deprecated. No need to do that.
@SuppressWarnings("deprecation")
public abstract class AbstractRipper
        extends Observable
        implements RipperInterface, Runnable {

    private static final Logger logger = LogManager.getLogger(AbstractRipper.class);

    // For albums
    protected final Set<RipUrlId> itemsPending = Collections.synchronizedSet(new HashSet<>());
    protected final Map<RipUrlId, Path> itemsCompleted = Collections.synchronizedMap(new HashMap<>());
    protected final Map<RipUrlId, String> itemsErrored = Collections.synchronizedMap(new HashMap<>());
    protected final Map<RipUrlId, String> itemsSkipped = Collections.synchronizedMap(new HashMap<>());

    /**
     * Rippers should set itemsTotal to the best known number of total items,
     * if known at the start of the rip, e.g. in getFirstPage().
     * The best known number might be indicated on the album page,
     * or calculated by the number of pages and the number of items per page.
     * Once the last item is seen by the HTML or JSON crawler, the final value is set.
     */
    private final AtomicInteger itemsTotal = new AtomicInteger(0);

    /**
     * If an album has a duplicate RipUrlId (e.g. the same image linked twice),
     * duplicates can't be counted by itemsPending, but {@link #waitForRipperThreads()} needs
     * to know that the ripper has seen each link crawled.
     */
    private final AtomicInteger itemsSeen = new AtomicInteger(0);

    /** For individual files. See {@link #useByteProgessBar()} */
    protected long bytesCompleted = 0;
    /** For individual files. See {@link #useByteProgessBar()} */
    protected long bytesTotal = 1; // avoid divide by 0

    private final String URLHistoryFile = Utils.getURLHistoryFile();

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36";

    private static Random randomGenerator = new Random();

    protected URL url;
    protected File workingDir;
    private DownloadThreadPool ripperThreadPool;
    private DownloadThreadPool crawlerThreadPool;
    RipStatusHandler observer = null;

    private final AtomicBoolean completed = new AtomicBoolean(false);

    public abstract void rip() throws IOException, URISyntaxException;

    public abstract String getHost();

    public abstract String getGID(URL url) throws MalformedURLException, URISyntaxException;

    protected abstract boolean allowDuplicates();

    public boolean hasASAPRipping() {
        return false;
    }

    // Everytime addUrlToDownload skips a already downloaded url this increases by 1
    public int alreadyDownloadedUrls = 0;
    protected final AtomicBoolean shouldStop = new AtomicBoolean(false);
    protected final AtomicBoolean shouldPanic = new AtomicBoolean(false);
    private static boolean thisIsATest = false;

    public void stop() {
        logger.trace("stop()");
        shouldStop.set(true);
    }

    public void panic() {
        logger.trace("panic()");
        shouldStop.set(true);
        shouldPanic.set(true);
    }

    public boolean isPanicked() {
        return shouldPanic.get();
    }

    public boolean isStopped() {
        return shouldStop.get();
    }

    protected void stopCheck() throws IOException {
        if (shouldStop.get()) {
            throw new IOException("Ripping interrupted");
        }
    }

    /**
     * Used for file downloads. Used by {@link #addURLToDownload(TokenedUrlGetter, RipUrlId, Path, String, String, Map, Boolean)}
     */
    protected DownloadThreadPool getRipperThreadPool() {
        return ripperThreadPool;
    }

    /**
     * Used by Rippers to crawl file pages.<br>
     * After the last file page is crawled and all threads are queued to the ripper thread pool,
     * {@link #rip()} terminates the crawler thread pool.<br>
     * After the crawler thread pool is finished, {@link #rip()} terminates the ripper thread pool.
     */
    protected DownloadThreadPool getCrawlerThreadPool() {
        return crawlerThreadPool;
    }

    /**
     * Adds a URL to the url history file
     *
     * @param downloadedURL URL to check if downloaded
     */
    protected void writeDownloadedURL(String downloadedURL) throws IOException {
        // If "save urls only" is checked don't write to the url history file
        if (Utils.getConfigBoolean("urls_only.save", false)) {
            return;
        }
        downloadedURL = normalizeUrl(downloadedURL);
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(URLHistoryFile);
            if (!new File(Utils.getConfigDir()).exists()) {
                logger.error("Config dir doesn't exist");
                logger.info("Making config dir");
                boolean couldMakeDir = new File(Utils.getConfigDir()).mkdirs();
                if (!couldMakeDir) {
                    logger.error("Couldn't make config dir");
                    return;
                }
            }
            // if file doesnt exists, then create it
            if (!file.exists()) {
                boolean couldMakeDir = file.createNewFile();
                if (!couldMakeDir) {
                    logger.error("Couldn't url history file");
                    return;
                }
            }
            if (!file.canWrite()) {
                logger.error("Can't write to url history file: " + URLHistoryFile);
                return;
            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(downloadedURL);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Normalize a URL
     *
     * @param url URL to check if downloaded
     */
    public String normalizeUrl(String url) {
        return url;
    }

    /**
     * Checks to see if Ripme has already downloaded a URL
     *
     * @param url URL to check if downloaded
     * @return Returns true if previously downloaded.
     *         Returns false if not yet downloaded.
     */
    protected boolean hasDownloadedURL(String url) {
        File file = new File(URLHistoryFile);
        url = normalizeUrl(url);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                final String lineFromFile = scanner.nextLine();
                if (lineFromFile.equals(url)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        return false;
    }

    /**
     * Ensures inheriting ripper can rip this URL, raises exception if not.
     * Otherwise initializes working directory and thread pool.
     *
     * @param url URL to rip.
     * @throws IOException If anything goes wrong.
     */
    public AbstractRipper(URL url) throws IOException {
        if (!canRip(url)) {
            throw new MalformedURLException("Unable to rip url: " + url);
        }
        try {
            this.url = sanitizeURL(url);
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    /**
     * Sets ripper's:
     * - Working directory
     * - Logger (for debugging)
     * - FileAppender
     * - Threadpool
     *
     * @throws IOException Always be prepared.
     */
    public void setup() throws IOException, URISyntaxException {
        setWorkingDir(this.url);
        // we do not care if the RollingFileAppender is active,
        // just change the logfile in case.
        // TODO this does not work - not even with
        // .withFileName("${sys:logFilename}")
        // in Utils.java, RollingFileAppender.
        // System.setProperty("logFilename", this.workingDir + "/log.txt");
        // logger.debug("Changing log file to '{}/log.txt'", this.workingDir);
        // LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        // ctx.reconfigure();
        // ctx.updateLoggers();

        this.ripperThreadPool = new DownloadThreadPool(getClass().getSimpleName() + "-ripper-" + getGID(url));
        this.crawlerThreadPool = new DownloadThreadPool(getClass().getSimpleName() + "-crawler");
    }

    public void setObserver(RipStatusHandler obs) {
        this.observer = obs;
    }

    /**
     * Queues image to be downloaded and saved.
     *
     * @param url    URL of the file
     * @param saveAs Path of the local file to save the content to.
     * @return True on success, false on failure.
     */
    public boolean addURLToDownload(URL url, Path saveAs) {
        return addURLToDownload(url, saveAs, null, null, false);
    }

    /**
     * Queues image to be downloaded and saved.
     *
     * @param url      URL of the file
     * @param saveAs   Path of the local file to save the content to.
     * @param referrer The HTTP referrer to use while downloading this file.
     * @param cookies  The cookies to send to the server while downloading this
     *                 file.
     * @return True if downloaded successfully
     *         False if failed to download
     */
    public boolean addURLToDownload(URL url, Path saveAs, String referrer, Map<String, String> cookies, Boolean getFileExtFromMIME) {
        TokenedUrlGetter tug = () -> url;
        RipUrlId ripUrlId = new RipUrlId(getClass(), getHost(), url);
        Path directory = saveAs.getParent();
        String filename = saveAs.getFileName().toString();
        return addURLToDownload(tug, ripUrlId, directory, filename, referrer, cookies, getFileExtFromMIME);
    }

    protected boolean addURLToDownload(TokenedUrlGetter tug, RipUrlId ripUrlId, Path directory, String referrer, Map<String, String> cookies, Boolean getFileExtFromMIME) {
        return addURLToDownload(tug, ripUrlId, directory, null, referrer, cookies, getFileExtFromMIME);
    }

    /**
     * Queues multiple URLs of single images to download from a single Album URL
     */
    public boolean addURLToDownload(TokenedUrlGetter tug, RipUrlId ripUrlId, Path directory, String filename, String referrer, Map<String,String> cookies, Boolean getFileExtFromMIME) {
        // Only download one file if this is a test.
        if (isThisATest() && (itemsCompleted.size() > 0 || itemsErrored.size() > 0)) {
            stop();
            itemsPending.clear();
            return false;
        }
        itemsSeen.incrementAndGet();

        if (!allowDuplicates()
                && ( itemsPending.contains(ripUrlId)
                || itemsCompleted.containsKey(ripUrlId)
                || itemsErrored.containsKey(ripUrlId) )) {
            // Item is already downloaded/downloading, skip it.
            // TODO print path if in itemsCompleted or itemsErrored
            logger.info("[!] Skipping " + ripUrlId + " -- already attempted: " + Utils.removeCWD(directory));
            return false;
        }

        if (Utils.getConfigBoolean("urls_only.save", false)) {
            // Output URL to file
            Path urlFile = Paths.get(this.workingDir + "/urls.txt");
            URL url = null;
            try {
                url = tug.getTokenedUrl();
            } catch (IOException | URISyntaxException e) {
                logger.error("Unable to get URL for {}", ripUrlId, e);
                itemsErrored.put(ripUrlId, e.getMessage());
                return false;
            }
            if (AbstractRipper.shouldIgnoreExtension(url)) {
                sendUpdate(STATUS.DOWNLOAD_SKIP, "Skipping " + url.toExternalForm() + " - ignored extension");
                return false;
            }
            String text = url.toExternalForm() + System.lineSeparator();
            try {
                Files.write(urlFile, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                itemsCompleted.put(ripUrlId, urlFile);
            } catch (IOException e) {
                logger.error("Error while writing to " + urlFile, e);
                return false;
            }
            return true;
        }
        else {
            itemsPending.add(ripUrlId);
            DownloadFileThread dft = new DownloadFileThread(tug, ripUrlId, directory, filename, this, getFileExtFromMIME);
            if (referrer != null) {
                dft.setReferrer(referrer);
            }
            if (cookies != null) {
                dft.setCookies(cookies);
            }
            getRipperThreadPool().addThread(dft);
        }

        return true;
    }



    /**
     * Queues image to be downloaded and saved.
     *
     * @param url     URL of the file
     * @param options A map<String,String> containing any changes to the default
     *                options.
     *                Options are getFileExtFromMIME, prefix, subdirectory,
     *                referrer, fileName, extension, getFileExtFromMIME.
     *                getFileExtFromMIME should be "true" or "false"
     * @param cookies The cookies to send to the server while downloading this file.
     * @return True if downloaded successfully
     *         False if failed to download
     */
    protected boolean addURLToDownload(URL url, Map<String, String> options, Map<String, String> cookies) {
        // Bit of a hack but this lets us pass a bool using a map<string,String>
        boolean useMIME = options.getOrDefault("getFileExtFromMIME", "false").equalsIgnoreCase("true");
        return addURLToDownload(url,
                options.getOrDefault("subdirectory", ""),
                options.getOrDefault("referrer", null),
                cookies,
                options.getOrDefault("prefix", ""), options.getOrDefault("fileName", null),
                options.getOrDefault("extension", null),
                useMIME);
    }

    /**
     * Queues image to be downloaded and saved.
     *
     * @param url     URL of the file
     * @param options A map<String,String> containing any changes to the default
     *                options.
     *                Options are getFileExtFromMIME, prefix, subdirectory,
     *                referrer, fileName, extension, getFileExtFromMIME.
     *                getFileExtFromMIME should be "true" or "false"
     * @return True if downloaded successfully
     *         False if failed to download
     */
    protected boolean addURLToDownload(URL url, Map<String, String> options) {
        return addURLToDownload(url, options, null);
    }

    /**
     * Queues image to be downloaded and saved.
     *
     * @param url          URL of the file
     * @param prefix       Prefix for the downloaded file
     * @param subdirectory Path to get to desired directory from working directory
     * @param referrer     The HTTP referrer to use while downloading this file.
     * @param cookies      The cookies to send to the server while downloading this
     *                     file.
     * @param fileName     The name that file will be written to
     * @return True if downloaded successfully
     *         False if failed to download
     */
    protected boolean addURLToDownload(URL url, String subdirectory, String referrer, Map<String, String> cookies,
            String prefix, String fileName, String extension, Boolean getFileExtFromMIME) {
        // A common bug is rippers adding urls that are just "http:".
        // This rejects said urls.
        if (url.toExternalForm().equals("http:") || url.toExternalForm().equals("https:")) {
            logger.info(url.toExternalForm() + " is a invalid url and will be changed");
            return false;

        }

        // Make sure the url doesn't contain any spaces as that can cause a 400 error
        // when requesting the file
        if (url.toExternalForm().contains(" ")) {
            // If for some reason the url with all spaces encoded as %20 is malformed print
            // an error
            try {
                url = new URI(url.toExternalForm().replaceAll(" ", "%20")).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                logger.error("Unable to remove spaces from url\nURL: " + url.toExternalForm());
                e.printStackTrace();
            }
        }

        // Don't re-add the url if it was downloaded in a previous rip
        if (Utils.getConfigBoolean("remember.url_history", true) && !isThisATest()) {
            if (hasDownloadedURL(url.toExternalForm())) {
                sendUpdate(STATUS.DOWNLOAD_WARN, "Already downloaded " + url.toExternalForm());
                alreadyDownloadedUrls += 1;
                return false;
            }
        }

        try {
            stopCheck();
        } catch (IOException e) {
            logger.debug("Ripper has been stopped");
            return false;
        }

        logger.debug("url: " + url + ", subdirectory" + subdirectory + ", referrer: " + referrer + ", cookies: "
                + cookies + ", prefix: " + prefix + ", fileName: " + fileName);

        Path saveAs;
        try {
            saveAs = getFilePath(url, subdirectory, prefix, fileName, extension);
            logger.debug("Downloading " + url + " to " + saveAs);
            if (!Files.exists(saveAs.getParent())) {
                logger.info("[+] Creating directory: " + saveAs.getParent());
                Files.createDirectories(saveAs.getParent());
            }
        } catch (IOException e) {
            logger.error("[!] Error creating save file path for URL '" + url + "':", e);
            return false;
        }

        if (Utils.getConfigBoolean("remember.url_history", true) && !isThisATest()) {
            logger.info("Writing " + url.toExternalForm() + " to file");
            try {
                writeDownloadedURL(url.toExternalForm() + "\n");
            } catch (IOException e) {
                logger.debug("Unable to write URL history file");
            }
        }

        return addURLToDownload(url, saveAs, referrer, cookies, getFileExtFromMIME);
    }

    protected boolean addURLToDownload(URL url, String prefix, String subdirectory, String referrer,
            Map<String, String> cookies, String fileName, String extension) {
        return addURLToDownload(url, subdirectory, referrer, cookies, prefix, fileName, extension, false);
    }

    protected boolean addURLToDownload(URL url, String prefix, String subdirectory, String referrer,
            Map<String, String> cookies, String fileName) {
        return addURLToDownload(url, prefix, subdirectory, referrer, cookies, fileName, null);
    }

    /**
     * Queues file to be downloaded and saved. With options.
     *
     * @param url          URL to download.
     * @param prefix       Prefix to prepend to the saved filename.
     * @param subdirectory Sub-directory of the working directory to save the images
     *                     to.
     * @return True on success, flase on failure.
     */
    protected boolean addURLToDownload(URL url, String prefix, String subdirectory) {
        return addURLToDownload(url, prefix, subdirectory, null, null, null);
    }

    protected boolean addURLToDownload(URL url, String prefix, String subdirectory,
            String referrer, Map<String, String> cookies) {
        return addURLToDownload(url, prefix, subdirectory, referrer, cookies, null);
    }

    /**
     * Queues image to be downloaded and saved.
     * Uses filename from URL (and 'prefix') to decide filename.
     *
     * @param url
     *               URL to download
     * @param prefix
     *               Text to append to saved filename.
     * @return True on success, flase on failure.
     */
    protected boolean addURLToDownload(URL url, String prefix) {
        // Use empty subdirectory
        return addURLToDownload(url, prefix, "");
    }

    public Path getFilePath(URL url, String subdir, String prefix, String fileName, String extension)
            throws IOException {
        // construct the path: workingdir + subdir + prefix + filename + extension
        // save into working dir
        Path filepath = Paths.get(workingDir.getCanonicalPath());

        if (null != App.stringToAppendToFoldername) {
            filepath = filepath.resolveSibling(filepath.getFileName() + App.stringToAppendToFoldername);
        }

        if (null != subdir && !subdir.trim().isEmpty()) {
            filepath = filepath.resolve(Utils.filesystemSafe(subdir));
        }

        filepath = filepath.resolve(getFileName(url, prefix, fileName, extension));
        return filepath;
    }

    public static String getFileName(URL url, String prefix, String fileName, String extension) {
        // retrieve filename from URL if not passed
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = url.toExternalForm();
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }

        if (fileName.indexOf('?') >= 0) {
            fileName = fileName.substring(0, fileName.indexOf('?'));
        }

        if (fileName.indexOf('#') >= 0) {
            fileName = fileName.substring(0, fileName.indexOf('#'));
        }

        if (fileName.indexOf('&') >= 0) {
            fileName = fileName.substring(0, fileName.indexOf('&'));
        }

        if (fileName.indexOf(':') >= 0) {
            fileName = fileName.substring(0, fileName.indexOf(':'));
        }

        // add prefix
        if (prefix != null && !prefix.trim().isEmpty()) {
            fileName = prefix + fileName;
        }

        // retrieve extension from URL if not passed, no extension if nothing found
        if (extension == null || extension.trim().isEmpty()) {
            // Get the extension of the file
            String[] lastBitOfURL = url.toExternalForm().split("/");

            String[] lastBit = lastBitOfURL[lastBitOfURL.length - 1].split(".");
            if (lastBit.length != 0) {
                extension = lastBit[lastBit.length - 1];
            }
        }

        // if extension is passed or found, add it
        if (extension != null) {
            fileName = fileName + "." + extension;
        }

        // make sure filename is not too long and has no unsupported chars
        return Utils.sanitizeSaveAs(fileName);
    }

    /**
     * Waits for downloading threads to complete.
     */
    protected void waitForRipperThreads() {
        waitForRipperThreads(true);
    }

    protected void waitForRipperThreads(boolean notifyComplete) {
        logger.debug("Waiting for threads to finish; url: {}", url);
        if (!notifyComplete) {
            setItemsTotal(0);
        }
        ripperThreadPool.waitForThreads(() -> {
            boolean finished = shouldStop.get() || (itemsSeen.get() >= itemsTotal.get() && itemsPending.isEmpty());
            logger.trace("ripperThreadPool: are threads finished? {} url: {} shouldStop: {}; itemsPending.size(): {}; itemsCompleted.size(): {}; itemsErrored.size(): {}; itemsSkipped.size(): {}; itemsTotal: {}; itemsSeen: {}",
                    finished, url, shouldStop, itemsPending.size(), itemsCompleted.size(), itemsErrored.size(), itemsSkipped.size(), itemsTotal, itemsSeen);
            return finished;
        }, url);
        if (notifyComplete) {
            notifyComplete();
        }
    }

    /**
     * Notifies observers that source is being retrieved.
     *
     * @param url URL being retrieved
     */
    public void retrievingSource(String url) {
        RipStatusMessage msg = new RipStatusMessage(STATUS.LOADING_RESOURCE, url);
        if (observer != null) {
            observer.update(this, msg);
        }
    }

    /**
     * Notifies observers that a file download has completed.
     *
     * @param ripUrlId The RipUrlId that was completed.
     * @param saveAs   Where the downloaded file is stored.
     */
    protected void downloadCompleted(RipUrlId ripUrlId, Path saveAs) {
        if (observer == null) {
            return;
        }
        try {
            String path = Utils.removeCWD(saveAs);
            RipStatusMessage msg = new RipStatusMessage(STATUS.DOWNLOAD_COMPLETE, path);
            itemsPending.remove(ripUrlId);
            itemsCompleted.put(ripUrlId, saveAs);
            observer.update(this, msg);

            //checkIfComplete();
        } catch (Exception e) {
            logger.error("Exception while updating observer: ", e);
        }
    }

    /**
     * Notifies observers that a file could not be downloaded (includes a reason).
     */
    protected void downloadErrored(RipUrlId ripUrlId, String reason) {
        if (observer == null) {
            return;
        }
        itemsPending.remove(ripUrlId);
        itemsErrored.put(ripUrlId, reason);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_ERRORED, ripUrlId + " : " + reason));

        //checkIfComplete();
    }

    /**
     * Notifies observers that a file could not be downloaded (includes a reason).
     */
    protected void downloadSkipped(RipUrlId ripUrlId, String reason) {
        if (observer == null) {
            return;
        }
        itemsPending.remove(ripUrlId);
        //itemsSkipped.put(ripUrlId, reason);
        itemsCompleted.put(ripUrlId, null); // TODO use itemsSkipped and make the progress bar display it as completed
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_SKIP, ripUrlId + " : " + reason));

        //checkIfComplete();
    }

    /**
     * Notify observers that a download could not be completed,
     * but was not technically an "error".
     */
    protected void downloadExists(RipUrlId ripUrlId, Path file) {
        if (observer == null) {
            return;
        }

        itemsPending.remove(ripUrlId);
        itemsCompleted.put(ripUrlId, file);
        observer.update(this, new RipStatusMessage(STATUS.DOWNLOAD_WARN, ripUrlId + " already saved as " + file));

        //checkIfComplete();
    }

    /**
     * @return Number of files downloaded.
     */
    public int getCount() {
        return itemsCompleted.size() + itemsErrored.size();
    }

    /**
     * Notifies observers and updates state if all files have been ripped.
     */
    protected void notifyComplete() {
        if (observer == null) {
            logger.debug("observer is null");
            return;
        }

        if (!completed.getAndSet(true)) {
            logger.info("   Rip completed!");

            RipStatusComplete rsc = new RipStatusComplete(workingDir.toPath(), getCount());
            RipStatusMessage msg = new RipStatusMessage(STATUS.RIP_COMPLETE, rsc);
            logger.debug("Sending RIP_COMPLETE: url: {}", getURL());
            observer.update(this, msg);

            // we do not care if the rollingfileappender is active,
            // just change the logfile in case
            // TODO - does not work.
            // System.setProperty("logFilename", "ripme.log");
            // logger.debug("Changing log file back to 'ripme.log'");
            // LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            // ctx.reconfigure();

            if (Utils.getConfigBoolean("urls_only.save", false)) {
                String urlFile = this.workingDir + File.separator + "urls.txt";
                try {
                    Desktop.getDesktop().open(new File(urlFile));
                } catch (IOException e) {
                    logger.warn("Error while opening " + urlFile, e);
                }
            }
        }
    }

    /**
     * Gets URL
     *
     * @return Returns URL that wants to be downloaded.
     */
    public URL getURL() {
        return url;
    }

    /**
     * @return Path to the directory in which all files
     *         ripped via this ripper will be stored.
     */
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public abstract void setWorkingDir(URL url) throws IOException, URISyntaxException;

    /**
     * @param url The URL you want to get the title of.
     * @return host_URLid
     *         e.g. (for a reddit post)
     *         reddit_post_7mg2ur
     * @throws MalformedURLException If any of those damned URLs gets malformed.
     */
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            return getHost() + "_" + getGID(url);
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    /**
     * Finds, instantiates, and returns a compatible ripper for given URL.
     *
     * @param url URL to rip.
     * @return Instantiated ripper ready to rip given URL.
     * @throws Exception If no compatible rippers can be found.
     */
    public static AbstractRipper getRipper(URL url) throws Exception {
        for (Constructor<?> constructor : getRipperConstructors("com.rarchives.ripme.ripper.rippers")) {
            try {
                // by design: can throw ClassCastException
                AbstractRipper ripper = (AbstractRipper) constructor.newInstance(url);
                logger.debug("Found album ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (Exception e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        for (Constructor<?> constructor : getRipperConstructors("com.rarchives.ripme.ripper.rippers.video")) {
            try {
                // by design: can throw ClassCastException
                VideoRipper ripper = (VideoRipper) constructor.newInstance(url);
                logger.debug("Found video ripper: " + ripper.getClass().getName());
                return ripper;
            } catch (Exception e) {
                // Incompatible rippers *will* throw exceptions during instantiation.
            }
        }
        throw new Exception("No compatible ripper found");
    }

    /**
     * @param pkg The package name.
     * @return List of constructors for all eligible Rippers.
     */
    public static List<Constructor<?>> getRipperConstructors(String pkg) throws Exception {
        List<Constructor<?>> constructors = new ArrayList<>();
        for (Class<?> clazz : Utils.getClassesForPackage(pkg)) {
            if (AbstractRipper.class.isAssignableFrom(clazz)) {
                constructors.add(clazz.getConstructor(URL.class));
            }
        }
        return constructors;
    }

    /**
     * Sends an update message to the relevant observer(s) on this ripper.
     *
     * @param status
     */
    public void sendUpdate(STATUS status, Object message) {
        if (observer == null) {
            return;
        }
        observer.update(this, new RipStatusMessage(status, message));
    }

    /**
     * Get the completion percentage.
     *
     * @return Percentage complete
     */
    public abstract int getCompletionPercentage();

    /**
     * Rips the album when the thread is invoked.
     */
    public void run() {
        try {
            rip();
        } catch (HttpStatusException e) {
            logger.error("Got exception while running ripper:", e);
            waitForRipperThreads(false);
            sendUpdate(STATUS.RIP_ERRORED, "HTTP status code " + e.getStatusCode() + " for URL " + e.getUrl());
        } catch (Exception e) {
            logger.error("Got exception while running ripper:", e);
            waitForRipperThreads(false);
            sendUpdate(STATUS.RIP_ERRORED, e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Tries to delete any empty directories
     */
    private void cleanup() {
        if (this.workingDir.list().length == 0) {
            // No files, delete the dir
            logger.info("Deleting empty directory " + this.workingDir);
            boolean deleteResult = this.workingDir.delete();
            if (!deleteResult) {
                logger.error("Unable to delete empty directory " + this.workingDir);
            }
        }
    }

    /**
     * To look more like a natural human usage,
     * randomize the requested sleep interval on a gaussian distribution with a
     * std-deviation of 30% of the value requested
     * to look more like natural usage.
     *
     * The method nextGaussian returns a sample from a normal distribution with mean
     * 0 and std-deviation 1.
     * We need to transform this so that the standard deviation is 30% of the
     * requested sleep time.
     *
     * To achieve this, scale the value by 0.3 and multiply it by milliseconds to
     * get the adjustment we want, then apply that to the sleep time.
     * and then multiply that by milliseconds to get the final sleep time.
     * But we also want to make sure that we have an even better guarantee that we
     * sleep long enough
     * to avoid being rate-limited, so make the minimum for this value 47% (an
     * arbitrary number near 50%) less than the requested time.
     *
     * @param milliseconds Sleep time requested.
     * @return A sleep time that has been adjusted according to a gaussian
     *         distribution with mean milliseconds and std-deviation of 30% of
     *         milliseconds.
     */
    private int gaussianJitterSleepValue(int milliseconds) {
        logger.debug("Requested sleep time: " + milliseconds);

        String requestedMsg = " (Requested sleep time was " + milliseconds + " ms)";

        int sleepTime = (int) randomGenerator.nextGaussian(milliseconds, milliseconds * 0.3);
        logger.debug("Sleep time after gaussian jitter: " + sleepTime + requestedMsg);

        int minSleepTime = (int) (milliseconds * 0.47);
        if (sleepTime < minSleepTime) {
            logger.debug("Sleep time after gaussian jitter was " + sleepTime
                    + " which is less than minimum sleep time; adjusting to minimum sleep time of " + minSleepTime
                    + requestedMsg);

            sleepTime = minSleepTime;
        }

        return sleepTime;
    }

    /**
     * Pauses thread for a set amount of time.
     *
     * @param milliseconds Amount of time (in milliseconds) that the thread gets
     *                     paused for
     * @return True if paused successfully
     *         False if failed to pause/got interrupted.
     */
    protected boolean sleep(int milliseconds) {
        milliseconds = gaussianJitterSleepValue(milliseconds);

        try {
            logger.debug("Sleeping " + milliseconds + "ms");
            Thread.sleep(milliseconds);
            return true;
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting to load next page", e);
            return false;
        }
    }

    protected void setBytesTotal(long bytes) {
        this.bytesTotal = bytes;
    }

    protected void setBytesCompleted(long bytes) {
        this.bytesCompleted = bytes;
    }

    /** Methods for detecting when we're running a test. */
    public void markAsTest() {
        logger.debug("THIS IS A TEST RIP");
        thisIsATest = true;
    }

    protected static boolean isThisATest() {
        return thisIsATest;
    }

    // If true ripme uses a byte progress bar
    public boolean useByteProgessBar() {
        return false;
    }

    // If true ripme will try to resume a broken download for this ripper
    protected boolean tryResumeDownload() {
        return false;
    }

    protected static boolean shouldIgnoreExtension(URL url) {
        final String[] ignoredExtensions = Utils.getConfigStringArray("download.ignore_extensions");
        if (ignoredExtensions == null || ignoredExtensions.length == 0)
            return false; // nothing ignored
        String[] pathElements = url.getPath().split("\\.");
        if (pathElements.length == 0)
            return false; // no extension, can't filter
        String extension = pathElements[pathElements.length - 1];
        for (String ignoredExtension : ignoredExtensions) {
            if (ignoredExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public int getPendingCount() {
        DownloadThreadPool threadPool = getRipperThreadPool();
        if (threadPool != null) {
            return threadPool.getPendingThreadCount();
        }
        return itemsPending.size();
    }

    public int getCompletedCount() {
        return itemsCompleted.size();
    }

    public int getErroredCount() {
        return itemsErrored.size();
    }

    public int getActiveCount() {
        return ripperThreadPool.getActiveThreadCount();
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public long getBytesCompleted() {
        return bytesCompleted;
    }

    /**
     * Gets the best estimate of total items.
     */
    public int getTotalCount() {
        if (itemsTotal.get() == 0) {
            return itemsPending.size() + itemsErrored.size() + itemsCompleted.size();
        }
        return itemsTotal.get();
    }

    /**
     * Gets the asserted number of total items, or 0 if unknown.
     * Possibly useful in rippers.
     * MainWindow probably wants {@link #getTotalCount()}
     */
    protected int getItemsTotal() {
        return itemsTotal.get();
    }

    /**
     * For use in rippers to update the best estimate of total items.
     */
    protected void setItemsTotal(int itemsTotal) {
        if (itemsTotal < 0) {
            throw new IllegalArgumentException("itemsTotal cannot be negative. Use 0 for unknown.");
        }
        this.itemsTotal.set(itemsTotal);
    }
}
