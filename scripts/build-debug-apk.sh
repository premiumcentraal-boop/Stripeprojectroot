#!/usr/bin/env bash
# Builds the RootDeck debug APK and stages it for the website download.
# Run from the project root.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$ROOT_DIR/android"
APK_SRC="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
APK_DST_DIR="$ROOT_DIR/public/downloads"
APK_DST="$APK_DST_DIR/rootdeck-debug.apk"

if [ ! -d "$ANDROID_DIR" ]; then
  echo "error: $ANDROID_DIR does not exist." >&2
  exit 1
fi

cd "$ANDROID_DIR"

if [ ! -f "./gradlew" ]; then
  echo "error: gradlew not found in $ANDROID_DIR." >&2
  echo "Generate it once with:  gradle wrapper --gradle-version 8.7" >&2
  exit 1
fi

if [ ! -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
  cat >&2 <<'MSG'
warning: gradle/wrapper/gradle-wrapper.jar is missing.
The repo only ships gradle-wrapper.properties; the jar is generated locally.
Bootstrap it once with:

    cd android
    gradle wrapper --gradle-version 8.7

Then re-run this script.
MSG
  exit 1
fi

chmod +x ./gradlew
./gradlew --no-daemon assembleDebug

if [ ! -f "$APK_SRC" ]; then
  echo "error: build succeeded but APK not found at $APK_SRC" >&2
  exit 1
fi

mkdir -p "$APK_DST_DIR"
cp "$APK_SRC" "$APK_DST"

echo
echo "Built APK: $APK_SRC"
echo "Staged at: $APK_DST"
echo "Website link: /downloads/rootdeck-debug.apk"
