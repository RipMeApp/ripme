package com.rarchives.ripme.android.ui.log

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rarchives.ripme.android.engine.RipEngine
import com.rarchives.ripme.android.ui.theme.legibleOn

/**
 * Android port of the desktop's `ui.compose.log.LogScreen` (coloured lines from the engine's log
 * buffer) - see that file (src/main/kotlin/com/rarchives/ripme/ui/compose/log/LogScreen.kt).
 * Reads [RipEngine.queueController] directly rather than through a per-screen ViewModel, matching
 * QueueScreen/HistoryScreen (see QueueScreen's header comment).
 *
 * Each line's stored colour is run through [legibleOn] before rendering - the ported engine emits
 * a handful of colours tuned for the desktop's always-light background (see that function's header
 * comment for which ones actually need adjusting on a dark surface).
 */
@Composable
fun LogScreen() {
    val log by RipEngine.queueController.logFlow.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val listState = rememberLazyListState()

    LaunchedEffect(log.size) {
        if (log.isNotEmpty()) {
            listState.animateScrollToItem(log.size - 1)
        }
    }

    if (log.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No log output yet")
        }
    } else {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            items(log) { line ->
                Text(
                    text = line.text,
                    color = line.color.legibleOn(isDark),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
