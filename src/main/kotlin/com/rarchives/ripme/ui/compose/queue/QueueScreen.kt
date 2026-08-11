package com.rarchives.ripme.ui.compose.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rarchives.ripme.ui.compose.tr

/**
 * Port of MainWindow's queue panel + QueueMenuMouseListener context actions: a scrollable list
 * of pending URLs with remove-selected / remove-all(with confirm) actions - each mutation
 * persists via [QueueController.persist] (plan §6). No drag-reorder (MainWindow doesn't support
 * it either).
 */
@Composable
fun QueueScreen(component: QueueComponent) {
    val controller = component.queueController
    val selected = remember { mutableStateOf(setOf<Int>()) }
    var confirmClearAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
            itemsIndexed(controller.queue) { index, url ->
                val isSelected = index in selected.value
                Text(
                    text = url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable {
                            selected.value = if (isSelected) selected.value - index else selected.value + index
                        }
                        .padding(4.dp),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Button(onClick = {
                controller.removeAll(selected.value)
                selected.value = emptySet()
            }, enabled = selected.value.isNotEmpty()) {
                Text(tr("queue.remove.selected"))
            }
            Button(onClick = { confirmClearAll = true }, enabled = controller.queue.isNotEmpty()) {
                Text(tr("queue.remove.all"))
            }
        }
    }

    if (confirmClearAll) {
        AlertDialog(
            onDismissRequest = { confirmClearAll = false },
            title = { Text("RipMe") },
            text = { Text(tr("queue.validation")) },
            confirmButton = {
                TextButton(onClick = {
                    controller.clear()
                    selected.value = emptySet()
                    confirmClearAll = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAll = false }) { Text("No") }
            },
        )
    }
}
