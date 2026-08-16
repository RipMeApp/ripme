package com.rarchives.ripme.android.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rarchives.ripme.android.engine.RipEngine
import com.rarchives.ripme.android.export.MediaStoreExporter
import com.rarchives.ripme.android.ui.LocalSnackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android port of the desktop's `ui.compose.history.HistoryScreen` (list, re-rip, clear) - see
 * that file (src/main/kotlin/com/rarchives/ripme/ui/compose/history/HistoryScreen.kt) - plus the
 * "Export to Downloads" action (plan §3) the desktop has no equivalent of. Reads
 * [RipEngine.historyStore]/[RipEngine.queueController] directly rather than through a per-screen
 * ViewModel, matching QueueScreen (see its header comment).
 *
 * Desktop's dense multi-column table (url/created/modified/count/checkbox, fixed pixel-width
 * columns under a `horizontalScroll`) is replaced with a vertically stacked card per row - a
 * phone's width can't fit that table without scrolling sideways just to read a date, which is a
 * bad trade for a list that's usually read top-to-bottom. The desktop file's *two* independent
 * selection mechanisms both survive the layout change unchanged, though: tapping a row toggles an
 * ephemeral highlight-select (drives "Remove Selected", not persisted), while each row's
 * [Checkbox] is the *persisted* `HistoryEntry.selected` flag ("Re-rip Checked" / "Check All" /
 * "Check None" all act on that one instead).
 */
@Composable
fun HistoryScreen() {
    val store = RipEngine.historyStore
    val queueController = RipEngine.queueController
    val rows by store.rowsFlow.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    var removalSelection by remember { mutableStateOf(setOf<Int>()) }
    var confirmClear by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(
                onClick = { store.remove(removalSelection); removalSelection = emptySet() },
                enabled = removalSelection.isNotEmpty(),
            ) { Text("Remove Selected") }
            Button(onClick = { confirmClear = true }, enabled = rows.isNotEmpty()) { Text("Clear") }
            Button(onClick = {
                if (store.isEmpty()) {
                    errorMessage = "There are no history entries to re-rip. Rip some albums first"
                    return@Button
                }
                val urls = store.selectedUrls()
                if (urls.isEmpty()) {
                    errorMessage = "No history entries are checked - tap a row's checkbox to select it"
                    return@Button
                }
                urls.forEach { queueController.enqueue(it) }
            }) { Text("Re-rip Checked") }
            Button(onClick = { store.setAllSelected(true) }) { Text("Check All") }
            Button(onClick = { store.setAllSelected(false) }) { Text("Check None") }
        }
        HorizontalDivider()

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("No rip history yet")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                itemsIndexed(rows) { index, row ->
                    val isRemovalSelected = index in removalSelection
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isRemovalSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable {
                                removalSelection = if (isRemovalSelected) removalSelection - index else removalSelection + index
                            }
                            .padding(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = row.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Checkbox(
                                checked = row.selected,
                                onCheckedChange = { checked -> store.setSelected(index, checked) },
                            )
                        }
                        Text(
                            text = "Created ${row.created}  ·  Modified ${row.modified}  ·  ${row.count} files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (row.dir.isBlank()) {
                            // See HistoryRow's header comment: HistoryEntry.dir never round-trips
                            // through history.json, so an entry that predates this app process
                            // (reloaded at the next launch) has lost track of its folder.
                            Text(
                                text = "Folder unknown for this entry (re-rip to enable export)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            OutlinedButton(onClick = {
                                val albumDir = File(row.dir)
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { MediaStoreExporter.export(context, albumDir) }
                                    val message = result.fold(
                                        onSuccess = { "Exported ${it.filesCopied} file(s) to ${it.relativePath}" },
                                        onFailure = { "Export failed: ${it.message}" },
                                    )
                                    snackbarHostState.showSnackbar(message)
                                }
                            }) { Text("Export to Downloads") }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("RipMe") },
            text = { Text("Clear all history?") },
            confirmButton = {
                TextButton(onClick = { store.clear(); confirmClear = false }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("No") }
            },
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("RipMe") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            },
        )
    }
}
