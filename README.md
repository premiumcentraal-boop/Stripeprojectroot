# RootDeck

A clean root dashboard for Android 14+, plus a marketing website.

## Project overview

RootDeck is a transparent root-management utility for users who already have root access
through Magisk, KernelSU, APatch, or another user-authorized root provider. It does **not**
root devices, bypass security, hide itself, or run root commands without explicit user
confirmation.

This repository contains two deliverables:

- A polished public **product website** (`src/app` — React + Tailwind + shadcn/ui)
- A real native **Android app** project under `/android` (Kotlin + Jetpack Compose + Material 3)

## Safety policy

The single source of truth is [`src/app/config/policy.ts`](src/app/config/policy.ts), rendered on
the website at `/#policy`. Do not duplicate safety claims here, in marketing copy, or in
feature blurbs — link to the policy instead. This keeps the project's stance consistent and
makes future changes a one-file edit.

## Android build steps

The shipped repo contains the Gradle wrapper *scripts* but **not** the
`gradle-wrapper.jar` (binaries aren't checked in). Generate it once locally:

```bash
cd android
gradle wrapper --gradle-version 8.7   # one-time bootstrap
```

Then build + stage the APK for the website with the helper script:

```bash
bash scripts/build-debug-apk.sh
```

The script:

1. `cd`s into `android/`
2. Runs `./gradlew --no-daemon assembleDebug`
3. Copies `app/build/outputs/apk/debug/app-debug.apk` →
   `public/downloads/rootdeck-debug.apk`

Raw APK output path: `android/app/build/outputs/apk/debug/app-debug.apk`
Website-served path: `/downloads/rootdeck-debug.apk`
Sideload only on your own device.

## What's in the MVP

- **Dashboard** — root mode summary, root provider, device/CPU/RAM/storage cards.
- **Basic Tools grid** — 9 tiles. Three are real (Root Management, Memory Monitoring, Scheduled Reboot); the rest open a "Planned" page explaining what they will do.
- **Root Management** (flagship feature):
  - Global root switch that flips `RootMode` between `DISABLED` and `ROOTDECK_ONLY` after a safety dialog + `su -c id` probe.
  - Explanation card making clear that RootDeck-internal policy ≠ OS root permission.
  - Per-app allowlist persisted in SharedPreferences.
  - "Open Root Manager" button that launches Magisk / KernelSU / APatch if installed.
- **Logs** — every root probe, command, and policy change recorded with timestamp.
- **Settings** — Advanced mode, command previews, theme (Dark/Black/System); confirmations locked on.

## Feature list

- **Dashboard** — root status, root provider detection (Magisk / KernelSU / APatch),
  Android version, device, storage, CPU/RAM, last command result, and a single
  *Check Root Access* button (no background polling).
- **Tools** — reboot / recovery / bootloader, read-only mount check, per-package cache
  clear, and an Advanced-mode custom command field. Every command shows a preview,
  risk label, and confirmation dialog.
- **Apps** — local installed-app browser with search, copy package name, optional
  `pm disable-user --user 0 <pkg>` and `pm enable <pkg>` actions with extra warnings
  for system apps. No automatic uninstalls.
- **Process Privacy Guard** (v0.2.0) — transparent per-app controls:
  force-stop, `cmd appops` background and wake-lock restrictions, and an optional
  `pm disable-user --user 0` toggle. Every command goes through a confirmation
  dialog with command preview and is logged. RootDeck does **not** perform
  stealth process hiding. This feature provides safe local process privacy
  controls such as force stop, background restriction, wake lock restriction,
  and optional disable/enable for apps selected by the user.
- **Logs** — timestamped record of every root command with stdout, stderr, and exit
  code. Clearable with confirmation.
- **Settings** — Advanced mode toggle, command preview toggle, confirmation toggle
  (locked on by design), theme selector (Dark / Black / System), about, and the
  safety disclaimer.

## Known limitations

- MVP only — no persistence; logs and settings reset when the process dies.
- App enumeration on Android 14 requires `QUERY_ALL_PACKAGES`; some Play distribution
  channels restrict this.
- No bundled `gradle/wrapper/gradle-wrapper.jar`. Generate one locally with
  `gradle wrapper --gradle-version 8.7` before building, or use the bundled wrapper
  from Android Studio.
- The download link on the website (`downloads/rootdeck-debug.apk`) is a placeholder;
  produce the artifact by building locally.

## Roadmap

- Persist logs and settings via DataStore.
- Optional Shizuku backend as a non-root fallback for app-management actions.
- Per-tool risk classification surfaced in the UI palette.
- In-app source viewer for the exact command being executed.
- Localized strings (English only at present).
