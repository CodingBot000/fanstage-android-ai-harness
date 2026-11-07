# 장애 주입 및 복구 · 테스트 시나리오

FanStage는 Fake transport, 격리된 Spring Boot·PostgreSQL 서버, 테스트용 TCP 프록시와 HLS fixture를 사용해 **정상 응답뿐 아니라 유실·지연·중복·재연결 상황**을 재현합니다.

| 시나리오 | 주입 조건 | 확인한 동작 |
|---|---|---|
| NET-01 · 연결 단절 | 테스트 서버의 10초 단절 및 별도 Android 소켓 취소 | 재구독·HTTP snapshot 복구 경로, 재연결 대기와 재시도 제한 |
| NET-02 · ACK 유실 | 서버 commit 이후 ACK 및 발신자 echo 유실 | Pending 유지·같은 `clientMessageId` 재전송·서버 중복 저장 방지 |
| NET-03 · 중복/역순 | 수신 이벤트 순서 역전과 중복 | `serverSequence` 기준 정렬·중복 제거·누락 탐지 |
| NET-04 · 룸 전환 | 재연결 중 다른 채팅방으로 이동 | 이전 룸의 지연 응답을 현재 상태에 반영하지 않음 |
| NET-05 · 지연 snapshot | WebSocket 이벤트보다 늦게 HTTP snapshot 응답 | 최신 이벤트 보존·중복 제거·누락 cursor 관리 |
| NET-06 · 메시지 급증 | 가상시간 기반 10/50/100 msg/s 합성 이벤트 | Fake/Compose 경로의 보관·표시 메시지 상한 확인 |
| MEDIA-01 · 미디어 오류 | HLS 세그먼트 지연·오류 | Player 오류·재시도 UI와 채팅의 독립성 |
| LIFE · 화면 수명 | 이탈·재진입·화면 재생성 | Player/Listener/Collector 및 채팅 구독 정리 |

## 복구 설계의 핵심

ACK가 없어도 서버 저장은 성공했을 수 있으므로, 클라이언트는 동일 메시지 ID로 재전송하거나 저장 결과를 확인합니다. 재접속 후에는 서버 순번을 기준으로 빠진 범위를 보충합니다. 화면이 바뀐 뒤 도착한 과거 응답도 현재 상태와 분리합니다.

## 재현·검증 자료

- [서버 GitHub Actions](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227): 격리 DB·WebSocket·장애 주입 검사
- [서버 테스트 코드](../../backend/chat-server/tests/): 통합 테스트와 프록시 장애 주입
- [Android 계측 코드](../../android/app/src/androidTest/kotlin/dev/fanstage/android/): 화면·Transport·Player 테스트
- [검증 결과 요약](../verification.md): PR별 테스트 수와 실행 환경

NET-06은 합성 이벤트 기반의 처리·버퍼 정책 검증이며 운영 네트워크 부하 측정과는 구분합니다. NET-01의 서버 단절과 Android 소켓 취소도 각각의 테스트 경로에서 확인했습니다.
