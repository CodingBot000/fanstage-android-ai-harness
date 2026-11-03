# FanStage — Android AI Harness & Portfolio Master Plan

> **공개판:** v0.1-public (2026-11-04)
> **공개 원칙:** 개인 경력과 특정 기업의 실명 없이 재현 가능한 Android 기술 검증 계획만 기록한다.
> **현재 상태:** [STATUS.md](STATUS.md)를 기준으로 판단한다.

---

## 0. 새 ChatGPT 프로젝트에서 가장 먼저 읽을 내용

### 0.1 지금 해야 할 일

**첫 번째 구현 단계는 Android 프로젝트 생성이 아니라 `M0. AI Harness Core 구축`이다.**

- 계획 작성 당시 상태: `[ ] M0 시작 전`. 실제 실행·승인 상태는 `docs/STATUS.md`를 따른다.
- `M0`에서는 **Repository, SDD, AGENTS.md, CLAUDE.md, 검증 정책, Phase 관리 구조만** 준비한다.
- Android Studio 프로젝트 생성, Gradle 의존성 추가, LIVE/채팅 구현은 `M1` 이후에 한다.
- 마스터 계획은 구현이 진행되면 실제 결정·검증 결과에 맞게 갱신한다. 계획상의 기능을 이미 구현했다고 간주하지 않는다.
- **한 번의 Codex 작업 요청 = 하나의 승인된 작업 단위**를 원칙으로 한다. 에이전트가 전체 로드맵을 임의로 한 번에 구현하지 못하게 한다.

### 0.2 새로운 채팅에서 이 문서를 활용하는 방법

1. 이 파일을 새 ChatGPT 프로젝트의 지식 파일로 추가하고, 실제 Git Repository에도 `docs/MASTER_PLAN.md`로 복사한다.
2. 새 채팅 첫 메시지에 다음과 같이 요청한다.
   > `첨부한 FanStage 마스터 계획을 기준으로 진행하자. 현재 M0 시작 전이다. M0의 목표·산출물·검증 기준을 확인하고, Codex에게 전달할 첫 작업 지시문을 작성해 줘. M1 이후 구현은 시작하지 말아 줘.`
3. ChatGPT에서는 범위와 설계를 검토하고 Codex 지시문을 확정한다.
4. Codex 실행 결과(요약, 변경 파일, 테스트/검증 로그, 실패 원인)를 채팅에 다시 전달해 검토받는다.
5. 승인된 결과만 단계 상태에 반영한다. 다음 단계는 승인 후 진행한다.
6. 대화가 길어지거나 새 채팅으로 이동해도 `docs/STATUS.md`와 `harness/build-log.md`를 기준으로 재개한다.

### 0.3 문서 간 진실의 원천(Source of Truth)

| 내용 | 기준 파일 | 설명 |
|---|---|---|
| 프로젝트 목표·범위·로드맵·품질 조건 | `docs/MASTER_PLAN.md` | 지금 작성한 문서를 복사. 변경 시 수정 이유 기록 |
| 현재 단계·완료 체크·막힌 이슈·다음 행동 | `docs/STATUS.md` | 세션 종료마다 갱신. 현재 실행 위치를 판단하는 기준 |
| 단계별 상세 SDD·완료 조건 | `docs/specs/Mx-*.md` | 해당 단계를 시작하기 전 작성·승인 |
| 기술·아키텍처 결정 | `docs/adr/ADR-*.md` | 선택 이유·대안·트레이드오프 포함 |
| 수행 명령·테스트 로그·증거 | `harness/build-log.md` | 실행 사실과 추정 구분; 증거 경로·커밋 SHA 연결 |
| 에이전트 상시 규칙 | `AGENTS.md` | 짧고 명확한 핵심 불변식·참조 문서 |
| Claude Code 지침 진입점 | `CLAUDE.md` | `@AGENTS.md`를 import해 규칙 중복 방지 |

**상충할 때:** 목표·범위는 `MASTER_PLAN`, 실행 상태는 `STATUS`, 변경된 기술 결정은 가장 최근 승인된 ADR을 따른다. 문서가 충돌하면 임의로 추론하지 말고 차이를 보고하고 수정안을 제안한다.

---

## 1. 프로젝트의 배경과 목표

### 1.1 기술 초점: Android 엔지니어링

**업무와 연결되는 핵심 축**

- LIVE 동영상 등 미디어 서비스 개발
- 채팅·DM 서비스 개발
- 팬 커뮤니티·콘텐츠·커머스 연계
- 글로벌 환경에서 네트워크·성능·언어 최적화
- **Claude Code / Cursor / Copilot 등 AI 개발 도구로 설계·구현·테스트·리팩토링하고, 생성 코드 품질을 검증할 역량**

**핵심 기술 검증 영역**

- Kotlin 중심 구현과 필요한 범위의 Java 상호운용성
- Compose, Coroutine
- MVVM, Clean Architecture, Multi Module
- 코드 리뷰 및 테스트·품질 검증

**추가 검증 영역**

- 실시간 채팅, 미디어, 글로벌 서비스 성능, 테스트 자동화
- 결제·구독(Google Play Billing)은 v0.1 핵심 범위 밖의 선택 확장

### 1.2 구현 결과와 검증 증거

구현된 기능, 테스트, 장애 재현, 성능 측정 결과를 연결한다. 개인의 경력이나 특정 기업의 요구사항은 공개 프로젝트의 검증 근거로 사용하지 않는다.

### 1.3 포트폴리오 핵심 메시지

> **AI 코딩 에이전트로 구현 속도를 높이면서도 Android LIVE·실시간 채팅 서비스의 아키텍처, 네트워크 복구, 상태 정합성, 성능, 테스트·배포 품질을 개발자가 검증하고 책임진다.**

목표는 **실제로 재현 가능한 기술적 문제를 해결하고 증거를 제시하는 Android Native 프로젝트**이다.

### 1.4 가칭·브랜딩

- 프로젝트명: `FanStage` (가칭, 변경 가능)
- 공개 설명: `FanStage — Native Android LIVE & Realtime Fan Experience`
- 실제 아티스트의 영상·사진·음원·개인정보는 허가 없이 사용하지 않는다.
- 테스트 콘텐츠는 직접 제작·라이선스 허용 샘플·합성 계정을 사용한다.

---

## 2. 확정 범위, 선택 확장, 명시적 제외

### 2.1 v0.1 필수 사용자 흐름 (Vertical Slice)

1. 사용자가 가상의 팬 커뮤니티 / LIVE 룸 목록을 확인한다.
2. LIVE 룸에 진입하면 Android Native Media3 기반 HLS 스트림이 재생된다.
3. LIVE 화면에서 WebSocket 채팅 메시지를 실시간으로 수신하고 전송한다.
4. 네트워크가 일시 중단되어도 **전송 상태·메시지 순서·중복·누락 처리**를 통해 현재 화면이 복구된다.
5. 백그라운드/포그라운드 복귀, 화면 전환·회전 등에서 재생/채팅 리소스를 정책대로 유지·해제한다.
6. 주요 경로를 Unit/Integration/UI/장애 주입 테스트로 검증한다.
7. 실기기에서 성능 측정을 수행하고, 문제→진단→변경→재측정 근거를 남긴다.
8. 위 흐름이 SDD, 에이전트 작업 로그, CI 결과로 추적된다.

### 2.2 필수 구현 범위

| 코드 | 기능 | 완료 판단의 핵심 |
|---|---|---|
| F-01 | Compose LIVE 룸 목록·상세 | 상태·로딩·오류 분기, 화면 이동 정상 |
| F-02 | Media3 HLS 재생 | 실제 재생·버퍼링·오류·해제 확인 |
| F-03 | 채팅 표시·전송 | 단순 수신 외 ACK·전송 상태 제공 |
| F-04 | 네트워크 재연결·메시지 정합성 | 순서 역전·중복·누락·느린 응답 테스트 |
| F-05 | 앱 생명주기 복구 | 화면 이탈·복귀·회전·프로세스 재생성 구분 |
| F-06 | 성능 계측과 리포트 | 동일 조건의 전후 데이터 + Raw 결과 |
| F-07 | AI Harness/CI | Spec·검증·리뷰·실행 결과 증명 |
| F-08 | 최소 다국어 | 한국어·영어 리소스와 날짜·시간 표시 검증 |

