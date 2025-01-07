package com.rarchives.ripme.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class ClipboardUtils {

    private static final Logger logger = LogManager.getLogger(ClipboardUtils.class);

    private static AutoripThread autoripThread = new AutoripThread();

    public static void setClipboardAutoRip(boolean enabled) {
        if (enabled) {
            autoripThread.kill();
            autoripThread = new AutoripThread();
            autoripThread.isRunning = true;
            autoripThread.start();
        } else {
            autoripThread.kill();
        }
    }
    public static boolean getClipboardAutoRip() {
        return autoripThread.isRunning;
    }

    public static String getClipboardString() {
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                logger.debug("ignore this one" + e.getMessage());
            }
        }
        return null;
    }
}

class AutoripThread extends Thread {
    volatile boolean isRunning = false;
    private final Set<String> rippedURLs = new HashSet<>();

    public void run() {
        isRunning = true;
        try {
            while (isRunning) {
                // Check clipboard
                String clipboard = ClipboardUtils.getClipboardString();
                if (clipboard != null) {
                    Pattern p = Pattern.compile(
                            // TODO: This regex is a monster and doesn't match all links; It needs to be rewritten
                            "\\b(((ht|f)tp(s?)://|~/|/)|www.)" +
                            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
                            "|mil|biz|info|mobi|name|aero|jobs|museum" +
                            "|travel|cafe|[a-z]{2}))(:[\\d]{1,5})?" +
                            "(((/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|/)+|\\?|#)?" +
                            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
                            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
                            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
                            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");
                    Matcher m = p.matcher(clipboard);
                    while (m.find()) {
                        String url = m.group();
                        if (!rippedURLs.contains(url)) {
                            rippedURLs.add(url);
                            // TODO Queue rip instead of just starting it
                            MainWindow.ripAlbumStatic(url);
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        isRunning = false;
    }
}
