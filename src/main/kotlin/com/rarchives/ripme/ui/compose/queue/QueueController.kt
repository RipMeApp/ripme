package com.rarchives.ripme.ui.compose.queue

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.rarchives.ripme.ripper.AbstractRipper
import com.rarchives.ripme.ui.ClipboardUtils
import com.rarchives.ripme.ui.compose.LogLine
import com.rarchives.ripme.ui.compose.RipController
import com.rarchives.ripme.ui.compose.RipOutcome
import com.rarchives.ripme.ui.compose.history.HistoryStore
import com.rarchives.ripme.utils.Utils
import org.apache.logging.log4j.LogManager
import java.net.URI
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.runtime.mutableStateListOf
import javax.swing.SwingUtilities

private val LOGGER = LogManager.getLogger("QueueController")

/**
 * 1:1 port of MainWindow's queue methods (RipButtonHandler/ripNextAlbum/canRip/stop/panic),
 * owning a single [RipController] rather than duplicating its start/stop/panic logic (plan §4).
 * The Rip button enqueues; this controller drains the queue by calling [RipController.beginRip]
 * as the tail of the drain loop.
 */
class QueueController(
    private val ripController: RipController,
    private val historyStore: HistoryStore,
) {
    val queue: SnapshotStateList<String> = mutableStateListOf()

    // Delegated straight through to the owned RipController, so UI can bind to QueueController
    // alone (plan §4).
    val busy: Boolean get() = ripController.busy
    val status: String get() = ripController.status
    val statusColor: Color get() = ripController.statusColor
    val progress: Float get() = ripController.progress
    val log: List<LogLine> get() = ripController.log

    private val gracefulStop = AtomicBoolean(false)
    private val panicStop = AtomicBoolean(false)
    private val isRipperActive = AtomicBoolean(false)

    private val retryExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "QueueController-retry").apply { isDaemon = true }
    }

    /** Fired once per rip start, so the UI can auto-show the Log panel (mirrors MainWindow.ripAlbum). */
    var onRipStarted: (() -> Unit)? = null

    init {
        ripController.onFinished = { outcome -> onRipFinished(outcome) }
    }

    /** Matches MainWindow's `"queue" + (n==0 ? "" : "(n)")` label format (caller supplies the localized base text). */
    fun queueCountSuffix(): String = if (queue.isEmpty()) "" else "(${queue.size})"

    /** Port of MainWindow.canRip. */
    fun canRip(urlString: String): Boolean {
        return try {
            var urlText = urlString.trim()
            if (urlText.isEmpty()) {
                return false
            }
            if (!urlText.startsWith("http")) {
                urlText = "http://$urlText"
            }
            val url: URL = URI(urlText).toURL()
            AbstractRipper.getRipper(url)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Port of MainWindow.RipButtonHandler.actionPerformed: rejects empty/duplicate URLs, expands
     * `{start-end}` ranges (with per-URL canRip() validation), then drains.
     */
    fun enqueue(rawUrl: String) {
        val url = rawUrl.trim()
        if (url.isEmpty()) {
            return
        }
        if (queue.contains(url)) {
            ripController.postLog("This URL is already in queue: $url", Color.Red)
            ripController.postStatus("This URL is already in queue: $url", Color(0xFFFFA500))
            ripNextAlbum()
            return
        }

        if (url.contains("{") && url.contains("}")) {
            val rangeToParse = url.substring(url.indexOf('{') + 1, url.indexOf('}'))
            val parts = rangeToParse.split("-")
            try {
                val rangeStart = parts[0].trim().toInt()
                val rangeEnd = parts[1].trim().toInt()
                for (i in rangeStart..rangeEnd) {
                    val realUrl = url.replace(Regex("\\{\\S*}"), i.toString())
                    if (canRip(realUrl)) {
                        queue.add(realUrl)
                    } else {
                        ripController.postLog("Can't find ripper for $realUrl", Color.Red)
                    }
                }
            } catch (e: Exception) {
                ripController.postLog("Can't parse range in $url: ${e.message}", Color.Red)
            }
        } else if (url.contains("{")) {
            // Range opened but never closed - mirrors MainWindow silently no-op'ing this case.
            LOGGER.debug("URL contains '{' but no closing '}': {}", url)
        } else {
            queue.add(url)
        }

        ripNextAlbum()
    }

    /** Gracefully stop the in-progress rip, if any: re-enqueues the current URL at the head. */
    fun stop() {
        val url = ripController.currentUrl ?: return
        ripController.stop()
        gracefulStop.set(true)
        queue.add(0, url.toString())
        persist()
        ripController.postStatus("Rip gracefully stopping", Color.Black)
        ripController.postLog("Download interrupted", Color.Red)
    }

    /** Immediately abort the in-progress rip, if any: re-enqueues the current URL at the head. */
    fun panic() {
        val url = ripController.currentUrl ?: return
        ripController.panic()
        panicStop.set(true)
        queue.add(0, url.toString())
        persist()
        ripController.postStatus("Rip interrupted", Color.Black)
        ripController.postLog("Download interrupted", Color.Red)
    }

    /** Appends a log line - exposed for other panels (e.g. ConfigScreen's update-check result). */
    fun postLog(text: String, color: Color) = ripController.postLog(text, color)

    /** Persists the current queue contents + saves config, called on every queue mutation. */
    fun persist() {
        Utils.setConfigList("queue", ArrayList<Any>(queue))
        Utils.saveConfig()
    }

    /** Populates [queue] from the persisted config list - call once at startup. */
    fun loadOnStartup() {
        queue.clear()
        queue.addAll(Utils.getConfigList("queue"))
    }

    /** Registers this controller as ClipboardUtils' autorip dispatch target (plan §11). */
    fun installClipboardHandler() {
        ClipboardUtils.setRipHandler { url -> SwingUtilities.invokeLater { enqueue(url) } }
    }

    fun removeAt(index: Int) {
        if (index in queue.indices) {
            queue.removeAt(index)
            persist()
        }
    }

    fun removeAll(indices: Collection<Int>) {
        indices.sortedDescending().forEach { idx ->
            if (idx in queue.indices) {
                queue.removeAt(idx)
            }
        }
        persist()
    }

    fun clear() {
        queue.clear()
        persist()
    }

    private fun ripNextAlbum() {
        LOGGER.debug("ripNextAlbum called")
        if (isRipperActive.getAndSet(true)) {
            LOGGER.debug("already ripping")
            return
        }

        persist()

        val wasGracefulStop = gracefulStop.getAndSet(false)
        val wasPanicStop = panicStop.getAndSet(false)
        if (wasGracefulStop || wasPanicStop) {
            LOGGER.debug("wasGracefulStop or wasPanicStop")
            isRipperActive.set(false)
            return
        }

        if (queue.isEmpty()) {
            isRipperActive.set(false)
            return
        }

        val next = queue.removeAt(0)
        persist()

        onRipStarted?.invoke()

        LOGGER.debug("calling beginRip(\"{}\")", next)
        val started = ripController.beginRip(next)
        if (!started) {
            LOGGER.debug("beginRip() failed to launch a thread")
            isRipperActive.set(false)
            // Mirrors MainWindow's 500ms backoff before continuing to drain, but off the render
            // thread (a scheduled retry, not a blocking Thread.sleep - see plan §4 risk notes).
            retryExecutor.schedule({
                SwingUtilities.invokeLater { ripNextAlbum() }
            }, 500, TimeUnit.MILLISECONDS)
        }
    }

    private fun onRipFinished(outcome: RipOutcome) {
        if (outcome is RipOutcome.Complete) {
            historyStore.addOrUpdateOnComplete(outcome.ripper, outcome.rsc)
        }
        // Deliberate deviation from MainWindow: MainWindow's handleEvent doesn't call
        // ripNextAlbum() after NO_ALBUM_OR_USER, which stalls the queue forever if that status
        // fires mid-drain (see e.g. TumblrRipper). Draining continues here for all three terminal
        // outcomes so a bad URL doesn't wedge the rest of the queue.
        isRipperActive.set(false)
        ripNextAlbum()
    }
}