### 2.3 권장 확장(v0.2, v0.1을 훼손하지 않는 경우에만)

- 이미지·동영상 혼합 팬 커뮤니티 피드, 댓글·좋아요, 페이징·가상화, 이미지 Prefetch
- 타임라인 멀티미디어 로딩 최적화
- 다양한 네트워크 지역·대역폭 프로필
- 네트워크 최적화 실험 보고서
- 별도의 DM 기능(룸 채팅과 다른 1:1 메시징 계약)
- Google Play Billing 구독 + 서버 검증 (실제 Google Play 테스트 환경 확보 후)
- 접근성, 다크 모드, 화면 크기별 레이아웃

### 2.4 현재 제외(Non-goals)

- 실제 아티스트 콘텐츠 수집 및 스크래핑
- 실서비스용 대규모 영상 송출 CDN/트랜스코딩 플랫폼 운영
- WebRTC 다자간 영상 통화나 자체 초저지연 스트리밍 프로토콜 개발
- Kubernetes, Kafka, MSA 등 포트폴리오 목적과 직접 연결되지 않는 과잉 인프라
- 24시간 상시 과금 서버 운영
- AI 모델 학습·RAG·LangGraph 기능을 앱 안에 억지로 추가
- v0.1에서 실제 유료 결제

**설계 의도:** 채용 대상은 Android 엔지니어다. AI를 앱 서비스 기능으로 추가하는 것보다, **AI 코딩 에이전트를 사용하는 개발·검증 프로세스**가 핵심이다.

---

## 3. 설계 원칙과 잠정 기술 스택

### 3.1 Architecture

- Kotlin 중심의 Single-Activity, Jetpack Compose UI.
- UI → ViewModel → Domain/Repository Contract → Data Source의 책임 분리.
- UI는 `UiState`를 표시하고 사용자 의도는 `UiAction` 또는 ViewModel 메서드로 전달한다. 단방향 데이터 흐름(UDF).
- ViewModel에는 Android UI 객체/ExoPlayer 인스턴스/구체 WebSocket 구현을 직접 넣지 않는다.
- `StateFlow`/`Flow`와 `collectAsStateWithLifecycle`을 사용한다.
- UI 이벤트, 단발성 내비게이션, 영속 데이터 상태를 혼동하지 않는다.
- 레이어별 책임이 실제로 나뉠 때만 UseCase를 두며, 빈 껍데기/반복 전달용 추상화를 남발하지 않는다.
- 테스트 가능하도록 Repository 인터페이스와 Fake Data Source를 제공한다.
- 기능이 적더라도 Multi Module을 실제 의존성 검증에 사용한다. 무분별하게 모듈을 쪼개지 않는다.

### 3.2 권장 기술(버전은 M0/M1에서 공식 호환성 확인 후 고정)

| 영역 | 기술 | 주의 |
|---|---|---|
| 앱 | Kotlin, Jetpack Compose, Material 3 | 공식 BOM 및 컴파일러 호환 조합 확인 |
| 비동기 | Kotlin Coroutine, Flow | cancellation·structured concurrency 준수 |
| UI/상태 | AndroidX Lifecycle, ViewModel, Navigation | 사용 버전의 안정성 확인 |
| DI | Hilt | 사용 이유·생성 비용 검토 |
| Network | Retrofit/OkHttp, OkHttp WebSocket | HTTP/WS 정책·timeout 분리 |
| 미디어 | AndroidX Media3 ExoPlayer + HLS | 실제 HLS LIVE와 VOD fixture를 구분 |
| 저장 | Room, DataStore (필요 범위만) | DB migration 테스트 필수 |
| 로그 | Android logcat/Trace + 개인정보 제거된 디버그 이벤트 | 민감 데이터 출력 금지 |
| 단위 테스트 | JUnit, Kotlin Coroutines Test, Turbine 등 | 도입 시 버전·호환성 검토 |
| 네트워크 테스트 | MockWebServer / Fake Repository | 결정적 시퀀스·오류 주입 |
| UI 테스트 | Compose UI Test | 안정적 테스트 태그와 상태 주입 |
| 성능 | Macrobenchmark, Baseline Profile, Perfetto | 실기기와 Release-like 빌드 필수 |
| Backend(최소) | Spring Boot + WebSocket + PostgreSQL | 로컬 Docker Compose로 가동 |
| CI | GitHub Actions | PR의 빠른 품질 게이트 위주 |
| Coding Agent | Codex / Claude Code | 실제 사용 모델·CLI 버전 기록 |

**버전 선정 정책:** 문서 작성 시점의 버전 번호를 추측해 고정하지 않는다. M0/M1에서 Android Gradle Plugin, Gradle, Kotlin, Compose Compiler/BOM, JDK, Media3, Hilt의 **공식 호환성·릴리스 상태**를 확인하고 Version Catalog에 고정하며 근거 URL을 기록한다.

### 3.3 목표 Repository 구조(계획, 아직 생성되지 않음)

```text
fanstage/
├─ README.md
├─ AGENTS.md                     # Codex 등 공통 가이드
├─ CLAUDE.md                     # @AGENTS.md 참조
├─ .gitignore
├─ .github/workflows/           # PR 품질 게이트
├─ .agents/skills/              # Codex repo-local skills (필요한 것만)
├─ .claude/skills/              # Claude repo-local skills (필요한 것만)
├─ docs/
│  ├─ MASTER_PLAN.md            # 이 문서
│  ├─ STATUS.md                 # 현재 단계·다음 행동
│  ├─ specs/                    # 각 단계 SDD
│  ├─ adr/                      # 아키텍처 결정 기록
│  ├─ test-matrix.md
│  ├─ performance/              # 원시 결과·측정조건·요약
│  └─ portfolio/                # 이력서/포트폴리오 자료
├─ harness/
│  ├─ build/                    # 단계별 에이전트 작업 범위
│  ├─ build-log.md
│  ├─ reviews/
│  └─ fixtures/                 # 합성 데이터·장애 시나리오
├─ scripts/                     # 로컬 검증 명령(필요 시)
├─ android/                     # M1부터 생성
│  ├─ app/
│  ├─ core/model/
│  ├─ core/designsystem/
│  ├─ core/network/
│  ├─ core/database/            # 실제 필요해지면 추가
│  ├─ core/testing/
│  ├─ feature/live/
│  ├─ feature/chat/
│  └─ macrobenchmark/
└─ backend/                     # M4부터, 최소 기능만
   └─ chat-server/
```

**모듈 정책:** 위 구조는 목표안이지 생성 명령이 아니다. M1 시점에 최소 동작 가능한 모듈부터 시작하고, 사용하지 않는 `core/database` 같은 모듈은 미리 만들 필요가 없다. 경로를 정하면 그 구조에 맞춰 문서를 갱신한다.

### 3.4 잠정 데이터 계약

채팅의 핵심은 **서버의 영속 순서와 클라이언트의 중복 없는 표시**이다.

- `roomId`: LIVE 룸 식별자
- `clientMessageId`: 클라이언트가 재전송 시에도 유지하는 메시지 ID(멱등 키)
- `messageId`: 서버 확정 메시지 ID
- `serverSequence`: 룸 내 단조 증가하는 영속 순번(총순서가 필요한 범위 명시)
- `sentAt`: 서버 확정 시각(표시 보조용, 순서 보장 수단 아님)
- `status`: 클라이언트 전송 상태(`PENDING`, `SENT`, `FAILED` 등)
- `connectionState`: `CONNECTING`, `CONNECTED`, `RECONNECTING`, `DISCONNECTED`

원칙:

