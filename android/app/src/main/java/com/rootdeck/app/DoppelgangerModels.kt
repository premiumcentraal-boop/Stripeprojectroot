package com.rootdeck.app

data class CloneUser(
    val userId: Int,
    val name: String,
)

data class ClonedApp(
    val userId: Int,
    val packageName: String,
)

data class DoppelgangerState(
    val users: List<CloneUser> = emptyList(),
    val clones: List<ClonedApp> = emptyList(),
    val busy: Boolean = false,
    val lastError: String? = null,
)
