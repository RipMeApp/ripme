package com.rarchives.ripme;

import java.io.File;
import java.io.FilenameFilter;
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
import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ui.History;
import com.rarchives.ripme.ui.HistoryEntry;
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.Utils;

/**
 * Entry point to application.
 * Decides to display UI or to run silently via command-line.
 */
public class App {

    public static Logger logger;
    private static final History HISTORY = new History();

    public static void main(String[] args) throws MalformedURLException {
        Utils.configureLogger();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RipMe");
        logger = Logger.getLogger(App.class);
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());

        if (args.length > 0) {
            handleArguments(args);
        } else {
            MainWindow mw = new MainWindow();
            SwingUtilities.invokeLater(mw);
        }
    }

    public static void rip(URL url) throws Exception {
        AbstractRipper ripper = AbstractRipper.getRipper(url);
        ripper.setup();
        ripper.rip();
    }

    public static void handleArguments(String[] args) {
        CommandLine cl = getArgs(args);
        if (cl.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("java -jar ripme.jar [OPTIONS]", getOptions());
            System.exit(0);
        }
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
            if (HISTORY.toList().size() == 0) {
                System.err.println("There are no history entries to re-rip. Rip some albums first");
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
                System.err.println("No history entries have been 'Checked'\n" +
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
            System.err.println("\nCannot specify '-d' and '-D' simultaneously");
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

    // this function will attempt to rip the provided url
    public static void ripURL(String targetURL, boolean saveConfig) {
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
            logger.error("[!] Given URL is not valid. Expected URL format is http://domain.com/...");
            // System.exit(-1);
        } catch (Exception e) {
            logger.error("[!] Error while ripping URL " + targetURL, e);
            // System.exit(-1);
        }
    }

    public static Options getOptions() {
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
        return opts;
    }

    public static CommandLine getArgs(String[] args) {
        BasicParser parser = new BasicParser();
        try {
            CommandLine cl = parser.parse(getOptions(), args, false);
            return cl;
        } catch (ParseException e) {
            logger.error("[!] Error while parsing command-line arguments: " + Arrays.toString(args), e);
            System.exit(-1);
            return null;
        }
    }

    private static void loadHistory() {
        File historyFile = new File("history.json");
        HISTORY.clear();
        if (historyFile.exists()) {
            try {
                logger.info("Loading history from history.json");
                HISTORY.fromFile("history.json");
            } catch (IOException e) {
                logger.error("Failed to load history from file " + historyFile, e);
                System.out.println(
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
                String[] dirs = Utils.getWorkingDirectory().list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String file) {
                        return new File(dir.getAbsolutePath() + File.separator + file).isDirectory();
                    }
                });
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
