# UX-007 — Premium Visual Foundation

상태: 진행 중. 기준 `5bd022b`의 UX-001~006 및 M8 이력을 보존하는 별도 확장.

## 범위와 계약
사용자가 제공한 스타일 참고 이미지는 분위기·색채 참조로만 사용한다. 세 가상 아티스트의 독자적 콘텐츠 자산을 내장 imagegen으로 생성·선정하고 압축하여 앱에서 실제 참조한다. `:core:designsystem`의 색·타이포그래피·형태·간격 토큰을 다듬는다. 실존 인물·상표·기존 서비스의 고유 디자인은 복제하지 않는다. Player/Chat·서버 계약은 변경하지 않는다.

## 기존 기준
기준 화면: [UX-006 Home](../evidence/ux-006/home.png), [LIVE](../evidence/ux-006/live.png); UX-001~006의 기존 캡처와 테스트는 보존한다. 현재 기능 모델·Fake Repository는 `:app`, 재생/채팅은 `:feature:live`, 모델은 `:core:model`에 있다. 기존 태그와 DM IME/최신 메시지 스크롤을 보존한다.

## 검증 및 기록
생성 프롬프트·출처·해시·크기·매핑을 자산 문서에 기록한다. 실제 앱 렌더와 카드 크롭을 확인하고 관련 빌드·테스트를 실행한다. 테스트는 에뮬레이터만 보이는 ADB 환경에서만 수행한다. 전체 계측·서버 회귀·성능은 최종 단계에 수행하되 P0/P1 및 해당 기능의 필수 검증은 우회하지 않는다. 실기기 성능·독립 리뷰 등 미실행은 NOT_RUN으로 둔다.

## Open Questions
없음. 중대한 기능 범위나 데이터 정책 변경이 발견되면 결정 전 사용자에게 확인한다.

## UX-007 결과
내장 imagegen으로 그룹별 Hero·커버·피드 9개를 생성·검수해 WebP로 최적화하고 Fake 데이터와 화면에 연결했다. 생성 기록과 해시·크기는 [자산 목록](../portfolio/visual-assets.md)에 있다. 색상은 Ink `#21152C`, Plum `#653D75`, Canvas `#FAF8FC`, 그룹 accent plum/blue/emerald, radius 12/16/24dp, spacing 8/12/16/20/24dp. 기준 `5bd022b`; 관련 `:core:designsystem:assembleDebug :app:assembleDebug :app:testDebugUnitTest` PASS. 별도 API 36 에뮬레이터 [실제 Hero 렌더](../evidence/ux-007/artwork-render.png) 및 [두 번째 Hero](../evidence/ux-007/blue-render.png)를 확인했다. 전체 UI 회귀·서버·성능 NOT_RUN_DEFERRED. 커버·피드 전수 크롭 검증은 UX-009에서 이어간다.
