# FanStage

**Native Android Media & Realtime Messaging — Engineering Portfolio**

FanStage는 가상의 팬 커뮤니티를 배경으로 **미디어 재생, 실시간 메시징, Android 앱 아키텍처, AI 보조 개발의 품질 검증**을 다루는 Kotlin 기반 Android 프로젝트입니다. 기능을 나열하는 데 그치지 않고, 네트워크 장애와 비동기 상태 경합을 재현하고 설계·테스트·CI 증거로 해결 과정을 설명하는 데 초점을 맞췄습니다.

**핵심 기술:** Kotlin · Jetpack Compose · Coroutines / Flow · MVVM / UDF · Multi-module · AndroidX Media3 · WebSocket / HTTP · Spring Boot · PostgreSQL · GitHub Actions · Codex

> **프로젝트 범위:** 학습·검증용 엔지니어링 포트폴리오(v0.1, 소스 공개)입니다. 앱 화면의 LIVE는 데모 UI입니다. 기본 합성 HLS와 Debug에서 로컬 서버의 사전 녹화 MP4를 재생할 수 있습니다. 기본 채팅은 Fake 데이터로 동작하고, 실제 WebSocket/HTTP 통합은 별도의 로컬 개발 서버로 검증했습니다. 상용 방송 서비스나 운영 트래픽을 제공·재현한 프로젝트는 아닙니다.

## 앱 미리보기

아래 화면과 영상은 Galaxy S22 Ultra(Android 16) 실기기에서 촬영했습니다. 화면 데이터는 가상이며, 재생 시연은 로컬 서버의 사전 녹화 MP4를 사용합니다. 실제 생방송 장면은 아닙니다.

| Discover | Community |
|:---:|:---:|
| ![Discover: 가상 아티스트와 콘텐츠 탐색](docs/evidence/physical-20261108/discover.png) | ![Community: 아티스트 커뮤니티와 피드](docs/evidence/physical-20261108/community.png) |
| Media | Messages |
| ![Media: 영상 목록과 필터](docs/evidence/physical-20261108/media.png) | ![Messages: 로컬 대화 목록](docs/evidence/physical-20261108/messages.png) |

[![실기기 UI 탐색 시연 미리보기](docs/evidence/physical-20261108/discover.png)](docs/portfolio/media/physical-20261108/navigation.mp4)

**[실기기 UI 탐색 시연 (약 13초, 무음)](docs/portfolio/media/physical-20261108/navigation.mp4)** · [LIVE 데모 화면](docs/evidence/physical-20261108/live.png) · [Replay 화면](docs/evidence/physical-20261108/replay.png) · [대화 화면](docs/evidence/physical-20261108/conversation.png)

[![실기기 Media3 재생 시연 미리보기](docs/evidence/physical-20261108/player-settings.png)](docs/portfolio/media/physical-20261108/playback.mp4)

**[실기기 영상 재생·설정 시연 (약 16초, 무음)](docs/portfolio/media/physical-20261108/playback.mp4)** — USB로 연결된 로컬 서버의 녹화 MP4와 데모 채팅, 플레이어 설정을 보여줍니다. 실제 서버 채팅이나 네트워크 장애 주입 장면은 포함하지 않습니다. 사용자 확인에 따라 영상 화면의 공개 사용 권한이 있는 자료를 촬영했으며, 원본 MP4는 저장소에 포함하지 않습니다.

## Engineering Highlights

### 1. 불확실한 메시지 전송 결과와 중복 방지

네트워크가 불안정할 때 **서버는 저장했지만 클라이언트는 ACK를 받지 못하는 상황**을 별도로 다뤘습니다. ACK 타임아웃을 곧바로 전송 실패로 확정하지 않고 Pending 상태를 유지하며, 동일한 `clientMessageId`로 재전송했을 때 서버가 중복 저장·방송하지 않도록 멱등 처리합니다.

로컬 TCP 프록시에서 ACK와 발신자 echo를 유실시키고, Android 상태 전이·서버 저장 결과·다른 구독자의 이벤트를 확인했습니다. 이는 네트워크 전 구간의 *exactly-once delivery*를 보장한다는 뜻이 아닙니다.

### 2. 재연결 시 메시지 순서·누락·오래된 응답 처리

WebSocket 이벤트는 순서가 뒤바뀌거나, 재연결 중 HTTP Snapshot과 겹칠 수 있습니다. 룸 단위 `serverSequence`를 기준으로 중복 제거·누락 탐지·보충을 처리하고, 이전 룸이나 과거 Snapshot의 늦은 응답이 최신 화면 상태를 덮어쓰지 않도록 했습니다.

