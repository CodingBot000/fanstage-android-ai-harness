#!/usr/bin/env bash
# Controlled negative test: the Android test must fail and its XML must retain trace events.
set -euo pipefail
: "${ANDROID_SERIAL:?Select an isolated Android test device with ANDROID_SERIAL}"
devices="$(adb devices | awk 'NR > 1 && $2 == "device" {print $1}')"
if [ "$devices" != "$ANDROID_SERIAL" ]; then
  printf '%s\n' 'FAIL: ADB server must expose exactly the selected device' >&2
  exit 1
fi
root="$(cd "$(dirname "$0")/.." && pwd)"
fixture="$root/scripts/fixtures/RotationAuditFailureProbe.kt"
target="$root/android/app/src/androidTest/kotlin/dev/fanstage/android/RotationAuditFailureProbe.kt"
if [ -e "$target" ] || [ -L "$target" ]; then printf '%s\n' 'FAIL: temporary test target already exists' >&2; exit 1; fi
output="$(mktemp -d "${TMPDIR:-/tmp}/fan-rotation-negative-XXXXXX")"
cleanup() { if [ -f "$target" ] && cmp -s "$fixture" "$target"; then rm "$target"; fi; }
trap cleanup EXIT
cp "$fixture" "$target"
cd "$root/android"
start_ns="$(python3 -c 'import time; print(time.time_ns())')"
run_id="negative$(python3 -c 'import uuid; print(uuid.uuid4().hex[:16])')"
set +e
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
  '-Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RotationAuditFailureProbe' \
  "-Pandroid.testInstrumentationRunnerArguments.fanRotationRunId=$run_id" \
  --no-daemon --max-workers=2 > "$output/gradle.log" 2>&1
status=$?
set -e
python3 - "$status" "$output" "$start_ns" "$run_id" <<'PY'
from pathlib import Path
import re,shutil,sys,xml.etree.ElementTree as ET
status=int(sys.argv[1]);out=Path(sys.argv[2]);start_ns=int(sys.argv[3]);run_id=sys.argv[4];reports=list(Path('app/build/outputs/androidTest-results/connected').rglob('TEST-*.xml'))
log=(out/'gradle.log').read_text(errors='replace')
if status==0 or len(reports)!=1 or 'Starting 1 tests' not in log or 'Finished 1 tests' not in log: raise SystemExit('FAIL: expected one newly executed failed instrumentation result')
if reports[0].stat().st_mtime_ns < start_ns: raise SystemExit('FAIL: test result predates this execution')
r=ET.parse(reports[0]).getroot();cases=list(r.iter('testcase'))
if r.get('tests')!='1' or r.get('failures')!='1' or r.get('errors')!='0' or r.get('skipped')!='0' or len(cases)!=1: raise SystemExit('FAIL: unexpected negative-probe result')
case=cases[0];failure=case.find('failure');text='' if failure is None else ''.join(failure.itertext())
if case.get('classname')!='dev.fanstage.android.RotationAuditFailureProbe' or case.get('name')!='controlledFailureCarriesRotationEvents': raise SystemExit('FAIL: unexpected negative-probe case')
if 'intentional rotation diagnostic probe' not in text: raise SystemExit('FAIL: controlled failure marker missing')
pattern=re.compile(r'^run=' + re.escape(run_id) + r' ns=([0-9]+) event=([A-Za-z_]+)(?: |$)')
events=[(int(m.group(1)),line.strip()) for line in text.splitlines() if (m:=pattern.match(line.strip()))]
if not events: raise SystemExit('FAIL: diagnostic run ID does not match this execution')
if any(a[0]>b[0] for a,b in zip(events,events[1:])): raise SystemExit('FAIL: diagnostic timestamps are not monotonic')
lines=[line for _,line in events]
for marker in ('event=test_start','event=controlled_failure_probe','event=ui_checkpoint name=probe_before_cleanup','event=ui_checkpoint name=failure_after_cleanup'):
    if not any(marker in line for line in lines): raise SystemExit('FAIL: current-run diagnostic marker missing')
snapshot_pattern=re.compile(r'event=player_snapshot name=probe_before_cleanup .*player=[1-9][0-9]* .*surface=[1-9][0-9]* .*available=true')
snapshots=[index for index,line in enumerate(lines) if snapshot_pattern.search(line)]
if not snapshots: raise SystemExit('FAIL: current-run fullscreen Player/Surface snapshot missing before cleanup')
def first(marker): return next(index for index,line in enumerate(lines) if marker in line)
sequence=[first('event=test_start'), snapshots[0], first('event=ui_checkpoint name=probe_before_cleanup'), first('event=controlled_failure_probe'), first('event=ui_checkpoint name=failure_after_cleanup')]
if sequence != sorted(sequence) or len(set(sequence)) != len(sequence): raise SystemExit('FAIL: current-run diagnostic event order is invalid')
shutil.copyfile(reports[0],out/'negative-probe.xml')
print('PASS: intentional failure was reported and JUnit XML retained the diagnostic trace')
print('Negative-probe output directory:',out)
PY
