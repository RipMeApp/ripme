package com.rarchives.ripme.ui.compose.config

import com.rarchives.ripme.ui.compose.queue.QueueController

/**
 * Configuration panel component: exposes the shared [ConfigController] for [ConfigScreen], plus
 * [QueueController] so the "download URL list" file picker can feed parsed URLs straight into
 * the queue (mirrors MainWindow.addUrlToQueue).
 */
class ConfigComponent(
    val configController: ConfigController,
    val queueController: QueueController,
)
