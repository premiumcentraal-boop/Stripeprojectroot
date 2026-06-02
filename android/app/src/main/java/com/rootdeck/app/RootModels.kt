package com.rootdeck.app

/**
 * RootDeck data models. These describe RootDeck's *internal* policy state — they never
 * imply that RootDeck has granted real OS-level root permission to any third-party app.
 * Real root permission is controlled exclusively by the user's installed root manager
 * (Magisk, KernelSU, APatch, …).
 */

enum class RootMode {
    /** RootDeck will not attempt any root commands. */
    DISABLED,

    /** RootDeck itself may run root commands after explicit per-action confirmation. */
    ROOTDECK_ONLY,
}

enum class ThemeMode { Dark, Black, System }

/** Snapshot of the current root situation. */
data class RootStatus(
    val isRootAvailable: Boolean,
    val isRootGranted: Boolean,
    val providerName: String?,
    val message: String,
) {
    companion object {
        val Unknown = RootStatus(
            isRootAvailable = false,
            isRootGranted = false,
            providerName = null,
            message = "Root status not yet checked.",
        )
    }
}

/**
 * Represents an installed application as far as RootDeck's *internal* allowlist is
 * concerned. [rootActionsAllowedInsideRootDeck] only gates RootDeck's own actions —
 * it does not influence the OS root permission of the app itself.
 */
data class ManagedApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val rootActionsAllowedInsideRootDeck: Boolean,
)

/** Result of executing a single shell command. */
data class CommandResult(
    val command: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timestamp: Long,
) {
    val success: Boolean get() = exitCode == 0
}

/** A single log entry. Includes both root command results and policy changes. */
data class LogEntry(
    val timestamp: Long,
    val title: String,
    val detail: String,
    val isError: Boolean = false,
)
