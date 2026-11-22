# FanStage Android 멀티모듈 리팩토링 제안

**2026-11-23 · 검토안** · 분석 기준 [원격 `main`의 `b47256c`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/b47256c8f955096e9e0429de4b431045167771f0)

FanStage의 11개 Gradle 모듈을 유지하면서 `app`이 알아야 하는 기능 내부 구현을 줄인다. 첫 변경은 불필요한 의존성 두 개를 제거하고 모듈 경계 검사를 보완하는 작업이다. 이후 기능별 단위 테스트를 제자리로 옮기고, Messages를 시작으로 목적지 구현을 각 기능에 둔다. 카탈로그 계약 정리와 재생 모듈 분리는 앞 단계와 독립적으로 검토할 수 있다. [모듈 경계 결정](../adr/ADR-0001-module-boundaries.md)

## 단계별 커밋과 연속 진행

**구현을 시작하면 1~4단계를 중단 없이 끝까지 진행한다.** 각 단계에서 `구현 → 검증 → 해당 단계 커밋 → 원격 작업 브랜치 푸시 → 원격 반영 확인` 순서를 반드시 완료한다. 서로 다른 단계의 변경은 각각 별도 커밋으로 남긴다.

푸시를 확인하면 별도 확인이나 재개 지시를 기다리지 않고 즉시 다음 단계에 착수한다. CI 결과를 기다리는 동안에도 진행 가능한 작업을 계속한다. 검증이나 푸시가 실패하면 원인을 확인하고 해결 가능한 문제는 수정·재검증한 뒤 다시 푸시한다.

제품 동작을 바꾸는 미정 결정, 권한 부족, 복구할 수 없는 환경 문제는 영향받는 작업의 차단 사유로 기록한다. 그 작업을 제외하고 진행할 수 있는 구현과 검증은 계속한다. 전체 완료 보고는 1~4단계의 검증과 원격 푸시를 모두 확인한 뒤 작성한다. [프로젝트 작업 규칙](../../AGENTS.md)

## 현재 구조와 지켜야 할 동작

`app`은 루트 Navigation, 공통 화면 틀, Hilt 바인딩을 담당한다. 다섯 기능 모듈 사이에 직접 Gradle 의존성은 없으며, `core:model`과 `core:data`는 순수 Kotlin 모듈이다. 채팅 연결·메시지 정합성 분리와 DM·댓글 초안 보존은 이미 `main`에 반영됐다. [아키텍처](../architecture.md) · [채팅 오류 복구 PR](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/76) · [초안 보존 PR](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/77)

| 영역 | 회귀 검증 기준 |
|---|---|
| 화면 전환 | 기존 route와 인자 키, 탭 선택·저장·복원, 뒤로가기, 접근성 설명과 `testTag`를 보존한다. |
| ViewModel과 초안 | 일반 ViewModel 네 개는 Activity 범위를 사용한다. `ChatViewModel`은 룸, DM·댓글 초안은 해당 대화·게시물의 `NavBackStackEntry`를 사용한다. 초안은 재생성과 저장된 목적지 복원에서 유지되며, 접수 성공 또는 목적지 최종 제거 후 비워진다. 새 프로세스에서는 빈 상태로 시작한다. |
| 재생 | LIVE·Replay·로컬 VOD의 진입과 재생 소스 선택을 보존한다. 화면 재생 세션이 Player·Listener·Surface를 관리하고, 전체 화면 전환에서는 같은 세션을 사용한다. |
| 채팅 | 룸 이동·이탈 시 이전 작업을 취소하고 늦게 도착한 응답을 무시한다. ACK 유실, 중복·역순, 실제 누락 조회와 오류 구분은 기존 계약을 따른다. |

[Hilt 범위](../adr/ADR-0004-hilt-di-migration.md) · [Player 수명](../adr/ADR-0002-player-lifecycle.md) · [실시간 채팅 계약](realtime-chat.md) · [UI 재사용 기준](../adr/ADR-0005-ui-component-reuse.md)

## 1. 직접 의존성과 경계 검사 정리

`app`의 `implementation(project(":core:artwork"))`와 `feature:live`의 `api(project(":core:model"))`를 제거할 수 있다. 두 선언을 뺀 임시 복사본에서 앱과 벤치마크 빌드를 검증했다. 아트워크를 참조하는 기능 모듈의 직접 의존성은 필요하다. [app Gradle 설정](../../android/app/build.gradle.kts) · [LIVE Gradle 설정](../../android/feature/live/build.gradle.kts)

현재 검사 스크립트는 Gradle 파일에서 `project(...)` 문자열을 읽고 `app` 최상위 Kotlin 파일명을 비교한다. 임시 복사본에서 `FanStageNavHost.kt`를 최상위에 추가했을 때 실패했고, 하위 패키지에 기능 클래스를 추가하거나 `api`를 `implementation`으로 바꿨을 때는 통과했다. [현재 검사 스크립트](../../scripts/check-ux-module-boundaries.py)

