package com.rarchives.ripme.utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Process-wide "stop everything" signal, used to cancel the rest of the rip
 * queue (e.g. when a download fails because the disk is full).
 * <p>
 * This is intentionally a tiny, Swing-free holder rather than a field on
 * {@code MainWindow}. Code that may run with no GUI at all - such as
 * {@code DownloadFileThread} during a headless/CLI batch rip
 * ({@code -r}/{@code -R}/{@code -f}) - must be able to request a queue-wide
 * stop without touching {@code MainWindow}: merely referencing that class
 * triggers its static Swing field initializers and can throw a
 * {@code HeadlessException} when there is no display.
 */
public class RipStatus {

    private static final AtomicBoolean panicAll = new AtomicBoolean(false);

    private RipStatus() { }

    /**
     * Requests that all remaining work - the rest of the current album plus
     * anything still queued - be cancelled.
     */
    public static void requestPanicAll() {
        panicAll.set(true);
    }

    /**
     * Returns whether a queue-wide panic was requested, and resets the flag.
     * Consuming the flag (rather than just peeking at it) ensures a disk-full
     * panic from one rip session doesn't leak into the next, unrelated one.
     *
     * @return true if {@link #requestPanicAll()} was called since the last
     *         time this method was called.
     */
    public static boolean consumePanicAll() {
        return panicAll.getAndSet(false);
    }
}
