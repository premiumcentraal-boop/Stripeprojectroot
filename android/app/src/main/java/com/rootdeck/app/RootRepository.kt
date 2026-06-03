package com.rootdeck.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Known root manager packages, in priority order. */
private val knownRootManagers = listOf(
    "com.topjohnwu.magisk" to "Magisk",
    "me.weishu.kernelsu" to "KernelSU",
    "me.bmax.apatch" to "APatch",
)

/**
 * Central app state + persistence: root mode, per-app allowlist, settings, log.
 * Persisted in SharedPreferences.
 */
class RootRepository(private val app: android.app.Application) : ViewModel() {

    private val prefs: SharedPreferences =
        app.getSharedPreferences("rootdeck.prefs", Context.MODE_PRIVATE)

    // --- Settings ---
    val advancedMode = mutableStateOf(prefs.getBoolean(KEY_ADVANCED, false))
    val showCommandPreviews = mutableStateOf(prefs.getBoolean(KEY_PREVIEWS, true))
    val theme = mutableStateOf(
        runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.Dark.name)!!) }
            .getOrDefault(ThemeMode.Dark)
    )

    // --- Root state ---
    val rootMode = mutableStateOf(
        runCatching { RootMode.valueOf(prefs.getString(KEY_ROOT_MODE, RootMode.DISABLED.name)!!) }
            .getOrDefault(RootMode.DISABLED)
    )
    val rootStatus = mutableStateOf(RootStatus.Unknown)
    val lastResult = mutableStateOf<CommandResult?>(null)

    // --- Installed apps + RootDeck-internal allowlist ---
    val apps = mutableStateListOf<ManagedApp>()
    val appsLoading = mutableStateOf(false)
    private val allowedPackages: MutableSet<String> =
        prefs.getStringSet(KEY_ALLOWED, emptySet())!!.toMutableSet()

    // --- Logs ---
    val logs = mutableStateListOf<LogEntry>()

    fun setAdvancedMode(v: Boolean) {
        advancedMode.value = v
        prefs.edit().putBoolean(KEY_ADVANCED, v).apply()
        log("Setting changed", "Advanced mode = $v")
    }

    fun setShowCommandPreviews(v: Boolean) {
        showCommandPreviews.value = v
        prefs.edit().putBoolean(KEY_PREVIEWS, v).apply()
    }

    fun setTheme(t: ThemeMode) {
        theme.value = t
        prefs.edit().putString(KEY_THEME, t.name).apply()
    }

    /**
     * Toggle RootDeck's global root mode. When enabling, we probe `su -c id`.
     * The actual permission grant happens inside the user's root manager, not here.
     */
    fun setGlobalRootEnabled(enabled: Boolean, onResult: (RootStatus) -> Unit = {}) {
        if (!enabled) {
            rootMode.value = RootMode.DISABLED
            persistRootMode()
            val s = RootStatus(
                isRootAvailable = rootStatus.value.isRootAvailable,
                isRootGranted = false,
                providerName = rootStatus.value.providerName,
                message = "RootDeck root mode disabled.",
            )
            rootStatus.value = s
            log("Root mode", "Disabled by user.")
            onResult(s)
            return
        }

        viewModelScope.launch {
            val result = RootShell.checkRoot()
            lastResult.value = result
            val provider = detectProvider()
            val status = if (RootShell.isRootActive(result)) {
                rootMode.value = RootMode.ROOTDECK_ONLY
                persistRootMode()
                RootStatus(
                    isRootAvailable = true,
                    isRootGranted = true,
                    providerName = provider,
                    message = "Root active for RootDeck (uid=0 via ${provider ?: "su"}).",
                )
            } else {
                rootMode.value = RootMode.DISABLED
                persistRootMode()
                RootStatus(
                    isRootAvailable = provider != null || result.exitCode != -1,
                    isRootGranted = false,
                    providerName = provider,
                    message = "Root not granted or device not rooted.",
                )
            }
            rootStatus.value = status
            log(
                title = "su -c id",
                detail = "exit=${result.exitCode} ${result.stdout.trim().ifBlank { result.stderr.trim() }}",
                isError = !status.isRootGranted,
            )
            onResult(status)
        }
    }

    /**
     * Run a root command through the user's root provider. The caller MUST have
     * already shown a confirmation dialog.
     */
    fun runConfirmedCommand(
        title: String,
        command: String,
        timeoutSeconds: Long = 10,
        onDone: (CommandResult) -> Unit = {},
    ) {
        if (rootMode.value != RootMode.ROOTDECK_ONLY) {
            val r = CommandResult(command, -1, "", "RootDeck global root mode is disabled.",
                System.currentTimeMillis())
            lastResult.value = r
            log(title, "Refused: global root mode disabled.", isError = true)
            onDone(r)
            return
        }
        viewModelScope.launch {
            val result = RootShell.runRootCommand(command, timeoutSeconds = timeoutSeconds)
            lastResult.value = result
            log(
                title = title,
                detail = "$ $command\nexit=${result.exitCode}\n${result.stdout.ifBlank { result.stderr }}".trim(),
                isError = !result.success,
            )
            onDone(result)
        }
    }

    fun isAppAllowed(pkg: String): Boolean = allowedPackages.contains(pkg)

    fun setAppAllowed(pkg: String, allowed: Boolean) {
        if (allowed) allowedPackages.add(pkg) else allowedPackages.remove(pkg)
        prefs.edit().putStringSet(KEY_ALLOWED, allowedPackages).apply()
        val idx = apps.indexOfFirst { it.packageName == pkg }
        if (idx >= 0) apps[idx] = apps[idx].copy(rootActionsAllowedInsideRootDeck = allowed)
        log("Per-app policy", "$pkg → ${if (allowed) "allowed inside RootDeck" else "disallowed"}")
    }

    fun loadInstalledApps() {
        if (appsLoading.value) return
        appsLoading.value = true
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { queryInstalledApps() }
            apps.clear()
            apps.addAll(list)
            appsLoading.value = false
        }
    }

    private fun queryInstalledApps(): List<ManagedApp> {
        val pm = app.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .map {
                ManagedApp(
                    packageName = it.packageName,
                    label = pm.getApplicationLabel(it).toString(),
                    isSystemApp = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    rootActionsAllowedInsideRootDeck = allowedPackages.contains(it.packageName),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /** Detect the first installed known root manager, if any. */
    fun detectProvider(): String? {
        val pm = app.packageManager
        for ((pkg, label) in knownRootManagers) {
            try {
                pm.getPackageInfo(pkg, 0)
                return label
            } catch (_: PackageManager.NameNotFoundException) {
                // continue
            }
        }
        return null
    }

    /** Launch the installed root manager's UI, or return false if none is installed. */
    fun openRootManager(context: Context): Boolean {
        val pm = context.packageManager
        for ((pkg, _) in knownRootManagers) {
            val intent = pm.getLaunchIntentForPackage(pkg) ?: continue
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            log("Open root manager", "Launched $pkg")
            return true
        }
        log("Open root manager", "No supported root manager detected.", isError = true)
        return false
    }

    fun clearLogs() {
        logs.clear()
        log("Logs", "Log cleared.")
    }

    fun log(title: String, detail: String, isError: Boolean = false) {
        logs.add(0, LogEntry(System.currentTimeMillis(), title, detail, isError))
    }

    private fun persistRootMode() {
        prefs.edit().putString(KEY_ROOT_MODE, rootMode.value.name).apply()
    }

    companion object {
        private const val KEY_ADVANCED = "advanced_mode"
        private const val KEY_PREVIEWS = "show_previews"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_ROOT_MODE = "root_mode"
        private const val KEY_ALLOWED = "allowed_packages"

        fun factory(application: android.app.Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RootRepository(application) as T
            }
    }
}