1. `clientMessageId`로 동일 사용자의 재전송 중복 반영을 방지한다. 서버는 원자적으로 검사해야 한다.
2. ACK를 못 받았다고 메시지가 실패한 것으로 단정하지 않는다. 상태 조회나 같은 멱등 키 재전송으로 확인한다.
3. 클라이언트는 `serverSequence`로 누락 범위를 탐지하고 Snapshot/Gap Fetch로 보충한다.
4. 이벤트가 역순 도착해도 표시가 뒤섞이지 않게 한다.
5. 진입한 룸이 변경되거나 앱 세션이 바뀌면 이전 룸의 늦은 응답을 현재 UI에 적용하지 않는다.
6. '화면에서 중복 없이 표시'와 네트워크 전 구간 'Exactly-once delivery'는 별개다. 후자는 주장하지 않는다.
7. 대량 메시지에서도 무제한 메모리 적재·화면 전체 재합성을 피한다. 보관/화면 표시 버퍼의 한도를 결정하고 근거를 남긴다.

### 3.5 LIVE 동영상의 정확한 정의

- 정적 `.m3u8` 기반 VOD가 재생되는 것만으로 **LIVE 스트리밍 구현 완료**라고 쓰지 않는다.
- 구현 초기에는 재현 가능한 HLS VOD fixture로 플레이어를 검증해도 된다.
- LIVE 검증 단계에서는 시간이 지나면서 새 세그먼트가 추가되는 **라이브 HLS playlist** 또는 사용 허가된 실제 라이브 스트림을 사용한다.
- 영상 전송 지연과 WebSocket 채팅 도착 시간은 별도 경로이다. 정밀 동기화가 없으므로 동기화 보장이라고 주장하지 않는다.
- 백그라운드 오디오·PIP·화면 밖 재생 정책은 M2 SDD에서 명시적으로 결정한다. 자동 재생 유지가 기본값은 아니다.

### 3.6 네트워크와 운영 비용 원칙

- 기본 개발은 로컬 Mac + Android Emulator + Android 실기기에서 가능해야 한다.
- 로컬 Docker Compose로 채팅 서버·DB를 띄우고 테스트 후 종료한다.
- 영상 fixture는 라이선스가 적법한 샘플을 사용한다.
- 외부 공개 서버/상시 과금 리소스가 필요한 설계는 별도 검토·승인 전 사용하지 않는다.
- Android Emulator와 실제 단말의 `localhost` 연결 방식이 다르므로 LAN IP/에뮬레이터 브리지 설정을 문서화한다.
- 공개 배포를 한다면 HTTPS/WSS, 인증·Rate Limit·악용 방지·배포 비용을 별도 검토한다.

---

## 4. AI Harness Core: 가장 먼저 구축할 것 (M0)

### 4.1 Harness 역할

Harness는 프롬프트만 모아놓은 폴더가 아니다. 다음의 **제어와 증거**를 묶는 개발 체계다.

- 입력: 승인된 SDD, 기술 제약, 수용 기준, 금지 범위
- 실행: Codex/Claude가 목표 기능을 구현
- 검증: 정적 분석, 빌드, 테스트, 회귀, 실기기 측정(구현 단계에 따라 차등 적용)
- 리뷰: 패치의 아키텍처·동시성·누수·보안·성능 영향 확인
- 출력: 변경 파일, 실행한 검증 명령과 결과, 실패, 미실행 이유, 다음 행동
- 기록: 변경과 테스트 증거를 특정 커밋·환경·시나리오에 연결

**중요한 구분:** `AGENTS.md`/Skills는 에이전트 행동을 유도하는 지침이며, 검증 강제 장치가 아니다. 반드시 Gradle·테스트 스크립트·CI의 실제 실패 조건을 통해 품질 게이트를 만든다.

### 4.2 M0에서 만들 파일(권장 최소 집합)

- `README.md` — 프로젝트 개요·상태·목표·최초 실행 방법(초기에는 Harness 사용법)
- `AGENTS.md` — 공통 핵심 지침(간결하게)
- `CLAUDE.md` — 첫 줄 `@AGENTS.md`를 두어 동일 지침 로딩, Claude 전용 사항만 추가
- `docs/MASTER_PLAN.md` — 이 문서
- `docs/STATUS.md` — 상태·승인·다음 작업
- `docs/specs/M0-harness-core.md` — M0 작업 자체의 SDD
- `docs/adr/ADR-0001-development-workflow.md` — SDD/검증·문서 관리 선택 근거
- `docs/test-matrix.md` — 테스트 계층·CI/실기기 분리 기준
- `harness/build/M0-harness-core.md` — Codex 작업 범위·절차·출력 계약
- `harness/build-log.md` — 변경/실행/검증/증거 기록
- `.gitignore` — 키·로컬 파일·빌드 출력 제외
- 필요한 경우에만 `harness/reviews/README.md` 또는 템플릿

**M0에서는 아직 자동으로 실행할 Android Gradle 프로젝트가 없으므로** Gradle 테스트/Android CI가 실제 동작한다고 보고해서는 안 된다. M1에서 실행 가능한 게이트를 연결한다.

### 4.3 `AGENTS.md`에 포함할 불변 규칙

- 작업을 시작할 때 `docs/STATUS.md`의 현재 단계를 확인하고 해당 작업의 SDD만 읽는다. 모든 문서를 매번 읽도록 강제하지 않는다.
- **명시적으로 승인된 현재 단계/작업만 변경**, 다음 단계 기능 선행 구현 금지.
- 아키텍처 경계, 외부 라이브러리 추가, 네트워크/저장 계약 변경 전에는 영향을 분석하고 승인을 요청하거나 ADR 초안을 작성.
- 성공을 주장하기 전 실제 실행한 검증 명령·결과를 기록. 실행하지 못한 테스트는 `NOT_RUN`.
- API Key, 서명 키, 개인정보, 실사용 사용자 메시지/영상 등은 Git/로그/스크린샷에 포함하지 않는다.
- 새 버그를 발견하면 재현 조건→실패 테스트→최소 수정→회귀 검증 순서(가능한 범위에서).
- 불명확한 요구사항은 임의 확장 대신 SDD의 `Open Questions`에 남긴다.
- 품질 게이트를 우회하는 테스트 비활성화·Lint suppression·`try/catch` 삼키기는 근거 없이 금지.
- 유사 코드/과한 추상화·모듈/의존성 확산을 제한하고, 변경한 기능과 관련된 검증을 우선 실행한다.
- 매 작업 종료에 변경 파일, 테스트, 미실행, 리스크, 다음 단계 승인 필요 여부 보고.

### 4.4 Claude Code 호환성 (선택, Codex가 주 실행 에이전트)

Claude Code는 기본적으로 `CLAUDE.md`를 읽는다. 공식 문서에서 권장하는 형태:

```markdown
@AGENTS.md

## Claude Code 전용
- 변경 전 현재 단계와 SDD 확인.
- 코드 리뷰 시 기능성만 아니라 Android 생명주기·Coroutine cancellation·메모리 누수·오래된 이벤트 반영 위험 점검.
```

Codex가 주 실행·검증 에이전트다. Codex와 Claude에 서로 다른 장문의 복제 지침을 넣지 않는다. Claude 전용 파일은 선택적인 호환성 인터페이스이며 M0의 필수 실행 게이트가 아니다.

### 4.5 Skills — 초기에 최소한만

M0에서 모든 Skills를 만들어야 한다는 뜻은 아니다. 공통 핵심 절차를 먼저 정의하고, 반복 사용하면서 실제로 필요한 것만 파일화한다.

권장 Skills 후보:

| Skill | 목적 | 도입 시점 |
|---|---|---|
| `spec-implementation` | 승인된 SDD 확인 → 작업 범위 → 구현 → 증거 출력 | M0 또는 M1 |
| `android-review` | Compose/ViewModel/Flow/Coroutine/Resource lifecycle 집중 리뷰 | M1 |
| `regression-investigation` | 실패 재현·최소 수정·회귀 등록 | M2~ |
| `performance-compare` | 조건 고정, 원시 측정 결과 비교, 개선 주장 검증 | M6 |

- Codex repository-local Skills 경로 예시: `.agents/skills/<name>/SKILL.md`.
- Claude Code repository-local Skills 경로: `.claude/skills/<name>/SKILL.md`.
- 공통 스킬 본문은 한 원본을 유지하고 동기화/링크 방식은 도구 호환성 검증 후 결정한다. 동작하지 않는 symlink를 마냥 가정하지 않는다.
- Skill frontmatter는 최소한 `name`/`description`을 두고, 자주 호출되지 않는 긴 지식은 `references/`로 분리한다.
- MCP는 반드시 필요한 외부 제어(예: Android 빌드/실기기 UI 검사)에만 추가. **MCP를 많이 설치하는 것이 Harness 품질의 증거는 아니다.**

