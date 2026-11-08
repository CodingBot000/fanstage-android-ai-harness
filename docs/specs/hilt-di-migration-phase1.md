# FanStage Hilt DI Migration — Phase 1 SDD

- Status: IMPLEMENTED / STATIC_GATES_PASS / DEVICE_NOT_RUN_AT_PHASE_1
- Note: `DEVICE_NOT_RUN`은 **Phase 1 종료 당시** 기록이다. 이후 Phase 3에서 선택된 실기기 계측 39/39, Hilt 바인딩 1/1을 실행한 사실은 [후속 검증 기록](hilt-di-migration-verification.md)과 분리한다.
- 기준: 2026-11-08, `main` commit `8dd532f2114d8588a4f2e8a21d0844edf01e5014` (Codex 실행 시 HEAD 재확인)
- Related ADR: `../adr/ADR-0004-hilt-di-migration.md`
- 목표: Hilt가 앱 시작점에서 실제로 Repository 의존성을 공급하도록 연결하면서 기존 UI·채팅·Media3 동작을 보존한다.

## 배경과 문제 (Phase 1 착수 당시)

당시 `android/app/src/main/kotlin/dev/fanstage/android/AppContainer.kt`는 Fake Repository를 기본 인자로 직접 생성하며 수동 `ViewModelProvider.Factory`를 제공한다. `MainActivity.kt`는 Compose에서 `remember { AppContainer() }`를 생성한다. Hilt는 아직 Gradle/Manifest/앱 시작점에 연결되어 있지 않다. Multi Module 경계, ViewModel 생성·재생/채팅 자원 소유권, 기존 테스트를 보존하는 점진적 DI 전환이 필요하다.

## 범위 (Phase 1만)

1. `libs.versions.toml`과 필요한 Gradle 설정에 공식 근거로 검증한 Hilt·KSP 플러그인 및 의존성을 추가한다. 검증 당시 실제 버전과 출처 URL을 문서화한다. Dagger `2.60+`, KSP `2.3.9+`의 안정 지원 하한을 참조하되 실제 AGP/Kotlin 호환성을 빌드로 확인한다.
2. `app` 모듈에 `@HiltAndroidApp`를 붙인 `Application`을 추가하고 Manifest `android:name`에 명시한다. `MainActivity`에 `@AndroidEntryPoint`를 적용한다.
3. `app`의 DI 모듈에서 `DiscoverRepository`, `CommunityRepository`, `MessageRepository`를 기존 `FakeDiscoverRepository`, `FakeCommunityRepository`, `FakeMessageRepository`로 각각 바인딩한다. **`core:data`는 현재 순수 Kotlin/JVM 모듈로 유지**하며, 필요한 경우 `app`의 `@Provides`로 생성한다.
4. Hilt가 실제 조립을 맡도록 `AppContainer`를 생성자 주입 가능한 형태로 변경하고, `MainActivity`에서 주입받아 `FanStageApp`에 전달한다. 수동 `ViewModelProvider.Factory`와 기존 ViewModel 생성 경로는 유지한다. Compose의 수동 `remember { AppContainer() }`는 제거한다.
5. 새 `Application` 소스가 기존 명시적 파일 허용 목록에 추가되도록 `scripts/check-ux-module-boundaries.py`의 강제 검사를 정확히 갱신한다. 검사 자체를 삭제하거나 무력화하지 않는다.
6. 관련 아키텍처/Android README 및 검증 기록을 **부분 전환 상태**에 맞게 갱신한다. 최종 마이그레이션 완료라고 쓰지 않는다.

## 제외 범위

- `@HiltViewModel`, `hiltViewModel()`를 통한 ViewModel 전체 전환 (Phase 2).
- 룸별 `ChatViewModel.Factory` 제거, `ChatTransport` 생성·교체·종료 변경 (Phase 2의 별도 세부 작업).
- `PlayerPanel`, `Media3PlayerController`, `PlaybackSession` 소유권·생성·해제 리팩토링 (필요한 경우 Phase 3에서 별도 검토).
- 현재 일반 계측 테스트의 러너를 전면 교체하거나 `@HiltAndroidTest` 테스트를 대량 도입하는 작업. 전용 Hilt 테스트 구성은 필요성이 확인될 때 Phase 3에서 진행한다.
- 서버, API, 영속 데이터 계약, 미디어 콘텐츠, UX, 멀티모듈 재구성, 과잉 추상화.

