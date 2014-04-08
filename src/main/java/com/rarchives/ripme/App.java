package com.rarchives.ripme;

import java.net.MalformedURLException;
import java.net.URL;

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

    public static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws MalformedURLException {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RipMe");
        logger.info("Initialized ripme v" + UpdateUtils.getThisJarVersion());
        UpdateUtils.moveUpdatedJar();
        if (args.length > 0) {
            CommandLine cl = handleArguments(args);

            try {
                URL url = new URL(cl.getOptionValue('u'));
                rip(url);
            } catch (MalformedURLException e) {
                logger.error("[!] Given URL is not valid. Expected URL format is http://domain.com/...");
                System.exit(-1);
            }
        } else {
            MainWindow mw = new MainWindow();
            SwingUtilities.invokeLater(mw);
        }
    }

    public static void rip(URL url) {
        try {
            AbstractRipper ripper = AbstractRipper.getRipper(url);
            ripper.rip();
        } catch (Exception e) {
            logger.error("[!] Error while ripping: " + e.getMessage(), e);
            System.exit(-1);
        }
    }

    public static CommandLine handleArguments(String[] args) {
        CommandLine cl = getArgs(args);
        if (cl.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("asdf", getOptions());
            System.exit(0);
        }
        if (cl.hasOption('w')) {
            Utils.setConfigBoolean("file.overwrite", true);
        }
        if (cl.hasOption('t')) {
            Utils.setConfigInteger("threads.size", Integer.parseInt(cl.getOptionValue('t')));
        }
        if (!cl.hasOption('u')) {
            System.err.println("\nRequired URL ('-u' or '--url') not provided");
            System.err.println("\n\tExample: java -jar ripme.jar -u http://imgur.com/a/abcde");
            System.exit(-1);
        }
        return cl;
    }

    public static Options getOptions() {
        Options opts = new Options();
        opts.addOption("h", "help",      false, "Print the help");
        opts.addOption("u", "url",       true,  "URL of album to rip");
        opts.addOption("t", "threads",   true,  "Number of download threads per rip");
        opts.addOption("w", "overwrite", false, "Overwrite existing files");
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
