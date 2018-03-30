package com.rarchives.ripme;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ui.History;
import com.rarchives.ripme.ui.HistoryEntry;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Utils;

/**
 * Entry point to application.
 * Decides to display UI or to run silently via command-line.
 */
public class App {

    public static final Logger logger = Logger.getLogger(App.class);
    private static final History HISTORY = new History();

    public static void main(String[] args) {
        CommandLine cl = null;

        try {
            cl = getArgs(args);
        } catch (ParseException e) {
            logger.error("Error while parsing command-line arguments: " + Arrays.toString(args), e);
            System.exit(-1);
        }

        if (args.length > 0 && cl.hasOption('v')){
            logger.info(UpdateUtils.getThisJarVersion());
            System.exit(0);
        }

        if (GraphicsEnvironment.isHeadless() || args.length > 0) {
            handleArguments(cl);
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
     */
    private static void rip(URL url) {
        AbstractRipper ripper = null;
        try {
            ripper = AbstractRipper.getRipper(url);
        } catch (NoSuchElementException e) {
            logger.error("No compatible ripper found.", e);
        }
        try {
            ripper.setup();
        } catch (IOException e) {
            logger.error("Working directory not found!", e);
        }
        try {
            ripper.rip();
        } catch (IOException e) {
            logger.error("Rip failed!", e);
        }

    }

    /**
     * For dealing with command-line arguments.
     * @param cl CommandLine object containing the parsed arguments
     */
    private static void handleArguments(CommandLine cl) {
        if (cl.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("java -jar ripme.jar [OPTIONS]", getOptions());
            System.exit(0);
        }

        Utils.configureLogger();
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        if (cl.hasOption('w')) {
            Utils.setConfigBoolean("file.overwrite", true);
        }

        if (cl.hasOption('t')) {
            Utils.setConfigInteger("threads.size", Integer.parseInt(cl.getOptionValue('t')));
        }

        if (cl.hasOption('4')) {
            Utils.setConfigBoolean("errors.skip404", true);
        }

        if (cl.hasOption('r')) {
            // Re-rip all via command-line
            List<String> history = Utils.getConfigList("download.history");
            for (String urlString : history) {
                try {
                    URL url = new URL(urlString.trim());
                    rip(url);
                } catch (MalformedURLException e) {
                    logger.error("Given URL is not valid. Expected URL format is http(s)://(www.)domain.com/", e);
                    continue;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while re-ripping history.");
                    System.exit(-1);
                }
            }
            // Exit
            System.exit(0);
        }

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
                        URL url = new URL(entry.url);
                        rip(url);
                    } catch (MalformedURLException e) {
                        logger.error("Given URL is not valid. Expected URL format is http(s)://(www.)domain.com/", e);
                        continue;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.warn("Interrupted while re-ripping history");
                        System.exit(-1);
                    }
                }
            }
            if (added == 0) {
                // TODO: Find a way to add this to the command-line. As-is, the message doesn't make sense for headless-only users.
                logger.error("No history entries have been 'Checked'\n" +
                    "Check an entry by clicking the checkbox to the right of the URL or Right-click a URL to check/uncheck all items");
                System.exit(-1);
            }
        }

        if (cl.hasOption('d')) {
            Utils.setConfigBoolean("download.save_order", true);
        }

        if (cl.hasOption('D')) {
            Utils.setConfigBoolean("download.save_order", false);
        }

        if ((cl.hasOption('d'))&&(cl.hasOption('D'))) {
            logger.error("\nCannot specify '-d' and '-D' simultaneously");
            System.exit(-1);
        }

        if (cl.hasOption('l')) {
            // change the default rips directory
            Utils.setConfigString("rips.directory", cl.getOptionValue('l'));
        }

        if (cl.hasOption('f')) {
            String filename = cl.getOptionValue('f');
            try {
                String url;
                BufferedReader br = new BufferedReader(new FileReader(filename));
                while ((url = br.readLine()) != null) {
                    // loop through each url in the file and proces each url individually.
                    ripURL(url.trim(), cl.hasOption("n"));
                }
            } catch (FileNotFoundException e) {
                logger.error("File containing list of URLs not found. Cannot continue.", e);
            } catch (IOException e) {
                logger.error("Failed reading file containing list of URLs. Cannot continue.", e);
            }
        }

        if (cl.hasOption('u')) {
            String url = cl.getOptionValue('u').trim();
            ripURL(url, cl.hasOption("n"));
        }
    }

    /**
     * Attempt to rip targetURL.
     * @param targetURL URL to rip
     * @param saveConfig Whether or not you want to save the config (?)
     */
    private static void ripURL(String targetURL, boolean saveConfig) {
        try {
            URL url = new URL(targetURL);
            rip(url);
            List<String> history = Utils.getConfigList("download.history");

            if (!history.contains(url.toExternalForm())) {
                history.add(url.toExternalForm());
                Utils.setConfigList("download.history", Arrays.asList(history.toArray()));
                if (saveConfig) {
                    Utils.saveConfig();
                }
            }
        } catch (MalformedURLException e) {
            logger.error("Given URL is not valid. Expected URL format is http(s)://(www.)domain.com/", e);
            System.exit(-1);
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
        opts.addOption(null, "verbose", false, "Show additional information in the log");
        return opts;
    }

    /**
     * Tries to parse commandline arguments.
     * @param args Array of commandline arguments.
     * @return CommandLine object containing arguments.
     */
    private static CommandLine getArgs(String[] args) throws ParseException {
        BasicParser parser = new BasicParser();
        return parser.parse(getOptions(), args, false);
    }
    
    /**
     * Loads history from history file into memory.
     */
    private static void loadHistory() {
        File historyFile = new File(Utils.getConfigDir() + File.separator + "history.json");
        HISTORY.clear();
        if (historyFile.exists()) {
            try {
                logger.info("Loading history from " + historyFile.getCanonicalPath());
                HISTORY.fromFile(historyFile.getCanonicalPath());
            } catch (IOException e) {
                logger.error("Failed to load history from file " + historyFile, e);
                logger.warn(
                        "RipMe failed to load the history file at " + historyFile.getAbsolutePath() + "\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Closing RipMe will automatically overwrite the contents of this file,\n" +
                        "so you may want to back the file up before closing RipMe!");
            }
        } else {
            logger.info("Loading history from configuration");
            HISTORY.fromList(Utils.getConfigList("download.history"));
            MainWindow.guessHistoryFromRipFolder(HISTORY);
        }
    }
}
