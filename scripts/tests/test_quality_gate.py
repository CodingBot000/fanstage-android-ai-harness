import unittest
import os
import subprocess
from unittest import mock
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import quality_gate as gate

class QualityGateTests(unittest.TestCase):
    def test_docs_only_skips_unrelated_checks(self):
        self.assertEqual({'android':False,'server':False,'docs':True}, gate.classify(['docs/evidence/verification/sample.xml']))
    def test_android_triggers_android_and_server(self):
        self.assertEqual({'android':True,'server':True,'docs':False}, gate.classify(['android/app/src/main/A.kt']))
    def test_nested_markdown_and_checker_trigger_docs(self):
        self.assertTrue(gate.classify(['nested/guide.md'])['docs'])
        self.assertTrue(gate.classify(['scripts/check-verification-evidence.py'])['docs'])
        self.assertTrue(gate.classify(['scripts/tests/test_verification_evidence.py'])['docs'])
    def test_server_only(self):
        self.assertEqual({'android':False,'server':True,'docs':False}, gate.classify(['backend/chat-server/A.java']))
    def test_unknown_and_mixed_paths_run_all(self):
        for paths in (['scripts/m6-run-baseline.py'], ['.github/actions/new/action.yml'], ['docs/guide.md', 'new-file.bin']):
            with self.subTest(paths=paths): self.assertTrue(all(gate.classify(paths).values()))
    def test_policy_changes_run_all(self):
        for path in gate.POLICY_FILES:
            self.assertTrue(all(gate.classify([path]).values()))
    def test_empty_diff_is_rejected_by_cli(self):
        with mock.patch.object(sys, 'argv', ['quality_gate.py', 'classify', 'a'*40, 'b'*40]), \
             mock.patch.object(gate.subprocess, 'check_output', return_value=b''):
            with self.assertRaises(ValueError): gate.main()
    def test_cli_returns_nonzero_for_failed_required_workflow(self):
        env = dict(os.environ, CHANGES_RESULT='success', ANDROID_NEEDED='true', ANDROID_RESULT='failure',
                   SERVER_NEEDED='false', SERVER_RESULT='skipped', DOCS_NEEDED='false', DOCS_RESULT='skipped')
        result = subprocess.run([sys.executable, str(Path(gate.__file__)), 'evaluate'], env=env, capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertIn('required workflow is failure', result.stderr)
    def test_selected_success_and_explicit_skip(self):
        self.assertFalse(gate.evaluate('success', {'android':'true','server':'false','docs':'true'}, {'android':'success','server':'skipped','docs':'success'}))
    def test_fail_cancel_skip_and_missing_cannot_pass_selected(self):
        for result in ('failure','cancelled','skipped','', 'timed_out'):
            with self.subTest(result=result):
                self.assertTrue(gate.evaluate('success', {'android':'true','server':'false','docs':'false'}, {'android':result,'server':'skipped','docs':'skipped'}))
    def test_missing_or_failed_change_detection_and_bad_flags_fail(self):
        flags={'android':'false','server':'false','docs':'false'}; results={x:'skipped' for x in flags}
        for result in ('','failure','cancelled','skipped'): self.assertTrue(gate.evaluate(result,flags,results))
        for invalid in ('','TRUE','unknown'):
            bad=dict(flags,android=invalid); self.assertTrue(gate.evaluate('success',bad,results))
    def test_unselected_failure_or_missing_is_not_silently_accepted(self):
        flags={'android':'false','server':'false','docs':'false'}
        for result in ('failure','cancelled','', 'success'):
            self.assertTrue(gate.evaluate('success',flags,{'android':result,'server':'skipped','docs':'skipped'}))

if __name__=='__main__': unittest.main()
