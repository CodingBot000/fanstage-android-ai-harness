# Verification Evidence · What Was Actually Tested

검증 수치는 원본 PR/CI의 해당 리비전과 연결합니다. **코드 생성, 컴파일 성공, 기기 실행, 운영 검증은 서로 다른 단계**입니다.

## 대표 검증 기록

| 기능 | 실행 증거 | 범위 |
|---|---|---|
| Media3·Lifecycle·트랙 설정 | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47), [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) | 로컬 단위 56/56, API 36 계측 41/41; Android 빌드·단위 CI |
| 채팅 UX·로컬 서버 연결 | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) | 로컬 단위 56/56, API 36 계측 28/28 |
| Compose IME·입력 UX | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) | 로컬 단위 62/62, API 36 관련 24/24, 실기기 3/3 |
| 모듈 경계 | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) | 독립 기능 모듈 이동, 컴파일·경계 검사·CI |
| WebSocket·PostgreSQL·fault injection | [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) · [재현 시나리오](specs/failure-recovery.md) | 격리 서버에서 ACK 유실, 재전송, 지연 응답, 순번·중복 |
| 소스 재현·영상 시연 | [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) | Galaxy S22 Ultra 로컬 MP4/화면 녹화, 링크·파일 검증 |

## 장애를 어떻게 주입했나

1. **ACK/echo 손실:** 테스트 전용 TCP 프록시에서 응답 이벤트를 유실시키고, Pending 유지와 같은 ID 재전송의 서버 중복 방지를 확인했습니다.
2. **지연 snapshot:** 라이브 WebSocket 이벤트보다 늦게 도착한 HTTP snapshot을 합성해 최신 state와 누락 cursor의 정합성을 검사했습니다.
3. **연결 재수립:** 서버 단절·소켓 취소를 주입하고 제한된 재연결 및 snapshot 보충을 확인했습니다.
4. **HLS 오류:** 테스트 fixture의 segment 오류를 주입해 재시도 UI, Player 정리, 채팅 경계가 유지되는지 검사했습니다.

시나리오·관찰의 구체적 구분은 [장애 복구 계약](specs/failure-recovery.md)을 따릅니다.

## 자동화 검증과 로컬 기기 검증의 역할

[Android GitHub Actions](../.github/workflows/android-ci.yml)는 빌드·Lint·단위·manifest·모듈 검사를 실행합니다. [서버 GitHub Actions](../.github/workflows/m4-integration.yml)는 실제 격리 데이터베이스·WebSocket 통합과 장애 주입을 실행합니다.

**Android 에뮬레이터 계측은 현재 로컬 실행 방식**이며 원격 PR CI의 자동 계측 결과로 서술하지 않습니다. 여러 디바이스/OS에서의 전수 검증이나 운영 트래픽·원격 생방송 환경의 증거도 이 결과에 포함하지 않습니다.

테스트 계층·완료 규칙은 [Test Matrix](test-matrix.md), 관련 소스는 [Android README](../android/README.md)에서 확인할 수 있습니다.
