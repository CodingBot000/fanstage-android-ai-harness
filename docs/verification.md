# 테스트·검증 결과

테스트 결과는 **해당 기능의 PR, 실행한 환경, 통과 건수**를 기준으로 정리했습니다.

| 기능 | 실행 환경 | 결과·근거 |
|---|---|---|
| Media3 설정·전체 화면·수명주기 | Android 단위 테스트 / 로컬 API 36 에뮬레이터 | **56/56, 41/41 통과** · [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) |
| 채팅 UI·전송 경로 변경 | Android 단위 테스트 / 로컬 API 36 에뮬레이터 | **56/56, 28/28 통과** · [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 키보드·입력창 위치 | Android 단위 / 로컬 API 36 에뮬레이터 / Galaxy 실기기 | **62/62, 24/24, 3/3 통과** · [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) |
| Compose 기능별 모듈 분리 | Android 빌드·단위/기기 회귀 / 모듈 경계 검사 | **통과** · [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) |
| PostgreSQL·WebSocket·장애 주입 | GitHub Actions의 격리 서버 | **통과** · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) |
| 실기기 시연 | Galaxy S22 Ultra / 로컬 MP4 | 화면 탐색·재생 영상 검증 · [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) |
| Hilt DI·룸 수명주기 전환 | Galaxy S22 Ultra/API 36 및 격리 API 36 에뮬레이터 | 선택 일반 39/39, Hilt 바인딩 1/1, 격리 서버 Android 통합 1/1 PASS · [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) · [실행 기록](specs/hilt-di-migration-verification.md) |

위 수치는 각 PR에서 기록한 **해당 코드 버전의 실행 결과**입니다.

## Hilt 전환 및 플레이어 실기기 회귀

로컬 JDK 21과 JDK 17·Android SDK에서 Debug·Release 계열 빌드, Lint, 단위 테스트와 모듈·Manifest 검사를 실행했습니다. Galaxy S22 Ultra / API 36에서 선택한 화면·플레이어·채팅 계측 39/39와 Hilt 테스트 바인딩 교체 1/1이 통과했습니다. 별도 API 36 에뮬레이터와 격리된 PostgreSQL·WebSocket 서버를 사용한 Android 연결·복구 통합 테스트도 1/1 통과했습니다. 실제 백엔드의 실기기 연결, 장시간 누수와 성능 검증은 **NOT_RUN**입니다.

기기 계측 이후 최신 main을 통합했으며 Android 코드·테스트는 계측 당시와 같습니다. 최초 LIVE 테스트 실패, 원인 조사, 일시정지 버튼 수정과 재검증의 명령·범위는 [작업 검증 기록](specs/hilt-di-migration-verification.md)에 남겼습니다. GitHub [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67)과 병합 후 `main`의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874845) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874806) · [문서 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874813)가 성공했습니다. CI의 성공은 로컬 기기 테스트 재실행을 의미하지 않으며, 원본 로컬 계측 로그는 공개 증거로 첨부됐는지 별도 확인이 필요합니다. Hilt 전용 테스트 APK를 별도 컴파일하는 CI 단계는 [워크플로](../.github/workflows/android-ci.yml)에서 확인할 수 있습니다.


## 자동화와 기기 검증의 역할

**GitHub Actions**
- [Android CI](../.github/workflows/android-ci.yml): Debug·Release 계열 빌드, Android Lint, 단위 테스트, 모듈 의존성·Manifest 검사
- [서버 CI](../.github/workflows/m4-integration.yml): PostgreSQL·WebSocket 통합, 순번·재전송, ACK 유실·서버 단절 테스트
- [문서 검사](../.github/workflows/docs-check.yml): Markdown 내부 링크와 ADR 번호 검사

**로컬 Android 기기 검증**
- 에뮬레이터: 플레이어 전환·화질·자막, Compose 화면 복귀, 채팅 연결과 키보드 처리
- Galaxy 실기기: 화면·로컬 MP4 재생 시연, 관련 키보드·입력창 계측

Android 기기 계측 결과는 PR에 별도로 기록하며, GitHub Actions의 빌드·단위 테스트 결과와 구분합니다.

## 장애 재현과 회귀 테스트

메시지의 ACK 유실·재전송, 역순·누락 복구, 오래된 HTTP 응답, HLS 오류를 독립된 테스트 시나리오로 구성했습니다. 구현 계약과 재현 조건은 [장애 주입 테스트](specs/failure-recovery.md)에 정리했습니다.

[테스트 매트릭스](test-matrix.md) · [Android 실행 안내](../android/README.md) · [AI 개발·검증 과정](ai-development-workflow.md)
