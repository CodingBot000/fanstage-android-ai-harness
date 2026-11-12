# Android 앱 실행·테스트 안내

FanStage Android 앱은 Kotlin·Jetpack Compose·Coroutines/Flow·MVVM/UDF·Hilt·Media3 기반으로 구현했습니다. 이 문서는 **앱을 실행하고 실제 화면을 재현하는 방법**에 집중합니다. 기술적 의사결정은 [아키텍처](../docs/architecture.md), 검증 수치는 [검증 결과](../docs/verification.md)에서 확인할 수 있습니다.

## 필요한 환경과 실행

- JDK 17 및 Android Studio 또는 Android SDK
- Android SDK Platform 37, Build Tools 36.0.0
- 저장소에 포함된 Gradle Wrapper 9.5.0
- Android 설정: `minSdk 24`, `targetSdk 36`, `compileSdk 37`

`ANDROID_HOME`을 지정하거나 버전 관리에서 제외한 `local.properties`에 SDK 경로를 설정합니다. Android Studio에서는 `android/` 폴더를 열고 Debug 앱을 설치합니다.

```bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
```

## 시연 경로

### 서버 없이 영상 재생하기 — 권장 시작 경로

1. 설치한 **Debug 앱**을 실행합니다.
2. 하단 탐색바의 **Media** 탭을 선택합니다.
3. Media 목록의 **Synthetic HLS** 카드(설명: Paper Lantern)를 누릅니다.
4. **Replay** 화면으로 이동한 뒤 앱에 포함된 합성 HLS 영상을 재생합니다.
5. 재생·일시정지·전체 화면 등 플레이어 동작을 확인합니다. 화질·자막 선택은 실제 소스에서 제공하는 트랙에 따라 표시됩니다.

**예상 결과:** 로컬 서버를 실행하지 않아도 Replay 플레이어 화면에서 영상이 재생됩니다. 이 경로는 앱에 포함된 HLS **VOD** 자산을 사용합니다.

**경로 구분:** Debug의 **Discover 화면 일반 다시보기 카드**는 로컬 MP4 영상 경로로 연결됩니다. 서버 없는 HLS 시연은 **Media → Synthetic HLS** 카드를 사용해야 합니다. [화면 이동 구현](../android/app/src/main/kotlin/dev/fanstage/android/MainActivity.kt) · [Media 카드 구현](../android/feature/media/src/main/kotlin/dev/fanstage/android/feature/media/MediaScreen.kt)

### LIVE · 채팅과 로컬 서버 시연

| 화면/기능 | 콘텐츠·동작 | 필요한 환경 |
|---|---|---|
| Discover·Community·Messages | 가상 콘텐츠와 화면 탐색 | Debug 앱 |
| Media → Synthetic HLS → Replay | 앱 내 HLS VOD 재생 | Debug 앱 |
| LIVE 상세(지정된 3개 룸) | 로컬 서버의 녹화 MP4 + 기본 Fake 채팅 | Debug 앱 + 로컬 미디어 서버 |
| Media의 로컬 영상 카드 | 로컬 MP4를 Replay 화면으로 재생 | Debug 앱 + 로컬 미디어 서버 |
| LIVE의 실제 채팅 연결 | 개발 설정에서 로컬 WebSocket·PostgreSQL 서버로 전환 | Debug 앱 + 로컬 채팅 서버·테스트 토큰 |

LIVE 채팅은 기본 Fake 데이터로도 화면 동작을 확인할 수 있습니다. 실제 송수신·재연결을 확인할 때는 [서버 실행·설정 안내](../backend/chat-server/README.md)를 참조하세요.

## 프로젝트 구성

- `app`: Navigation, Hilt 의존성 조립, 화면 진입
- `feature`: discover, community, media, messages, live
- `core`: model, data, artwork, designsystem
- `macrobenchmark`: 성능 계측 전용

총 **11개 Gradle 모듈**입니다. `core:designsystem`은 여러 화면에서 재사용되는 UI를 제공하고, Media 카드 3종의 공통 레이아웃은 `feature:media` 내부 `MediaArtworkCard`로 유지합니다. Scaffold와 NavHost는 `app`의 `FanStageAppShell`·`FanStageNavHost`에서 구성합니다.

[모듈·UI 설계](../docs/architecture.md) · [UI 공통화 결정](../docs/adr/ADR-0005-ui-component-reuse.md) · [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)

## 테스트와 검증

GitHub Actions는 Android 빌드·Lint·단위 테스트·모듈 경계 검사 및 일반/Hilt 계측 APK **컴파일**을 수행합니다. Compose·플레이어·채팅 UI 계측 **실행**은 로컬 API 36 에뮬레이터나 Galaxy 실기기에서 진행해 해당 PR에 연결했습니다.

재생·전체 화면은 [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47), 채팅 화면·서버 전환은 [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53), IME/입력 배치는 [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59), Hilt·룸 수명주기는 [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67), 검증 감사와 UI 공통화는 [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69) · [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)에 기록했습니다.

Hilt 바인딩 교체 계측은 다음처럼 명시적으로 실행할 수 있습니다.

```bash
cd android
./gradlew :app:connectedDebugAndroidTest \
  -PfanstageHiltInstrumentation=true \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest
```

일반 계측은 `-PfanstageHiltInstrumentation=true` 없이 실행합니다. 테스트별 환경·커밋·실행 결과는 [검증 결과](../docs/verification.md)에서 확인합니다.
