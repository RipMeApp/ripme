package com.rarchives.ripme.android.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The five always-visible NavigationBar destinations (plan's `:app` table). This is the Android
 * idiom swap for the desktop's `nav/Panel.kt`: MainWindow/MainScreen toggle four *overlay* panels
 * underneath a permanently-visible Rip form; a phone screen doesn't have room for that layout, so
 * here Rip is a destination of equal standing to the other four, chosen exactly one at a time by
 * a bottom NavigationBar - Material's standard mobile navigation pattern, and
 * MainActivity's replacement for Panel.kt's `NavController`.
 */
enum class Destination(val label: String, val icon: ImageVector) {
    Rip("Rip", Icons.Filled.PlayArrow),
    // AutoMirrored: List's glyph should flip under RTL locales; plain Icons.Filled.List is
    // deprecated in favor of this in the same material-icons-core artifact.
    Queue("Queue", Icons.AutoMirrored.Filled.List),
    // material-icons-core (the curated, BOM-pinned subset - see :app's build.gradle.kts) has no
    // dedicated "History" glyph (that one's -extended only); DateRange (a calendar) is the
    // closest available stand-in for "a log of past rips by date". Checked directly against the
    // resolved AAR (androidx.compose.material:material-icons-core-android:1.7.8's classes.jar,
    // package androidx/compose/material/icons/filled/), not assumed - it does not have a
    // HistoryKt entry, alongside several other omissions (Stop, Article/Description/Assignment,
    // Download) that ruled out other candidate icons used elsewhere in this app.
    History("History", Icons.Filled.DateRange),
    Log("Log", Icons.Filled.Info),
    Settings("Settings", Icons.Filled.Settings),
}
