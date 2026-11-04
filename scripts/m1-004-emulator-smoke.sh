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
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RoomNavigationSmokeTest --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/android-emulator-test.log"
# Capture Media3 instance creation/release after the completed test.
adb logcat -d -s ExoPlayerImpl:I > "$GITHUB_WORKSPACE/android-player-lifecycle.log"
adb logcat -d -s TestRunner:I > "$GITHUB_WORKSPACE/android-test-runner.log"
python3 - <<'PYPLAYER'
import collections
import re
from pathlib import Path
import sys
log = Path("../android-player-lifecycle.log").read_text()
runner = Path("../android-test-runner.log").read_text()
# Match the exact instrumentation case's process, not unrelated emulator apps.
pids = set(re.findall(r"^\S+\s+\S+\s+(\d+)\s+\d+\s+I\s+TestRunner:\s+finished: roomListOpensDetailAndReturns\(dev\.fanstage\.android\.RoomNavigationSmokeTest\)", runner, re.M))
if len(pids) != 1:
    sys.exit(f"FAIL: could not uniquely identify instrumentation process: {pids}")
pid = next(iter(pids))
lines = [line for line in log.splitlines() if re.match(rf"^\S+\s+\S+\s+{pid}\s+\d+\s+I\s+ExoPlayerImpl:", line)]
starts = collections.Counter(re.findall(r"ExoPlayerImpl: Init ([0-9a-f]+)", "\n".join(lines)))
ends = collections.Counter(re.findall(r"ExoPlayerImpl: Release ([0-9a-f]+)", "\n".join(lines)))
if sum(starts.values()) < 7 or starts != ends:
    sys.exit(f"FAIL: Media3 init/release mismatch for PID {pid}: init={starts}, release={ends}")
print(f"PASS: {sum(starts.values())} Media3 instances released in instrumentation PID {pid}; ids match")
PYPLAYER
# Connected test Gradle has already installed/launched the app; avoid a teardown-time pm path assumption.
python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
base = Path("app/build/outputs/androidTest-results/connected")
reports = sorted(base.rglob("TEST-*.xml"))
if len(reports) != 1:
    sys.exit(f"FAIL: expected exactly one connected test XML, found {len(reports)}")
root = ET.parse(reports[0]).getroot()
suites = root.findall("testsuite")
if root.tag != "testsuites" or len(suites) != 1:
    sys.exit("FAIL: unexpected connected test XML structure")
for node in (root, suites[0]):
    for key, expected in {"tests": "1", "failures": "0", "errors": "0", "skipped": "0"}.items():
        if node.get(key) != expected:
            sys.exit(f"FAIL: {node.tag} {key}={node.get(key)!r}, wanted {expected}")
cases = suites[0].findall("testcase")
if len(cases) != 1 or (cases[0].get("classname"), cases[0].get("name")) != (
    "dev.fanstage.android.RoomNavigationSmokeTest", "roomListOpensDetailAndReturns"
):
    sys.exit("FAIL: exact Compose navigation test not found")
if any(cases[0].find(tag) is not None for tag in ("failure", "error", "skipped")):
    sys.exit("FAIL: connected test case failed or skipped")
codes = list(base.rglob("test-result-exit-code.txt"))
if len(codes) != 1 or codes[0].read_text().strip() != "0":
    sys.exit("FAIL: missing/nonzero AndroidTest process exit code")
print(f"PASS: verified connected Compose XML and runner exit: {reports[0]}")
PY
