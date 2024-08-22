package com.rarchives.ripme;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ui.History;
import com.rarchives.ripme.ui.HistoryEntry;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Proxy;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

/**
 * Entry point to the RipMe application.
 * Handles both command-line execution and GUI mode.
 */
public class App {
    public static final Logger logger = Logger.getLogger(App.class);
    private static String stringToAppendToFoldername = null;
    private static final History HISTORY = new History();

    public static void main(String[] args) {
        CommandLine cl = parseArguments(args);

        configureProxySettings();
        configureAppendToFolderName(cl);

        if (shouldRunInCommandLineMode(args)) {
            handleCommandLineArguments(cl, args);
        } else {
            launchGUI();
        }
    }

    private static void configureProxySettings() {
        if (Utils.getConfigString("proxy.http", null) != null) {
            Proxy.setHTTPProxy(Utils.getConfigString("proxy.http", null));
        } else if (Utils.getConfigString("proxy.socks", null) != null) {
            Proxy.setSocks(Utils.getConfigString("proxy.socks", null));
        }
    }

    private static void configureAppendToFolderName(CommandLine cl) {
        if (cl.hasOption("a")) {
            stringToAppendToFoldername = cl.getOptionValue("a");
            logger.info("Appending string to folder name: " + stringToAppendToFoldername);
        }
    }

    private static boolean shouldRunInCommandLineMode(String[] args) {
        return GraphicsEnvironment.isHeadless() || args.length > 0;
    }

