package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Two kinds of entries in the Basic Tools grid:
 * - [BasicTool]: legacy implemented tools or simple "Planned" placeholders without a model.
 * - [PlannedBasicTool] (from PlannedBasicToolModels.kt): roadmap modules with rich detail pages.
 */
data class BasicTool(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val implemented: Boolean,
)

val basicTools = listOf(
    BasicTool("root", "Root Management", Icons.Outlined.AdminPanelSettings, implemented = true),
    BasicTool("memory", "Memory Monitoring", Icons.Outlined.Memory, implemented = true),
    BasicTool("reboot", "Scheduled Reboot", Icons.Outlined.RestartAlt, implemented = true),
    BasicTool("privacy", "Process Privacy Guard", Icons.Outlined.ViewList, implemented = true),
    BasicTool("doppelganger", "Doppelganger", Icons.Outlined.ContentCopy, implemented = true),
    BasicTool("video-injection", "Video Test Feed", Icons.Outlined.Videocam, implemented = true),
    BasicTool("upload", "Upload File", Icons.Outlined.UploadFile, implemented = false),
    BasicTool("keepalive", "Process Keep-Alive", Icons.Outlined.Bolt, implemented = false),
    BasicTool("autostart", "App Auto-Start", Icons.Outlined.PlayCircle, implemented = false),
    BasicTool("proxy", "App Proxy", Icons.Outlined.VpnLock, implemented = false),
    BasicTool("location", "Virtual Location", Icons.Outlined.LocationOn, implemented = false),
)

@Composable
fun BasicToolsScreen(
    onPick: (BasicTool) -> Unit,
    onPickPlanned: (PlannedBasicTool) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Basic Tools", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(basicTools, key = { it.id }) { tool ->
                ToolTile(tool, onClick = { onPick(tool) })
            }
            items(plannedBasicTools, key = { "planned-${it.id}" }) { tool ->
                PlannedToolTile(tool, onClick = { onPickPlanned(tool) })
            }
        }
    }
}

@Composable
private fun ToolTile(tool: BasicTool, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(tool.icon, contentDescription = tool.title)
            Spacer(Modifier.height(8.dp))
            Text(
                tool.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
            if (!tool.implemented) {
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = onClick,
                    label = { Text("Planned", style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
    }
}

@Composable
private fun PlannedToolTile(tool: PlannedBasicTool, onClick: () -> Unit) {
    val statusLabel = when (tool.status) {
        PlannedToolStatus.Planned -> "Planned"
        PlannedToolStatus.Restricted -> "Restricted"
        PlannedToolStatus.ResearchOnly -> "Research only"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(tool.icon, contentDescription = tool.safeName)
            Spacer(Modifier.height(8.dp))
            Text(
                tool.safeName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            AssistChip(
                onClick = onClick,
                label = { Text(statusLabel, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
fun PlannedToolScreen(tool: BasicTool, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Back to tools") }
        Spacer(Modifier.height(8.dp))
        Text(tool.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Planned · not available in this build", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        SafetyPolicyLink()
    }
}
