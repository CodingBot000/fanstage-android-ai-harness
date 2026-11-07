# FanStage

**Kotlin · Jetpack Compose 기반 Android 미디어·실시간 채팅 포트폴리오**

FanStage는 가상의 팬 커뮤니티를 배경으로 만든 Android 앱입니다. Media3 기반 영상 재생과 WebSocket 채팅을 구현하고, **플레이어 수명주기**, **메시지 정합성·장애 복구**, **Compose 아키텍처**에 집중했습니다. 개발에는 Codex를 활용했으며, 구현한 코드를 테스트·리뷰·CI로 검증하는 과정을 함께 정리했습니다.

**기술 스택:** Kotlin · Jetpack Compose · Coroutines/Flow · MVVM/UDF · 멀티모듈(11개) · AndroidX Media3 · WebSocket/HTTP · Spring Boot · PostgreSQL · GitHub Actions · Codex

## 앱 화면과 시연

Galaxy S22 Ultra(Android 16)에서 촬영했습니다. 팬 커뮤니티 데이터와 아티스트 이미지는 가상이며, 영상 시연에서는 공개 표시 권한을 확인한 사전 녹화 MP4를 로컬 서버에서 재생했습니다.

| Discover | Community |
|:--:|:--:|
| ![Discover](docs/evidence/physical-20261108/discover.png) | ![Community](docs/evidence/physical-20261108/community.png) |
| Media | Messages |
| ![Media](docs/evidence/physical-20261108/media.png) | ![Messages](docs/evidence/physical-20261108/messages.png) |

[실기기 화면 탐색 영상 · 13초](docs/portfolio/media/physical-20261108/navigation.mp4) · [실기기 Media3 재생·설정 영상 · 16초](docs/portfolio/media/physical-20261108/playback.mp4)

[LIVE 데모 화면](docs/evidence/physical-20261108/live.png) · [Replay 화면](docs/evidence/physical-20261108/replay.png) · [플레이어 설정](docs/evidence/physical-20261108/player-settings.png)

시연용 LIVE 화면은 **합성 HLS VOD**를 사용합니다. 기본 채팅은 Fake 데이터로 동작하며, 실제 WebSocket 연결과 복구는 별도의 로컬 서버에서 검증했습니다.

## 주요 구현과 문제 해결

### 1. Media3 영상 재생과 Lifecycle 관리

HLS VOD·로컬 MP4 재생, 버퍼링·오류·재시도, 화질·자막·배속 선택과 전체 화면을 구현했습니다. 전체 화면 전환에서는 재생 세션을 유지하고, 화면 이탈 시 Player·Listener·Surface를 정리하도록 수명주기 책임을 분리했습니다. 화면 재진입·회전·재생 상태 전환을 계측 테스트로 확인했습니다.

### 2. 네트워크 장애 상황에서 채팅 정합성 유지

서버에 메시지가 저장된 뒤 ACK가 유실되면 클라이언트는 전송 결과를 확정할 수 없습니다. 이때 **Pending 상태와 clientMessageId**를 유지해 동일 메시지를 안전하게 재전송하도록 구현했습니다. 또한 **serverSequence**로 중복·역순 이벤트를 정리하고, 재연결 시 HTTP snapshot으로 누락 메시지를 보충합니다. 격리된 테스트 서버·TCP 프록시에서 ACK 유실과 지연 응답을 재현했습니다.

### 3. Compose·MVVM 기반 멀티모듈 아키텍처

Discover·Community·Media·Messages·LIVE를 기능별로 분리하고, 공통 모델·데이터·디자인 시스템·이미지 리소스를 core 모듈에 배치해 총 **11개 Gradle 모듈**로 구성했습니다. ViewModel과 Compose 화면의 상태·이벤트 책임을 나누고, 모듈 간 의존 방향을 스크립트로 검증합니다.

### 4. AI 코딩 도구를 활용한 개발·품질 검증

기능별 요구사항과 수용 기준을 정리한 뒤 **Codex 구현 → 코드 리뷰 → 빌드·Lint·테스트 → GitHub Actions 검증** 순서로 작업했습니다. 특히 Media3 수명주기, 채팅 전송 상태, 화면 전환 등 오류가 발생하기 쉬운 경로는 재현 테스트와 회귀 검증을 연결했습니다.

## 테스트 및 검증 결과

| 대상 | 확인한 결과 | 근거 |
|---|---|---|
| Media3·화질/자막·전체 화면 | 단위 56/56, 로컬 API 36 계측 41/41 PASS | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) |
| LIVE 채팅 UX·로컬 서버 연동 | 단위 56/56, 로컬 API 36 계측 28/28 PASS | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 키보드·입력 화면 | 단위 62/62, 로컬 AVD 관련 계측 24/24, 실기기 3/3 PASS | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) |
| 모듈 경계·Compose 구조 | 모듈 의존성 검사 및 Android CI PASS | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) |
| WebSocket·PostgreSQL·장애 주입 | 서버 통합 및 장애 시나리오 CI PASS | [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) |

테스트 수치는 **각 PR에 기록된 소스 버전별 결과**입니다. Android 계측 수치는 로컬 에뮬레이터·실기기에서 측정했으며, GitHub Actions의 자동 빌드·단위 테스트 결과와 구분했습니다. 자세한 범위는 [검증 기록](docs/verification.md)에 있습니다.

## 직접 실행하기

JDK 17, Android SDK Platform 37, Build Tools 36.0.0이 필요합니다.

~~~bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
~~~

Android Studio에서 `android/`를 열어 Debug 앱을 실행할 수 있습니다. 기본 화면·합성 HLS 재생·Fake 채팅은 서버 없이 동작하며, 실제 채팅 서버는 선택적으로 로컬에서 실행합니다.

[Android 빌드·테스트 안내](android/README.md) · [로컬 서버 실행 안내](backend/chat-server/README.md)

## 설계와 검증 문서

[아키텍처](docs/architecture.md) · [AI 개발·검증 과정](docs/ai-development-workflow.md) · [기능별 장애 복구 테스트](docs/specs/failure-recovery.md) · [테스트·검증 결과](docs/verification.md) · [핵심 설계 결정](docs/adr/)

원본 코드·문서는 [MIT 라이선스](LICENSE), 포함된 외부 구성요소는 [제3자 라이선스 안내](THIRD_PARTY_NOTICES.md)를 따릅니다.
