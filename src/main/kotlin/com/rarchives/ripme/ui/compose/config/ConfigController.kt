package com.rarchives.ripme.ui.compose.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rarchives.ripme.ui.ClipboardUtils
import com.rarchives.ripme.ui.UpdateUtils
import com.rarchives.ripme.ui.compose.LocalStrings
import com.rarchives.ripme.utils.Utils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

private val LOGGER = LogManager.getLogger("ConfigController")

/**
 * Every setting MainWindow's configuration panel exposes, read/written directly through
 * `Utils.getConfig*`/`setConfig*` - no parallel store (plan §6). Numeric fields keep their raw
 * text so an in-progress edit (e.g. "1" while typing "12") isn't clobbered by a parse failure;
 * they're only committed to config when they parse to a positive integer, mirroring
 * MainWindow.configField's DocumentListener.
 */
class ConfigController {
    // `private set`: each property has an explicit public `fun onXxxChanged(...)` mutator below
    // (which also writes through to Utils.setConfig*) - `private set` forces callers through that
    // mutator instead of assigning the property directly and skipping the config write. (Mutators
    // are named onXxxChanged rather than setXxx to avoid a JVM signature clash with the
    // property's own Kotlin-generated `setXxx` accessor.)
    var fileOverwrite by mutableStateOf(Utils.getConfigBoolean("file.overwrite", false)); private set
    var autoUpdate by mutableStateOf(Utils.getConfigBoolean("auto.update", true)); private set
    var playSound by mutableStateOf(Utils.getConfigBoolean("play.sound", false)); private set
    var showPopup by mutableStateOf(Utils.getConfigBoolean("download.show_popup", false)); private set
    var saveOrder by mutableStateOf(Utils.getConfigBoolean("download.save_order", true)); private set
    var saveLogs by mutableStateOf(Utils.getConfigBoolean("log.save", false)); private set
    var saveUrlsOnly by mutableStateOf(Utils.getConfigBoolean("urls_only.save", false)); private set
    var saveAlbumTitles by mutableStateOf(Utils.getConfigBoolean("album_titles.save", true)); private set
    var clipboardAutorip by mutableStateOf(Utils.getConfigBoolean("clipboard.autorip", false)); private set
    var saveDescriptions by mutableStateOf(Utils.getConfigBoolean("descriptions.save", true)); private set
    var preferMp4 by mutableStateOf(Utils.getConfigBoolean("prefer.mp4", false)); private set
    var windowPosition by mutableStateOf(Utils.getConfigBoolean("window.position", true)); private set
    var rememberUrlHistory by mutableStateOf(Utils.getConfigBoolean("remember.url_history", true)); private set
    var sslVerifyOff by mutableStateOf(Utils.getConfigBoolean("ssl.verify.off", false)); private set

    var threadsText by mutableStateOf(Utils.getConfigInteger("threads.size", 3).toString()); private set
    var timeoutText by mutableStateOf(Utils.getConfigInteger("download.timeout", 60000).toString()); private set
    var retriesText by mutableStateOf(Utils.getConfigInteger("download.retries", 3).toString()); private set
    var retrySleepText by mutableStateOf(Utils.getConfigInteger("download.retry.sleep", 5000).toString()); private set

    var logLevel by mutableStateOf(Utils.getConfigString("log.level", "Log level: Debug")); private set
    var language by mutableStateOf(Utils.getConfigString("lang", Utils.getSelectedLanguage())); private set

    var saveDirLabel by mutableStateOf(runCatching { Utils.shortenPath(Utils.getWorkingDirectory()) }.getOrDefault("")); private set

    var updateStatus by mutableStateOf(tr_currentVersion()); private set

    private fun tr_currentVersion(): String =
        Utils.getLocalizedString("current.version") + ": " + UpdateUtils.getThisJarVersion()

    init {
        onLogLevelChanged(logLevel)
    }

    fun onFileOverwriteChanged(value: Boolean) {
        fileOverwrite = value
        Utils.setConfigBoolean("file.overwrite", value)
    }

    fun onAutoUpdateChanged(value: Boolean) {
        autoUpdate = value
        Utils.setConfigBoolean("auto.update", value)
    }

    fun onPlaySoundChanged(value: Boolean) {
        playSound = value
        Utils.setConfigBoolean("play.sound", value)
    }

    fun onShowPopupChanged(value: Boolean) {
        showPopup = value
        Utils.setConfigBoolean("download.show_popup", value)
    }

    fun onSaveOrderChanged(value: Boolean) {
        saveOrder = value
        Utils.setConfigBoolean("download.save_order", value)
    }

    fun onSaveLogsChanged(value: Boolean) {
        saveLogs = value
        Utils.setConfigBoolean("log.save", value)
    }

    fun onSaveUrlsOnlyChanged(value: Boolean) {
        saveUrlsOnly = value
        Utils.setConfigBoolean("urls_only.save", value)
    }

    fun onSaveAlbumTitlesChanged(value: Boolean) {
        saveAlbumTitles = value
        Utils.setConfigBoolean("album_titles.save", value)
    }

    /**
     * Drives ClipboardUtils' autorip thread. Both the config-panel checkbox and the tray
     * checkbox bind to this same [clipboardAutorip] state, so they stay in sync automatically
     * without any extra wiring (see TrayIntegration).
     */
    fun onClipboardAutoripChanged(value: Boolean) {
        clipboardAutorip = value
        Utils.setConfigBoolean("clipboard.autorip", value)
        ClipboardUtils.setClipboardAutoRip(value)
        Utils.configureLogger()
    }

