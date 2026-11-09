# ADR-0005 · UI 컴포넌트 재사용 경계

상태: 적용 · 2026-11-09 · [상세 SDD](../specs/ui-designsystem-refactoring.md)

## 배경

`core:designsystem`은 이미 Theme, spacing, 접히는 뒤로 제목, 한 줄 카드 텍스트, 전송 아이콘을 제공한다. Media의 세 카드는 한 feature에서 같은 이미지·텍스트 배치를 반복하지만, 입력 UI·상태 UI·다른 카드들은 행동과 배치가 다르다. `FanStageApp`은 조립과 Navigation 책임이 한 함수에 모여 있다. 11개 모듈 의존 방향과 Player·채팅·ViewModel 수명은 [ADR-0001](ADR-0001-module-boundaries.md), [ADR-0002](ADR-0002-player-lifecycle.md), [ADR-0003](ADR-0003-chat-consistency.md), [ADR-0004](ADR-0004-hilt-di-migration.md)를 따른다.

## 결정

`core:designsystem`을 공통 UI의 경계로 유지하고, 용도와 값이 일치하는 **기존 디자인 토큰을 재사용한다**. 둘 이상의 기능에서 레이아웃과 동작이 같으며 도메인 모델에 의존하지 않을 때만 컴포넌트를 이 모듈에 추가한다. 공통 API는 표시 값·이벤트·필요한 슬롯을 받아 그리는 역할에 집중하고, Navigation·ViewModel·네트워크·Player의 상태는 소유하지 않는다. 이번 리팩토링에서는 새 범용 컴포넌트나 토큰을 만들지 않았다.

Media 카드 세 종류의 공통 레이아웃은 `feature:media`에, 앱 화면 구성과 NavHost는 `app`에 둔다. 겉모양만 비슷하고 동작이 다른 상태 UI·헤더·입력창은 각 기능에서 유지한다. Material3 Button·Card·TextField를 의미 없는 일대일 래퍼로 바꾸지 않는다. 기존 `BackTitleBar`, `CardSingleLineText`, `SendIcon`은 이미 재사용되는 책임으로 유지한다.

## 대안과 영향

- 모든 화면에 범용 카드·상태·입력 wrapper: 초기 중복은 줄어도 slot과 조건이 늘고 각 화면의 전송·접기·접근성 동작을 혼동한다. 채택하지 않는다.
- 모든 중복 유지: 모듈 표면적은 적지만 Media의 세 동일 레이아웃과 공통 spacing 관리가 계속 분산된다. Media 내부 추출 및 의미가 일치하는 토큰 사용으로 제한한다.
- 신규 디자인 시스템 Gradle 모듈: 기존 모듈과 책임이 겹치고 의존성 및 빌드 비용이 생긴다. 추가하지 않는다.

공통 API를 만들기 전에는 사용처의 중복과 동작·접근성·testTag가 같은지 확인한다. 수치가 같다는 이유만으로 간격을 통일하지 않는다. 회귀가 발생하면 해당 단계를 되돌리고 영향받는 화면과 수명주기를 다시 검증한다. Media 내부 카드와 app 조립 책임 분리는 [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)에 구현해 병합했다. `MediaArtworkCard`와 `FanStageAppShell`/`FanStageNavHost`는 각각 Feature·App 내부 함수로 유지한다. 에뮬레이터·실기기 선택 회귀 기록과 변경 전후 스크린샷 비교 `NOT_RUN`은 [SDD](../specs/ui-designsystem-refactoring.md)에 구분한다.
