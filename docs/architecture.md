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

## 2. Compose 상태 관리

~~~text
사용자 입력 → ViewModel → Repository/ChatTransport
                             ↓
                      StateFlow<UiState>
                             ↓
                       Compose 화면
~~~

ViewModel은 화면에 표시할 상태와 비동기 요청을 관리합니다. Compose는 상태를 읽어 UI를 그리고 사용자 동작을 ViewModel로 전달합니다. 룸이 바뀌거나 요청이 취소되면 이전 응답이 현재 화면에 반영되지 않도록 처리합니다.

## 3. LIVE와 Replay의 역할

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
