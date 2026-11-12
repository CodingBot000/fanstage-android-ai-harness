# 실시간 채팅 — WebSocket/HTTP 설계 및 검증

FanStage 채팅은 **전송 결과가 불확실한 상황에서도 메시지의 표시 상태와 순서를 일관되게 유지**하는 데 초점을 맞췄습니다. 주요 경로는 WebSocket 실시간 이벤트, HTTP 누락 조회, ViewModel의 화면 상태 갱신입니다.

## 데이터 모델과 소유권

| 모델·인터페이스 | 실제 필드 | 역할 |
|---|---|---|
| `ServerMessage` | `roomId`, `messageId`, `clientMessageId`, `serverSequence` | 서버가 확정한 메시지와 룸 내 순서 |
| `ChatMessage` | `key`, `clientMessageId`, `sequence`, `delivery`, `isUncertain` | 화면 표시 메시지·전송 상태 |
| `ChatTransport` | `connectionState` | 연결 상태를 제공하는 `StateFlow<ChatConnectionStatus>` |
| `ChatUiState` | `messages`, `connectionStatus`, `gapUnresolved` | `ChatViewModel`이 소유하며 Compose가 구독하는 UI 상태 |

전송 상태는 `PENDING`, `SENT`, `FAILED`, 연결 상태는 `CONNECTING`, `CONNECTED`, `RECONNECTING`, `DISCONNECTED`로 구분합니다.

## 대표 문제 → 설계 판단 → 구현 → 테스트

### 1. 서버 저장 후 ACK 유실

**문제:** 저장은 성공했으나 ACK만 유실될 수 있어 타임아웃만으로 전송 실패를 확정할 수 없습니다.

**선택:** Pending을 유지하고 재시도 시 동일한 `clientMessageId`를 사용합니다. 서버의 멱등 처리와 클라이언트의 확정 메시지 병합으로 중복 저장·방송·표시를 방지합니다.

**확인 경로:** [ViewModel의 Pending/재시도](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) → [전송 타임아웃](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/RealChatTransport.kt) → [중복 표시 단위 테스트](../../android/feature/live/src/test/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModelTest.kt) → [TCP 프록시 ACK 차단 테스트](../../backend/chat-server/tests/m5_faults.py)

### 2. 역순 수신·누락 메시지 복구

**문제:** WebSocket 이벤트와 재연결 후 HTTP 조회 결과의 도착 순서가 달라질 수 있습니다.

**선택:** `serverSequence`의 **연속 확인 순번**과 **최대 관측 순번**을 구분합니다. 이벤트를 병합·중복 제거하고 비어 있는 범위는 HTTP로 보충합니다. 오래된 룸의 지연 응답은 새 룸에 적용하지 않습니다.

**확인 경로:** [ChatViewModel 메시지 병합·recover](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) → [RealChatTransport.fetchAfter](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/RealChatTransport.kt) → [ChatViewModelTest의 순서·누락 검증](../../android/feature/live/src/test/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModelTest.kt) → [서버 통합 테스트](../../backend/chat-server/tests/integration.py)

### 3. 전송 중 Transport 변경

**문제:** 미확정 메시지가 있을 때 서버 연결을 바꾸면 이전 연결의 결과를 잃거나 화면 상태를 잘못 확정할 수 있습니다.

**선택:** `ChatViewModel.replaceTransport()`는 Pending 메시지가 있으면 연결 변경을 차단합니다. 로컬 서버로 전환할 때는 새 연결에서 인증을 확인한 후 적용하며, 인증 실패 시 기존 모드를 유지합니다.

**확인 경로:** [ChatViewModel.replaceTransport](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) → [ChatViewModelTest](../../android/feature/live/src/test/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModelTest.kt) → [Android 실제 연결 계측](../../android/app/src/androidTest/kotlin/dev/fanstage/android/RealChatIntegrationTest.kt) → [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)

## 구현 구성과 기술 근거

- **`FakeChatTransport`:** 서버 없이 채팅을 시연하고 지연·중복·누락을 제어하는 테스트 전송 계층
- **`RealChatTransport`:** Android Debug의 WebSocket 수신·전송과 HTTP 누락 조회 계층
- **Spring Boot·PostgreSQL:** 룸별 순번, 멱등 키, 메시지 저장, ACK·조회 API
- **`ChatViewModel`:** 전송 상태, 확인 순번, 보관 한도, 룸 전환과 화면 `UiState`의 소유자

[설계 결정 ADR-0003](../adr/ADR-0003-chat-consistency.md) · [장애 주입 시나리오](failure-recovery.md) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) · [검증 결과](../verification.md) · [서버 실행](../../backend/chat-server/README.md)
