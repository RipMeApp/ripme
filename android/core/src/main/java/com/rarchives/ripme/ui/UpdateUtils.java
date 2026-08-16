package com.rarchives.ripme.ui;

/**
 * Stand-in for the desktop {@code com.rarchives.ripme.ui.UpdateUtils} (excluded from :core's
 * synced source copy in build.gradle.kts - it drives Swing "check for update" dialogs and
 * downloads/relaunches a new jar, none of which applies to an app installed and updated through
 * normal Android channels).
 *
 * <p>The shared ripper code reaches into {@code UpdateUtils} for exactly one static method:
 * {@code RedditRipper} calls {@code getThisJarVersion()} to build its User-Agent header (see
 * {@code RedditRipper.java:71}). The desktop version reads
 * {@code UpdateUtils.class.getPackage().getImplementationVersion()}, which relies on a jar
 * manifest attribute that has no equivalent for a dex'd Android app. This shim instead exposes a
 * settable version string that the Android app installs during startup (from its own
 * BuildConfig/PackageInfo), falling back to a fixed placeholder so callers - including plain JVM
 * unit tests that never run that bootstrap - still get a well-formed, non-null version string.
 */
public class UpdateUtils {

    // Mirrors the intent of the desktop class's DEFAULT_VERSION fallback (UpdateUtils.java:32)
    // without depending on its exact value: this string is never compared against a real
    // release, only concatenated into a User-Agent header.
    private static final String DEFAULT_VERSION = "0.0.0-android-unset";

    private static volatile String jarVersion = DEFAULT_VERSION;

    /** Installed by the Android app's bootstrap; optional (defaults to DEFAULT_VERSION). */
    public static void setThisJarVersion(String version) {
        jarVersion = version;
    }

    public static String getThisJarVersion() {
        return jarVersion;
    }
}
