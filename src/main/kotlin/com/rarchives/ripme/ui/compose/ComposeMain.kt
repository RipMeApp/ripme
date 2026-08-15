package com.rarchives.ripme.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rarchives.ripme.ui.ClipboardUtils
import com.rarchives.ripme.ui.UpdateUtils
import com.rarchives.ripme.ui.compose.config.ConfigComponent
import com.rarchives.ripme.ui.compose.config.ConfigController
import com.rarchives.ripme.ui.compose.history.HistoryComponent
import com.rarchives.ripme.ui.compose.history.HistoryStore
import com.rarchives.ripme.ui.compose.log.LogComponent
import com.rarchives.ripme.ui.compose.nav.NavController
import com.rarchives.ripme.ui.compose.nav.PanelComponents
import com.rarchives.ripme.ui.compose.queue.QueueComponent
import com.rarchives.ripme.ui.compose.queue.QueueController
import com.rarchives.ripme.ui.compose.tray.RipmeTray
import com.rarchives.ripme.utils.Utils
import org.apache.logging.log4j.LogManager

private val LOGGER = LogManager.getLogger("ComposeMain")

/**
 * Java-callable entry point into the Compose Desktop GUI, selected via the
 * `-Dripme.gui=compose` system property (see App.main). The Swing GUI (MainWindow) remains the
 * unchanged default. Wires up every shared singleton exactly once (plan §12) before entering
 * `application {}` - the panel components and [NavController] are plain objects (see
 * ui/compose/nav/Panel.kt) constructed once here and held for the app's lifetime.
 */
object ComposeMain {
    @JvmStatic
    fun launch() {
        val ripController = RipController()
        val historyStore = HistoryStore()
        val configController = ConfigController()
        val queueController = QueueController(ripController, historyStore)

        val nav = NavController()
        val panels = PanelComponents(
            log = LogComponent(queueController),
            history = HistoryComponent(historyStore, queueController),
            queue = QueueComponent(queueController),
            config = ConfigComponent(configController, queueController),
        )

        // Mirrors MainWindow.ripAlbum's auto-show-Log-on-rip-start behavior (plan §3).
        queueController.onRipStarted = { nav.showLogIfNotActive() }

        // Startup sequence (plan §12).
        historyStore.load()
        queueController.loadOnStartup()
        queueController.installClipboardHandler()
        val autoripEnabled = Utils.getConfigBoolean("clipboard.autorip", false)
        ClipboardUtils.setClipboardAutoRip(autoripEnabled)

        fun shutdownPersist(windowState: WindowState?) {
            queueController.persist()
            historyStore.save()
            configController.persistAll()
            if (windowState != null) {
                saveWindowPosition(windowState, configController)
            }
            Utils.saveConfig()
        }

        application {
            var windowVisible by remember { mutableStateOf(true) }
            val initialState = remember { initialWindowState(configController) }
            val windowState = rememberWindowState(
                position = initialState.position,
                size = initialState.size,
            )

            RipmeTray(
                windowVisible = windowVisible,
                onToggleVisible = { windowVisible = !windowVisible },
                configController = configController,
                onExit = {
                    shutdownPersist(windowState)
                    exitApplication()
                },
            )

            Window(
                visible = windowVisible,
                onCloseRequest = {
                    shutdownPersist(windowState)
                    exitApplication()
                },
                state = windowState,
                title = "RipMe v" + UpdateUtils.getThisJarVersion(),
            ) {
                MainScreen(nav, queueController, panels)
            }
        }
    }

    // Java on Windows has a bug where manually setting the Window's position keeps javaw.exe
    // from exiting cleanly - MainWindow.hasWindowPositionBug, ported verbatim (plan §9).
    private fun hasWindowPositionBug(): Boolean {
        val osName = System.getProperty("os.name")
        return osName == null || osName.startsWith("Windows")
    }

    private fun isWindowPositioningEnabled(configController: ConfigController): Boolean =
        configController.windowPosition && !hasWindowPositionBug()

    private val DEFAULT_SIZE = DpSize(900.dp, 700.dp)

    private fun initialWindowState(configController: ConfigController): WindowState {
        if (!isWindowPositioningEnabled(configController)) {
            return WindowState(size = DEFAULT_SIZE)
        }
        val x = Utils.getConfigInteger("window.x", -1)
        val y = Utils.getConfigInteger("window.y", -1)
        val w = Utils.getConfigInteger("window.w", -1)
        val h = Utils.getConfigInteger("window.h", -1)
        if (x < 0 || y < 0 || w <= 0 || h <= 0) {
            return WindowState(size = DEFAULT_SIZE)
        }
        return WindowState(position = WindowPosition(x.dp, y.dp), size = DpSize(w.dp, h.dp))
    }

    private fun saveWindowPosition(windowState: WindowState, configController: ConfigController) {
        if (!isWindowPositioningEnabled(configController)) {
            return
        }
        val pos = windowState.position
        if (pos !is WindowPosition.Absolute) {
            LOGGER.debug("Window position not yet absolute, skipping save")
            return
        }
        Utils.setConfigInteger("window.x", pos.x.value.toInt())
        Utils.setConfigInteger("window.y", pos.y.value.toInt())
        Utils.setConfigInteger("window.w", windowState.size.width.value.toInt())
        Utils.setConfigInteger("window.h", windowState.size.height.value.toInt())
    }
}
