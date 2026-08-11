package com.rarchives.ripme.ui.compose.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.rarchives.ripme.utils.Utils
import com.rarchives.ripme.ui.compose.tr

/**
 * Port of MainWindow's history table + HistoryMenuMouseListener context actions: url/created/
 * modified/count/checkbox columns, remove / clear(confirm) / re-rip-checked buttons, and
 * check-all/none actions (plan §6). Row multi-select uses row background highlight rather than
 * Swing's native table selection, and check-all/none are always-visible buttons rather than a
 * right-click context menu - a deliberate Compose-idiomatic adaptation, not a functional gap.
 */
@Composable
fun HistoryScreen(component: HistoryComponent) {
    val store = component.historyStore
    val queueController = component.queueController
    val selected = remember { mutableStateOf(setOf<Int>()) }
    var confirmClear by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Precomputed here (composable context) since tr() can't be called from the plain onClick
    // lambdas below.
    val noneToRerip = tr("history.load.none")
    val noneChecked = tr("history.load.none.checked")

    Column(modifier = Modifier.fillMaxSize()) {
        val hScroll = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize().weight(1f).horizontalScroll(hScroll)) {
            Row(modifier = Modifier.padding(4.dp)) {
                Text(tr("History"), modifier = Modifier.width(260.dp))
                Text("created", modifier = Modifier.width(110.dp))
                Text("modified", modifier = Modifier.width(110.dp))
                Text("#", modifier = Modifier.width(40.dp))
                Text("", modifier = Modifier.width(24.dp))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(store.rows) { index, row ->
                    val isSelected = index in selected.value
                    Row(
                        modifier = Modifier
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable {
                                selected.value = if (isSelected) selected.value - index else selected.value + index
                            }
                            .padding(2.dp),
                    ) {
                        Text(
                            text = row.url,
                            modifier = Modifier.width(260.dp),
                        )
                        Text(row.created, modifier = Modifier.width(110.dp))
                        Text(row.modified, modifier = Modifier.width(110.dp))
                        Text(row.count.toString(), modifier = Modifier.width(40.dp))
                        Checkbox(
                            checked = row.selected,
                            onCheckedChange = { checked -> store.setSelected(index, checked) },
                            modifier = Modifier.width(24.dp),
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Button(onClick = {
                store.remove(selected.value)
                selected.value = emptySet()
            }, enabled = selected.value.isNotEmpty()) {
                Text(tr("remove"))
            }
            Button(onClick = {
                if (Utils.getConfigBoolean("history.warn_before_delete", true)) {
                    confirmClear = true
                } else {
                    store.clear()
                }
            }) { Text(tr("clear")) }
            Button(onClick = {
                if (store.isEmpty()) {
                    errorMessage = noneToRerip
                    return@Button
                }
                val urls = store.selectedUrls()
                if (urls.isEmpty()) {
                    errorMessage = noneChecked
                    return@Button
                }
                urls.forEach { queueController.enqueue(it) }
            }) { Text(tr("re-rip.checked")) }
            Button(onClick = { store.setAllSelected(true) }) { Text(tr("history.check.all")) }
            Button(onClick = { store.setAllSelected(false) }) { Text(tr("history.check.none")) }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("RipMe") },
            text = { Text(tr("clear") + "?") },
            confirmButton = {
                TextButton(onClick = {
                    store.clear()
                    confirmClear = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("No") }
            },
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("RipMe Error") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            },
        )
    }
}
