package com.rarchives.ripme.ui.compose.history

import com.rarchives.ripme.ui.compose.queue.QueueController

/** History panel component: exposes the shared [HistoryStore] + [QueueController] (for re-rip). */
class HistoryComponent(
    val historyStore: HistoryStore,
    val queueController: QueueController,
)
