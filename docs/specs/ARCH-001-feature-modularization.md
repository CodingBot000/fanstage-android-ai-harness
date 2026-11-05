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

## ARCH-003 경계 점검 (진행 중, 2026-11-06)
Replay 화면을 기존 `:feature:live`로 옮겨 app에는 MainActivity/Navigation과 AppContainer만 남겼다. `scripts/check-ux-module-boundaries.py`에서 승인된 각 feature→core 의존, 순수 모델의 Android 참조 금지, app 소스 경계를 검사한다. 스크립트 PASS. 전용 AVD만 보이는 별도 ADB 서버에서 Discover 및 기존 LIVE 계측 13/13 PASS. 전체 실제 서버 통합과 최종 CI는 별도 수행한다.

## ARCH-003 로컬 통합 (2026-11-06)
기준 5개 모듈에서 app, core 4개, feature 5개 및 macrobenchmark 총 11개로 변경했다. app의 주 Kotlin 소스는 Navigation/MainActivity와 AppContainer 두 파일이다. `python3 scripts/check-ux-module-boundaries.py` PASS. 모듈별 관련 빌드 및 앱/데이터 단위 테스트, Debug·Release·Benchmark 빌드와 lint PASS.

별도 ADB 서버에서 전용 API 36 에뮬레이터만 보이는지 계측 직전 확인하고, 격리된 로컬 PostgreSQL/WebSocket 서버를 사용한 전체 Android 계측 14/14(스킵 0), 서버 통합·ACK 유실·연결 중단 회귀 PASS. 이후 Activity 재생성 후 게시물·대화 상태 및 IME 위 전송 버튼 검사를 강화해 `DiscoverTest` 9/9 추가 PASS. 임시 합성 서버 자격·DB는 종료 시 삭제했다. 관련 UI 8개 캡처와 [16초 무음 4탭 시연](../portfolio/media/ux-premium-demo.mp4)을 기록했다. 시연은 실제 방송·서버 전달을 보여주지 않는다.

NOT_RUN: 실기기 성능 A/B, 전 화면 큰 글꼴·TalkBack, 실제 DM/원격 LIVE, 독립 코드 리뷰 및 최종 PR CI. 이미지 생성 결과의 인물 동일성이나 모든 제3자 UI 권리 검토를 보증하지 않는다. M6 성능 미입증과 M8 예외·라이선스 한계를 보존한다. `ArtistId` 등은 현재 문자열 typealias여서 컴파일 시 다른 ID 유형 혼용을 강제 차단하지 못한다. 전체 connected 실행은 특별 ADB 서버에서 테스트용 에뮬레이터 하나만 보이는 것을 확인하고 실행했다.