단위 테스트에서 지연·중복·역순을 결정적으로 재현하고, 별도 로컬 서버·프록시·Android 에뮬레이터 통합 테스트로 일부 경로를 검증했습니다. 물리 네트워크 전환과 운영 규모의 부하는 검증 범위에 포함되지 않습니다.

**근거:** [장애 주입 시나리오와 PASS/부분 PASS/NOT_RUN](docs/specs/M5-fault-recovery.md) · [아키텍처 및 검증 증거](docs/portfolio/architecture-and-evidence.md)

### 3. Media3 플레이어의 상태와 Lifecycle 소유권

AndroidX Media3 기반 HLS VOD 재생, 버퍼링·오류·재시도, 실제 트랙 기준 화질/자막 선택, 배속, 전체 화면을 구성했습니다. 화면과 플레이어 상태의 책임을 구분하고, 전체 화면 전환에서 같은 플레이어를 사용하며 화면 이탈 시 Player·Listener 해제를 검사했습니다.

재생과 채팅은 독립된 경로로 동작하도록 설계했습니다. 화면 재진입·회전·백그라운드 복귀 등 제한된 테스트는 수행했지만, 장시간 메모리 누수가 없음을 입증한 것은 아닙니다.

**근거:** [플레이어 요구사항·검증](docs/specs/PLAYER-001-player-settings-fullscreen.md) · [플레이어 설정 화면](docs/evidence/physical-20261108/player-settings.png) · [플레이어 PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)

### 4. 기능 경계를 분리한 Compose 아키텍처

Discover·Community·Media·Messages·LIVE 화면을 Compose로 구현하고, 기능 모듈과 공통 모델·데이터·아트워크·디자인 시스템을 분리했습니다. 현재 Android 프로젝트는 `app`, 5개 `feature`, 4개 `core`, `macrobenchmark`의 **11개 모듈**로 구성됩니다.

`:app`은 Navigation과 AppContainer를 담당하고, 공통 도메인 모델은 기능 모듈에서 재사용합니다. 모듈화 자체를 성과로 간주하기보다, 기능 간 의존 방향과 `feature → app` 역참조 방지 규칙을 검사하도록 구성했습니다.

**근거:** [모듈 구조·의존 방향](docs/specs/ARCH-001-feature-modularization.md) · [아키텍처 그림](docs/portfolio/architecture-and-evidence.md) · [화면·모듈 개편 PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38)

### 5. 실기기 측정과 개선안 철회 결정

실기기(SM-G975N, Android 12)에서 성능 가설 두 가지를 비교했습니다. 수신 이벤트의 ViewModel 수용 구간이 단축된 경우도 있었지만 대조군 프레임 회귀 가능성이 나타났고, 다른 변경에서는 앱 시작 지표가 악화됐습니다. **두 변경 모두 되돌렸으며, 제품 성능 향상을 달성했다고 주장하지 않습니다.**

측정 조건, 각 10회 반복, 비교 수치와 원본 증거의 관리 방식을 기록했습니다. 최적화는 코드 변경 자체가 아니라 비교 가능한 측정과 회귀 판단으로 평가했습니다.

**근거:** [실기기 성능 A/B 실험 및 한계](docs/reviews/M6-002-ab-experiments.md)

## AI-assisted Development & Quality Harness

이 저장소에서 AI 코딩 에이전트는 구현 도구이며, **요구사항의 범위 설정과 검증 책임은 개발 프로세스에 명시적으로 남깁니다.**

```text
SDD (요구사항·계약·수용 기준)
   ↓
Codex 기반 구현 / 리팩토링
   ↓
빌드 · Lint · Unit / Integration / UI 테스트
   ↓
코드 리뷰 · GitHub Actions 검증
   ↓
실기기 검증·장애 재현 (해당 작업에서 가능한 범위)
   ↓
PASS / FAIL / NOT_RUN 증거 기록 → 변경 수용 또는 재작업
```

- **SDD / ADR:** 기능 범위, 정상·장애 시나리오, 아키텍처 변경의 이유와 대안을 문서화합니다.
- **실행형 검증:** `AGENTS.md`와 Skills는 에이전트 행동 지침이며 품질을 보증하는 장치가 아닙니다. 실제 테스트·Lint·CI 결과를 근거로 판단합니다.
- **결과 추적:** 검증 대상 커밋, 실행 환경, 실패와 미실행 사유를 구분해 기록합니다. 자동 리뷰 결과는 테스트나 사람의 최종 판단을 대체하지 않습니다.
- **실제 적용:** UI/모듈 변경 [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38), 플레이어 [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47), 채팅 UX·연동 [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)에 변경·검증 기록을 연결했습니다.

