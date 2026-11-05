# UX-006 — 통합 검증

상태: 로컬 검증 진행. CI·독립 리뷰·성능 검증과 병합은 별도다. v0.1 포트폴리오 준비 완료 상태와 별도로 관리한다.

## 검증 결과 (2026-11-05)

- PASS: 최종 소스 작업 트리에서 `:app:assembleDebug :app:assembleRelease :app:assembleBenchmark :macrobenchmark:assembleBenchmark :app:lintDebug testDebugUnitTest :app:connectedDebugAndroidTest`. 격리된 로컬 PostgreSQL/WebSocket 및 API 36 에뮬레이터, Android 계측 XML 14/14·실패/오류/건너뜀 0. 실제 서버 Android 통합 1건과 기존 LIVE/Chat 회귀 4건, 새 UI 9건 포함. 앱 단위 취소 회귀 2건 포함.
- PASS: 동일 격리 서버에서 `backend/chat-server/tests/integration.py`, `m5_faults.py`. 동시성·중복·롤백·재시작·ACK 유실·10초 연결 중단 복구. 임시 합성 자격 증명과 테스트 DB는 종료 시 삭제했다. 최초 별도 프로젝트명 실행은 테스트의 프로젝트명 허용 조건으로 FAIL했고 지원되는 격리 프로젝트에서 재실행 PASS.
- PASS: UI 테스트에서 한국어 2배 글꼴의 Discover·Community 제목, 탭 복원, 공지, 로컬 메시지 읽음·전송, Replay 첫 프레임 확인. 이는 전체 화면의 접근성·현지화 검증이 아니다.
- 화면 증거: [Home](../evidence/ux-006/home.png), [LIVE 합성 재생](../evidence/ux-006/live.png). 이전 단계 [Community](../evidence/ux-003/community.png), [Media](../evidence/ux-004/media.png), [Messages](../evidence/ux-005/messages.png)는 공통 TopBar 추가 전 캡처다.
- NOT_RUN: 동일 조건의 실기기 Macrobenchmark/A-B, 모든 화면의 큰 글꼴·TalkBack, 실제 DM 서버·원격 LIVE, 독립 리뷰 및 최종 PR CI. 기존 M6 수치를 새 UI의 성능 주장에 사용하지 않는다.

## 변경과 한계
공통 TopBar 및 탭 복원, Community 가상 공지·상태·로컬 읽음 표시, 화면 번역 및 취소된 오래된 요청의 오류 방어를 보완했다. 좋아요·댓글·메시지는 메모리 기반의 로컬 데모다. 로컬 읽음은 사용자가 수신 메시지를 열었음을 뜻하며 상대방의 수신 확인이 아니다. LIVE 상세는 기존 Player/Chat 주위의 표현을 정리했으며 영상은 합성 테스트 fixture다. 후속 성능 측정과 전 화면 접근성 검토는 위 미실행 항목을 유지한다.
