package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun DoppelgangerScreen(
    repo: RootRepository,
    doppel: DoppelgangerRepository,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (doppel.state.value.users.isEmpty()) doppel.refresh()
        if (repo.apps.isEmpty()) repo.loadInstalledApps()
    }
    val state = doppel.state.value

    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    var newUserName by remember { mutableStateOf("RootDeck-Clone") }
    var clonePackage by remember { mutableStateOf("") }
    var appSearch by remember { mutableStateOf("") }
    var cloneTargetUser by remember { mutableStateOf<Int?>(null) }

    pendingAction?.let { action ->
        SafetyConfirmDialog(
            title = action.title,
            command = action.command,
            risk = action.risk,
            showPreview = repo.showCommandPreviews.value,
            body = action.body,
            onConfirm = {
                action.run()
                pendingAction = null
            },
            onDismiss = { pendingAction = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        Text("Doppelganger", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Run a second instance of an installed app under a separate Android user.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        if (repo.rootMode.value != RootMode.ROOTDECK_ONLY) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Root not active", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Enable RootDeck's global root mode first.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        state.lastError?.let { err ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Last error", style = MaterialTheme.typography.titleSmall)
                    Text(err, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { doppel.refresh() }, enabled = !state.busy) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Refresh")
            }
            if (state.busy) {
                Spacer(Modifier.width(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }

        // Create user
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Create a new clone user", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newUserName,
                    onValueChange = { newUserName = it },
                    label = { Text("User name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Use the user name as the safe clone label. Renaming the cloned app package or APK is not recommended because it can break signatures, app data, login state, and updates.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val name = newUserName
                        pendingAction = PendingAction(
                            title = "Create Android user",
                            command = "pm create-user \"$name\"",
                            risk = RiskLevel.Low,
                            body = "Creates a new secondary Android user. Reversible via remove-user.",
                            run = { doppel.createCloneUser(name) },
                        )
                    },
                    enabled = !state.busy,
                ) { Text("Create user") }
            }
        }

        // Clone an app
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Clone an app into a user", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = appSearch,
                    onValueChange = { appSearch = it },
                    label = { Text("Search installed apps") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                val selectableApps = repo.apps
                    .filter { app ->
                        val q = appSearch.trim().lowercase()
                        q.isBlank() || app.label.lowercase().contains(q) || app.packageName.lowercase().contains(q)
                    }
                    .filterNot { it.packageName == "com.rootdeck.app" }
                    .take(8)
                if (repo.appsLoading.value) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Loading installed apps…", style = MaterialTheme.typography.bodySmall)
                } else {
                    selectableApps.forEach { app ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clonePackage = app.packageName
                                    appSearch = app.label
                                },
                            tonalElevation = if (clonePackage == app.packageName) 3.dp else 0.dp,
                            color = if (clonePackage == app.packageName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(app.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    OutlinedTextField(
                        value = clonePackage,
                        onValueChange = { clonePackage = it },
                        label = { Text("Selected package") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Target user:", style = MaterialTheme.typography.labelMedium)
                Column {
                    state.users.filter { it.userId != 0 }.forEach { u ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = cloneTargetUser == u.userId,
                                onClick = { cloneTargetUser = u.userId },
                            )
                            Text("${u.name} (id=${u.userId})")
                        }
                    }
                    if (state.users.none { it.userId != 0 }) {
                        Text(
                            "No secondary users yet. Create one above.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val pkg = clonePackage.trim()
                        val uid = cloneTargetUser ?: return@Button
                        if (pkg.isBlank()) return@Button
                        pendingAction = PendingAction(
                            title = "Clone $pkg into user $uid",
                            command = "pm install-existing --user $uid $pkg",
                            risk = RiskLevel.Low,
                            body = "Installs the already-present APK into a secondary user. Each user " +
                                "keeps its own data and accounts. Reversible via uninstall.",
                            run = { doppel.cloneAppIntoUser(pkg, uid) },
                        )
                    },
                    enabled = !state.busy && clonePackage.isNotBlank() && cloneTargetUser != null,
                ) { Text("Clone into user") }
            }
        }

        // Existing clones
        if (state.clones.isNotEmpty()) {
            Text("Active cloned apps", style = MaterialTheme.typography.titleSmall)
            state.clones.forEach { c ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(c.packageName, style = MaterialTheme.typography.bodyMedium)
                        Text("user id=${c.userId}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    pendingAction = PendingAction(
                                        title = "Launch clone",
                                        command = "monkey -p ${c.packageName} --user ${c.userId} 1",
                                        risk = RiskLevel.Low,
                                        body = "Starts ${c.packageName} as user ${c.userId}.",
                                        run = { doppel.launchClone(c.packageName, c.userId) },
                                    )
                                },
                                enabled = !state.busy,
                            ) { Text("Launch") }
                            OutlinedButton(
                                onClick = {
                                    pendingAction = PendingAction(
                                        title = "Remove cloned app",
                                        command = "pm uninstall --user ${c.userId} ${c.packageName}",
                                        risk = RiskLevel.Medium,
                                        body = "Uninstalls the clone from user ${c.userId}. The original under " +
                                            "user 0 is not affected.",
                                        run = { doppel.removeClone(c.packageName, c.userId) },
                                    )
                                },
                                enabled = !state.busy,
                            ) { Text("Remove app clone") }
                        }
                    }
                }
            }
        }

        // Users list (with remove)
        if (state.users.any { it.userId != 0 }) {
            Text("Clone users", style = MaterialTheme.typography.titleSmall)
            state.users.filter { it.userId != 0 }.forEach { u ->
                val userCloneCount = state.clones.count { it.userId == u.userId }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(u.name, style = MaterialTheme.typography.bodyMedium)
                            Text("id=${u.userId} · $userCloneCount cloned apps", style = MaterialTheme.typography.bodySmall)
                            Text("Deleting this user removes all cloned apps and app data inside it.", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                pendingAction = PendingAction(
                                    title = "Delete clone user and all cloned apps",
                                    command = "pm remove-user ${u.userId}",
                                    risk = RiskLevel.High,
                                    body = "Deletes clone user ${u.name}, all $userCloneCount cloned apps inside it, and all app data for that clone user. The original apps in your main profile are not affected. This cannot be undone.",
                                    run = { doppel.removeUser(u.userId) },
                                )
                            },
                            enabled = !state.busy,
                        ) { Text("Delete user + apps") }
                    }
                }
            }
        }
    }
}

private data class PendingAction(
    val title: String,
    val command: String,
    val risk: RiskLevel,
    val body: String,
    val run: () -> Unit,
)
