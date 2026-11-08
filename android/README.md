# Android 앱 실행·테스트 안내

## 프로젝트 구성

FanStage는 Kotlin·Jetpack Compose·Coroutines/Flow·MVVM/UDF·Media3를 사용합니다.

- `app`: 화면 이동, ViewModel 생성, 의존성 조립
- `feature`: discover, community, media, messages, live
- `core`: model, data, artwork, designsystem
- `macrobenchmark`: 성능 계측용 모듈

총 **11개 Gradle 모듈**이며 각 기능의 의존 관계는 [아키텍처 문서](../docs/architecture.md)에 정리했습니다.

## 필요한 환경

- JDK 17 및 Android Studio 또는 Android SDK
- Android SDK Platform 37, Build Tools 36.0.0
- Gradle Wrapper 9.5.0(저장소 포함)
- `minSdk 24`, `targetSdk 36`, `compileSdk 37`

`ANDROID_HOME`을 지정하거나 버전 관리에서 제외한 `local.properties`에 SDK 경로를 설정합니다.

저장소 루트에서 실행:

~~~bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
~~~

Android Studio에서는 `android/` 폴더를 열어 Debug 앱을 설치합니다.

## 시연 경로

| 경로 | 데이터·영상 | 필요한 환경 |
|---|---|---|
| Discover·Community·Messages | 가상 콘텐츠·메모리 기반 데이터 | Debug 앱 |
| 기본 Replay | 앱에 포함된 합성 HLS VOD | Debug 앱 |
| LIVE 상세(지정된 3개 룸) | 로컬 서버의 녹화 MP4 + Fake 채팅 | Debug 앱 + 로컬 미디어 서버 |
| Media의 로컬 영상 | 로컬 서버의 녹화 MP4를 Replay 화면에서 재생 | Debug 앱 + 로컬 미디어 서버 |
| 실제 WebSocket 채팅 | 로컬 Spring Boot·PostgreSQL에서 메시지 송수신·복구 | Debug 앱 + 로컬 채팅 서버·테스트 토큰 |

LIVE 채팅 화면의 개발 설정에서 Fake와 로컬 서버 연결을 선택할 수 있습니다. 서버 설정과 연결 주소는 [로컬 서버 안내](../backend/chat-server/README.md)를 참조하세요.

## 테스트와 검증

Android 계측 테스트에는 합성 HLS·MP4·WebVTT 자산을 사용합니다. 프로젝트의 Android GitHub Actions는 빌드·Lint·단위 테스트·모듈 경계 검사를 실행하고, Compose 화면·플레이어·채팅 UI 계측은 로컬 AVD나 실기기에서 실행했습니다.

- [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47): Media3 설정·전체 화면, 로컬 API 36 계측 **41/41** 통과
- [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53): 채팅 화면·로컬 서버 연동, 로컬 API 36 계측 **28/28** 통과
- [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59): IME와 입력창 배치, 로컬 API 36 관련 계측 **24/24**, 실기기 **3/3** 통과

각 PR에 연결된 단위 테스트와 CI 결과는 [검증 문서](../docs/verification.md)에 모았습니다.

## Hilt 의존성 주입

일반 ViewModel은 Activity 범위에서 공유하며, ChatViewModel은 룸별 Navigation 항목에 속합니다. 테스트용 바인딩 교체와 기기 검증 범위는 [검증 문서](../docs/verification.md)를 참조하세요.
