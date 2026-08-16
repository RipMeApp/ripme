package com.rarchives.ripme.android.engine.queue

import android.os.Handler
import android.os.Looper
import androidx.compose.ui.graphics.Color
import com.rarchives.ripme.android.RipperRegistry
import com.rarchives.ripme.android.engine.LogLine
import com.rarchives.ripme.android.engine.RipController
import com.rarchives.ripme.android.engine.RipOutcome
import com.rarchives.ripme.android.engine.history.HistoryStore
import com.rarchives.ripme.ui.RipStatusComplete
import com.rarchives.ripme.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.logging.log4j.LogManager
import java.net.URI
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private val LOGGER = LogManager.getLogger("QueueController")

/**
 * Android port of the desktop's `ui.compose.queue.QueueController` - see that file
 * (src/main/kotlin/com/rarchives/ripme/ui/compose/queue/QueueController.kt) for the full
 * rationale. Still a 1:1 port of MainWindow's queue methods (RipButtonHandler/ripNextAlbum/
 * canRip/stop/panic); still owns a single [RipController] rather than duplicating its
 * start/stop/panic logic; still drains by calling [RipController.beginRip] as the tail of the
 * drain loop. Two differences versus the desktop file:
 *  - the queue itself is a `StateFlow<List<String>>` instead of a `SnapshotStateList`, for the
 *    same reason RipController's fields are StateFlows (see its header comment);
 *  - `installClipboardHandler` isn't ported: `ClipboardUtils` is Swing/AWT-only and excluded from
 *    :core's synced source (android/core/build.gradle.kts) - there is no clipboard-autorip
 *    equivalent in this v1.
 */
