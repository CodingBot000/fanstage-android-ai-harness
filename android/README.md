# Android 앱 빌드 및 테스트

## 프로젝트 구성

FanStage는 **Kotlin, Jetpack Compose, Coroutines/Flow, MVVM/UDF, Media3**를 사용하며 11개 Gradle 모듈로 구성됩니다.

- `app`: Navigation과 앱 의존성 조립
- `feature`: discover, community, media, messages, live
- `core`: model, data, artwork, designsystem
- `macrobenchmark`: 성능 계측용 독립 모듈

기본 Debug 앱은 가상 콘텐츠, 합성 HLS VOD와 Fake 채팅을 사용해 서버 없이 실행됩니다. LIVE 화면의 개발 설정에서는 로컬 Spring Boot WebSocket/HTTP 서버를 선택해 채팅 연동을 테스트할 수 있습니다.

## 준비 환경

- JDK 17, Android Studio 또는 Android SDK
- Android SDK Platform **37**, Build Tools **36.0.0**
- 저장소에 포함된 Gradle Wrapper **9.5.0**
- Android `minSdk 24`, `targetSdk 36`, `compileSdk 37`

`ANDROID_HOME`을 설정하거나 버전 관리에서 제외한 `local.properties`에 SDK 위치를 지정합니다.

저장소 루트에서:

~~~bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
~~~

Android Studio에서는 `android/` 디렉터리를 열어 Debug 앱을 설치합니다.

## 기능별 테스트

| 주요 검증 | 기록 |
|---|---|
| Media3 재생·화질/자막·전체 화면 | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · 단위 56/56, 로컬 API 36 계측 41/41 |
| 채팅 연결·상태·UI | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) · 단위 56/56, 로컬 API 36 계측 28/28 |
| 입력창·키보드·화면 위치 | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) · 단위 62/62, 로컬 API 36 관련 계측 24/24, 실기기 3/3 |

테스트용 합성 다중 화질 HLS·MP4·WebVTT는 `androidTest` 자산에 포함되어 있습니다. 일반 앱에는 별도의 합성 HLS VOD가 포함됩니다. 위 계측 결과는 로컬 AVD·실기기에서 실행한 결과이며, GitHub Actions는 Android 빌드·Lint·단위 테스트와 코드 경계 검사를 담당합니다.

## 로컬 미디어·채팅 서버

기본 영상은 합성 HLS **VOD**, 별도의 로컬 HTTP 영상은 사전 녹화된 **Progressive MP4 VOD**입니다. LIVE 테마의 데모 룸에서는 재생과 채팅을 함께 다루고, Replay에서는 녹화 영상 재생 화면을 제공합니다.

로컬 서버 준비와 연결 방법은 [서버 실행 안내](../backend/chat-server/README.md), 기능 계약은 [Media3 설계](../docs/specs/media3-player.md)와 [채팅 설계](../docs/specs/realtime-chat.md), 상세 결과는 [검증 기록](../docs/verification.md)을 참조하세요.
