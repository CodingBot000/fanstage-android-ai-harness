# UX-008 — Discover 및 App Shell 시각 개선

상태: 진행 중. UX-007 자산·토큰을 사용해 Discover Hero를 16:9로 구성하고 하단 탐색에 벡터 아이콘과 명시적 라벨을 적용한다. 기존 탭 상태·Room/Artist/Replay 이동과 로딩·오류·Retry를 유지한다. 합성 VOD를 이용하는 테스트 룸은 데모로 표시한다. 참고 전 화면은 [UX-006 Home](../evidence/ux-006/home.png). 변경 후 앱 캡처와 관련 빌드·테스트를 기록한다.

## 작업 기록 (2026-11-06)
16:9 Hero, 가로 미디어 카드, 벡터 하단 아이콘, 한국어/영어 데모 LIVE 라벨 적용. [변경 후 캡처](../evidence/ux-008/discover.png), 비교 기준 [UX-006](../evidence/ux-006/home.png). API 36 전용 에뮬레이터에서 카드→기존 LIVE 상세→뒤로가기→Community 탭을 수동 확인했다. `:app:assembleDebug :app:lintDebug :app:testDebugUnitTest` PASS. connected 계측은 ADB에 두 에뮬레이터가 보여 실행하지 않았고 NOT_RUN_DEFERRED; 좁은 화면 및 전체 경로는 최종 통합에서 추가 확인한다.
