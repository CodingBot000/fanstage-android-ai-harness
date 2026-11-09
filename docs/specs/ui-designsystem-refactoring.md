# UI 컴포넌트 재사용과 앱 화면 구성 — 설계·구현 기록

**상태:** Phase 2A·2B 구현 완료. Phase 3의 빌드·에뮬레이터·실기기 회귀 결과를 기록했으며 변경 전후 스크린샷 대조는 `NOT_RUN`이다.

설계 기준은 [PR #69 병합 커밋 `3299c9d`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/3299c9d97cd8a43a3360f4c3c77beac10ce4b22d), 구현은 [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70) (`5d335ec`)이다. 아래의 구조 분석은 **리팩토링 착수 당시 코드**를 다루며, 구현 결과는 마지막 절에 정리했다. [ADR-0005](../adr/ADR-0005-ui-component-reuse.md).

## 리팩토링 전 구조와 공통화 대상 (PR #69 기준)

11개 모듈과 의존 방향은 [아키텍처](../architecture.md)를 따른다. `core:designsystem`은 Compose Material3를 `api`로 제공하며 기능·모델·artwork·Navigation을 참조하지 않는다. 신규 Gradle 모듈이나 역방향 의존성을 추가하지 않는다.

- [FanStageTheme.kt](../../android/core/designsystem/src/main/kotlin/dev/fanstage/android/core/designsystem/FanStageTheme.kt): light 색상표(Plum, Ink, Canvas 등), Material3 typography의 세 글꼴 가중치, 12/16/24dp shape와 `FanStageSpacing` 8/12/16/20/24dp. 착수 당시 `FanStageSpacing`은 선언 외 Kotlin 사용처가 없었다. PR #70 이후 Media에서 기존 토큰을 사용한다. 같은 숫자가 항상 같은 의미는 아니며 현재는 dark theme 전역 정의도 없다.
- [BackTitleBar.kt](../../android/core/designsystem/src/main/kotlin/dev/fanstage/android/core/designsystem/BackTitleBar.kt): 제목 두 줄 측정·ellipsis·접기 상태·뒤로 접근성 및 `backTag`; Community와 게시물, Messages 대화, LIVE·Replay에서 사용한다. [CollapsingTitleBar.kt](../../android/core/designsystem/src/main/kotlin/dev/fanstage/android/core/designsystem/CollapsingTitleBar.kt)의 `CompositionLocal`과 nested scroll은 app 조립에 연결된다.
- [CardSingleLineText.kt](../../android/core/designsystem/src/main/kotlin/dev/fanstage/android/core/designsystem/CardSingleLineText.kt): 단일 행, 줄바꿈 방지와 말줄임. Media와 Discover 등에서 사용한다. [SendIcon.kt](../../android/core/designsystem/src/main/kotlin/dev/fanstage/android/core/designsystem/SendIcon.kt)은 댓글·DM·LIVE 전송 아이콘을 공유한다.
- [MediaScreen.kt](../../android/feature/media/src/main/kotlin/dev/fanstage/android/feature/media/MediaScreen.kt)의 `LiveMediaCard`, `LocalVideoCard`, `ReplayCard`는 클릭 Card, 16:10 Crop 이미지, 12dp 안쪽 여백, label/title/description의 3개 `CardSingleLineText`를 반복한다. Live/Replay는 `Alignment.TopCenter`, 로컬 영상은 기본 Center다. `media_<id>`/`media_replay`, 아티스트 선택과 click 대상도 서로 다르다.
- [DiscoverScreen.kt](../../android/feature/discover/src/main/kotlin/dev/fanstage/android/feature/discover/DiscoverScreen.kt)은 hero·가로 LIVE 카드·Replay·추천 콘텐츠를 사용한다. [CommunityScreen.kt](../../android/feature/community/src/main/kotlin/dev/fanstage/android/feature/community/CommunityScreen.kt)의 게시물은 작성자·본문·행동, [MessagesListScreen.kt](../../android/feature/messages/src/main/kotlin/dev/fanstage/android/feature/messages/MessagesListScreen.kt)의 대화 항목은 아바타·마지막 메시지·미읽음 상태가 중요하다. 이미지와 텍스트가 함께 있다는 사실만으로 같은 카드 계약으로 취급하지 않는다.
- Discover 로딩은 중앙 정렬·`discover_loading`, 오류는 텍스트와 재시도·`discover_error`; Media는 기본 spinner 또는 버튼만 표시한다. Community는 목록 항목에 loading/error/empty를 표시하며 `community_loading`, `community_error`, `community_empty`를 사용한다. [RoomScreens.kt](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/RoomScreens.kt)의 별도 `RoomListScreen`에도 `rooms_loading/empty/error/retry`가 있으나 현재 앱 시작 경로는 `DiscoverRoute`다. 사용되지 않는 경로를 근거로 공통화하지 않는다.
- [PostDetailScreen.kt](../../android/feature/community/src/main/kotlin/dev/fanstage/android/feature/community/PostDetailScreen.kt)와 [ConversationScreen.kt](../../android/feature/messages/src/main/kotlin/dev/fanstage/android/feature/messages/ConversationScreen.kt)은 `OutlinedTextField`의 trailing `IconButton`과 IME Send, 최대 네 줄을 사용하지만 여백·label·이벤트가 다르다. [ChatPanel.kt](../../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatPanel.kt)의 `ChatComposer`는 별도 `FilledIconButton`, 비어 있으면 비활성화, 성공한 경우에만 draft 초기화한다. 메시지 상태·연결 설정과 수명주기는 각 기능 소유다.
- [MainActivity.kt](../../android/app/src/main/kotlin/dev/fanstage/android/MainActivity.kt)의 실제 이름은 `FanStageApp` 하나다. 여기서 Activity 범위 Hilt ViewModel 4개, `NavController`, `NavHost`, Scaffold, 접히는 상단 `TopAppBar`, Haze 기반 floating `NavigationBar`, 라우트·URI 변환과 화면 조립을 맡는다. `FanStageAppShell`과 `FanStageNavHost`는 **착수 당시 존재하지 않았고**, 이후 PR #70에서 같은 파일의 함수로 구현했다. `ChatViewModel`은 `rooms/{roomId}`의 BackStackEntry 소유다.
- 하드코딩 예: Media grid 20dp 페이지·12dp 간격/카드 여백, Messages 목록 20dp·12dp, 앱의 상단 64dp와 floating bar 72dp·바깥 16dp·색상 alpha, LIVE 입력의 24dp 모양·48dp 버튼. 각 숫자의 기능·기하 의미가 다르므로 일괄 치환하지 않는다. LIVE/Replay의 검은 재생 배경은 전역 light palette로 통일하지 않는다.

## 분류와 계약

| 분류 | 후보 및 실제 사용처 | 책임·모델 및 API/슬롯 | 이점과 과잉 추상화 위험 / 테스트 영향 |
|---|---|---|---|
| A 기존 공통 유지 | Theme/Spacing: Media·Messages 등 다수의 동일 용도 여백 | `FanStageSpacing.page`/`medium` 등 기존 값 공개; 콘텐츠 모델 없음. 동일한 역할·값만 기존 상수로 치환, 용도 불명은 그대로 둔다. 슬롯 없음 | 불일치 감소. 같은 수치라도 의미가 다른 경우 결합 위험. 위치·크기·IME 영향 비교, 태그 유지. 새로운 의미 토큰은 반복 용도 확인 후 별도 검토 |
| A 기존 공통 유지 | `BackTitleBar`: Community/게시물/대화/LIVE/Replay | `title`, `onBack`, `modifier`, `backTag`와 기존 접기 Local; 모델 없음, 자의적 slot 추가 없음 | 이미 재사용 중. 새 헤더로 이중 추상화하지 않는다. `community_back`, `messages_back`, `room_back`, `replay_back`과 접근성·접기 유지 |
| A 기존 공통 유지 | `CardSingleLineText`와 `SendIcon`: Discover/Media, 댓글/DM/LIVE | 텍스트·style·modifier·기존 layout 콜백, 아이콘 contentDescription; 모델 없음 | 이미 공유된 정책만 보존. 추가 래퍼는 의미 없음. 한 줄·큰 글꼴·ellipsis 및 전송 태그/설명 유지 |
| B 우선 | Media 세 카드 | 아래 `MediaArtworkCard`는 feature 내부 private UI, `Painter`와 표시 문자열/콜백만 받고 모델·artwork 조회는 호출자가 수행 | 세 군데 동일 책임만 묶는다. 이미지 alignment를 고정하면 로컬 카드가 바뀌는 위험. `media_<id>`, `media_replay` modifier·click semantics, 필터·격자·16:10·세 텍스트 유지 |
| B 조건부 | App shell과 NavHost: `FanStageApp` 한 조립 지점 | `app` 내부에서 nav controller·현재 route·Haze·padding 및 content slot 전달. ViewModel 생성은 기존 Activity 소유 위치, Chat은 각 entry 소유 유지 | 소유권을 옮기지 않은 가독성 개선. composition 위치 변경은 상태 파괴 위험. `floating_navigation`, `tab_*`, `collapsing_top_bar`, back stack/IME/재생 검증 필수. 파일 분리 자체를 목표로 삼지 않는다 |
| C 유지 | Loading/Error/Empty: Discover·Media·Community·독립 RoomList | 각 상태·재시도는 기능 소유. Material3 spinner/Button/Text와 기존 배치 유지, 공통 API/slot 없음 | 형태·화면 내 위치가 달라 공통 상태 wrapper는 분기와 태그 이동 위험만 늘린다. 기존 태그와 동작 유지 |
| C 유지 | 댓글·DM·LIVE composer | 기능의 상태·submit·검증·IME/keyboard 소유. Material3 TextField/Button과 기존 SendIcon 사용, 새 API/slot 없음 | LIVE의 외부 버튼·비활성 조건·성공 시 draft 초기화와 다른 두 화면의 trailing 버튼을 하나로 묶으면 의미 변화 가능. `comment_*`, `dm_*`, `chat_*` 및 IME 보존 |
| C 유지 | 앱 TopAppBar·FloatingNavigation / 타 기능 카드 | Material3와 app 한 곳의 현재 구현을 유지. 재사용 slot/API 없음 | 다른 헤더의 back/접기 동작 및 haze·탐색 semantics가 달라 일반화 이점 부족. 위치·탭 선택·복원 테스트 유지 |

### Media 내부 후보 API (Phase 2A 제안)

```kotlin
@Composable
private fun MediaArtworkCard(
    image: Painter,
    imageAlignment: Alignment,
    label: String,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

`Card(onClick, modifier)`에 호출자의 testTag를 그대로 적용하고 이미지 contentDescription은 기존처럼 null, Crop과 비율은 고정한다. 세 문자열은 기존 색·타이포그래피의 `CardSingleLineText`로 표시한다. 이미지 slot은 현재 세 종류가 모두 Image이므로 도입하지 않는다. 호출자에서 `artworkResource`, title 현지화, `first`와 fallback의 기존 차이, 라벨, ID·클릭 및 imageAlignment를 그대로 결정한다. 문자열 모델은 전달하지 않으며 임의 기본 alignment를 넣지 않는다. 기존 feature 전용 함수는 어댑터로 유지하거나 호출 지점에서 동등하게 매핑한다.

### App 조립 분리 제안 (Phase 2B)

`FanStageApp`은 `rememberNavController`, Activity 소유 4개 Hilt ViewModel과 `remember` 값의 소유 위치를 유지한다. `FanStageAppShell`은 `currentRoute`, 접기 상태, Haze 상태, `onTabSelected`와 `@Composable (PaddingValues) -> Unit` 등 명시적 입력으로 Scaffold와 navigation UI만 그리는 후보이고, `FanStageNavHost`는 동일한 `NavHostController`, 기존 ViewModel 인스턴스·artists 및 필요한 callbacks를 받는다. 실제 구현에서 padding 소비·Haze source·nestedScroll·`CompositionLocalProvider`의 범위와 NavHost의 composition key를 현재와 동일하게 유지해야 한다. route 문자열, URI encoding, `popUpTo/saveState/restoreState/launchSingleTop` 및 Debug VOD 선택도 바꾸지 않는다. `hiltViewModel(viewModelStoreOwner = entry)`는 해당 목적지 안에 남긴다. Haze·Navigation이나 ViewModel을 designsystem에 노출하지 않는다.

## 단계·허용 범위

1. **Phase 2A**: 실제 배치와 의미가 같은 곳에 한해 기존 간격 토큰 적용, Media feature 내부 카드 중복만 추출. 새 토큰은 필요성을 별도로 검증하고 의미가 불명확하면 추가하지 않는다. `android/core/designsystem`과 `android/feature/media` 및 관련 테스트만 변경 후보. 디자인시스템 신규 범용 카드/상태/입력 컴포넌트는 계획하지 않는다.
2. **Phase 2B**: `android/app` 안에서 shell/NavHost의 책임 분리. Navigation 그래프, 상태 소유자, Surface·Socket 생성/해제, IME, 태그 및 배치 보존. feature·Gradle 의존성 변경은 원칙적으로 제외한다.
3. **Phase 3**: 변경된 단계의 빌드·Lint·단위/Compose·실기기 회귀 및 문서 정합성을 실행하고 증거와 미실행 항목을 기록한다. 화면 비교는 현재 기준 캡처와 같은 기기·화면 크기·글꼴 배율·방향·테마로 비교하며 실제 수행 전 PASS로 표시하지 않는다.

공통 제외: 신규 모듈, 기능·데이터 모델·서버·네트워크 변경, 시각 디자인 개편, route/BackStack 변경, ViewModel scope 이관, Player/Surface·Socket 소유권 변경, testTag 삭제/변경, 테스트 약화. 동등한 이름의 숫자를 기계적으로 토큰화하지 않는다.

## 검증 계획과 수용 기준

- 각 단계에서 `android/`의 `./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug testDebugUnitTest`와 `python3 scripts/check-ux-module-boundaries.py` 실행. 필요시 CI와 동일하게 일반/Hilt 계측 APK를 각각 컴파일한다. 명령 및 결과·기기·커밋을 기록한다.
- Media: `DeviceUiRegressionTest`의 짧은/긴 제목 크기, 큰 글꼴과 단일 줄·말줄임, filter 0/1/2, `media_<id>`/`media_replay` 클릭과 LIVE/VOD 구별, 이미지 Crop과 TopCenter/Center 차이 검증. 기존 `LocalMediaTest`, `DiscoverTest` 경로와 에러/빈 데이터도 확인한다.
- Shell: `FloatingNavigationTest`, `CollapsingTitleBarTest`, `UiContractTest`, `RoomNavigationSmokeTest`, `DeviceUiRegressionTest`, `PlayerSettingsTest`, `HiltRoomLifecycleTest`의 관련 범위를 실행한다. 탭 복원·뒤로가기, 홈 복귀·회전·Activity 재생성, LIVE/Replay 진입·이탈과 Surface, 채팅 재진입·draft·IME·전송을 확인한다. 실제 서버 연결은 해당 경로에 영향이 있으면 격리 서버로 별도 검증한다.
- 접근성 설명·semantics tree와 태그가 유지되고, 클릭·레이아웃·색·이미지 정렬 및 글자 크기별 스크린샷이 기준과 일치해야 한다. 확인되지 않은 항목은 `NOT_RUN`. 최초 회전 실패 원인 미확정 및 장시간 누수 `INCONCLUSIVE`는 새 증거 없이 바꾸지 않는다.
- **Phase 1 설계 문서만 변경하던 당시에는** `python3 scripts/check-portfolio-docs.py`, `git diff --check`와 코드·문서 대조만 요구했다. 이후 Phase 2A·2B에서는 실제 빌드·기기 회귀를 실행했으며 결과는 아래 기록을 따른다.

## 롤백

2A의 카드 추출과 토큰 치환, 2B의 shell 분리는 독립 변경으로 유지한다. 회귀 시 해당 단계 커밋을 역순 revert하고 기존 태그·동작 및 위 영향 범위 테스트를 재실행한다. 이미 후속 단계가 해당 API에 의존하면 관련 단계까지 함께 되돌린다. 기록 삭제나 force push로 결과를 감추지 않는다.

## 구현 및 검증 결과 (2026-11-09 · PR #70)

`MediaArtworkCard`는 `feature:media` 안의 `private` 함수이고, `FanStageAppShell`과 `FanStageNavHost`도 `MainActivity.kt` 내부 함수다. 이번 작업에서 새 Gradle 모듈이나 범용 컴포넌트를 추가하지 않았다. 기기 테스트 결과는 [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)에 기록했으며 [PR 필수 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42502757479)와 병합 후 [Android](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381222)·[서버](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381250)·[문서](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381240) 검사는 PASS했다. 화면 캡처 비교, 이 리팩토링에 대한 실제 서버 및 장시간 누수 재검사는 수행하지 않았다.

- Media 세 카드는 `MediaArtworkCard`로 feature 내부에서 통합했다. Live/Replay의 TopCenter와 로컬 영상의 Center, 각 태그·콜백·문자열·artwork 조회를 유지한다. Media의 페이지·grid 간격·카드 패딩에 같은 값과 의미의 기존 spacing 토큰을 적용했다.
- `FanStageApp`에서 `FanStageAppShell`과 `FanStageNavHost`를 app 내부 함수로 분리했다. 네 일반 ViewModel은 기존 상위 위치에서 얻고 ChatViewModel은 해당 BackStackEntry에서 얻는다. route와 graph, haze·스크롤 연결, 배경 및 태그는 유지했다.
- `ANDROID_HOME`을 지정한 뒤 `:app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease :app:lintDebug testDebugUnitTest` (`-PfanstageDeviceTest=true`) **PASS**. 최초에는 SDK 경로 미설정으로 빌드가 시작 전에 실패했으며 경로 지정 후 재실행했다. 모듈 경계 및 문서 링크 검사는 PASS.
- API 36 `FanStage_FloatingNav_20261106` 에뮬레이터의 격리 `.devicecheck` 패키지에서 `FloatingNavigationTest`, `CollapsingTitleBarTest`, `UiContractTest`, `DeviceUiRegressionTest`, `DiscoverTest`, `RoomNavigationSmokeTest`, `PlayerSettingsTest` **43/43 PASS**. 별도 Hilt 러너의 `RepositoryBindingsTest` **3/3 PASS**. `:app:connectedDebugAndroidTest -PfanstageDeviceTest=true`에 해당 클래스 필터를 전달했고 Hilt 테스트에는 `-PfanstageHiltInstrumentation=true`를 추가했다.
- **NOT_RUN:** 변경 전후 동일 조건의 스크린샷 대조, 실제 서버 연동 및 장시간 누수 검사. 선택된 실기기 회귀는 시각적 동일성 전체나 장시간 안정성을 증명하지 않는다. 기존 회전 실패의 원인과 장시간 누수 판단은 바뀌지 않는다.

- Galaxy S22 Ultra (SM-S908N), Android API 36, 격리 `.devicecheck` 패키지에서 일반 회귀 43/43, Hilt 바인딩 3/3, 룸 수명주기 재실행 2/2 및 채팅 전송·재생 테스트 추가 반복 8/8 통과했다.
