#!/usr/bin/env python3
"""Run from android/: protect release and isolate the profileable benchmark app."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

A = '{http://schemas.android.com/apk/res/android}'
def manifest(variant):
    paths = list(Path('app/build/intermediates/merged_manifests', variant).rglob('AndroidManifest.xml'))
    if len(paths) != 1: sys.exit('FAIL: expected one ' + variant + ' merged manifest')
    return ET.parse(paths[0]).getroot()
bench = manifest('benchmark'); release = manifest('release')
bapp = bench.find('application'); rapp = release.find('application')
if bapp is None or rapp is None: sys.exit('FAIL: application missing')
if bench.get('package') != 'dev.fanstage.android.benchmark' or release.get('package') != 'dev.fanstage.android':
    sys.exit('FAIL: benchmark package is not isolated')
if bapp.get(A+'debuggable', 'false') != 'false' or rapp.get(A+'debuggable', 'false') != 'false':
    sys.exit('FAIL: benchmark or release is debuggable')
if bapp.get(A+'usesCleartextTraffic') != 'false' or rapp.get(A+'usesCleartextTraffic') != 'false':
    sys.exit('FAIL: cleartext enabled')
profile = bapp.find('profileable')
if profile is None or profile.get(A+'shell') != 'true' or rapp.find('profileable') is not None:
    sys.exit('FAIL: profileable differs from benchmark-only policy')
def fixture(app): return [a for a in app.findall('activity') if a.get(A+'name', '').endswith('.BenchmarkRoomActivity')]
if len(fixture(bapp)) != 1 or fixture(rapp): sys.exit('FAIL: benchmark fixture leaked into release')
print('PASS: isolated non-debuggable profileable benchmark; release boundary preserved')
