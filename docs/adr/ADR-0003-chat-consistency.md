# ADR-0003 · Realtime Message Consistency

**Decision: Accepted.** 송신 불확실성과 표시 정합성을 분리합니다. 클라이언트는 clientMessageId를 재전송 시 유지하고, 서버는 중복 처리·room sequence를 영속화합니다. 실시간 WebSocket 이벤트와 HTTP snapshot은 동일 serverSequence로 합쳐 누락·역순·중복을 처리합니다.

**Why:** 연결 종료나 ACK 소실은 서버 commit의 성공/실패를 증명하지 못합니다. 단순 시간순 정렬과 마지막 cursor만 갱신하는 방식은 메시지 누락과 오래된 응답 덮어쓰기를 놓칠 수 있습니다.

**Trade-off:** 멱등 ID 관리·Gap 보충·제한 버퍼와 재시도 상태 머신이 필요합니다. 네트워크 전체의 exactly-once 전송이나 무한 보관을 보장하는 설계는 아닙니다.

**Evidence:** [채팅 SDD](../specs/realtime-chat.md) · [Fault 시나리오](../specs/failure-recovery.md) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227).
