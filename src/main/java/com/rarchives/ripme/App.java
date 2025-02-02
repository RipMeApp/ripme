package com.rarchives.ripme;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ui.History;
import com.rarchives.ripme.ui.HistoryEntry;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Proxy;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Entry point to application.
 * This is where all the fun happens, with the main method.
 * Decides to display UI or to run silently via command-line.
 *
 * As the "controller" to all other classes, it parses command line parameters and loads the history.
 */
public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    public static String stringToAppendToFoldername = null;
    private static final History HISTORY = new History();

    /**
     * Where everything starts. Takes in, and tries to parse as many commandline arguments as possible.
     * Otherwise, it launches a GUI.
     *
     * @param args Array of command line arguments.
     */
    public static void main(String[] args) throws IOException {
        CommandLine cl = getArgs(args);

        if (args.length > 0 && cl.hasOption('v')){
            System.out.println(UpdateUtils.getThisJarVersion());
            System.exit(0);
        }

        if (Utils.getConfigString("proxy.http", null) != null) {
            Proxy.setHTTPProxy(Utils.getConfigString("proxy.http", null));
        } else if (Utils.getConfigString("proxy.socks", null) != null) {
            Proxy.setSocks(Utils.getConfigString("proxy.socks", null));
        }

        // This has to be here instead of handleArgs because handleArgs isn't parsed until after a item is ripper
        if (cl.hasOption("a")) {
            logger.info(cl.getOptionValue("a"));
            stringToAppendToFoldername = cl.getOptionValue("a");
        }

        if (GraphicsEnvironment.isHeadless() || args.length > 0) {
            handleArguments(args);
        } else {
            if (SystemUtils.IS_OS_MAC_OSX) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RipMe");
            }

            Utils.configureLogger();

            logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

            MainWindow mw = new MainWindow();
            SwingUtilities.invokeLater(mw);
        }
    }

    /**
     * Creates an abstract ripper and instructs it to rip.
     * @param url URL to be ripped
     * @throws Exception Nothing too specific here, just a catch-all.
     *
     */
    private static void rip(URL url) throws Exception {
        AbstractRipper ripper = AbstractRipper.getRipper(url);
        ripper.setup();
        ripper.rip();

        String u = ripper.getURL().toExternalForm();
        Date date = new Date();
        if (HISTORY.containsURL(u)) {
            HistoryEntry entry = HISTORY.getEntryByURL(u);
            entry.modifiedDate = date;
        } else {
            HistoryEntry entry = new HistoryEntry();
            entry.url = u;
            entry.dir = ripper.getWorkingDir().getAbsolutePath();
            try {
                entry.title = ripper.getAlbumTitle(ripper.getURL());
            } catch (MalformedURLException ignored) { }
            HISTORY.add(entry);
        }
    }

    /**
     * For dealing with command-line arguments.
     * @param args Array of Command-line arguments
     */
    private static void handleArguments(String[] args) throws IOException {
        CommandLine cl = getArgs(args);

        //Help (list commands)
        if (cl.hasOption('h') || args.length == 0) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("java -jar ripme.jar [OPTIONS]", getOptions());
            System.exit(0);
        }

        Utils.configureLogger();
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        //Set history file
        if (cl.hasOption('H')) {
            String historyLocation = cl.getOptionValue('H');
            Utils.setConfigString("history.location", historyLocation);
            logger.info("Set history file to " + historyLocation);
        }

        //Allow file overwriting
        if (cl.hasOption('w')) {
            Utils.setConfigBoolean("file.overwrite", true);
        }

        //SOCKS proxy server
        if (cl.hasOption('s')) {
            String sservfull = cl.getOptionValue('s').trim();
            Proxy.setSocks(sservfull);
        }

        //HTTP proxy server
        if (cl.hasOption('p')) {
            String proxyserverfull = cl.getOptionValue('p').trim();
            Proxy.setHTTPProxy(proxyserverfull);
        }

        //Number of threads
        if (cl.hasOption('t')) {
            Utils.setConfigInteger("threads.size", Integer.parseInt(cl.getOptionValue('t')));
        }

        //Ignore 404
        if (cl.hasOption('4')) {
            Utils.setConfigBoolean("errors.skip404", true);
        }

        //Destination directory
        if (cl.hasOption('l')) {
            // change the default rips directory
            Utils.setConfigString("rips.directory", cl.getOptionValue('l'));
        }

        //Re-rip <i>all</i> previous albums
        if (cl.hasOption('r')) {
            // Re-rip all via command-line
            loadHistory();
            if (HISTORY.toList().isEmpty()) {
                logger.error("There are no history entries to re-rip. Rip some albums first");
                System.exit(-1);
            }
            for (HistoryEntry entry : HISTORY.toList()) {
                try {
                    URL url = new URI(entry.url).toURL();
                     rip(url);
                } catch (Exception e) {
                    logger.error("[!] Failed to rip URL " + entry.url, e);
                    continue;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.warn("[!] Interrupted while re-ripping history");
                    System.exit(-1);
                }
            }
            // Exit
            System.exit(0);
        }

        //Re-rip all <i>selected</i> albums
        if (cl.hasOption('R')) {
            loadHistory();
            if (HISTORY.toList().isEmpty()) {
                logger.error("There are no history entries to re-rip. Rip some albums first");
                System.exit(-1);
            }
            int added = 0;
            for (HistoryEntry entry : HISTORY.toList()) {
                if (entry.selected) {
                    added++;
                    try {
                        URL url = new URI(entry.url).toURL();
                        rip(url);
                    } catch (Exception e) {
                        logger.error("[!] Failed to rip URL " + entry.url, e);
                        continue;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.warn("[!] Interrupted while re-ripping history");
                        System.exit(-1);
                    }
                }
            }
            if (added == 0) {
                logger.error("No history entries have been 'Checked'\n" +
                    "Check an entry by clicking the checkbox to the right of the URL or Right-click a URL to check/uncheck all items");
                System.exit(-1);
            }
        }

        //Save the order of images in album
        if (cl.hasOption('d')) {
            Utils.setConfigBoolean("download.save_order", true);
        }

        //Don't save the order of images in album
        if (cl.hasOption('D')) {
            Utils.setConfigBoolean("download.save_order", false);
        }

        //In case specify both, break and exit since it isn't possible.
        if ((cl.hasOption('d'))&&(cl.hasOption('D'))) {
            logger.error("\nCannot specify '-d' and '-D' simultaneously");
            System.exit(-1);
        }

        //Read URLs from File
        if (cl.hasOption('f')) {
            Path urlfile = Paths.get(cl.getOptionValue('f'));

            try (BufferedReader br = Files.newBufferedReader(urlfile)) {
                String url;
                while ((url = br.readLine()) != null) {
                    if (url.startsWith("//") || url.startsWith("#")) {
                        logger.debug("Skipping over line \"" + url + "\"because it is a comment");
                    } else {
                        // loop through each url in the file and process each url individually.
                        ripURL(url.trim(), !cl.hasOption("n"));
                    }
                }
            } catch (FileNotFoundException fne) {
                logger.error("[!] File containing list of URLs not found. Cannot continue.");
            } catch (IOException ioe) {
                logger.error("[!] Failed reading file containing list of URLs. Cannot continue.");
            }
        }

        //The URL to rip.
        if (cl.hasOption('u')) {
            loadHistory();
            String url = cl.getOptionValue('u').trim();
            ripURL(url, !cl.hasOption("n"));
        }

        if (cl.hasOption('j')) {
            UpdateUtils.updateProgramCLI();
        }

    }

    /**
     * Attempt to rip targetURL.
     * @param targetURL URL to rip
     * @param saveConfig Whether you want to save the config (?)
     */
    private static void ripURL(String targetURL, boolean saveConfig) {
        try {
            URL url = new URI(targetURL).toURL();
            rip(url);
            saveHistory();
        } catch (MalformedURLException e) {
            logger.error("[!] Given URL is not valid. Expected URL format is http://domain.com/...");
            // System.exit(-1);
        } catch (Exception e) {
            logger.error("[!] Error while ripping URL " + targetURL, e);
            // System.exit(-1);
        }
    }

    /**
     * Creates an Options object, returns it.
     * @return Returns all acceptable command-line options.
     */
    private static Options getOptions() {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Print the help");
        opts.addOption("u", "url", true, "URL of album to rip");
        opts.addOption("t", "threads", true, "Number of download threads per rip");
        opts.addOption("w", "overwrite", false, "Overwrite existing files");
        opts.addOption("r", "rerip", false, "Re-rip all ripped albums");
        opts.addOption("R", "rerip-selected", false, "Re-rip all selected albums");
        opts.addOption("d", "saveorder", false, "Save the order of images in album");
        opts.addOption("D", "nosaveorder", false, "Don't save order of images");
        opts.addOption("4", "skip404", false, "Don't retry after a 404 (not found) error");
        opts.addOption("l", "ripsdirectory", true, "Rips Directory (Default: ./rips)");
        opts.addOption("n", "no-prop-file", false, "Do not create properties file.");
        opts.addOption("f", "urls-file", true, "Rip URLs from a file.");
        opts.addOption("v", "version", false, "Show current version");
        opts.addOption("s", "socks-server", true, "Use socks server ([user:password]@host[:port])");
        opts.addOption("p", "proxy-server", true, "Use HTTP Proxy server ([user:password]@host[:port])");
        opts.addOption("j", "update", false, "Update ripme");
        opts.addOption("a","append-to-folder", true, "Append a string to the output folder name");
        opts.addOption("H", "history", true, "Set history file location.");
        return opts;
    }

    /**
     * Tries to parse commandline arguments.
     * @param args Array of commandline arguments.
     * @return CommandLine object containing arguments.
     */
    private static CommandLine getArgs(String[] args) {
        var parser = new DefaultParser();
        try {
            return parser.parse(getOptions(), args, false);
        } catch (ParseException e) {
            logger.error("[!] Error while parsing command-line arguments: " + Arrays.toString(args), e);
            System.exit(-1);
            return null;
        }
    }

    /**
     * Loads history from history file into memory.
     */
    private static void loadHistory() throws IOException {
        Path historyFile = Paths.get(Utils.getConfigDir() + "/history.json");
        HISTORY.clear();
        if (Files.exists(historyFile)) {
            try {
                logger.info("Loading history from " + historyFile);
                HISTORY.fromFile(historyFile.toString());
            } catch (IOException e) {
                logger.error("Failed to load history from file " + historyFile, e);
                logger.warn(
                        "RipMe failed to load the history file at " + historyFile + "\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Closing RipMe will automatically overwrite the contents of this file,\n" +
                        "so you may want to back the file up before closing RipMe!");
            }
        } else {
            logger.info("Loading history from configuration");
            HISTORY.fromList(Utils.getConfigList("download.history"));
            if (HISTORY.toList().isEmpty()) {
                // Loaded from config, still no entries.
                // Guess rip history based on rip folder
                Stream<Path> stream = Files.list(Utils.getWorkingDirectory())
                        .filter(Files::isDirectory);

                stream.forEach(dir -> {
                    String url = RipUtils.urlFromDirectoryName(dir.toString());
                    if (url != null) {
                        // We found one, add it to history
                        HistoryEntry entry = new HistoryEntry();
                        entry.url = url;
                        HISTORY.add(entry);
                    }
                });
            }
        }
    }

    /*
    * @see MainWindow.saveHistory
    */
    private static void saveHistory() {
        Path historyFile = Paths.get(Utils.getConfigDir() + "/history.json");
        try {
            if (!Files.exists(historyFile)) {
                Files.createDirectories(historyFile.getParent());
                Files.createFile(historyFile);
            }

            HISTORY.toFile(historyFile.toString());
            Utils.setConfigList("download.history", Collections.emptyList());
        } catch (IOException e) {
            logger.error("Failed to save history to file " + historyFile, e);
        }
    }
}
