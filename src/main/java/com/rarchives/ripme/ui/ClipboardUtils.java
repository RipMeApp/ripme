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
    private static final Pattern rippableUrlPattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    volatile boolean isRunning = false;
    private final Set<String> rippedURLs = new HashSet<>();

    public void run() {
        isRunning = true;
        try {
            while (isRunning) {
                // Check clipboard
                String clipboard = ClipboardUtils.getClipboardString();
                if (clipboard != null) {
                    Matcher m = rippableUrlPattern.matcher(clipboard);
                    while (m.find()) {
                        String url = m.group();
                        if (!rippedURLs.contains(url)) {
                            rippedURLs.add(url);
                            // TODO Queue rip instead of just starting it
                            MainWindow.ripAlbumStatic(url);
                        }
                    }
                }
                Thread.sleep(250);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void kill() {
        isRunning = false;
    }
}
