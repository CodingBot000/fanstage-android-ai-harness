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