### 4.6 PR 품질 게이트 계획

| Level | 내용 | 어디서 | 차단 여부 |
|---|---|---|---|
| G0 | 문서/SDD 상태·금지된 범위·시크릿 검사 | 로컬/PR | 가능하면 차단 |
| G1 | 컴파일, Android Lint, Detekt 등 정적 분석 | M1 이후 GitHub Actions | 실패 시 차단 |
| G2 | Unit/Repository/ViewModel 테스트 | 로컬/CI | 실패 시 차단 |
| G3 | Emulator Instrumentation/Compose UI 핵심 회귀 | M1 이후 별도 Job | 안정화 후 차단 여부 결정 |
| G4 | 실기기 Macrobenchmark/Perfetto/발열·메모리 | 개인 실기기 | PR 자동 차단 대신 측정 증거/릴리스 조건 |
| G5 | AI 코드 리뷰 | 별도 도구/로컬 또는 PR | 보조 증거; 자동 승인 대체 불가 |

- CI는 매번 긴 GPU/영상 라이브 검증을 돌리는 형태로 설계하지 않는다.
- 측정 재현성이 떨어지는 벤치마크를 GitHub Hosted Runner에서 절대적인 수치 기준으로 차단하지 않는다.
- 테스트 종류마다 실행 비용·실패 원인·필요한 디바이스/환경을 명확히 한다.
- AI 리뷰는 린트·테스트와 중복되는 단순 지적만 늘리지 않도록 영역을 한정한다.

### 4.7 에이전트 한 작업의 필수 출력 형식

```text
[작업 ID / 현재 M 단계]
1. 범위: 실제 변경한 요구사항과 의도적으로 제외한 범위
2. 변경 파일: 경로와 변경 이유
3. 아키텍처/계약 영향: 있음/없음 + 근거
4. 검증 결과:
   - PASS: 실행 명령, 결과, 로그/Artifact 경로
   - FAIL: 재현, 원인, 남은 작업
   - NOT_RUN: 실행 불가/생략 사유
5. 위험·가정·미해결 이슈
6. STATUS / build-log 갱신 내용
7. 커밋 여부·SHA (실제 커밋한 경우에만)
8. 다음 작업 제안 (임의 구현 금지)
```

### 4.8 M0 완료 기준(Exit Gate)

- [ ] Repository 초기화와 첫 구조 생성 완료
- [ ] `MASTER_PLAN.md` 복사 및 문서 버전 확인
- [ ] `AGENTS.md`, `CLAUDE.md`, `STATUS.md` 작성
- [ ] Codex GitHub Actions에서 현재 단계·AGENTS 지침 인식을 읽기 전용으로 확인(실제 실행했을 때만 PASS; Claude import는 선택 호환성 검사)
- [ ] M0 SDD와 ADR-0001 존재
- [ ] 단계 변경 승인·로그 기록 규칙 존재
- [ ] 최소 Harness 문서/링크 검증 명령이 동작하거나 미구현 사유 명시
- [ ] Codex로 **실제 한 번** 작은 계획-only 작업을 수행하고 지정된 보고 형식을 검토
- [ ] 이후 단계 코드를 건드리지 않았음이 diff로 확인됨
- [ ] `STATUS`에 M0 결과와 M1 진입 조건 기재

---

## 5. 구현 마일스톤 전체 로드맵

> 단계는 논리적 의존성을 뜻하며 기간 약속이 아니다. 각 단계 시작 전 상세 SDD 승인, 단계 완료 후 증거 리뷰를 한다.

| 단계 | 제목 | 현재 상태 | 주요 결과물 | 승인 기준 |
|---|---|---|---|---|
| **M0** | **AI Harness Core (선행)** | ☐ 미시작 | 지침·SDD·상태/기록·검증 정책 | M0 Gate |
| M1 | Android Native Foundation | ☐ 미시작 | Compose 앱, Multi Module, Fake data, 동작하는 Gradle/CI | M1 Gate |
| M2 | HLS Player Vertical Slice | ☐ 미시작 | Media3 HLS·재생 상태·lifecycle 테스트 | M2 Gate |
| M3 | Realtime Chat with Fake Transport | ☐ 미시작 | 메시지 모델·ViewModel·시퀀스·UI 검증 | M3 Gate |
| M4 | Minimal Chat Server & Integration | ☐ 미시작 | Spring Boot WS·메시지 조회·멱등 처리 | M4 Gate |
| M5 | Failure Injection & Recovery | ☐ 미시작 | 네트워크 단절·경합·재연결 회귀 | M5 Gate |
| M6 | Performance Baseline & Optimization | ☐ 미시작 | Raw benchmark + 추적 + 개선 증거 | M6 Gate |
| M7 | Globalization & UI/Product Polish | ☐ 미시작 | ko/en, 접근성·오류 UX·시연 흐름 | M7 Gate |
| M8 | Release Candidate & Portfolio Packaging | ☐ 미시작 | APK·시연 영상·README·최종 검증 보고서 | M8 Gate |
| M9 | 선택 확장(v0.2) | ☐ 보류 | 커뮤니티/DM/결제 등 | 별도 SDD |

### M1. Android Native Foundation

**목적:** Harness 규칙 아래에서 실제 Android 앱 골격을 만들고, 이후 모든 기능이 테스트 가능한 구조를 갖추게 한다.

구현 범위:

- 공식 버전 호환성 조사 및 `libs.versions.toml` 사용 여부 결정.
- 최소 앱 + `core:model`, `core:designsystem`, `feature:live`, `feature:chat`, `core:testing` 등 **실제로 필요한 최소 Multi Module**.
- Compose 화면 2개: 룸 목록, LIVE 상세(처음에는 Fake 데이터·플레이어 자리 표시).
- 화면 상태: Loading / Content / Empty / Error.
- 기본 UDF, ViewModel, DI, nav, FakeRepository 및 Unit Test.
- GitHub Actions에서 Android compile, lint, unit tests 연결.
- Android 실기기 또는 에뮬레이터에서 설치/실행 확인.

**M1 Gate**

- [ ] 로컬 클린 빌드 성공, CI도 같은 검증 실행
- [ ] Multi Module 의존 방향 및 순환 참조 없음
- [ ] Compose 화면 네비게이션과 기본 상태 테스트
- [ ] AI 구현 코드 리뷰: ViewModel/Compose 책임과 Lifecycle 위반 없음
- [ ] 빌드/테스트 버전과 환경이 README/로그에 기록됨

### M2. HLS Player Vertical Slice

**목적:** 보여주기용 비디오 플레이어가 아닌 **미디어 수명·오류·복구 계약이 있는** HLS 화면.

구현 범위:

- `PlayerController` 계약, Media3 실제 구현, 필요시 FakePlayer 도입.
- HLS fixture(초기 VOD) 재생·일시정지·버퍼링·오류 표시.
- 실제 LIVE playlist 테스트 환경 구성 및 재생 검증.
- 재생 시작 시간/상태 이벤트를 테스트 가능한 형태로 노출.
- 화면 전환·회전·백그라운드에 따른 재생 유지/종료 정책 정의.
- `Player.Listener`, surface, collector, coroutine job의 소유권·해제 검사.
- 네트워크 오류 시 제한된 재시도와 사용자 조치(재시도 버튼 등).

**M2 Gate**

- [ ] HLS 재생이 Android 실기기에서 확인됨
- [ ] LIVE fixture인지 VOD fixture인지 증거에 명시됨
- [ ] 정상 재생, 종료, 재진입, 오류, 백그라운드·복귀 검증
- [ ] 리스너·Surface·player가 화면 반복 진입으로 누적되지 않음(도구로 확인)
- [ ] 플레이어가 뷰모델/Composable 내부에 무분별하게 중복 생성되지 않음

### M3. Realtime Chat with Fake Transport

**목적:** 서버 통합 전에 **정합성 알고리즘과 UI 상태**를 결정적으로 테스트한다.

