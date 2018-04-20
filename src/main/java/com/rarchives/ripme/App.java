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
import com.rarchives.ripme.utils.Proxy;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

/**
 * Entry point to application.
 * Decides to display UI or to run silently via command-line.
 */
public class App {

    public static final Logger logger = Logger.getLogger(App.class);
    private static final History HISTORY = new History();

    public static void main(String[] args) throws MalformedURLException {
        CommandLine cl = getArgs(args);

        if (args.length > 0 && cl.hasOption('v')){
            logger.info(UpdateUtils.getThisJarVersion());
            System.exit(0);
        }

        if (Utils.getConfigString("proxy.http", null) != null) {
            Proxy.setHTTPProxy(Utils.getConfigString("proxy.http", null));
        } else if (Utils.getConfigString("proxy.socks", null) != null) {
            Proxy.setSocks(Utils.getConfigString("proxy.socks", null));
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
     * @throws Exception
     */
    private static void rip(URL url) throws Exception {
        AbstractRipper ripper = AbstractRipper.getRipper(url);
        ripper.setup();
        ripper.rip();
    }

    /**
     * For dealing with command-line arguments.
     * @param args Array of Command-line arguments
     */
    private static void handleArguments(String[] args) {
        CommandLine cl = getArgs(args);

        if (cl.hasOption('h') || args.length == 0) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("java -jar ripme.jar [OPTIONS]", getOptions());
            System.exit(0);
        }

        Utils.configureLogger();
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        if (cl.hasOption('w')) {
            Utils.setConfigBoolean("file.overwrite", true);
        }

        if (cl.hasOption('s')) {
            String sservfull = cl.getOptionValue('s').trim();
            Proxy.setSocks(sservfull);
        }

        if (cl.hasOption('p')) {
            String proxyserverfull = cl.getOptionValue('p').trim();
            Proxy.setHTTPProxy(proxyserverfull);
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
                } catch (Exception e) {
                    logger.error("[!] Failed to rip URL " + urlString, e);
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
            } catch (FileNotFoundException fne) {
                logger.error("[!] File containing list of URLs not found. Cannot continue.");
            } catch (IOException ioe) {
                logger.error("[!] Failed reading file containing list of URLs. Cannot continue.");
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
            if (!history.contains(url.toExternalForm())) {//if you haven't already downloaded the file before
                history.add(url.toExternalForm());//add it to history so you won't have to redownload
                Utils.setConfigList("download.history", Arrays.asList(history.toArray()));
                if (saveConfig) {
                    Utils.saveConfig();
                }
            }
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
        return opts;
    }

    /**
     * Tries to parse commandline arguments.
     * @param args Array of commandline arguments.
     * @return CommandLine object containing arguments.
     */
    private static CommandLine getArgs(String[] args) {
        BasicParser parser = new BasicParser();
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
            if (HISTORY.toList().size() == 0) {
                // Loaded from config, still no entries.
                // Guess rip history based on rip folder
                String[] dirs = Utils.getWorkingDirectory().list((dir, file) -> new File(dir.getAbsolutePath() + File.separator + file).isDirectory());
                for (String dir : dirs) {
                    String url = RipUtils.urlFromDirectoryName(dir);
                    if (url != null) {
                        // We found one, add it to history
                        HistoryEntry entry = new HistoryEntry();
                        entry.url = url;
                        HISTORY.add(entry);
                    }
                }
            }
        }
    }
}
