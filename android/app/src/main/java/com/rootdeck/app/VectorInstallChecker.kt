package com.rootdeck.app

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VectorInstallStatus(
    val rootAvailable: Boolean,
    val magiskDetected: Boolean,
    val magiskVersion: String?,
    val zygiskEnabled: Boolean?,
    val androidSdk: Int,
    val androidRelease: String,
    val deviceCodename: String?,
    val buildId: String?,
    val vectorManagerDetected: Boolean?,
    val vectorModuleDetected: Boolean?,
)

object VectorInstallChecker {
    suspend fun check(): VectorInstallStatus = withContext(Dispatchers.IO) {
        val rootOutput = shell("su -c id")
        val rootAvailable = rootOutput.contains("uid=0")
        val magiskVersion = shell("su -c magisk -v")
            .ifBlank { shell("su -c magisk -V") }
            .trim()
            .ifBlank { null }
        val zygiskRaw = shell("su -c \"magisk --sqlite 'SELECT value FROM settings WHERE key=\\\"zygisk\\\";'\"")
        val packages = shell("pm list packages")
        val managerDetected = detectVectorManager(packages)
        val moduleDetected = if (rootAvailable) detectVectorModule() else null

        VectorInstallStatus(
            rootAvailable = rootAvailable,
            magiskDetected = magiskVersion != null,
            magiskVersion = magiskVersion,
            zygiskEnabled = parseZygisk(zygiskRaw),
            androidSdk = Build.VERSION.SDK_INT,
            androidRelease = Build.VERSION.RELEASE ?: "Unknown",
            deviceCodename = getProp("ro.product.device"),
            buildId = getProp("ro.build.id"),
            vectorManagerDetected = managerDetected,
            vectorModuleDetected = moduleDetected,
        )
    }

    private fun detectVectorManager(packages: String): Boolean? {
        if (packages.isBlank()) return null
        val lower = packages.lowercase()
        return listOf(
            "org.lsposed.manager",
            "io.github.libxposed.manager",
            "lsposed",
            "vector",
        ).any { lower.contains(it) }
    }

    private fun detectVectorModule(): Boolean? {
        val moduleList = shell("su -c 'ls -1 /data/adb/modules /data/adb/modules_update 2>/dev/null'")
        if (moduleList.isBlank()) return null
        val lower = moduleList.lowercase()
        return listOf("lsposed", "vector", "zygisk").any { lower.contains(it) }
    }

    private fun parseZygisk(raw: String): Boolean? = when {
        Regex("(^|\\D)1($|\\D)").containsMatchIn(raw) -> true
        Regex("(^|\\D)0($|\\D)").containsMatchIn(raw) -> false
        else -> null
    }

    private fun getProp(key: String): String? = shell("getprop $key").trim().ifBlank { null }

    private fun shell(command: String): String = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val out = process.inputStream.bufferedReader().readText()
        val err = process.errorStream.bufferedReader().readText()
        process.waitFor()
        (out + err).trim()
    }.getOrDefault("")
}