구현 범위:

- `ChatMessage`, `ChatUiState`, `ChatRepository`/`ChatTransport` 계약.
- Fake Transport로 채팅 송수신, ACK, 지연, 메시지 역순, 중복, 누락 발생.
- Pending→Sent/Failed 전송 상태 UI.
- `serverSequence` 기반 정렬/중복 제거 및 누락 탐지·보충 경로 설계.
- LazyColumn + 안정적 key + bounded message list/viewport 정책.
- Room 전환 직후 이전 룸의 늦은 메시지·요청 결과 무시.

**M3 Gate**

- [ ] 정상/지연/중복/역순/누락 메시지 Unit Test 모두 통과
- [ ] 느린 이전 요청이 최신 메시지·새 룸 상태를 덮어쓰지 않음
- [ ] 화면을 이탈할 때 구독·Job이 중단되거나 명시적 공유 정책을 따름
- [ ] 합성 메시지 발생기를 seed 고정하여 동일 결과 재현 가능
- [ ] Chat UI가 Fake Transport와 실제 Transport의 동일 계약을 사용

### M4. Minimal Chat Server & Integration

**목적:** 앱 단위의 Fake 성공과 실 WebSocket/HTTP 네트워크의 성공을 분리해 검증한다.

구현 범위:

- Spring Boot WebSocket endpoint(한 개 LIVE room에서 시작).
- 메시지 전송 → 서버의 멱등 판별 및 순번 부여 → 저장 → ACK/방송.
- 메시지 `afterSequence` 조회 API로 재접속 보충.
- 전송 ID 재사용 시 동일 내용은 기존 결과, 다른 내용은 충돌 처리.
- 단순 인증은 개발용 테스트 사용자 토큰 또는 제한된 테스트 권한으로 시작. 보안 경계 명시.
- 서버 메시지량·중복·조회 이력 계측; 필요 시 PostgreSQL 영속화.
- Docker Compose로 로컬 시작·종료·DB 초기화/테스트.

**M4 Gate**

- [ ] 실제 Android 앱 ↔ 로컬 WebSocket 연결 확인
- [ ] 동일 메시지 재전송 시 서버 중복 저장/방송 방지 테스트
- [ ] 동시 요청에도 룸 단위 순번의 중복·역전이 없음
- [ ] 네트워크 수신/서버 commit/ACK 타이밍을 구분하여 기록
- [ ] 재접속 후 누락 범위를 HTTP API로 보충 확인
- [ ] 데모 서버가 상시 비용 없이 로컬에서 재현 가능

### M5. Failure Injection & Recovery

**목적:** 핵심 차별화. 네트워크가 정상일 때 작동하는 앱을 넘어, **불확실한 결과와 리소스 경합**에 대응한다.

필수 장애 시나리오:

| ID | 주입 상황 | 기대 결과 |
|---|---|---|
| NET-01 | Wi-Fi 끊김 10초 후 복구 | 제한된 backoff 후 재연결, UI 정합성 회복 |
| NET-02 | 서버 저장 성공 뒤 ACK 소실 | 중복 전송/표시 방지, 상태 재확인 |
| NET-03 | 메시지 이벤트 순서 역전 | serverSequence 기준 정렬·중복 제거 |
| NET-04 | 재연결 중 기존 룸 탈퇴 | 이전 룸의 늦은 응답이 현재 화면에 반영되지 않음 |
| NET-05 | 조회 API가 이전 Snapshot을 늦게 반환 | 현재 상태·최신 이벤트가 덮어써지지 않음 |
| NET-06 | 채팅 트래픽 급증(합성 10/50/100 msg/s) | 메모리 상한 및 UI 업데이트 정책 유지; 성능 측정 |
| MEDIA-01 | HLS segment 요청 timeout/404/일시 네트워크 단절 | 오류 표시·제한된 복구·사용자 재시도 |
| LIFE-01 | 백그라운드/포그라운드 왕복 | 정책대로 재생/채팅 상태 복구, 구독 누수 없음 |
| LIFE-02 | 회전/프로세스 재생성 | 복구 가능한 상태만 유지, 잘못된 플레이어 중복 생성 없음 |
| LIFE-03 | LIVE 룸 반복 진입/이탈 | Player/Socket/Collector 리소스 누적 없음 |

실행 방식:

- Fake transport로 결정적 단위 테스트를 먼저 만들고,
- MockWebServer/Proxy 또는 자체 테스트 서버의 지연·차단·오류 응답으로 통합 검증,
- 네트워크 전환·프로세스 재생성·발열/메모리는 실기기로 확인.

**M5 Gate**

- [ ] 각 장애 시나리오가 테스트 코드나 재현 가능한 실행 절차와 연결됨
- [ ] 실패/수정 전 상태와 수정 후 검증이 `harness/build-log.md`에 남음
- [ ] Pending/Failed/Sent가 잘못 확정되는 경합 없음
- [ ] UI에서 메시지가 중복 표시되거나 이전 룸 데이터가 섞이지 않음
- [ ] 모든 작업이 무한 재시도/무한 버퍼링되지 않음

### M6. Performance Baseline & Optimization

**목적:** 성능 개선을 숫자로 증명한다. 최적화 기술을 넣는 행위 자체가 성과는 아니다.

기본 측정 항목:

| 지표 | 수집 위치 | 보고 방법 |
|---|---|---|
| Cold startup TTID/가능 시 TTFD | Macrobenchmark | 반복 측정 분포와 median/p95 등 |
| LIVE 룸 진입→첫 프레임 | Media3 이벤트 + 앱 Trace | 측정 시작/종료 지점 명시 |
| LazyColumn Scroll frame overrun/jank | Macrobenchmark FrameTimingMetric | Raw JSON, Perfetto trace |
| 채팅 이벤트 수신→UI 적용 지연 | monotonic clock 기반 앱 instrumentation | UI 상태 반영 proxy인지 실제 화면 표시 측정인지 구분 |
| LIVE 재버퍼링 횟수/시간 | Media3 playback events | 스트림/네트워크 조건 함께 기록 |
| 메모리 증가/GC/리소스 누수 | Android Profiler/Perfetto/heap | 반복 진입·이탈 전후 트렌드 |
| CPU·발열·배터리 | Android 측정/시스템 정보(가능 범위) | 환경·측정 한계·변동 요인 표기 |

**측정 프로토콜**

1. Android 실기기(예: Samsung Galaxy S24 Ultra)를 고정하고 모델·OS·앱 빌드/커밋 SHA 기록.
2. Release-like, profileable, non-debuggable 빌드로 측정한다. debug 결과와 비교하지 않는다.
3. 전원·배터리·온도·네트워크/서버·측정 횟수·기기 상태를 통제한다.
4. 먼저 아무 최적화 없는 Baseline을 측정하고 Raw JSON/trace를 보관한다.
5. 한 번에 한 가설을 세워 변경하고 동일 시나리오로 재측정한다.
6. 노이즈·편차·회귀·트레이드오프를 함께 기록한다. 실측 없는 % 개선 주장 금지.
7. Baseline Profile을 적용했다면 적용 전후, 설치/컴파일 조건을 맞춘 뒤 비교한다.

우선 검사 후보:

- 메시지 이벤트를 UI 상태로 변환하는 빈도 및 batch 처리 비용.
- LazyColumn item key/리컴포지션 범위/화면 밖 아이템 비용.
- 이미지 로딩/해제·Chat 목록 크기·불필요한 형식 변환.
- Player 재생과 채팅 렌더링의 동시 CPU/메모리 부담.
- 앱 초기화 지연, 불필요한 DI/DB/네트워크 초기화.

**M6 Gate**

- [ ] Raw Baseline·변경 후 Raw·환경·커밋 SHA 존재
- [ ] 측정 스크립트/시나리오와 반복 조건 문서화
- [ ] 문제→원인 가설→수정→재검증 사례 최소 2개 확보(개선 실패 사례도 정직하게 기록)
- [ ] 개선 항목 외 주요 회귀 테스트 통과
- [ ] 보고서에서 측정값, 추정, 측정 불가 항목이 명확히 구분됨

