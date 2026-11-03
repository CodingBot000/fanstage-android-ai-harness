# M1 — Android Native Foundation SDD

- Status: APPROVED FOR IMPLEMENTATION (2026-11-04, 사용자 포괄 실행 승인). 검증 Gate는 별도 PASS 필요.
- Current work: M1-001 (설계·공식 호환성 후보 수집). 후속 작업 M1-002는 **최소 Android 프로젝트·2개 화면 및 FakeRepository** 구현.

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

### M1-002
- [ ] Android project + Gradle wrapper and minimal multi-module dependency graph created.
- [ ] Room list and detail in Compose with Fake data and Loading/Content/Empty/Error states.
- [ ] Unit tests for state/repository path.
- [ ] CI attempt logs exact compile/lint/unit results; unexecuted steps marked NOT_RUN.
- [ ] API secret not passed to build/test commands for untrusted generated code.

### M1 Gate
- [ ] Clean local or CI build and matching test suite PASS.
- [ ] Minimal module deps and navigation/UiState checked.
- [ ] Emulator or physical install/launch result accurately stated.
- [ ] Build versions/environment/commands captured in `harness/build-log.md`.
- [ ] Actual review before marking VERIFIED.

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

- [ ] `.github/workflows/android-ci.yml` is actually triggered on Android PR/main changes.
- [ ] PR CI validates `:app:assembleDebug`, `:app:lintDebug`, and `testDebugUnitTest` on JDK17/Gradle9.5/Android SDK37.0.
- [ ] This gate is pull_request safe: GITHUB_TOKEN contents:read, checkout credentials not persisted, OPENAI_API_KEY not injected.
- [ ] Attach actual Run URL/commit and Gradle outputs to build-log before checking.
- [ ] CI success is not evidence of emulator/device UX.

M1-004 separately validates installation and navigation/Compose states with instrumentation on a running emulator/device. It must be NOT_RUN until executed.
