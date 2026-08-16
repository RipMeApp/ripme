package com.rarchives.ripme.android

import android.app.Application
import android.os.Environment
import com.rarchives.ripme.android.engine.RipEngine
import com.rarchives.ripme.ui.MainWindow
import com.rarchives.ripme.ui.UpdateUtils
import com.rarchives.ripme.utils.Utils
import java.io.File

/**
 * Installs the Android-specific bootstrap the shared ripper engine needs, in the one place
 * guaranteed to run before any other app component (Activity/Service) touches it.
 *
 * Ordering here is load-bearing. `Utils` (src/main/java/com/rarchives/ripme/utils/Utils.java)
 * loads rip.properties in a *static initializer*, which runs the first time anything touches the
 * `Utils` class - so step 1 below must be this method's very first statement, strictly before
 * anything (including [RipEngine], whose own construction is Utils-free but whose [RipEngine.init]
 * is not) has a chance to touch `Utils`.
 */
class RipMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Redirect Utils.getConfigDir() (the sanctioned one-line patch in Utils.java) away
        // from $HOME/.config/ripme, which doesn't exist - and isn't writable - on Android.
        val configDir = File(filesDir, "config").apply { mkdirs() }
        System.setProperty("ripme.config.dir", configDir.absolutePath)

        // 2. First real touch of Utils: safe now that (1) has run.
        //  - rips.directory: getWorkingDirectory() already honors this config key (no patch
        //    needed), redirected to app-scoped external storage so downloads don't need
        //    WRITE_EXTERNAL_STORAGE/SAF; filesDir is the fallback for the rare case
        //    getExternalFilesDir returns null (no shared storage currently mounted).
        //  - urls_only.save=false keeps AbstractRipper's one java.awt.Desktop call
        //    (AbstractRipper.java:744, Desktop.getDesktop().open(...)) permanently unreachable -
        //    java.awt has no Android implementation.
        //  - play.sound=false likewise retires Utils.playSound's javax.sound.sampled use, which
        //    also has no Android implementation.
        val ripsDir = (getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir).resolve("rips")
        Utils.setConfigString("rips.directory", ripsDir.absolutePath)
        Utils.setConfigBoolean("urls_only.save", false)
        Utils.setConfigBoolean("play.sound", false)
        Utils.saveConfig()

        // 3. AbstractHTMLRipper's hasQueueSupport() path (see MainWindow.java's header comment
        // for the exact call site) reaches into this shim to hand ripper-discovered album URLs
        // back to a queue; without a listener installed, addUrlToQueue is a silent no-op.
        MainWindow.setQueueUrlListener { url -> RipEngine.enqueue(url) }

        // RedditRipper's User-Agent (RedditRipper.java:71) - see UpdateUtils.java's header
        // comment. BuildConfig.VERSION_NAME tracks defaultConfig.versionName in build.gradle.kts.
        UpdateUtils.setThisJarVersion(BuildConfig.VERSION_NAME)

        RipEngine.init(this)
    }
}
