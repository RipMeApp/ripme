package com.rarchives.ripme.ui.compose

import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.UIManager
import kotlin.concurrent.thread

private val LOGGER = LogManager.getLogger("AwtInterop")

/**
 * Swing/AWT interop helpers for the Compose GUI - file pickers and Desktop open/browse, matching
 * MainWindow's exact `JFileChooser` configuration (plan §10). All entry points here spawn a
 * background thread and marshal results back via the given callback so a modal Swing dialog can
 * never block the Compose render thread.
 */
object AwtInterop {

    /** Mirrors MainWindow's save-dir chooser (DIRECTORIES_ONLY). Calls [onChosen] on success. */
    fun chooseDirectory(startDir: String, onChosen: (File) -> Unit) {
        thread(name = "AwtInterop-chooseDirectory") {
            UIManager.put("FileChooser.useSystemExtensionHiding", false)
            val jfc = JFileChooser(startDir)
            jfc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val result = jfc.showDialog(null, "select directory")
            if (result == JFileChooser.APPROVE_OPTION) {
                val chosen = jfc.selectedFile
                if (chosen != null) {
                    onChosen(chosen)
                }
            }
        }
    }

    /** Mirrors MainWindow's URL-list file chooser (FILES_ONLY). Calls [onChosen] on success. */
    fun chooseFile(startDir: String, onChosen: (File) -> Unit) {
        thread(name = "AwtInterop-chooseFile") {
            UIManager.put("FileChooser.useSystemExtensionHiding", false)
            val jfc = JFileChooser(startDir)
            jfc.fileSelectionMode = JFileChooser.FILES_ONLY
            val result = jfc.showDialog(null, "Open")
            if (result == JFileChooser.APPROVE_OPTION) {
                val chosen = jfc.selectedFile
                if (chosen != null) {
                    onChosen(chosen)
                }
            }
        }
    }

    /** Opens [file] in the OS file manager, off the render thread. */
    fun openInFileManager(file: File) {
        thread(name = "AwtInterop-open") {
            try {
                Desktop.getDesktop().open(file)
            } catch (e: Exception) {
                LOGGER.warn(e.message)
            }
        }
    }

    /** Opens [uri] in the default browser, off the render thread. */
    fun browse(uri: String) {
        thread(name = "AwtInterop-browse") {
            try {
                Desktop.getDesktop().browse(URI.create(uri))
            } catch (e: Exception) {
                LOGGER.error("Exception while opening $uri", e)
            }
        }
    }
}