### M7. Globalization & UI/Product Polish

**목적:** 글로벌 팬 서비스 관점의 기본 UX 완성.

- ko/en 리소스화; 길이가 다른 번역에서 레이아웃 깨짐 검사.
- RTL 확장 가능성은 고려하되 기본 구현은 필요 여부 판단.
- 시간대/상대시간/서버 타임스탬프/locale의 처리 기준 명시.
- 네트워크 불안정 상태의 사용자 메시지, 접근성 contentDescription, font scaling.
- 영상 재생/채팅 오류 상태와 재시도 UI.
- 데모용 가상 아티스트/팬 콘텐츠, 최소한의 시각적 정리.

**M7 Gate**

- [ ] ko/en locale 전환 및 텍스트 길이 확인
- [ ] 영상·채팅·네트워크 오류 UX 시나리오 시연 가능
- [ ] 최소 접근성·확대 글꼴 시나리오 점검
- [ ] 실제 브랜드/저작권 침해 소지가 있는 자료 미사용

### M8. Release Candidate & Portfolio Packaging

**목적:** 채용 검토자가 빠르게 이해하고 신뢰할 수 있는 증거 중심 결과물 정리.

최종 제공물:

- 빌드 가능한 GitHub 공개 저장소(비밀정보·영상 저작권 확인).
- 다운로드 가능한 데모 APK 또는 명확한 재현 절차(배포 시 서명 정책 확인).
- 핵심 시연 영상(목록→LIVE→채팅→네트워크 단절→복구).
- `README` 첫 화면에 3가지 정보: **무엇을 구현했는가 / 어떤 문제가 있었는가 / 어떻게 측정·검증했는가**.
- 아키텍처 다이어그램(Compose/ViewModel/Data/WS/Media3 경계).
- 자동화·CI 스크린샷 또는 결과 링크, 장애 주입 테스트 근거.
- 성능 `Before / After` 표 + 측정 조건 + Raw 결과 링크.
- AI Coding Agent 작업 방식: SDD → Codex 구현 → 독립 리뷰 → 자동/실기기 검증 → 회귀 등록.
- 이력서에 들어갈 3~5줄 요약은 실제 결과가 나온 후 작성.

**M8 Gate**

- [ ] 외부 개발자가 README로 로컬 실행 재현 가능
- [ ] 공개 파일에 민감정보/보안 키/미허가 콘텐츠 없음
- [ ] 릴리스 후보 빌드 및 핵심 시나리오 검증 통과
- [ ] 성능 수치가 Raw 증거와 일치
- [ ] '대용량 트래픽 운영'과 '합성 부하 실험'을 구분
- [ ] 미완료·한계·보완 계획을 솔직하게 명시

---

## 6. 결함 검증 매트릭스와 구체적인 합격 기준

### 6.1 테스트 계층

| 계층 | 대상 | 실행 시점 |
|---|---|---|
| Unit | 순서 관리, dedupe, pending/ack, 상태 전이, Flow cancel | 변경 시 매번 |
| Integration | 실제 HTTP/WS 계약, DB 중복/순번, API 재연결 | 관련 기능 PR |
| UI | Compose 화면·내비게이션·오류/로딩/복귀 | 주요 흐름 변경 시 |
| Fault Injection | 응답 지연, 단절, 역순, timeout, 429/5xx | M5부터, 변경 영향 시 |
| Manual Device | 화면 회전·백그라운드·네트워크 전환·메모리 | 기능 Gate별 |
| Benchmark | 시작·스크롤·첫 프레임·메모리 | M6부터 비교 기준 |
| Security/Privacy | 시크릿, 로그 식별자, 외부 리소스·권한 | 각 기능/공개 전 |

### 6.2 절대 통과할 수 없는 조건(Release Blockers)

- 채팅에 잘못된 메시지 본문/룸 또는 다른 사용자 메시지가 섞임.
- 같은 메시지가 화면에 중복 확정 표시됨(설계상 허용한 Pending 표시와 확정 표시의 단계 차이는 예외).
- 네트워크 복구 후 누락된 메시지를 정상으로 오인하고 방치함.
- 화면 종료 후 보유하면 안 되는 Player/Socket/Collector가 계속 누적됨.
- 검증 실패 또는 미실행 상태를 통과로 기록함.
- 디버그 로그/리포지토리에 시크릿이나 민감한 데이터가 노출됨.
- 측정 환경이 다르거나 Raw 근거가 없는 성능 향상 수치를 성과로 기재함.

### 6.3 목표치 관련 운영 원칙

초기 계획에서 특정 수치(예: 40% 성능 개선, 1만 동접 지원)를 **이미 달성된 결과**로 선언하지 않는다. 측정 전 목표는 목표로만 표시하고, M6 때 Baseline과 평가 기준을 정한다.

합성 트래픽 `10 / 50 / 100 messages/sec`는 **성능 시나리오의 시작 샘플**이며 실제 프로덕션 동접 수를 의미하지 않는다. 테스트 환경의 서버·네트워크가 병목인지 앱 렌더링이 병목인지 분리해서 분석한다.

---

## 7. AI Harness 운영 절차 — ChatGPT ↔ Codex 간 왕복

### 7.1 하나의 작업 사이클

1. **ChatGPT:** `STATUS`와 해당 SDD를 읽고 이 단계에서 할 범위 1개를 선택한다.
2. **ChatGPT:** 목표, 비목표, 변경 허용 파일, 재현/테스트, 완료 기준이 포함된 Codex 지시문을 만든다.
3. **Codex:** 현재 Repository를 읽기 전용으로 조사하고 실제 구현에 필요한 영향 범위를 확인한다. 필요하면 짧게 계획한다.
4. **Codex:** 승인 범위 내에서 구현·검증을 수행한다. 범위 확대가 필요하면 중단하고 이유를 보고한다.
5. **Codex:** `build-log`와 `STATUS`의 결과를 갱신하되, **사람의 최종 승인 없이 단계 완료로 확정하지 않는다**.
6. **ChatGPT:** Codex의 diff·테스트 로그·결함·가정을 검토한다. 결과가 불충분하면 재검증/보완 요청을 작성한다.
7. **사람:** 주요 UX·실기기·성능 등 자동화로 보증하기 어려운 부분을 확인하고 진행을 승인한다.
8. **다음 작업:** 동일 단계에서 다음 작은 작업으로 이동하거나, Exit Gate를 통과하면 다음 단계 SDD를 시작한다.

### 7.2 단계별 SDD 템플릿

```markdown
# [Mx] [기능/단계 이름] — SDD

## Status
DRAFT / APPROVED / IMPLEMENTING / VERIFIED / BLOCKED

## Goal
이번 단계에서 해결할 사용자/기술 문제.

## In Scope / Out of Scope
포함/제외할 범위를 구체적으로 명시.

## Input / Output / Contracts
UI, API, DB, 이벤트 계약 및 오류.

## Architecture Impact
모듈 간 의존, Lifecycle 소유권, 데이터 책임.

## Normal Path
정상 사용자 시나리오.

## Failure / Recovery
네트워크, 시퀀스, 취소, 재진입, 중복, 리소스 해제.

## Test Plan
Unit/Integration/UI/실기기 검증 대상.

## Acceptance Criteria
- [ ] 관찰·재현 가능한 합격 조건

## Evidence / Commands
실행 명령, 테스트 로그, 스크린샷/trace.

## Open Questions / ADRs
미확정 사항과 결정 필요 시점.
```

### 7.3 `docs/STATUS.md`의 최소 형식

```markdown
# FanStage 작업 현황

- Updated: YYYY-MM-DD HH:mm +09:00
- Current milestone: M0
- Current task: M0-001
- State: NOT_STARTED | IN_PROGRESS | BLOCKED | REVIEW | VERIFIED
- Last approved commit: 없음
- Latest verification: NOT_RUN (사유)
- Current blockers: 없음
- Next single action: M0 Harness 문서 생성 및 read-only 검토

## Milestone Checklist
- [ ] M0 Harness Core
- [ ] M1 Android Foundation
- [ ] M2 HLS Player
- [ ] M3 Fake Chat
- [ ] M4 Real Chat Server
- [ ] M5 Fault & Recovery
- [ ] M6 Performance
- [ ] M7 Globalization/Polish
- [ ] M8 Portfolio Release

## Recent Decisions
- YYYY-MM-DD: 결정/근거/관련 ADR

## Evidence Links
- `harness/build-log.md` 해당 작업 기록
- CI Run URL / 테스트 로그 / 커밋 SHA
```

