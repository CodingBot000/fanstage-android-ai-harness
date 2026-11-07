# FanStage · Current Project Status

**기준:** 2026-11-08 · source portfolio · 메인 소스 PR #62 반영 시점 이후 문서 정리.

## 구현된 기능

- Kotlin / Compose, UDF 기반 Discover·Community·Media·Messages·LIVE 화면, app/feature/core로 분리된 11개 모듈.
- Media3 HLS 합성 VOD, Debug 로컬 Progressive MP4, 트랙 기반 화질·자막·배속, 전체 화면과 화면별 Player lifecycle.
- Fake 채팅과 로컬 Spring Boot·PostgreSQL WebSocket/HTTP 연동, ACK 재전송 멱등성, 순서·중복·누락 복구.
- 테스트 전용 프록시의 ACK·echo 유실 및 지연 snapshot·재연결 시나리오.
- 실기기 화면 녹화와 API 36 로컬 에뮬레이터 UI/계측 테스트.

## 최신 확인 가능한 대표 검증

| 기준 변경 | 검증 결과 |
|---|---|
| [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) | Debug 빌드·Lint·단위 62/62, 로컬 AVD 관련 24/24, Galaxy 실기기 관련 3/3 PASS |
| [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) | 단위 56/56 및 로컬 API 36 계측 28/28 PASS |
| [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) | 플레이어 단위 56/56 및 로컬 API 36 계측 41/41 PASS; PR Android·서버 CI 통과 |
| [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) | Galaxy S22 Ultra 화면·재생 데모 갱신, 링크·영상 디코딩 검사 |
| [PR #62](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/62) | 사용하지 않는 미디어 파일 정리, Android Debug 빌드·Lint PASS |

검증 범위와 현재 CI 실행 정책은 [verification](verification.md)에서 확인한다. 원격 에뮬레이터 자동 작업은 사용하지 않으며, 계측 결과는 위 PR의 로컬 실행 증거다.

## 작업 방식 및 문서 기준

- 코드·테스트·정적 검사 및 PR CI를 실제 완료 근거로 사용한다.
- 기능 계약은 [specs](specs/), 아키텍처 선택은 [ADR](adr/), 실행 정책은 [test-matrix](test-matrix.md)를 따른다.
- 상세한 과거 작업·실패·승인 과정은 Git 커밋·PR 기록을 참조한다. 현재 작업에서 새로 발견한 결함은 별도로 재현·수정한다.
- 신규 작업 시 [AGENTS](../AGENTS.md)와 관련 기술 문서를 읽고 해당 기능에 필요한 테스트를 선택한다.

**현재 데모 경계:** 합성/사전 녹화 영상, 로컬 개발 서버와 Fake 채팅. 운영 LIVE 서비스나 상용 대규모 트래픽을 제공하는 저장소는 아니다.
