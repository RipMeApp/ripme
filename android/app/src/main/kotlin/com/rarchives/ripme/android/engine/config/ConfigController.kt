package com.rarchives.ripme.android.engine.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rarchives.ripme.utils.Utils

/**
 * Android port of the phone-relevant slice of the desktop's `ui.compose.config.ConfigController`
 * (src/main/kotlin/com/rarchives/ripme/ui/compose/config/ConfigController.kt): same
 * read/write-straight-through-to-`Utils.getConfig*`/`setConfig*` design, same "keep the raw text
 * so an in-progress numeric edit isn't clobbered by a parse failure, only commit when it parses to
 * a positive integer" handling for the numeric fields. Trimmed to the keys the plan calls
 * phone-relevant: threads, timeout, retries, overwrite, album titles + save order, save
 * descriptions. Dropped versus desktop: log level (no log4j-core on this build, see :app's
 * build.gradle.kts), sound (`Utils.playSound` is unreachable here, see RipMeApplication), the
 * update checker (no in-app updates, plan's "out of scope"), language, clipboard-autorip
 * (`ClipboardUtils` is Swing/AWT-only and excluded from :core's synced source), the SSL/window-
 * position/URL-history toggles, cookies, and the URL-list file import (no SAF picker, plan's
 * "out of scope"). The rips directory is exposed but read-only, so users can find their files -
 * same reason, no folder picker in this build.
 *
 * One deliberate deviation from desktop's `ConfigController`: this calls `Utils.saveConfig()`
 * immediately from every `onXxxChanged`, rather than only batching a flush into a `persistAll()`
 * called from an orderly shutdown hook. Android has no equivalent of that hook - a backgrounded
 * app's process is routinely killed by the OS with no callback at all - so batching here would
 * mean a changed setting is only as durable as the app happening to still be alive next time
 * something else calls `Utils.saveConfig()`. This mirrors what QueueController.persist() and
 * HistoryStore already do on this app (save on every mutation, not at shutdown).
 *
 * Compose `mutableStateOf` rather than the `StateFlow` style RipController/QueueController/
 * HistoryStore use: those three are process-level singletons that a plain (non-`@Composable`)
 * `RipService` also needs to observe; config is only ever read/written from SettingsScreen, so a
 * per-composition instance (`remember { ConfigController() }`, recreated fresh from `Utils`'
 * current values whenever Settings is (re)entered) is simpler and just as correct - `Utils`' own
 * in-memory config is the actual single source of truth either way.
 */
class ConfigController {
    var fileOverwrite by mutableStateOf(Utils.getConfigBoolean("file.overwrite", false)); private set
    var saveOrder by mutableStateOf(Utils.getConfigBoolean("download.save_order", true)); private set
    var saveAlbumTitles by mutableStateOf(Utils.getConfigBoolean("album_titles.save", true)); private set
    var saveDescriptions by mutableStateOf(Utils.getConfigBoolean("descriptions.save", true)); private set

    var threadsText by mutableStateOf(Utils.getConfigInteger("threads.size", 3).toString()); private set
    var timeoutText by mutableStateOf(Utils.getConfigInteger("download.timeout", 60000).toString()); private set
    var retriesText by mutableStateOf(Utils.getConfigInteger("download.retries", 3).toString()); private set

    /** Read-only display so users can find their files - no SAF folder picker in this build. */
    val ripsDirLabel: String =
        runCatching { Utils.getWorkingDirectory().toAbsolutePath().toString() }.getOrDefault("(unavailable)")

    fun onFileOverwriteChanged(value: Boolean) {
        fileOverwrite = value
        Utils.setConfigBoolean("file.overwrite", value)
        Utils.saveConfig()
    }

    fun onSaveOrderChanged(value: Boolean) {
        saveOrder = value
        Utils.setConfigBoolean("download.save_order", value)
        Utils.saveConfig()
    }

    fun onSaveAlbumTitlesChanged(value: Boolean) {
        saveAlbumTitles = value
        Utils.setConfigBoolean("album_titles.save", value)
        Utils.saveConfig()
    }

    fun onSaveDescriptionsChanged(value: Boolean) {
        saveDescriptions = value
        Utils.setConfigBoolean("descriptions.save", value)
        Utils.saveConfig()
    }

    fun onThreadsTextChanged(value: String) {
        threadsText = value
        value.toIntOrNull()?.let {
            if (it > 0) {
                Utils.setConfigInteger("threads.size", it)
                Utils.saveConfig()
            }
        }
    }

    fun onTimeoutTextChanged(value: String) {
        timeoutText = value
        value.toIntOrNull()?.let {
            if (it > 0) {
                Utils.setConfigInteger("download.timeout", it)
                Utils.saveConfig()
            }
        }
    }

    fun onRetriesTextChanged(value: String) {
        retriesText = value
        value.toIntOrNull()?.let {
            if (it > 0) {
                Utils.setConfigInteger("download.retries", it)
                Utils.saveConfig()
            }
        }
    }
}