### 7.4 `harness/build-log.md`의 한 작업 기록 템플릿

```markdown
## Mx-yyy — 작업명 (YYYY-MM-DD)
- Spec: docs/specs/Mx-xxx.md
- Agent/Tool: Codex CLI (실제 사용 버전 확인 후 기록)
- Scope approved: ...
- Changed files: ...
- Commit: 실제 SHA 또는 UNCOMMITTED
- Commands: ...
- PASS: ...
- FAIL: ...
- NOT_RUN: ...
- Evidence paths: ...
- Reviewer findings: ...
- Open issues / Next action: ...
- Human approval: PENDING / APPROVED / REJECTED
```

### 7.5 각 작업에 재사용할 Codex 지시문 템플릿

```text
작업: [Mx-yyy 제목]

최우선으로 현재 저장소의 AGENTS.md, docs/STATUS.md를 확인하고,
이번 단계의 SDD 및 직접 관련된 ADR을 읽어줘.
전체 문서를 무조건 읽거나 다음 단계까지 구현하지 마.

목표: [정확한 하나의 결과]
허용 범위: [파일/모듈/기능]
제외 범위: [이번 작업에서 하지 말 것]

진행 순서:
1. 현재 구현·테스트·의존 관계를 조사하고 변경 영향과 위험을 짧게 요약.
2. 구현 전 실패 조건/정상 조건에 대응하는 테스트를 정의.
3. 범위 내에서 최소 변경으로 구현.
4. 관련 정적검사·테스트·빌드를 실제 실행. 실행 불가 항목은 NOT_RUN.
5. 변경 파일·성공·실패·미실행·증거 경로·리스크를 보고.
6. harness/build-log.md, docs/STATUS.md에 사실 기반으로 기록.

제약:
- 승인 없는 범위 확대/라이브러리 도입/아키텍처 변경/성능 주장 금지.
- 테스트를 숨기거나 꺼서 통과시키지 말 것.
- 무관한 파일 포매팅 및 대규모 리팩토링 금지.
- 단계 완료, push, 외부 비용 발생 작업은 사용자 승인 없이 실행하지 말 것.
```

### 7.6 Codex 결과를 ChatGPT로 가져올 때 사용할 메시지

```text
FanStage 현재 단계: [Mx]
Codex 작업 ID: [Mx-yyy]
목표: [짧게]
변경 파일/핵심 diff: [붙여넣기 또는 파일 첨부]
테스트: PASS/FAIL/NOT_RUN과 명령·로그
예외/미해결: [내용]
STATUS와 build-log 변경: [내용]

이 작업이 SDD Exit Gate를 충족했는지 검토해 줘.
문제점이 있다면 보완할 Codex 지시문을 작성하고,
충족했다면 다음 **한 작업**을 제안해 줘.
```

### 7.7 다른 채팅으로 갈 때 복원 프롬프트

```text
FanStage 프로젝트를 이어서 진행하자.
최상위 기준은 docs/MASTER_PLAN.md,
현재 진행 상태는 docs/STATUS.md,
실행 검증 이력은 harness/build-log.md야.
이 세 파일과 현재 단계 SDD를 우선 확인해.
실제 완료된 것과 계획만 있는 것을 구분하고,
누락·충돌·미검증 항목을 짚은 뒤 다음 한 작업의 Codex 지시문만 제안해 줘.
다음 단계 구현은 내가 승인하기 전에는 진행하지 마.
```

---

## 8. 단계별 Git 작업·리뷰 규칙

- Phase 하나를 통째로 큰 PR로 만들기보다 수용 가능한 단위로 분리한다.
- 브랜치 예: `feat/m2-hls-player`, `test/m5-chat-ack-loss`, `perf/m6-chat-batch`.
- 문서 변경만 있는 PR도 목표/범위와 증거를 적는다.
- 리뷰 체크: 기능 계약, Lifecycle, Coroutine cancellation, state race, 메모리/성능, 보안/데이터, 테스트 품질, 범위 이탈.
- 중요한 결함은 테스트 실패 재현과 그 실패를 없앤 수정으로 남긴다.
- CI green은 실제 기기 완전 정상의 증명이 아니다. 디바이스 검증을 별도로 기록한다.
- 실측 개선 전후 커밋을 둘 다 식별할 수 있어야 한다.
- 공개 포트폴리오이므로 의미 있는 ADR과 트러블슈팅 문서는 PR 및 코드와 함께 버전 관리한다.

---

## 9. 리스크·함정·트레이드오프

| 위험 | 왜 문제인가 | 회피/검증 방법 |
|---|---|---|
| 포트폴리오 과대 범위 | 앱은 커지지만 Android 심층 역량 증명이 약해짐 | LIVE+Chat+복구+성능을 먼저 완료 |
| 과도한 Harness | 지침만 많고 실제 자동 검증은 없음 | M0 최소화, M1부터 실행형 검사 연결 |
| Agent가 만든 코드 무검증 | 코드 생성 자체는 차별화 요소가 아님 | SDD 테스트·Fault·실기기·독립 리뷰 |
| 과도한 Multi Module | 유지관리·빌드 비용 증가 | 필요한 경계만 모듈화, ADR 남김 |
| 영상 VOD를 LIVE로 포장 | 미디어 역량 설명이 부정확 | 움직이는 LIVE playlist 별도 재현 |
| 메시지 Exactly-once 주장 | 송수신 네트워크는 중복/불확실성 존재 | 멱등·순서·UI dedupe 계약만 정확히 주장 |
| 라이브/채팅 타임라인 동기화 혼동 | 서로 다른 전송 지연 | 시간축 구분, 동기화 보장 주장 금지 |
| 에뮬레이터 성능 일반화 | 실제 사용 기기 성능과 다름 | 실기기 Release-like 측정 |
| 성능 측정 재현성 부족 | 효과가 노이즈인지 구분 불가 | 조건 고정, 반복, Raw trace 보관 |
| 실제 트래픽 과장 | 면접 시 신뢰성 하락 | 합성 이벤트 부하와 상용 운영 구분 |
| 영구적인 외부 비용 | 개인 프로젝트 유지 부담 | 로컬 우선, 서버리스 유료 연동은 사전 승인 |
| 미허가 영상·브랜드 복제 | 저작권·상표 문제 | 가상 캐릭터·허가된 샘플·독립 UI |
| 코드/테스트 범위 폭주 | 지원 일정 전에 결과가 늦어짐 | 명확한 Gate, 선택 확장 보류 |

---

## 10. 포트폴리오 최종 평가 관점

면접관이 보았을 때 아래 질문에 GitHub/리포트로 답할 수 있어야 한다.

1. 왜 MVVM/UDF/Multi Module 구조를 선택했나? 실제로 어떤 변경 영향이 줄었나?
2. ExoPlayer와 채팅이 함께 동작할 때, 화면 재진입·백그라운드 복귀에서 리소스 소유권을 어떻게 설계했나?
3. 서버가 저장했지만 ACK가 유실된 메시지는 어떻게 처리하나?
4. 중복·역순·누락·느린 과거 응답을 어떻게 테스트로 재현했나?
5. 성능 문제가 실제로 있었나? 원시 trace는 무엇이고 어떤 가설로 최적화했나?
6. AI 에이전트가 생성한 코드에 어떤 실수가 있었고 이를 어떤 규칙·테스트로 재발 방지했나?
7. 테스트를 실행하지 못한 범위와 실서비스 수준으로 확장했을 때 남은 한계는 무엇인가?
8. 왜 불필요한 클라우드·MSA·LLM 기능을 제외했나?

**높은 평가를 받을 만한 결과:** 앱이 동작할 뿐 아니라, 코드와 SDD·실패/복구 테스트·성능 근거가 서로 연결된다.

