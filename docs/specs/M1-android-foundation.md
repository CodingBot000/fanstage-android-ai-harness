# M1 — Android Native Foundation SDD

- Status: APPROVED FOR IMPLEMENTATION (2026-11-04, 사용자 포괄 실행 승인). 검증 Gate는 별도 PASS 필요.
- Current work: Fast Track v1 transition and M1 evidence review. Local clean and main CI compile/lint/unit PASS; README corrected in PR #20. CI used a fresh runner but did not explicitly run the `clean` task. Device/lifecycle limitations below remain NOT_RUN.

## Goal

M0 Harness의 실제 검증 정책 아래에서 Kotlin/Compose 기반 Android 앱 골격을 만들고, LIVE/채팅 기능의 테스트 가능한 모듈 경계·상태 관리·CI 기반을 확보한다.

## Scope

### M1-001 — 계획 및 기술 조사 (이번 작업)
- M1 상세 SDD와 기술 스택 ADR 작성·근거 URL 기록
- 공식 호환성 기반 후보 선정; 검증 전까지 빌드 가능/최신 호환을 주장하지 않는다.
- Android/Gradle/Backend 코드는 아직 생성하지 않는다.

### M1-002 — 앱 최소 골격 및 테스트
- `android/` 아래 Gradle wrapper, Version Catalog, `:app`, `:core:model`, `:feature:live`, 필요 시 `:core:testing`만 생성
- Compose: LIVE 룸 목록 → 상세 화면(플레이어·채팅 자리 표시만), Loading/Content/Empty/Error 분기
- 단방향 데이터 흐름(UDF), ViewModel, StateFlow 및 FakeRoomRepository; 동작/상태 테스트
- 과한 DI, DB, HTTP/WS, Media3, 실시간 채팅 구현 금지
- 실제 컴파일/lint/unit 테스트는 GitHub Actions의 별도 비밀정보 없는 검증 단계에서 실행

### M1-003 — CI 및 디바이스 체크
- PR compile/lint/unit 품질 게이트, 독립 검증 결과·로그 기록
- 실제 에뮬레이터/실기기 설치 및 화면 이동은 별도 증거를 보존; 실행 환경 없을 경우 NOT_RUN

## Non-goals

HLS Media3(M2), Chat Transport(M3), Backend(M4), 장애 복구(M5), 벤치마크(M6), 결제·DM·실서비스 자산·외부 상시 서버. 유명 서비스 UI/브랜드 복제 금지.

## Candidate versions and compatibility risks

2026-11-04 공식 자료에 근거한 **후보**이며, 실제 빌드가 성공할 때까지 확정으로 기록하지 않는다.

| 영역 | 후보 | 선정 근거·위험 |
|---|---|---|
| JDK | 17 | AGP 9.3 기본/최소 JDK 17 |
| AGP | 9.3.3 **후보** | 9.3.3 patch는 릴리스됨. Kotlin 2.4.21의 공식 KGP 표는 **9.3.1까지** 명시하므로 9.3.3 조합은 CI 실검증 필수 |
| Gradle | 9.5.0 | AGP 9.3 최소 버전 |
| Kotlin/Compose compiler | 2.4.21 **후보** | Kotlin 2.0+는 Compose compiler plugin 사용, AGP 9.x는 built-in Kotlin으로 kotlin-android 플러그인 중복 적용 금지 |
| Compose BOM | 2026.09.00 | Google 공식 Compose 설정 문서 |
| compileSdk | 37 | AGP 9.3 최대 지원 API 37 |
| minSdk | 24 | 현행 Navigation 등 AndroidX의 API 24 최소 정책과 호환 |
| targetSdk | 36 (잠정) | 포트폴리오 테스트 편의 선택, 정책·디바이스 기준 M1 중 검토 |

실제 `M1-002`에서는 Gradle resolution/compile 검증 후 버전을 고정하고 실패하면 ADR에 변경 이유를 기록한다.

## Official sources
- AGP 9.3 compatibility / patch fixes: https://developer.android.com/build/releases/agp-9-3-0-release-notes
- AGP ↔ Gradle: https://developer.android.com/build/releases/about-agp
- Kotlin 2.4.21 KGP compatibility: https://kotlinlang.org/docs/gradle-configure-project.html
- Built-in Kotlin with AGP 9: https://developer.android.com/build/migrate-to-built-in-kotlin
- Kotlin Compose compiler plugin: https://kotlinlang.org/docs/compose-compiler-migration-guide.html
- Compose BOM: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- AndroidX Navigation: https://developer.android.com/jetpack/androidx/releases/navigation

