#!/usr/bin/env python3
"""Run isolated non-debuggable Macrobenchmarks on one explicitly selected physical device."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / 'android'
SERIAL = os.environ.get('ANDROID_SERIAL')
if not SERIAL:
    sys.exit('NOT_RUN: set ANDROID_SERIAL to the authorized physical device')
ADB = shutil.which('adb')
if not ADB: sys.exit('NOT_RUN: adb not found')

def command(*args, check=True):
    return subprocess.run(args, text=True, capture_output=True, check=check, timeout=30).stdout.strip()

def adb(*args, check=True): return command(ADB, '-s', SERIAL, *args, check=check)
def git(*args): return command('git', '-C', str(ROOT), *args)

if git('status', '--porcelain', '--untracked-files=all'):
    sys.exit('NOT_RUN: commit or otherwise resolve the working tree before measuring; source SHA must identify the app')
if adb('get-state') != 'device' or adb('shell', 'getprop', 'ro.kernel.qemu') == '1':
    sys.exit('NOT_RUN: selected target is not an authorized physical device')
if int(adb('shell', 'getprop', 'ro.build.version.sdk')) < 29:
    sys.exit('NOT_RUN: API 29 or later required')
for package in ('dev.fanstage.android.benchmark', 'dev.fanstage.benchmark'):
    if adb('shell', 'pm', 'path', package, check=False):
        sys.exit('NOT_RUN: benchmark package already installed; preserve existing app data and inspect it first')

out = Path.home() / 'Library' / 'Logs' / 'FanStage' / 'M6' / datetime.now().strftime('%Y%m%d-%H%M%S')
out.mkdir(parents=True, exist_ok=False)
os.chmod(out, 0o700)
def snapshot():
    battery = adb('shell', 'dumpsys', 'battery')
    display = adb('shell', 'dumpsys', 'display')
    return {
        'at_utc': datetime.now(timezone.utc).isoformat(),
        'model': adb('shell', 'getprop', 'ro.product.model'),
        'android_release': adb('shell', 'getprop', 'ro.build.version.release'),
        'api': adb('shell', 'getprop', 'ro.build.version.sdk'),
        'battery_lines': [line.strip() for line in battery.splitlines() if re.match(r'^\s*(level|temperature|status|AC powered|USB powered|Wireless powered):', line)],
        'display_refresh_rates': sorted(set(re.findall(r'refreshRate=([0-9.]+)', display))),
    }

def artifacts():
    directory = ANDROID / 'macrobenchmark' / 'build' / 'outputs'
    return {str(p): (p.stat().st_mtime_ns, p.stat().st_size) for p in directory.rglob('*')
            if p.is_file() and (p.name.endswith('benchmarkData.json') or p.name.endswith('.perfetto-trace'))} if directory.exists() else {}

sha = git('rev-parse', 'HEAD')
before = snapshot(); old = artifacts(); start = time.monotonic()
(out / 'environment-before.json').write_text(json.dumps(before, indent=2) + '\n')
print('Physical device preflight PASS; results are stored in a private local directory.', flush=True)
cmd = ['./gradlew', ':macrobenchmark:connectedBenchmarkAndroidTest', '--no-daemon', '--stacktrace']
with (out / 'gradle.log').open('w') as log:
    result = subprocess.run(cmd, cwd=ANDROID, stdout=log, stderr=subprocess.STDOUT, env=os.environ.copy())
after = snapshot(); (out / 'environment-after.json').write_text(json.dumps(after, indent=2) + '\n')
raw = out / 'raw'; raw.mkdir()
files = []
for name, meta in artifacts().items():
    if old.get(name) == meta: continue
    source = Path(name); relative = source.relative_to(ANDROID / 'macrobenchmark' / 'build' / 'outputs')
    dest = raw / relative; dest.parent.mkdir(parents=True, exist_ok=True); shutil.copy2(source, dest)
    files.append({'path': str(dest.relative_to(out)), 'sha256': hashlib.sha256(dest.read_bytes()).hexdigest(), 'bytes': dest.stat().st_size})
manifest = {'source_commit': sha, 'gradle_command': cmd, 'exit_code': result.returncode,
            'elapsed_seconds': round(time.monotonic()-start, 3), 'raw_files': files,
            'target_package': 'dev.fanstage.android.benchmark', 'compilation_mode': 'None',
            'note': 'Device identifier intentionally omitted; traces and log remain local and may contain private system data.'}
(out / 'manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
print('Output directory:', out)
print('Gradle exit code:', result.returncode, 'new raw files:', len(files))
if result.returncode != 0: sys.exit(result.returncode)
if not any(f['path'].endswith('benchmarkData.json') for f in files) or not any(f['path'].endswith('.perfetto-trace') for f in files):
    sys.exit('FAIL: successful runner did not yield both raw JSON and Perfetto trace')
