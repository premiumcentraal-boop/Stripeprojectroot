package com.rootdeck.app

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Environment status used by the Vector / LSPosed setup assistant.
data class RootEnvironment(
    val androidRelease: String,
    val sdkInt: Int,
    val deviceModel: String,
    val productDevice: String?,
    val buildId: String?,
    val cpuAbi: String?,
    val rootAvailable: Boolean,
    val magiskDetected: Boolean?,
    val magiskVersion: String?,
    val zygiskEnabled: Boolean?,
    val bootloaderUnlocked: Boolean?,
)

object RootEnvironmentDetector {
    suspend fun detect(): RootEnvironment = withContext(Dispatchers.IO) {
        val root = shell("su -c id")
        val rootAvailable = root.contains("uid=0")
        val magiskVersionText = shell("su -c magisk -v").ifBlank { shell("su -c magisk -V") }.trim().ifBlank { null }
        val zygiskRaw = shell("su -c \"magisk --sqlite 'SELECT value FROM settings WHERE key=\\\"zygisk\\\";'\"").trim()
        val flashLocked = getProp("ro.boot.flash.locked")
        val verifiedState = getProp("ro.boot.verifiedbootstate")
        RootEnvironment(
            androidRelease = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            deviceModel = listOf(Build.MANUFACTURER, Build.MODEL).filter { it.isNotBlank() }.joinToString(" "),
            productDevice = getProp("ro.product.device"),
            buildId = getProp("ro.build.id"),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull(),
            rootAvailable = rootAvailable,
            magiskDetected = when {
                magiskVersionText != null -> true
                rootAvailable -> false
                else -> null
            },
            magiskVersion = magiskVersionText,
            zygiskEnabled = when {
                Regex("(^|\\D)1($|\\D)").containsMatchIn(zygiskRaw) -> true
                Regex("(^|\\D)0($|\\D)").containsMatchIn(zygiskRaw) -> false
                else -> null
            },
            bootloaderUnlocked = when {
                flashLocked == "0" || verifiedState.equals("orange", ignoreCase = true) -> true
                flashLocked == "1" || verifiedState.equals("green", ignoreCase = true) -> false
                else -> null
            },
        )
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
