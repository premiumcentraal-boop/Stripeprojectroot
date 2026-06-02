The next step is to stop treating the Figma output as the “app” and turn it into a **real Android Studio project** with the Figma screens used as UI reference.

Important limitation first: on a real Android 14+ phone, your app **cannot truly grant or revoke root access for other apps** unless your app is the actual root manager, like Magisk / KernelSU / APatch, or it has official integration with them. A normal app can only:

1. Request root for **itself** through `su`.
2. Run root commands after the user grants permission.
3. Manage its **own internal root policy**.
4. Open the installed root manager so the user can change app root permissions there.

So your first MVP should make this clear:

```text
Global root access = RootDeck is allowed to use root tools globally inside RootDeck.
Application root access = RootDeck allows/disallows root actions for selected apps inside RootDeck.
True app root permission = handled by Magisk / KernelSU / APatch, not directly by RootDeck.
```

## Build plan

### Phase 1: Create the real Android project

Start with a native Android app, not a web export.

Use:

```text
Kotlin
Jetpack Compose
Material 3
Android Studio
minSdk 34
target Android 14+
Package: com.rootdeck.app
```

Folder structure:

```text
/android
  /app
    /src/main
      AndroidManifest.xml
      /java/com/rootdeck/app
        MainActivity.kt
        RootShell.kt
        RootModels.kt
        RootRepository.kt
        DashboardScreen.kt
        BasicToolsScreen.kt
        RootManagementScreen.kt
        AppPickerScreen.kt
        LogsScreen.kt
        SettingsScreen.kt
```

First target:

```bash
./gradlew assembleDebug
```

Expected APK:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Phase 2: Basic Tools menu

Recreate the first Figma screen as a real Compose grid.

Tools from your screenshot:

```text
Root Management
Process Keep-Alive
Hide App Processes
App Auto-Start
Memory Monitoring
Virtual Location
Scheduled Reboot
App Proxy
Upload File
```

But implement them one by one. Do **not** build everything visually first and then leave fake buttons. Each tile should open either a working page or a “planned” page.

Recommended build order:

```text
1. Root Management
2. Memory Monitoring
3. Scheduled Reboot
4. Upload File
5. Process Keep-Alive
6. App Auto-Start
7. App Proxy
8. Virtual Location
9. Hide App Processes, renamed safely
```

For “Hide App Processes,” I would not build anti-detection or process hiding. Make it a safe **Process Manager** screen instead: view running processes, stop selected app, disable selected app with confirmation, or hide it only from RootDeck’s own UI.

## Phase 3: Start with Root Management

This should be the first real feature.

### What the Root Management screen should do

The screen should have:

```text
Top global root switch
Explanation card
Root status card
Installed apps list
Per-app toggle
Root provider detection
Open root manager button
Logs
```

### Root Management behavior

The top global switch should control whether **RootDeck itself** is allowed to run root commands.

When user turns it on:

```text
1. Show safety dialog.
2. User confirms.
3. App runs: su -c id
4. If uid=0 appears, RootDeck root mode becomes enabled.
5. If it fails, show “Root not granted or device not rooted.”
```

For each app toggle:

```text
OFF = RootDeck will not run root actions targeting this app.
ON = RootDeck can run approved RootDeck actions for this app after confirmation.
```

Do not claim that the toggle grants real Magisk root permission to that app.

Add a button:

```text
Open Magisk / KernelSU / APatch root permission manager
```

If RootDeck detects one of those installed apps, open it. Otherwise show:

```text
“No supported root manager app detected. Root permissions must be controlled from your installed root manager.”
```

## Data models for Root Management

Use something like this:

```kotlin
enum class RootMode {
    DISABLED,
    ROOTDECK_ONLY
}

data class RootStatus(
    val isRootAvailable: Boolean,
    val isRootGranted: Boolean,
    val providerName: String?,
    val message: String
)

data class ManagedApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val rootActionsAllowedInsideRootDeck: Boolean
)

data class CommandResult(
    val command: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timestamp: Long
)
```

## RootShell.kt MVP

This is the most important backend file.

```kotlin
package com.rootdeck.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object RootShell {

    suspend fun checkRoot(): CommandResult {
        return runRootCommand("id", timeoutSeconds = 5)
    }

    suspend fun runRootCommand(
        command: String,
        timeoutSeconds: Long = 10
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
                    stderr = "Command timed out",
                    timestamp = System.currentTimeMillis()
                )
            }

            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)

            CommandResult(
                command = command,
                exitCode = process.exitValue(),
                stdout = stdout,
                stderr = stderr,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            CommandResult(
                command = command,
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Unknown root error",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun isRootActive(result: CommandResult): Boolean {
        return result.exitCode == 0 && result.stdout.contains("uid=0")
    }
}
```

This does not root the phone. It only checks whether the user’s existing root provider grants RootDeck root access.

## Compose screen logic for Root Management

The screen should follow this flow:

```text
User opens Root Management
→ Screen shows root disabled
→ User taps global switch
→ Safety dialog appears
→ User confirms
→ App calls RootShell.checkRoot()
→ If root granted: enable RootDeck root tools
→ If root denied: keep disabled and show message
```

