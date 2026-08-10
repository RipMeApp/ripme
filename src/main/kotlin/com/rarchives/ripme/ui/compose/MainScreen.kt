package com.rarchives.ripme.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * First real Compose screen for RipMe (RipMeApp/ripme#2082 scaffold): a URL
 * field, Rip/Stop/Panic buttons, a status line, a progress bar, and a
 * scrolling log pane - all bound to [RipController]. Deliberately minimal:
 * no queue, no history panel, no configuration panel (see plan §6).
 */
@Composable
fun MainScreen(controller: RipController) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                var urlText by remember { mutableStateOf("") }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            controller.startRip(urlText)
                            urlText = ""
                        },
                        enabled = !controller.busy
                    ) {
                        Text("Rip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { controller.stop() },
                        enabled = controller.busy
                    ) {
                        Text("Stop")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { controller.panic() },
                        enabled = controller.busy
                    ) {
                        Text("Panic!")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = controller.status, color = controller.statusColor)

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { controller.progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val listState = rememberLazyListState()
                LaunchedEffect(controller.log.size) {
                    if (controller.log.isNotEmpty()) {
                        listState.animateScrollToItem(controller.log.size - 1)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(controller.log) { line ->
                        Text(text = line.text, color = line.color)
                    }
                }
            }
        }
    }
}
