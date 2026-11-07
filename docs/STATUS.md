# FanStage 개발 현황

**갱신:** 2026-11-08  
**현재 상태:** Android 포트폴리오용 소스 공개 및 기능 검증 자료 정리 완료

## 구현 범위

- **Android:** Compose 기반 Discover·Community·Media·Messages·LIVE 화면과 11개 Gradle 모듈
- **Media3:** 합성 HLS VOD·선택형 로컬 MP4, 재생/오류 상태·화질/자막/배속·전체 화면 및 수명주기 관리
- **채팅:** Fake transport와 로컬 WebSocket/HTTP·PostgreSQL, ACK 재전송 멱등성·메시지 순서·누락 복구
- **검증:** Android 빌드·Lint·단위 테스트, 로컬 기기 계측, 서버 통합·장애 주입 CI

## 검증 기준

최근 주요 UI 변경의 기기 검증은 [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59), 플레이어는 [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47), 채팅은 [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)에 연결됩니다. [PR #63](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/63)에서는 공개 문서와 CI 참조 구조를 정리했습니다.

기능별 테스트 수·실행 조건은 [검증 결과](verification.md), 자동화·계측 분리 기준은 [테스트 매트릭스](test-matrix.md)에서 확인할 수 있습니다.

## 이후 작업 기준

변경할 기능의 계약과 테스트를 먼저 확인한 뒤 필요한 검증을 실행합니다. 설계 근거는 [ADR](adr/), 기능별 장애·통신 계약은 [specs](specs/)를 참조합니다.