    fun onSaveDescriptionsChanged(value: Boolean) {
        saveDescriptions = value
        Utils.setConfigBoolean("descriptions.save", value)
    }

    fun onPreferMp4Changed(value: Boolean) {
        preferMp4 = value
        Utils.setConfigBoolean("prefer.mp4", value)
    }

    fun onWindowPositionChanged(value: Boolean) {
        windowPosition = value
        Utils.setConfigBoolean("window.position", value)
    }

    fun onRememberUrlHistoryChanged(value: Boolean) {
        rememberUrlHistory = value
        Utils.setConfigBoolean("remember.url_history", value)
    }

    fun onSslVerifyOffChanged(value: Boolean) {
        sslVerifyOff = value
        Utils.setConfigBoolean("ssl.verify.off", value)
    }

    fun onThreadsTextChanged(value: String) {
        threadsText = value
        value.toIntOrNull()?.let { if (it > 0) Utils.setConfigInteger("threads.size", it) }
    }

    fun onTimeoutTextChanged(value: String) {
        timeoutText = value
        value.toIntOrNull()?.let { if (it > 0) Utils.setConfigInteger("download.timeout", it) }
    }

    fun onRetriesTextChanged(value: String) {
        retriesText = value
        value.toIntOrNull()?.let { if (it > 0) Utils.setConfigInteger("download.retries", it) }
    }

    fun onRetrySleepTextChanged(value: String) {
        retrySleepText = value
        value.toIntOrNull()?.let { if (it > 0) Utils.setConfigInteger("download.retry.sleep", it) }
    }

    fun onLogLevelChanged(value: String) {
        logLevel = value
        Utils.setConfigString("log.level", value)
        val levelWord = value.substring(value.lastIndexOf(' ') + 1)
        val level = when (levelWord) {
            "Debug" -> Level.DEBUG
            "Info" -> Level.INFO
            "Warn" -> Level.WARN
            else -> Level.ERROR
        }
        Utils.configureLogger(level)
    }

    fun onLanguageChanged(value: String) {
        language = value
        Utils.setLanguage(value)
        Utils.setConfigString("lang", value)
        updateStatus = tr_currentVersion()
        saveDirLabel = runCatching { Utils.shortenPath(Utils.getWorkingDirectory()) }.getOrDefault(saveDirLabel)
        // Bumps LocalStrings.localeVersion so every live `tr(...)` call site recomposes against
        // the newly-selected resource bundle - the declarative equivalent of MainWindow's
        // imperative changeLocale() (plan §7).
        LocalStrings.refresh()
    }

    fun setSaveDir(path: String) {
        saveDirLabel = Utils.shortenPath(java.nio.file.Paths.get(path))
        Utils.setConfigString("rips.directory", path)
    }

    /** Runs the (headless) CLI update check on a background thread - see plan §6 deviation note:
     * MainWindow's updateProgramGUI needs a Swing JLabel; we reflect status into [updateStatus]
     * plus the shared log instead. */
    fun checkForUpdates(onLog: (String) -> Unit) {
        updateStatus = "Checking for update..."
        thread(name = "ConfigController-update") {
            try {
                UpdateUtils.updateProgramCLI()
                SwingUtilities.invokeLater {
                    updateStatus = tr_currentVersion()
                    onLog("Update check complete (see application log for details).")
                }
            } catch (e: Exception) {
                LOGGER.error("Error while checking for updates", e)
                SwingUtilities.invokeLater {
                    updateStatus = "Error while checking for updates: ${e.message}"
                    onLog("Error while checking for updates: ${e.message}")
                }
            }
        }
    }

    /** Persists every setting - called from ComposeMain's shutdown chain (plan §12). */
    fun persistAll() {
        Utils.setConfigBoolean("file.overwrite", fileOverwrite)
        Utils.setConfigInteger("threads.size", threadsText.toIntOrNull() ?: 3)
        Utils.setConfigInteger("download.retries", retriesText.toIntOrNull() ?: 3)
        Utils.setConfigInteger("download.timeout", timeoutText.toIntOrNull() ?: 60000)
        Utils.setConfigInteger("download.retry.sleep", retrySleepText.toIntOrNull() ?: 5000)
        Utils.setConfigBoolean("clipboard.autorip", ClipboardUtils.getClipboardAutoRip())
        Utils.setConfigBoolean("auto.update", autoUpdate)
        Utils.setConfigString("log.level", logLevel)
        Utils.setConfigBoolean("play.sound", playSound)
        Utils.setConfigBoolean("download.save_order", saveOrder)
        Utils.setConfigBoolean("download.show_popup", showPopup)
        Utils.setConfigBoolean("log.save", saveLogs)
        Utils.setConfigBoolean("urls_only.save", saveUrlsOnly)
        Utils.setConfigBoolean("album_titles.save", saveAlbumTitles)
        Utils.setConfigBoolean("descriptions.save", saveDescriptions)
        Utils.setConfigBoolean("prefer.mp4", preferMp4)
        Utils.setConfigBoolean("remember.url_history", rememberUrlHistory)
        Utils.setConfigBoolean("ssl.verify.off", sslVerifyOff)
        Utils.setConfigString("lang", language)
        Utils.setConfigBoolean("window.position", windowPosition)
    }
}
