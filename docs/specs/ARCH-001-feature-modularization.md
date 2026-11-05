# ARCH-001~003 — 공유 모델과 기능 모듈

상태: 진행 중. 기준 UX-011 `8126094`. UX-007~011의 실제 화면과 기존 LIVE/Chat/서버 계약을 보존한다.

## 경계 및 데이터 계약
`:core:model`은 Android/Compose 리소스에 의존하지 않는 공통 Artist/Content/Post/Conversation ID, ArtworkKey, 모델 및 Repository 계약을 제공한다. `:core:data`는 공통 가상 카탈로그와 세션 내 Fake Community·Message 저장소를 제공한다. UI 리소스 ID는 작은 Android `:core:artwork` 모듈의 resolver에서만 결정한다. 별도 자산 모듈은 여러 feature가 동일 자산에 접근하면서 feature→app 역방향 의존을 피하기 위해 필요하다.

앱은 세션 수명의 AppContainer에서 저장소 인스턴스를 조립하고 화면은 콜백으로 Navigation을 요청한다. 이후 feature Discover→Community→Media→Messages 순으로 이동한다. 각 이동에서 빌드와 관련 테스트를 실행하고 기존 테스트·태그·DM IME 및 Player/Chat 수명 계약을 유지한다. 전체 계측과 서버 회귀는 ADB 기기 격리 후 최종 단계에서 수행한다.

## 검증 및 미결정
모델의 Android 타입 누출, 카탈로그 ID 일관성, 가짜 저장소의 상태 유지·빈 입력·읽음 격리, 모듈 의존 방향 및 재시도 취소를 검증한다. 기존 사용자 `.gitignore` 변경은 별도 보존한다. Open Questions: 없음. 기능·저장 정책 변경이 필요하면 승인 전 구현하지 않는다.

## ARCH-001 결과 (2026-11-06)
공통 ID 별칭·ArtworkKey·모델/Repository 계약을 순수 `:core:model`, 세션 Fake 구현을 `:core:data`, 리소스 resolver와 아트워크를 `:core:artwork`로 분리했다. AppContainer가 세 저장소를 조립한다. 기능 UI는 모델/자산 계약을 사용하며 `:core:model`에는 Android/Compose import 및 `R.drawable` 참조가 없다. Fake 카탈로그 ID 결합, 좋아요·댓글 재조회, 빈 메시지·읽음 격리, 기존 취소 회귀 단위 테스트를 실행했다.

`:core:model:build :core:data:test :core:artwork:assembleDebug :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` PASS, `:app:lintDebug` PASS. 전용 AVD에서 Home·Community 및 Messages 표시와 좋아요 왕복을 수동 확인했다. [Home](../evidence/arch-001/home.png). Android connected 자동 실행은 다중 AVD로 NOT_RUN_DEFERRED. ID는 현재 문자열 typealias이므로 타입 간 컴파일 수준 혼용 방지는 제공하지 않는다. 다음 ARCH-002에서 각 기능 UI/ViewModel을 단계적으로 이동한다.

## ARCH-002 Discover 이동 (2026-11-06)
`Discover` 화면·Route·상태·ViewModel과 해당 문자열을 `:feature:discover`로 이동했다. app composition root는 API와 콜백만 사용하며 `feature:discover`는 model/designsystem/artwork에 의존한다. `:feature:discover:assembleDebug :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` PASS. 전용 API 36 AVD에서 Home→합성 LIVE Playing을 수동 확인했으며 자동 connected는 다중 AVD로 NOT_RUN_DEFERRED. 다음 Community 이동.

## ARCH-002 Community 이동 (2026-11-06)
Community 화면·Route·상태·ViewModel 및 문자열을 `:feature:community`로 이동했다. 카탈로그와 Repository 계약을 상위 app에서 주입하며 feature→feature/data 의존은 추가하지 않았다. `:feature:community:assembleDebug :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` PASS. 전용 API 36 AVD에서 아티스트→Post 좋아요 및 왕복 유지 수동 확인. connected 자동 검증은 다중 AVD로 NOT_RUN_DEFERRED. 다음 Media 이동.

## ARCH-002 Media 이동 (2026-11-06)
Media 화면과 독립 MediaViewModel을 `:feature:media`로 이동하고 공유 Repository 계약을 주입했다. Discover feature에 직접 의존하지 않는다. `:feature:media:assembleDebug :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` PASS; 취소된 Media 요청이 새 결과를 덮지 않는 단위 테스트 PASS. 전용 API 36 AVD에서 Media→Replay 필터→합성 재생 수동 확인. 전체 connected 자동 회귀 NOT_RUN_DEFERRED. 다음 Messages 이동.

## ARCH-002 Messages 이동 (2026-11-06)
Messages 목록·대화 화면과 ViewModel을 `:feature:messages`로 이동했다. 모델과 Repository 계약은 core:model, Fake 상태는 app의 AppContainer가 주입하는 core:data 인스턴스다. 기존 IME padding·viewport/신규 메시지 스크롤과 태그를 유지한다. `:feature:messages:assembleDebug :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` PASS. 전용 API 36 AVD에서 대화 진입·IME 위 입력 및 합성 메시지 로컬 전송 수동 확인. connected 자동 회귀는 다중 AVD로 NOT_RUN_DEFERRED. 다음 ARCH-003 경계 감사·통합.
