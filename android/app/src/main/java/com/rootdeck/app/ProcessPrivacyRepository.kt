package com.rootdeck.app

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository for Process Privacy Guard. Persists per-package policies in
 * SharedPreferences and runs commands through [RootRepository] so every command
 * goes through the same confirmation + logging path.
 *
 * No commands run automatically. Every public method that touches root expects
 * the caller to have already shown a SafetyConfirmDialog.
 */
class ProcessPrivacyRepository(
    private val context: Context,
    private val root: RootRepository,
    private val scope: CoroutineScope,
) {
    private val prefs = context.getSharedPreferences("rootdeck.privacy", Context.MODE_PRIVATE)

    /** package -> policy. */
    val policies = mutableStateMapOf<String, ProcessPrivacyPolicy>()
    val processes = mutableStateListOf<RunningProcessInfo>()
    val lastActionResult = mutableStateOf<CommandResult?>(null)
    val processesLoading = mutableStateOf(false)

    init {
        loadFromDisk()
    }

    val activePoliciesCount: Int get() = policies.values.count { it.enabled }

    fun policyFor(pkg: String, fallbackLabel: String): ProcessPrivacyPolicy =
        policies[pkg] ?: ProcessPrivacyPolicy.defaultFor(pkg, fallbackLabel)

    /** Called when the user opens the screen — pure log entry, no root I/O. */
    fun onScreenOpened() {
        root.log("Process Privacy Guard", "Opened.")
    }

    /**
     * Apply the user-confirmed policy. Each command runs sequentially; results
     * are appended to [RootRepository.logs] via [runConfirmedCommandSuspend].
     */
    fun applyPolicy(policy: ProcessPrivacyPolicy) {
        val enabledPolicy = policy.copy(enabled = true)
        persist(enabledPolicy)
        if (root.rootMode.value != RootMode.ROOTDECK_ONLY) {
            root.log(
                "Process Privacy Guard",
                "Saved policy for ${policy.packageName} but root mode is disabled; no commands run.",
                isError = true,
            )
            return
        }
        scope.launch {
            val pkg = policy.packageName
            root.log("Process Privacy Guard", "Applying policy to $pkg")
            if (policy.forceStopOnEnable) runConfirmed("Force stop $pkg", "am force-stop $pkg")
            if (policy.restrictBackground) {
                runConfirmed("Restrict RUN_IN_BACKGROUND for $pkg",
                    "cmd appops set $pkg RUN_IN_BACKGROUND ignore")
                runConfirmed("Restrict RUN_ANY_IN_BACKGROUND for $pkg",
                    "cmd appops set $pkg RUN_ANY_IN_BACKGROUND ignore")
            }
            if (policy.restrictWakeLocks) {
                runConfirmed("Restrict WAKE_LOCK for $pkg",
                    "cmd appops set $pkg WAKE_LOCK ignore")
            }
            if (policy.disableForCurrentUser) {
                runConfirmed("Disable $pkg for current user",
                    "pm disable-user --user 0 $pkg")
            }
        }
    }

    /** Restore defaults for [pkg] and mark the policy disabled. */
    fun restoreDefaults(policy: ProcessPrivacyPolicy) {
        val restored = policy.copy(enabled = false)
        persist(restored)
        if (root.rootMode.value != RootMode.ROOTDECK_ONLY) {
            root.log(
                "Process Privacy Guard",
                "Cleared policy for ${policy.packageName} locally; root mode is disabled so no restore commands run.",
                isError = true,
            )
            return
        }
        scope.launch {
            val pkg = policy.packageName
            root.log("Process Privacy Guard", "Restoring defaults for $pkg")
            if (policy.restrictBackground) {
                runConfirmed("Restore RUN_IN_BACKGROUND for $pkg",
                    "cmd appops set $pkg RUN_IN_BACKGROUND allow")
                runConfirmed("Restore RUN_ANY_IN_BACKGROUND for $pkg",
                    "cmd appops set $pkg RUN_ANY_IN_BACKGROUND allow")
            }
            if (policy.restrictWakeLocks) {
                runConfirmed("Restore WAKE_LOCK for $pkg",
                    "cmd appops set $pkg WAKE_LOCK allow")
            }
            if (policy.disableForCurrentUser) {
                runConfirmed("Re-enable $pkg", "pm enable $pkg")
            }
        }
    }

    /** Force stop right now without changing the persisted policy. */
    fun forceStopNow(pkg: String) {
        if (root.rootMode.value != RootMode.ROOTDECK_ONLY) {
            root.log("Process Privacy Guard", "Force stop refused: root mode disabled.", isError = true)
            return
        }
        scope.launch { runConfirmed("Force stop $pkg", "am force-stop $pkg") }
    }

    /** Refresh `ps -A` snapshot. Requires root mode to be on. */
    fun refreshProcesses() {
        if (root.rootMode.value != RootMode.ROOTDECK_ONLY) {
            root.log("Process Privacy Guard", "ps -A skipped: root mode disabled.", isError = true)
            return
        }
        if (processesLoading.value) return
        processesLoading.value = true
        scope.launch {
            root.log("Process Privacy Guard", "Listing processes via ps -A")
            val result = RootShell.runRootCommand("ps -A")
            lastActionResult.value = result
            root.log(
                "ps -A",
                "exit=${result.exitCode}\n${result.stdout.take(400).ifBlank { result.stderr.take(400) }}",
                isError = !result.success,
            )
            val parsed = withContext(Dispatchers.Default) { parseProcessList(result.stdout) }
            processes.clear()
            processes.addAll(parsed)
            processesLoading.value = false
        }
    }

    fun runningProcessesFor(pkg: String): List<RunningProcessInfo> =
        processes.filter { it.packageOrCommand.startsWith(pkg) }

    private suspend fun runConfirmed(title: String, command: String) {
        val result = RootShell.runRootCommand(command)
        lastActionResult.value = result
        root.log(
            title,
            "$ $command\nexit=${result.exitCode}\n" +
                (result.stdout.ifBlank { result.stderr }).take(400),
            isError = !result.success,
        )
    }

    private fun persist(policy: ProcessPrivacyPolicy) {
        policies[policy.packageName] = policy
        saveAll()
    }

    private fun saveAll() {
        val arr = JSONArray()
        policies.values.forEach { p ->
            arr.put(
                JSONObject()
                    .put("pkg", p.packageName)
                    .put("label", p.label)
                    .put("forceStop", p.forceStopOnEnable)
                    .put("restrictBackground", p.restrictBackground)
                    .put("restrictWakeLocks", p.restrictWakeLocks)
                    .put("disable", p.disableForCurrentUser)
                    .put("enabled", p.enabled)
            )
        }
        prefs.edit().putString("policies", arr.toString()).apply()
    }

    private fun loadFromDisk() {
        val raw = prefs.getString("policies", null) ?: return
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val p = ProcessPrivacyPolicy(
                    packageName = o.getString("pkg"),
                    label = o.optString("label", o.getString("pkg")),
                    forceStopOnEnable = o.optBoolean("forceStop", true),
                    restrictBackground = o.optBoolean("restrictBackground", true),
                    restrictWakeLocks = o.optBoolean("restrictWakeLocks", false),
                    disableForCurrentUser = o.optBoolean("disable", false),
                    enabled = o.optBoolean("enabled", false),
                )
                policies[p.packageName] = p
            }
        }
    }

    companion object {
        /** Parse `ps -A` output. Header line is dropped. */
        fun parseProcessList(stdout: String): List<RunningProcessInfo> {
            val lines = stdout.lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toList()
            if (lines.isEmpty()) return emptyList()
            val data = if (lines.first().startsWith("USER", ignoreCase = true)) lines.drop(1) else lines
            return data.mapNotNull { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 9) return@mapNotNull null
                RunningProcessInfo(
                    user = parts[0],
                    pid = parts[1],
                    packageOrCommand = parts.last(),
                    rawLine = line,
                )
            }
        }
    }
}
