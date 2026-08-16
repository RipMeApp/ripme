package com.rarchives.ripme.android.ui.rip

import androidx.lifecycle.ViewModel
import com.rarchives.ripme.android.engine.RipEngine

/**
 * Thin ViewModel wrapping the process-level [RipEngine] singleton for [RipScreen] - exists so the
 * screen depends on a small, screen-scoped surface rather than reaching into the global singleton
 * directly (and so it has something to collect via `viewModel()`/`collectAsStateWithLifecycle()`,
 * the idiomatic Compose-on-Android pairing). [RipEngine], not this class, is the actual source of
 * truth: a ViewModel's lifecycle is per-Activity, RipEngine's is per-process, so recreating this
 * wrapper across rotation (or navigating away and back) loses nothing.
 */
class RipViewModel : ViewModel() {
    val statusFlow = RipEngine.queueController.statusFlow
    val statusColorFlow = RipEngine.queueController.statusColorFlow
    val progressFlow = RipEngine.queueController.progressFlow
    val busyFlow = RipEngine.queueController.busyFlow
    val lastCompletedRipFlow = RipEngine.queueController.lastCompletedRipFlow

    fun rip(url: String) = RipEngine.queueController.enqueue(url)
    fun stop() = RipEngine.queueController.stop()
    fun panic() = RipEngine.queueController.panic()
    fun clearLastCompletedRip() = RipEngine.queueController.clearLastCompletedRip()
}
