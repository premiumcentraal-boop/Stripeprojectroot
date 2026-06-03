package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class SetupStep { RootAccess, Recommendations, Installing, Success }

@Composable
fun StartupSetupScreen(
    repo: RootRepository,
    onFinish: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(SetupStep.RootAccess) }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("RootDeck needs root access to install LSPosed for you.") }
    var installState by remember { mutableStateOf(LsposedInstallState()) }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }

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
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (step) {
                        SetupStep.RootAccess -> {
                            Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(42.dp))
                            Text("Set up RootDeck", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "To install LSPosed automatically, RootDeck needs root access. Tap the button below, then press Allow in Magisk when it asks.",
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
                            Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(42.dp))
                            Text("Recommended installations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (busy) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Scanning…", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Recommended: LSPosed", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    installState.message ?: "LSPosed helps RootDeck features work on Magisk devices.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                installState.releaseName?.let { Text("Version: $it", style = MaterialTheme.typography.bodySmall) }
                                installState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
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
                                                if (installState.error == null) step = SetupStep.Success
                                            }
                                        },
                                    ) { Text("Install LSPosed") }
                                }
                                TextButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Not now") }
                            }
                        }

                        SetupStep.Installing -> {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(42.dp))
                            Text("Installing LSPosed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            val progress = downloadProgress
                            if (progress != null && progress > 0f) {
                                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                                Text("Downloading ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            } else {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text("Preparing download…", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Magisk may ask for permission again. Please allow it.", style = MaterialTheme.typography.bodyMedium)
                            installState.error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = { step = SetupStep.Recommendations }, modifier = Modifier.fillMaxWidth()) { Text("Try again") }
                            }
                        }

                        SetupStep.Success -> {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("LSPosed installed", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Great. LSPosed was installed through Magisk. Restart your phone now. After reboot, open LSPosed Manager from the notification or launcher if available.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Continue to RootDeck") }
                        }
                    }
                }
            }
        }
    }
}