상세한 설계·검증 자료는 [개발 워크플로 ADR](docs/adr/ADR-0001-development-workflow.md), [테스트 매트릭스](docs/test-matrix.md), [실행 기록](harness/build-log.md)을 참고하세요.

## 검증 증거와 범위

| 검증 영역 | 확인한 내용 | 근거 |
|---|---|---|
| Android 빌드·단위·계측 | PLAYER-001 PR 기준 로컬 단위 56/56, API 36 전용 AVD 계측 41/41 PASS | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) |
| WebSocket/DB 통합 | PostgreSQL, ACK 유실·재전송, 지연된 Snapshot과 재연결의 테스트 경로 | [장애 주입 결과](docs/specs/M5-fault-recovery.md) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) |
| 최근 채팅 UX 변경 | PR #53 기준 Debug 빌드·Lint·단위 56/56, 로컬 API 36 AVD 계측 28/28 PASS | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 성능 실험 | 동일 기기의 반복 측정에서 개선안 회귀 가능성·시작 지표 악화를 관찰하고 철회 | [A/B 측정 보고서](docs/reviews/M6-002-ab-experiments.md) |

위 숫자는 **각 PR·측정 시점과 환경에서 수행한 범위**입니다. 일부 GitHub Actions 실행에서는 원격 에뮬레이터 또는 서버 측 Android 계측 단계가 **SKIPPED**였으며, 이를 전체 계측 테스트 통과로 해석하지 않습니다.

## 로컬 실행

**필요 환경:** JDK 17, Android Studio 또는 Android SDK (platform 37, build-tools 36.0.0). Android SDK 경로는 로컬 환경에 맞게 설정합니다.

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"  # macOS 예시
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
```

Android Studio에서 `android/` 디렉터리를 열고 `app` Debug 구성을 에뮬레이터 또는 기기에서 실행합니다. 기본 모드는 **Offline fake**이며 외부 서버 없이 UI 탐색, 앱에 포함된 합성 HLS VOD 재생과 Fake 채팅을 체험할 수 있습니다. Debug 빌드에는 개발용 서명이 적용됩니다. **공개 APK는 제공하지 않습니다.**

실제 로컬 WebSocket/HTTP·PostgreSQL 연동과 선택적 MP4 VOD 스트리밍에는 추가 서버 설정이 필요합니다. MP4 파일은 저장소에 포함되지 않으며 사전 녹화 Progressive VOD로, 실제 LIVE HLS가 아닙니다.

자세한 명령과 환경 설정: [Android 실행 안내](android/README.md) · [로컬 채팅/미디어 서버 안내](backend/chat-server/README.md)

## 적용 범위와 남은 한계

- 원격 실시간 LIVE 방송 및 상용 인증/TLS·대규모 동시접속 운영은 구현·검증 범위가 아닙니다.
- 실기기의 물리 네트워크 전환, 실제 저메모리 종료(LMK), 장시간 heap/누수 검사, 운영 환경의 스트리밍 지연은 충분히 검증하지 않았습니다.
- PiP는 v0.1에 포함되지 않았습니다. 별도 로컬 실험에서 빠른 강제 스택 제거 후 Player 해제 결함이 남아 확장을 보류했습니다.
- 성능 A/B 변경은 되돌렸습니다. 원본 실기기 trace는 공개하지 않으며, 환경·집계값과 manifest 해시를 기록했습니다.
- 공개 소스와 시연 자산의 제3자 라이선스·권리 검토에는 남은 확인 항목이 있습니다. 자세한 내용은 [릴리스 검증 범위](docs/reviews/M8-final-readiness.md)와 [라이선스 감사](docs/reviews/M8-license-audit.md)를 참조하세요.

## 문서

[아키텍처·검증 근거](docs/portfolio/architecture-and-evidence.md) · [시연과 장애 테스트](docs/portfolio/demo-and-faults.md) · [현재 개발 상태](docs/STATUS.md) · [기능별 SDD](docs/specs/) · [설계 결정 ADR](docs/adr/)

## 라이선스

직접 작성한 코드·문서는 [MIT License](LICENSE)를 따릅니다. Gradle Wrapper와 기타 제3자 구성요소는 별도 조건이 적용됩니다. [제3자 고지](THIRD_PARTY_NOTICES.md) 및 [라이선스 감사](docs/reviews/M8-license-audit.md)를 참고하세요.