## 아키텍처와 데이터 계약

- 현재: `Composable -> remember(AppContainer()) -> 수동 Factory -> ViewModel -> Repository`.
- Phase 1: `@HiltAndroidApp Application -> @AndroidEntryPoint MainActivity -> (Hilt 주입 AppContainer) -> 기존 수동 Factory -> ViewModel -> (Hilt 제공 Repository 인터페이스)`.
- `app`은 composition root로 남는다. `feature:*`는 `app`을 참조하지 않고 `core:model`의 Repository 계약을 사용한다.
- `FakeCommunityRepository`와 `FakeMessageRepository`는 가변 메모리 상태를 보유한다. 무심코 `@Singleton`으로 승격하지 않는다. **기존 앱 액티비티 단위 생성/유지 범위에 최대한 일치**시키고 화면 회전/재진입에서 차이가 있는지 확인한다.
- `ChatTransport`나 `Media3PlayerController`를 Singleton으로 만들지 않는다. Hilt 도입이 Player/Socket 소유권 변경을 정당화하지 않는다.

## 정상·실패·복구 시나리오

- 정상: `MainActivity` 실행 시 Hilt 그래프 생성/주입 성공, Discover·Community·Media·Messages 화면이 기존 Fake Repository 데이터로 정상 표시되고 이전 액션 유지.
- 전환 시나리오: 탭 이동, 커뮤니티 Like/Comment, Messages Send/Read, Activity 재생성 이후 기존 ViewModel 상태와 초기 상태의 정책이 유지됨.
- 오류: Hilt 플러그인/KSP/AGP의 컴파일 불일치, MissingBinding, 잘못된 Application 또는 injection timing, 모듈 경계 스크립트 실패, 계측 런처 Crash 모두 수정 전 PASS 처리 금지.
- 자원 안정성: LIVE/Replay 플레이어 생성·해제, 채팅 연결/교체/종료 경로가 diff상 바뀌지 않아야 하며 기존 검증 회귀가 유지되어야 한다.

## 검증 계획 (실제 실행 결과만 PASS)

1. 현행 `main`/작업 브랜치, Gradle/JDK/AGP/Kotlin/KSP/Hilt 버전 확인 및 근거 URL 기록. Kotlin KGP `2.4.21`의 문서상 AGP 완전 지원 최대 `9.3.1`과 저장소 AGP `9.3.3`의 차이는 별도 호환성 리스크로 보고한다. 무근거 일괄 다운그레이드/업그레이드 금지.
2. `cd android && ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease :app:assembleRcLocal :app:assembleBenchmark :macrobenchmark:assembleBenchmark :app:lintDebug testDebugUnitTest :core:data:test --no-daemon --max-workers=2 --stacktrace`.
3. 루트에서 `python3 scripts/check-ux-module-boundaries.py`, `python3 scripts/m4-check-manifests.py`, `python3 scripts/m6-check-manifests.py`.
4. 가용한 에뮬레이터/실기기에서 기존 `MainActivity` Compose UI smoke 및 Activity 재생성 회귀를 실행. 기기가 없으면 `NOT_RUN`과 사유 기록. 기존 일반 `AndroidJUnitRunner`와 프로덕션 `Application`으로 동작하는 계측은 우선 그대로 둔다.
5. 변경 범위 `git diff --stat`, 중요 diff 리뷰 및 Gradle 설정·Manifest·DI Graph·라이프사이클 점검. 실제 결과는 PR/CI/실기기 증거로 구분한다.

## Acceptance Criteria / Exit Gate (Phase 1 종료 시점의 근거)

