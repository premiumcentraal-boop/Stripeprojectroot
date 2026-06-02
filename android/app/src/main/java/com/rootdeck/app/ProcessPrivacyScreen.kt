package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Process Privacy Guard screen. Transparent, log-everything, confirmation-gated
 * controls for an app's background activity. Does NOT hide processes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessPrivacyScreen(
    root: RootRepository,
    privacy: ProcessPrivacyRepository,
    onBack: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(PrivacyFilter.All) }
    var showInfo by remember { mutableStateOf(false) }
    var detailFor by remember { mutableStateOf<ManagedApp?>(null) }
    var pendingApply by remember { mutableStateOf<ProcessPrivacyPolicy?>(null) }
    var pendingRestore by remember { mutableStateOf<ProcessPrivacyPolicy?>(null) }

    LaunchedEffect(Unit) {
        privacy.onScreenOpened()
        if (root.apps.isEmpty()) root.loadInstalledApps()
    }

    val visibleApps = remember(root.apps.toList(), privacy.policies.toMap(), search, filter) {
        root.apps.asSequence()
            .filter { app ->
                when (filter) {
                    PrivacyFilter.All -> true
                    PrivacyFilter.UserApps -> !app.isSystemApp
                    PrivacyFilter.SystemApps -> app.isSystemApp
                    PrivacyFilter.ActivePolicies -> privacy.policies[app.packageName]?.enabled == true
                }
            }
            .filter {
                search.isBlank() ||
                    it.label.contains(search, true) ||
                    it.packageName.contains(search, true)
            }
            .toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Process Privacy Guard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "About")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Control background app activity",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(6.dp))
                        SafetyPolicyLink()
                    }
                }
            }

            if (root.rootMode.value != RootMode.ROOTDECK_ONLY) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("RootDeck root mode is disabled.",
                                style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Enable root in Root Management first. " +
                                    "You can still mark policies — RootDeck will only run commands once root is active.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
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

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrivacyFilter.values().forEach { f ->
                        FilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = {
                                Text(
                                    when (f) {
                                        PrivacyFilter.All -> "All"
                                        PrivacyFilter.UserApps -> "User apps"
                                        PrivacyFilter.SystemApps -> "System apps"
                                        PrivacyFilter.ActivePolicies ->
                                            "Active (${privacy.activePoliciesCount})"
                                    }
                                )
                            },
                        )
                    }
                }
            }

            item {
                Text(
                    "Applications suitable for privacy control",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            if (root.appsLoading.value) {
                item {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(visibleApps, key = { it.packageName }) { app ->
                val policy = privacy.policies[app.packageName]
                AppRow(
                    app = app,
                    enabled = policy?.enabled == true,
                    onToggle = { wantOn ->
                        val base = privacy.policyFor(app.packageName, app.label)
                        if (wantOn) {
                            pendingApply = base.copy(enabled = false)
                        } else if (policy != null) {
                            pendingRestore = policy
                        }
                    },
                    onClick = { detailFor = app },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("About Process Privacy Guard") },
            text = {
                Text(
                    "RootDeck can force stop apps, restrict their background execution and " +
                        "wake locks via Android's appops, and optionally disable them for the " +
                        "current user. It cannot — and will not — invisibly hide processes " +
                        "from Android, other apps, or security tools."
                )
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Got it") } },
        )
    }

    detailFor?.let { app ->
        AppDetailDialog(
            app = app,
            policy = privacy.policies[app.packageName],
            running = privacy.runningProcessesFor(app.packageName),
            rootActive = root.rootMode.value == RootMode.ROOTDECK_ONLY,
            showPreviews = root.showCommandPreviews.value,
            onForceStop = {
                privacy.forceStopNow(app.packageName)
                detailFor = null
            },
            onApply = {
                pendingApply = privacy.policyFor(app.packageName, app.label).copy(enabled = false)
                detailFor = null
            },
            onRestore = {
                privacy.policies[app.packageName]?.let { pendingRestore = it }
                detailFor = null
            },
            onCopy = {
                // handled inside dialog
            },
            onRefreshProcesses = { privacy.refreshProcesses() },
            onDismiss = { detailFor = null },
        )
    }

    pendingApply?.let { base ->
        ApplyPolicyDialog(
            initial = base,
            showPreviews = root.showCommandPreviews.value,
            rootActive = root.rootMode.value == RootMode.ROOTDECK_ONLY,
            isSystemApp = root.apps.firstOrNull { it.packageName == base.packageName }?.isSystemApp == true,
            onConfirm = { configured ->
                pendingApply = null
                privacy.applyPolicy(configured)
            },
            onDismiss = { pendingApply = null },
        )
    }

    pendingRestore?.let { p ->
        RestoreDialog(
            policy = p,
            showPreviews = root.showCommandPreviews.value,
            onConfirm = {
                pendingRestore = null
                privacy.restoreDefaults(p)
            },
            onDismiss = { pendingRestore = null },
        )
    }
}

@Composable
private fun AppRow(
    app: ManagedApp,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(app.label, style = MaterialTheme.typography.titleSmall)
                    if (enabled) {
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = onClick,
                            label = { Text("Policy active",
                                style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                if (app.isSystemApp) {
                    Text("System app", style = MaterialTheme.typography.labelSmall)
                }
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ApplyPolicyDialog(
    initial: ProcessPrivacyPolicy,
    showPreviews: Boolean,
    rootActive: Boolean,
    isSystemApp: Boolean,
    onConfirm: (ProcessPrivacyPolicy) -> Unit,
    onDismiss: () -> Unit,
) {
    var forceStop by remember { mutableStateOf(initial.forceStopOnEnable) }
    var restrictBg by remember { mutableStateOf(initial.restrictBackground) }
    var restrictWake by remember { mutableStateOf(initial.restrictWakeLocks) }
    var disableApp by remember { mutableStateOf(initial.disableForCurrentUser) }
    val pkg = initial.packageName

    val commands = buildList {
        if (forceStop) add("am force-stop $pkg")
        if (restrictBg) {
            add("cmd appops set $pkg RUN_IN_BACKGROUND ignore")
            add("cmd appops set $pkg RUN_ANY_IN_BACKGROUND ignore")
        }
        if (restrictWake) add("cmd appops set $pkg WAKE_LOCK ignore")
        if (disableApp) add("pm disable-user --user 0 $pkg")
    }
    val anySelected = commands.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Process Privacy Guard to ${initial.label}?") },
        text = {
            Column {
                Text(
                    "Force-stop the app and restrict selected background actions via shell commands."
                )
                if (isSystemApp) {
                    Spacer(Modifier.height(8.dp))
                    Text("⚠ This is a system app. Disabling or restricting it may " +
                        "destabilize the device.",
                        style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                CheckRow("Force stop now", forceStop) { forceStop = it }
                CheckRow("Restrict background execution", restrictBg) { restrictBg = it }
                CheckRow("Restrict wake locks", restrictWake) { restrictWake = it }
                CheckRow("Disable for current user (high risk)", disableApp) { disableApp = it }

                if (showPreviews && commands.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Command preview:", style = MaterialTheme.typography.labelMedium)
                    commands.forEach {
                        Text("$ $it",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (!rootActive) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Root mode is disabled. The policy will be saved but no commands will run yet.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = anySelected,
                onClick = {
                    onConfirm(
                        initial.copy(
                            forceStopOnEnable = forceStop,
                            restrictBackground = restrictBg,
                            restrictWakeLocks = restrictWake,
                            disableForCurrentUser = disableApp,
                            enabled = true,
                        )
                    )
                },
            ) { Text("Apply policy") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RestoreDialog(
    policy: ProcessPrivacyPolicy,
    showPreviews: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val pkg = policy.packageName
    val commands = buildList {
        if (policy.restrictBackground) {
            add("cmd appops set $pkg RUN_IN_BACKGROUND allow")
            add("cmd appops set $pkg RUN_ANY_IN_BACKGROUND allow")
        }
        if (policy.restrictWakeLocks) add("cmd appops set $pkg WAKE_LOCK allow")
        if (policy.disableForCurrentUser) add("pm enable $pkg")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore ${policy.label}?") },
        text = {
            Column {
                Text("Restore the previously restricted background permissions and re-enable the app if it was disabled.")
                if (showPreviews && commands.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Command preview:", style = MaterialTheme.typography.labelMedium)
                    commands.forEach {
                        Text("$ $it",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (commands.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Nothing to restore — the policy did not change any system state.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Restore defaults") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AppDetailDialog(
    app: ManagedApp,
    policy: ProcessPrivacyPolicy?,
    running: List<RunningProcessInfo>,
    rootActive: Boolean,
    showPreviews: Boolean,
    onForceStop: () -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
    onCopy: () -> Unit,
    onRefreshProcesses: () -> Unit,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(app.label) },
        text = {
            Column {
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                if (app.isSystemApp) {
                    Text("System app", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
                Text("Current policy", style = MaterialTheme.typography.labelMedium)
                Text(
                    if (policy?.enabled == true)
                        buildString {
                            if (policy.forceStopOnEnable) append("force-stop · ")
                            if (policy.restrictBackground) append("restrict background · ")
                            if (policy.restrictWakeLocks) append("restrict wake locks · ")
                            if (policy.disableForCurrentUser) append("disabled for user 0 · ")
                        }.trimEnd(' ', '·')
                    else "No active policy.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Running processes", style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onRefreshProcesses,
                        enabled = rootActive,
                    ) { Text("Refresh") }
                }
                if (running.isEmpty()) {
                    Text("Run ps -A from this screen to inspect process state.",
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    running.take(6).forEach {
                        Text("${it.pid}  ${it.packageOrCommand}",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (showPreviews) {
                    Spacer(Modifier.height(8.dp))
                    Text("Quick command:  am force-stop ${app.packageName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace)
                }
            }
        },
        confirmButton = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onApply) { Text("Apply policy") }
                    TextButton(
                        onClick = onForceStop,
                        enabled = rootActive,
                    ) { Text("Force stop now") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onRestore,
                        enabled = policy?.enabled == true,
                    ) { Text("Restore defaults") }
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(app.packageName))
                        onCopy()
                    }) { Text("Copy package") }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label)
    }
}
