package com.rarchives.ripme.ui.compose.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.rarchives.ripme.ui.compose.AwtInterop
import com.rarchives.ripme.ui.compose.tr
import com.rarchives.ripme.utils.Utils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import org.apache.logging.log4j.LogManager

private val LOGGER = LogManager.getLogger("ConfigScreen")

// Compact button chrome shared by every button in this screen - Material3's default content
// padding (16.dp horizontal / 8.dp vertical) is why the panel used to run about 2x taller than
// MainWindow's equivalent Swing JButtons.
private val CompactButtonPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)

/**
 * Every setting MainWindow's configuration panel exposes (plan §6): threads/timeout/retries/
 * retry-sleep numeric fields; the full set of behavior checkboxes; log-level + language combos
 * (feeding [com.rarchives.ripme.ui.compose.LocalStrings.refresh] so `tr(...)` call sites
 * relocalize live); save-dir picker + click-to-open; URL-list file picker; cookies button.
 *
 * Laid out as a two-column grid that mirrors MainWindow's GridBagLayout row-for-row (each
 * addItemToConfigGridBagConstraints(...) call there has a matching SettingsRow(...) call here) -
 * one setting per row stacked full-width made the panel run roughly twice as tall as the Swing
 * equivalent for no layout reason. Each column holds one component (a label, a field, a checkbox,
 * a button) rather than nesting label+field together in a single column - that's what mirrors
 * GridBagLayout's actual per-column allocation and keeps each half of the row from being squeezed.
 */
@Composable
fun ConfigScreen(component: ConfigComponent) {
    val config = component.configController
    val queueController = component.queueController
    var showCookieDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp)) {
        SettingsRow(
            left = { Text(config.updateStatus) },
            right = {
                Button(
                    contentPadding = CompactButtonPadding,
                    onClick = {
                        config.checkForUpdates { message ->
                            queueController.postLog(message, androidx.compose.ui.graphics.Color.Black)
                        }
                    },
                ) { Text(tr("check.for.updates")) }
            },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("auto.update"), config.autoUpdate, config::onAutoUpdateChanged) },
            right = { LogLevelDropdown(config) },
        )
        SettingsRow(
            left = { Text(tr("max.download.threads")) },
            right = { CompactTextField(config.threadsText, config::onThreadsTextChanged) },
        )
        SettingsRow(
            left = { Text(tr("timeout.mill")) },
            right = { CompactTextField(config.timeoutText, config::onTimeoutTextChanged) },
        )
        SettingsRow(
            left = { Text(tr("retry.download.count")) },
            right = { CompactTextField(config.retriesText, config::onRetriesTextChanged) },
        )
        SettingsRow(
            left = { Text(tr("retry.sleep.mill")) },
            right = { CompactTextField(config.retrySleepText, config::onRetrySleepTextChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("overwrite.existing.files"), config.fileOverwrite, config::onFileOverwriteChanged) },
            right = { LabeledCheckbox(tr("preserve.order"), config.saveOrder, config::onSaveOrderChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("sound.when.rip.completes"), config.playSound, config::onPlaySoundChanged) },
            right = { LabeledCheckbox(tr("save.logs"), config.saveLogs, config::onSaveLogsChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("notification.when.rip.starts"), config.showPopup, config::onShowPopupChanged) },
            right = { LabeledCheckbox(tr("save.urls.only"), config.saveUrlsOnly, config::onSaveUrlsOnlyChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("autorip.from.clipboard"), config.clipboardAutorip, config::onClipboardAutoripChanged) },
            right = { LabeledCheckbox(tr("save.album.titles"), config.saveAlbumTitles, config::onSaveAlbumTitlesChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("save.descriptions"), config.saveDescriptions, config::onSaveDescriptionsChanged) },
            right = { LabeledCheckbox(tr("prefer.mp4.over.gif"), config.preferMp4, config::onPreferMp4Changed) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("restore.window.position"), config.windowPosition, config::onWindowPositionChanged) },
            right = { LabeledCheckbox(tr("remember.url.history"), config.rememberUrlHistory, config::onRememberUrlHistoryChanged) },
        )
        SettingsRow(
            left = { LabeledCheckbox(tr("ssl.verify.off"), config.sslVerifyOff, config::onSslVerifyOffChanged) },
        )
        SettingsRow(
            left = { LanguageDropdown(config) },
            right = {
                Button(
                    contentPadding = CompactButtonPadding,
                    onClick = {
                        AwtInterop.chooseFile(Utils.getWorkingDirectory().toAbsolutePath().toString()) { file ->
                            importUrlList(file, queueController)
                        }
                    },
                ) { Text(tr("download.url.list")) }
            },
        )
        SettingsRow(
            left = {
                Text(
                    text = config.saveDirLabel,
                    modifier = Modifier.clickable { AwtInterop.openInFileManager(Utils.getWorkingDirectory().toFile()) },
                )
            },
            right = {
                Button(
                    contentPadding = CompactButtonPadding,
                    onClick = {
                        AwtInterop.chooseDirectory(Utils.getWorkingDirectory().toString()) { dir ->
                            config.setSaveDir(dir.path)
                        }
                    },
                ) { Text(tr("select.save.dir") + "...") }
            },
        )
        SettingsRow(
            left = { Text(tr("cookies.configure")) },
            right = {
                Button(contentPadding = CompactButtonPadding, onClick = { showCookieDialog = true }) {
                    Text(tr("cookies.configure"))
                }
            },
        )
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

/**
 * One line of the settings grid, split into two equal-weight columns - mirrors a single
 * GridBagLayout row in MainWindow (gridx 0/1). [right] is omitted for rows that only need one
 * column (e.g. MainWindow's ssl.verify.off row, which duplicates its own component into both
 * cells - not worth replicating here).
 */
@Composable
private fun SettingsRow(left: @Composable () -> Unit, right: (@Composable () -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) { left() }
        Box(modifier = Modifier.weight(1f)) { right?.invoke() }
    }
}

@Composable
private fun LabeledCheckbox(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange, modifier = Modifier.size(32.dp))
        Text(label)
    }
}

/**
 * A single-line text box sized like Swing's JTextField (just tall enough for one line of text +
 * a thin border) instead of Material3's OutlinedTextField, whose ~56.dp default height (floating
 * label + generous padding) is what made the numeric fields look oversized next to MainWindow's
 * compact fields.
 */
@Composable
private fun CompactTextField(value: String, onChange: (String) -> Unit) {
    val textColor = MaterialTheme.colorScheme.onSurface
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = textColor),
        cursorBrush = SolidColor(textColor),
        modifier = Modifier
            .width(80.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun LogLevelDropdown(config: ConfigController) {
    val options = listOf("Log level: Error", "Log level: Warn", "Log level: Info", "Log level: Debug")
    Dropdown(current = config.logLevel, options = options, onSelected = config::onLogLevelChanged)
}

@Composable
private fun LanguageDropdown(config: ConfigController) {
    val options = remember { Utils.getSupportedLanguages().toList() }
    Dropdown(current = config.language, options = options, onSelected = config::onLanguageChanged)
}

@Composable
private fun Dropdown(current: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(contentPadding = CompactButtonPadding, onClick = { expanded = true }) { Text(current) }
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
