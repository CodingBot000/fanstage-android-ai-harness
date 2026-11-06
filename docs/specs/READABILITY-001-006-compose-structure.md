# READABILITY-001~006 — Compose 책임 분리

상태: 진행 중. 기준 `09728c43896b5e5aa134c2b856340e8aad696229` (PR #43 squash); 시작 작업 트리 clean, 별도 `codex/compose-readability` 브랜치. SDK `/Users/switch/Library/Android/sdk`, 기존 Gradle 캐시 사용. 코드와 UI 동작·Modifier 순서·lazy item/key·effect 키와 수명·Nav/Repository 및 11개 모듈을 보존한다. 외부 영상·서버·기기 데이터는 수정하지 않는다. 미결정 사항 없음.

## 변경 전 책임·공개 API와 이동 계획
| 기존 파일 | 기존 공개 API 및 책임 | 계획 |
|---|---|---|
| Community.kt | CommunityState/UiAction, CommunityViewModel, CommunityRoute/Screen, PostDetailRoute/Screen | Contract, ViewModel, Route, CommunityScreen, PostDetailScreen |
| Discover.kt | DiscoverState/UiAction, ViewModel, Route, Screen, localizedContentTitle, 3 Preview | Contract, ViewModel, Route, Screen/의미 있는 섹션 |
| Media.kt | MediaState/UiAction, ViewModel, Route, Screen | Contract, ViewModel, Route, Screen/필터·카드 |
| Messages.kt | MessagesUiState/Action, ViewModel, 목록/대화 Route와 Screen | Contract, ViewModel, Route, 목록 Screen, 대화 Screen |

동일 package와 public 함수 시그니처 유지. Community/Media의 saveable 탭·필터, 댓글·DM의 remember 초안, Conversation의 LazyListState와 effect는 기존 수명·키를 유지한다.

## 기존 검증·태그
CommunityActionTest, LoadCancellationTest, MessagesViewModelTest, UiContractTest, DiscoverTest, DeviceUiRegressionTest, RoomNavigationSmokeTest 및 core:data 테스트 유지. 주요 태그: community_feed/community_notice/post_*/post_detail/post_like/comment_input/comment_send; rooms_content/live_carousel/room_*/replay_card; media_screen/media_grid/media_filter_*/media_*/media_replay; messages_list/conversation_*/conversation_detail/unread_*/dm_input/dm_send. 다른 태그와 접근성 의미도 보존한다.

## 기준선
[PR #43](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/43) 최종 HEAD `c73d7ce`: Android/서버 CI 및 독립 리뷰 PASS, 원격 에뮬레이터 SKIPPED. 이는 변경 전 증거다. 이번 기준선 `ANDROID_HOME=… ./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin --no-daemon --max-workers=2` PASS. 본 작업의 계측·통합·CI NOT_RUN.

## READABILITY-001 Community
Contract, ViewModel, Community/Post 각 Route·Screen으로 기존 공개 선언을 이동하고 아티스트 헤더·게시물 카드를 의미 단위로 추출했다. 저장 가능한 탭, 댓글 초안과 VM 취소 처리는 기존 수명·연산 유지. SDK 지정 계획상의 CommunityActionTest·LoadCancellationTest·core:data와 app/계측 컴파일 PASS. 전체 계측 NOT_RUN (최종 단계). 다음 Discover.

## READABILITY-002 Discover
Contract/VM/Route/Screen 분리 및 Heading·Hero·Community·LIVE·Replay·추천 섹션 추출. Lazy item 순서와 key, Local VOD/benchmark 콜백, 기존 Preview 3개 유지. SDK 지정 app/계측 컴파일 및 LoadCancellationTest PASS; 전체 계측 NOT_RUN. 다음 Media.

## READABILITY-003 Media
Contract/VM/Route/Screen 분리, 필터 행 및 LIVE·로컬 영상·합성 Replay 카드 추출. Route의 saveable 필터와 그리드 조건·순서, artwork 조회 예외 정책 유지. SDK 지정 app/계측 컴파일 및 LoadCancellationTest PASS. 전체 계측 NOT_RUN. 다음 Messages.

## READABILITY-004 Messages
계약·VM·목록/대화 Route 및 Screen 분리, 메시지 버블과 composer 추출. 진입 읽음 effect, remember 초안, 대화 Screen 내 LazyListState와 두 effect의 키·수명·viewport 관찰 및 IME Modifier 순서를 유지한다. SDK 지정 app/계측 컴파일, MessagesViewModelTest 및 core:data:test PASS. 실제 IME/스크롤 계측 NOT_RUN (최종 단계).

## READABILITY-005 정적 감사
4 feature의 import를 명시적으로 정리하고 Kotlin 스타일로 포맷했다. ViewModel은 Compose import 없이 Repository/StateFlow만 소유한다. `git diff --check`, 모듈 경계 검사 및 app/계측 컴파일 PASS. 공개 함수 package/signature 유지, MainActivity/의존성/Live/서버/영상 미변경. 다음 최종 검증.

## READABILITY-006 통합 검증 — 차단
- SDK 지정 `:app:assembleDebug :app:assembleRelease :app:assembleRcLocal :app:assembleBenchmark :macrobenchmark:assembleBenchmark :app:lintDebug testDebugUnitTest :core:data:test --no-daemon --max-workers=2` PASS. 모듈 경계, m4/m6 manifest, `git diff --check` PASS.
- 새 전용 Pixel 2 API 36 AVD (1080×1920, 격리 ADB 5039, 단일 기기)에서 UiContractTest/DiscoverTest/DeviceUiRegressionTest/RoomNavigationSmokeTest 20건 중 18 PASS, 2 FAIL: DiscoverTest.communityPostLikeAndComment의 댓글 노드 미발견(line 120); DeviceUiRegressionTest.liveChatKeyboardSendAndBack의 send bounds assertion(line 125). 같은 AVD에서 변경 전 `09728c4`의 같은 두 테스트도 동일 지점 FAIL. 임시 가상 화면 override 1080×2400에서는 변경 전 LIVE 검사는 PASS, 댓글 검사는 동일하게 FAIL. 테스트 assertion을 변경·비활성화하지 않았다.
- 동일 Pixel 2, 언어/크기에서 기준·변경 APK의 Home/Community/Artist/Post/Media/Messages/Conversation 캡처 비교: 화면 본문(상태 표시줄 제외) 6개는 동일했고 Community는 탭 선택 애니메이션 프레임 차이. LIVE 진입·복귀는 계측에서 검사했으나 해당 bounds FAIL. 전 화면·모든 상태 동등성은 미검증.
- 기존 기기 데이터 변경을 피하려고 새 Pixel 6 AVD도 생성했으나 격리 ADB가 장치를 감지하지 못해 계측 NOT_RUN. 차단된 테스트의 원인 및 해결 전 PR/CI/리뷰/병합 NOT_RUN. 서버 회귀 NOT_RUN (변경 전 PR #43 서버 CI 참조), 외부 MP4·실기기·전체 TalkBack·성능 NOT_RUN.

## READABILITY-006 再検証 (ユーザー選択 1)
- ユーザーが既存失敗の追加調査と必要なテスト・LIVE UI修正の範囲拡大を承認。Communityの失敗は画面外の LazyColumn 項目を直接探していたため、`post_comments` タグを追加し、当該コメントへスクロールしてから送信・再生成後の内容を検証する。アサーションは削除せず、ユーザーの閲覧操作を再現する。
- LIVE位置検査の再失敗では入力欄 top 627.43dp、ボタン top 335.62dp と、同じ親子を別々の IME 移動フレームで測ったことを示す座標差が観測された。同様のDM検査も一度失敗。両者について2つの矩形が300ms安定してから元の全辺包含を検証する。LIVE製品コードは変更していない。初回失敗の根本原因の完全な証明はなく、タイミング依存のリスクは残す。
- 新規 Pixel 2 API 36 (1080×1920、ADB 5039単一対象): 修正前全20件は18 PASS/2 FAIL、変更前にも同一2件 FAIL。コメント修正後の焦点2件は初回および追加3回 PASS、ただし別の全体実行でLIVE座標失敗と次の実行でDM座標失敗を観測。安定待ち追加後、UiContractTest/DiscoverTest/DeviceUiRegressionTest/RoomNavigationSmokeTest **20/20 PASS**。元の失敗は上の記録に保持。
- 本体・タグ変更後の全ビルド/lint/unit/core:data PASS、モジュル境界・manifest・diff check PASS。テスト最終版も上記全20件でコンパイル・実行済。サーバ独立回帰は NOT_RUN (前回 main CI 参照)。実機・性能・全TalkBack・外部MP4は NOT_RUN。最終 PR CI・独立レビュー待ち。
