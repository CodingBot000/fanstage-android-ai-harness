#!/usr/bin/env bash
# Executed by the emulator runner on an isolated CI host after Compose startup.
set -euo pipefail
: "${GITHUB_WORKSPACE:?}" "${RUNNER_TEMP:?}"
source "$RUNNER_TEMP/fanstage-m4.env"
cd "$GITHUB_WORKSPACE/android"
test "$(adb shell getprop ro.build.version.sdk | tr -d '\r')" = 35
adb logcat -c
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RealChatIntegrationTest \
  -Pandroid.testInstrumentationRunnerArguments.fanTokenA="$FANSTAGE_TOKEN_ALICE" \
  -Pandroid.testInstrumentationRunnerArguments.fanTokenB="$FANSTAGE_TOKEN_BOB" \
  --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$RUNNER_TEMP/fanstage-m4-e2e.log"
python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
base = Path("app/build/outputs/androidTest-results/connected")
reports = list(base.rglob("TEST-*.xml"))
if len(reports) != 1:
    sys.exit("FAIL: expected exactly one real-chat result XML")
root = ET.parse(reports[0]).getroot()
suites = root.findall("testsuite")
if root.tag != "testsuites" or len(suites) != 1:
    sys.exit("FAIL: unexpected real-chat XML")
for node in (root, suites[0]):
    for key, expected in {"tests":"1", "failures":"0", "errors":"0", "skipped":"0"}.items():
        if node.get(key) != expected:
            sys.exit(f"FAIL: {node.tag} {key}={node.get(key)!r}")
cases = suites[0].findall("testcase")
if len(cases) != 1 or (cases[0].get("classname"), cases[0].get("name")) != (
    "dev.fanstage.android.RealChatIntegrationTest", "androidRealWebSocketHistoryReconnectAndPlayerIsolation"
):
    sys.exit("FAIL: exact real-chat test missing")
if any(cases[0].find(tag) is not None for tag in ("failure", "error", "skipped")):
    sys.exit("FAIL: real-chat case failed or skipped")
codes = list(base.rglob("test-result-exit-code.txt"))
if len(codes) != 1 or codes[0].read_text().strip() != "0":
    sys.exit("FAIL: missing/nonzero instrumentation exit code")
print("PASS: real Android WebSocket/reconnect UI case and runner exit")
PY
