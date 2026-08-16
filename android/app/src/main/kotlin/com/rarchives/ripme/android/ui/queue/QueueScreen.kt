package com.rarchives.ripme.android.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rarchives.ripme.android.engine.RipEngine

/**
 * Android port of the desktop's `ui.compose.queue.QueueScreen` (add/remove/clear) - see that file
 * (src/main/kotlin/com/rarchives/ripme/ui/compose/queue/QueueScreen.kt). Reads
 * [RipEngine.queueController] directly rather than through a per-screen ViewModel: unlike
 * RipScreen, nothing here needs `viewModel()`'s rotation-survival (this screen has no local field
 * state worth surviving beyond the transient tap-to-select set below), so there's no benefit to
 * the extra indirection layer - matches the plan the Phase B stub for this screen described.
 *
 * URLs are still added from RipScreen (there is no separate "add" field here, matching the
 * desktop: MainWindow's own queue panel has no add field either - "Rip" is always where a URL
 * enters the queue). Tapping a row toggles an ephemeral highlight-select (not persisted - this is
 * a different, transient selection from HistoryScreen's persisted per-row checkbox) that
 * "Remove selected" acts on; no drag-reorder (MainWindow doesn't support it either).
 */
@Composable
fun QueueScreen() {
    val queue by RipEngine.queueController.queueFlow.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var confirmClearAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (queue.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("Queue is empty")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                itemsIndexed(queue) { index, url ->
                    val isSelected = index in selected
                    Text(
                        text = url,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable { selected = if (isSelected) selected - index else selected + index }
                            .padding(12.dp),
                    )
                    HorizontalDivider()
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    RipEngine.queueController.removeAll(selected)
                    selected = emptySet()
                },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) { Text("Remove Selected") }
            Button(
                onClick = { confirmClearAll = true },
                enabled = queue.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) { Text("Remove All") }
        }
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("RipMe") },
            text = { Text("Are you sure you want to remove all elements from the queue?") },
            confirmButton = {
                TextButton(onClick = {
                    RipEngine.queueController.clear()
                    selected = emptySet()
                    confirmClearAll = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text("No") }
            },
        )
    }
}
