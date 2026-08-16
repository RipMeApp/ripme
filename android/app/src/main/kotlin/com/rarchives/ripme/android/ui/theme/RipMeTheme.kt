package com.rarchives.ripme.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * App-wide Material3 theme - fixes one of the two Phase B UI defects the plan called out.
 * MainActivity's previous `MaterialTheme { ... }` call supplied no `colorScheme` argument, which
 * defaults to Material3's baseline **light** scheme regardless of the device setting, so dark mode
 * had no effect on the Compose content at all (see also AndroidManifest.xml's `Theme.RipMe`, the
 * matching fix for the brief pre-Compose frame at activity creation). This wraps that same call
 * site with an explicit light/dark choice driven by [isSystemInDarkTheme] instead.
 *
 * Deliberately not using dynamic color (`dynamicLightColorScheme`/`dynamicDarkColorScheme`,
 * Android 12+ wallpaper-derived Material You palettes): that would make the app's colour identity
 * depend on the test device's wallpaper, which is more variance than a v1/preview app needs.
 *
 * The log/status colours emitted by the ported engine (RipController, QueueController) are a
 * separate concern from this colour *scheme* - see [legibleOn] in LogColors.kt for why those need
 * their own dark-mode adjustment on top of this theme switch.
 */
@Composable
fun RipMeTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
