package com.rarchives.ripme.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rarchives.ripme.android.engine.config.ConfigController

/**
 * Android port of the phone-relevant slice of the desktop's `ui.compose.config.ConfigScreen` - see
 * that file (src/main/kotlin/com/rarchives/ripme/ui/compose/config/ConfigScreen.kt) and
 * [ConfigController]'s header comment for exactly which keys are in scope here and why the rest
 * are dropped (plan: "phone-relevant keys only").
 *
 * Desktop's dense two-column GridBagLayout-style grid (compact `BasicTextField`s, checkboxes) is
 * replaced with a single column of full-width rows (label left, control end-aligned) - Android's
 * own idiomatic Settings-list layout, better suited to a narrow phone viewport, using Material3's
 * own [OutlinedTextField]/[Switch] rather than the desktop's hand-styled compact equivalents
 * (matches RipScreen's URL field, which already made the same call).
 */
@Composable
fun SettingsScreen() {
    val config = remember { ConfigController() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Downloads", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        NumberSettingRow("Max download threads", config.threadsText, config::onThreadsTextChanged)
        NumberSettingRow("Timeout (ms)", config.timeoutText, config::onTimeoutTextChanged)
        NumberSettingRow("Retry count", config.retriesText, config::onRetriesTextChanged)
        SwitchSettingRow("Overwrite existing files", config.fileOverwrite, config::onFileOverwriteChanged)
        SwitchSettingRow("Preserve download order", config.saveOrder, config::onSaveOrderChanged)
        SwitchSettingRow("Save album titles", config.saveAlbumTitles, config::onSaveAlbumTitlesChanged)
        SwitchSettingRow("Save descriptions", config.saveDescriptions, config::onSaveDescriptionsChanged)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Rip Files Location", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        // Read-only: no SAF folder picker in this build (plan's "out of scope"), shown so users
        // can find their files (and, via a file manager, whatever a rip's "Export to Downloads"
        // action didn't already copy out to MediaStore - see HistoryScreen/RipScreen).
        Text(
            text = config.ripsDirLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NumberSettingRow(label: String, value: String, onChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp),
        )
    }
}

@Composable
private fun SwitchSettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
