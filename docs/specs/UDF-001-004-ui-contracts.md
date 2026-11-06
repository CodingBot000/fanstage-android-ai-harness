# UDF-001~004 — UI 상태·행동 계약

상태: 진행 중. 기준 main `da28ee1`; 브랜치 `codex/udf-contract-refactor`. 사용자 승인 계획에 따라 기존 MVVM에 명시적 행동과 Route/Stateless Screen 경계를 적용한다. 11개 모듈, Navigation, Repository, UI 및 Player/Chat/서버/로컬 MP4 계약은 유지한다. 새로운 MVI 프레임워크나 효과 파이프라인은 도입하지 않는다. 화면 전용 탭·필터·초안·스크롤은 Compose 소유로 유지한다. 불명확한 정책: 없음.

## UDF-001
CommunityState의 Loading/Error/Ready 의미를 유지하고 CommunityUiAction을 추가했다. CommunityRoute가 아티스트별 탭을 소유하고 PostDetailRoute가 상태와 초안을 관리하며 Screen은 값과 콜백만 받는다. 초안은 기존과 같이 저장하지 않는다. 앱 라우팅과 기존 UI 테스트 호출을 갱신했다.

검증: SDK 경로 지정 후 `:app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --tests '*LoadCancellationTest*' :core:data:test` PASS; `:app:testDebugUnitTest --tests '*CommunityActionTest*' --tests '*LoadCancellationTest*' :core:data:test` PASS. 최초 실행은 잘못된 태스크명과 SDK 미설정으로 각각 FAIL 후 수정했다. 에뮬레이터·전체 통합·CI는 NOT_RUN (UDF-004). 다음: Messages.

## UDF-002
MessagesUiState와 ConversationOpened/SendClicked 행동을 추가하고 목록·대화를 Route와 Screen으로 분리했다. 진입 읽음 처리는 artistId 기준 LaunchedEffect에 있고 초안은 기존처럼 remember만 사용한다. 스크롤 및 IME 로직은 Screen에 남는 UI 전용 상태다. 기존 Repository 및 Navigation 계약은 유지한다.

검증: `:app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --tests '*MessagesViewModelTest*' --tests '*LoadCancellationTest*' :core:data:test` PASS. 최초 계측 컴파일은 기존 테스트의 직접 메서드 호출 때문에 FAIL, 액션으로 갱신 후 PASS. 실제 계측·통합·CI NOT_RUN. 다음: UDF-003.

## UDF-003
Discover/Media 재시도에 필요한 액션만 연결했다. Media 필터는 Route의 rememberSaveable 단일 소유이며 Screen은 선택값과 콜백을 받는다. 내비게이션 콜백 및 미디어 데이터는 유지한다.

검증: SDK 지정 `:app:compileDebugKotlin :app:compileDebugAndroidTestKotlin :app:testDebugUnitTest --tests '*LoadCancellationTest*'` PASS (세 취소 경합 및 카탈로그 격리). 전체 통합·계측·CI NOT_RUN. 다음 UDF-004.

## UDF-004 통합 (진행 중)
- ViewModel 없이 PostDetailScreen과 ConversationScreen을 렌더링하고 액션을 검증하는 계측 테스트 2/2 PASS. 기존 DiscoverTest·DeviceUiRegressionTest·RoomNavigationSmokeTest 전용 API 36 AVD 18/18 PASS (재생·입력·Activity recreate 포함). 재생성은 프로세스 종료 복구를 증명하지 않는다.
- SDK 지정 `:app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease :app:assembleRcLocal :app:assembleBenchmark :macrobenchmark:assembleBenchmark :app:lintDebug testDebugUnitTest :core:data:test` PASS. 모듈 경계 및 m4/m6 manifest 검사 PASS. 서버 unit PASS; 기존 서버와 분리한 18766 포트의 임시 Compose 스택에서 동일 서버 통합·장애 스크립트(포트만 변경한 임시 복사본) PASS. 기존 서버·영상은 건드리지 않았다.
- 전용 AVD 홈 캡처와 README의 UX-008 홈을 육안 비교: 구조는 유사하나 기존 UX-012/로컬 VOD 변경으로 간격·라벨이 다르므로 전체 화면 픽셀 동일성은 미검증. 이미지 증거는 임시 파일로만 사용했다.
- NOT_RUN: 7개 외부 MP4 opt-in, 실제 기기·성능 A/B·전체 TalkBack·강제 프로세스 종료 복구. 원격 CI 및 최종 독립 리뷰 대기. 기존 사용자 요청으로 원격 에뮬레이터 작업은 비활성 상태이며 이번에 변경하지 않았다.
