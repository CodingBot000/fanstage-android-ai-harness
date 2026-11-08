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
    def test_cold_statistics_checked(self):
        values=list(range(1,11)); good={'observations': []}
        for label in ('baseline','candidate'):
            good['observations'].append({'label':label,'iterations_ms':values,'median_ms':5.5,'p95_nearest_rank_ms':10,'sample_stdev_ms':__import__('statistics').stdev(values),'source_commit':'a'*40,'raw_benchmark_json_sha256':'a'*64,'private_manifest_sha256':'b'*64,'perfetto_trace_sha256':['c'*64]*10})
        check.check_cold(good);good['observations'][0]['p95_nearest_rank_ms']=9
        with self.assertRaises(check.EvidenceError): check.check_cold(good)

if __name__ == '__main__': unittest.main()