class QueueController(
    private val ripController: RipController,
    private val historyStore: HistoryStore,
) {
    private val _queue = MutableStateFlow<List<String>>(emptyList())
    val queueFlow: StateFlow<List<String>> = _queue.asStateFlow()

    // Retains the most recent RIP_COMPLETE outcome (folder + file count) so RipScreen can offer a
    // one-tap "Export to Downloads" right where the user is already looking, in addition to the
    // same action being available later from any HistoryScreen row (see MediaStoreExporter). Has
    // no desktop equivalent - the desktop build has no export action at all.
    private val _lastCompletedRip = MutableStateFlow<RipStatusComplete?>(null)
    val lastCompletedRipFlow: StateFlow<RipStatusComplete?> = _lastCompletedRip.asStateFlow()

    /** Dismisses RipScreen's "rip complete" card without affecting the History row it also wrote. */
    fun clearLastCompletedRip() {
        _lastCompletedRip.value = null
    }

    // Delegated straight through to the owned RipController, so UI/Service can bind to
    // QueueController alone.
    val busyFlow: StateFlow<Boolean> get() = ripController.busyFlow
    val statusFlow: StateFlow<String> get() = ripController.statusFlow
    val statusColorFlow: StateFlow<Color> get() = ripController.statusColorFlow
    val progressFlow: StateFlow<Float> get() = ripController.progressFlow
    val logFlow: StateFlow<List<LogLine>> get() = ripController.logFlow

    private val gracefulStop = AtomicBoolean(false)
    private val panicStop = AtomicBoolean(false)
    private val isRipperActive = AtomicBoolean(false)

    private val retryExecutor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "QueueController-retry").apply { isDaemon = true }
    }
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Fired once per rip start (mirrors MainWindow.ripAlbum). On the desktop this auto-shows the
     * Log panel; Android has no such toggleable panel (Log is its own always-present
     * NavigationBar destination), so [com.rarchives.ripme.android.engine.RipEngine] repurposes
     * this same hook to start the foreground service instead.
     */
    var onRipStarted: (() -> Unit)? = null

    init {
        ripController.onFinished = { outcome -> onRipFinished(outcome) }
    }

    /** Matches MainWindow's `"queue" + (n==0 ? "" : "(n)")` label format (caller supplies the localized base text). */
    fun queueCountSuffix(): String = if (_queue.value.isEmpty()) "" else "(${_queue.value.size})"

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
            RipperRegistry.getRipper(url)
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
        if (_queue.value.contains(url)) {
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
                        addToQueue(realUrl)
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
            addToQueue(url)
        }

        ripNextAlbum()
    }

    /** Gracefully stop the in-progress rip, if any: re-enqueues the current URL at the head. */
    fun stop() {
        val url = ripController.currentUrl ?: return
        ripController.stop()
        gracefulStop.set(true)
        addToFront(url.toString())
        persist()
        ripController.postStatus("Rip gracefully stopping", Color.Black)
        ripController.postLog("Download interrupted", Color.Red)
    }

    /** Immediately abort the in-progress rip, if any: re-enqueues the current URL at the head. */
    fun panic() {
        val url = ripController.currentUrl ?: return
        ripController.panic()
        panicStop.set(true)
        addToFront(url.toString())
        persist()
        ripController.postStatus("Rip interrupted", Color.Black)
        ripController.postLog("Download interrupted", Color.Red)
    }

    /** Appends a log line - exposed for other panels (e.g. a future update-check result). */
    fun postLog(text: String, color: Color) = ripController.postLog(text, color)

    /** Persists the current queue contents + saves config, called on every queue mutation. */
    fun persist() {
        Utils.setConfigList("queue", ArrayList<Any>(_queue.value))
        Utils.saveConfig()
    }

    /** Populates [queueFlow] from the persisted config list - call once at startup. */
    fun loadOnStartup() {
        _queue.value = Utils.getConfigList("queue")
    }

    fun removeAt(index: Int) {
        val current = _queue.value
        if (index in current.indices) {
            _queue.value = current.toMutableList().apply { removeAt(index) }
            persist()
        }
    }

    fun removeAll(indices: Collection<Int>) {
        val current = _queue.value.toMutableList()
        indices.sortedDescending().forEach { idx ->
            if (idx in current.indices) {
                current.removeAt(idx)
            }
        }
        _queue.value = current
        persist()
    }

    fun clear() {
        _queue.value = emptyList()
        persist()
    }

    private fun addToQueue(url: String) {
        _queue.value = _queue.value + url
    }

    private fun addToFront(url: String) {
        _queue.value = listOf(url) + _queue.value
    }

    private fun removeFirst(): String {
        val head = _queue.value.first()
        _queue.value = _queue.value.drop(1)
        return head
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

        if (_queue.value.isEmpty()) {
            isRipperActive.set(false)
            return
        }

        val next = removeFirst()
        persist()

        onRipStarted?.invoke()

        LOGGER.debug("calling beginRip(\"{}\")", next)
        val started = ripController.beginRip(next)
        if (!started) {
            LOGGER.debug("beginRip() failed to launch a thread")
            isRipperActive.set(false)
            // Mirrors MainWindow's 500ms backoff before continuing to drain, but off the render
            // thread (a scheduled retry, not a blocking Thread.sleep).
            retryExecutor.schedule({
                mainHandler.post { ripNextAlbum() }
            }, 500, TimeUnit.MILLISECONDS)
        }
    }

    private fun onRipFinished(outcome: RipOutcome) {
        if (outcome is RipOutcome.Complete) {
            historyStore.addOrUpdateOnComplete(outcome.ripper, outcome.rsc)
            _lastCompletedRip.value = outcome.rsc
        }
        // Deliberate deviation from MainWindow: MainWindow's handleEvent doesn't call
        // ripNextAlbum() after NO_ALBUM_OR_USER, which stalls the queue forever if that status
        // fires mid-drain (see e.g. TumblrRipper). Draining continues here for all three terminal
        // outcomes so a bad URL doesn't wedge the rest of the queue (same deviation the desktop
        // Compose GUI makes - see its QueueController.onRipFinished).
        isRipperActive.set(false)
        ripNextAlbum()
    }
}
