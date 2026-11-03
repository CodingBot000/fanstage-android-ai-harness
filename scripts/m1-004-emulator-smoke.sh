#!/usr/bin/env bash
# Invoked as one sh -c command by the pinned ReactiveCircus emulator runner.
# The runner's 'working-directory: android' is not relied upon implicitly.
set -euo pipefail
cd "${GITHUB_WORKSPACE:?}/android"
chmod +x gradlew
adb devices -l
device_api="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
printf 'Emulator API: %s\n' "$device_api"
test "$device_api" = "35"
adb logcat -c
trap 'adb logcat -d -t 1200 -s AndroidRuntime:E TestRunner:I ActivityTaskManager:I > "$GITHUB_WORKSPACE/android-emulator-logcat.txt" || true' EXIT
./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/android-emulator-test.log"
adb shell pm path dev.fanstage.android | grep '^package:'
