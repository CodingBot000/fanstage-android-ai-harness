# FanStage

**Native Android Media & Realtime Messaging · Engineering Portfolio**

Kotlin·Jetpack Compose 기반 팬 커뮤니티 앱을 구현하고, **Media3 플레이어의 수명주기**, **WebSocket 메시지 정합성·장애 복구**, **멀티모듈 설계**, **AI 코딩 에이전트 결과의 검증**을 실제 코드·테스트·PR로 연결한 프로젝트입니다.

**Tech stack:** Kotlin · Jetpack Compose · Coroutines/Flow · MVVM/UDF · 11 Gradle modules · AndroidX Media3 · WebSocket/HTTP · Spring Boot · PostgreSQL · GitHub Actions · Codex

## 앱 시연

Galaxy S22 Ultra(Android 16)에서 촬영한 화면입니다. 가상 팬 콘텐츠를 사용하며 영상 시연에는 로컬 서버에서 제공한 사전 녹화 MP4가 사용됐습니다.

| Discover | Community |
|:--:|:--:|
| ![Discover](docs/evidence/physical-20261108/discover.png) | ![Community](docs/evidence/physical-20261108/community.png) |
| Media | Messages |
| ![Media](docs/evidence/physical-20261108/media.png) | ![Messages](docs/evidence/physical-20261108/messages.png) |

- [실기기 네비게이션 데모 · 13초](docs/portfolio/media/physical-20261108/navigation.mp4)
- [실기기 Media3 재생·설정 데모 · 16초](docs/portfolio/media/physical-20261108/playback.mp4)
- [LIVE 데모 화면](docs/evidence/physical-20261108/live.png) · [Replay 화면](docs/evidence/physical-20261108/replay.png) · [화질·자막·배속 설정](docs/evidence/physical-20261108/player-settings.png)

## Engineering Highlights

### 1. ACK 유실에도 메시지 중복 저장·표시 방지

서버 저장은 성공했지만 ACK가 유실되면 클라이언트는 결과를 모르는 상태입니다. 이를 즉시 Failed로 확정하지 않고 Pending 상태를 유지하고, 동일 **clientMessageId**로 재전송해 서버에서 중복 저장·방송을 차단했습니다. 격리된 TCP 프록시로 ACK/echo 유실을 주입하고 Android 상태 전이와 PostgreSQL 결과를 확인했습니다.

### 2. WebSocket 재연결과 메시지 순서·누락 복구

룸 단위 **serverSequence**로 중복·역순 메시지를 조정하고, 재연결 시 HTTP snapshot으로 누락 범위를 보충합니다. 과거 룸 또는 지연된 snapshot의 응답이 현재 화면을 덮지 않도록 처리했습니다. Fake transport 단위 테스트와 로컬 WebSocket/HTTP·PostgreSQL 통합 테스트를 구분해 검증했습니다.

### 3. Media3 플레이어 상태와 Android Lifecycle

합성 HLS VOD 및 로컬 Progressive MP4 재생, 버퍼링·오류·재시도, 화질/자막/배속 선택과 전체 화면을 구현했습니다. Player/Listener/Surface의 소유권을 화면 수명과 연결하고, 화면 전환·회전·재진입 테스트를 수행했습니다. **LIVE는 데모 UI이며 실제 방송 스트림 송출을 주장하지 않습니다.**

### 4. 기능 경계가 명확한 Compose 아키텍처

**11개 Gradle 모듈**에서 app의 Navigation·의존성 조립과 feature별 화면, core의 모델·데이터·아트워크·디자인 시스템을 분리했습니다. UDF의 UiState/Action과 ViewModel 취소·이전 응답 경계를 테스트하고, 모듈 의존 방향을 정적 스크립트로 검사합니다.

### 5. AI-assisted Development와 실행형 품질 게이트

**SDD → Codex 구현 → 리뷰 → 빌드/Lint/단위·통합·계측 테스트 → PR·CI 근거**로 개발 결과를 검증했습니다. AI 지침만으로 품질을 보증한다고 주장하지 않고, 실패 재현·테스트 및 코드 리뷰를 실제 완료 판단의 근거로 사용했습니다.

## 재현 가능한 검증 근거

| 검증 범위 | 기록된 결과와 연결 |
|---|---|
| Media3 플레이어 | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · 해당 소스에서 단위 56/56, 로컬 API 36 계측 41/41 PASS · [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) |
| LIVE 채팅 UX 및 통합 | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) · 단위 56/56, 로컬 API 36 계측 28/28 PASS |
| 키보드·입력 UX | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) · 단위 62/62, 로컬 API 36 관련 계측 24/24, Galaxy 실기기 3/3 PASS |
| 멀티모듈 | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) · feature/core 경계 및 CI 검증 |
| 로컬 서버·장애 복구 | [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) · [시나리오 및 테스트 근거](docs/specs/failure-recovery.md) |

수치는 **각 PR에 기록된 코드·환경의 결과**입니다. GitHub Actions는 Android 빌드·정적 검사·단위 테스트와 서버 통합을 수행하며, 위 Android 기기 계측 수치는 로컬 에뮬레이터·실기기 검증 결과입니다.

## 실행하기

JDK 17, Android SDK Platform 37 및 Build Tools 36.0.0을 준비한 뒤:

~~~bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
~~~

Android Studio에서 **android/**를 열고 Debug 앱을 설치합니다. 기본 모드는 외부 서버 없이 합성 HLS 영상과 Fake 채팅을 재현합니다. 실제 로컬 WebSocket/HTTP·PostgreSQL은 별도로 실행할 수 있습니다.

- [Android 빌드·테스트 안내](android/README.md)
- [로컬 서버 실행](backend/chat-server/README.md)

## 기술 문서

[아키텍처](docs/architecture.md) · [검증 증거](docs/verification.md) · [AI 개발·검증 워크플로](docs/ai-development-workflow.md) · [주요 설계 결정](docs/adr/) · [핵심 기능 계약](docs/specs/) · [성능 계측 방법](docs/performance.md)

**범위:** 공개 소스 중심의 엔지니어링 데모입니다. 앱 기본 데이터·채팅은 합성/Fake이며 실제 서버 연결은 로컬 개발 환경에서 검증했습니다. 운영 규모 트래픽, 실제 원격 LIVE 송출, 상용 인증·TLS 서비스의 운영 경험을 이 프로젝트 성과로 주장하지 않습니다.

직접 작성한 코드·문서는 [MIT](LICENSE) 라이선스를 따르며, 포함된 제3자 구성요소와 자산의 권리는 [Third-party notices](THIRD_PARTY_NOTICES.md)를 따릅니다.
