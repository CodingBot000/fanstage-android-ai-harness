#!/usr/bin/env python3
"""Verify the approved direction of project module dependencies."""
from pathlib import Path
import re
import sys

root = Path(__file__).resolve().parents[1] / "android"
expected = {
    "core/model": set(),
    "core/data": {":core:model"},
    "core/artwork": {":core:model"},
    "core/designsystem": set(),
    "feature/live": {":core:model", ":core:designsystem"},
    "feature/discover": {":core:model", ":core:artwork", ":core:designsystem"},
    "feature/community": {":core:model", ":core:artwork", ":core:designsystem"},
    "feature/media": {":core:model", ":core:artwork", ":core:designsystem"},
    "feature/messages": {":core:model", ":core:artwork", ":core:designsystem"},
}
errors = []
for module, allowed in expected.items():
    text = (root / module / "build.gradle.kts").read_text()
    actual = set(re.findall(r'project\("(:[^\"]+)"\)', text))
    if actual != allowed:
        errors.append(f"{module}: expected {sorted(allowed)}, found {sorted(actual)}")
for path in (root / "core/model/src/main").rglob("*.kt"):
    if re.search(r'^import\s+(?:android|androidx)\.|\bR\.drawable\b|\bContext\b', path.read_text(), re.M):
        errors.append(f"Android UI reference in pure model: {path.relative_to(root)}")
app_files = {p.name for p in (root / "app/src/main/kotlin/dev/fanstage/android").glob("*.kt")}
if app_files != {"MainActivity.kt", "AppContainer.kt", "FanStageApplication.kt"}:
    errors.append(f"unexpected app product source: {sorted(app_files)}")
if errors:
    print("FAIL\n" + "\n".join(errors)); sys.exit(1)
print("PASS: feature/core dependency direction, pure models, and app composition root")
