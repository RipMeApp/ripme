package com.rarchives.ripme.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * RipController/QueueController (engine/RipController.kt, engine/queue/QueueController.kt) emit a
 * fixed 5-colour palette ported 1:1 from the desktop Compose GUI: `Color.Black` for neutral
 * status/log text, `Color.Red` for errors, and a dark green / orange / yellow for
 * success / warn / skip (see RipController.handle's `RipStatusMessage.STATUS` branches). Those
 * constants were tuned against the desktop's permanently-light background; two of them do not
 * survive [RipMeTheme]'s dark scheme unmodified (hand-computed against WCAG's relative-luminance
 * formula, target surface ~#121212, Material3's usual dark `background`/`surface` tone):
 *  - `Color.Black` *is* the neutral colour - on a dark surface that's text painted the same colour
 *    as the page underneath it (contrast ratio ~1:1 - effectively invisible).
 *  - the ported success green (`0xFF008000`) computes to ~3.7:1 against that dark surface, under
 *    WCAG AA's 4.5:1 minimum for body text.
 * Red/orange/yellow are untouched: against the same dark surface they compute to ~4.7:1 / ~10.6:1
 * / ~12.2:1 - all already clear AA.
 *
 * The engine layer can't make this adjustment itself: RipController/QueueController are plain,
 * non-`@Composable` classes with no access to `isSystemInDarkTheme()`, and are shared, StateFlow-
 * based singletons a non-Compose `RipService` also reads (see RipController's header comment) - so
 * rather than make the engine theme-aware, the two composables that render these colours
 * (RipScreen's status line, LogScreen's log lines) run every colour through this adapter at render
 * time instead, leaving the ported engine's colour semantics untouched.
 */
fun Color.legibleOn(isDark: Boolean): Color = when {
    !isDark -> this
    this == Color.Black -> Color(0xFFE3E3E3)
    this == Color(0xFF008000) -> Color(0xFF4CD964)
    else -> this
}
