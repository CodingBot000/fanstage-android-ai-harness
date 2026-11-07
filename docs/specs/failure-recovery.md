# Failure Injection & Recovery · 테스트 계약

단순한 성공 시나리오 외에 **결과 불명, 이전 이벤트, 순서 역전과 재연결**을 재현할 수 있도록 Fake transport·TCP 프록시·격리 PostgreSQL과 테스트용 HLS fixture를 사용합니다.

| ID | 주입 상황 | 검증한 동작과 범위 |
|---|---|---|
| NET-01 | 테스트 서버의 연결 단절/복구 | 재구독·snapshot, Android 소켓 취소 뒤 재연결 대기 및 재시도 상한. 물리 Wi-Fi 토글과 동일한 단일 실험은 아님 |
| NET-02 | 서버 commit 후 ACK/발신자 echo 유실 | Android Pending 불명 유지, 같은 clientMessageId 재전송, 서버 중복 저장·방송 방지 |
| NET-03 | 수신 이벤트 순서 역전·중복 | serverSequence 기반 정렬/중복 제거와 누락 cursor 처리 |
| NET-04 | 재연결 중 룸 변경 | 이전 룸의 늦은 이벤트·요청을 현재 룸 상태에 적용하지 않음 |
| NET-05 | 지연 HTTP snapshot | 최신 실시간 이벤트와 이전 snapshot을 합칠 때 현재 상태 보존 |
| NET-06 | 합성 10/50/100 msg/s | Fake/Compose 경로의 메시지 보관·화면 표시 상한 검사. 운영 네트워크 부하 결과는 아님 |
| MEDIA-01 | HLS segment 오류/지연 | Player 오류·재시도 UX, 채팅과 별개인 재생 수명 경계 |
| LIFE | 화면 이탈·재진입·재생성 | Player/Listener/Collector, coroutine 취소와 채팅 transport 정리 경로 |

## 핵심 판단

ACK가 없다는 사실만으로 저장 실패를 의미하지 않습니다. 클라이언트는 동일 멱등 ID로 재전송하거나 서버 상태를 확인해야 하며, 재연결 후에는 serverSequence 기반으로 빠진 범위를 보충해야 합니다. 뷰가 바뀐 뒤 늦은 응답이 들어오더라도 현재 화면을 훼손하면 안 됩니다.

## Reproduction / Evidence

- [서버 GitHub Actions](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227)는 격리 DB·WebSocket 및 fault 테스트를 실행합니다.
- [서버 테스트 스크립트](../../backend/chat-server/tests/)는 통합·장애 주입을 재현합니다.
- [Android 계측 테스트](../../android/app/src/androidTest/kotlin/dev/fanstage/android/)는 실제 Android의 UI/transport/Player 상태를 검증합니다.
- PR별 테스트 수와 로컬/CI 실행 구분은 [검증 인덱스](../verification.md)에 있습니다.

위 각 경로의 PASS는 기록된 fixture·환경·커밋 범위의 결과이며, 운영 서버·실물 네트워크·장시간 리소스 안정성의 전수 보증은 아닙니다.