**낮은 평가 위험이 있는 결과:** 화면은 화려하지만 외부 API 예제의 단순 조립, SDD 실현 여부 불명, 벤치마크 미측정, 허위 성과 수치.

---

## 11. 현재 미확정 결정(작업 과정에서 필요 시 확정)

다음은 지금 불필요하게 질문으로 막지 말고, 해당 M 단계 시작 시 ADR 또는 SDD로 결정할 사항이다.

| 항목 | 기본 제안 | 확정 시점 |
|---|---|---|
| Repository 이름 | `fanstage-android-ai-harness` | M0 |
| 앱 namespace / applicationId | 예시용 독립 도메인 사용 | M1 |
| minSdk / targetSdk / AGP / Kotlin | 공식 호환성 및 테스트 환경 검토 후 고정 | M1 |
| Navigation 버전 | 당시 안정 조합 | M1 |
| 실제 LIVE HLS fixture | 재현 가능한 로컬/허가 스트림 | M2 |
| Player 백그라운드 정책 | 화면 종료 시 해제 우선, 상세 SDD 결정 | M2 |
| 채팅 저장·조회 범위 | 룸 순서 + 제한된 보관 | M3/M4 |
| Backend 인증 모형 | 제한된 데모 사용자/토큰 | M4 |
| CI 기기 테스트 범위 | 빠른 PR 게이트와 실기기 Gate 분리 | M1/M5 |
| 성능 측정 기기·반복 수 | 확보된 실기기와 안정적 반복(예: 10회 이상) | M6 |
| 성능 합격 기준 | 실측 Baseline 확보 후 합리적 기준 | M6 |
| GitHub 공개 여부/라이선스 | 결과물·저작권·보안 검토 후 결정 | M8 |

---

## 12. 공식 참고 자료 (2026-11-04 확인)

공식 자료에서 확인된 범위를 기반으로 하며, 아래는 구현 직전에 다시 버전/권장사항을 확인할 자료이다.

- [Android Architecture Guide](https://developer.android.com/topic/architecture) — UI/Data, SSOT, UDF, 필요한 경우 Domain Layer.
- [Android Architecture Recommendations](https://developer.android.com/topic/architecture/recommendations) — ViewModel/Flow/Compose/테스트/성능 권장.
- [Media3 ExoPlayer HLS](https://developer.android.com/media/media3/exoplayer/hls) — HLS/LIVE와 적응형 스트리밍.
- [Macrobenchmark Guide](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) — Startup/Compose scroll, 실기기 benchmark, Release-like 설정, JSON/trace.
- [OpenAI: Iterating Development Workflows with Codex](https://developers.openai.com/cookbook/examples/codex/iterating-development-workflows-with-codex) — AGENTS/단계별 계획/실행 로그와 승인 게이트의 예시. 파일 구조는 필수가 아니라 권장 컨벤션.
- [OpenAI: Skills for OSS maintenance](https://developers.openai.com/blog/skills-agents-sdk) — `.agents/skills/` repo-local 기술.
- [OpenAI: Rethinking skills and prompts](https://developers.openai.com/blog/rethinking-skills-and-prompts-for-gpt-6-astra) — AGENTS/Skills에 과도하게 지침을 넣지 않고 맥락별로 필요한 것만 확인.
- [Claude Code: Project Memory](https://code.claude.com/docs/en/memory) — `CLAUDE.md`, `@AGENTS.md` import 공식 안내.
- [Claude Code: Skills](https://code.claude.com/docs/en/skills) — `.claude/skills/<skill-name>/SKILL.md`.
- [Google Play Billing Integration](https://developer.android.com/google/play/billing/integrate) — v0.2 결제 확장 시 서버 구매 검증·권한 지급·승인 규칙.

참고 자료는 **구현 타당성의 지침**이지 포트폴리오 결과가 실제로 검증되었다는 증거는 아니다. 성능 결과·구현 완료는 이 Repository에서 실측/실행한 로그만으로 주장한다.

---

## 13. 바로 다음 실제 행동: M0-001만 실행

**목적:** Repository와 Harness 코어 문서를 생성하고 첫 단계 관리가 가능한지 확인한다. **Android 프로젝트 생성 금지.**

### 13.1 ChatGPT에 요청할 문구

> `MASTER_PLAN.md를 읽고 M0-001(Codex용 Harness Core 초기 설정)을 진행하자. 우선 Codex 지시문을 작성하되 Android 프로젝트나 기능 코드는 아직 생성하지 마. AGENTS.md는 짧게 유지하고 Claude는 @AGENTS.md로 공통 규칙을 불러오게 해줘. 상태 추적과 검증 로그를 만들어 단계 누락을 방지하자.`

### 13.2 Codex에 전달할 초기 작업 지시문 초안

```text
프로젝트: FanStage — Android AI Harness & Portfolio
현재 작업: M0-001 (Harness Core만 생성)

먼저 현재 디렉터리/기존 파일을 read-only로 조사해줘.
기존 작업이 있으면 임의로 덮어쓰지 말고 충돌과 보존 방안을 보고해줘.

docs/MASTER_PLAN.md를 최상위 요구사항으로 사용해 아래 항목만 구현해줘.
- Git Repository 기본 구조(기존 Git이 있다면 재초기화 금지)
- AGENTS.md (핵심 규칙을 짧게 작성)
- CLAUDE.md (`@AGENTS.md` import)
- docs/STATUS.md (M0-001 진행 상태)
- docs/specs/M0-harness-core.md
- docs/adr/ADR-0001-development-workflow.md
- docs/test-matrix.md
- harness/build/M0-harness-core.md
- harness/build-log.md
- README.md, .gitignore (현재 상황에 맞는 최소 내용)

제약:
1. Android Studio 프로젝트/Gradle/앱 코드/Backend 프로젝트 생성 금지.
2. 실제로 없는 테스트나 CI 성공을 주장하지 말 것.
3. 임의의 외부 유료 인프라 연결, GitHub push 금지.
4. 각 파일의 역할이 중복되거나 문서가 순환 참조하지 않는지 점검.
5. 추가 파일/도구/Skill 도입 필요성이 생겨도 이번 작업에서 과잉 구현하지 말고 제안만 할 것.

검증:
- 산출물 경로·링크·참조 유효성 검토
- AGENTS.md / CLAUDE.md 내용 확인
- 향후 한 단계씩 실행·승인·기록할 수 있는지 검토
- git diff를 검토해 승인 범위 밖 변경 여부 확인
- 실행한 명령/결과, NOT_RUN, 보류 사항을 build-log와 결과 보고에 남길 것
- 단계 최종 승인(PASS)은 사용자가 리뷰한 후 확정하게 할 것

종료 보고:
1. 생성/수정 파일 목록
2. 문서 간 참조 구조
3. 수행한 검증과 NOT_RUN
4. 미확정 결정/리스크
5. 다음으로 진행할 수 있는 작업(M0-002) 제안만 작성
```

### 13.3 M0-001 이후에도 아직 해서는 안 되는 것

- M0가 완료되지 않았는데 M1 Android 프로젝트를 생성하는 것.
- Codex가 M0 이후 전체 로드맵을 연속 실행하는 것.
- GitHub Actions 파일만 만들어 두고 실제 CI 성공이라고 주장하는 것.
- 성능 데이터 없이 'xx% 향상' 문구를 미리 작성하는 것.

---

## 14. 변경 이력

| 날짜 | 버전 | 변경 내역 |
|---|---|---|
| 2026-11-04 | v0.1 | FanStage 계획, Harness 우선, 마일스톤 M0~M8, 검증/인계 체계를 수립. 당시 구현·검증 결과 없음. |
| 2026-11-04 | v0.1-public | 공개 저장소용 문서에서 개인 경력·특정 기업 지칭을 제외하고, 실제 진행 상태는 STATUS를 따르도록 분리. |
| 2026-11-04 | v0.1.1-public | Codex를 주 실행 에이전트로 확정. Claude CLI import는 선택 호환성 검사로 변경하고, M0-002를 Codex API 기반 읽기 전용 리허설로 구체화. |

**이 문서의 시작 작업은 M0-001이다. 최신 실행·승인 상태는 [STATUS.md](STATUS.md)를 따른다.**
