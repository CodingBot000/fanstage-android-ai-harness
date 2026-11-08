#!/usr/bin/env python3
"""Classify PR changes and fail closed when a required reusable workflow did not pass."""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys

CHECKS = ('android', 'server', 'docs')
POLICY_FILES = {
    '.github/workflows/required-quality.yml',
    'scripts/quality_gate.py',
    'scripts/tests/test_quality_gate.py',
}
ANDROID_SCRIPTS = {
    'scripts/m1-004-emulator-smoke.sh', 'scripts/ux012-emulator-gate.sh',
    'scripts/m4-check-manifests.py', 'scripts/m6-check-manifests.py',
    'scripts/check-ux-module-boundaries.py',
}
DOC_SCRIPTS = {
    'scripts/check-portfolio-docs.py', 'scripts/check-verification-evidence.py',
    'scripts/tests/test_verification_evidence.py',
}


def classify(paths):
    flags = {key: False for key in CHECKS}
    for path in paths:
        if path in POLICY_FILES:
            return {key: True for key in CHECKS}
        selected = {
            'android': path.startswith('android/') or path in ANDROID_SCRIPTS or path == '.github/workflows/android-ci.yml',
            'server': path.startswith(('backend/', 'android/', 'scripts/m4-')) or path in {'compose.yaml', 'compose.media.yaml', '.github/workflows/m4-integration.yml'},
            'docs': path.startswith('docs/') or path.endswith('.md') or path in DOC_SCRIPTS or path == '.github/workflows/docs-check.yml',
        }
        # An unknown path is not evidence that all checks are irrelevant.
        if not any(selected.values()):
            return {key: True for key in CHECKS}
        for key in CHECKS:
            flags[key] |= selected[key]
    return flags


def evaluate(change_result, flags, results):
    errors = []
    if change_result != 'success': errors.append('change classification did not succeed')
    for key in CHECKS:
        flag, result = flags.get(key), results.get(key)
        if flag not in ('true', 'false'):
            errors.append(f'{key}: missing or invalid selection')
        elif flag == 'true' and result != 'success':
            errors.append(f'{key}: required workflow is {result or "missing"}')
        elif flag == 'false' and result != 'skipped':
            errors.append(f'{key}: excluded workflow should be skipped, got {result or "missing"}')
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='command', required=True)
    changed = sub.add_parser('classify')
    changed.add_argument('base'); changed.add_argument('head')
    sub.add_parser('evaluate')
    args = parser.parse_args()
    if args.command == 'classify':
        if not all(re.fullmatch(r'[0-9a-f]{40}', x) for x in (args.base, args.head)):
            parser.error('base and head must be full commit SHAs')
        raw = subprocess.check_output(['git', 'diff', '--name-only', '--no-renames', '-z', f'{args.base}...{args.head}'])
        paths = [os.fsdecode(p) for p in raw.split(b'\0') if p]
        if not paths:
            raise ValueError('no changed paths; refusing to infer a successful gate')
        for name, value in classify(paths).items(): print(f'{name}={str(value).lower()}')
    else:
        flags = {x: os.environ.get(x.upper() + '_NEEDED', '') for x in CHECKS}
        results = {x: os.environ.get(x.upper() + '_RESULT', '') for x in CHECKS}
        errors = evaluate(os.environ.get('CHANGES_RESULT', ''), flags, results)
        if errors:
            for message in errors: print('FAIL:', message, file=sys.stderr)
            return 1
        for name in CHECKS:
            print(f'{name}: {"PASS" if flags[name] == "true" else "NOT_APPLICABLE (no relevant changes)"}')
        print('PASS: selected checks succeeded and excluded checks were explicitly skipped')
    return 0


if __name__ == '__main__':
    try:
        sys.exit(main())
    except (ValueError, subprocess.SubprocessError) as error:
        print('FAIL:', error, file=sys.stderr); sys.exit(1)
