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

private enum class SetupStep { RootAccess, Recommendations, Installing, Success, Failed, Installed }

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
    onOpenVectorSetup: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) SetupStep.Recommendations else SetupStep.RootAccess) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf(if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) "Root access is already enabled. RootDeck will now show the recommended installs." else "RootDeck needs root access to check Magisk/Zygisk and guide the right Vector setup.") }
    var installState by remember { mutableStateOf(LsposedInstallState()) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }

    val recommendations = listOf(
        RecommendedInstall(
            title = "Vector / LSPosed",
            subtitle = "Recommended modern continuation/fork for RootDeck module features on Magisk devices.",
            status = when {
                installState.lsposedInstalled -> "Installed"
                installState.rebootPending -> "Reboot needed"
                installState.assetUrl != null -> "Guide ready"
                else -> "Scan needed"
            },
            icon = Icons.Outlined.Extension,
            primary = true,
        ),
        RecommendedInstall(
            title = "Magisk root access",
            subtitle = "Lets RootDeck check your Magisk/Zygisk setup after your approval.",
            status = if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) "Enabled" else "Needs permission",
            icon = Icons.Outlined.Security,
        ),
        RecommendedInstall(
            title = "Zygisk",
            subtitle = "Recommended for modern Vector module loading.",
            status = if (installState.zygiskEnabled) "Enabled ✓" else "Recommended on",
            icon = Icons.Outlined.Verified,
            primary = installState.zygiskEnabled,
        ),
        RecommendedInstall(
            title = "Reboot after install",
            subtitle = "Vector becomes active after restarting your phone.",
            status = if (installState.rebootPending) "Needed now" else if (installState.lsposedInstalled) "Done" else "Final step",
            icon = Icons.Outlined.RestartAlt,
        ),
    )

    fun scanRecommendations() {
        scope.launch {
            step = SetupStep.Recommendations
            busy = true
            statusText = "Scanning your device and checking the recommended Vector setup…"
            installState = LsposedInstaller.check(context, repo)
            statusText = "Scan finished. Review the recommended installs below, then open the official Vector setup guide."
            if (installState.lsposedInstalled) {
                step = SetupStep.Installed
            }
            busy = false
        }
    }

    fun runRecommendedSetup() {
        if (busy) return
        if (!installState.zygiskEnabled && repo.rootMode.value == RootMode.ROOTDECK_ONLY) {
            busy = true
            statusText = "Enabling Zygisk in Magisk…"
            scope.launch {
                val result = LsposedInstaller.enableZygisk(repo)
                installState = LsposedInstaller.check(context, repo)
                statusText = if (result.success) "Zygisk enabled. It will show as complete after Magisk applies the setting; reboot may be required." else result.stderr.ifBlank { "Could not enable Zygisk." }
                busy = false
            }
            return
        }
        statusText = "Opening the Vector / LSPosed setup guide. RootDeck will not silently install Magisk modules."
        onOpenVectorSetup()
    }

    LaunchedEffect(Unit) {
        if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) scanRecommendations()
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
                                "RootDeck can check Magisk and Zygisk first, then guide you to the correct official Vector release. Tap Setup, then press Allow in Magisk if it asks.",
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
                            ) { Text(if (busy) "Waiting for Magisk…" else "Setup") }
                            TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("No thanks") }
                        }

                        SetupStep.Recommendations -> {
                            HeaderIcon(Icons.Outlined.Search)
                            Text("Recommended installations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                statusText,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (busy) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Looking for Magisk, Android version, Zygisk, and the right Vector setup…", style = MaterialTheme.typography.bodySmall)
                                RecommendationGallery(recommendations)
                            } else {
                                Text(
                                    "RootDeck found these setup steps for your device. Start with Vector / LSPosed, then reboot and enable only the RootDeck module where needed.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                RecommendationGallery(recommendations)
                                installState.releaseName?.let {
                                    AssistChip(onClick = {}, label = { Text("Detected LSPosed/Vector version: $it") })
                                }
                                installState.error?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                                if (!installState.zygiskEnabled && repo.rootMode.value == RootMode.ROOTDECK_ONLY) {
                                    ElevatedCard(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("Recommended: enable Zygisk", style = MaterialTheme.typography.titleSmall)
                                            Text("Zygisk is recommended for Vector. Tap Setup below to enable it in Magisk settings when root mode is enabled.", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                                if (installState.zygiskEnabled) {
                                    AssistChip(onClick = {}, label = { Text("✓ Zygisk enabled — no further setup needed") })
                                }
                                if (installState.rebootPending) {
                                    NextStepCard(
                                        title = "Reboot still needed",
                                        steps = listOf(
                                            "Restart your phone once to activate Vector / LSPosed.",
                                            "Open RootDeck again after reboot.",
                                            "RootDeck will detect the active framework and will not recommend setup again.",
                                        ),
                                    )
                                }
                                Button(
                                    enabled = !busy && !installState.rebootPending,
                                    onClick = { runRecommendedSetup() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(if (installState.rebootPending) "Reboot Required" else "Setup") }
                                TextButton(
                                    onClick = onFinish,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text("No thanks") }
                            }
                        }

                        SetupStep.Installing -> {
                            HeaderIcon(Icons.Outlined.Download)
                            Text("Opening Vector setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            val progress = downloadProgress
                            if (progress != null && progress > 0f) {
                                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                                Text("Download progress: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            } else {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Preparing download…", style = MaterialTheme.typography.bodySmall)
                            }
                            SetupFlow(current = 1)
                            Text("RootDeck no longer silently installs Magisk modules. The Vector guide opens the official release, downloads the ZIP to Downloads, and shows the exact Magisk steps.", style = MaterialTheme.typography.bodyMedium)
                            Text(statusText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        SetupStep.Success -> {
                            HeaderIcon(Icons.Outlined.CheckCircle)
                            Text("Vector setup guide finished", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                installState.message ?: "Follow the Vector guide and reboot once after installing the ZIP through Magisk Modules.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            installState.downloadedFile?.let {
                                AssistChip(onClick = {}, label = { Text("Installed from downloaded ZIP") })
                            }
                            SetupFlow(current = 3)
                            NextStepCard(
                                title = "What to do next",
                                steps = listOf(
                                    "Restart your phone.",
                                    "After reboot, open the Vector/LSPosed notification or manager.",
                                    "Enable the RootDeck module and scope it only to your own test apps.",
                                    "Open Video Test Feed to choose a video file, front/back camera target, and crop/resize mode.",
                                ),
                            )
                            Button(onClick = onOpenVideoTestFeed, enabled = !installState.rebootPending, modifier = Modifier.fillMaxWidth()) { Text(if (installState.rebootPending) "Reboot first" else "Set up Video Test Feed") }
                            OutlinedButton(
                                onClick = onFinish,
                                enabled = !installState.rebootPending,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(if (installState.rebootPending) "Reboot before continuing" else "Continue to RootDeck") }
                        }


                        SetupStep.Installed -> {
                            val versionLabel = installState.lsposedManagerVersion ?: "Detected"
                            HeaderIcon(Icons.Outlined.CheckCircle)
                            Text("Vector / LSPosed detected", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Vector / LSPosed is successfully installed and detected on this device. No actions are needed.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Version: $versionLabel") },
                            )
                            Button(
                                onClick = {
                                    LsposedInstaller.markSetupComplete(context)
                                    onFinish()
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Continue") }
                        }

                        SetupStep.Failed -> {
                            HeaderIcon(Icons.Outlined.WarningAmber)
                            Text("Vector setup needs attention", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "RootDeck could not finish the Vector setup check. Nothing was installed automatically; use the official Vector guide and Magisk Modules screen.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            installState.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            installState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                            NextStepCard(
                                title = "Try this",
                                steps = listOf(
                                    "Check that Magisk is installed and working.",
                                    "Make sure RootDeck is allowed in Magisk Superuser.",
                                    "Tap Setup to retry the next recommended step.",
                                ),
                            )
                            Button(onClick = { scanRecommendations() }, modifier = Modifier.fillMaxWidth()) { Text("Setup") }
                            TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("No thanks") }
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
        Icons.Outlined.Security to "Zygisk",
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
