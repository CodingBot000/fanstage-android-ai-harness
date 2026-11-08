# ADR-0005 · UI 컴포넌트 재사용 경계

상태: 적용 · 2026-11-09 · [상세 SDD](../specs/ui-designsystem-refactoring.md)

## 배경

`core:designsystem`은 이미 Theme, spacing, 접히는 뒤로 제목, 한 줄 카드 텍스트, 전송 아이콘을 제공한다. Media의 세 카드는 한 feature에서 같은 이미지·텍스트 배치를 반복하지만, 입력 UI·상태 UI·다른 카드들은 행동과 배치가 다르다. `FanStageApp`은 조립과 Navigation 책임이 한 함수에 모여 있다. 11개 모듈 의존 방향과 Player·채팅·ViewModel 수명은 [ADR-0001](ADR-0001-module-boundaries.md), [ADR-0002](ADR-0002-player-lifecycle.md), [ADR-0003](ADR-0003-chat-consistency.md), [ADR-0004](ADR-0004-hilt-di-migration.md)를 따른다.

## 결정

기존 `core:designsystem`을 공통 디자인 책임의 경계로 유지하고, **실제 용도와 값이 일치하는 기존 디자인 토큰을 우선 재사용한다**. 이번 구현에서는 신규 범용 토큰이나 컴포넌트를 추가하지 않았다. 이번 분석으로 식별된 신규 범용 컴포넌트는 없으며, 기존 API를 우선 재사용한다. 복수 기능에서 실제로 동일한 시각적·상호작용 책임이 있고 모델에 독립적인 경우에만 공통 컴포넌트로 추출한다. API는 표시 값·이벤트·필요한 slot만 받고 도메인 모델, Navigation, ViewModel, 네트워크 및 Player를 소유하지 않는다. 앞으로 조건이 충족될 때만 공통 API를 추가한다.

한 feature에서만 반복되는 Media 세 카드의 레이아웃은 `feature:media`에 둔다. 분리하는 app shell과 NavHost는 app에 둔다. 재사용 수요가 하나뿐이거나 비슷한 외형 외에 계약이 다른 상태 UI·헤더·입력창은 각 기능에 남긴다. Material3 Button·Card·TextField를 의미 없는 일대일 래퍼로 바꾸지 않는다. 기존 `BackTitleBar`, `CardSingleLineText`, `SendIcon`은 이미 재사용되는 책임으로 유지한다.

## 대안과 영향

- 모든 화면에 범용 카드·상태·입력 wrapper: 초기 중복은 줄어도 slot과 조건이 늘고 각 화면의 전송·접기·접근성 동작을 혼동한다. 채택하지 않는다.
- 모든 중복 유지: 모듈 표면적은 적지만 Media의 세 동일 레이아웃과 공통 spacing 관리가 계속 분산된다. Media 내부 추출 및 의미가 일치하는 토큰 사용으로 제한한다.
- 신규 디자인 시스템 Gradle 모듈: 기존 모듈과 책임이 겹치고 의존성 및 빌드 비용이 생긴다. 추가하지 않는다.

공유 API는 호출처를 결합하므로 실제 중복과 동일한 의미, 일관된 접근성 및 testTag를 확인한 뒤 도입한다. 동일한 dp 숫자만으로 토큰화하지 않으며, 시각적인 차이를 지우기 위한 정규화도 하지 않는다. 회귀가 나면 단계별 revert하고 변경 범위의 화면·수명주기를 재검증한다. Media 내부 카드와 app 조립 책임 분리는 [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)에 구현해 병합했다. `MediaArtworkCard`와 `FanStageAppShell`/`FanStageNavHost`는 각각 Feature·App 내부 함수로 유지한다. 에뮬레이터·실기기 선택 회귀 기록과 변경 전후 스크린샷 비교 `NOT_RUN`은 [SDD](../specs/ui-designsystem-refactoring.md)에 구분한다.
