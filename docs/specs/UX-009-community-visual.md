# UX-009 — Community 및 Feed 시각 개선

상태: 진행 중. 아티스트 커버·프로필·탭, 피드·게시물 상세를 UX-007 자산과 토큰으로 개선한다. 기존 가상 ID와 로컬 좋아요·댓글의 세션 상태를 유지하고 별도의 상용 서버 기능을 추가하지 않는다. 키 및 기존 테스트 태그, 경로와 뒤로가기를 보존한다. 실제 렌더 및 스크롤·상태 전환을 검증한다.

## 결과 (2026-11-06)
아티스트별 커버·프로필·피드 이미지와 상세 화면을 변경하고 잘못된 뒤로가기 문구를 수정했다. `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest` PASS. 전용 API 36 에뮬레이터에서 Discover→Paper Lantern→Post, 좋아요·합성 댓글 입력·상세 왕복 후 상태 유지 수동 확인. [Artist/Feed](../evidence/ux-009/artist.png), [Post](../evidence/ux-009/post.png). 다른 AVD가 함께 연결되어 자동 connected 테스트는 NOT_RUN_DEFERRED; 전체 자동 회귀와 다른 아티스트의 크롭은 후속 통합에서 확인한다.
