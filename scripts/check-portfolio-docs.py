#!/usr/bin/env python3
"""Validate relative Markdown links and unique ADR identifiers."""
from __future__ import annotations

import re
import sys
from pathlib import Path
from urllib.parse import unquote

ROOT = Path(__file__).resolve().parents[1]
LINK = re.compile(r"!?(?:\\[[^\\]]*\\])\\(([^)]+)\\)")
ADR_ID = re.compile(r"^ADR-(\\d+)-")
SKIP = {".git", ".gradle", ".idea", "build", "node_modules"}
errors: list[str] = []
checked = 0

for markdown in sorted(ROOT.rglob("*.md")):
    relative = markdown.relative_to(ROOT)
    if any(part in SKIP for part in relative.parts):
        continue
    for raw in LINK.findall(markdown.read_text(encoding="utf-8")):
        address = raw.strip().strip("<>")
        if not address or address.startswith(("#", "https://", "http://", "mailto:", "data:")):
            continue
        # Only local path existence is checked; remote URL and anchor validity are outside this gate.
        address = unquote(address.split("#", 1)[0].split("?", 1)[0])
        target = (markdown.parent / address).resolve()
        checked += 1
        if not target.is_relative_to(ROOT) or not target.exists():
            errors.append(f"{relative}: missing relative link {raw}")

seen: dict[str, Path] = {}
for adr in sorted((ROOT / "docs" / "adr").glob("ADR-*.md")):
    found = ADR_ID.match(adr.name)
    if not found:
        errors.append(f"ADR filename has no numeric ID: {adr.relative_to(ROOT)}")
        continue
    number = found.group(1)
    if number in seen:
        errors.append(
            f"duplicate ADR-{number}: {seen[number].relative_to(ROOT)} and {adr.relative_to(ROOT)}"
        )
    seen[number] = adr

if errors:
    for error in errors:
        print("ERROR:", error, file=sys.stderr)
    sys.exit(1)

print(f"PASS: {checked} relative Markdown links; {len(seen)} unique ADR identifiers")
