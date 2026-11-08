# FanStage 개발 현황

**기준:** 2026-11-08 · Android 소스 포트폴리오

## 현재 구현

- **UI:** Discover·Community·Media·Messages·LIVE, Compose 기반 11개 모듈
- **미디어:** Media3 HLS·로컬 MP4 재생, 화질·자막·배속·전체 화면, 플레이어 수명주기 관리
- **채팅:** Fake 및 로컬 WebSocket·PostgreSQL, ACK 재전송·메시지 순서·누락 복구
- **DI:** Hilt 기반 Repository·ViewModel 주입, Activity 공유와 룸별 수명주기 유지
- **검증:** Android 빌드·단위 테스트, 로컬 기기 계측, 서버 통합·장애 주입 검사

## 개발·검증 기준

- 관련 기능 계약과 [설계 결정](adr/)을 확인한 뒤 변경합니다.
- 검사 선택은 [테스트 매트릭스](test-matrix.md), 실제 결과는 [검증 문서](verification.md)와 해당 PR을 기준으로 합니다.
- Codex 작업 제어·리뷰와 검증 절차는 [AI 개발 워크플로](ai-development-workflow.md)를 따릅니다.
