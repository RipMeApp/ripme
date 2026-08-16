package com.rarchives.ripme.ui;

import java.util.function.Consumer;

/**
 * Stand-in for the desktop {@code com.rarchives.ripme.ui.MainWindow} (excluded from :core's
 * synced source copy in build.gradle.kts - it's the ~1000-line Swing main frame).
 *
 * <p>The shared ripper code reaches into {@code MainWindow} for exactly one static call:
 * {@code AbstractHTMLRipper.rip()} calls {@code addUrlToQueue(String)} when a page turns out to
 * be a list of albums rather than a list of images (the {@code hasQueueSupport()} path - see
 * {@code AbstractHTMLRipper.java:122}). On the desktop that appends the URL to the Swing queue
 * table. This shim replaces that direct Swing dependency with a settable callback that the
 * Android app installs during startup, so those ripper-discovered URLs reach its own queue
 * instead. Until installed, {@code addUrlToQueue} is a no-op rather than a crash, so plain unit
 * tests that exercise ripper code without an Android bootstrap still work.
 */
public class MainWindow {

    private static volatile Consumer<String> queueUrlListener;

    /** Installed by the Android app's bootstrap; optional (defaults to a no-op). */
    public static void setQueueUrlListener(Consumer<String> listener) {
        queueUrlListener = listener;
    }

    public static void addUrlToQueue(String url) {
        Consumer<String> listener = queueUrlListener;
        if (listener != null) {
            listener.accept(url);
        }
    }
}
