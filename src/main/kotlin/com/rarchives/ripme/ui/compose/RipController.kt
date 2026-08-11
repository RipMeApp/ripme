package com.rarchives.ripme.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.rarchives.ripme.ripper.AbstractRipper
import com.rarchives.ripme.ui.RipStatusComplete
import com.rarchives.ripme.ui.RipStatusHandler
import com.rarchives.ripme.ui.RipStatusMessage
import com.rarchives.ripme.utils.Utils
import java.net.URI
import java.net.URL
import javax.swing.SwingUtilities

/** One line of the log pane, with the color it should render in. */
data class LogLine(val text: String, val color: Color)

private const val MAX_LOG_LINES = 2000

/** Terminal outcome of a single rip, handed to [RipController.onFinished] (plan §4). */
sealed interface RipOutcome {
    /** Successful rip. [ripper] is still valid at this point (for URL/title lookups). */
    data class Complete(val ripper: AbstractRipper, val rsc: RipStatusComplete) : RipOutcome
    data class Errored(val message: String) : RipOutcome
    data class NoAlbumOrUser(val message: String) : RipOutcome
}

/**
 * State/threading bridge between the Compose UI and the existing Java ripper business logic
 * (AbstractRipper + friends). Mirrors the proven logic in MainWindow (handleEvent/update/
 * ripAlbum). Drives exactly one URL at a time - queueing/draining and history writes are owned
 * by [com.rarchives.ripme.ui.compose.queue.QueueController], which treats [beginRip] as the tail
 * of its drain loop rather than a direct UI entry point (see plan §4).
 */
class RipController : RipStatusHandler {

    var status by mutableStateOf("Inactive")
        private set
    var statusColor by mutableStateOf(Color.Black)
        private set
    var progress by mutableStateOf(0f)
        private set
    var busy by mutableStateOf(false)
        private set

    val log = mutableStateListOf<LogLine>()

    private var ripper: AbstractRipper? = null

    /** The URL currently being ripped, if any - used by QueueController to re-enqueue on stop/panic. */
    val currentUrl: URL?
        get() = ripper?.url

    /** Fired exactly once per rip, when it terminally ends (success or failure). */
    var onFinished: ((RipOutcome) -> Unit)? = null

    /**
     * Normalizes, resolves a ripper for, and starts ripping [urlText] on a background thread.
     * Returns true if a rip thread was actually launched (mirrors MainWindow.ripAlbum returning
     * a non-null Thread); false means the caller (QueueController) should treat this URL as
     * un-rippable and move on rather than retry it forever. No-op (returns false) while busy.
     */
    fun beginRip(urlText: String): Boolean {
        if (busy) {
            return false
        }

        var normalized = urlText.trim()
        if (normalized.isEmpty()) {
            return false
        }
        // Mirrors MainWindow.ripAlbum's gonewild: shorthand rewrite.
        if (normalized.lowercase().startsWith("gonewild:")) {
            normalized = "http://gonewild.com/user/" + normalized.substring(normalized.indexOf(':') + 1)
        }
        if (!normalized.startsWith("http")) {
            normalized = "http://$normalized"
        }

        val url: URL
        try {
            url = URI(normalized).toURL()
        } catch (e: Exception) {
            appendLog("Can't rip this URL: ${e.message}", Color.Red)
            setStatus("Can't rip this URL: ${e.message}", Color.Red)
            return false
        }

        val newRipper: AbstractRipper
        try {
            newRipper = AbstractRipper.getRipper(url)
            newRipper.setup()
        } catch (e: Exception) {
            appendLog("Can't find ripper for this URL", Color.Red)
            setStatus("Can't find ripper for this URL", Color.Red)
            return false
        }

        ripper = newRipper
        newRipper.setObserver(this)
        busy = true
        progress = 0f
        setStatus("Starting rip...", Color.Black)

        Thread(newRipper).start()
        return true
    }

