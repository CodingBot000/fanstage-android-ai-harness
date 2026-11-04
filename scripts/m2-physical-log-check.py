#!/usr/bin/env python3
"""Check already-filtered physical-device Media3 and SurfaceUtils logs."""
from collections import Counter
from pathlib import Path
import re
import sys

if len(sys.argv) != 3:
    sys.exit("usage: python3 scripts/m2-physical-log-check.py PLAYER_LOG SURFACE_LOG")
player = Path(sys.argv[1]).read_text()
surface = Path(sys.argv[2]).read_text()
if not re.search(r"TestRunner:\s+finished: roomListOpensDetailAndReturns\(dev\.fanstage\.android\.RoomNavigationSmokeTest\)", player):
    sys.exit("FAIL: expected instrumentation completion missing")
starts = Counter(re.findall(r"ExoPlayerImpl: Init ([0-9a-f]+)", player))
ends = Counter(re.findall(r"ExoPlayerImpl: Release ([0-9a-f]+)", player))
if sum(starts.values()) < 6 or starts != ends:
    sys.exit("FAIL: missing or unmatched Media3 player releases")
connections = Counter(re.findall(r"connecting to surface (0x[0-9a-f]+), reason connectToSurface$", surface, re.M))
disconnections = Counter(re.findall(r"disconnecting from surface (0x[0-9a-f]+), reason disconnectFromSurface$", surface, re.M))
if not connections or connections != disconnections:
    sys.exit("FAIL: missing or unmatched codec surface disconnects")
print(f"PASS: {sum(starts.values())} matched players; {sum(connections.values())} matched codec surface connections")
print("LIMIT: matching logs do not prove the absence of all heap or listener leaks")
