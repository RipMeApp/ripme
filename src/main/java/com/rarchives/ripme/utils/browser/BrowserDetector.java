package com.rarchives.ripme.utils.browser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Finds out which supported browsers are currently running, by looking at the process list.
 */
final class BrowserDetector {

    private BrowserDetector() {
    }

    static Set<Browser> runningBrowsers() {
        Set<Browser> found = EnumSet.noneOf(Browser.class);
        try {
            ProcessHandle.allProcesses().forEach(process -> process.info().command().ifPresent(command -> {
                String name = normalize(command);
                for (Browser browser : Browser.values()) {
                    if (browser.matchesProcess(name)) {
                        found.add(browser);
                    }
                }
            }));
        } catch (RuntimeException e) {
            // Process inspection can be denied (SecurityException) or unsupported; fall back to installed browsers.
        }
        return found;
    }

    static String normalize(String command) {
        Path fileName = Paths.get(command).getFileName();
        if (fileName == null) {
            return "";
        }
        return fileName.toString().toLowerCase(Locale.ROOT).replaceAll("\\.exe$", "").replace(' ', '-');
    }
}
