# Realtime Chat · WebSocket / HTTP Contract

## Message model

| 필드 | 의미 |
|---|---|
| roomId | LIVE 룸 식별자 |
| clientMessageId | 같은 내용을 재전송할 때 유지하는 클라이언트 멱등 ID |
| messageId | 서버 확정 메시지 ID |
| serverSequence | 룸 단위 서버 영속 순번 |
| status | Pending / Sent / Failed 등 UI 전송 상태 |
| connectionState | Connecting / Connected / Reconnecting / Disconnected 등 연결 상태 |

## State consistency

1. ACK 수신이 지연되면 전송 실패를 단정하지 않고 결과 불명 Pending으로 유지합니다.
2. 동일 clientMessageId의 재전송은 PostgreSQL 저장·서버 브로드캐스트의 중복을 방지합니다.
3. WebSocket 이벤트와 HTTP snapshot은 같은 룸 serverSequence로 합치며 중복·역순·누락 범위를 처리합니다.
4. 이전 룸의 지연 이벤트, 화면 종료 후 구독 결과 또는 오래된 snapshot이 현재 UI에 반영되지 않도록 합니다.
5. 화면·Fake 데이터 메시지 버퍼를 제한하고 무한 적재를 피합니다.
6. 네트워크 구간 전체의 exactly-once delivery를 주장하지 않으며, **서버 멱등 처리와 클라이언트 표시 정합성**을 구분합니다.

## Implementation and evidence

- **Fake transport:** 오프라인 데모 및 결정적 지연·누락·중복 테스트.
- **Real transport:** Debug에서 선택하는 로컬 Spring Boot WebSocket 이벤트, HTTP snapshot 보충, PostgreSQL 영속 순번.
- [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53): UI/transport 선택과 관련 28개 로컬 AVD 계측.
- [Server CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) 및 [장애 주입 계약](failure-recovery.md).

외부 인증/TLS가 적용된 운영 채팅 서버가 아니라 로컬 통합 테스트 서버입니다. [Backend 실행](../../backend/chat-server/README.md).