첫 PR의 범위는 두 Gradle 선언, 경계 검사와 관련 CI 호출이다. Gradle이 평가한 직접 프로젝트 의존성을 기준으로 기능→기능, 기능→`app`, `core`→기능 방향과 `api` 공개 범위를 검사한다. 제품과 테스트 구성을 구분하고, `app`의 제품 소스는 하위 패키지까지 확인한다. Navigation과 DI 조립을 허용하면서 기능 구현의 유입을 감지하고, 순수 `core:model` 검사를 유지한다.

**완료 기준.** 정상적인 Navigation 파일 분리는 통과한다. 금지된 의존 방향, 승인되지 않은 `api` 노출, `app` 하위 패키지의 기능 구현은 실패 사례로 확인한다. Debug·Release·RcLocal·Benchmark 빌드와 일반·Hilt 계측 APK가 컴파일된다. [Android CI](../../.github/workflows/android-ci.yml)

## 2. 기능 단위 테스트 이관

Discover의 로딩·실패·재시도, Community의 게시물 동작, Media의 로딩 취소, Messages의 상태 전이에는 기능별 단위 테스트가 있다. 검토 기준 커밋에서는 `app/src/test`에서 실행됐다. 테스트를 관련 `feature`의 `src/test`로 옮기고, DM·댓글 초안 접수 검증도 대상 기능에 둔다. Repository 인터페이스를 구현한 테스트 대역으로 성공·실패·취소를 재현한다. 기능 간 오류 격리, Hilt 그래프, 실제 탭과 BackStack 복원은 앱 수준에서 검증한다. [기존 로딩 취소와 탭 격리 테스트](https://github.com/CodingBot000/fanstage-android-ai-harness/blob/b47256c8f955096e9e0429de4b431045167771f0/android/app/src/test/kotlin/dev/fanstage/android/LoadCancellationTest.kt) · [초안 접수 테스트](https://github.com/CodingBot000/fanstage-android-ai-harness/blob/b47256c8f955096e9e0429de4b431045167771f0/android/app/src/test/kotlin/dev/fanstage/android/DraftAcceptanceTest.kt)

**완료 기준.** Discover·Community·Media·Messages의 `:feature:<이름>:testDebugUnitTest`가 각각 관련 사례를 실행한다. 이관 전후에 늦은 응답 무시, 취소 전파, 대상별 초안 격리, 접수 거절 시 초안 보존을 동일한 조건으로 검증한다. 앱 수준의 교차 기능 검증은 계속 실행된다. [테스트 매트릭스](../test-matrix.md)

## 3. Navigation 목적지의 기능별 소유

`FanStageNavHost`는 목적지 연결과 함께 인자 해석, 초안과 채팅 ViewModel 생성, 재생 소스 선택을 맡는다. 첫 적용은 Messages 목록과 대화 상세를 등록하는 기능 내부 Navigation 코드다. 기존 Navigation Compose를 사용하고, `app`은 루트 그래프·탭·기능 간 이동을 연결한다. 기능은 자기 목적지의 인자를 읽고 화면을 구성하며, 다른 기능으로 이동할 때는 ID를 넘기는 콜백을 호출한다. [현행 `FanStageNavHost`](https://github.com/CodingBot000/fanstage-android-ai-harness/blob/b47256c8f955096e9e0429de4b431045167771f0/android/app/src/main/kotlin/dev/fanstage/android/MainActivity.kt#L145) · [Android 멀티모듈 Navigation 가이드](https://developer.android.com/guide/navigation/integrations/multi-module)

대화 초안 ViewModel은 같은 `NavBackStackEntry`에서 가져오고, `MessagesViewModel`은 기존 Activity 범위를 사용한다. Messages에서 회귀 검증을 마친 뒤 Community와 LIVE로 확대할 수 있다. LIVE 이관 시 `SavedStateHandle`의 `roomId`, LIVE 데모와 로컬 VOD의 구분, `BuildConfig.DEBUG`에 따른 재생 소스 선택도 확인한다. [ChatViewModel](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) · [로컬 미디어 계측](../../android/app/src/androidTest/kotlin/dev/fanstage/android/LocalMediaTest.kt)

**완료 기준.** 대화 열기·뒤로가기·다른 대화 열기, 탭 전환 후 복원, 회전·Activity 재생성, IME 전송을 기존 계측 테스트로 확인한다. 전송 접수 후와 최종 pop 이후에 초안이 비워지고, 저장된 목적지로 돌아오면 초안이 유지된다. [초안 수명 계측](https://github.com/CodingBot000/fanstage-android-ai-harness/blob/b47256c8f955096e9e0429de4b431045167771f0/android/app/src/androidTest/kotlin/dev/fanstage/android/DraftLifecycleTest.kt)

## 4. 공유 카탈로그 계약 정리

Discover와 Media는 모두 `DiscoverRepository.load()`로 같은 데이터 구조를 읽는다. Community·Messages에 전달하는 아티스트 목록은 `app`이 `FakeCatalog`에서 직접 읽는다. Discover·Media의 공통 조회 계약을 카탈로그 역할에 맞게 표현하고, Community·Messages가 사용하는 아티스트 조회도 교체 가능한 바인딩으로 제공하는 방안을 검토한다. `app`의 Hilt 모듈은 구현을 선택하고, 탭별 화면 상태와 재시도는 각 ViewModel이 관리한다. [MediaViewModel](../../android/feature/media/src/main/kotlin/dev/fanstage/android/feature/media/MediaViewModel.kt) · [Fake 데이터 구현](../../android/core/data/src/main/kotlin/dev/fanstage/android/core/data/FakeRepositories.kt) · [Hilt 바인딩](../../android/app/src/main/kotlin/dev/fanstage/android/di/RepositoryModule.kt)

**완료 기준.** 가짜 카탈로그를 교체해 각 사용처에 반영되는지 확인한다. Discover 로딩 실패 이후에도 Community·Messages의 아티스트 표시는 유지되고, Media의 누락 아티스트 대체 표시와 영상 클릭이 동작한다. 공유 캐시와 갱신 정책이 필요해지면 데이터의 원본과 실패 시 표시 기준을 먼저 결정한다. [탭 오류 격리](../../android/app/src/test/kotlin/dev/fanstage/android/CatalogIsolationTest.kt) · [Media 누락 아티스트 계측](../../android/app/src/androidTest/kotlin/dev/fanstage/android/MediaMissingArtistTest.kt)

## 별도 판단이 필요한 후속 후보

| 후보 | 검토 시점과 판단 기준 |
|---|---|
| 일반 ViewModel 생성 지연 | 현재 `FanStageApp`은 첫 composition에서 네 ViewModel을 얻는다. 첫 탭 방문으로 생성을 늦추려면 Activity 범위를 유지하면서 시작 시간, 첫 탭 진입 지연, 재시도와 탭 복원을 동일 조건에서 비교한다. |
| 공용 재생 모듈 | LIVE와 Replay·로컬 VOD는 `PlayerPanel`을 공유한다. VOD 목적지를 `feature:media`가 소유할 필요가 생기면 두 기능의 공용 재생 모듈을 검토한다. 첫 프레임, 회전·전체 화면, 화면 이탈 시 해제와 벤치마크 진입을 수용 기준으로 삼는다. |
| Gradle convention plugin | Discover·Community·Media·Messages는 namespace를 제외한 Gradle 설정이 동일하다. 공통 설정의 변경이 반복되거나 적용 모듈이 늘어날 때 SDK·JVM 대상·Compose 설정부터 묶고, 기능별 라이브러리 의존성은 각 모듈에서 확인할 수 있게 둔다. |

[앱의 ViewModel 조립](../../android/app/src/main/kotlin/dev/fanstage/android/MainActivity.kt) · [Replay 화면](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/ReplayScreen.kt) · [벤치마크 진입점](../../android/app/src/benchmark/kotlin/dev/fanstage/android/BenchmarkRoomActivity.kt) · [Gradle convention plugin 가이드](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_convention.html)

## 검증과 제출 기준

검토 단계에서는 기준 커밋의 임시 복사본에서 `:app:assembleDebug`, `:app:lintDebug`, `testDebugUnitTest`, `:core:data:test`와 기존 경계 검사가 통과했다. 두 직접 의존성을 제거한 복사본에서도 Debug·Release·RcLocal·Benchmark 빌드, 일반·Hilt 계측 APK 컴파일, Lint와 단위 테스트가 통과했다. 제거 실험의 기기 계측과 성능 측정은 미실행이다. [같은 Android 소스의 직전 main CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/44293681559)

구현 단계에서는 변경마다 모듈 경계 검사, 빌드·Lint·단위 테스트와 영향 범위에 맞는 계측을 실행한다. Navigation 변경은 `FloatingNavigationTest`, `DraftLifecycleTest`와 Hilt 바인딩 계측을, LIVE까지 변경하면 `HiltRoomLifecycleTest`, `LocalMediaTest`, `PlayerSettingsTest`의 영향 범위를 포함한다. 시각 변경이 있는 단계의 화면 동등성은 같은 조건의 전후 캡처로 확인한다. [테스트 매트릭스](../test-matrix.md)

단계별로 변경 모듈과 이유, 실행 명령·결과, 커밋 SHA와 푸시한 브랜치, PR·CI 링크, 미실행 항목을 기록한다. 각 단계는 별도 PR로 리뷰할 수 있다. 회귀 시 해당 변경을 되돌리고 영향 범위 검사를 다시 실행한다. 직접 의존성 선언과 검사 정책은 함께 되돌린다. [검증 기록](../verification.md)
