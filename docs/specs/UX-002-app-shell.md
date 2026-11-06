# UX-002 — App Shell 및 Navigation

상태: 진행 중. v0.3 UX 확장 트랙.

## 범위와 계약
Discover, Community, Media, Messages 네 탭을 앱 상위 Navigation에서 연결한다. 탭 전환 시 Navigation의 saveState/restoreState를 이용해 백스택과 화면의 저장 가능한 스크롤 상태를 복원하고, 탭 재선택은 중복 목적지를 만들지 않는다. 상세 화면에서는 하단 탐색을 숨기고 뒤로가기로 이전 화면으로 돌아간다. 미구현 화면은 준비 중으로 표시하며 이후 단계에서 교체한다. 기존 LIVE 상세 및 Player/Chat 계약은 유지한다.

## 검증
탭 전환·재선택·상태 복원·상세 뒤로가기 및 실제 에뮬레이터 렌더를 확인한다. 전체 계측·서버 회귀는 NOT_RUN_DEFERRED로 기록하되 관련 P0/P1은 우회하지 않는다.

## Open Questions
없음.

## 실행 기록
네 탭과 Navigation save/restore를 연결했다. API 36에서 탭 4개 전환과 Discover 스크롤 복원, LIVE/Replay 왕복 및 Replay 첫 프레임을 포함한 DiscoverTest 4/4 PASS; assembleDebug·lintDebug PASS. [캡처](../evidence/ux-002/shell.png). 탭 내부는 UX-003~005에서 교체할 준비 중 화면이며, 공통 TopBar 완성과 상세 백스택·전체 통합 검증은 후속 단계에서 확인한다.
- UX-006에서 공통 TopBar 및 Media 필터 복원을 추가했다. 관련 계측과 최종 전체 계측 결과는 [통합 SDD](UX-006-integration.md) 참조.

## 상단 간격 조정 (진행 중)
공통 셸의 시스템 인셋을 하위 화면에 중복 적용하지 않고 네 기본 탭의 상단 콘텐츠 여백을 5dp로 통일한다. 상태 표시줄·상단 바는 유지한다.
- Debug 빌드·lint PASS. 전용 API 36 에뮬레이터 DiscoverTest 9/9 PASS(탭 이동·스크롤 복원·상세 뒤로가기·댓글·Replay 포함). 실기기 및 전 화면 접근성 검증 NOT_RUN.
