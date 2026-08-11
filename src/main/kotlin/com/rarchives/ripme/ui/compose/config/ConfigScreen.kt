package com.rarchives.ripme.ui.compose.config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rarchives.ripme.ui.compose.AwtInterop
import com.rarchives.ripme.ui.compose.tr
import com.rarchives.ripme.utils.Utils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import org.apache.logging.log4j.LogManager

private val LOGGER = LogManager.getLogger("ConfigScreen")

/**
 * Every setting MainWindow's configuration panel exposes (plan §6): threads/timeout/retries/
 * retry-sleep numeric fields; the full set of behavior checkboxes; log-level + language combos
 * (feeding [com.rarchives.ripme.ui.compose.LocalStrings.refresh] so `tr(...)` call sites
 * relocalize live); save-dir picker + click-to-open; URL-list file picker; cookies button.
 */
@Composable
fun ConfigScreen(component: ConfigComponent) {
    val config = component.configController
    val queueController = component.queueController
    var showCookieDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(config.updateStatus)
            Button(onClick = {
                config.checkForUpdates { message ->
                    queueController.postLog(message, androidx.compose.ui.graphics.Color.Black)
                }
            }) { Text(tr("check.for.updates")) }
        }

        LabeledCheckbox(tr("auto.update"), config.autoUpdate, config::onAutoUpdateChanged)
        LogLevelDropdown(config)
        LabeledTextField(tr("max.download.threads"), config.threadsText, config::onThreadsTextChanged)
        LabeledTextField(tr("timeout.mill"), config.timeoutText, config::onTimeoutTextChanged)
        LabeledTextField(tr("retry.download.count"), config.retriesText, config::onRetriesTextChanged)
        LabeledTextField(tr("retry.sleep.mill"), config.retrySleepText, config::onRetrySleepTextChanged)

        LabeledCheckbox(tr("overwrite.existing.files"), config.fileOverwrite, config::onFileOverwriteChanged)
        LabeledCheckbox(tr("preserve.order"), config.saveOrder, config::onSaveOrderChanged)
        LabeledCheckbox(tr("sound.when.rip.completes"), config.playSound, config::onPlaySoundChanged)
        LabeledCheckbox(tr("save.logs"), config.saveLogs, config::onSaveLogsChanged)
        LabeledCheckbox(tr("notification.when.rip.starts"), config.showPopup, config::onShowPopupChanged)
        LabeledCheckbox(tr("save.urls.only"), config.saveUrlsOnly, config::onSaveUrlsOnlyChanged)
        LabeledCheckbox(tr("autorip.from.clipboard"), config.clipboardAutorip, config::onClipboardAutoripChanged)
        LabeledCheckbox(tr("save.album.titles"), config.saveAlbumTitles, config::onSaveAlbumTitlesChanged)
        LabeledCheckbox(tr("save.descriptions"), config.saveDescriptions, config::onSaveDescriptionsChanged)
        LabeledCheckbox(tr("prefer.mp4.over.gif"), config.preferMp4, config::onPreferMp4Changed)
        LabeledCheckbox(tr("restore.window.position"), config.windowPosition, config::onWindowPositionChanged)
        LabeledCheckbox(tr("remember.url.history"), config.rememberUrlHistory, config::onRememberUrlHistoryChanged)
        LabeledCheckbox(tr("ssl.verify.off"), config.sslVerifyOff, config::onSslVerifyOffChanged)

        LanguageDropdown(config)

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = config.saveDirLabel,
                modifier = Modifier.clickable { AwtInterop.openInFileManager(Utils.getWorkingDirectory().toFile()) },
            )
            Button(onClick = {
                AwtInterop.chooseDirectory(Utils.getWorkingDirectory().toString()) { dir ->
                    config.setSaveDir(dir.path)
                }
            }) { Text(tr("select.save.dir") + "...") }
        }

        Button(onClick = {
            AwtInterop.chooseFile(Utils.getWorkingDirectory().toAbsolutePath().toString()) { file ->
                importUrlList(file, queueController)
            }
        }) { Text(tr("download.url.list")) }

        Button(onClick = { showCookieDialog = true }) { Text(tr("cookies.configure")) }
    }

    if (showCookieDialog) {
        CookieDialog(onClose = { showCookieDialog = false })
    }
}

private fun importUrlList(file: File, queueController: com.rarchives.ripme.ui.compose.queue.QueueController) {
    try {
        BufferedReader(FileReader(file)).use { br ->
            var line = br.readLine()
            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.startsWith("http")) {
                    queueController.enqueue(trimmed)
                } else if (trimmed.isNotEmpty()) {
                    LOGGER.error("Skipping url $trimmed because it looks malformed (doesn't start with http)")
                }
                line = br.readLine()
            }
        }
    } catch (e: Exception) {
        LOGGER.error("Error reading file ${e.message}")
    }
}

@Composable
private fun LabeledCheckbox(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label)
    }
}

@Composable
private fun LabeledTextField(label: String, value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(220.dp))
        OutlinedTextField(value = value, onValueChange = onChange, singleLine = true, modifier = Modifier.width(120.dp))
    }
}

@Composable
private fun LogLevelDropdown(config: ConfigController) {
    val options = listOf("Log level: Error", "Log level: Warn", "Log level: Info", "Log level: Debug")
    Dropdown(label = "Log level", current = config.logLevel, options = options, onSelected = config::onLogLevelChanged)
}

@Composable
private fun LanguageDropdown(config: ConfigController) {
    val options = remember { Utils.getSupportedLanguages().toList() }
    Dropdown(label = "Language", current = config.language, options = options, onSelected = config::onLanguageChanged)
}

@Composable
private fun Dropdown(label: String, current: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(120.dp))
        Button(onClick = { expanded = true }) { Text(current) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    expanded = false
                    onSelected(option)
                })
            }
        }
    }
}