- [x] Hilt 2.60.1/KSP 2.3.10을 Version Catalog에 고정하고 공식 문서 근거를 기록했다.
- [x] `@HiltAndroidApp` + Manifest `android:name` + `@AndroidEntryPoint`를 빌드에서 확인했다.
- [x] Repository 3종이 Hilt에서 제공되고 Phase 1의 `AppContainer`가 주입받도록 구성했다. *`AppContainer`는 이후 Phase 2A에서 제거되었다.*
- [x] Phase 1 당시 기존 ViewModel 수동 Factory 및 ChatTransport·Media3 수명주기를 유지했다. 후속 변경은 Phase 2A·2B의 별도 커밋에서 수행했다.
- [x] Phase 1 필수 Android 빌드·Lint·단위 테스트와 모듈/Manifest 검사 PASS를 기록했다.
- [x] 당시 연결된 기기가 없어 주요 화면·회전 계측은 `NOT_RUN`으로 기록했다. **해당 Phase 자체의 계측 PASS를 의미하지 않는다.**
- [x] Phase 1은 부분 전환으로 문서화하고 별도 로컬 커밋 `1c234c6`을 만들었다. 이후 PR #67로 전체 단계가 병합되었으나 사용자 승인 절차는 공개 Git 로그만으로 판정하지 않는다.

**후속 완료 근거:** [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) · [Phase 3](hilt-di-migration-phase3.md) · [실기기/서버 검증 이력](hilt-di-migration-verification.md). 체크박스는 Phase 1 종료 시점의 충족/기록을 뜻하며 후속 검증 결과를 소급 적용한 것이 아니다.

---
## 전체 Migration Roadmap (Phase 1 이후는 이번 구현 대상 아님)

- Phase 2A: Discover/Community/Media/Messages 등 일반 ViewModel을 `@HiltViewModel`과 Hilt Navigation 경로로 전환, 테스트 유지.
- Phase 2B: LIVE 룸별 `ChatViewModel`의 roomId 전달, TransportFactory·수명주기·실패 복구 계약을 분리 검증하며 Hilt로 전환.
- Phase 3: Hilt 테스트 대체 바인딩과 계측 구성, 전체 회귀 검증, Media3 Factory DI 필요성 검토, 최종 ADR·README/검증 보고 정리.

## Open Questions / Risk

- 현재 커밋과 개발 중인 브랜치의 차이/진행 중인 작업 여부는 Codex 실행 시 확인한다.
- KSP/Hilt/AGP9 조합은 실제 빌드로 확인한다. 도구 체인 변경이 불가피하면 이번 단계 구현을 확장하지 말고 영향과 대안을 보고한다.
- Hilt 전용 계측 러너의 도입은 Phase 1 범위가 아니다. 기존 테스트 구성을 먼저 보존한다.

## 공식 참고자료

- https://dagger.dev/hilt/gradle-setup.html
- https://dagger.dev/dev-guide/ksp.html
- https://kotlinlang.org/docs/ksp-quickstart.html
- https://kotlinlang.org/docs/gradle-configure-project.html
- https://developer.android.com/training/dependency-injection/hilt-testing

## 실행 기록 (2026-11-08)

- 기준 HEAD `0451c02`; 외부 원본 두 문서는 복사 직후 `cmp` 일치.
- Hilt 2.60.1, KSP 2.3.10, AGP 9.3.3, Kotlin 2.4.21. [Dagger KSP](https://dagger.dev/dev-guide/ksp.html)는 2.60+/2.3.9+ 안정 지원; [KSP 안내](https://kotlinlang.org/docs/ksp-quickstart.html)는 Kotlin 2.4.21과 2.3.10 예시. [Kotlin 호환표](https://kotlinlang.org/docs/gradle-configure-project.html)의 AGP 완전 지원 상한 9.3.1을 초과하며 실제 로컬 빌드 통과는 공식 호환 보증이 아니다.
- JDK 21, Android SDK 로컬 경로 지정. 본문 필수 Gradle 명령 PASS (803 tasks); 모듈 경계, M4/M6 manifest, 두 benchmark APK 서명 검사 PASS. 최초 Hilt 컴파일은 Kotlin 기본 생성자 중복으로 실패해 기본 인자 제거 후 재실행 PASS.
- 계측/Activity 재생성/Media3 및 Chat 기기 회귀 NOT_RUN: 연결된 기기 없음. 기존 Factory, Transport 및 Player 소유권은 변경하지 않았다.
