# Android 아키텍처

## 1. 모듈 경계

FanStage는 **앱 진입점 1개, 기능 모듈 5개, 공통 모듈 4개, 벤치마크 모듈 1개**로 이루어져 있습니다.

| 구분 | 모듈 | 책임 |
|---|---|---|
| 앱 | `app` | 화면 이동, ViewModel 생성, 의존성 조립 |
| 기능 | `feature:discover`, `community`, `media`, `messages`, `live` | 각 화면과 화면 상태·이벤트 처리 |
| 공통 | `core:model`, `data`, `artwork`, `designsystem` | 모델 계약, Fake 데이터, 이미지 매핑, 공통 UI |
| 측정 | `macrobenchmark` | 성능 계측 전용 화면·테스트 |

~~~mermaid
flowchart TB
  APP["app · Navigation / Hilt"] --> DISC["feature:discover"]
  APP --> COMM["feature:community"]
  APP --> MEDIA["feature:media"]
  APP --> MSG["feature:messages"]
  APP --> LIVE["feature:live"]
  APP --> MODEL["core:model"]
  APP --> DATA["core:data"]
  APP --> ART["core:artwork"]
  APP --> DESIGN["core:designsystem"]
  DISC & COMM & MEDIA & MSG --> MODEL
  DISC & COMM & MEDIA & MSG --> ART
  DISC & COMM & MEDIA & MSG --> DESIGN
  LIVE --> MODEL
  LIVE --> DESIGN
  DATA --> MODEL
  ART --> MODEL
~~~

다이어그램은 `android/`의 Gradle 프로젝트 의존 관계를 요약한 것입니다. [모듈 경계 검사](../scripts/check-ux-module-boundaries.py)는 각 기능 모듈의 허용 의존성과 `core:model`의 Android UI 참조 여부를 검사합니다. 기능 분리 과정과 검증 결과는 [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38)에 남겼습니다.

## Hilt 의존성 주입

`FanStageApplication`에서 Hilt를 초기화하고 `MainActivity`가 일반 ViewModel을 Activity 범위로 공유합니다. `ChatViewModel`은 Navigation BackStackEntry에 속하며 `SavedStateHandle`의 룸 ID로 룸별 Transport를 생성합니다. Repository 인터페이스는 `app` 모듈에서 Fake 구현으로 연결하며 가변 저장소나 Transport를 전역 Singleton으로 지정하지 않습니다. `core:data`는 순수 Kotlin 모듈로 유지합니다.

DI 구조와 선택한 테스트 범위는 [Hilt 설계 결정](adr/ADR-0004-hilt-di-migration.md)과 [검증 결과](verification.md)를 참조합니다.

**수동 Factory 잔존 범위:** `ChatViewModel.Factory`는 테스트·벤치마크에서 명시적 Transport를 주입하기 위해 유지합니다. `RoomListViewModel.Factory`는 초기 독립 룸 목록 구현과 단위 테스트에 남아 있지만, 현재 `MainActivity`가 실제 화면 진입에 사용하는 것은 `DiscoverRoute`입니다. 따라서 일반 ViewModel 네 개의 Hilt 전환과 혼동하지 않으며, 미사용 경로 정리는 테스트 보존 여부를 검토한 별도 작업으로 다룹니다.

## 2. UI 디자인 시스템과 화면 조립 책임

- **`core:designsystem`:** `FanStageTheme`, `FanStageSpacing`, `BackTitleBar`, `CardSingleLineText`, `SendIcon`, 접히는 타이틀바 상태 등 **기존 공통 API**를 제공합니다. Feature 도메인 모델·Navigation·ViewModel·Media3 Player에는 의존하지 않습니다. 이번 PR에서 신규 Gradle 모듈이나 범용 카드 래퍼를 추가하지 않았습니다.
- **`feature:media`:** LIVE·로컬 영상·Replay의 카드가 공유하는 16:10 이미지/라벨/제목/설명 레이아웃을 비공개 Composable `MediaArtworkCard`로 통합했습니다. 데이터 매핑, 이미지 리소스, 클릭 행동과 `media_*` 테스트 태그는 각 카드의 호출부에서 유지합니다. 이 컴포넌트는 다른 Feature에서 직접 사용하지 않습니다.
- **`app`:** `MainActivity.kt`의 `FanStageApp`에서 NavController와 일반 ViewModel 네 개를 생성하고, 같은 파일 안의 `FanStageAppShell`은 Scaffold·상단바·FloatingNavigation을, `FanStageNavHost`는 목적지·화면 조립을 담당합니다. `ChatViewModel`의 룸별 Navigation BackStackEntry 소유권은 변경하지 않았습니다.

복수 Feature가 동일한 UI 표현·상호작용 계약을 공유할 때에만 `core:designsystem`으로 승격합니다. Material3의 단순 일대일 래퍼나 기능별 모델을 받는 공통 컴포넌트는 만들지 않는 기준입니다. [UI 재사용 결정 ADR-0005](adr/ADR-0005-ui-component-reuse.md) · [설계·구현 SDD](specs/ui-designsystem-refactoring.md) · [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70).

## 3. Compose 상태 관리

~~~text
사용자 입력 → ViewModel → Repository/ChatTransport
                             ↓
                      StateFlow<UiState>
                             ↓
                       Compose 화면
~~~

ViewModel은 화면에 표시할 상태와 비동기 요청을 관리합니다. Compose는 상태를 읽어 UI를 그리고 사용자 동작을 ViewModel로 전달합니다. 룸이 바뀌거나 요청이 취소되면 이전 응답이 현재 화면에 반영되지 않도록 처리합니다.

## 4. LIVE와 Replay의 역할

**LIVE 상세**에는 영상 플레이어와 채팅 화면이 있습니다. **Replay**는 영상 재생 화면이며 채팅 입력 기능을 제공하지 않습니다. 두 화면은 Media3 재생 컴포넌트를 재사용합니다.

~~~mermaid
flowchart TB
  LIVE["LIVE 상세"] --> PLAYER["Media3 PlayerPanel"]
  LIVE --> CHAT["ChatViewModel"]
  REPLAY["Replay"] --> PLAYER
  PLAYER --> HLS["앱 내 합성 HLS VOD"]
  PLAYER --> MP4["로컬 서버의 녹화 MP4"]
  CHAT --> TRANSPORT["ChatTransport"]
  TRANSPORT --> FAKE["Fake 채팅"]
  TRANSPORT --> REAL["로컬 서버 연결 · Debug"]
  REAL --> WS["WebSocket 메시지"]
  REAL --> HTTP["HTTP 누락 조회"]
  WS --> SERVER["Spring Boot / PostgreSQL"]
  HTTP --> SERVER
~~~

실제 재생 소스는 화면 경로와 Debug 설정에 따라 선택됩니다. 기본 Replay 경로는 합성 HLS VOD를 사용하고, Debug의 지정된 LIVE 룸과 로컬 영상 항목은 로컬 MP4를 사용합니다.

**설계 근거:** [기능 모듈 분리](adr/ADR-0001-module-boundaries.md) · [Media3 플레이어 소유권](adr/ADR-0002-player-lifecycle.md) · [채팅 메시지 정합성](adr/ADR-0003-chat-consistency.md)
