#!/usr/bin/env python3
"""Copy CI evidence with disposable credentials redacted; never upload raw test output."""
import os
from pathlib import Path
import shutil

root = Path(os.environ["GITHUB_WORKSPACE"])
temp = Path(os.environ["RUNNER_TEMP"])
out = temp / "fanstage-m4-sanitized"
out.mkdir(exist_ok=True)
secrets = [os.environ.get(k, "") for k in ("FANSTAGE_TOKEN_ALICE", "FANSTAGE_TOKEN_BOB", "FANSTAGE_DB_PASSWORD")]
files = [temp / "fanstage-m4-e2e.log", temp / "fanstage-m4-server.log"]
files += list((root / "android/app/build/outputs/androidTest-results/connected").rglob("*.xml"))
files += list((root / "android/app/build/outputs/androidTest-results/connected").rglob("test-result-exit-code.txt"))
for index, source in enumerate(p for p in files if p.is_file()):
    text = source.read_text(errors="replace")
    for secret in secrets:
        if secret: text = text.replace(secret, "[REDACTED]")
    (out / f"{index}-{source.name}").write_text(text)
print(f"Prepared {len(list(out.iterdir()))} sanitized evidence files")
