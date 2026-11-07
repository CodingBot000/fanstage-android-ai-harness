# ADR-0003 · 채팅 전송 결과와 메시지 정합성

## 결정

클라이언트는 재전송할 때 동일한 `clientMessageId`를 사용하고, 서버는 이를 멱등 처리하여 중복 저장·방송을 방지합니다. 서버의 `serverSequence`로 실시간 WebSocket 이벤트와 HTTP snapshot을 합치며, 중복·역순·누락을 처리합니다.

## 선택 이유

ACK가 유실되어도 서버 저장은 성공했을 수 있습니다. 이때 전송을 바로 실패로 확정하거나 새 ID를 부여하면 중복 메시지가 생길 수 있습니다. 또한 가장 큰 순번만 저장하는 방식은 중간 누락을 놓칠 수 있어, 연속 확인 순번과 관측 순번을 구분합니다.

## 고려 사항

멱등 ID, 누락 구간 보충, 제한된 메시지 버퍼와 재연결 상태를 함께 관리해야 합니다. 목표는 네트워크 전 구간의 exactly-once 보장이 아니라 **서버의 중복 처리 방지와 클라이언트 화면 정합성**입니다.

**구현·검증:** [채팅 계약](../specs/realtime-chat.md) · [장애 주입](../specs/failure-recovery.md) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227).
