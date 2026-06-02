package com.rootdeck.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

/** Wrapper around the device `su` binary. */
object RootShell {

    /** Probe the user's root provider with a harmless `id` call. */
    suspend fun checkRoot(): CommandResult = runRootCommand("id", timeoutSeconds = 5)

    suspend fun runRootCommand(
        command: String,
        timeoutSeconds: Long = 10,
    ): CommandResult = withContext(Dispatchers.IO) {
        val fullCommand = arrayOf("su", "-c", command)
        try {
            val process = Runtime.getRuntime().exec(fullCommand)
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return@withContext CommandResult(
                    command = command,
                    exitCode = -1,
                    stdout = "",
                    stderr = "Command timed out after ${timeoutSeconds}s",
                    timestamp = System.currentTimeMillis(),
                )
            }

            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)

            CommandResult(
                command = command,
                exitCode = process.exitValue(),
                stdout = stdout,
                stderr = stderr,
                timestamp = System.currentTimeMillis(),
            )
        } catch (e: Exception) {
            CommandResult(
                command = command,
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Unknown root error",
                timestamp = System.currentTimeMillis(),
            )
        }
    }

    /** True only when `su -c id` succeeded and reported uid=0. */
    fun isRootActive(result: CommandResult): Boolean =
        result.exitCode == 0 && result.stdout.contains("uid=0")
}
