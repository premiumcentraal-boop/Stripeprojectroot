package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * Generic installed-app picker used by future tools (Process Manager, App Proxy, etc.).
 * Honors the RootDeck-internal allowlist — apps not allowed cannot be picked unless
 * the caller enables [allowDisallowedApps].
 */
@Composable
fun AppPickerScreen(
    repo: RootRepository,
    allowDisallowedApps: Boolean = true,
    onPick: (ManagedApp) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { if (repo.apps.isEmpty()) repo.loadInstalledApps() }

    val filtered = remember(repo.apps.toList(), search, allowDisallowedApps) {
        val base = if (allowDisallowedApps) repo.apps.toList()
        else repo.apps.filter { it.rootActionsAllowedInsideRootDeck }
        if (search.isBlank()) base
        else base.filter {
            it.label.contains(search, true) || it.packageName.contains(search, true)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pick an app", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(app.label, style = MaterialTheme.typography.titleSmall)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                            if (!app.rootActionsAllowedInsideRootDeck) {
                                Text(
                                    "Not allowed inside RootDeck",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(app.packageName))
                        }) { Text("Copy") }
                        TextButton(onClick = { onPick(app) }) { Text("Pick") }
                    }
                }
            }
        }
    }
}
