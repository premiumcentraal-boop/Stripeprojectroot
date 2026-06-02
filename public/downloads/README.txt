Place the built APK here as `rootdeck-debug.apk`.

Build it from the project root:

    bash scripts/build-debug-apk.sh

The script runs `./gradlew assembleDebug` inside `android/` and copies the
resulting `app-debug.apk` into this folder.
