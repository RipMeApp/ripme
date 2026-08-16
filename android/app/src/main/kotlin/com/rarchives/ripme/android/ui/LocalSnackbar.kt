package com.rarchives.ripme.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The single [SnackbarHostState] backing MainActivity's one Scaffold, exposed down to whichever
 * destination is currently showing. Both HistoryScreen (per-row "Export to Downloads") and
 * RipScreen (the same export action offered right after a rip completes) need to report an export
 * result without owning a Scaffold/SnackbarHost of their own - MainActivity's is the only one in
 * the app.
 */
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalSnackbarHostState not provided - MainActivity must wrap its content with CompositionLocalProvider(LocalSnackbarHostState provides ...)")
}
