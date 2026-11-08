"""Controlled stubs verify that the negative runner cannot accept stale XML."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import time
import unittest

ROOT = Path(__file__).resolve().parents[2]

class RotationNegativeRunnerTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root/'scripts/fixtures').mkdir(parents=True)
        shutil.copyfile(ROOT/'scripts/verify-rotation-diagnostic-failure.sh', self.root/'scripts/verify-rotation-diagnostic-failure.sh')
        shutil.copyfile(ROOT/'scripts/fixtures/RotationAuditFailureProbe.kt', self.root/'scripts/fixtures/RotationAuditFailureProbe.kt')
        self.target = self.root/'android/app/src/androidTest/kotlin/dev/fanstage/android/RotationAuditFailureProbe.kt'
        self.target.parent.mkdir(parents=True)
        self.results = self.root/'android/app/build/outputs/androidTest-results/connected/debug'
        self.results.mkdir(parents=True)
        self.binary = self.root/'bin'; self.binary.mkdir()
        adb = self.binary/'adb'; adb.write_text('#!/bin/sh\nprintf "List of devices attached\\nsynthetic\\tdevice\\n\\n"\n'); adb.chmod(0o755)
        gradle = self.root/'android/gradlew'
        gradle.write_text('''#!/usr/bin/env python3
import os,sys,xml.etree.ElementTree as ET
from pathlib import Path
run=next(x.split('=',1)[1] for x in sys.argv if x.startswith('-Pandroid.testInstrumentationRunnerArguments.fanRotationRunId='))
if os.environ.get('PROBE_STUB') in ('fresh','mixed','unordered'):
 root=ET.Element('testsuites',tests='1',failures='1',errors='0',skipped='0')
 suite=ET.SubElement(root,'testsuite',name='fixture',tests='1',failures='1',errors='0',skipped='0')
 case=ET.SubElement(suite,'testcase',classname='dev.fanstage.android.RotationAuditFailureProbe',name='controlledFailureCarriesRotationEvents')
 failure=ET.SubElement(case,'failure')
 other='oldRun' if os.environ.get('PROBE_STUB')=='mixed' else run
 events=['event=player_snapshot name=probe_before_cleanup view=1 player=2 state=3 surface=4 type=TextureView available=true','event=ui_checkpoint name=probe_before_cleanup states=ok','event=controlled_failure_probe','event=ui_checkpoint name=failure_after_cleanup states=ok']
 if os.environ.get('PROBE_STUB')=='unordered': events[0],events[2]=events[2],events[0]
 failure.text='intentional rotation diagnostic probe\\nrun='+run+' ns=1 event=test_start'+''.join('\\nrun='+other+' ns='+str(i+2)+' '+event for i,event in enumerate(events))
 path=Path('app/build/outputs/androidTest-results/connected/debug/TEST-fixture.xml')
 ET.ElementTree(root).write(path,encoding='utf-8',xml_declaration=True)
print('Starting 1 tests on synthetic')
print('Finished 1 tests on synthetic')
sys.exit(1)
''');gradle.chmod(0o755)
        (self.root/'temp').mkdir()

    def run_script(self, mode):
        env=dict(os.environ, PATH=str(self.binary)+os.pathsep+os.environ['PATH'], ANDROID_SERIAL='synthetic', PROBE_STUB=mode, TMPDIR=str(self.root/'temp'))
        return subprocess.run(['bash',str(self.root/'scripts/verify-rotation-diagnostic-failure.sh')],env=env,capture_output=True,text=True)

    def test_current_run_id_is_accepted(self):
        result=self.run_script('fresh')
        self.assertEqual(0,result.returncode,result.stdout+result.stderr)
        self.assertFalse(self.target.exists())

    def test_future_dated_stale_xml_is_rejected(self):
        old=ROOT/'docs/evidence/verification/audit-20261108/rotation-negative-fresh.xml'
        target=self.results/'TEST-stale.xml'; shutil.copyfile(old,target)
        future=time.time()+3600;os.utime(target,(future,future))
        result=self.run_script('stale')
        self.assertNotEqual(0,result.returncode)
        self.assertIn('run ID does not match',result.stderr)
        self.assertFalse(self.target.exists())

    def test_mixed_run_diagnostics_are_rejected(self):
        result=self.run_script('mixed')
        self.assertNotEqual(0,result.returncode)
        self.assertIn('current-run diagnostic marker missing',result.stderr)
        self.assertFalse(self.target.exists())

    def test_wrong_semantic_event_order_is_rejected(self):
        result=self.run_script('unordered')
        self.assertNotEqual(0,result.returncode)
        self.assertIn('event order is invalid',result.stderr)
        self.assertFalse(self.target.exists())

    def test_multiple_devices_are_rejected_before_writing(self):
        adb=self.binary/'adb';adb.write_text('#!/bin/sh\nprintf "List of devices attached\\nsynthetic\\tdevice\\nother\\tdevice\\n"\n');adb.chmod(0o755)
        result=self.run_script('fresh')
        self.assertNotEqual(0,result.returncode)
        self.assertIn('exactly the selected device',result.stderr)
        self.assertFalse(self.target.exists())

if __name__=='__main__': unittest.main()
