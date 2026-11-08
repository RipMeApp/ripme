package com.rarchives.ripme.utils.browser;

import java.util.List;

/**
 * Browsers whose cookie stores can be read.
 */
public enum Browser {
    FIREFOX("Firefox", false, List.of("firefox", "firefox-bin")),
    EDGE("Microsoft Edge", true, List.of("msedge", "microsoft-edge", "microsoft-edge-stable")),
    CHROME("Google Chrome", true, List.of("chrome", "google-chrome", "google-chrome-stable")),
    BRAVE("Brave", true, List.of("brave", "brave-browser", "brave-browser-stable")),
    CHROMIUM("Chromium", true, List.of("chromium", "chromium-browser"));

    private final String displayName;
    private final boolean chromiumBased;
    private final List<String> processNames;

    Browser(String displayName, boolean chromiumBased, List<String> processNames) {
        this.displayName = displayName;
        this.chromiumBased = chromiumBased;
        this.processNames = processNames;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isChromiumBased() {
        return chromiumBased;
    }

    /**
     * @param normalizedProcessName executable name, lower-case, without ".exe", spaces replaced by '-'
     */
    boolean matchesProcess(String normalizedProcessName) {
        return processNames.contains(normalizedProcessName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
