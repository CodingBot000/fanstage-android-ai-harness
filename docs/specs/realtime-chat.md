# 실시간 채팅 · WebSocket/HTTP 계약

## 메시지 모델

| 필드 | 역할 |
|---|---|
| `roomId` | LIVE 채팅방 식별자 |
| `clientMessageId` | 재전송 시 유지하는 클라이언트 메시지 ID(멱등 키) |
| `messageId` | 서버에서 확정한 메시지 ID |
| `serverSequence` | 채팅방 단위 서버 메시지 순번 |
| `status` | Pending / Sent / Failed 등 전송 상태 |
| `connectionState` | 연결 중 / 연결됨 / 재연결 / 종료 등 연결 상태 |

## 전송·복구 처리

1. ACK 수신이 지연되면 메시지를 Pending으로 유지하고 결과를 확인합니다.
2. 동일 `clientMessageId`로 재전송한 메시지는 서버에서 중복 저장·방송하지 않도록 처리합니다.
3. WebSocket 실시간 이벤트와 HTTP snapshot을 같은 `serverSequence` 기준으로 합쳐 중복·역순·누락을 관리합니다.
4. 화면을 나가거나 룸을 변경한 뒤 도착한 이전 응답은 현재 화면에 반영하지 않습니다.
5. 클라이언트 메시지 보관·표시 버퍼를 제한해 무한 적재를 막습니다.

## 데이터 경로

- **Fake transport:** 오프라인 데모 및 지연·중복·누락 상황을 재현하는 결정적 테스트
- **Real transport:** Android Debug에서 선택할 수 있는 로컬 WebSocket 이벤트와 HTTP snapshot
- **Backend:** Spring Boot·PostgreSQL에서 메시지 순번·중복 전송·조회 처리

[PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)에서 LIVE 채팅 UI·연결 전환과 로컬 API 36 계측 28개를 검증했습니다. 메시지 전송 실패와 복구는 [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) 및 [장애 주입 시나리오](failure-recovery.md)에 연결됩니다.

**설계 근거:** [메시지 정합성 ADR](../adr/ADR-0003-chat-consistency.md) · [로컬 서버 실행](../../backend/chat-server/README.md).