For app-specific toggles:

```text
User toggles app ON
→ Show confirmation
→ Save package as allowed inside RootDeck
→ Future RootDeck actions targeting that package are allowed
```

For example:

```text
Allow root actions for NordVPN inside RootDeck?
This does not grant NordVPN root permission directly.
It only allows RootDeck to run approved commands targeting this app.
```

## Figma Make follow-up prompt

Paste this into Figma Make next:

```text
Now convert the Root Management screen into a real native Android Kotlin + Jetpack Compose implementation.

Do not create a fake web UI. Build the actual Android source files inside /android.

Implement the first working feature: Root Management.

Requirements:

1. Create RootManagementScreen.kt based on the provided screenshot.
2. Add a global root switch at the top.
3. When the user enables the switch, show a safety confirmation dialog.
4. After confirmation, call RootShell.checkRoot(), which runs su -c id.
5. If the output contains uid=0, show “Root active for RootDeck.”
6. If root is denied or unavailable, show “Root not granted or device not rooted.”
7. Add a list of installed apps with icons, names, package names, and toggles.
8. App toggles must only control RootDeck’s internal policy. They must not claim to grant real Magisk/KernelSU/APatch root permission to those apps.
9. Add explanatory text: “Real root permission is controlled by your installed root manager. RootDeck can only manage its own root actions.”
10. Add a button: “Open Root Manager.”
11. Detect common root manager packages where possible and open them if installed:
    - com.topjohnwu.magisk
    - me.weishu.kernelsu
12. Add LogsScreen support so every root check and toggle action is logged.
13. Add RootShell.kt with safe command execution, timeout handling, stdout/stderr capture, and clear error handling.
14. No background root commands. No hidden actions. No bypasses. No exploit code.
15. Make the screen buildable inside Android Studio with ./gradlew assembleDebug.

The UI should visually match the reference:
- White or light card-based layout
- Top title: ROOT Management
- Global root switch
- Blue info boxes
- App list with icons and right-side toggles
- Clean spacing and Android 14+ Material 3 styling
```

## After Root Management, build each tool one by one

### 1. Memory Monitoring

Safe and easy. Build this next.

Features:

```text
RAM usage
Storage usage
CPU load if available
Running apps count
Refresh button
Optional root-enhanced memory details
```

No dangerous actions yet.

### 2. Scheduled Reboot

Root command:

```bash
su -c reboot
```

Also:

```bash
su -c reboot recovery
su -c reboot bootloader
```

Add strong confirmation:

```text
This will immediately reboot your device. Continue?
```

For scheduled reboot, use Android alarm/work scheduling to notify the user first, then require confirmation before executing.

### 3. Upload File

This should be a local file import/export tool.

Features:

```text
Pick file
Copy to RootDeck folder
Show file path
Optional root copy to selected destination
Require confirmation before root copy
```

Root command example only after confirmation:

```bash
cp /sdcard/Download/file.apk /data/local/tmp/file.apk
```

### 4. Process Keep-Alive

Do this safely. Do not create stealth persistence.

Features:

```text
Select app
Show app package
Launch app
Keep RootDeck foreground service active
Optional watchdog notification
```

Avoid hidden background tricks. Android 14+ is strict about background services, so use a visible foreground service.

### 5. App Auto-Start

On normal Android, third-party apps cannot universally force auto-start across all brands.

Build it as:

```text
Open battery settings
Open app info settings
Show manufacturer-specific instructions
Optional root command experiments behind advanced mode
```

Do not promise guaranteed auto-start.

### 6. App Proxy

Use Android `VpnService` for a safe per-app proxy/VPN-style implementation.

Features:

```text
Per-app include list
Proxy host
Proxy port
Start VPN profile
Stop VPN profile
```

This is a bigger module, so keep it after the root basics.

### 7. Virtual Location

Use Android’s mock location provider flow.

Features:

```text
Explain that user must enable Developer Options
Ask user to select RootDeck as mock location app
Set test latitude/longitude
Start/stop mock location
```

Do not frame it as bypassing app checks. Frame it as testing/development.

### 8. Process Manager instead of Hide App Processes

Rename this from “Hide App Processes” to something safer:

```text
Process Manager
```

Features:

```text
View running processes
Search process/package
Force stop selected app with confirmation
Disable/enable selected package with confirmation
```

Root commands:

```bash
am force-stop PACKAGE
pm disable-user --user 0 PACKAGE
pm enable PACKAGE
```

Add warnings for system apps.

## The real MVP milestone

Your first downloadable APK should only include:

```text
Dashboard
Basic Tools grid
Root Management working
Logs working
Settings working
Build instructions
```

Do not wait until every VMOS-style tool is built. Get one real APK working first, then add each tool.

The first success condition is simple:

```text
Install APK on rooted Android 14+
Open Root Management
Tap global root switch
Magisk/KernelSU/APatch asks for root
RootDeck receives uid=0
Root status changes to active
Installed apps list loads
Per-app RootDeck toggles save correctly
Logs show every action
```

Once that works, the rest of the toolbox can be built cleanly on top of the same `RootShell`, logs, confirmation dialogs, and app-selection system.
