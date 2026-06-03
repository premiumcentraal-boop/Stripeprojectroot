package com.rootdeck.app

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VectorSetupGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var env by remember { mutableStateOf<RootEnvironment?>(null) }
    var release by remember { mutableStateOf<VectorRelease?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var downloadMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        env = RootEnvironmentDetector.detect()
        VectorReleaseRepository.fetchLatest()
            .onSuccess { release = it }
            .onFailure { error = it.message ?: "Could not fetch Vector release information." }
    }

    val recommendation = env?.let { VectorRecommendationEngine.recommend(it, release) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        Text("Vector / LSPosed Setup", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Vector is the maintained modern continuation/fork commonly used instead of old LSPosed builds. RootDeck checks your device and points you to the official Vector release.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        WarningCard()

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Device status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val e = env
                if (e == null) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Checking device…", style = MaterialTheme.typography.bodySmall)
                } else {
                    VectorStatusRow("Android", "${e.androidRelease} · SDK ${e.sdkInt}")
                    VectorStatusRow("Device", e.deviceModel)
                    VectorStatusRow("Codename", e.productDevice ?: "Unknown")
                    VectorStatusRow("Build ID", e.buildId ?: "Unknown")
                    VectorStatusRow("CPU ABI", e.cpuAbi ?: "Unknown")
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                    Text("Root status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                val e = env
                if (e == null) {
                    Text("Checking root environment…", style = MaterialTheme.typography.bodySmall)
                } else {
                    VectorStatusRow("Root available", if (e.rootAvailable) "Good — Yes" else "Needs action — No")
                    VectorStatusRow("Magisk", when (e.magiskDetected) { true -> "Good — detected"; false -> "Not detected"; null -> "Unknown" })
                    VectorStatusRow("Magisk version", e.magiskVersion ?: "Unknown")
                    VectorStatusRow("Zygisk", when (e.zygiskEnabled) { true -> "Good — enabled"; false -> "Needs action — enable in Magisk"; null -> "Unknown — check Magisk settings" })
                    VectorStatusRow("Bootloader", when (e.bootloaderUnlocked) { true -> "Unlocked"; false -> "Locked"; null -> "Unknown" })
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Verified, contentDescription = null)
                    Text("Vector recommendation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                val r = recommendation
                if (r == null) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Preparing recommendation…", style = MaterialTheme.typography.bodySmall)
                } else {
                    VectorStatusRow("Framework", r.framework)
                    VectorStatusRow("Install type", r.installType)
                    VectorStatusRow("Source", r.source)
                    VectorStatusRow("Latest release", release?.name ?: release?.tagName ?: "Unknown")
                    VectorStatusRow("Release date", release?.publishedAt ?: "Unknown")
                    VectorStatusRow("Recommended ZIP", r.asset?.name ?: "Manual selection needed")
                    Text(r.note, style = MaterialTheme.typography.bodySmall)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    val releasePageUrl = release?.htmlUrl ?: "https://github.com/JingMatrix/Vector/releases/latest"
                    OutlinedButton(
                        onClick = { openUrl(context, releasePageUrl) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Open official release page")
                    }
                    Button(
                        onClick = {
                            r.asset?.let { asset ->
                                downloadVectorZip(context, asset)
                                downloadMessage = "Download started. Open Magisk > Modules > Install from storage > select ${asset.name}."
                            }
                        },
                        enabled = r.asset?.browserDownloadUrl?.isNotBlank() == true,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Download recommended ZIP")
                    }
                    downloadMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Short setup guide", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                listOf(
                    "Open Magisk.",
                    "Enable Zygisk in Magisk settings.",
                    "Reboot.",
                    "Download the recommended Vector Zygisk ZIP.",
                    "Open Magisk > Modules > Install from storage.",
                    "Select the Vector ZIP.",
                    "Reboot.",
                    "Open Vector/LSPosed manager from notification or launcher shortcut if available.",
                    "Install LSPosed/Xposed modules only from trusted sources.",
                    "Enable modules only for the apps they need to affect.",
                ).forEachIndexed { index, text -> Text("${index + 1}. $text", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun WarningCard() {
    ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Important", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Only install Vector from the official JingMatrix/Vector GitHub releases. Do not install random LSPosed APKs or ZIPs from mirror sites. Install one module at a time and reboot after each module.", style = MaterialTheme.typography.bodySmall)
            Text("RootDeck does not install Magisk modules automatically. It only checks your environment and guides you to the official release.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun VectorStatusRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.labelMedium)
        Text(value, modifier = Modifier.weight(0.6f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun downloadVectorZip(context: Context, asset: VectorAsset) {
    val request = DownloadManager.Request(Uri.parse(asset.browserDownloadUrl))
        .setTitle(asset.name)
        .setDescription("Vector, formerly LSPosed, official Zygisk module ZIP")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, asset.name)
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)
}