    /**
     * Appends a log line from outside a rip's own event stream (e.g. QueueController's
     * "already in queue" / "can't find ripper" messages) - keeps the log a single source of
     * truth even though QueueController owns the surrounding queue logic.
     */
    fun postLog(text: String, color: Color) = appendLog(text, color)

    /** Sets the status line from outside a rip's own event stream (see [postLog]). */
    fun postStatus(text: String, color: Color) = setStatus(text, color)

    /** Gracefully stop the in-progress rip, if any. */
    fun stop() {
        ripper?.stop()
    }

    /** Immediately abort the in-progress rip, if any. */
    fun panic() {
        ripper?.let {
            it.stop()
            it.panic()
        }
    }

    override fun update(r: AbstractRipper, message: RipStatusMessage) {
        SwingUtilities.invokeLater {
            handle(r, message)
        }
    }

    private fun handle(r: AbstractRipper, message: RipStatusMessage) {
        // CHUNK_BYTES is noisy (fired per network chunk) - skip, no transfer-rate label in this pass.
        if (message.status == RipStatusMessage.STATUS.CHUNK_BYTES) {
            return
        }

        progress = r.completionPercentage / 100f
        // Mirrors MainWindow.handleEvent: status() always resets to black first;
        // the branches below override to red for the error cases.
        setStatus(r.statusText, Color.Black)

        when (message.status) {
            RipStatusMessage.STATUS.LOADING_RESOURCE,
            RipStatusMessage.STATUS.DOWNLOAD_STARTED ->
                appendLog("Downloading " + message.`object`, Color.Black)

            RipStatusMessage.STATUS.DOWNLOAD_COMPLETE,
            RipStatusMessage.STATUS.DOWNLOAD_COMPLETE_HISTORY ->
                appendLog(message.`object`.toString(), Color(0xFF008000))

            RipStatusMessage.STATUS.DOWNLOAD_ERRORED ->
                appendLog(message.`object`.toString(), Color.Red)

            RipStatusMessage.STATUS.DOWNLOAD_WARN ->
                appendLog(message.`object`.toString(), Color(0xFFFFA500))

            RipStatusMessage.STATUS.DOWNLOAD_SKIP ->
                appendLog(message.`object`.toString(), Color(0xFFCCCC00))

            RipStatusMessage.STATUS.RIP_ERRORED -> {
                val text = message.`object`.toString()
                appendLog(text, Color.Red)
                setStatus("Error: $text", Color.Red)
                busy = false
                ripper = null
                onFinished?.invoke(RipOutcome.Errored(text))
            }

            RipStatusMessage.STATUS.NO_ALBUM_OR_USER -> {
                val text = message.`object`.toString()
                appendLog(text, Color.Red)
                setStatus("Error: $text", Color.Red)
                busy = false
                ripper = null
                onFinished?.invoke(RipOutcome.NoAlbumOrUser(text))
            }

            RipStatusMessage.STATUS.RIP_COMPLETE -> {
                val rsc = message.`object` as RipStatusComplete
                appendLog("Rip complete, saved to " + rsc.dir, Color(0xFF008000))
                if (Utils.getConfigBoolean("play.sound", false)) {
                    Utils.playSound("camera.wav")
                }
                setStatus("Rip complete, saved to " + rsc.dir, Color(0xFF008000))
                busy = false
                val finishedRipper = r
                ripper = null
                onFinished?.invoke(RipOutcome.Complete(finishedRipper, rsc))
            }

            RipStatusMessage.STATUS.TOTAL_BYTES, RipStatusMessage.STATUS.COMPLETED_BYTES -> {
                // no-op, see plan - no byte-count UI in this pass.
            }

            else -> {
                // CHUNK_BYTES already handled above.
            }
        }
    }

    private fun setStatus(text: String, color: Color) {
        status = text
        statusColor = color
    }

    private fun appendLog(text: String, color: Color) {
        log.add(LogLine(text, color))
        if (log.size > MAX_LOG_LINES) {
            log.removeAt(0)
        }
    }
}
