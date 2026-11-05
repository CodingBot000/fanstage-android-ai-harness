# UX-001 — Visual Foundation & Discover Home

상태: 진행 중. v0.3 UX-001~006 독립 확장 트랙; M8 Gate 완료를 의미하지 않는다.

## 범위 및 계약
기존 룸 상세·PlayerController·ChatViewModel·서버 계약은 유지한다. 앱 시작 경로에 Discover를 두고, 기존 룸 ID를 가진 LIVE 카드에서 기존 상세 경로로 이동한다. 가상 데이터는 로컬 Fake에서 제공한다. Community·DM은 각각 UX-003·UX-005에서 구현한다. Replay는 기존 합성 VOD 재사용을 검증한 뒤 연결하며, 작동하지 않는 카드는 기능처럼 표시하지 않는다. Shop은 이 트랙 범위 밖이다. 네트워크·상시 과금 인프라는 추가하지 않는다.

UI는 상태와 이벤트를 받는 stateless 화면과 상태를 소유하는 ViewModel로 구분한다. 최소 `core:designsystem`은 앱에만 의존되는 디자인 토큰을 제공한다. 기존 feature/live 및 core/model의 의존 방향은 바꾸지 않는다. 홈의 정상·로딩·오류 상태, 재시도 및 LIVE 왕복을 검증한다. 기존 계측 테스트와 시작 벤치마크는 시작 화면 변경의 영향을 받으므로 변경된 경로에 맞춰 검토한다. 이전 성능 측정과 새 홈 측정은 직접 비교하지 않는다.

2026년 참고: [Weverse 3.15 안내](https://weverse.io/notice/36020?hl=ko)는 홈의 My Feed·Discover·Party 및 하단 커뮤니티 접근을 설명한다. 구조적 참고만 하며 브랜딩과 콘텐츠를 복사하지 않는다.

## 완료 기준
- 홈 정상·로딩·오류 Preview와 실제 렌더링 캡처, LIVE 진입 및 복귀 검증.
- 관련 빌드·단위·계측 및 기존 회귀 결과를 실행 사실대로 기록.
- 기존 v0.1 증거를 보존하고 미실행은 NOT_RUN으로 기록.

## Open Questions
- 탭 상태 및 스크롤 복원의 구체적 구현은 UX-002 설계에서 검증한다.

## 현재 검증과 잔여 작업
API 36 에뮬레이터 홈 캡처, Discover 상태·LIVE 왕복 테스트 2건, 빌드·lint 및 기존 live 단위 테스트 PASS. 기존 LIVE 회귀는 최종 UI 소규모 변경 전 PASS. 상세 실행은 STATUS 및 build-log 참조. 별도 아티스트/추천 데이터 모델과 이미지 카드, 최종 코드 전체 계측 회귀, Preview 시각 확인은 미완료다. Replay는 준비 중인 정적 카드다. 최종 완료 판정은 보류한다.

## v0.3 연속 개발
기존 화면과 미커밋 증거를 보존한다. 가상 아티스트 및 콘텐츠 ID, 이미지와 실제 동작하는 탐색을 UX-001에서 보강한다. 전체 계측·서버 회귀와 성능 측정은 통합 단계에 계획하되 관련 P0/P1 검증을 우회하지 않는다.

## v0.3 구현 기록
가상 아티스트 3개 및 공통 ID, 로컬 이미지, Hero 및 추천 캐러셀을 추가했다. LIVE는 기존 룸으로, Replay는 기존 합성 VOD 플레이어로 연결한다. API 36 에뮬레이터에서 Discover 상태·LIVE 왕복·Replay 첫 프레임 및 복귀 3/3 PASS; assembleDebug·lintDebug PASS. [캡처](../evidence/ux-001/home-v03.png). 커뮤니티 항목 연결은 UX-003에서 구현한다. 전체 통합 회귀·성능 측정은 NOT_RUN_DEFERRED다.
