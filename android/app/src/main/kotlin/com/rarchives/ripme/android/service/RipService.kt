package com.rarchives.ripme.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.rarchives.ripme.android.R
import com.rarchives.ripme.android.engine.RipEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val CHANNEL_ID = "rip_progress"
private const val NOTIFICATION_ID = 1

/** Action on the notification's "Stop" button; delivered back to this same service via onStartCommand. */
private const val ACTION_STOP = "com.rarchives.ripme.android.action.STOP"

/**
 * Foreground service ("so rips survive backgrounding") that keeps the process alive while
 * [RipEngine]'s queue is draining. It owns none of the rip logic itself - RipEngine's singleton
 * controllers do exactly the same work whether or not this service happens to be running; this
 * class only holds up Android's foreground-service contract (a visible notification + elevated
 * process priority) for as long as there's something to show, and relays the notification's Stop
 * action back into [com.rarchives.ripme.android.engine.queue.QueueController.stop].
 *
 * Lifecycle is push-to-start, pull-to-stop: [RipEngine.init] wires QueueController.onRipStarted to
 * `startForegroundService` (the push), but this service decides for itself when to shut down by
 * observing the same StateFlows the UI does (the pull) - simpler than threading a matching
 * "onQueueIdle" callback back out through QueueController, and it means the service's notion of
 * "idle" can never drift from what the Rip screen is showing.
 */
class RipService : Service() {

    // A Service has no built-in coroutine scope (that's LifecycleService, a different artifact
    // than the lifecycle-*-compose libraries this app already depends on) - own one for exactly
    // this service instance's lifetime, cancelled in onDestroy.
    private val scope = CoroutineScope(Dispatchers.Main.immediate + Job())

    private data class ObservedState(val busy: Boolean, val queueSize: Int, val status: String, val progress: Float)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()

        // startForeground has a short platform-enforced deadline after startForegroundService()
        // - post a first notification synchronously rather than waiting on the collector below
        // for its first emission (which, in practice, already reflects an in-progress rip by the
        // time Android gets around to calling onCreate - see RipEngine's header comment on the
        // ordering - but this doesn't depend on that timing).
        startForeground(NOTIFICATION_ID, buildNotification("Ripping...", 0f))

        val state = combine(
            RipEngine.queueController.busyFlow,
            RipEngine.queueController.queueFlow,
            RipEngine.queueController.statusFlow,
            RipEngine.queueController.progressFlow,
        ) { busy, queue, status, progress -> ObservedState(busy, queue.size, status, progress) }

        // Keep the notification content live for as long as there's anything to show. Unlike the
        // stop decision below, a stale, about-to-be-superseded label update is harmless, so this
        // one reacts immediately rather than through the debounce.
        state
            .onEach { s ->
                if (s.busy || s.queueSize > 0) {
                    val label = if (s.queueSize > 0) "${s.status} (${s.queueSize} queued)" else s.status
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(label, s.progress))
                }
            }
            .launchIn(scope)

        // The actual stop decision, debounced. Confirmed necessary on-device, not just a
        // theoretical race: QueueController's drain loop (ripNextAlbum) can go busy=false with an
        // empty queue for a few milliseconds *between* two rips - most visibly when one URL fails
        // to resolve (beginRip returns false instantly, before ever setting busy=true) and the
        // next queued URL starts right after. RipEngine.init's onRipStarted hook calls
        // startForegroundService for *every* rip attempt, including that instantly-failing one; an
        // un-debounced version of this stopped the freshly-created service instance (and cancelled
        // its collector in onDestroy) before the very next rip's startForegroundService call could
        // even be delivered, orphaning that second rip with no collector left to ever stop its
        // (still-showing, now permanently stale) notification. Only stopping once "nothing to do"
        // has held steady for half a second rides out that gap: a real new rip's busy=true arrives
        // well within 500ms and this simply never fires.
        state
            .map { s -> !s.busy && s.queueSize == 0 }
            .debounce(500)
            .onEach { idle -> if (idle) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } }
            .launchIn(scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            RipEngine.queueController.stop()
        }
        // Not sticky: if the system kills this process, the in-flight rip thread dies with it -
        // restarting an empty service with no rip to resume would just show a stale notification.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(NotificationManager::class.java)

    private fun createNotificationChannelIfNeeded() {
        // IMPORTANCE_LOW: an ongoing-operation notification, not an alert - no sound/heads-up,
        // per Android's own guidance for progress notifications. Channels are permanent once
        // created and re-creating with the same ID is a harmless no-op, so this is safe to call
        // on every service start rather than needing separate one-time setup in RipMeApplication.
        val channel = NotificationChannel(CHANNEL_ID, "Rip progress", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String, progress: Float): Notification {
        val stopIntent = Intent(this, RipService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            // R.mipmap.ic_launcher is a full-color icon repurposed here rather than a proper
            // white-on-transparent status-bar glyph (see plan: "reuse [icon.png] rather than
            // inventing artwork") - Android will mask it to its alpha channel on the status bar,
            // so it won't look as crisp as a dedicated small-icon asset, but it's not worth
            // drawing a second icon for a v1/preview app.
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, (progress * 100).toInt(), false)
            .addAction(0, "Stop", stopPendingIntent)
            .build()
    }
}
