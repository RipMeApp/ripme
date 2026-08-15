package com.rarchives.ripme.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rarchives.ripme.ui.compose.config.ConfigScreen
import com.rarchives.ripme.ui.compose.history.HistoryScreen
import com.rarchives.ripme.ui.compose.log.LogScreen
import com.rarchives.ripme.ui.compose.nav.NavController
import com.rarchives.ripme.ui.compose.nav.Panel
import com.rarchives.ripme.ui.compose.nav.PanelComponents
import com.rarchives.ripme.ui.compose.queue.QueueController
import com.rarchives.ripme.ui.compose.queue.QueueScreen

/**
 * Full-parity main screen (RipMe #2082): URL field, Rip/Stop/Panic buttons, status + progress,
 * the four toggleable option panels (Log/History/Queue/Configuration) routed through
 * [NavController] - all bound to [QueueController] (which itself owns the single
 * [RipController] instance) rather than driving rips directly (plan §3/§4).
 */
@Composable
fun MainScreen(nav: NavController, queueController: QueueController, panels: PanelComponents) {
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
                    Button(onClick = {
                        queueController.enqueue(urlText)
                        urlText = ""
                    }) {
                        // Matches MainWindow: "Rip"/"Stop"/"Panic!" are hardcoded literals, never
                        // routed through Utils.getLocalizedString.
                        Text("Rip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { queueController.stop() },
                        enabled = queueController.busy
                    ) {
                        Text("Stop")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { queueController.panic() },
                        enabled = queueController.busy
                    ) {
                        Text("Panic!")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = queueController.status, color = queueController.statusColor)

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { queueController.progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val activePanel = nav.active

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OptionButton(tr("Log"), "comment.png", activePanel == Panel.Log) { nav.select(Panel.Log) }
                    Spacer(modifier = Modifier.width(4.dp))
                    OptionButton(tr("History"), "time.png", activePanel == Panel.History) { nav.select(Panel.History) }
                    Spacer(modifier = Modifier.width(4.dp))
                    OptionButton(
                        tr("queue") + queueController.queueCountSuffix(),
                        "list.png",
                        activePanel == Panel.Queue,
                    ) { nav.select(Panel.Queue) }
                    Spacer(modifier = Modifier.width(4.dp))
                    OptionButton(tr("Configuration"), "gear.png", activePanel == Panel.Configuration) { nav.select(Panel.Configuration) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    when (activePanel) {
                        Panel.None -> {}
                        Panel.Log -> LogScreen(panels.log)
                        Panel.History -> HistoryScreen(panels.history)
                        Panel.Queue -> QueueScreen(panels.queue)
                        Panel.Configuration -> ConfigScreen(panels.config)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionButton(label: String, iconResource: String, active: Boolean, onClick: () -> Unit) {
    val icon = rememberResourceIcon(iconResource)
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    ) {
        if (icon != null) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = label, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}
