package com.rootdeck.app

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    repo: RootRepository,
    privacy: ProcessPrivacyRepository,
    onOpenRootManagement: () -> Unit,
    onOpenProcessPrivacy: () -> Unit,
    onOpenBasicTools: () -> Unit = {},
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("RootDeck", style = MaterialTheme.typography.headlineSmall)
        AssistChip(onClick = {}, label = { Text("App version v0.19.0 · build 19") })
        SafetyPolicyLink()

        StatusCard(
            "Global root mode",
            when (repo.rootMode.value) {
                RootMode.DISABLED -> "Disabled — RootDeck will not run root commands."
                RootMode.ROOTDECK_ONLY -> "ROOTDECK_ONLY — RootDeck may run confirmed commands."
            },
        )
        StatusCard("Root status", repo.rootStatus.value.message)
        StatusCard("Root provider", repo.rootStatus.value.providerName
            ?: repo.detectProvider() ?: "Not detected")
        StatusCard("App version", "RootDeck v0.19.0 · Android versionCode 19")
        StatusCard("Android", "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}")
        StatusCard("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
        StatusCard("Storage", storageLabel())
        StatusCard("CPU / RAM", cpuRamLabel(context))
        StatusCard(
            "Last command",
            repo.lastResult.value?.let { "$ ${it.command}\nexit=${it.exitCode}" }
                ?: "No commands run yet.",
            monospace = true,
        )

        Button(onClick = onOpenRootManagement, modifier = Modifier.fillMaxWidth()) {
            Text("Open Root Management")
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Process Privacy Guard", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("Active policies: ${privacy.activePoliciesCount}",
                    style = MaterialTheme.typography.bodySmall)
                Text(
                    "Last action: " + (privacy.lastActionResult.value?.let {
                        "$ ${it.command} (exit=${it.exitCode})"
                    } ?: "None yet."),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onOpenProcessPrivacy) { Text("Open") }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Basic Tools Roadmap", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("${plannedBasicTools.size} planned modules added",
                    style = MaterialTheme.typography.bodySmall)
                Text("No commands enabled", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onOpenBasicTools) { Text("View Basic Tools") }
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, body: String, monospace: Boolean = false) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            )
        }
    }
}

private fun storageLabel(): String = try {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.blockCountLong * stat.blockSizeLong
    val free = stat.availableBlocksLong * stat.blockSizeLong
    "${(total - free).toGB()} / ${total.toGB()} GB used"
} catch (t: Throwable) { "Unavailable" }

private fun cpuRamLabel(context: Context): String = try {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    "${Runtime.getRuntime().availableProcessors()} cores · ${mi.availMem.toGB()} / ${mi.totalMem.toGB()} GB free"
} catch (t: Throwable) { "Unavailable" }

private fun Long.toGB(): String = "%.1f".format(this / 1024.0 / 1024.0 / 1024.0)
