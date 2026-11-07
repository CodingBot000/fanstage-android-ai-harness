# Architecture · Android Native Media & Messaging

## 11개 모듈과 의존성 경계

- **app:** Navigation, AppContainer, 화면 조립.
- **feature:** discover / community / media / messages / live. 화면 상태·이벤트와 UI를 기능별로 분리.
- **core:** model / data / artwork / designsystem. 공통 도메인 모델, 세션 Fake 데이터, 이미지 리소스 매핑, 디자인 컴포넌트.
- **macrobenchmark:** 별도 측정용 테스트 모듈.

~~~mermaid
flowchart TB
  APP["app · Navigation & wiring"] --> D["feature:discover"]
  APP --> C["feature:community"]
  APP --> M["feature:media"]
  APP --> MSG["feature:messages"]
  APP --> LIVE["feature:live"]
  APP --> DATA["core:data"]
  D & C & M & MSG --> MODEL["core:model"]
  D & C & M & MSG --> ART["core:artwork"]
  D & C & M & MSG --> DS["core:designsystem"]
  DATA --> MODEL
  LIVE --> MODEL
~~~

기능 모듈이 app을 역참조하지 않도록 [모듈 경계 검사](../scripts/check-ux-module-boundaries.py)를 실행합니다. 공통 Model은 화면 구현·저장 라이브러리에 종속되지 않는 계약으로 사용합니다. [코드 이동과 검증 PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38).

## 화면 상태 흐름

**UI action → ViewModel → Repository/Transport → StateFlow/UiState → Compose 렌더링** 순으로 상태 흐름을 분리합니다. 세션 전환·요청 취소 후 이전 응답이 새 화면을 덮지 않도록 하고, 화면 전용 초안·스크롤 등은 필요한 위치에서 Compose 상태로 소유합니다. 세부 계약은 [Compose SDD](specs/compose-architecture.md)를 참조합니다.

## Media3와 채팅 경계

~~~mermaid
flowchart LR
  SCREEN["Compose LIVE/Replay"] --> PLAYER["PlaybackSession · Media3"]
  SCREEN --> VM["ChatViewModel"]
  PLAYER --> HLS["합성 HLS VOD"]
  PLAYER --> MP4["선택형 로컬 MP4 VOD"]
  VM --> CT["ChatTransport contract"]
  CT --> FAKE["Fake transport"]
  CT --> REAL["Real transport · Debug"]
  REAL --> WS["WebSocket events"]
  REAL --> HTTP["HTTP snapshot"]
  WS --> SERVER["Spring Boot"]
  HTTP --> SERVER
  SERVER --> PG["PostgreSQL"]
~~~

**Player는 화면 수명주기에**, **ChatTransport는 채팅 ViewModel의 연결·구독 수명에** 책임을 둡니다. 전체 화면으로 이동할 때 재생 세션을 유지하면서 화면이 소멸하면 Listener/Surface/Player를 정리합니다. 채팅 오류가 재생을 중단시키지 않고, 동영상 오류가 채팅을 불필요하게 종료하지 않도록 경계를 나눴습니다.

채팅은 같은 메시지 ID를 사용하는 재전송, serverSequence 기반 순서/누락 보충, 지연 HTTP snapshot 합성을 처리합니다. 재생과 메시지 도착 시각은 독립적이므로 미디어/채팅 간 정밀 동기화는 주장하지 않습니다.

**연결 자료:** [플레이어 기술 계약](specs/media3-player.md) · [채팅 계약](specs/realtime-chat.md) · [장애 복구](specs/failure-recovery.md) · [ADR](adr/).
