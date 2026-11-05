# UX-005 — 로컬 Messages

상태: 진행 중. 가상 대화 목록과 상세, 로컬 전송·상태 표시를 구현한다. 데이터는 메모리 Fake이며 실제 전송·DM 서버·영구 저장은 범위 밖이다. LIVE Chat과 계약과 상태를 공유하지 않는다. 탐색·전송과 빈 입력을 계측 테스트하고 전체 통합 검증은 UX-006에 수행한다.

## 실행 기록
메모리 Fake 대화 목록과 상세·로컬 입력 및 저장 상태를 구현했다. API 36 DiscoverTest 7/7, assembleDebug·lintDebug PASS. [캡처](../evidence/ux-005/messages.png). 실제 전송이나 읽음 확인은 제공하지 않으며 세션 종료 시 메모리 데이터는 사라진다. 전체 suite·서버 회귀 NOT_RUN_DEFERRED.
