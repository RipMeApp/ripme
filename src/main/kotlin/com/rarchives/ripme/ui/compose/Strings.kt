package com.rarchives.ripme.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rarchives.ripme.utils.Utils

/**
 * `Utils.getLocalizedString` reads from a static [java.util.ResourceBundle] that isn't
 * Compose-observable on its own (see plan §7). [localeVersion] is a manual recomposition
 * trigger: bumping it after `Utils.setLanguage(...)` invalidates every composable that called
 * [tr], which re-reads the (now different) resource bundle - the declarative equivalent of
 * MainWindow's imperative `changeLocale()`.
 */
object LocalStrings {
    var localeVersion by mutableStateOf(0)
        private set

    fun refresh() {
        localeVersion++
    }
}

/** Localized string lookup that recomposes automatically when the language changes. */
@Composable
fun tr(key: String): String {
    @Suppress("UNUSED_EXPRESSION")
    LocalStrings.localeVersion
    return Utils.getLocalizedString(key)
}
