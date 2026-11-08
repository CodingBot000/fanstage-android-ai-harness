#!/usr/bin/env python3
"""Check published audit evidence, JUnit counts and Git provenance (not private originals)."""
from __future__ import annotations

import argparse
from collections import Counter
import hashlib
import json
import math
import re
import statistics
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path, PurePosixPath

REPO = Path(__file__).resolve().parents[1]
EVIDENCE = REPO / 'docs/evidence/verification/audit-20261108'
SHA256 = re.compile(r'^[0-9a-f]{64}$')
COMMIT = re.compile(r'^[0-9a-f]{40}$')
REQUIRED = {'additional-ui-physical.xml',
 'backend-regression.json',
 'branch-policy.json',
 'cold-start-observations.json',
 'hilt-activity.xml',
 'hilt-baseline.xml',
 'hilt-physical-cleanup.xml',
 'hilt-physical.xml',
 'historical-manifest-check.json',
 'real-chat-30-physical.xml',
 'real-chat-physical.xml',
 'resource-lifecycle-physical.xml',
 'resource-observations.json',
 'rotation-repeat-1.xml',
 'rotation-repeat-2.xml',
 'rotation-repeat-3.xml',
 'rotation-repeat-4.xml',
 'rotation-repeat-5.xml',
 'rotation-retry.xml',
 'synthetic-media-physical.xml',
 'ui-physical-final.xml',
 'ui-physical-initial.xml',
 'ui-physical-retry.xml',
 'unit/app/TEST-dev.fanstage.android.CommunityActionTest.xml',
 'unit/app/TEST-dev.fanstage.android.LoadCancellationTest.xml',
 'unit/app/TEST-dev.fanstage.android.MessagesViewModelTest.xml',
 'unit/core/data/TEST-dev.fanstage.android.core.data.FakeRepositoriesTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.FakeRoomRepositoryTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.PlaybackSessionTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.PlayerSelectionRulesTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.RoomListViewModelTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.TrackGroupIdsTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.chat.ChatTimestampTest.xml',
 'unit/feature/live/TEST-dev.fanstage.android.feature.live.chat.ChatViewModelTest.xml'}
REQUIRED |= {'unit-rotation-final/' + path.removeprefix('unit/') for path in REQUIRED if path.startswith('unit/')}
REQUIRED |= {f'rotation-instrumented-{phase}-{index:02}.xml' for phase in ('initial', 'final') for index in range(1, 21)}
REQUIRED |= {'rotation-diagnostic-preflight.xml', 'rotation-intentional-negative.xml', 'rotation-initial-trace.json', 'rotation-final-trace.json', 'rotation-source-fingerprints.json', 'rotation-final-regression.xml', 'rotation-final-hilt.xml', 'rotation-negative-fresh.xml'}
PINNED_FAILURE_HASHES = {
    'ui-physical-initial.xml': '3e8079711b8f95475e3e6c2cc1ee174b203dc70ea854a6672660c890f926f18a',
    'rotation-intentional-negative.xml': '1bbb14e078cc84e9680d82b9d9024d37e56bc17147083a8bdbe47b1c3bf5097d',
    'rotation-negative-fresh.xml': '25f7555fee38aba1a787c9a6db6b46037d2f30016196e2e9b0048510461643ae',
}
PINNED_FAILURES = {
    'ui-physical-initial.xml': {
        ('dev.fanstage.android.PlayerSettingsTest', 'fullscreenRestoresAfterActualRotation', 'failure')
    },
    'rotation-intentional-negative.xml': {
        ('dev.fanstage.android.RotationAuditFailureProbe', 'controlledFailureCarriesRotationEvents', 'failure')
    },
    'rotation-negative-fresh.xml': {
        ('dev.fanstage.android.RotationAuditFailureProbe', 'controlledFailureCarriesRotationEvents', 'failure')
    },
}

class EvidenceError(ValueError):
    pass


def fail(message: str) -> None:
    raise EvidenceError(message)


