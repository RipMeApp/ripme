package com.rarchives.ripme.android.engine

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.rarchives.ripme.android.engine.history.HistoryStore
import com.rarchives.ripme.android.engine.queue.QueueController
import com.rarchives.ripme.android.service.RipService

/**
 * Process-level singleton wiring [RipController] + [QueueController] + [HistoryStore] together -
 * the Android equivalent of the desktop's `ComposeMain.launch()`, minus the window/tray state and
 * clipboard-autorip that only make sense on the desktop (see QueueController's header comment).
 *
 * Constructing this object (touching any member) is safe at any time - none of RipController's,
 * QueueController's or HistoryStore's *constructors* touch the shared engine's `Utils` class.
 * [init] is different: `historyStore.load()` does touch `Utils` (which loads rip.properties in a
 * static initializer), so [init] must run after RipMeApplication.onCreate has set the
 * `ripme.config.dir` system property. That's guaranteed by construction: Application.onCreate is
 * the only caller of [init], and Android guarantees it runs before any other component
 * (Activity/Service) in this process can touch this object first.
 */
object RipEngine {

    val ripController = RipController()
    val historyStore = HistoryStore()
    val queueController = QueueController(ripController, historyStore)

    private lateinit var appContext: Context
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext

        // Mirrors ComposeMain.launch()'s startup sequence (history, then queue).
        historyStore.load()
        queueController.loadOnStartup()

        // Android has no toggleable Log panel to auto-show on rip start the way the desktop does
        // (Log is its own always-present NavigationBar destination) - repurpose that same hook to
        // start the foreground service instead. RipService decides for itself when to stop (it
        // watches busyFlow/queueFlow directly), so starting it here is the only push half needed.
        queueController.onRipStarted = {
            ContextCompat.startForegroundService(appContext, Intent(appContext, RipService::class.java))
        }
    }

    fun enqueue(url: String) = queueController.enqueue(url)
}
