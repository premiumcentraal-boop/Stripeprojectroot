package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * The flagship screen.
 *
 *  - A global root switch toggles RootDeck's own [RootMode] (DISABLED ↔ ROOTDECK_ONLY).
 *    Enabling triggers a safety dialog, then runs `su -c id`.
 *  - An explanatory card makes the distinction between "RootDeck-internal policy"
 *    and "real OS root permission" explicit.
 *  - A per-app list lets the user mark which packages RootDeck is internally allowed
 *    to target with confirmed root actions. Toggles do NOT change the OS permission
 *    those apps have — the screen says so plainly.
 *  - An "Open Root Manager" button hands the user off to Magisk / KernelSU / APatch.
 */
@Composable
fun RootManagementScreen(repo: RootRepository, onOpenLogs: () -> Unit) {
    val context = LocalContext.current
    var pendingEnable by remember { mutableStateOf(false) }
    var pendingDisable by remember { mutableStateOf(false) }
    var pendingApp by remember { mutableStateOf<Pair<ManagedApp, Boolean>?>(null) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { if (repo.apps.isEmpty()) repo.loadInstalledApps() }

    val filtered = remember(repo.apps.toList(), search) {
        if (search.isBlank()) repo.apps.toList()
        else repo.apps.filter {
            it.label.contains(search, ignoreCase = true) ||
                it.packageName.contains(search, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("ROOT Management", style = MaterialTheme.typography.headlineSmall)
        }

        // Global root switch
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Global root access (RootDeck)",
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Allows RootDeck itself to run confirmed root commands.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = repo.rootMode.value == RootMode.ROOTDECK_ONLY,
                            onCheckedChange = { wanted ->
                                if (wanted) pendingEnable = true else pendingDisable = true
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(repo.rootStatus.value.message, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Explanation card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("How root permissions actually work",
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Real root permission is controlled by your installed root manager " +
                            "(Magisk, KernelSU, APatch). RootDeck can only manage its own " +
                            "internal policy — which apps RootDeck is allowed to target with " +
                            "confirmed root actions. Toggling an app below does NOT grant or " +
                            "revoke that app's OS-level root permission.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // Open root manager
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    val provider = repo.rootStatus.value.providerName ?: repo.detectProvider()
                    Text(
                        provider?.let { "Detected root manager: $it" }
                            ?: "No supported root manager detected.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val opened = repo.openRootManager(context)
                            if (!opened) {
                                repo.log(
                                    "Open root manager",
                                    "Root permissions must be controlled from your installed root manager.",
                                    isError = true,
                                )
                            }
                        },
                        enabled = provider != null,
                    ) { Text("Open Root Manager") }
                }
            }
        }

        // Per-app allowlist
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Allowed apps inside RootDeck",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onOpenLogs) { Text("View logs") }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search apps") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (repo.appsLoading.value) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) { CircularProgressIndicator() }
            }
        }

        items(filtered, key = { it.packageName }) { entry ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.label, style = MaterialTheme.typography.titleSmall)
                        Text(entry.packageName, style = MaterialTheme.typography.bodySmall)
                        if (entry.isSystemApp) {
                            Text("System app", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Switch(
                        checked = entry.rootActionsAllowedInsideRootDeck,
                        onCheckedChange = { wanted -> pendingApp = entry to wanted },
                    )
                }
            }
        }
    }

    if (pendingEnable) {
        SafetyConfirmDialog(
            title = "Enable RootDeck root mode?",
            command = "su -c id",
            risk = RiskLevel.Low,
            showPreview = repo.showCommandPreviews.value,
            body = "RootDeck will ask your installed root manager for a shell, run `id`, " +
                "and look for uid=0. No other commands run.",
            onConfirm = {
                pendingEnable = false
                repo.setGlobalRootEnabled(true)
            },
            onDismiss = { pendingEnable = false },
        )
    }

    if (pendingDisable) {
        AlertDialog(
            onDismissRequest = { pendingDisable = false },
            title = { Text("Disable RootDeck root mode?") },
            text = { Text("RootDeck will refuse all root commands until you re-enable it.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDisable = false
                    repo.setGlobalRootEnabled(false)
                }) { Text("Disable") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDisable = false }) { Text("Cancel") }
            },
        )
    }

    pendingApp?.let { (entry, wanted) ->
        AlertDialog(
            onDismissRequest = { pendingApp = null },
            title = { Text(if (wanted) "Allow root actions for ${entry.label}?" else "Disallow ${entry.label}?") },
            text = {
                Text(
                    if (wanted)
                        "This only allows RootDeck to run approved commands targeting " +
                            "${entry.packageName}. It does not grant ${entry.label} root " +
                            "permission — that is controlled by your installed root manager."
                    else
                        "RootDeck will no longer target ${entry.packageName} with root actions."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    repo.setAppAllowed(entry.packageName, wanted)
                    pendingApp = null
                }) { Text(if (wanted) "Allow" else "Disallow") }
            },
            dismissButton = {
                TextButton(onClick = { pendingApp = null }) { Text("Cancel") }
            },
        )
    }
}
