# 장애 주입 및 복구 테스트

성공 상황만 확인하지 않고 **응답 유실·순서 역전·지연·재연결**을 테스트 환경에서 재현했습니다. 채팅은 Fake Transport와 격리 PostgreSQL·WebSocket 서버·TCP 프록시를, 영상은 테스트용 HLS 세그먼트를 사용합니다.

| 시나리오 | 장애 주입 | 확인한 처리 |
|---|---|---|
| NET-01 · 재연결 | 테스트 서버 10초 단절, 별도 Android 소켓 취소 | 재구독·HTTP 누락 보충, 재연결 대기와 횟수 제한 |
| NET-02 · ACK 유실 | 서버 저장 후 ACK·발신자 이벤트 제거 | Pending 유지, 같은 메시지 ID 재전송, 중복 저장 방지 |
| NET-03 · 중복·역순 | 이벤트 중복 전송 및 도착 순서 변경 | 서버 순번 기준 중복 제거·정렬·누락 탐지 |
| NET-04 · 룸 변경 | 재연결 중 채팅방 변경 | 이전 룸의 지연 응답 제외 |
| NET-05 · 조회 지연 | HTTP 조회가 최신 WebSocket 이벤트보다 늦게 응답 | 새 메시지 보존, 순번·누락 구간 유지 |
| NET-06 · 이벤트 급증 | 가상시간 기반 10/50/100건/초 합성 메시지 | Fake/Compose 메시지 보관·표시 상한 |
| MEDIA-01 · 재생 오류 | HLS 세그먼트 요청 오류·지연 | 오류 안내·재시도, 채팅 상태 유지 |
| LIFE · 화면 수명 | 화면 이탈·회전·재진입 | Player/Listener와 채팅 구독 정리 |

**설계 핵심:** ACK가 유실되면 클라이언트는 전송 결과를 확정하지 않고 같은 `clientMessageId`로 재전송합니다. 재연결 후에는 `serverSequence`로 빠진 범위를 조회하고, 이전 룸의 늦은 응답은 현재 화면에 반영하지 않습니다.

## 코드와 검증 근거

- [서버 통합·장애 주입 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227)
- [서버 테스트 코드](../../backend/chat-server/tests/) — 메시지 저장·조회·ACK 유실
- [Android 계측 테스트](../../android/app/src/androidTest/kotlin/dev/fanstage/android/) — 재생·화면·채팅 상태
- [테스트 실행 결과](../verification.md)

NET-06은 합성 이벤트 처리와 버퍼 제한을 검증한 시나리오입니다. 서버 단절과 Android 소켓 취소도 각 환경에서 재현했습니다.
