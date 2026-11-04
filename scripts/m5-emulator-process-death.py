#!/usr/bin/env python3
"""Exercise background-process kill and saved navigation on the isolated test emulator.

This uses `am kill` to simulate background process death; it is not an LMK test.
Run after installing the debug APK with only the test emulator connected.
"""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PACKAGE = "dev.fanstage.android"


def adb(*args, check=True):
    return subprocess.run(["adb", *args], text=True, capture_output=True, check=check, timeout=35).stdout.strip()


def hierarchy():
    raw = adb("exec-out", "uiautomator", "dump", "/dev/tty")
    start, end = raw.find("<?xml"), raw.find("</hierarchy>")
    if start < 0 or end < 0:
        raise RuntimeError("Unable to read the foreground app hierarchy")
    root = ET.fromstring(raw[start : end + len("</hierarchy>")])
    if not any(n.get("package") == PACKAGE for n in root.iter("node")):
        raise RuntimeError("FanStage is not in the foreground; unlock the device and retry")
    return root


def wait_for(predicate, timeout=25):
    end = time.monotonic() + timeout
    while time.monotonic() < end:
        if predicate():
            return
        time.sleep(0.5)
    raise RuntimeError("Timed out waiting for the expected state")


def has_text(root, text):
    return any(node.get("text") == text for node in root.iter("node"))


def clickable_with_text(root, text):
    def search(node, clickable=None):
        current = node if node.get("clickable") == "true" else clickable
        if node.get("text") == text and current is not None:
            return current
        for child in node:
            found = search(child, current)
            if found is not None:
                return found
        return None
    return search(root)


def main():
    devices = [line.split()[0] for line in adb("devices").splitlines()[1:] if line.split()[-1:] == ["device"]]
    if devices != ["emulator-5554"]:
        raise RuntimeError("Exactly the isolated emulator-5554 is required")
    adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
    root = hierarchy()
    if has_text(root, "Room detail"):
        adb("shell", "input", "keyevent", "BACK")
    wait_for(lambda: has_text(hierarchy(), "Studio session"))
    node = clickable_with_text(hierarchy(), "Studio session")
    if node is None:
        raise RuntimeError("Room entry not found")
    bounds = [int(x) for x in re.findall(r"\d+", node.get("bounds", ""))]
    if len(bounds) != 4:
        raise RuntimeError("Invalid room bounds")
    adb("shell", "input", "tap", str((bounds[0] + bounds[2]) // 2), str((bounds[1] + bounds[3]) // 2))
    wait_for(lambda: has_text(hierarchy(), "Room detail") and has_text(hierarchy(), "Playing"))
    adb("shell", "input", "keyevent", "HOME")
    time.sleep(2)
    adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
    wait_for(lambda: has_text(hierarchy(), "Room detail") and has_text(hierarchy(), "Paused"))
    play = clickable_with_text(hierarchy(), "Play")
    if play is None:
        raise RuntimeError("Manual play control not found after background")
    play_bounds = [int(x) for x in re.findall(r"\d+", play.get("bounds", ""))]
    if len(play_bounds) != 4:
        raise RuntimeError("Invalid play control bounds")
    adb("shell", "input", "tap", str((play_bounds[0] + play_bounds[2]) // 2), str((play_bounds[1] + play_bounds[3]) // 2))
    wait_for(lambda: has_text(hierarchy(), "Playing"))
    print("PASS: actual HOME background paused; manual play resumed VOD", flush=True)
    before = adb("shell", "pidof", PACKAGE)
    if not before:
        raise RuntimeError("Initial app process missing")
    print("PASS: room detail and VOD playing before process kill", flush=True)
    adb("shell", "input", "keyevent", "HOME")
    time.sleep(2)  # Allow the activity to stop before killing a background process.
    adb("shell", "am", "kill", PACKAGE)
    wait_for(lambda: not adb("shell", "pidof", PACKAGE, check=False), timeout=10)
    print("PASS: background app process terminated by am kill", flush=True)
    adb("shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity")
    wait_for(lambda: bool(adb("shell", "pidof", PACKAGE, check=False)))
    after = adb("shell", "pidof", PACKAGE)
    if before == after:
        raise RuntimeError("The process ID did not change")
    wait_for(lambda: has_text(hierarchy(), "Room detail") and has_text(hierarchy(), "Paused"))
    print("PASS: new process restored room detail in paused state", flush=True)
    print("LIMIT: am kill simulates background process death; low-memory killer not exercised", flush=True)


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, subprocess.SubprocessError) as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        sys.exit(1)
