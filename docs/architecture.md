# Android 아키텍처

## 멀티모듈 구조

FanStage는 화면 기능 5개와 공통 기능 4개, 앱 진입점, 벤치마크를 포함한 **11개 Gradle 모듈**로 구성됩니다.

- **app:** Navigation, ViewModel 생성과 의존성 조립
- **feature:** discover / community / media / messages / live — 기능별 화면과 상태·이벤트 처리
- **core:** model / data / artwork / designsystem — 공통 계약, Fake 데이터, 이미지 매핑, 디자인 컴포넌트
- **macrobenchmark:** 별도 성능 측정용 모듈

~~~mermaid
flowchart TB
    APP["app · Navigation / AppContainer"] --> DISC["feature:discover"]
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

다이어그램은 `android/settings.gradle.kts`와 각 모듈의 Gradle 의존 관계를 기준으로 작성했습니다. [모듈 경계 검사](../scripts/check-ux-module-boundaries.py)는 기능별 허용 의존성과 순수 Kotlin 모델의 Android UI 참조 여부를 확인합니다. 실제 모듈 분리 과정은 [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38)에 기록했습니다.

## Compose 상태 흐름

사용자 입력은 **UI Action → ViewModel → Repository/Transport → StateFlow(UiState) → Compose** 흐름으로 처리합니다.

화면에 지속적으로 표시되는 상태는 ViewModel의 StateFlow로 관리하고, 탭 선택·입력 초안·스크롤 위치처럼 화면에 밀접한 임시 상태는 사용 범위에 맞춰 Compose 또는 ViewModel에서 관리합니다. 화면 전환이나 요청 취소 이후 도착한 이전 응답이 현재 화면을 바꾸지 않도록 상태와 취소 경계를 분리했습니다.

## 영상 재생과 채팅의 독립성

~~~mermaid
flowchart LR
    UI["Compose LIVE / Replay"] --> PLAYER["Media3 재생 세션"]
    UI --> VM["ChatViewModel"]
    PLAYER --> HLS["합성 HLS VOD"]
    PLAYER --> MP4["선택형 로컬 MP4"]
    VM --> CONTRACT["ChatTransport"]
    CONTRACT --> FAKE["Fake"]
    CONTRACT --> REAL["Real · Debug"]
    REAL --> WS["WebSocket 이벤트"]
    REAL --> HTTP["HTTP snapshot"]
    WS --> SERVER["Spring Boot"]
    HTTP --> SERVER
    SERVER --> PG["PostgreSQL"]
~~~

Player는 재생 화면의 수명주기에서 관리하고, 채팅 연결과 구독은 ChatViewModel/Transport의 수명에 맞춰 관리합니다. 따라서 플레이어를 종료하거나 채팅이 재연결되는 과정에서도 서로의 상태를 독립적으로 다룰 수 있습니다.

채팅은 동일 메시지 ID를 사용하는 재전송, `serverSequence` 기반 순서·누락 복구, 지연된 HTTP 응답의 정합성 처리를 적용했습니다.

**상세 설계:** [모듈 경계](adr/ADR-0001-module-boundaries.md) · [Media3 수명주기](adr/ADR-0002-player-lifecycle.md) · [채팅 일관성](adr/ADR-0003-chat-consistency.md)
