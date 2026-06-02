package com.rootdeck.app

/** Models for the Process Privacy Guard feature. */
data class ProcessPrivacyPolicy(
    val packageName: String,
    val label: String,
    val forceStopOnEnable: Boolean,
    val restrictBackground: Boolean,
    val restrictWakeLocks: Boolean,
    val disableForCurrentUser: Boolean,
    val enabled: Boolean,
) {
    companion object {
        fun defaultFor(packageName: String, label: String) = ProcessPrivacyPolicy(
            packageName = packageName,
            label = label,
            forceStopOnEnable = true,
            restrictBackground = true,
            restrictWakeLocks = false,
            disableForCurrentUser = false,
            enabled = false,
        )
    }
}

/** A single row from `ps -A`. */
data class RunningProcessInfo(
    val user: String,
    val pid: String,
    val packageOrCommand: String,
    val rawLine: String,
)

enum class PrivacyFilter { All, UserApps, SystemApps, ActivePolicies }
