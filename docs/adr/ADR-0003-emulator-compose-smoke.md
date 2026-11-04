# ADR-0003 — Separate emulator-based Compose smoke in Android PR CI

- Date: 2026-11-04
- Status: ACCEPTED for M1-004 emulator smoke only; overall M1 Exit Gate pending (2026-11-04)
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


## Amendment — Kotlin test import compile failure (2026-11-04)

Second PR attempt [#41734088258](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41734088258) proved the emulator/Bash integration runs but AndroidTest Kotlin compilation fails on `import androidx.compose.ui.test.assertExists`. The `assertExists()` call is a member of `SemanticsNodeInteraction` and requires no import. Remove that one incorrect import; do not replace the assertion or relax CI. Instrumentation, APK install and Compose navigation are still NOT_RUN until a new green connected-device test report is inspected.


## Amendment — passing connected test versus failing post-test check (2026-11-04)

[Run #41741160963](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41741160963) confirms API35 emulator, instrumented MainActivity launch and list/detail/back test PASSED (1/1, errors=0) but the job still FAILS after Gradle succeeds: final `adb shell pm path dev.fanstage.android | grep '^package:'` returns nonzero. A package-path probe **after** Gradle's connected-test lifecycle is not a dependable proof of the earlier install/launch; its failure mode (uninstall versus adb/package issue) is not conclusively distinguished by current logs. Preserve test and pipeline failure gates; replace the brittle late assertion with validation of the produced test XML and real launch/instrumentation evidence in the next separate run. Overall PR merge remains blocked until CI itself passes.

## Amendment — test result is stronger than post-teardown package path (2026-11-04)

Fourth PR Run https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212 reproduces Gradle AndroidTest PASS followed by post-test package-path exit 1. Whether the APK was uninstalled during teardown is not established; do not assert this as fact.
Replace only the redundant late probe with strict standard-library parsing of the persisted connected test XML and runner exit-code text, requiring exactly the Compose room list/detail/back test with one success and zero errors/failures/skips. Gradle still runs under Bash pipefail with captured stdout and logcat; an absent report must fail. This avoids a teardown timing dependency without reducing coverage. No physical-device, rotation, LIVE or performance claim.

## Acceptance evidence — merged PR #14 (2026-11-04)

[PR #14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14) merged as `ede28dc8ef8416986a586d3c41ca7a5617b845fb`. Independently checked [PR CI #41748827365](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41748827365) and [main CI #41755701493](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493): Android build/lint/unit 7/7, API 35 connected navigation 1/1, zero failures/errors/skips, strict test XML/exit 0; original jobs and ZIP digests verified in `harness/build-log.md`. This ADR acceptance does not grant M1 overall verification: local clean build, physical-device, rotation, LIVE and performance proof remain NOT_RUN or later-stage gates. No testing gates were disabled.
