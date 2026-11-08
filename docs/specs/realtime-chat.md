# 실시간 채팅 · WebSocket/HTTP 설계

## 1. 데이터 모델과 역할

서버 메시지, 화면 표시 메시지, 연결 상태를 서로 다른 모델로 관리합니다.

| 모델·인터페이스 | 실제 필드 | 역할 |
|---|---|---|
| `ServerMessage` | `roomId`, `messageId`, `clientMessageId`, `serverSequence` | 서버가 확정한 메시지와 룸 내 순서 |
| `ChatMessage` | `key`, `clientMessageId`, `sequence`, `delivery`, `isUncertain` | 화면에 표시하는 메시지와 전송 상태 |
| `ChatTransport` | `connectionState` | 연결 상태를 제공하는 `StateFlow<ChatConnectionStatus>` |
| `ChatUiState` | `messages`, `connectionStatus`, `gapUnresolved` | Compose가 구독하는 화면 상태 |

`ChatMessage.delivery`는 `PENDING`, `SENT`, `FAILED` 중 하나입니다. 연결 상태는 `CONNECTING`, `CONNECTED`, `RECONNECTING`, `DISCONNECTED`로 구분합니다. 서버 순번은 `ServerMessage.serverSequence`에 보관합니다.

## 2. 전송·재연결 처리

1. **ACK 유실:** 전송 결과가 확인되지 않으면 Pending을 유지합니다. 같은 `clientMessageId`로 다시 보내 서버의 중복 저장·방송을 방지합니다.
2. **순서·누락:** WebSocket 이벤트와 HTTP 메시지 조회 결과를 `serverSequence`로 합칩니다. 확인된 연속 순번과 가장 큰 관측 순번을 구분해 빠진 구간을 찾아 보충합니다.
3. **룸 변경:** 이전 룸에서 늦게 도착한 이벤트나 취소된 요청은 현재 룸에 적용하지 않습니다.
4. **전송 경로 변경:** `ChatViewModel.replaceTransport()`는 미확정 메시지가 있으면 전환을 차단합니다. 로컬 서버를 선택할 때는 인증을 확인한 뒤 새 연결을 적용합니다.
5. **메모리 관리:** Fake 채팅과 화면 메시지에는 보관·표시 개수 제한을 둡니다.

## 3. 구현과 검증

- **`FakeChatTransport`:** 서버 없이 채팅 상태를 시연하고 지연·중복·누락 상황을 테스트합니다.
- **`RealChatTransport`:** Android Debug에서 로컬 WebSocket 메시지와 HTTP 누락 조회를 사용합니다.
- **Spring Boot·PostgreSQL:** 메시지 순번과 멱등 키를 저장하고 ACK·메시지 조회를 처리합니다.

[PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)에서 전송 경로 변경·인증 실패·미확정 메시지 보호를 포함한 Android 로컬 계측 **28/28**을 확인했습니다. 서버의 중복 처리와 장애 주입은 [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227)에서 검증했습니다.

[채팅 설계 결정](../adr/ADR-0003-chat-consistency.md) · [장애 주입 시나리오](failure-recovery.md) · [로컬 서버 실행](../../backend/chat-server/README.md)
