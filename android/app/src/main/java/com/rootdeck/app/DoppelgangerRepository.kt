package com.rootdeck.app

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Drives Doppelganger app cloning via Android's secondary-user mechanism. */
class DoppelgangerRepository(
    private val repo: RootRepository,
    private val scope: CoroutineScope,
) {
    val state = mutableStateOf(DoppelgangerState())

    private suspend fun run(title: String, command: String): CommandResult =
        suspendCoroutine { cont ->
            repo.runConfirmedCommand(title, command) { result -> cont.resume(result) }
        }

    fun refresh() {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val usersResult = run("Doppelganger · list users", "pm list users")
            val users = parseUsers(usersResult.stdout)
            val clones = mutableListOf<ClonedApp>()
            for (u in users.filter { it.userId != 0 }) {
                val pkgs = run(
                    "Doppelganger · list packages for user ${u.userId}",
                    "pm list packages --user ${u.userId}",
                )
                pkgs.stdout.lineSequence()
                    .map { it.removePrefix("package:").trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { clones += ClonedApp(u.userId, it) }
            }
            update {
                it.copy(
                    users = users,
                    clones = clones,
                    busy = false,
                    lastError = usersResult.stderr.takeIf { s -> usersResult.exitCode != 0 && s.isNotBlank() },
                )
            }
        }
    }

    fun createCloneUser(name: String) {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val safe = name.ifBlank { "RootDeck-Clone" }.replace("\"", "")
            val result = run("Doppelganger · create user", "pm create-user \"$safe\"")
            if (result.exitCode != 0) {
                update { it.copy(busy = false, lastError = result.stderr.ifBlank { "create-user failed" }) }
                return@launch
            }
            refresh()
        }
    }

    fun cloneAppIntoUser(packageName: String, userId: Int) {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val result = run(
                "Doppelganger · clone $packageName to user $userId",
                "pm install-existing --user $userId $packageName",
            )
            if (result.exitCode != 0) {
                update { it.copy(busy = false, lastError = result.stderr.ifBlank { "install-existing failed" }) }
                return@launch
            }
            refresh()
        }
    }

    fun launchClone(packageName: String, userId: Int) {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val cmd = "monkey --pct-syskeys 0 -p $packageName --user $userId 1"
            val result = run("Doppelganger · launch $packageName as user $userId", cmd)
            update {
                it.copy(
                    busy = false,
                    lastError = if (result.exitCode == 0) null else result.stderr.ifBlank { "launch failed" },
                )
            }
        }
    }

    fun removeClone(packageName: String, userId: Int) {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val result = run(
                "Doppelganger · remove clone $packageName from user $userId",
                "pm uninstall --user $userId $packageName",
            )
            if (result.exitCode != 0) {
                update { it.copy(busy = false, lastError = result.stderr.ifBlank { "uninstall failed" }) }
                return@launch
            }
            refresh()
        }
    }

    fun removeUser(userId: Int) {
        scope.launch {
            update { it.copy(busy = true, lastError = null) }
            val result = run("Doppelganger · remove user $userId", "pm remove-user $userId")
            if (result.exitCode != 0) {
                update { it.copy(busy = false, lastError = result.stderr.ifBlank { "remove-user failed" }) }
                return@launch
            }
            refresh()
        }
    }

    private inline fun update(transform: (DoppelgangerState) -> DoppelgangerState) {
        state.value = transform(state.value)
    }

    private fun parseUsers(stdout: String): List<CloneUser> {
        // Lines look like: "\tUserInfo{0:Owner:13} running"
        val regex = Regex("""UserInfo\{(\d+):([^:}]+):""")
        return stdout.lineSequence()
            .mapNotNull { regex.find(it)?.groupValues }
            .map { CloneUser(it[1].toInt(), it[2]) }
            .toList()
    }
}