## Contracts and Architecture

`app` owns MainActivity/navigation/DI composition, `feature:live` owns screens and UiState/ViewModel, `core:model` owns neutral Room model/Repository interface; `core:testing` is optional only if shared test fakes truly need a module.

Strict direction: `app → feature:live → core:model`. The feature must never import app internals; domain/data interfaces avoid Android UI/Context types. Do not put ExoPlayer or WebSocket in ViewModel.

`RoomUiState`: `Loading | Content(rooms) | Empty | Error(message)`. Fake repository behavior is deterministic and injected to ViewModel. Separate one-time navigation effects from persistent screen state.

## Failure / Recovery

- Dependency resolution/compile failure → FAIL with exact task/log/versions; no silent skipping.
- Navigation after leaving page → no stale state or listener leak.
- Fake repository error → Error UI and explicit retry action.
- Remote API/funding unavailable → keep local Fake; no surprise paid infrastructure.
- Signing keys/real media/person information → never commit.

## Test plan

- Unit: Fake state transitions, error/empty/retry, ViewModel lifecycle/cancellation where practical
- Compile: clean `assembleDebug`
- Static: Android Lint (Gradle task actual result)
- UI: Compose navigation and semantic tags (device/emulator gate; NOT_RUN until runnable)
- Architecture: module dependency graph and no reversed edges
- CI: GitHub Action on trusted runner; avoid API key in generated Android build job
- Manual device: install, back/forward navigation, rotation (M1 Gate if device access exists)

## Acceptance Criteria

### M1-001
- [x] Official source URLs and provisional versions documented with an explicit KGP/AGP compatibility caveat.
- [x] M1 SDD, boundaries, contracts, test plan and gates described.
- [x] Android/Backend implementation untouched.

### M1-002 (retrospective evidence audit, 2026-11-04)
- [x] Android project + Gradle wrapper and minimal three-module dependency graph exist.
- [x] Room list and detail in Compose with Fake data and Loading/Content/Empty/Error state branches; only Content navigation is instrumented.
- [x] Seven JUnit tests cover FakeRepository and ViewModel state/retry/cancellation.
- [x] Actual main CI compile/lint/unit + emulator logs and NOT_RUN limitations are recorded.
- [x] Build/test workflow grants contents:read and passes no API secret to untrusted code.

### M1 Gate — 검토 결과
- [x] Local clean/assemble/lint/unit PASS and CI assemble/lint/unit PASS; CI on its fresh runner did not explicitly execute the `clean` task. See [build-log](../../harness/build-log.md) for source revisions and limitations.
- [x] Main CI build/lint + Unit 7/7 + Emulator 1/1 PASS: [Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235). Lint warnings persist.
- [x] Static dependency graph is `:app → :feature:live → :core:model`, without reverse/circular project edges in checked Gradle files.
- [x] Basic Compose list/detail/back, Activity recreation and ViewModel Loading/Empty/Error/Content logic checked; alternate UI branches have not been instrumented.
- [x] Emulator install/launch evidence and build versions/environment/commands recorded in `harness/build-log.md`. Physical device/real rotation/process death **NOT_RUN**.
- [x] Source-scoped independent static review performed for app/feature/core contracts, state cancellation, navigation, Compose state collection and CI secret boundary. Review does not prove every runtime lifecycle scenario; details in audit.
- [x] `android/README.md` verification and version descriptions corrected in PR #20.
- [x] M1 core evidence reviewed under Fast Track v1. Physical-device installation, real rotation and process-death/relaunch remain NOT_RUN and are not represented by emulator Activity recreation.

## Evidence / Commands

- `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest` or task-specific equivalents after project creation; run from `android/`.
- Record GitHub Actions run URL, versions, commit SHA and failed/unrun tasks.
- Record API-billed Codex execution separately from Android build results.

## Open Questions

- AGP 9.3.3 with Kotlin/Compose plugin 2.4.21 is a *candidate*, not listed as fully supported by Kotlin's table (9.3.1 maximum); verify and fall back to fully supported combination as needed.
- Determine actual Navigation, lifecycle/Activity, Hilt versions from compatible stable releases before adding.
- M1 should not introduce backend, HLS/Media3, or chat transport.

## SDK API 37 package ID validation

