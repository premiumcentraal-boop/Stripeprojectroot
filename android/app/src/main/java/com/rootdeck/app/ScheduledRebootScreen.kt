package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class RebootAction(val title: String, val command: String)

private val rebootActions = listOf(
    RebootAction("Reboot device", "reboot"),
    RebootAction("Reboot to recovery", "reboot recovery"),
    RebootAction("Reboot to bootloader", "reboot bootloader"),
)

@Composable
fun ScheduledRebootScreen(repo: RootRepository, onBack: () -> Unit) {
    var pending by remember { mutableStateOf<RebootAction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to tools") }
        Text("Scheduled Reboot", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Reboots execute immediately after you confirm. Scheduled reboots will arrive " +
                "in a future build via Android's WorkManager — you'll get a notification " +
                "before any command runs.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (repo.rootMode.value != RootMode.ROOTDECK_ONLY) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Enable RootDeck root mode first",
                        style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Open Root Management and turn on the global root switch.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        rebootActions.forEach { action ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(action.title, style = MaterialTheme.typography.titleSmall)
                    if (repo.showCommandPreviews.value) {
                        Text("$ ${action.command}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { pending = action },
                        enabled = repo.rootMode.value == RootMode.ROOTDECK_ONLY,
                    ) { Text("Run") }
                }
            }
        }
    }

    pending?.let { action ->
        SafetyConfirmDialog(
            title = action.title,
            command = action.command,
            risk = RiskLevel.High,
            showPreview = repo.showCommandPreviews.value,
            body = "This will immediately reboot your device. All unsaved work will be lost.",
            onConfirm = {
                val a = action
                pending = null
                repo.runConfirmedCommand(a.title, a.command)
            },
            onDismiss = { pending = null },
        )
    }
}
