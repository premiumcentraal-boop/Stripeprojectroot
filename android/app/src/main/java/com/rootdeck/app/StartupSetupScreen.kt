package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class SetupStep { RootAccess, Recommendations, Installing, Success, Failed }

private data class RecommendedInstall(
    val title: String,
    val subtitle: String,
    val status: String,
    val icon: ImageVector,
    val primary: Boolean = false,
)

@Composable
fun StartupSetupScreen(
    repo: RootRepository,
    onFinish: () -> Unit,
    onOpenVideoTestFeed: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(SetupStep.RootAccess) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("RootDeck needs root access to install LSPosed for you.") }
    var installState by remember { mutableStateOf(LsposedInstallState()) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }

    val recommendations = listOf(
        RecommendedInstall(
            title = "LSPosed",
            subtitle = "Required for RootDeck module features on Magisk devices.",
            status = if (installState.assetUrl != null) "Ready to install" else "Scan needed",
            icon = Icons.Outlined.Extension,
            primary = true,
        ),
        RecommendedInstall(
            title = "Magisk root access",
            subtitle = "Lets RootDeck install modules after your approval.",
            status = if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) "Enabled" else "Needs permission",
            icon = Icons.Outlined.Security,
        ),
        RecommendedInstall(
            title = "Reboot after install",
            subtitle = "LSPosed becomes active after restarting your phone.",
            status = "Final step",
            icon = Icons.Outlined.RestartAlt,
        ),
    )

    fun scanRecommendations() {
        scope.launch {
            busy = true
            statusText = "Scanning your device and checking the recommended LSPosed install…"
            installState = LsposedInstaller.check(context, repo)
            busy = false
            step = SetupStep.Recommendations
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (step) {
                        SetupStep.RootAccess -> {
                            HeaderIcon(Icons.Outlined.Security)
                            Text("Set up RootDeck", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "To set up LSPosed automatically, RootDeck needs root access. Tap the button below, then press Allow in Magisk when it asks.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(statusText, style = MaterialTheme.typography.bodySmall)
                            Button(
                                enabled = !busy,
                                onClick = {
                                    busy = true
                                    statusText = "Waiting for Magisk root permission…"
                                    repo.setGlobalRootEnabled(true) { status ->
                                        busy = false
                                        statusText = status.message
                                        if (status.isRootGranted) scanRecommendations()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(if (busy) "Waiting for Magisk…" else "Allow Root Access") }
                            TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") }
                        }

                        SetupStep.Recommendations -> {
                            HeaderIcon(Icons.Outlined.Search)
                            Text("Recommended installations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (busy) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Looking for the best setup for your phone…", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text(
                                    "RootDeck found these setup steps for your device. Start with LSPosed.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                RecommendationGallery(recommendations)
                                installState.releaseName?.let {
                                    AssistChip(onClick = {}, label = { Text("Selected LSPosed version: $it") })
                                }
                                installState.error?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { scanRecommendations() }, enabled = !busy) { Text("Scan again") }
                                    Button(
                                        enabled = installState.assetUrl != null && !busy,
                                        onClick = {
                                            step = SetupStep.Installing
                                            busy = true
                                            downloadProgress = 0f
                                            scope.launch {
                                                installState = LsposedInstaller.downloadAndInstall(
                                                    context = context,
                                                    repo = repo,
                                                    current = installState,
                                                    onProgress = { downloadProgress = it },
                                                )
                                                busy = false
                                                step = if (installState.error == null) SetupStep.Success else SetupStep.Failed
                                            }
                                        },
                                    ) { Text("Install LSPosed") }
                                }
                                TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Not now") }
                            }
                        }

                        SetupStep.Installing -> {
                            HeaderIcon(Icons.Outlined.Download)
                            Text("Downloading LSPosed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            val progress = downloadProgress
                            if (progress != null && progress > 0f) {
                                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                                Text("Download progress: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            } else {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Preparing download…", style = MaterialTheme.typography.bodySmall)
                            }
                            SetupFlow(current = 1)
                            Text("After the download, Magisk installs the module. Please allow any Magisk prompt.", style = MaterialTheme.typography.bodyMedium)
                        }

                        SetupStep.Success -> {
                            HeaderIcon(Icons.Outlined.CheckCircle)
                            Text("LSPosed setup finished", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Download and Magisk install completed successfully. One restart is required before LSPosed can work.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            SetupFlow(current = 3)
                            NextStepCard(
                                title = "What to do next",
                                steps = listOf(
                                    "Restart your phone.",
                                    "After reboot, open the LSPosed notification or LSPosed Manager.",
                                    "Enable the RootDeck module and scope it only to your own test apps.",
                                    "Open Video Test Feed to choose a video file and front/back camera target.",
                                ),
                            )
                            Button(onClick = onOpenVideoTestFeed, modifier = Modifier.fillMaxWidth()) { Text("Set up Video Test Feed") }
                            OutlinedButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Continue to RootDeck") }
                        }

                        SetupStep.Failed -> {
                            HeaderIcon(Icons.Outlined.WarningAmber)
                            Text("LSPosed setup needs attention", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "RootDeck could not finish the LSPosed install. Nothing was changed unless Magisk reported a successful install.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            installState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                            NextStepCard(
                                title = "Try this",
                                steps = listOf(
                                    "Check that Magisk is installed and working.",
                                    "Make sure RootDeck is allowed in Magisk Superuser.",
                                    "Tap Scan again, then Install LSPosed.",
                                ),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { scanRecommendations() }) { Text("Scan again") }
                                Button(onClick = { step = SetupStep.Recommendations }) { Text("Back") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(icon: ImageVector) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.primary)
}

@Composable
private fun RecommendationGallery(items: List<RecommendedInstall>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (item.primary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(item.icon, contentDescription = null, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                    AssistChip(onClick = {}, label = { Text(item.status) })
                }
            }
        }
    }
}

@Composable
private fun SetupFlow(current: Int) {
    val steps = listOf(
        Icons.Outlined.Download to "Download",
        Icons.Outlined.Settings to "Install",
        Icons.Outlined.RestartAlt to "Reboot",
        Icons.Outlined.Verified to "Enable",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Icon(
                    step.first,
                    contentDescription = null,
                    tint = if (index <= current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(step.second, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun NextStepCard(title: String, steps: List<String>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            steps.forEachIndexed { index, text ->
                Text("${index + 1}. $text", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
