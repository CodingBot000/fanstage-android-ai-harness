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

`FanStageApplication`에서 Hilt를 초기화하고 `MainActivity`가 일반 ViewModel을 Activity 범위로 공유합니다. `ChatViewModel`은 Navigation BackStackEntry에 속하며 `SavedStateHandle`의 룸 ID로 룸별 Transport를 생성합니다. DM·댓글 입력 초안은 별도의 `ConversationDraftViewModel`·`PostDraftViewModel`을 해당 목적지 BackStackEntry 소유자로 생성하며, 문자열을 `SavedStateHandle`·영속 저장소에 기록하지 않습니다. Repository 인터페이스는 `app` 모듈에서 Fake 구현으로 연결하며 가변 저장소나 Transport를 전역 Singleton으로 지정하지 않습니다. `core:data`는 순수 Kotlin 모듈로 유지합니다.

DI 구조와 선택한 테스트 범위는 [Hilt 설계 결정](adr/ADR-0004-hilt-di-migration.md)과 [검증 결과](verification.md)를 참조합니다.

**수동 Factory 유지 범위:** `ChatViewModel.Factory`는 테스트·벤치마크에서 명시적 Transport를 주입하기 위해 유지합니다. 초기 독립 룸 목록의 `RoomListViewModel`·Factory·Repository·모델과 `RoomListScreen`은 실제 진입 경로인 `DiscoverRoute`와 중복되어 정리했습니다. 로딩·빈 결과·재시도·취소·늦은 응답 검증은 `DiscoverViewModelTest`로, Fake 데이터 계약은 `FakeRepositoriesTest`로 옮겼고 현지화 계측은 실제 `DiscoverScreen`을 사용합니다. 일반 ViewModel의 Activity 소유 범위는 바꾸지 않았습니다.

## 2. UI 디자인 시스템과 화면 조립 책임

- **`core:designsystem`:** `FanStageTheme`, `FanStageSpacing`, `BackTitleBar`, `CardSingleLineText`, `SendIcon`, 접히는 타이틀바 상태 등 **기존 공통 API**를 제공합니다. Feature 도메인 모델·Navigation·ViewModel·Media3 Player에는 의존하지 않습니다. 공통 컴포넌트와 기능별 UI의 경계를 기존 모듈 안에서 유지합니다.
- **`feature:media`:** LIVE·로컬 영상·Replay의 카드가 공유하는 16:10 이미지/라벨/제목/설명 레이아웃을 비공개 Composable `MediaArtworkCard`로 통합했습니다. 데이터 매핑, 이미지 리소스, 클릭 행동과 `media_*` 테스트 태그는 각 카드의 호출부에서 유지합니다. 이 컴포넌트는 다른 Feature에서 직접 사용하지 않습니다.
- **`app`:** `MainActivity.kt`의 `FanStageApp`에서 NavController와 일반 ViewModel 네 개를 생성하고, 같은 파일 안의 `FanStageAppShell`은 Scaffold·상단바·FloatingNavigation을, `FanStageNavHost`는 목적지·화면 조립을 담당합니다. `ChatViewModel`의 룸별 Navigation BackStackEntry 소유권은 유지하고, DM·댓글 목적지의 초안 ViewModel만 해당 entry에 명시적으로 연결했습니다.

둘 이상의 기능에서 표현과 동작이 같고 기능별 모델에 의존하지 않을 때만 `core:designsystem`에 공통 컴포넌트를 둡니다. 그 외 UI는 해당 기능 모듈에서 관리합니다. [UI 재사용 결정 ADR-0005](adr/ADR-0005-ui-component-reuse.md) · [설계·구현 SDD](specs/ui-designsystem-refactoring.md) · [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70).

## 3. Compose 상태 관리

~~~mermaid
flowchart LR
  UI["Compose 화면"] -->|"사용자 동작"| VM["ViewModel<br/>UiState 소유"]
  VM -->|"요청·구독"| DATA["Repository / ChatTransport"]
  DATA -->|"데이터·응답·이벤트"| VM
  VM -->|"StateFlow&lt;UiState&gt;"| UI
~~~

ViewModel이 화면별 `UiState`를 소유하고 `StateFlow`로 공개합니다. Repository/ChatTransport는 데이터·이벤트를 제공하며 UI 상태를 직접 소유하지 않습니다. Compose는 `collectAsStateWithLifecycle`로 상태를 읽고 사용자 동작을 ViewModel에 전달합니다. 룸이 바뀌거나 요청이 취소되면 이전 응답이 현재 화면에 반영되지 않도록 처리합니다. [채팅 ViewModel](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) · [Media 화면 상태 구독](../android/feature/media/src/main/kotlin/dev/fanstage/android/feature/media/MediaRoute.kt).

