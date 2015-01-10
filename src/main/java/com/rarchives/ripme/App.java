package com.rarchives.ripme;

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
import com.rarchives.ripme.ui.MainWindow;
import com.rarchives.ripme.ui.UpdateUtils;
import com.rarchives.ripme.utils.Utils;

/**
 *
 */
public class App {

    public static Logger logger;

    public static void main(String[] args) throws MalformedURLException {
        Utils.configureLogger();
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RipMe");
        logger  = Logger.getLogger(App.class);
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
        if (cl.hasOption('u')) {
            // User provided URL, rip it.
            try {
                URL url = new URL(cl.getOptionValue('u').trim());
                rip(url);
                List<String> history = Utils.getConfigList("download.history");
                if (!history.contains(url.toExternalForm())) {
                    history.add(url.toExternalForm());
                    Utils.setConfigList("download.history", Arrays.asList(history.toArray()));
                    Utils.saveConfig();
                }
            } catch (MalformedURLException e) {
                logger.error("[!] Given URL is not valid. Expected URL format is http://domain.com/...");
                System.exit(-1);
            } catch (Exception e) {
                logger.error("[!] Error while ripping URL " + cl.getOptionValue('u'), e);
                System.exit(-1);
            }
        }
        if (!cl.hasOption('u')) {
            System.err.println("\nRequired URL ('-u' or '--url') not provided");
            System.err.println("\n\tExample: java -jar ripme.jar -u http://imgur.com/a/abcde");
            System.exit(-1);
        }
    }

    public static Options getOptions() {
        Options opts = new Options();
        opts.addOption("h", "help",      false, "Print the help");
        opts.addOption("u", "url",       true,  "URL of album to rip");
        opts.addOption("t", "threads",   true,  "Number of download threads per rip");
        opts.addOption("w", "overwrite", false, "Overwrite existing files");
        opts.addOption("r", "rerip",     false, "Re-rip all ripped albums");
        opts.addOption("d", "saveorder",   false, "Save the order of images in album");
        opts.addOption("D", "nosaveorder", false, "Don't save order of images");
        opts.addOption("4", "skip404",   false, "Don't retry after a 404 (not found) error");
        return opts;
    }

    public static CommandLine getArgs(String[] args) {
        BasicParser parser = new BasicParser();
        try {
            CommandLine cl = parser.parse(getOptions(), args, false);
            return cl;
        } catch (ParseException e) {
            logger.error("[!] Error while parsing command-line arguments: " + args, e);
            System.exit(-1);
            return null;
        }
    }
}
