Build the next RootDeck Android app update: Process Privacy Guard.

This feature replaces the planned “Hide App Processes” tile with a safe, transparent root tool inspired by the VMOS Toolbox screen, but it must not implement stealth, anti-detection, root hiding, kernel hooks, Magisk hiding, Shamiko-style behavior, Play Integrity bypasses, or hiding processes from the Android OS.

Feature name in UI:
Process Privacy Guard

Optional subtitle:
Control background app activity with transparent root actions

The UI should visually match the provided reference:

* Top app bar with back arrow
* Title: Process Privacy Guard
* Info icon on the right
* Blue explanation card
* Section title: Applications suitable for privacy control
* List of installed apps with app icon, app name, package name, and right-side toggle
* Toggle off by default
* Clean white/light layout similar to VMOS
* RootDeck styling should remain polished and stable

Important explanation text:
“Process Privacy Guard does not hide apps from Android, other apps, security tools, or root managers. It safely controls selected app background activity using visible commands, confirmations, and logs.”

Core behavior:
When the user turns on the toggle for an app:

1. Show a confirmation dialog.
2. Explain exactly what will happen.
3. Let the user choose one or more safe actions:

   * Force stop app now
   * Restrict background execution
   * Restrict wake locks
   * Optional: disable app for current user
4. Show command preview before running.
5. Require confirmation.
6. Run commands only if RootDeck root mode is active.
7. Log every command and result.
8. Save the app’s selected privacy policy in SharedPreferences.

When the user turns the toggle off:

1. Show confirmation.
2. Restore the app’s background permissions where possible.
3. Optionally enable the app if it was disabled by RootDeck.
4. Log the restore action.

Do not claim that this feature hides processes. The app should be honest:

* It can force stop apps.
* It can restrict background activity.
* It can disable or re-enable apps for the current user.
* It can show running processes.
* It cannot invisibly hide processes from Android or other apps.

Create or update these Android files:

/android/app/src/main/java/com/rootdeck/app/ProcessPrivacyScreen.kt
/android/app/src/main/java/com/rootdeck/app/ProcessPrivacyModels.kt
/android/app/src/main/java/com/rootdeck/app/ProcessPrivacyRepository.kt

Update:

* BasicToolsScreen.kt
* MainActivity.kt
* RootRepository.kt if needed
* LogsScreen.kt if needed

Data models:

data class ProcessPrivacyPolicy(
val packageName: String,
val label: String,
val forceStopOnEnable: Boolean,
val restrictBackground: Boolean,
val restrictWakeLocks: Boolean,
val disableForCurrentUser: Boolean,
val enabled: Boolean
)

data class RunningProcessInfo(
val user: String,
val pid: String,
val packageOrCommand: String,
val rawLine: String
)

Commands to support safely:

List running processes:
ps -A

Force stop selected app:
am force-stop PACKAGE_NAME

Restrict background execution:
cmd appops set PACKAGE_NAME RUN_IN_BACKGROUND ignore
cmd appops set PACKAGE_NAME RUN_ANY_IN_BACKGROUND ignore

Restore background execution:
cmd appops set PACKAGE_NAME RUN_IN_BACKGROUND allow
cmd appops set PACKAGE_NAME RUN_ANY_IN_BACKGROUND allow

Restrict wake locks:
cmd appops set PACKAGE_NAME WAKE_LOCK ignore

Restore wake locks:
cmd appops set PACKAGE_NAME WAKE_LOCK allow

Optional disable app for current user:
pm disable-user --user 0 PACKAGE_NAME

Restore disabled app:
pm enable PACKAGE_NAME

Safety rules:

* Never run commands automatically on app launch.
* Never run commands without confirmation.
* Never target critical Android system packages without a strong warning.
* Add a system-app warning if isSystemApp is true.
* Do not include process hiding, root hiding, anti-detection, Play Integrity bypass, Magisk DenyList automation, Shamiko integration, kernel modules, ptrace tricks, procfs hiding, LSPosed hooks, or stealth background services.
* Every command must go through SafetyConfirmDialog.
* Every command must be logged with timestamp, package name, command, exit code, stdout, stderr, and success/failure.

Screen behavior:

ProcessPrivacyScreen:

* Loads installed apps using existing AppPickerScreen or package manager logic.
* Shows search bar.
* Shows filter chips:
  All
  User apps
  System apps
  Active policies
* Each row shows:
  app icon
  app label
  package name
  status badge if policy is active
  toggle
* Tapping a row opens a bottom sheet or detail screen:
  App name
  Package name
  Current policy
  Running process status from ps -A
  Buttons:
  Force stop now
  Apply privacy policy
  Restore defaults
  Copy package name

Toggle ON flow:

* Show dialog:
  “Apply Process Privacy Guard to this app?”
  Explain:
  “RootDeck will not hide this process. It can force stop the app and restrict selected background actions using Android shell commands.”
* Show checkboxes:
  Force stop now
  Restrict background execution
  Restrict wake locks
  Disable for current user
* Show command preview based on selected options.
* Confirm button:
  Apply policy
* Cancel button:
  Cancel

Toggle OFF flow:

* Show dialog:
  “Restore this app?”
* Show restore command preview.
* Confirm button:
  Restore defaults

Root requirement:

* If RootDeck root mode is not active, disable root action buttons.
* Show:
  “RootDeck root mode is disabled. Enable root in Root Management first.”

Logging:
Add log entries for:

* Opened Process Privacy Guard
* Listed processes
* Applied policy
* Restored policy
* Force stopped app
* Background restriction result
* Wake lock restriction result
* Disable/enable result

BasicToolsScreen update:

* Replace the old “Hide App Processes” tile with:
  Process Privacy Guard
* Icon can remain similar to the process/hide icon, but label must be safe.
* Route it to ProcessPrivacyScreen.

Dashboard update:
Add a small card:
Process Privacy Guard
Shows:

* Active policies count
* Last action result
* Button: Open

README update:
Add feature section:
Process Privacy Guard

Explain:
“RootDeck does not perform stealth process hiding. This feature provides safe local process privacy controls such as force stop, background restriction, wake lock restriction, and optional disable/enable for apps selected by the user.”

Testing checklist:

* Build APK succeeds.
* Process Privacy Guard opens from Basic Tools.
* App list loads.
* Search works.
* Toggle ON shows confirmation.
* Command preview is shown.
* Commands only run when root mode is active.
* Toggle OFF restores appops and enable state where applicable.
* Logs show every action.
* System apps show warning.
* No stealth or anti-detection features are present.

Keep the APK stable. Do not add App Proxy, Virtual Location, or Upload File yet. This update should only add the working Process Privacy Guard feature.