def pairs_no_duplicates(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            fail(f'duplicate JSON key: {key}')
        result[key] = value
    return result


def read_json(path: Path):
    try:
        return json.loads(path.read_text(encoding='utf-8'), object_pairs_hook=pairs_no_duplicates,
                          parse_constant=lambda value: fail(f'invalid JSON number: {value}'))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        fail(f'invalid JSON {path.name}: {error}')


def count(value, label):
    if isinstance(value, bool) or not isinstance(value, int) or value < 0:
        fail(f'{label}: expected nonnegative integer')
    return value


def attrs(element, label):
    result = {}
    for name in ('tests', 'failures', 'errors', 'skipped'):
        raw = element.get(name)
        if raw is None or not re.fullmatch(r'0|[1-9][0-9]*', raw):
            fail(f'{label}: invalid or missing {name}')
        result[name] = int(raw)
    return result


def xml_counts(path: Path):
    try:
        raw = path.read_bytes()
        text = raw.decode('utf-8-sig')
        if '\x00' in text or '<!DOCTYPE' in text.upper() or '<!ENTITY' in text.upper():
            fail(f'{path.name}: XML DTD/entity or non-UTF-8 encoding not allowed')
        root = ET.fromstring(raw)
    except (OSError, UnicodeError, ET.ParseError) as error:
        fail(f'{path.name}: invalid XML: {error}')
    if root.tag not in ('testsuite', 'testsuites'):
        fail(f'{path.name}: unexpected JUnit root')
    for parent in root.iter():
        if any(child.tag == 'testcase' and parent.tag != 'testsuite' for child in parent):
            fail(f'{path.name}: testcase outside a testsuite')
    suites = list(root.iter('testsuite'))
    if not suites:
        fail(f'{path.name}: no test suites')
    all_cases = list(root.iter('testcase'))
    if not all_cases:
        fail(f'{path.name}: no test cases')
    failures = set()
    def verify(element, label):
        cases = list(element.iter('testcase'))
        calculated = {'tests': len(cases), 'failures': 0, 'errors': 0, 'skipped': 0}
        identities = set()
        for case in cases:
            cls, name = case.get('classname'), case.get('name')
            if not cls or not name or (cls, name) in identities:
                fail(f'{label}: missing or duplicate testcase identity')
            identities.add((cls, name))
            statuses = [kind for kind in ('failure', 'error', 'skipped') if case.find(kind) is not None]
            if len(statuses) > 1:
                fail(f'{label}: contradictory testcase statuses')
            if statuses:
                kind = statuses[0]
                calculated[{'failure':'failures', 'error':'errors', 'skipped':'skipped'}[kind]] += 1
                if kind != 'skipped':
                    node = case.find(kind)
                    if not (''.join(node.itertext()).strip() or (node.get('message') or '').strip()):
                        fail(f'{label}: empty failure detail')
                    failures.add((cls, name, kind))
        if attrs(element, label) != calculated:
            fail(f'{label}: JUnit declared counts do not match testcase counts')
        return calculated
    for i, suite in enumerate(suites):
        verify(suite, f'{path.name}:suite[{i}]')
    totals = verify(root, f'{path.name}:root')
    return totals, failures, {(case.get('classname'), case.get('name')) for case in all_cases}


def commit_provenance(entry, git_check):
    head = entry.get('head_at_execution')
    snapshot = entry.get('tested_code_snapshot_commit')
    if head is None and snapshot is None:
        if entry.get('file') not in {'cold-start-observations.json', 'historical-manifest-check.json', 'branch-policy.json'}:
            fail(f"{entry['file']}: missing source provenance")
        return  # These observations do not have a single tested-code snapshot.
    if not isinstance(head, str) or not COMMIT.fullmatch(head) or not isinstance(snapshot, str) or not COMMIT.fullmatch(snapshot):
        fail(f"{entry['file']}: invalid source commit SHA")
    explanation = entry.get('snapshot_interpretation')
    if not isinstance(explanation, str) or not explanation.strip():
        fail(f"{entry['file']}: missing execution/snapshot interpretation")
    if head != snapshot and ('uncommitted' not in explanation.lower() or 'after execution' not in explanation.lower()):
        fail(f"{entry['file']}: pre-commit execution is not explicitly explained")
    if head == snapshot and 'head at execution' not in explanation.lower():
        fail(f"{entry['file']}: matching HEAD is not explicitly explained")
    if git_check:
        for sha in (head, snapshot):
            if subprocess.run(['git', '-C', str(REPO), 'cat-file', '-e', sha+'^{commit}'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
                fail(f"{entry['file']}: commit not available: {sha}")
        if head != snapshot and subprocess.run(['git', '-C', str(REPO), 'merge-base', '--is-ancestor', head, snapshot], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
            fail(f"{entry['file']}: execution HEAD is not an ancestor of later snapshot")


def check_cold(data, git_check=False):
    observations = data.get('observations') if isinstance(data, dict) else None
    if not isinstance(observations, list) or len(observations) != 2 or any(not isinstance(x, dict) for x in observations):
        fail('cold-start: expected baseline and candidate')
    if {x.get('label') for x in observations if isinstance(x, dict)} != {'baseline', 'candidate'}:
        fail('cold-start: invalid labels')
    for x in observations:
        runs = x.get('iterations_ms')
        if not isinstance(runs, list) or len(runs) != 10 or any(isinstance(v, bool) or not isinstance(v, (float, int)) or not math.isfinite(v) or v <= 0 for v in runs):
            fail('cold-start: invalid published repetitions')
        for name, expected in [('median_ms', statistics.median(runs)), ('p95_nearest_rank_ms', sorted(runs)[math.ceil(.95*len(runs))-1]), ('sample_stdev_ms', statistics.stdev(runs))]:
            value = x.get(name)
            if isinstance(value, bool) or not isinstance(value, (float, int)) or not math.isclose(value, expected, rel_tol=1e-9, abs_tol=1e-9):
                fail(f'cold-start: {name} inconsistent')
        if not isinstance(x.get('source_commit'), str) or not COMMIT.fullmatch(x['source_commit']):
            fail('cold-start: invalid source commit')
        if git_check and subprocess.run(['git', '-C', str(REPO), 'cat-file', '-e', x['source_commit']+'^{commit}'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode:
            fail('cold-start: source commit not available')
        for key in ('raw_benchmark_json_sha256', 'private_manifest_sha256'):
            if not isinstance(x.get(key), str) or not SHA256.fullmatch(x[key]):
                fail(f'cold-start: invalid {key}')
        hashes = x.get('perfetto_trace_sha256')
        if not isinstance(hashes, list) or len(hashes) != 10 or any(not isinstance(h, str) or not SHA256.fullmatch(h) for h in hashes):
            fail('cold-start: invalid private trace hash list')


ROTATION_SOURCE_PATHS = {
    'android/app/src/androidTest/kotlin/dev/fanstage/android/PlayerSettingsTest.kt',
    'android/app/src/androidTest/kotlin/dev/fanstage/android/RotationAudit.kt',
    'android/app/src/androidTest/kotlin/dev/fanstage/android/RotationAuditTest.kt',
    'android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/Media3PlayerController.kt',
    'android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/PlayerPanel.kt',
    'android/feature/live/src/debug/kotlin/dev/fanstage/android/feature/live/PlayerRotationTrace.kt',
    'android/feature/live/src/release/kotlin/dev/fanstage/android/feature/live/PlayerRotationTrace.kt',
}


def check_rotation_fingerprints(data, git_check=False):
    if not isinstance(data, dict): fail('rotation fingerprints: invalid object')
    for phase in ('initial', 'final'):
        entry = data.get(phase)
        if not isinstance(entry, dict): fail('rotation fingerprints: missing phase')
        head, snapshot, files = entry.get('head_at_execution'), entry.get('tested_code_snapshot_commit'), entry.get('files')
        if not isinstance(head, str) or not COMMIT.fullmatch(head) or not isinstance(snapshot, str) or not COMMIT.fullmatch(snapshot):
            fail('rotation fingerprints: invalid commit')
        if head == snapshot or not any(word in str(entry.get('note','')).lower() for word in ('modified','uncommitted')):
            fail('rotation fingerprints: pre-commit execution is not disclosed')
        if not isinstance(files, dict) or set(files) != ROTATION_SOURCE_PATHS:
            fail('rotation fingerprints: relevant source file set differs')
        for path, digest in files.items():
            if not isinstance(digest, str) or not SHA256.fullmatch(digest): fail('rotation fingerprints: invalid source file hash')
            if git_check:
                result = subprocess.run(['git','-C',str(REPO),'show',f'{snapshot}:{path}'],capture_output=True)
                if result.returncode or hashlib.sha256(result.stdout).hexdigest() != digest:
                    fail(f'rotation fingerprints: committed file hash mismatch: {path}')
        if git_check and subprocess.run(['git','-C',str(REPO),'merge-base','--is-ancestor',head,snapshot],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode:
            fail('rotation fingerprints: execution HEAD is not an ancestor of the snapshot')


ROTATION_EVENTS = {
    'test_start', 'activity_created', 'activity_destroyed', 'activity_started', 'activity_stopped',
    'activity_resumed', 'activity_paused', 'player_events', 'compose_recompose',
    'compose_state_observed', 'dialog_created', 'dialog_disposed', 'surface_attached',
    'surface_detached', 'listener_registered', 'listener_removed', 'player_released', 'ui_checkpoint',
}


def check_rotation_trace(data, name):
    prefix = 'finalRotation' if name == 'rotation-final-trace.json' else 'rotation'
    if not isinstance(data, dict) or not isinstance(data.get('source_log_sha256'), str) or not SHA256.fullmatch(data['source_log_sha256']):
        fail(f'{name}: invalid private source log hash')
    lines = data.get('events')
    if not isinstance(lines, list) or not lines:
        fail(f'{name}: no diagnostic events')
    runs = {}
    pattern = re.compile(r'^run=(' + prefix + r'[0-9]{2}) ns=([0-9]+) event=([A-Za-z_]+)(?: |$)')
    for line in lines:
        if not isinstance(line, str) or not re.fullmatch(r'[A-Za-z0-9_=,:. -]+', line):
            fail(f'{name}: invalid diagnostic line')
        match = pattern.match(line)
        if not match: fail(f'{name}: invalid run, timestamp or event')
        runs.setdefault(match.group(1), []).append((int(match.group(2)), match.group(3), line))
    if set(runs) != {f'{prefix}{i:02}' for i in range(1, 21)}:
        fail(f'{name}: expected exactly 20 run IDs')
    for run, events in runs.items():
        if any(a[0] > b[0] for a, b in zip(events, events[1:])):
            fail(f'{name}: nonmonotonic events for {run}')
        if not ROTATION_EVENTS.issubset({event for _, event, _ in events}):
            fail(f'{name}: missing diagnostic event for {run}')
        lines_for_run = [line for _, _, line in events]
        for checkpoint in ('playing_before_fullscreen', 'fullscreen_before_rotation', 'landscape_paused', 'room_exited'):
            if not any(f'event=ui_checkpoint name={checkpoint} ' in line for line in lines_for_run):
                fail(f'{name}: missing {checkpoint} for {run}')
        if prefix == 'finalRotation':
            def listener_ids(kind):
                pairs = []
                for line in lines_for_run:
                    if f'event={kind} ' in line:
                        player = re.search(r'\bplayer=([0-9]+)\b', line)
                        listener = re.search(r'\blistener=([0-9]+)\b', line)
                        if not player or not listener: fail(f'{name}: invalid listener event for {run}')
                        pairs.append((player.group(1), listener.group(1)))
                return Counter(pairs)
            if listener_ids('listener_registered') != listener_ids('listener_removed'):
                fail(f'{name}: listener registration/removal events differ for {run}')
            if not any('event=player_snapshot name=fullscreen_before_rotation ' in line and re.search(r'\bsurface=[1-9][0-9]*\b', line) and 'available=true' in line for line in lines_for_run):
                fail(f'{name}: fullscreen surface observation missing for {run}')
            if not any('event=player_snapshot name=room_exited views=0' in line for line in lines_for_run):
                fail(f'{name}: room-exit observation missing for {run}')
            def player_ids(checkpoint):
                return {match.group(1) for line in lines_for_run
                        if f'event=player_snapshot name={checkpoint} ' in line
                        for match in [re.search(r'\bplayer=([1-9][0-9]*)\b', line)] if match}
            before, fullscreen, landscape = (player_ids(x) for x in ('playing_before_fullscreen', 'fullscreen_before_rotation', 'landscape_paused'))
            if not before.intersection(fullscreen) or not landscape or fullscreen.intersection(landscape):
                fail(f'{name}: Player identity observations differ from the recorded transition for {run}')
            if not any('event=player_snapshot name=landscape_paused ' in line and re.search(r'\bsurface=[1-9][0-9]*\b', line) and 'available=true' in line for line in lines_for_run):
                fail(f'{name}: rotated surface observation missing for {run}')
            for checkpoint, state in (('playing_before_fullscreen', 'playing=true'), ('fullscreen_before_rotation', 'playing=true'), ('landscape_paused', 'playing=false')):
                if not any(f'event=player_snapshot name={checkpoint} ' in line and state in line and re.search(r'\bplayer=[1-9][0-9]*\b', line) for line in lines_for_run):
                    fail(f'{name}: missing player snapshot for {run}/{checkpoint}')


def validate(directory: Path, required=REQUIRED, pinned_failures=PINNED_FAILURES, pinned_hashes=PINNED_FAILURE_HASHES, git_check=True):
    directory = directory.resolve(strict=True)
    manifest = read_json(directory / 'manifest.json')
    if not isinstance(manifest, dict) or isinstance(manifest.get('schema_version', 1), bool) or not isinstance(manifest.get('schema_version', 1), int) or manifest.get('schema_version', 1) not in (1, 2):
        fail('unsupported manifest schema')
    version = manifest.get('schema_version', 1)
    entries = manifest.get('results')
    if not isinstance(entries, list) or not entries:
        fail('manifest: results must be a nonempty list')
    seen = set()
    by_name = {}
    for entry in entries:
        if not isinstance(entry, dict): fail('manifest: entry must be an object')
        name = entry.get('file')
        if not isinstance(name, str) or not name or '\\' in name or '\x00' in name:
            fail('manifest: invalid path')
        pure = PurePosixPath(name)
        if pure.is_absolute() or any(part in ('.', '..') for part in name.split('/')) or str(pure) != name:
            fail(f'manifest: noncanonical or escaping path: {name}')
        if name in seen: fail(f'manifest: duplicate path: {name}')
        seen.add(name)
        by_name[name] = entry
        cursor = directory
        for part in pure.parts:
            cursor = cursor / part
            if cursor.is_symlink(): fail(f'manifest: symlink not allowed: {name}')
        path = cursor.resolve()
        if not path.is_relative_to(directory) or not path.is_file():
            fail(f'manifest: missing or escaping file: {name}')
        expected = entry.get('sha256')
        if not isinstance(expected, str) or not SHA256.fullmatch(expected) or hashlib.sha256(path.read_bytes()).hexdigest() != expected:
            fail(f'{name}: SHA-256 mismatch')
        if name in pinned_hashes and expected != pinned_hashes[name]:
            fail(f'{name}: historical failure evidence hash changed')
        original = entry.get('original_sha256')
        if original is not None and (not isinstance(original, str) or not SHA256.fullmatch(original)):
            fail(f'{name}: invalid original SHA-256 format')
        commit_provenance(entry, git_check)
        if path.suffix == '.xml':
            actual, failures, identities = xml_counts(path)
            if re.fullmatch(r'rotation-instrumented-(initial|final)-[0-9]{2}\.xml', name):
                expected_identity = {('dev.fanstage.android.PlayerSettingsTest', 'fullscreenRestoresAfterActualRotation')}
                if actual['tests'] != 1 or identities != expected_identity:
                    fail(f'{name}: incorrect rotation testcase identity')
            declared = {k: count(entry.get(k), f'{name}:{k}') for k in actual}
            if actual != declared: fail(f'{name}: manifest/JUnit count mismatch')
            outcome = 'FAIL' if actual['failures'] or actual['errors'] else 'SKIP' if actual['skipped'] else 'PASS'
            if version == 2 and entry.get('outcome') != outcome:
                fail(f'{name}: outcome does not match test results')
            declared_failures = entry.get('expected_failure_cases', [])
            if not isinstance(declared_failures, list): fail(f'{name}: invalid failure declaration')
            declared_set = set()
            for item in declared_failures:
                if not isinstance(item, dict) or set(item) != {'classname','name','kind'} or any(not isinstance(item[k], str) or not item[k] for k in item):
                    fail(f'{name}: invalid failure declaration')
                declared_set.add((item['classname'], item['name'], item['kind']))
            if len(declared_set) != len(declared_failures): fail(f'{name}: duplicate failure declaration')
            if version == 1 and name in pinned_failures and not declared_set and 'failure' in str(entry.get('purpose','')).lower():
                declared_set = pinned_failures[name]  # Legacy v1: known failure explicitly identified by policy and purpose.
            if failures != declared_set or (name in pinned_failures and failures != pinned_failures[name]):
                fail(f'{name}: historical failure detail does not match declaration')
        elif path.suffix == '.json':
            data = read_json(path)
            if name == 'cold-start-observations.json': check_cold(data, git_check=git_check)
            if name in ('rotation-initial-trace.json', 'rotation-final-trace.json'): check_rotation_trace(data, name)
            if name == 'rotation-source-fingerprints.json': check_rotation_fingerprints(data, git_check=git_check)
        else:
            fail(f'{name}: unsupported evidence type')
    if 'rotation-source-fingerprints.json' in seen:
        fingerprints = read_json(directory / 'rotation-source-fingerprints.json')
        for phase in ('initial', 'final'):
            source = fingerprints[phase]
            related = [f'rotation-{phase}-trace.json'] + [f'rotation-instrumented-{phase}-{i:02}.xml' for i in range(1, 21)]
            for artifact in related:
                entry = by_name.get(artifact)
                if not entry or any(entry.get(key) != source.get(key) for key in ('head_at_execution','tested_code_snapshot_commit')):
                    fail(f'{artifact}: provenance does not match {phase} source fingerprint')
    if not set(required).issubset(seen):
        fail('manifest: missing required evidence: ' + ', '.join(sorted(set(required)-seen)))
    items = list(directory.rglob('*'))
    if any(item.is_symlink() for item in items):
        fail('evidence tree contains a symlink')
    public = {str(p.relative_to(directory)) for p in items if p.is_file() and p.suffix in ('.xml','.json') and p != directory / 'manifest.json'}
    if public != seen:
        fail('manifest: unregistered or missing public evidence: ' + ', '.join(sorted(public ^ seen)))
    return len(seen)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--directory', type=Path, default=EVIDENCE)
    args = parser.parse_args()
    try:
        total = validate(args.directory)
    except (EvidenceError, OSError, subprocess.SubprocessError) as error:
        print(f'FAIL: {error}', file=sys.stderr); return 1
    print(f'PASS: {total} published evidence files, JUnit counts and available Git provenance')
    print('LIMIT: private originals and dirty-worktree identity cannot be independently verified from these files')
    return 0


if __name__ == '__main__':
    sys.exit(main())
