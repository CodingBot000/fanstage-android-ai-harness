#!/usr/bin/env python3
"""Adversarial fixtures for the public evidence integrity gate."""
import copy
import hashlib
import importlib.util
import json
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / 'check-verification-evidence.py'
spec = importlib.util.spec_from_file_location('evidence_check', SCRIPT)
check = importlib.util.module_from_spec(spec)
spec.loader.exec_module(check)

PASS = b'<testsuite name="s" tests="1" failures="0" errors="0" skipped="0"><testcase classname="C" name="ok"/></testsuite>'
FAIL = b'<testsuite name="s" tests="1" failures="1" errors="0" skipped="0"><testcase classname="C" name="historic"><failure>timeout</failure></testcase></testsuite>'

class EvidenceTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.root.joinpath('pass.xml').write_bytes(PASS)
        self.root.joinpath('failure.xml').write_bytes(FAIL)
        common = {'head_at_execution': 'a'*40, 'tested_code_snapshot_commit': 'a'*40, 'snapshot_interpretation': 'HEAD at execution'}
        self.manifest = {'schema_version': 2, 'results': [
            dict(common, file='pass.xml', sha256=hashlib.sha256(PASS).hexdigest(), tests=1, failures=0, errors=0, skipped=0, outcome='PASS'),
            dict(common, file='failure.xml', sha256=hashlib.sha256(FAIL).hexdigest(), tests=1, failures=1, errors=0, skipped=0, outcome='FAIL', purpose='historical failure preserved', expected_failure_cases=[{'classname':'C','name':'historic','kind':'failure'}]),
        ]}
        self.pinned = {'failure.xml': {('C', 'historic', 'failure')}}
        self.save()

    def save(self):
        (self.root/'manifest.json').write_text(json.dumps(self.manifest))

    def validate(self):
        return check.validate(self.root, required={'pass.xml','failure.xml'}, pinned_failures=self.pinned, pinned_hashes={'failure.xml':hashlib.sha256(FAIL).hexdigest()}, git_check=False)

    def assert_bad(self):
        self.save()
        with self.assertRaises(check.EvidenceError): self.validate()

    def rehash(self, name):
        entry = next(e for e in self.manifest['results'] if e['file']==name)
        entry['sha256'] = hashlib.sha256((self.root/name).read_bytes()).hexdigest()

    def test_cli_rejects_tampered_copy_of_public_evidence(self):
        copy_root = self.root / 'public-copy'
        shutil.copytree(check.EVIDENCE, copy_root)
        target = copy_root / 'hilt-physical.xml'
        target.write_bytes(target.read_bytes() + b' ')
        result = subprocess.run([sys.executable, str(SCRIPT), '--directory', str(copy_root)], capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('SHA-256 mismatch', result.stderr)

    def test_rotation_xml_identity_tamper_rejected_even_if_hash_updated(self):
        copy_root = self.root / 'rotation-copy'
        shutil.copytree(check.EVIDENCE, copy_root)
        target = copy_root / 'rotation-instrumented-final-01.xml'
        target.write_bytes(target.read_bytes().replace(b'fullscreenRestoresAfterActualRotation', b'wrongRotationCase'))
        path = copy_root/'manifest.json'; manifest = json.loads(path.read_text())
        next(e for e in manifest['results'] if e['file']==target.name)['sha256'] = hashlib.sha256(target.read_bytes()).hexdigest()
        path.write_text(json.dumps(manifest))
        result = subprocess.run([sys.executable, str(SCRIPT), '--directory', str(copy_root)], capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('incorrect rotation testcase identity', result.stderr)

    def test_final_real_chat_case_tamper_is_rejected(self):
        copy_root = self.root / 'chat-copy'
        shutil.copytree(check.EVIDENCE, copy_root)
        target = copy_root / 'rotation-final-real-chat.xml'
        target.write_bytes(target.read_bytes().replace(b'thirtyRealRoomSubscriptionsCloseTheirSocketsAndCollectors', b'wrongChatCase'))
        path = copy_root/'manifest.json'; manifest = json.loads(path.read_text())
        next(e for e in manifest['results'] if e['file']==target.name)['sha256'] = hashlib.sha256(target.read_bytes()).hexdigest()
        path.write_text(json.dumps(manifest))
        result = subprocess.run([sys.executable, str(SCRIPT), '--directory', str(copy_root)], capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('incorrect final real-chat testcase identities', result.stderr)

    def test_rotation_provenance_must_match_fingerprint(self):
        copy_root = self.root / 'provenance-copy'
        shutil.copytree(check.EVIDENCE, copy_root)
        path = copy_root/'manifest.json'; manifest = json.loads(path.read_text())
        entry = next(e for e in manifest['results'] if e['file']=='rotation-instrumented-final-01.xml')
        entry['tested_code_snapshot_commit'] = entry['head_at_execution']
        entry['snapshot_interpretation'] = 'HEAD at execution'
        path.write_text(json.dumps(manifest))
        result = subprocess.run([sys.executable, str(SCRIPT), '--directory', str(copy_root)], capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('provenance does not match', result.stderr)

    def test_valid_pass_and_historical_failure(self): self.assertEqual(2, self.validate())
    def test_v1_legacy_failure_compatibility(self):
        self.manifest.pop('schema_version')
        for e in self.manifest['results']: e.pop('outcome'); e.pop('expected_failure_cases', None)
        self.save(); self.assertEqual(2, self.validate())
    def test_missing_file(self): (self.root/'pass.xml').unlink(); self.assert_bad()
    def test_hash_mismatch(self): (self.root/'pass.xml').write_bytes(PASS+b' '); self.assert_bad()
    def test_utf16_entity_xml_rejected_even_with_updated_hash(self):
        xml = '<?xml version="1.0" encoding="UTF-16"?><!DOCTYPE testsuite [<!ENTITY x "value">]><testsuite name="s" tests="1" failures="0" errors="0" skipped="0"><testcase classname="C" name="&x;"/></testsuite>'
        (self.root/'pass.xml').write_bytes(xml.encode('utf-16')); self.rehash('pass.xml'); self.assert_bad()
    def test_manifest_test_count_mismatch(self): self.manifest['results'][0]['tests']=2; self.assert_bad()
    def test_manifest_failure_count_mismatch(self): self.manifest['results'][1]['failures']=0; self.assert_bad()
    def test_duplicate_manifest_entry(self): self.manifest['results'].append(copy.deepcopy(self.manifest['results'][0])); self.assert_bad()
    def test_invalid_xml_even_with_updated_hash(self): (self.root/'pass.xml').write_bytes(b'<broken>'); self.rehash('pass.xml'); self.assert_bad()
    def test_path_traversal_or_noncanonical(self):
        for path in ('../outside.xml','/tmp/outside.xml','x/../pass.xml','./pass.xml','x//pass.xml','x\\pass.xml'):
            with self.subTest(path=path):
                self.manifest['results'][0]['file']=path
                self.assert_bad()
    def test_symlink_escape(self):
        with tempfile.TemporaryDirectory() as d:
            outside=Path(d)/'outside.xml'; outside.write_bytes(PASS)
            (self.root/'link.xml').symlink_to(outside)
            self.manifest['results'][0]['file']='link.xml'; self.assert_bad()
    def test_unregistered_symlink_directory_is_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            (Path(d)/'hidden.xml').write_bytes(PASS)
            (self.root/'linked-dir').symlink_to(d, target_is_directory=True)
            self.assert_bad()
    def test_required_missing_even_when_file_and_entry_removed(self):
        (self.root/'pass.xml').unlink(); self.manifest['results'].pop(0); self.assert_bad()
    def test_unregistered_evidence(self): (self.root/'extra.json').write_text('{}'); self.assert_bad()
    def test_unregistered_nested_manifest_is_not_exempt(self):
        (self.root/'nested').mkdir(); (self.root/'nested/manifest.json').write_text('{}'); self.assert_bad()
    def test_missing_provenance_is_rejected(self):
        self.manifest['results'][0].pop('head_at_execution'); self.manifest['results'][0].pop('tested_code_snapshot_commit'); self.assert_bad()
    def test_suite_count_mismatch_with_updated_hash(self):
        (self.root/'pass.xml').write_bytes(PASS.replace(b'tests="1"',b'tests="2"')); self.rehash('pass.xml'); self.assert_bad()
    def test_orphan_testcase_outside_suite_is_rejected(self):
        orphan = b'<testsuites tests="2" failures="0" errors="0" skipped="0">' + PASS + b'<testcase classname="C" name="orphan"/></testsuites>'
        (self.root/'pass.xml').write_bytes(orphan); self.rehash('pass.xml'); self.manifest['results'][0]['tests']=2
        self.assert_bad()
    def test_historical_failure_erasure_even_if_hash_and_manifest_changed(self):
        (self.root/'failure.xml').write_bytes(PASS); self.rehash('failure.xml')
        e=self.manifest['results'][1];e.update(failures=0,outcome='PASS',expected_failure_cases=[])
        self.assert_bad()
    def test_historical_failure_detail_tampered_even_if_hash_updated(self):
        (self.root/'failure.xml').write_bytes(FAIL.replace(b'timeout',b'other failure'));self.rehash('failure.xml');self.assert_bad()
    def test_failure_details_must_be_nonempty(self):
        (self.root/'failure.xml').write_bytes(FAIL.replace(b'>timeout</failure>',b'></failure>'));self.rehash('failure.xml');self.assert_bad()
    def test_incorrect_precommit_provenance(self):
        self.manifest['results'][0]['tested_code_snapshot_commit']='b'*40; self.assert_bad()
    def test_boolean_count_rejected(self): self.manifest['results'][0]['tests']=True;self.assert_bad()
    def test_duplicate_json_keys_rejected(self):
        (self.root/'manifest.json').write_text('{"results":[],"results":[]}')
        with self.assertRaises(check.EvidenceError): self.validate()
    def test_rotation_source_fingerprints_require_all_files(self):
        phase={'head_at_execution':'a'*40, 'tested_code_snapshot_commit':'b'*40,
               'note':'source files were modified', 'files':{path:'c'*64 for path in check.ROTATION_SOURCE_PATHS}}
        data={'initial':copy.deepcopy(phase),'final':copy.deepcopy(phase)}
        check.check_rotation_fingerprints(data)
        data['final']['files'].pop(next(iter(check.ROTATION_SOURCE_PATHS)))
        with self.assertRaises(check.EvidenceError): check.check_rotation_fingerprints(data)

    def test_rotation_trace_requires_all_runs_and_monotonic_events(self):
        data={'source_log_sha256':'a'*64, 'events':[]}
        for index in range(1,21):
            run=f'rotation{index:02}'
            events=sorted(check.ROTATION_EVENTS)
            for number,event in enumerate(events,1):
                extra=' name=playing_before_fullscreen states=synthetic' if event=='ui_checkpoint' else ''
                data['events'].append(f'run={run} ns={number} event={event}{extra}')
            for number,name in enumerate(('fullscreen_before_rotation','landscape_paused','room_exited'),len(events)+1):
                data['events'].append(f'run={run} ns={number} event=ui_checkpoint name={name} states=synthetic')
        check.check_rotation_trace(data,'rotation-initial-trace.json')
        data['events'].pop()
        with self.assertRaises(check.EvidenceError): check.check_rotation_trace(data,'rotation-initial-trace.json')

    def test_final_rotation_trace_rejects_missing_surface_and_exit_observation(self):
        data=json.loads((check.EVIDENCE/'rotation-final-trace.json').read_text())
        check.check_rotation_trace(data,'rotation-final-trace.json')
        for marker in ('event=player_snapshot name=fullscreen_before_rotation ', 'event=player_snapshot name=room_exited views=0'):
            altered=copy.deepcopy(data)
            altered['events']=[line for line in altered['events'] if not (line.startswith('run=finalRotation01 ') and marker in line)]
            with self.subTest(marker=marker), self.assertRaises(check.EvidenceError):
                check.check_rotation_trace(altered,'rotation-final-trace.json')

    def test_final_rotation_trace_rejects_changed_fullscreen_player(self):
        import re
        data=json.loads((check.EVIDENCE/'rotation-final-trace.json').read_text())
        for index,line in enumerate(data['events']):
            if line.startswith('run=finalRotation01 ') and 'event=player_snapshot name=fullscreen_before_rotation ' in line:
                data['events'][index]=re.sub(r'\bplayer=[1-9][0-9]*', 'player=999999999', line)
                break
        else: self.fail('fullscreen observation missing')
        with self.assertRaises(check.EvidenceError): check.check_rotation_trace(data,'rotation-final-trace.json')

    def test_cold_statistics_checked(self):
        values=list(range(1,11)); good={'observations': []}
        for label in ('baseline','candidate'):
            good['observations'].append({'label':label,'iterations_ms':values,'median_ms':5.5,'p95_nearest_rank_ms':10,'sample_stdev_ms':__import__('statistics').stdev(values),'source_commit':'a'*40,'raw_benchmark_json_sha256':'a'*64,'private_manifest_sha256':'b'*64,'perfetto_trace_sha256':['c'*64]*10})
        check.check_cold(good);good['observations'][0]['p95_nearest_rank_ms']=9
        with self.assertRaises(check.EvidenceError): check.check_cold(good)

if __name__ == '__main__': unittest.main()
