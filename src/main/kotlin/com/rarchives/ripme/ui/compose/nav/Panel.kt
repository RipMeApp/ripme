package com.rarchives.ripme.ui.compose.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rarchives.ripme.ui.compose.config.ConfigComponent
import com.rarchives.ripme.ui.compose.history.HistoryComponent
import com.rarchives.ripme.ui.compose.log.LogComponent
import com.rarchives.ripme.ui.compose.queue.QueueComponent

/**
 * MainWindow's four mutually-exclusive panels (Log/History/Queue/Configuration), plus a real
 * "nothing shown" state (plan §3). Deliberately a plain enum + [NavController] rather than a
 * navigation library: this app's actual need is 5 mutually-exclusive panel states with no back
 * stack and no process-death state restoration, desktop-only - a `mutableStateOf<Panel>` does
 * that job with zero extra dependency and zero version-compat risk (see build.gradle.kts for the
 * full rationale; this replaces an earlier Decompose-based design that was working but was
 * deliberately swapped out as overkill for what this screen needs).
 */
enum class Panel { None, Log, History, Queue, Configuration }

/** Toggles panels: activating one hides whatever else is showing; clicking the active one hides it. */
class NavController {
    var active by mutableStateOf(Panel.None)
        private set

    fun select(target: Panel) {
        active = if (active == target) Panel.None else target
    }

    /** Shows the Log panel unless it's already active (MainWindow's auto-show-Log-on-rip-start). */
    fun showLogIfNotActive() {
        if (active != Panel.Log) {
            active = Panel.Log
        }
    }
}

/** The four panel components, constructed once and held for the app's lifetime. */
data class PanelComponents(
    val log: LogComponent,
    val history: HistoryComponent,
    val queue: QueueComponent,
    val config: ConfigComponent,
)
