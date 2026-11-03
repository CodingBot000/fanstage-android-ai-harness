# ADR-0003 — Separate emulator-based Compose smoke in Android PR CI

- Date: 2026-11-04
- Status: PROPOSED; awaiting actual CI/device evidence
- Scope: M1-004 only

## Decision

Add one dependent `emulator-compose-smoke` job to the existing read-only Android pull-request CI workflow. Use an API 35 Google APIs x86_64 Android emulator on `ubuntu-24.04`; keep Android project `compileSdk = 37` and its SDK package `platforms;android-37.0` unchanged. The test APK drives the production `MainActivity` with one Compose UI semantic-tag navigation test. Pin ReactiveCircus emulator runner at commit `a421e43855164a8197daf9d8d40fe71c6996bb0d` (annotated v2 tag), not a floating major tag. Keep `contents:read`, no API secrets, and no publishing permission.

## Alternatives

- Host-only Compose screenshot/Robolectric checks: faster, but not proof that APKs install/launch on an emulator.
- Manual `adb install` and coordinate-based taps: susceptible to screen density and layout changes, harder to assert Compose semantics.
- Running on a real Android phone: more representative, but no remote physical device is connected to this workflow.
- API 37 preview emulator: extra preview availability/boot uncertainty; API 35 is intentionally the test runtime while compile SDK remains 37.

## Trade-offs and acceptance

This adds Android system-image download and emulator execution time to PR CI. Its passing result proves only the tested list/detail/back path on the recorded emulator image, **not** full device compatibility, accessibility, process death, rotation, LIVE playback, performance or physical hardware behavior. Real device metrics and untested paths remain NOT_RUN. Preserve Gradle connected test XML, CI job logs and narrow logcat excerpt for diagnosis. No special service or recurring paid server is introduced.


## Amendment — preserve one Bash process inside emulator action (2026-11-04)

The first PR run (#41724518589) failed with `/usr/bin/sh: 1: set: Illegal option -o pipefail` after emulator boot. The action's `script` lines are individually executed by `sh -c`; Bash flags and `EXIT` traps cannot persist across lines. We therefore invoke **one checked-in Bash script** from the runner, explicitly using `set -euo pipefail` around the test command piped to `tee`. The exit trap collects logcat whether instrumentation succeeds or fails. This fixes only the proven shell boundary; actual connected test correctness remains unverified until CI produces XML. Alternative `sh` without `pipefail` rejected because it could conceal a failed Gradle process.
