package com.rarchives.ripme.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.rarchives.ripme.android.ui.LocalSnackbarHostState
import com.rarchives.ripme.android.ui.history.HistoryScreen
import com.rarchives.ripme.android.ui.log.LogScreen
import com.rarchives.ripme.android.ui.nav.Destination
import com.rarchives.ripme.android.ui.queue.QueueScreen
import com.rarchives.ripme.android.ui.rip.RipScreen
import com.rarchives.ripme.android.ui.settings.SettingsScreen
import com.rarchives.ripme.android.ui.theme.RipMeTheme

/**
 * Single-Activity host. Material3 [Scaffold] with a top [TopAppBar] + bottom [NavigationBar]
 * switching between the five destinations (plan's `:app` table) - the Android replacement for the
 * desktop's `MainScreen`/`NavController` overlay-panel pattern (see Destination's header comment).
 *
 * The [TopAppBar] fixes one of the two Phase B UI defects the plan called out: with no top bar,
 * this activity's content drew flush with the top of the window, under the status bar (targetSdk
 * 36 means the system enforces edge-to-edge - there's no opting out - so a window that doesn't
 * reserve space for the status bar itself has content drawn right underneath it). A titled
 * [TopAppBar] (Material3's own `windowInsets` default already accounts for the status bar) both
 * gives the app the title the desktop window already has ("RipMe") and reserves that space, so
 * [Scaffold]'s `innerPadding` now pushes every destination's content below both the status bar and
 * the bar itself. See [RipMeTheme] for the other defect (dark mode).
 *
 * All rip state lives in `RipEngine`, a process singleton independent of this Activity, so
 * rotation/recreation just re-collects the same StateFlows from scratch - nothing here needs
 * onSaveInstanceState beyond which tab is currently selected, and even that is a minor UX nicety
 * rather than anything load-bearing.
 */
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No-op either way: the foreground service (and the rip it's tracking) runs regardless
           of whether this is granted - only the notification's visibility depends on it. */ }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            RipMeTheme {
                var destination by remember { mutableStateOf(Destination.Rip) }
                // Backs MainActivity's one Scaffold - see LocalSnackbarHostState's header comment
                // for why every destination shares this single host rather than each owning one.
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    topBar = { TopAppBar(title = { Text("RipMe") }) },
                    bottomBar = {
                        NavigationBar {
                            Destination.entries.forEach { dest ->
                                NavigationBarItem(
                                    selected = destination == dest,
                                    onClick = { destination = dest },
                                    icon = { Icon(dest.icon, contentDescription = dest.label) },
                                    label = { Text(dest.label) },
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                ) { innerPadding ->
                    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                        Surface(modifier = Modifier.padding(innerPadding)) {
                            when (destination) {
                                Destination.Rip -> RipScreen()
                                Destination.Queue -> QueueScreen()
                                Destination.History -> HistoryScreen()
                                Destination.Log -> LogScreen()
                                Destination.Settings -> SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        // POST_NOTIFICATIONS is a runtime permission from API 33 onward; the foreground service
        // still runs and keeps the process alive without it either way (that guarantee is
        // Android's, independent of this permission) - only the notification itself stays
        // hidden - so this is a best-effort ask, not a gate on anything.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
