# UX-003 — Community 및 Feed

상태: 진행 중. 로컬 가상 아티스트와 Fake 게시물·좋아요·댓글을 제공한다. 서버·영구 저장은 범위 밖이다. 커뮤니티 목록에서 아티스트별 피드와 게시물 상세로 이동하며 Loading/Empty/Error를 표현한다. 화면은 UiState와 이벤트를 받고 ViewModel과 Fake Repository는 UI와 분리한다. LIVE 채팅 계약을 변경하지 않는다.

검증: 피드·상세 이동, 좋아요 및 댓글, 상태 화면과 관련 회귀를 에뮬레이터에서 확인한다. 전체 서버·계측 suite는 NOT_RUN_DEFERRED.

Open Questions: 없음.

## 실행 기록
가상 피드, 아티스트 커버·게시물/공지 탭, 상세와 메모리 내 좋아요·댓글을 연결했다. API 36에서 게시물 이동·좋아요·댓글을 포함한 DiscoverTest 5/5와 빌드·lint PASS. [Community 캡처](../evidence/ux-003/community.png). 공지 탭은 현재 빈 상태이며, 오류·빈 상태의 개별 주입 테스트와 상태 영속성은 미검증이다. 전체 suite/서버 회귀 NOT_RUN_DEFERRED.
- UX-006에서 가상 공지·빈/오류 상태 검사 및 취소된 요청 보호를 보완했다. [통합 SDD](UX-006-integration.md) 참조.
