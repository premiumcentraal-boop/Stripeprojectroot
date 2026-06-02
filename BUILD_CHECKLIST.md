# RootDeck — First Test APK Build Checklist

Use this as the final gate before publishing the first APK build.

## Build pipeline

- [ ] `/android` opens cleanly in Android Studio (Iguana or later).
- [ ] Gradle sync succeeds. AGP 8.5.x, Kotlin 2.0.0, Compose Compiler plugin 2.0.0.
- [ ] One-time bootstrap done: `cd android && gradle wrapper --gradle-version 8.7`
      (this produces `gradle/wrapper/gradle-wrapper.jar`, which is **not** shipped).
- [ ] `cd android && ./gradlew assembleDebug` completes with `BUILD SUCCESSFUL`.
- [ ] `android/app/build/outputs/apk/debug/app-debug.apk` exists.
- [ ] `bash scripts/build-debug-apk.sh` copies it to `public/downloads/rootdeck-debug.apk`.
- [ ] The website's *Download RootDeck Test APK* button activates after the file lands.
- [ ] Clicking the button serves the actual APK (not Vite's index.html fallback).

## App smoke test on a rooted Android 14+ device

- [ ] App installs from `adb install -r public/downloads/rootdeck-debug.apk`.
- [ ] App launches; Dashboard renders without crashes.
- [ ] Bottom nav switches between Dashboard / Tools / Logs / Settings.
- [ ] Tools grid shows all 9 tiles.
- [ ] Root Management screen opens from the Root Management tile.
- [ ] Tapping the global root switch shows the safety confirmation dialog.
- [ ] Confirming triggers a Magisk / KernelSU / APatch root prompt on-device.
- [ ] On grant: status flips to "Root active for RootDeck (uid=0 via …)".
- [ ] On deny / non-rooted: status shows "Root not granted or device not rooted." — no crash, no loop.
- [ ] Installed app list populates; per-app toggles persist across app restarts.
- [ ] App-toggle dialog text explicitly states it does **not** grant OS root permission.
- [ ] "Open Root Manager" launches Magisk/KernelSU/APatch if installed; otherwise stays disabled with a clear message.
- [ ] Memory Monitoring shows live RAM / storage / CPU / process count; Refresh updates values.
- [ ] Scheduled Reboot tiles are disabled until root mode is active. Confirmations appear before each reboot.
- [ ] Logs screen captures each root probe, command, and policy toggle with timestamp + exit code.
- [ ] Logs *Clear* button shows its own confirmation.
- [ ] Settings: Advanced mode, command previews, theme persist across restarts.
- [ ] "Always require confirmation" is visibly locked on.
- [ ] No "Planned" tile pretends to be working.

## Safety review

- [ ] No exploit / bypass / persistence / spyware / anti-detection code.
- [ ] No INTERNET permission requested.
- [ ] Every root command shows a preview + risk label + confirmation.
- [ ] No automatic root commands on app start (only the user-initiated `su -c id` probe).
