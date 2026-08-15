package com.rarchives.ripme.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClipboardUtils {

    private static final Logger logger = LogManager.getLogger(ClipboardUtils.class);

    private static AutoripThread autoripThread = new AutoripThread();

    // Dispatch target for autorip'd URLs. Defaults to the Swing entry point so MainWindow's
    // behavior is unchanged; the Compose GUI's QueueController overrides this at startup
    // (see plan §11/§4). This is a lazy method reference - it does not touch MainWindow's
    // static initializer until actually invoked, so running Compose-only never triggers
    // Swing UI construction.
    static Consumer<String> ripHandler = MainWindow::ripAlbumStatic;

    public static void setRipHandler(Consumer<String> handler) {
        ripHandler = handler;
    }

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
                    Pattern p = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
                    Matcher m = p.matcher(clipboard);
                    while (m.find()) {
                        String url = m.group();
                        if (!rippedURLs.contains(url)) {
                            rippedURLs.add(url);
                            ClipboardUtils.ripHandler.accept(url);
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