    private static void launchGUI() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RipMe");
        }

        Utils.configureLogger();
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        MainWindow mw = new MainWindow();
        SwingUtilities.invokeLater(mw);
    }

    private static void handleCommandLineArguments(CommandLine cl, String[] args) {
        if (cl.hasOption('h') || args.length == 0) {
            printHelp();
            return;
        }

        Utils.configureLogger();
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        if (cl.hasOption('H')) {
            setHistoryFileLocation(cl.getOptionValue('H'));
        }

        configureDownloadSettings(cl);

        if (cl.hasOption('r')) {
            reripAllAlbums();
        } else if (cl.hasOption('R')) {
            reripSelectedAlbums();
        } else if (cl.hasOption('f')) {
            ripUrlsFromFile(cl.getOptionValue('f'), !cl.hasOption("n"));
        } else if (cl.hasOption('u')) {
            ripUrl(cl.getOptionValue('u').trim(), !cl.hasOption("n"));
        } else if (cl.hasOption('j')) {
            UpdateUtils.updateProgramCLI();
        }
    }

    private static void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("java -jar ripme.jar [OPTIONS]", getOptions());
        System.exit(0);
    }

    private static void setHistoryFileLocation(String historyLocation) {
        Utils.setConfigString("history.location", historyLocation);
        logger.info("Set history file to " + historyLocation);
    }

    private static void configureDownloadSettings(CommandLine cl) {
        if (cl.hasOption('w')) {
            Utils.setConfigBoolean("file.overwrite", true);
        }

        if (cl.hasOption('s')) {
            Proxy.setSocks(cl.getOptionValue('s').trim());
        }

        if (cl.hasOption('p')) {
            Proxy.setHTTPProxy(cl.getOptionValue('p').trim());
        }

        if (cl.hasOption('t')) {
            Utils.setConfigInteger("threads.size", Integer.parseInt(cl.getOptionValue('t')));
        }

        if (cl.hasOption('4')) {
            Utils.setConfigBoolean("errors.skip404", true);
        }

        if (cl.hasOption('l')) {
            Utils.setConfigString("rips.directory", cl.getOptionValue('l'));
        }

        if (cl.hasOption('d')) {
            Utils.setConfigBoolean("download.save_order", true);
        } else if (cl.hasOption('D')) {
            Utils.setConfigBoolean("download.save_order", false);
        }
    }

    private static void reripAllAlbums() {
        loadHistory();
        if (HISTORY.isEmpty()) {
            logger.error("No history entries to rerip. Rip some albums first.");
            System.exit(-1);
        }

        for (HistoryEntry entry : HISTORY.toList()) {
            try {
                rip(new URL(entry.url));
            } catch (Exception e) {
                logger.error("Failed to rip URL " + entry.url, e);
            }
            sleep(500);
        }
        System.exit(0);
    }

    private static void reripSelectedAlbums() {
        loadHistory();
        if (HISTORY.isEmpty()) {
            logger.error("No history entries to rerip. Rip some albums first.");
            System.exit(-1);
        }

        int added = 0;
        for (HistoryEntry entry : HISTORY.toList()) {
            if (entry.selected) {
                added++;
                try {
                    rip(new URL(entry.url));
                } catch (Exception e) {
                    logger.error("Failed to rip URL " + entry.url, e);
                }
                sleep(500);
            }
        }

        if (added == 0) {
            logger.error("No selected history entries to rerip.");
            System.exit(-1);
        }
    }

    private static void ripUrlsFromFile(String filename, boolean saveConfig) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String url;
            while ((url = br.readLine()) != null) {
                if (isComment(url)) {
                    logger.debug("Skipping comment line: " + url);
                } else {
                    ripUrl(url.trim(), saveConfig);
                }
            }
        } catch (FileNotFoundException fne) {
            logger.error("File not found: " + filename);
        } catch (IOException ioe) {
            logger.error("Failed to read file: " + filename);
        }
    }

    private static boolean isComment(String line) {
        return line.startsWith("//") || line.startsWith("#");
    }

    private static void ripUrl(String targetURL, boolean saveConfig) {
        try {
            URL url = new URL(targetURL);
            rip(url);
            if (saveConfig) {
                saveHistory();
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid URL format: " + targetURL);
        } catch (Exception e) {
            logger.error("Error ripping URL: " + targetURL, e);
        }
    }

    private static void rip(URL url) throws Exception {
        AbstractRipper ripper = AbstractRipper.getRipper(url);
        ripper.setup();
        ripper.rip();

        updateHistory(ripper);
    }

    private static void updateHistory(AbstractRipper ripper) {
        String url = ripper.getURL().toExternalForm();
        Date date = new Date();

        if (HISTORY.containsURL(url)) {
            HistoryEntry entry = HISTORY.getEntryByURL(url);
            entry.modifiedDate = date;
        } else {
            HistoryEntry entry = createNewHistoryEntry(ripper, url, date);
            HISTORY.add(entry);
        }
    }

    private static HistoryEntry createNewHistoryEntry(AbstractRipper ripper, String url, Date date) {
        HistoryEntry entry = new HistoryEntry();
        entry.url = url;
        entry.dir = ripper.getWorkingDir().getAbsolutePath();
        entry.modifiedDate = date;
        try {
            entry.title = ripper.getAlbumTitle(ripper.getURL());
        } catch (MalformedURLException ignored) {
        }
        return entry;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn("Interrupted during sleep");
            System.exit(-1);
        }
    }

    private static void loadHistory() {
        HISTORY.clear();
        File historyFile = new File(Utils.getConfigDir() + File.separator + "history.json");
        if (historyFile.exists()) {
            loadHistoryFromFile(historyFile);
        } else {
            loadHistoryFromConfig();
        }
    }

    private static void loadHistoryFromFile(File historyFile) {
        try {
            logger.info("Loading history from " + historyFile.getCanonicalPath());
            HISTORY.fromFile(historyFile.getCanonicalPath());
        } catch (IOException e) {
            logger.error("Failed to load history file", e);
        }
    }

    private static void loadHistoryFromConfig() {
        logger.info("Loading history from config file");
        try {
            HISTORY.fromFile(Utils.getConfigDir() + File.separator + "history.json");
        } catch (IOException e) {
            logger.error("Failed to load history from config", e);
        }
    }

    private static void saveHistory() {
        try {
            String historyFilePath = Utils.getConfigDir() + File.separator + "history.json";
            logger.info("Saving history to " + historyFilePath);
            HISTORY.save(historyFilePath);
        } catch (IOException e) {
            logger.error("Failed to save history", e);
        }
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Show this help message")
               .addOption("u", "url", true, "URL of album to rip")
               .addOption("f", "file", true, "File containing list of URLs to rip")
               .addOption("r", "rerip", false, "Rerip all ripped albums")
               .addOption("R", "rerip-selected", false, "Rerip selected ripped albums")
               .addOption("w", "overwrite", false, "Overwrite existing files")
               .addOption("s", "socksProxy", true, "Use the specified SOCKS proxy")
               .addOption("p", "httpProxy", true, "Use the specified HTTP proxy")
               .addOption("t", "threads", true, "Number of download threads")
               .addOption("l", "directory", true, "Save downloaded albums to this directory")
               .addOption("d", "saveOrder", false, "Save download order to disk")
               .addOption("D", "disableSaveOrder", false, "Do not save download order to disk")
               .addOption("H", "historyFile", true, "Set history file location")
               .addOption("a", "append", true, "Append text to the end of ripped folder names")
               .addOption("4", "skip404", false, "Skip 404 errors during ripping")
               .addOption("j", "update", false, "Check for and install updates")
               .addOption("n", "noHistory", false, "Do not save URLs to history.json");
        return options;
    }

    private static CommandLine parseArguments(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(getOptions(), args);
        } catch (ParseException e) {
            logger.error("Failed to parse command line arguments", e);
            printHelp();
        }
        return cl;
    }
}
