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
for key, value in {'tests':'9', 'failures':'0', 'errors':'0', 'skipped':'0'}.items():
    if root.get(key) != value:
        sys.exit(f'FAIL: UX-012 {key}={root.get(key)!r}')
expected = {'gridCardsHaveEqualHeightsAndLongTextEllipsizes', 'messageCardsRemainAlignedWithUnreadAndLongPreview', 'detailHeadersUseBackIconAndKeyboardSendWorks', 'liveChatKeyboardSendAndBack', 'videoUsesTextureAndLeavesWithDetail', 'developmentFieldsStayAboveImeWithoutMovingVideo', 'chatSettingsStartCollapsedAndInvalidConnectionKeepsDemoSelected', 'chatSettingsSurviveActivityRecreation', 'chatModeControlsRemainSeparateAtDoubleFontAndNarrowWidth'}
cases = list(root.iter('testcase'))
if len(cases) != len(expected) or {case.get('name') for case in cases} != expected or any(
    case.get('classname') != 'dev.fanstage.android.DeviceUiRegressionTest' for case in cases
):
    sys.exit('FAIL: expected DeviceUiRegressionTest cases missing or duplicated')
if any(case.find(tag) is not None for case in cases for tag in ('failure', 'error', 'skipped')):
    sys.exit('FAIL: DeviceUiRegressionTest case failed or skipped')
print('PASS: all nine DeviceUiRegressionTest cases')
PY
