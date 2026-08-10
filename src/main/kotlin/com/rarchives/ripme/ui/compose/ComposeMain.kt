package com.rarchives.ripme.ui.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Java-callable entry point into the Compose Desktop GUI, selected via the
 * `-Dripme.gui=compose` system property (see App.main). The Swing GUI
 * (MainWindow) remains the unchanged default.
 */
object ComposeMain {
    @JvmStatic
    fun launch() {
        val controller = RipController()
        application {
            Window(onCloseRequest = ::exitApplication, title = "RipMe") {
                MainScreen(controller)
            }
        }
    }
}