- Android SDK Manager publishes Android 17 preview SDK as `platforms;android-37.0`; do not assume `platforms;android-37` exists.
- `compileSdk = 37` is the Gradle integer DSL, distinct from SDK package identifier.
- First two CI runs did **not** compile the Android app; see build-log, and prefer re-verifying preserved source artifacts over re-running paid generation when only CI infrastructure is broken.

## M1-003 — Persistent PR quality gate

- [x] `.github/workflows/android-ci.yml` is actually triggered on Android PR/main changes.
- [x] PR CI validates `:app:assembleDebug`, `:app:lintDebug`, and `testDebugUnitTest` on JDK17/Gradle9.5/Android SDK37.0.
- [x] This gate is pull_request safe: GITHUB_TOKEN contents:read, checkout credentials not persisted, OPENAI_API_KEY not injected.
- [x] Attach actual Run URL/commit and Gradle outputs to build-log before checking.
- [ ] CI success is not evidence of emulator/device UX.

M1-004 separately validates installation and navigation/Compose states with instrumentation on a running emulator/device. It must be NOT_RUN until executed.

M1-003 evidence reviewed: [Run #41700593263](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41700593263), source commit `424b1c4d3917f1fa2175364808743bb0c555c3b4`, job `126142531399`; 7 JUnit tests PASS / 0 failures / 0 skipped. Lint task PASS with 13 warnings (not zero-warning). Source verification precedes this documentation-only evidence commit. No emulator/Compose UI or physical device test was run; these remain M1-004 NOT_RUN.

## M1-004 — Emulator / Compose UI smoke (one bounded task, 2026-11-04)

- Scope: install the existing M1 Compose app in an Android API 35 x86_64 emulator and run **one** AndroidJUnitRunner Compose instrumentation test for Room List → Detail → Back. No media/chat/backend feature changes.
- Configuration: same JDK 17, Gradle 9.5.0, AGP 9.3.3 / compile SDK 37.0 used by existing CI; stable API 35 Google APIs emulator is a **runtime** device, not a downgrade of compile SDK. Emulator runner pinned by full commit SHA.
- The new `emulator-compose-smoke` job depends on successful unchanged compile/lint/unit job. PRs have `contents: read` token only; no OpenAI secret; no publish step. AndroidTest target app uses `androidx.test.runner.AndroidJUnitRunner`, Compose UI Test BOM, JUnit extensions.
- Command from `android/`: `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace`. CI captures test stdout, connected XML/HTML reports, selected logcat, `adb devices`, device SDK and installed application package path in the job log.
- [x] **PASS only after actual GitHub Actions logs and test report confirm** emulator boot, app/test APK install, list/detail/back assertion, and zero failures. Recording code/config alone is NOT_RUN, not PASS.
- [x] Actual Run URL, job outcome, artifacts and source SHA reviewed independently and entered in build-log on completion.
- [x] M1 overall Gate reviewed separately (**NOT VERIFIED**); physical-device install, real performance, rotation and process recreation remain NOT_RUN until proved.
- Failure policy: preserve original errors, avoid retries within same run, no skipped tests, no false success; fix only smallest verified cause next task.
- Third-party emulator runner provenance: https://github.com/ReactiveCircus/android-emulator-runner ; stable AndroidX Test runner/ext.junit release table: https://developer.android.com/jetpack/androidx/releases/test ; Compose test dependencies: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler

### M1-004 verified CI shell failure and bounded repair (2026-11-04)

- First attempt [Run #41724518589](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41724518589) on `b67cd7dfc523434eec213445ddd83d8575573c3e`: build/lint/unit **PASS**, emulator **boot PASS**, instrumentation **NOT_RUN**, full workflow **FAIL**.
- Proven cause in [emulator job #126227097645](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41724518589/job/126227097645) raw log: action executes each input script line as `/usr/bin/sh -c`; `set -euo pipefail` exits under Ubuntu Dash before Gradle runs. This also means a multiline `trap` would not persist.
- Small repair: replace multi-line runner `script` with **one** `bash ../scripts/m1-004-emulator-smoke.sh` command; checked-in Bash script owns `set -euo pipefail`, Gradle+tee, exit trap/logcat, adb package check. Do not suppress a failing test or disable CI.
- New repair's execution and connected XML remain **NOT_RUN until the next actual CI**. Preserve a single repair-triggered workflow run as evidence; no repeats for this attempt.

### M1-004 verified AndroidTest compilation failure and minimal import repair (2026-11-04)

- [Run #41734088258](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41734088258) on `1687a88b97733ae0c4e73af0e5579ca8c8584df8`: unchanged Android build/lint/unit **PASS**, API 35 emulator boots and one Bash script executes **PASS**, `:app:compileDebugAndroidTestKotlin` **FAIL** (`RoomNavigationSmokeTest.kt:3:33 Unresolved reference 'assertExists'`). AndroidTest APK/install and navigation assertions **NOT_RUN**; no connected test XML yet.
- Fix only the invalid top-level import; `assertExists()` is a method on Compose `SemanticsNodeInteraction`, and the existing call remains unchanged. Leave all checks, instrumentation coverage, and failure propagation enabled.
- [ ] Replacement commit must receive a **new** PR CI run with an actual connected test XML and assertion result before M1-004 can be PASS. Emulator/physical device requirements beyond this one flow remain NOT_RUN.

### M1-004 third CI run: connected instrumentation PASS, workflow still FAIL (2026-11-04)

- Commit `1099d9d812e07efdca23fe5b1228956f620ccea0`, [Run #41741160963](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41741160963): Kotlin AndroidTest compilation **PASS**, emulator API35 boot **PASS**, actual `:app:connectedDebugAndroidTest` **PASS**. Downloaded connected XML: 1 executed test `roomListOpensDetailAndReturns`, 0 failures/errors/skips; instrumentation runner `OK (1 test)` and MainActivity START/Displayed in logcat.
- **Overall CI FAIL:** Bash exits 1 *after* Gradle `BUILD SUCCESSFUL` because the final `adb shell pm path dev.fanstage.android | grep '^package:'` post-test assertion fails. The available log does not prove whether test teardown removed the package or a different adb/package-status problem caused the empty result. Do not claim full M1-004 PASS from the successful instrumentation subtest alone.
- Next constrained correction is to replace/reposition the redundant post-test package-path assumption with a durable validation of actual instrumentation XML/Gradle success and installation/launch proof; preserve shell `pipefail`, test count, and test assertions. No second CI triggered in this run.

### M1-004 fourth PR CI and bounded post-test repair (2026-11-04)

- A documentation-only commit triggered another Android CI because PR path filters consider the entire PR diff. Fourth Run https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212 (head 9970e56ef81a7a4b4105afe358b167c9e45e2cd7): build/lint/unit PASS; API 35 emulator, Gradle and 1/1 Compose connected test PASS; whole CI FAIL after Gradle at the redundant adb package-path check. No manual rerun.
- Downloaded original emulator artifact id 13281658056, ZIP SHA-256 0d929058d51fcd17f12600eaefe6e31f0b62fb7473b070f30dbacae8fd6786d9 independently verified. AndroidTest XML has tests=1, failures=0, errors=0, skipped=0; runner exit 0; logcat MainActivity START/Displayed and TestRunner finished.
- Only replace the late package-path check with a Python3 standard-library assertion for exactly one matching test case, zero XML errors/failures/skips and zero runner exit. Keep Gradle pipefail, Compose assertions, logs and artifact upload. Original XML positive check PASS; deliberate tests=0 negative control exits 1 PASS. This is offline replay, NOT_RUN for new CI until executed.
- Real physical device, rotation/process death, LIVE and performance remain NOT_RUN; M1 overall Gate remains open.

## M1-004 — merged PR and main CI proof (2026-11-04)

- **Scope/PR:** [#14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14) squash merged after code diff, resolved Codex P1 thread, and successful [PR Run #41748827365](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41748827365) were reviewed. Merge commit `ede28dc8ef8416986a586d3c41ca7a5617b845fb`, PR head `953aba5fb4e419fcd266ce9018164e860d847c77`.
- **Post-merge verification:** [main push Run #41755701493](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493), head `ede28dc8ef8416986a586d3c41ca7a5617b845fb`, completed SUCCESS; build job [#126331170470](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493/job/126331170470) and emulator job [#126332262156](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493/job/126332262156) both PASS.
- **Actual commands:** from `android/`: `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace` under Bash `set -euo pipefail` with `tee`; emulator runner invokes `bash ../scripts/m1-004-emulator-smoke.sh`, which runs `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace` plus strict connected XML and instrumentation exit-code verification. Ubuntu 24.04, JDK 17, Gradle 9.5.0, compilation SDK package `platforms;android-37.0`, emulator API 35 x86_64 Google APIs.
- **PASS:** main Android Gradle build/lint/unit `BUILD SUCCESSFUL in 2m 13s`, 93 tasks; two unit XML suites total 7/7 tests, failures=0/errors=0/skipped=0. Emulator boot, `:app:connectedDebugAndroidTest` `BUILD SUCCESSFUL in 1m 48s`; `RoomNavigationSmokeTest.roomListOpensDetailAndReturns` 1/1, failures=0/errors=0/skipped=0; result-exit-code file `0`; original TestRunner logcat shows 1 completed, 0 failed.
- **Artifact integrity:** unit `android-pr-gate-evidence` ID `13288307072` ZIP SHA-256 `bae7921d913d1b6f76ace26bef660f673587288c8f62913af50496e840d5f7ec`; emulator `android-emulator-compose-smoke-evidence` ID `13287838197` ZIP SHA-256 `6ad1fedf5d8d7479be1577f5901389ac8afd238e7cec164c36e4765279a01cec`. Both independently downloaded/hashed; match GitHub artifact digests. Original job logs/XML/exit file inspected, not just green badges.
- **Warnings/limitations:** Android Lint task PASS with **13 nonfatal warnings**; GitHub Actions Node20 deprecation and `setup-java@v4` warning remain. **FAIL:** none in this main CI. **NOT_RUN/blockers:** clean build on local developer machine, physical-device install, rotation/process recreation, LIVE streaming, release-like device performance; M1 full Exit Gate **NOT VERIFIED**. No Codex generation, API key reads or paid infrastructure in this work unit. Only one Android Actions run was triggered by merging PR #14; evidence-only follow-up is outside Android path filters.

## M1-005 — Activity recreation navigation contract (2026-11-04)

- Scope: one extension of the existing `RoomNavigationSmokeTest.roomListOpensDetailAndReturns` test; no new test method, app production code, dependency or workflow changes.
- Scenario: launch list, open a detail, verify it is displayed, call `activityRule.scenario.recreate()`, confirm a different Activity instance, verify detail route restored, then use in-app Back to return to the original list.
- Rationale/trade-off: [ADR-0004](../adr/ADR-0004-m1-activity-recreation.md). Activity recreation is **not** proof of screen orientation change, process death/relaunch, or LIVE restoration.
- Required connected evidence: one executed test (not zero), zero failures/errors/skips, strict XML and runner exit check; inspect original logs. Existing build/lint/unit and emulator gates remain unchanged.
- Initial state: source change proposed; PR-triggered CI result **NOT_RUN until executed**. Local Android SDK/physical device: **NOT_RUN**. M1 Exit Gate: **NOT VERIFIED**.

## M1-005 — merged and independently verified on main (2026-11-04)

- PR [#16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16), source head `dc49a4a54f6aad3848b159140f26938b414ae764`, squash merge commit `e22bb4042173d6b0160b9d46c50837cbfde6bb56`.
- PR [Run #41763099229](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41763099229) PASS after reading original job logs and ZIP test XML; Codex code review for that exact head completed without findings. Existing strict runner checks remain unchanged.
- Post-merge [main Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235), event `push`, attempt 1, completed SUCCESS. Build/lint/unit job `126372936584` PASS (93 Gradle tasks, seven JUnit tests). API 35 emulator job `126373527054` PASS (88 Gradle tasks, exactly one connected Compose test); instrumentation exit-code `0`, XML errors/failures/skips `0`. See build-log for commands and artifact digests.
- Only `ActivityScenario.recreate()`/new Activity instance and detail route/Back assertions were executed. **NOT_RUN**: real orientation switch, low-memory process death/relaunch, physical device, LIVE/HLS, performance, local workstation clean build. **M1 overall Gate remains NOT VERIFIED** until separate acceptance audit; this single passing test must not count for other lifecycle cases.

## M1-006 — independent Gate audit (2026-11-04)

[Review and evidence matrix](../reviews/M1-006-exit-gate-audit.md) compares the **strict MASTER_PLAN** local-clean + CI requirement with actual logs and source. Prior main [Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) remains PASS for build/lint/unit/emulator; this docs-only audit does not re-run it. Local clean NOT_RUN, stale android/README and future lifecycle/physical/LIVE/performance evidence NOT_RUN. No Gate promotion.

## M1-007 — developer-local clean build (2026-11-04)

Local clean/assemble/lint/unit PASS on macOS arm64 and JDK 17; seven unit tests passed, lint reported 13 warnings. An initial executable-bit failure was fixed in `android/gradlew` before rerun. Full command, source revision, environment, and local log hash are recorded in [build-log](../../harness/build-log.md). At that time M1 approval and README correction remained pending.
