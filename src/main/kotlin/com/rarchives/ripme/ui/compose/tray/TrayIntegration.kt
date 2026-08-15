package com.rarchives.ripme.ui.compose.tray

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberDialogState
import com.rarchives.ripme.ui.UpdateUtils
import com.rarchives.ripme.ui.compose.AwtInterop
import com.rarchives.ripme.ui.compose.config.ConfigController
import com.rarchives.ripme.ui.compose.tr
import com.rarchives.ripme.utils.Utils
import javax.imageio.ImageIO

/**
 * System tray icon + menu (Hide/Show, About, autorip checkbox, Exit), using Compose Desktop's
 * `Tray` API rather than raw AWT `SystemTray` (plan §8) - it covers every dynamic need here
 * (label/checkbox bound to Compose state) without hand-rolled AWT menu wiring. All tray code is
 * isolated in this one file so a raw-AWT fallback (`java.awt.SystemTray`/`TrayIcon`, identical to
 * MainWindow.setupTrayIcon) would be a contained swap if Compose's Tray proves unreliable on a
 * given platform - not needed in this pass since the smoke-launch environment has no real
 * display to visually confirm the icon against (see final report).
 */
@Composable
fun ApplicationScope.RipmeTray(
    windowVisible: Boolean,
    onToggleVisible: () -> Unit,
    configController: ConfigController,
    onExit: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }

    // Loads icon.png (src/main/resources/icon.png) straight off the Java classpath via ImageIO -
    // the same mechanism MainWindow.setupTrayIcon already uses for the Swing tray icon - rather
    // than the deprecated painterResource(String), which expects the Compose resources library's
    // own generated resource layout (commonMain/composeResources), not a plain classpath resource.
    val painter = remember {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream("icon.png")
        BitmapPainter(ImageIO.read(stream).toComposeImageBitmap())
    }

    // Computed here (composable context, so tr() and configController's state are both readable)
    // and closed over by the plain (non-composable) MenuScope builder lambda below - Tray
    // recomposes whenever these change, which re-creates the menu with fresh labels/state.
    val hideShowLabel = tr(if (windowVisible) "tray.hide" else "tray.show")
    val aboutLabel = "About RipMe v" + UpdateUtils.getThisJarVersion()
    val autoripLabel = tr("tray.autorip")
    val exitLabel = tr("tray.exit")
    val autoripChecked = configController.clipboardAutorip

    Tray(
        icon = painter,
        tooltip = "RipMe v" + UpdateUtils.getThisJarVersion(),
        onAction = onToggleVisible,
        menu = {
            Item(hideShowLabel, onClick = onToggleVisible)
            Item(aboutLabel, onClick = { showAbout = true })
            Separator()
            CheckboxItem(autoripLabel, checked = autoripChecked, onCheckedChange = configController::onClipboardAutoripChanged)
            Separator()
            Item(exitLabel, onClick = onExit)
        },
    )

    if (showAbout) {
        AboutDialog(onClose = { showAbout = false })
    }
}

@Composable
private fun AboutDialog(onClose: () -> Unit) {
    val albumRippers = remember { runCatching { Utils.getListOfAlbumRippers() }.getOrDefault(emptyList()) }
    val videoRippers = remember { runCatching { Utils.getListOfVideoRippers() }.getOrDefault(emptyList()) }

    DialogWindow(
        onCloseRequest = onClose,
        title = "About RipMe",
        state = rememberDialogState(width = 420.dp, height = 420.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Download albums and videos from various websites", modifier = Modifier.fillMaxWidth())
            LazyColumn(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                item { Text("Download albums from various websites:") }
                items(albumRippers) { Text("- " + shortRipperName(it)) }
                item { Text("") }
                item { Text("Download videos from video sites:") }
                items(videoRippers) { Text("- " + shortRipperName(it)) }
            }
            Text("Do you want to visit the project homepage on GitHub?")
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { AwtInterop.browse("http://github.com/ripmeapp/ripme") }) { Text("Yes") }
                Button(onClick = onClose) { Text("No") }
            }
        }
    }
}

private fun shortRipperName(fqcn: String): String {
    var name = fqcn.substring(fqcn.lastIndexOf('.') + 1)
    if (name.contains("Ripper")) {
        name = name.substring(0, name.indexOf("Ripper"))
    }
    return name
}
