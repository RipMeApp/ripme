package com.rarchives.ripme.ui.compose.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import com.rarchives.ripme.ui.compose.tr
import com.rarchives.ripme.utils.SiteCookieStorage

/**
 * Compose replica of Swing's CookieSettingsDialog (plan §6): domain field defaulting to
 * "reddit.com", cookie text field, help text, Save/Clear/Close via [SiteCookieStorage] with the
 * same validation/messages, using the same localized `cookies.*` keys.
 */
@Composable
fun CookieDialog(onClose: () -> Unit) {
    var domain by remember { mutableStateOf("reddit.com") }
    var cookieText by remember { mutableStateOf(loadDomainQuietly("reddit.com")) }
    var message by remember { mutableStateOf<String?>(null) }

    // Precomputed here (composable context) since tr() can't be called from the plain onClick
    // lambdas below.
    val domainRequiredMsg = tr("cookies.domain.required")
    val savedMsg = tr("cookies.saved")
    val invalidMsg = tr("cookies.invalid")
    val clearedMsg = tr("cookies.cleared")

    DialogWindow(
        onCloseRequest = onClose,
        title = tr("cookies.dialog.title"),
        state = rememberDialogState(width = 480.dp, height = 420.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(tr("cookies.domain"))
                OutlinedTextField(
                    value = domain,
                    onValueChange = { newDomain ->
                        domain = newDomain
                        cookieText = loadDomainQuietly(newDomain)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = cookieText,
                onValueChange = { cookieText = it },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )

            Text(tr("cookies.help"))

            message?.let { Text(it) }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = {
                    if (SiteCookieStorage.normalizeDomain(domain).isEmpty()) {
                        message = domainRequiredMsg
                        return@Button
                    }
                    try {
                        SiteCookieStorage.save(domain, cookieText)
                        message = savedMsg
                    } catch (e: IllegalArgumentException) {
                        message = invalidMsg
                    } catch (e: Exception) {
                        message = e.message
                    }
                }) { Text(tr("cookies.save")) }

                Button(onClick = {
                    if (SiteCookieStorage.normalizeDomain(domain).isEmpty()) {
                        return@Button
                    }
                    try {
                        SiteCookieStorage.clear(domain)
                        cookieText = ""
                        message = clearedMsg
                    } catch (e: Exception) {
                        message = e.message
                    }
                }) { Text(tr("cookies.clear")) }

                Button(onClick = onClose) { Text(tr("cookies.close")) }
            }
        }
    }
}

private fun loadDomainQuietly(domain: String): String =
    runCatching { SiteCookieStorage.load(domain) }.getOrDefault("")
