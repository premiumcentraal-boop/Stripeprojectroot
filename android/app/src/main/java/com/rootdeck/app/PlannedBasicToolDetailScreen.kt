package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlannedBasicToolDetailScreen(tool: PlannedBasicTool, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }

        Text(tool.safeName, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Reference name: ${tool.referenceName}",
            style = MaterialTheme.typography.bodySmall,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(tool.status)
            if (tool.addedIn != null) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("Added in ${tool.addedIn}", style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        Text(tool.subtitle, style = MaterialTheme.typography.titleSmall)

        Divider()

        Text("Not available in this build", style = MaterialTheme.typography.labelMedium)
        Text("This is a roadmap placeholder", style = MaterialTheme.typography.bodySmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = false, onCheckedChange = null, enabled = false)
            Spacer(Modifier.width(8.dp))
            Text("Enable module (disabled)", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = {}, enabled = false) { Text("Not available") }

        Spacer(Modifier.height(8.dp))
        SafetyPolicyLink()
    }
}

@Composable
private fun StatusBadge(status: PlannedToolStatus) {
    val label = when (status) {
        PlannedToolStatus.Planned -> "Planned"
        PlannedToolStatus.Restricted -> "Restricted"
        PlannedToolStatus.ResearchOnly -> "Research only"
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    )
}
