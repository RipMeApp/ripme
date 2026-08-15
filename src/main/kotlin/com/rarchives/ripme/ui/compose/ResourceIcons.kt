package com.rarchives.ripme.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import javax.imageio.ImageIO
import org.apache.logging.log4j.LogManager

private val LOGGER = LogManager.getLogger("ResourceIcons")

/**
 * Loads a PNG straight off the Java classpath (src/main/resources) via ImageIO - the same
 * mechanism MainWindow uses for its Swing JButton icons (comment.png/time.png/list.png/gear.png)
 * - mirroring the pattern tray/TrayIntegration.kt already uses for the tray icon, rather than the
 * deprecated painterResource(String), which expects Compose resources' own generated module
 * layout (commonMain/composeResources), not a plain classpath resource. Returns null (and logs a
 * warning, matching MainWindow's try/catch around its icon loading) if the resource is missing.
 */
@Composable
fun rememberResourceIcon(resourceName: String): Painter? = remember(resourceName) {
    runCatching {
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourceName)
            ?: return@runCatching null
        BitmapPainter(ImageIO.read(stream).toComposeImageBitmap())
    }.onFailure { LOGGER.warn(it.message) }.getOrNull()
}
