You are working inside the existing RootDeck Android Gradle project.

Goal:
Update the existing LSPosed setup guide into a modern “Vector, formerly LSPosed” setup assistant. Build a small RootDeck screen/tool that detects the device’s Android/root/Magisk/Zygisk status and recommends the correct Vector build to install. The app must guide the user safely, not flash or install modules automatically.

Important context:

* The old LSPosed guide currently exists in the app and must be updated.
* The new recommended project is JingMatrix/Vector, formerly LSPosed.
* Vector should be treated as a Magisk Zygisk module.
* Do not recommend random APK mirrors.
* Do not implement root hiding, Play Integrity bypass, banking bypass, anti-detection, or stealth behavior.
* The app should only detect local status, fetch official release information, and show a short setup guide.

Implement this inside the existing RootDeck Android Gradle build.

Main feature name:
Vector Setup Assistant

Required UI:
Create or update a screen named:

VectorSetupGuideScreen

The screen should show:

1. Device status card

* Android version
* SDK level
* Device model
* Device codename from getprop ro.product.device
* Build ID from getprop ro.build.id
* CPU ABI

2. Root status card

* Root available: Yes/No
* Magisk detected: Yes/No/Unknown
* Magisk version if detectable
* Zygisk enabled: Yes/No/Unknown
* Bootloader unlocked: Yes/No/Unknown if detectable

3. Vector recommendation card

* Recommended framework: Vector, formerly LSPosed
* Recommended install type: Zygisk Magisk module ZIP
* Source: Official GitHub releases from JingMatrix/Vector
* Show latest version name/tag fetched from GitHub
* Show release date if available
* Show recommended asset filename
* Show a button: “Open official release page”
* Show a button: “Download recommended ZIP” if a valid asset URL is found

4. Short setup guide card
   Show these steps:

* Open Magisk
* Enable Zygisk in Magisk settings
* Reboot
* Download the recommended Vector Zygisk ZIP
* Open Magisk > Modules > Install from storage
* Select the Vector ZIP
* Reboot
* Open Vector/LSPosed manager from notification or launcher shortcut if available
* Install LSPosed/Xposed modules only from trusted sources
* Enable modules only for the apps they need to affect

Detection requirements:
Create a Kotlin class:

RootEnvironmentDetector.kt

It should expose a data class:

RootEnvironment(
val androidRelease: String,
val sdkInt: Int,
val deviceModel: String,
val productDevice: String?,
val buildId: String?,
val cpuAbi: String?,
val rootAvailable: Boolean,
val magiskDetected: Boolean?,
val magiskVersion: String?,
val zygiskEnabled: Boolean?,
val bootloaderUnlocked: Boolean?
)

Detection implementation:

* Android version: Build.VERSION.RELEASE and Build.VERSION.SDK_INT
* Device model: Build.MANUFACTURER + Build.MODEL
* CPU ABI: Build.SUPPORTED_ABIS.firstOrNull()
* getprop values through Runtime exec:

  * getprop ro.product.device
  * getprop ro.build.id
  * getprop ro.boot.flash.locked
  * getprop ro.boot.verifiedbootstate
* Root check:

  * Run: su -c id
  * If output contains uid=0, rootAvailable = true
* Magisk check:

  * Run: su -c magisk -v
  * Run: su -c magisk -V
  * If either returns a valid value, magiskDetected = true
* Zygisk check:

  * Try: su -c "magisk --sqlite 'SELECT value FROM settings WHERE key="zygisk";'"
  * If result is 1, zygiskEnabled = true
  * If result is 0, zygiskEnabled = false
  * If command fails, return Unknown instead of crashing
* Do not assume Zygisk is disabled if detection fails. Show “Unknown, check Magisk settings.”

GitHub release fetch requirements:
Create:

VectorReleaseRepository.kt

Use the GitHub REST endpoint:

https://api.github.com/repos/JingMatrix/Vector/releases/latest

Parse:

* tag_name
* name
* html_url
* published_at
* body
* assets array:

  * name
  * browser_download_url
  * size
  * content_type

Create data classes:
VectorRelease
VectorAsset
VectorRecommendation

Recommendation logic:
Create:

VectorRecommendationEngine.kt

Rules:

* If Android SDK is 35 or higher, strongly recommend latest JingMatrix/Vector Zygisk release.
* If Android SDK is 34 or lower, still recommend Vector first, but mention legacy LSPosed may only be needed for older setups.
* Prefer assets where filename:

  * ends with .zip
  * contains zygisk, case-insensitive, if available
  * does not contain riru, case-insensitive
  * does not contain debug, case-insensitive, unless no stable asset exists
* If multiple matching ZIPs exist, prefer the newest release asset from latest release.
* If no asset can be confidently selected, show the official release page and tell the user to manually download the Zygisk ZIP.

Download behavior:

* Do not silently install anything.
* Use Android DownloadManager or open the asset URL in browser.
* Save to Downloads if using DownloadManager.
* After download, show: “Open Magisk > Modules > Install from storage > select this ZIP.”
* Do not request unnecessary storage permissions on modern Android.
* Handle no internet/API failure gracefully.

Navigation:

* Replace or update the existing LSPosed guide entry in RootDeck.
* Rename visible text from “LSPosed Setup Guide” to:
  “Vector / LSPosed Setup”
* Existing references to LSPosed should explain:
  “Vector is the maintained modern continuation/fork commonly used instead of old LSPosed builds.”

Safety copy in UI:
Add a warning box:
“Only install Vector from the official JingMatrix/Vector GitHub releases. Do not install random LSPosed APKs or ZIPs from mirror sites. Install one module at a time and reboot after each module.”

Also add:
“RootDeck does not install Magisk modules automatically. It only checks your environment and guides you to the official release.”

Files to create or update:

* app/src/main/java/.../root/RootEnvironmentDetector.kt
* app/src/main/java/.../vector/VectorReleaseRepository.kt
* app/src/main/java/.../vector/VectorRecommendationEngine.kt
* app/src/main/java/.../ui/screens/VectorSetupGuideScreen.kt
* Update current LSPosed guide screen or route to point to VectorSetupGuideScreen
* Update changelog entry to mention:
  “Updated LSPosed setup guide to Vector, formerly LSPosed, with Magisk/Zygisk detection and official release recommendation.”

UI style:
Use the existing RootDeck Compose style and cards.
Keep it compact, practical, and beginner-friendly.
Use clear status labels:

* Good
* Needs action
* Unknown
* Not detected

Acceptance criteria:

* App builds successfully with Gradle.
* Screen loads without root access and shows non-root status instead of crashing.
* On a rooted Magisk device, it detects root and attempts Magisk/Zygisk detection.
* It fetches the latest JingMatrix/Vector release from GitHub.
* It recommends a Zygisk ZIP asset when available.
* It never recommends the old archived LSPosed release for Android 15/16.
* It includes a short setup guide for installing the ZIP through Magisk Modules.
* It does not implement stealth, bypass, or anti-detection features.
* Changelog is updated.