### 채팅 내부 책임

`ChatPanel`은 메시지 목록·입력 UI를 조립하고 [ChatConnectionSettings](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatConnectionSettings.kt)는 개발용 연결 설정을 표시합니다. 연결 검증은 [LocalChatConnectionCoordinator](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/LocalChatConnectionCoordinator.kt)가 담당하며 해당 Compose 화면과 같은 수명을 사용합니다. 임시 연결은 검증 종료·취소 때 정리하고, 인계된 활성 Transport는 `ChatViewModel`이 소유합니다. 토큰은 메모리에만 두고 저장 상태나 로그로 옮기지 않습니다.

메시지 저장·중복·순번·충돌 판정과 공유 메모리 임대는 [ChatTimelineStore](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatTimelineStore.kt)에 둡니다. 저장소는 주 스레드에서 직렬로 사용하며 Coroutine·Socket·Android Context를 소유하지 않습니다. 룸의 `generation`, 수신·전송·복구 Job, `StateFlow` 발행과 활성 Transport 해제는 기존 `ChatViewModel`에 남깁니다. R-03·R-04에서 내부 책임을 추출할 당시에는 기존 동작을 보존했습니다. 이후 R-01에서 프로토콜 경고·실제 누락·수신 종료를 구분했고, R-02에서 DM·댓글 초안의 목적지 수명을 별도로 개선했습니다.

~~~mermaid
flowchart LR
  UI["ChatPanel<br/>채팅 화면"] --> VM["ChatViewModel<br/>룸·전송·복구 Job"]
  UI --> SET["ChatConnectionSettings<br/>개발 설정 UI"]
  SET --> COORD["LocalChatConnectionCoordinator<br/>검증·취소"]
  COORD -->|"검증 후 Transport 인계"| VM
  VM --> STORE["ChatTimelineStore<br/>순번·중복·충돌·임대"]
  VM --> TRANS["ChatTransport<br/>Fake / Real"]
~~~

`ChatUiState.gapUnresolved`는 실제 순번 누락에만 사용하며, `protocolIssue`는 잘못된 순번·길이·충돌·Pending 불일치를 나타냅니다. `connectionError`와 `streamError`도 분리합니다. 수신 Flow가 예외 없이 예상 밖에 완료될 때도 활성 Coroutine·룸 `generation`을 확인해 `streamError`를 표시합니다. 프로토콜 경고는 룸 세션 동안 남지만 누락 복구 버튼은 실제 누락이 있을 때만 표시됩니다. [채팅 설계](specs/realtime-chat.md) · [PR #76](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/76).

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

## 5. 누락된 로컬 영상 아티스트

로컬 영상의 아티스트는 ID로 조회합니다. 항목이 없으면 Media 기능의 중립적인 벡터 대체 이미지와 현지화한 `unknown_artist` 이름을 표시하고 카드·제목·영상 열기 동작을 유지합니다. 다른 아티스트를 임의로 연결하지 않습니다. 이 처리는 [실행 계획 D-03](specs/android-refactoring-plan.md)의 사용자 선택을 따르며, 정상 항목의 artwork·이름·정렬·필터는 유지합니다.

## 6. 목적지별 DM·댓글 초안 수명 (R-02)

`ConversationDraftViewModel`과 `PostDraftViewModel`은 각 Navigation BackStackEntry가 소유한다. app은 목적지 안에서 명시적 entry 소유자로 생성해 Route로 전달하고 기존 일반 ViewModel 네 개의 Activity 범위는 유지한다. 초안은 StateFlow의 메모리 문자열이며 SavedStateHandle이나 영속 저장소에 기록하지 않는다. 저장된 목적지에서는 유지하고 최종 제거 시 clear하며 새 프로세스 소유자는 빈 값으로 시작한다.

Screen은 입력·전송 이벤트만 전달하며 전송 성공을 추정해 초안을 지우지 않는다. 목적지 소유자가 실제 Repository 반영 여부를 확인한 뒤 제출한 초안만 초기화한다. 빈 입력·거절·오류·취소에는 초안을 유지한다. 현재 Fake Repository의 동기 접수 반영은 확인하지만 실제 서버 전달 확정·ACK로 해석하지 않습니다. 전송 중 초안이 다시 수정되면 제출 당시 값만 초기화 대상이며 새 입력을 지우지 않습니다. [PR #77](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/77)에서 목적지 저장·복원과 최종 pop을 검증했습니다.
