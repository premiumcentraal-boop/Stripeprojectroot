package com.rootdeck.app

import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WarningAmber
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
    var downloadProgress by remember { mutableStateOf<Float?>(null) }

    fun refresh() {
        scope.launch {
            downloadProgress = null
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
                                downloadProgress = 0f
                                state = state.copy(busy = true, message = "Downloading LSPosed module…", error = null)
                                val installState = LsposedInstaller.downloadAndInstall(
                                    context = context,
                                    repo = repo,
                                    current = state,
                                    onProgress = { downloadProgress = it },
                                )
                                state = installState
                            }
                        },
                    ) {
                        Text(if (state.busy) "Working…" else "Download & Install")
                    }
                }
                val progress = downloadProgress
                if (state.busy) {
                    if (progress != null && progress > 0f) {
                        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                        Text("Download progress: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    } else {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("Working… RootDeck will show a success or failure result here.", style = MaterialTheme.typography.labelSmall)
                    }
                }
                InstallResultGuide(state = state, rootEnabled = repo.rootMode.value == RootMode.ROOTDECK_ONLY)
                if (repo.rootMode.value != RootMode.ROOTDECK_ONLY) {
                    Text(
                        "Root access is not enabled yet. Go to Root Management, tap Allow Root Access, then come back here.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallResultGuide(state: LsposedInstallState, rootEnabled: Boolean) {
    val installed = state.downloadedFile != null && state.error == null && !state.busy
    val failed = state.error != null && !state.busy
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    when {
                        installed -> Icons.Outlined.CheckCircle
                        failed -> Icons.Outlined.WarningAmber
                        else -> Icons.Outlined.Download
                    },
                    contentDescription = null,
                    tint = when {
                        installed -> MaterialTheme.colorScheme.primary
                        failed -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    when {
                        installed -> "Install completed — reboot required"
                        failed -> "Install failed — see reason above"
                        rootEnabled -> "Ready to install"
                        else -> "Root access needed first"
                    },
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            SetupIconRow(current = if (installed) 3 else if (state.busy) 1 else 0)
            if (installed) {
                NumberedSteps(
                    listOf(
                        "Restart your phone now.",
                        "Open LSPosed Manager or tap the LSPosed notification after reboot.",
                        "Enable the RootDeck module and scope it only to your own test apps.",
                        "Open Video Test Feed and save your video, camera, crop, and resize setup.",
                    ),
                )
            } else if (failed) {
                NumberedSteps(
                    listOf(
                        "Confirm Magisk is installed and Superuser access is allowed for RootDeck.",
                        "Tap Refresh to scan again.",
                        "Tap Download & Install again and keep this screen open until the final result appears.",
                    ),
                )
            } else {
                NumberedSteps(
                    listOf(
                        "Refresh checks the correct LSPosed build for your Magisk version.",
                        "Download & Install downloads the ZIP and sends it to Magisk.",
                        "A success or failure result will stay visible on this screen.",
                    ),
                )
            }
        }
    }
}

@Composable
private fun SetupIconRow(current: Int) {
    val steps = listOf(
        Icons.Outlined.Download to "Download",
        Icons.Outlined.Security to "Magisk",
        Icons.Outlined.RestartAlt to "Reboot",
        Icons.Outlined.Verified to "Enable",
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        steps.forEachIndexed { index, step ->
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Icon(
                    step.first,
                    contentDescription = null,
                    tint = if (index <= current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(step.second, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun NumberedSteps(steps: List<String>) {
    steps.forEachIndexed { index, text ->
        Text("${index + 1}. $text", style = MaterialTheme.typography.bodySmall)
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
    val lsposedInstalled: Boolean = false,
    val rebootPending: Boolean = false,
    val lsposedManagerVersion: String? = null,
    val zygiskEnabled: Boolean = false,
)

object LsposedInstaller {
    private const val SETUP_PREFS = "rootdeck_lsposed_setup"
    private const val KEY_INSTALL_PENDING_REBOOT = "install_pending_reboot"
    private const val KEY_SETUP_COMPLETE = "setup_complete"

    fun installedLsposedManagerVersion(context: Context): String? {
        val possibleManagerPackages = listOf("org.lsposed.manager", "org.lsposed.manager.debug")
        return possibleManagerPackages.firstNotNullOfOrNull { packageName ->
            runCatching {
                val info = context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                info.versionName ?: packageName
            }.getOrNull()
        }
    }

    fun isLsposedManagerInstalled(context: Context): Boolean = installedLsposedManagerVersion(context) != null

    fun markSetupComplete(context: Context) {
        context.getSharedPreferences(SETUP_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .putBoolean(KEY_INSTALL_PENDING_REBOOT, false)
            .apply()
    }

    fun markInstallPendingReboot(context: Context) {
        context.getSharedPreferences(SETUP_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_INSTALL_PENDING_REBOOT, true)
            .putBoolean(KEY_SETUP_COMPLETE, false)
            .apply()
    }

    fun isInstallPendingReboot(context: Context): Boolean =
        context.getSharedPreferences(SETUP_PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_INSTALL_PENDING_REBOOT, false)

    fun shouldSkipStartupSetup(context: Context): Boolean {
        val prefs = context.getSharedPreferences(SETUP_PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }

    suspend fun check(context: Context, repo: RootRepository): LsposedInstallState = withContext(Dispatchers.IO) {
        val zygiskEnabled = isZygiskEnabled()
        val lsposedVersion = installedLsposedManagerVersion(context)
        val lsposedInstalled = lsposedVersion != null
        val rebootPending = isInstallPendingReboot(context) && !lsposedInstalled
        if (lsposedInstalled) {
            return@withContext LsposedInstallState(
                busy = false,
                message = "LSPosed is installed successfully.",
                lsposedInstalled = true,
                rebootPending = false,
                lsposedManagerVersion = lsposedVersion,
                zygiskEnabled = zygiskEnabled,
            )
        }
        val magisk = RootShell.runRootCommand("magisk -V 2>/dev/null || magisk -v 2>/dev/null", timeoutSeconds = 8)
        val magiskVersion = magisk.stdout.trim().ifBlank { magisk.stderr.trim() }.ifBlank { null }
        val preferZygisk = parseMagiskVersionCode(magiskVersion) >= 24_000
        val release = runCatching { fetchLatestRelease(preferZygisk) }.getOrElse { error ->
            return@withContext LsposedInstallState(
                busy = false,
                magiskVersion = magiskVersion,
                message = if (rebootPending) "LSPosed module install was sent to Magisk. Reboot is still needed." else "Could not read official LSPosed release metadata.",
                error = if (rebootPending) null else error.message ?: "Unknown release lookup error",
                rebootPending = rebootPending,
                zygiskEnabled = zygiskEnabled,
            )
        }
        LsposedInstallState(
            busy = false,
            magiskVersion = magiskVersion,
            releaseName = release.first,
            assetUrl = release.second,
            message = if (rebootPending) {
                "LSPosed install was completed in Magisk. Reboot the device once; after reboot RootDeck will detect LSPosed and skip this setup."
            } else if (magisk.success) {
                if (preferZygisk) "Magisk detected. Selected LSPosed Zygisk build for this Magisk version." else "Magisk detected. Selected LSPosed Riru build for this older Magisk version."
            } else {
                "Magisk command was not detected through root. Install Magisk or enable RootDeck root mode first."
            },
            error = if (magisk.success || rebootPending) null else magisk.stderr.ifBlank { "Magisk not detected." },
            rebootPending = rebootPending,
            zygiskEnabled = zygiskEnabled,
        )
    }

    suspend fun enableZygisk(repo: RootRepository): CommandResult = withContext(Dispatchers.IO) {
        val command = "magisk --sqlite \"REPLACE INTO settings (key,value) VALUES('zygisk',1);\""
        val done = kotlinx.coroutines.CompletableDeferred<CommandResult>()
        repo.runConfirmedCommand("Enable Magisk Zygisk", command, timeoutSeconds = 30) { done.complete(it) }
        done.await()
    }

    private fun isZygiskEnabled(): Boolean {
        val result = RootShell.runRootCommand(
            "magisk --sqlite \"SELECT value FROM settings WHERE key='zygisk';\" 2>/dev/null",
            timeoutSeconds = 8,
        )
        val raw = (result.stdout + "\n" + result.stderr).lowercase()
        return result.success && (Regex("(^|\\D)1($|\\D)").containsMatchIn(raw) || "true" in raw || "zygisk=1" in raw)
    }

    suspend fun downloadAndInstall(
        context: Context,
        repo: RootRepository,
        current: LsposedInstallState,
        onProgress: (Float) -> Unit = {},
    ): LsposedInstallState = withContext(Dispatchers.IO) {
        val url = current.assetUrl ?: return@withContext current.copy(busy = false, error = "No LSPosed asset selected.")
        val out = File(context.getExternalFilesDir(null) ?: context.cacheDir, "lsposed-magisk-module.zip")
        runCatching { download(url, out, onProgress) }.getOrElse { error ->
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
            markInstallPendingReboot(context)
            current.copy(
                busy = false,
                downloadedFile = out.absolutePath,
                message = "LSPosed module installed through Magisk. Reboot is required. After reboot RootDeck will detect LSPosed and skip this setup.",
                error = null,
                rebootPending = true,
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

    private suspend fun download(url: String, out: File, onProgress: (Float) -> Unit) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = 20_000
        connection.readTimeout = 60_000
        connection.setRequestProperty("User-Agent", "RootDeck")
        val total = connection.contentLengthLong.takeIf { it > 0L }
        var copied = 0L
        connection.inputStream.use { input ->
            out.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    copied += read
                    if (total != null) withContext(Dispatchers.Main) { onProgress((copied.toFloat() / total.toFloat()).coerceIn(0f, 1f)) }
                }
            }
        }
        withContext(Dispatchers.Main) { onProgress(1f) }
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
