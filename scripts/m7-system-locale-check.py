#!/usr/bin/env python3
"""Exercise system language + large font on an isolated emulator only."""
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

SERIAL = 'emulator-5554'
def adb(*args, timeout=30, check=True):
    return subprocess.run(['adb', '-s', SERIAL, *args], check=check, capture_output=True, timeout=timeout).stdout
if os.environ.get('FANSTAGE_M7_EPHEMERAL') != '1' or adb('shell', 'getprop', 'ro.kernel.qemu').strip() != b'1':
    sys.exit('FAIL: explicitly selected ephemeral emulator required')
if b'package:' in adb('shell', 'pm', 'path', 'dev.fanstage.android', check=False):
    sys.exit('FAIL: app already installed; preserve existing data')

def wait_for(probe, label, timeout=30):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        try:
            value = probe()
            if value is not None and value is not False: return value
        except (ValueError, ET.ParseError, subprocess.SubprocessError): pass
        time.sleep(.5)
    raise RuntimeError('timeout: ' + label)

def tree():
    raw = adb('exec-out', 'uiautomator', 'dump', '/dev/tty').decode('utf-8')
    start, end = raw.find('<?xml'), raw.find('</hierarchy>')
    if start < 0 or end < 0: raise ValueError('missing UI hierarchy')
    return ET.fromstring(raw[start:end + len('</hierarchy>')])

def find_text(text):
    return next((n for n in tree().iter('node') if n.get('text') == text), None)

def center(node):
    xy = list(map(int, re.findall(r'\d+', node.get('bounds', ''))))
    if len(xy) != 4 or xy[2] <= xy[0] or xy[3] <= xy[1]: raise ValueError('invalid bounds')
    return (xy[0]+xy[2])//2, (xy[1]+xy[3])//2

def tap(node):
    x,y = center(node); adb('shell','input','tap',str(x),str(y))

def start_app():
    adb('shell','am','start','-n','dev.fanstage.android/.MainActivity')

if not Path('app/build/outputs/apk/debug/app-debug.apk').is_file(): sys.exit('Run from android/ after assembling debug APK')
adb('shell','settings','put','system','font_scale','1.0')
adb('install','app/build/outputs/apk/debug/app-debug.apk', timeout=120)
config = adb('shell','am','get-config').decode()
if '-en-rUS' not in config: sys.exit('FAIL: initial system language must be English')
start_app(); wait_for(lambda: find_text('Studio session'), 'English room')
print('PASS: English system locale', flush=True)
adb('shell','am','start','-a','android.settings.LOCALE_SETTINGS')
if find_text('한국어(대한민국)') is None:
    tap(wait_for(lambda: find_text('Add a language'), 'add language'))
    tap(wait_for(lambda: next((n for n in tree().iter('node') if n.get('content-desc') == 'Search'),None), 'language search'))
    adb('shell','input','text','Korean')
    tap(wait_for(lambda: find_text('한국어'), 'Korean language'))
    tap(wait_for(lambda: find_text('대한민국'), 'Korean region'))
root = wait_for(lambda: tree() if find_text('한국어(대한민국)') is not None else None, 'language order')
parents = {child: parent for parent in root.iter() for child in parent}
node = next(n for n in root.iter('node') if n.get('text') == '한국어(대한민국)')
while node is not None and not any(x.get('resource-id','').endswith('/dragHandle') for x in node.iter('node')):
    node = parents.get(node)
if node is None: raise RuntimeError('language drag handle missing')
handle = next(x for x in node.iter('node') if x.get('resource-id','').endswith('/dragHandle'))
x,y = center(handle); adb('shell','input','swipe',str(x),str(y),str(x),str(y-300),'1000')
tap(wait_for(lambda: find_text('Change'), 'confirm system language'))
wait_for(lambda: '-ko-rKR' in adb('shell','am','get-config').decode().split(',')[0], 'Korean system config')
start_app(); wait_for(lambda: find_text('스튜디오 라이브'), 'Korean room')
print('PASS: system locale changed to Korean and app resolved Korean resources', flush=True)
adb('shell','settings','put','system','font_scale','2.0')
wait_for(lambda: find_text('스튜디오 라이브'), 'large-text Korean room')
tap(find_text('스튜디오 라이브'))
wait_for(lambda: find_text('방 상세'), 'large-text detail')
for _ in range(6):
    if find_text('전송') is not None: break
    adb('shell','input','swipe','500','1950','500','650','450')
node = wait_for(lambda: find_text('전송'), 'large-text send')
x,y = center(node)
if not (0 < x < 1080 and 0 < y < 2400): raise RuntimeError('send control outside screen')
Path('/tmp/fanstage-m7-large-font.png').write_bytes(adb('exec-out','screencap','-p'))
print('PASS: Korean 2x font detail and reachable send control', flush=True)

# Switch back while the detail screen exists, and verify live resource refresh.
adb('shell','am','start','-a','android.settings.LOCALE_SETTINGS')
root = wait_for(lambda: tree() if find_text('English (United States)') is not None else None, 'English language order')
parents = {child: parent for parent in root.iter() for child in parent}
node = next(n for n in root.iter('node') if n.get('text') == 'English (United States)')
while node is not None and not any(x.get('resource-id','').endswith('/dragHandle') for x in node.iter('node')):
    node = parents.get(node)
if node is None: raise RuntimeError('English language drag handle missing')
handle = next(x for x in node.iter('node') if x.get('resource-id','').endswith('/dragHandle'))
x,y = center(handle); adb('shell','input','swipe',str(x),str(y),str(x),str(y-320),'1000')
tap(wait_for(lambda: find_text('변경'), 'confirm English system language'))
wait_for(lambda: '-en-rUS' in adb('shell','am','get-config').decode().split(',')[0], 'English system config')
start_app(); wait_for(lambda: find_text('Welcome to the room!'), 'English detail after language change')
print('PASS: Korean-to-English system locale refreshed the existing detail screen', flush=True)
