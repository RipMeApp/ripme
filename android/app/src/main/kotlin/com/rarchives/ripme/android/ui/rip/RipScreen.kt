package com.rarchives.ripme.android.ui.rip

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rarchives.ripme.android.export.MediaStoreExporter
import com.rarchives.ripme.android.ui.LocalSnackbarHostState
import com.rarchives.ripme.android.ui.theme.legibleOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android port of the Rip-only slice of the desktop's `ui.compose.MainScreen` (URL field,
 * Rip/Stop/Panic, status line, progress bar) - see that file
 * (src/main/kotlin/com/rarchives/ripme/ui/compose/MainScreen.kt) for the full layout this is
 * drawn from. The four toggle buttons/panels below the fold in the desktop version are replaced
 * wholesale by MainActivity's NavigationBar (each now a destination of its own rather than an
 * overlay), so this screen is only ever the top portion of MainScreen.
 *
 * Adds one thing with no desktop equivalent: a "rip complete" card offering
 * [MediaStoreExporter]'s "Export to Downloads" action right after [RipViewModel.lastCompletedRipFlow]
 * reports a finished rip (plan §3 - "surfaced ... on rip-complete"; the other surface, a button
 * on each HistoryScreen row, has no per-rip timing to key off, so it's just always shown there).
 */
@Composable
fun RipScreen(viewModel: RipViewModel = viewModel()) {
    val status by viewModel.statusFlow.collectAsStateWithLifecycle()
    val statusColor by viewModel.statusColorFlow.collectAsStateWithLifecycle()
    val progress by viewModel.progressFlow.collectAsStateWithLifecycle()
    val busy by viewModel.busyFlow.collectAsStateWithLifecycle()
    val lastCompleted by viewModel.lastCompletedRipFlow.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        var urlText by remember { mutableStateOf("") }

        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = { Text("URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            // Matches MainScreen: "Rip"/"Stop"/"Panic!" are hardcoded literals there too (never
            // routed through getLocalizedString). Panic gets a warning triangle rather than a
            // plain stop icon because - like on the desktop - it has no graceful
            // finish-current-item behavior the way Stop does: it's ripper.stop()+ripper.panic(),
            // an immediate hard abort.
            Button(onClick = { viewModel.rip(urlText); urlText = "" }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rip")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.stop() }, enabled = busy, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stop")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.panic() }, enabled = busy, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Panic!")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = status, color = statusColor.legibleOn(isDark))

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

        lastCompleted?.let { rsc ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Rip complete: ${File(rsc.dir).name} (${rsc.count} files)",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        IconButton(onClick = { viewModel.clearLastCompletedRip() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = {
                        val albumDir = File(rsc.dir)
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
        }
    }
}
