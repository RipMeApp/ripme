package com.rarchives.ripme.ui.compose.history

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.rarchives.ripme.ripper.AbstractRipper
import com.rarchives.ripme.ui.History
import com.rarchives.ripme.ui.HistoryEntry
import com.rarchives.ripme.ui.RipStatusComplete
import com.rarchives.ripme.utils.RipUtils
import com.rarchives.ripme.utils.Utils
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date

private val LOGGER = LogManager.getLogger("HistoryStore")

/** Compose-friendly, immutable snapshot of one [HistoryEntry] row (mirrors History's table columns). */
data class HistoryRow(
    val url: String,
    val created: String,
    val modified: String,
    val count: Int,
    val selected: Boolean,
)

/**
 * Thin observable holder wrapping the shared Java [History] (used identically by the CLI via
 * App.java) - History/HistoryEntry are NOT made Compose-aware themselves so the on-disk format
 * stays a single source of truth (plan §5).
 */
class HistoryStore {
    private val history = History()
    val rows: SnapshotStateList<HistoryRow> = mutableStateListOf()

    private val dateFormat get() = SimpleDateFormat("yyyy/MM/dd")

    /** Port of MainWindow.loadHistory. */
    fun load() {
        val historyFile = File(Utils.getConfigDir() + "/history.json")
        history.clear()
        if (historyFile.exists()) {
            try {
                LOGGER.info("${Utils.getLocalizedString("loading.history.from")} ${historyFile.canonicalPath}")
                history.fromFile(historyFile.canonicalPath)
            } catch (e: Exception) {
                LOGGER.error("Failed to load history from file $historyFile", e)
            }
        } else {
            LOGGER.info(Utils.getLocalizedString("loading.history.from.configuration"))
            history.fromList(Utils.getConfigList("download.history"))
            if (history.toList().isEmpty()) {
                // Loaded from config, still no entries - guess rip history from the rip folder.
                try {
                    Files.list(Utils.getWorkingDirectory()).use { stream ->
                        stream.filter { Files.isDirectory(it) }.forEach { dir ->
                            val url = RipUtils.urlFromDirectoryName(dir.toString())
                            if (url != null) {
                                val entry = HistoryEntry()
                                entry.url = url
                                history.add(entry)
                            }
                        }
                    }
                } catch (e: Exception) {
                    LOGGER.warn(e.message)
                }
            }
        }
        refresh()
    }

    /** Full-invalidation snapshot copy from the underlying History (matches fireTableDataChanged). */
    fun refresh() {
        rows.clear()
        for (entry in history.toList()) {
            rows.add(
                HistoryRow(
                    url = entry.url,
                    created = dateFormat.format(entry.startDate),
                    modified = dateFormat.format(entry.modifiedDate),
                    count = entry.count,
                    selected = entry.selected,
                )
            )
        }
    }

    /** Port of MainWindow.saveHistory. */
    fun save() {
        val historyFile = Paths.get(Utils.getConfigDir() + "/history.json")
        try {
            if (!Files.exists(historyFile)) {
                Files.createDirectories(historyFile.parent)
                Files.createFile(historyFile)
            }
            history.toFile(historyFile.toString())
            Utils.setConfigList("download.history", Collections.emptyList())
        } catch (e: Exception) {
            LOGGER.error("Failed to save history to file $historyFile", e)
        }
    }

    fun setSelected(index: Int, selected: Boolean) {
        if (index !in 0 until history.toList().size) {
            return
        }
        history.get(index).selected = selected
        refresh()
    }

    fun setAllSelected(selected: Boolean) {
        for (entry in history.toList()) {
            entry.selected = selected
        }
        refresh()
    }

    /** Removes rows at [indices] (high-to-low, then save+refresh). */
    fun remove(indices: Collection<Int>) {
        indices.sortedDescending().forEach { idx ->
            if (idx in 0 until history.toList().size) {
                history.remove(idx)
            }
        }
        refresh()
        save()
    }

    fun clear() {
        Utils.clearURLHistory()
        history.clear()
        refresh()
        save()
    }

    fun isEmpty(): Boolean = history.isEmpty()

    fun selectedUrls(): List<String> = history.toList().filter { it.selected }.map { it.url }

    /**
     * Port of MainWindow.handleEvent's RIP_COMPLETE branch: update an existing entry's
     * count/modifiedDate, or create a new one, then save+refresh. Called from
     * QueueController.onRipFinished.
     */
    fun addOrUpdateOnComplete(ripper: AbstractRipper, rsc: RipStatusComplete) {
        val url = ripper.url.toExternalForm()
        if (history.containsURL(url)) {
            val entry = history.getEntryByURL(url)
            entry.count = rsc.count
            entry.modifiedDate = Date()
        } else {
            val entry = HistoryEntry()
            entry.url = url
            entry.dir = rsc.dir
            entry.count = rsc.count
            try {
                entry.title = ripper.getAlbumTitle(ripper.url)
            } catch (e: Exception) {
                LOGGER.warn(e.message)
            }
            history.add(entry)
        }
        refresh()
        save()
        Utils.saveConfig()
    }
}
