package com.rootdeck.app

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private const val LSPOSED_RELEASE_API = "https://api.github.com/repos/LSPosed/LSPosed/releases/latest"

@Composable
fun LsposedInstallerScreen(repo: RootRepository, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(LsposedInstallState()) }

    fun refresh() {
        scope.launch {
            state = state.copy(busy = true, message = "Checking Magisk and LSPosed release…", error = null)
            state = LsposedInstaller.check(context, repo)
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        Text("LSPosed Installer", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Downloads the matching LSPosed module build and installs it through Magisk after confirmation.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Security, contentDescription = null)
                    Text("Device compatibility", style = MaterialTheme.typography.titleSmall)
                }
                StatusLine("Root mode", if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) "Enabled" else "Disabled")
                StatusLine("Root provider", repo.detectProvider() ?: "Not detected")
                StatusLine("Magisk", state.magiskVersion ?: "Not detected yet")
                StatusLine("Android", "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
                StatusLine("CPU ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown")
                StatusLine("Selected LSPosed", state.releaseName ?: "Not selected yet")
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Download, contentDescription = null)
                    Text("Install module", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    state.message ?: "Ready to check release metadata.",
                    style = MaterialTheme.typography.bodySmall,
                )
                state.assetUrl?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { refresh() }, enabled = !state.busy) { Text("Refresh") }
                    Button(
                        enabled = !state.busy && state.assetUrl != null && repo.rootMode.value == RootMode.ROOTDECK_ONLY,
                        onClick = {
                            scope.launch {
                                state = state.copy(busy = true, message = "Downloading LSPosed module…", error = null)
                                val installState = LsposedInstaller.downloadAndInstall(context, repo, state)
                                state = installState
                            }
                        },
                    ) {
                        Text(if (state.busy) "Working…" else "Download & Install")
                    }
                }
                if (repo.rootMode.value != RootMode.ROOTDECK_ONLY) {
                    Text(
                        "Enable RootDeck root mode first in Root Management. Magisk will still show its own root prompt.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    "After installation, reboot the device and enable LSPosed from its manager/notification if required.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(0.42f), style = MaterialTheme.typography.labelMedium)
        Text(value, modifier = Modifier.weight(0.58f), style = MaterialTheme.typography.bodySmall)
    }
}

data class LsposedInstallState(
    val busy: Boolean = false,
    val magiskVersion: String? = null,
    val releaseName: String? = null,
    val assetUrl: String? = null,
    val downloadedFile: String? = null,
    val message: String? = null,
    val error: String? = null,
)

object LsposedInstaller {
    suspend fun check(context: Context, repo: RootRepository): LsposedInstallState = withContext(Dispatchers.IO) {
        val magisk = RootShell.runRootCommand("magisk -V 2>/dev/null || magisk -v 2>/dev/null", timeoutSeconds = 8)
        val magiskVersion = magisk.stdout.trim().ifBlank { magisk.stderr.trim() }.ifBlank { null }
        val preferZygisk = parseMagiskVersionCode(magiskVersion) >= 24_000
        val release = runCatching { fetchLatestRelease(preferZygisk) }.getOrElse { error ->
            return@withContext LsposedInstallState(
                busy = false,
                magiskVersion = magiskVersion,
                message = "Could not read official LSPosed release metadata.",
                error = error.message ?: "Unknown release lookup error",
            )
        }
        LsposedInstallState(
            busy = false,
            magiskVersion = magiskVersion,
            releaseName = release.first,
            assetUrl = release.second,
            message = if (magisk.success) {
                if (preferZygisk) "Magisk detected. Selected LSPosed Zygisk build for this Magisk version." else "Magisk detected. Selected LSPosed Riru build for this older Magisk version."
            } else {
                "Magisk command was not detected through root. Install Magisk or enable RootDeck root mode first."
            },
            error = if (magisk.success) null else magisk.stderr.ifBlank { "Magisk not detected." },
        )
    }

    suspend fun downloadAndInstall(
        context: Context,
        repo: RootRepository,
        current: LsposedInstallState,
    ): LsposedInstallState = withContext(Dispatchers.IO) {
        val url = current.assetUrl ?: return@withContext current.copy(busy = false, error = "No LSPosed asset selected.")
        val out = File(context.getExternalFilesDir(null) ?: context.cacheDir, "lsposed-magisk-module.zip")
        runCatching { download(url, out) }.getOrElse { error ->
            return@withContext current.copy(busy = false, error = "Download failed: ${error.message}")
        }
        val installCommand = "magisk --install-module '${out.absolutePath}'"
        var resultState = current.copy(
            busy = false,
            downloadedFile = out.absolutePath,
            message = "Downloaded. Waiting for Magisk install result…",
        )
        val done = kotlinx.coroutines.CompletableDeferred<CommandResult>()
        repo.runConfirmedCommand("Install LSPosed Magisk module", installCommand, timeoutSeconds = 120) { done.complete(it) }
        val result = done.await()
        resultState = if (result.success) {
            current.copy(
                busy = false,
                downloadedFile = out.absolutePath,
                message = "LSPosed module installed through Magisk. Reboot is required.",
                error = null,
            )
        } else {
            current.copy(
                busy = false,
                downloadedFile = out.absolutePath,
                message = "Downloaded to ${out.absolutePath}",
                error = "Magisk install failed: ${result.stderr.ifBlank { result.stdout }}",
            )
        }
        resultState
    }

    private fun parseMagiskVersionCode(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        return raw.lineSequence()
            .flatMap { line -> Regex("\\d+").findAll(line).map { it.value } }
            .mapNotNull { it.toIntOrNull() }
            .firstOrNull() ?: 0
    }

    private fun fetchLatestRelease(preferZygisk: Boolean): Pair<String, String> {
        val text = httpGet(LSPOSED_RELEASE_API)
        val json = JSONObject(text)
        val name = json.optString("name", json.optString("tag_name", "Latest LSPosed"))
        val assets = json.getJSONArray("assets")
        var fallbackZip: String? = null
        var zygiskZip: String? = null
        var riruZip: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val assetName = asset.optString("name")
            val downloadUrl = asset.optString("browser_download_url")
            if (assetName.endsWith(".zip", ignoreCase = true)) {
                val lower = assetName.lowercase()
                if (fallbackZip == null) fallbackZip = downloadUrl
                if ("zygisk" in lower && "riru" !in lower) zygiskZip = downloadUrl
                if ("riru" in lower) riruZip = downloadUrl
            }
        }
        val selected = if (preferZygisk) {
            zygiskZip ?: fallbackZip
        } else {
            riruZip ?: zygiskZip ?: fallbackZip
        }
        return name to (selected ?: error("No installable LSPosed zip asset was found in the latest release."))
    }

    private fun download(url: String, out: File) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 20_000
        connection.readTimeout = 60_000
        connection.setRequestProperty("User-Agent", "RootDeck")
        connection.inputStream.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun httpGet(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 20_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "RootDeck")
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}
