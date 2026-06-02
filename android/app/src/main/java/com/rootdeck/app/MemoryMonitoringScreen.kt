package com.rootdeck.app

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MemoryMonitoringScreen(repo: RootRepository, onBack: () -> Unit) {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(MemorySnapshot.read(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to tools") }
        Text("Memory Monitoring", style = MaterialTheme.typography.headlineSmall)

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { snapshot = MemorySnapshot.read(context) }) { Text("Refresh") }
        }

        Stat("RAM total", snapshot.ramTotalGb)
        Stat("RAM available", snapshot.ramAvailGb)
        Stat("RAM low?", snapshot.lowMem.toString())
        Stat("Storage total", snapshot.storageTotalGb)
        Stat("Storage free", snapshot.storageFreeGb)
        Stat("CPU cores", snapshot.cpuCores.toString())
        Stat("Running app processes", snapshot.runningAppCount.toString())

        if (repo.rootMode.value == RootMode.ROOTDECK_ONLY) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Root-enhanced details", style = MaterialTheme.typography.titleSmall)
                    Text("Tap to run `cat /proc/meminfo` through your root provider.",
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        repo.runConfirmedCommand("Read /proc/meminfo", "cat /proc/meminfo")
                    }) { Text("Run cat /proc/meminfo") }
                    val last = repo.lastResult.value
                    if (last != null && last.command == "cat /proc/meminfo") {
                        Spacer(Modifier.height(8.dp))
                        Text(last.stdout.take(800),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

private data class MemorySnapshot(
    val ramTotalGb: String,
    val ramAvailGb: String,
    val storageTotalGb: String,
    val storageFreeGb: String,
    val cpuCores: Int,
    val runningAppCount: Int,
    val lowMem: Boolean,
) {
    companion object {
        fun read(context: Context): MemorySnapshot {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
            val stat = runCatching { StatFs(Environment.getDataDirectory().path) }.getOrNull()
            val total = stat?.let { it.blockCountLong * it.blockSizeLong } ?: 0L
            val free = stat?.let { it.availableBlocksLong * it.blockSizeLong } ?: 0L
            val running = runCatching { am.runningAppProcesses?.size ?: 0 }.getOrDefault(0)
            return MemorySnapshot(
                ramTotalGb = "${gb(mi.totalMem)} GB",
                ramAvailGb = "${gb(mi.availMem)} GB",
                storageTotalGb = "${gb(total)} GB",
                storageFreeGb = "${gb(free)} GB",
                cpuCores = Runtime.getRuntime().availableProcessors(),
                runningAppCount = running,
                lowMem = mi.lowMemory,
            )
        }

        private fun gb(b: Long): String = "%.1f".format(b / 1024.0 / 1024.0 / 1024.0)
    }
}
