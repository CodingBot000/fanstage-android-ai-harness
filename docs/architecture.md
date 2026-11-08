# Android 아키텍처 및 의존성 주입

앱 모듈은 조립 지점이다. 순수 Kotlin `core:model`은 모델과 Repository 계약, `core:data`는 Fake 구현을 제공한다. `core:artwork`와 `core:designsystem`은 화면 공통 자원을, `feature:*`는 각 화면과 ViewModel을 제공한다. 모듈 의존 방향은 `scripts/check-ux-module-boundaries.py`가 검사한다.

`FanStageApplication`의 Hilt 그래프는 Repository 인터페이스에 unscoped Fake 구현을 제공한다. Discover·Media·Community·Messages ViewModel은 Activity의 ViewModelStore에 속해 화면 간 공유된다. Discover Fake는 상태가 없으며 Community·Messages의 가변 Fake는 각각 ViewModel이 보유한다. `ChatViewModel`은 Navigation BackStackEntry에 속하고 `SavedStateHandle`의 `roomId`를 통해 새 Fake Transport를 생성한다. 테스트와 benchmark에는 명시적으로 Transport를 전달하는 Factory가 남아 있다. Player와 Chat의 기존 생성·교체·해제 정책은 변경하지 않았다.

구현 이력과 검증 제한은 [Hilt ADR](adr/ADR-0004-hilt-di-migration.md) 및 [검증 기록](verification.md)을 참조한다.
