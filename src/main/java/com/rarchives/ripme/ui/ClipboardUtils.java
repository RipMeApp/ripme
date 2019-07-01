package com.rarchives.ripme.ui;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rarchives.ripme.App.logger;

class ClipboardUtils {
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
        try {
            return (String) Toolkit
                    .getDefaultToolkit()
                    .getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.error("Caught and recovered from IllegalStateException: " + e.getMessage());
        } catch (HeadlessException | IOException | UnsupportedFlavorException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class AutoripThread extends Thread {
    volatile boolean isRunning = false;
    private Set<String> rippedURLs = new HashSet<>();

    public void run() {
        isRunning = true;
        try {
            while (isRunning) {
                // Check clipboard
                String clipboard = ClipboardUtils.getClipboardString();
                if (clipboard != null) {
                    Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
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
