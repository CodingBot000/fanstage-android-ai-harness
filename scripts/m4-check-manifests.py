#!/usr/bin/env python3
"""Run from android/: verify cleartext is enabled in debug only."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

attribute = "{http://schemas.android.com/apk/res/android}usesCleartextTraffic"
for variant, expected in (("debug", "true"), ("release", "false")):
    manifests = list(Path("app/build/intermediates/merged_manifests", variant).rglob("AndroidManifest.xml"))
    if len(manifests) != 1:
        sys.exit(f"FAIL: expected one merged {variant} manifest")
    application = ET.parse(manifests[0]).getroot().find("application")
    if application is None or application.get(attribute) != expected:
        sys.exit(f"FAIL: unexpected {variant} cleartext setting")
print("PASS: cleartext enabled only in debug")
