# Compose Architecture · 핵심 설계 계약

**대상:** app + feature(discover, community, media, messages, live) + core(model, data, artwork, designsystem).

## Contract

1. 단일 Activity의 app은 Navigation과 의존성 조립을 소유합니다. 개별 feature는 app 구현을 역참조하지 않습니다.
2. 화면의 영속적 UI 상태는 ViewModel StateFlow/UiState로 표시하고, 사용자의 Action은 동일한 단방향 경로로 처리합니다.
3. 화면 전용 임시 상태(탭 선택, 입력 초안, 스크롤)는 책임이 명확한 Compose 또는 ViewModel 위치에서 소유합니다.
4. 기능 사이 공통 도메인 모델·리소스·Fake Repository는 실제 재사용 경계가 있는 core로만 이동합니다.
5. 화면 이동·세션 교체 또는 coroutine 취소 후 늦은 응답이 현재 UI를 덮어쓰지 않도록 합니다.
6. Compose의 Lazy key·IME·fontScale·화면 재생성 회귀를 명시적 UI 테스트로 검증합니다.

## Evidence

- [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38): app에서 feature 책임 분리, 11개 모듈과 의존성 경계.
- [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53): LIVE transport 선택과 화면 상태 계약.
- [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59): 대화·댓글 IME와 LIVE 영상 위치의 AVD/실기기 회귀.
- [모듈 경계 체크 스크립트](../../scripts/check-ux-module-boundaries.py).

설계 선택은 [module ADR](../adr/ADR-0001-module-boundaries.md), 전체 그림은 [architecture](../architecture.md).
