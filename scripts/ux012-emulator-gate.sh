#!/usr/bin/env bash
# Run the existing LIVE regression first, then the new layout/navigation/IME checks.
set -euo pipefail
cd "${GITHUB_WORKSPACE:?}/android"
bash ../scripts/m1-004-emulator-smoke.sh
mkdir -p "$GITHUB_WORKSPACE/ux012-baseline-evidence"
cp -R app/build/outputs/androidTest-results/connected "$GITHUB_WORKSPACE/ux012-baseline-evidence/"
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.DeviceUiRegressionTest \
  --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/ux012-emulator-test.log"
python3 - <<'PY'
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
reports = list(Path('app/build/outputs/androidTest-results/connected').rglob('TEST-*.xml'))
if len(reports) != 1:
    sys.exit('FAIL: expected one UX-012 test report')
root = ET.parse(reports[0]).getroot()
for key, value in {'tests':'5', 'failures':'0', 'errors':'0', 'skipped':'0'}.items():
    if root.get(key) != value:
        sys.exit(f'FAIL: UX-012 {key}={root.get(key)!r}')
expected = {'gridCardsHaveEqualHeightsAndLongTextEllipsizes', 'messageCardsRemainAlignedWithUnreadAndLongPreview', 'detailHeadersUseBackIconAndKeyboardSendWorks', 'liveChatKeyboardSendAndBack', 'videoUsesTextureAndLeavesWithDetail'}
cases = {case.get('name') for case in root.iter('testcase')}
if cases != expected:
    sys.exit(f'FAIL: unexpected UX-012 cases: {sorted(cases)}')
print('PASS: all five UX-012 UI regression cases')
PY
