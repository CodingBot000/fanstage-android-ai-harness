# 테스트 및 검증 결과

기능 구현과 함께 수행한 Android 단위·계측 테스트, 로컬 서버 통합 테스트, CI 실행 결과를 모았습니다.

## 대표 검증 이력

| 대상 | 테스트 결과 | 코드·실행 근거 |
|---|---|---|
| Media3 재생·트랙 설정·전체 화면 | 단위 56/56, 로컬 API 36 계측 41/41 PASS | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) |
| LIVE 채팅 UI·transport 교체 | 단위 56/56, 로컬 API 36 계측 28/28 PASS | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 댓글·대화·LIVE 입력창 | 단위 62/62, 로컬 API 36 관련 계측 24/24, Galaxy 실기기 3/3 PASS | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) |
| 기능별 Compose 모듈화 | Gradle 빌드·경계 검사·기기 회귀와 CI PASS | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) |
| WebSocket·HTTP·PostgreSQL | 통합 테스트와 격리 서버 장애 주입 CI PASS | [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) |
| 실기기 시연 자료 | Galaxy S22 Ultra 화면 탐색·로컬 MP4 재생, 영상 파일과 링크 검증 | [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) |

표의 테스트 수치는 **각 PR에 기록된 소스·기기 환경의 결과**이며 서로 합산하지 않습니다.

## 장애를 재현한 방식

- **ACK 및 발신자 echo 유실:** 테스트용 TCP 프록시로 이벤트를 제거하고, Pending 유지·동일 ID 재전송·서버 중복 방지를 확인했습니다.
- **늦게 도착한 HTTP snapshot:** 현재 수신 중인 WebSocket 이벤트와 병합해 메시지 순서·누락 cursor가 유지되는지 검사했습니다.
- **연결 단절과 복구:** 서버 단절·소켓 취소 시 재연결 횟수 제한, 구독 재수립, HTTP 보충 경로를 테스트했습니다.
- **영상 오류:** 합성 HLS 세그먼트 오류를 발생시켜 오류 표시·재시도·채팅과 재생의 독립성을 검사했습니다.

상세 조건은 [장애 복구 시나리오](specs/failure-recovery.md)에서 확인할 수 있습니다.

## 실행 환경

[Android CI](../.github/workflows/android-ci.yml)는 빌드·Lint·단위 테스트·모듈/Manifest 검사를 실행하고, [서버 CI](../.github/workflows/m4-integration.yml)는 격리 PostgreSQL·WebSocket·장애 주입 검사를 실행합니다. Android UI·플레이어 계측은 별도의 로컬 AVD 또는 실기기에서 수행해 해당 PR에 결과를 남겼습니다.

테스트 선택 및 완료 조건은 [테스트 매트릭스](test-matrix.md), 직접 실행 명령은 [Android 안내](../android/README.md)를 참조하세요.
