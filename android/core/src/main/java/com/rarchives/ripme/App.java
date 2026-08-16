package com.rarchives.ripme;

/**
 * Stand-in for the desktop {@code com.rarchives.ripme.App} (excluded from :core's synced source
 * copy in build.gradle.kts - it's the Swing/CLI entry point: {@code main()}, commons-cli option
 * parsing, history load/save on exit).
 *
 * <p>The shared ripper code reaches into {@code App} for exactly one field: {@code AbstractRipper}
 * reads {@code stringToAppendToFoldername} to support the desktop {@code -a}/{@code
 * --append-to-folder} command-line flag (see {@code AbstractRipper.java:546-547}). Android has no
 * such flag, so this shim just holds the field at its default (never rebound by anything in the
 * Android app), which makes that code path a permanent no-op.
 */
public class App {
    public static String stringToAppendToFoldername = null;
}
