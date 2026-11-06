# FanStage 작업 현황

- UI·UX 통합 PR 준비: 댓글·채팅 입력 하단 고정, 하트·카드·간격 개선, 로컬 VOD 우선 및 벤치마크 후순위. 전용 API 36 로컬 21/21 및 실제 서버 Android 통합 1/1 PASS. 사용자 요청으로 GitHub Actions 에뮬레이터 실행은 임시 OFF, 테스트 정의 보존. 원격 빌드·서버 CI와 PR 리뷰 확인 필요.

- LOCAL-MEDIA-001 진행 중: PR #40 병합 및 main CI PASS 기준. 사용자의 지시에 따라 실기기 대신 전용 에뮬레이터만 사용한다. 상세는 [SDD](specs/LOCAL-MEDIA-001-mp4-streaming.md).

- Current milestone: M8 — Release Candidate & Portfolio Packaging
- Current task: UX-007~011 비주얼 개편 및 ARCH-001~003 모듈화 병합 완료 — 제출용 문서 현행화
- State: **Portfolio Ready — v0.1 (source-only)**. M8-001 [PR #30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30), M8-002 [PR #31](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/31), 클린 체크아웃 재현 [PR #33](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/33), MIT/제3자 고지 [PR #34](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/34)가 병합됐다. 사용자 확인 범위에서 포트폴리오 제출 준비를 마감했다. 이는 공개 APK·운영 서비스 검증 완료나 모든 위험 해소를 뜻하지 않는다. 최초 WebSocket 타임아웃은 PR #30 한정 예외 아래 원인 미확정으로 보존한다. [최종 범위와 잔여 한계](reviews/M8-final-readiness.md).
- Human approval: M0~M8 개발과 commit/push/PR/merge 사전 승인; M9 제외. 상시 과금·신규 보안 자격 증명·비가역 외부 작업은 별도 승인.
- 사용자 결정: 모든 활성 룸의 화면·Fake 서버 합계 1,000개, 표시 500개, 메시지당 1,000 Unicode 코드포인트. v0.1은 공개 APK 없이 소스와 로컬 빌드 절차 제공. CI 전용 임시 Debug 키 생성 허용, 공개 Release 키는 별도 승인 전 생성 금지. 프로젝트 직접 작성 코드·문서는 MIT, 저작권 표기는 `2026 FanStage contributors`; 제3자 조건은 별도 유지.
- Latest verification: SM-G975N/API 31 실기기에서 A/B 각 10회 이상 측정. A는 수신 처리 시간이 감소했지만 대조군 프레임 회귀 가능성이 있어 되돌렸고, B는 시작·콘텐츠 탐지 중앙값 악화로 되돌렸다. [실험 보고서](reviews/M6-002-ab-experiments.md) 참조.
- NOT_RUN: M4 실기기 LAN 연결·운영 인증/TLS, M5 실기기 네트워크 전환·실제 네트워크 부하·장시간 메모리 검증; M6 TTFD·실제 룸 첫 프레임·수신→UI 지연·반복 입장 메모리/GC 및 실제 WebSocket 측정. M2 원격 LIVE, 실기기 LIVE 첫 프레임, 실제 LMK, 장시간 heap/성능도 계속 NOT_RUN.
- Latest source reproduction: 동일 개발 환경의 별도 `main` clone `e09beb205dcf1ae12510225dff710e3e05682cb8`에서 JDK 17 및 기존 SDK·Debug 키·Gradle 캐시로 README 빌드·lint·단위 51/51, API 36 에뮬레이터 기본 흐름 PASS. [재현 기록](reviews/M8-clean-clone-reproduction.md).
- Next action: 최신 11개 모듈과 Compose 화면 갤러리가 반영된 소스 포트폴리오를 제출한다. 실기기 성능 A/B·전 화면 큰 글꼴/TalkBack·원격 LIVE·운영 서비스 검증으로 확대 주장하지 않는다. 기존 PR #30 예외는 그 PR에만 적용한다.

## 최신 UX·모듈화 결과 (2026-11-06)

- **UX-007~011 및 ARCH-001~003 완료·병합:** [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) 최종 head `561afce227a40d552a7bed7e05d9c2c3f5360fb8`, Squash Merge `3c77a6305b5bf639fdd1e8b669ec6ed4a9f60ef9`. 가상 아티스트 WebP 자산 9개, Discover/Community/Media/Messages/LIVE/Replay 화면 리디자인, 총 11개 모듈 및 모듈 경계 검사 반영.
- **PR head 검증:** [Android build/lint/unit·API 35 에뮬레이터 CI #42066922626](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42066922626) PASS, [실제 WebSocket·PostgreSQL CI #42066922691](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42066922691) PASS. Codex 독립 코드 리뷰는 최종 head에서 완료, 지적 0건·미해결 스레드 0건.
- **로컬 기록:** 전용 API 36 에뮬레이터 전체 계측 14/14 및 별도 DiscoverTest 9/9·기존 LIVE 회귀 4/4, 서버 장애 회귀 PASS(정확한 실행 조건은 [ARCH SDD](specs/ARCH-001-feature-modularization.md)). 이미지 프롬프트·해시는 [자산 기록](portfolio/visual-assets.md); 화면·무음 영상은 README 참조.
- **범위·한계:** 합성 VOD·Fake DM/커뮤니티, 실기기 성능 A/B 및 전체 TalkBack·큰 글꼴 검증 NOT_RUN. 이전 M6 개선 미입증과 M8 권리·타임아웃 위험 기록 유지. 로컬 미커밋 `.gitignore`는 이번 원격 문서 정리에서 접근·변경하지 않았다.

## Milestone Checklist

- [x] M0 Harness Core
- [x] M1 Android Foundation
- [x] M2 HLS Player
- [x] M3 Fake Chat
- [x] M4 Real Chat Server (PR #25 병합; main CI PASS)
- [x] M5 Fault & Recovery (PR #26 병합; main CI PASS)
- [ ] M6 Performance
- [x] M7 Globalization/Polish (핵심 검증 PASS; 최종 PR head CI가 병합 조건)
- [x] M8 Portfolio Ready — v0.1 소스 공개 범위의 제출 준비 완료 (배포·운영 완전 검증 의미 아님)

## M1-008 문서 현행화

- Android README의 기존 CI·기기 검증 설명을 현행화했다. M1-007 로컬 clean build는 [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)에서 병합된 별도 증거다. README 수정은 [PR #20](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/20)에서 검증·병합되었다.

## 결정 및 진입 조건

- 2026-11-04: 사용자가 M0-001 보완 및 후속 작업 실행을 포괄 승인했다. [PR #1](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/1)을 Squash Merge하여 M0-001 문서 기반 산출물은 승인 처리했다.
- 2026-11-04: Codex가 주 구현·검증 에이전트다. Claude Code 파일은 선택 호환성 지침이며 실제 import는 M0 필수 Gate에서 제외한다. [M0 SDD](specs/M0-harness-core.md) 및 [ADR-0001](adr/ADR-0001-development-workflow.md) 참고.
- M0 Exit Gate: Codex plan-only attempt 2 PASS, JSON 스키마 검증 PASS, 후속 단계 파일 미생성 PASS. [Raw Codex 결과](../harness/reviews/M0-002-plan-only-result.json), [Actions Run](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41692832207), [build-log](../harness/build-log.md) 참조. M1 상세 SDD/호환성 조사가 다음 선행 작업.
- Repository `CodingBot000/fanstage-android-ai-harness`는 Public으로 생성되어 `main`에 초기 커밋 `93304b079cb20ccb470afd7dcd1737a41eaa9a7a`가 업로드되었다. 최초 M0-001 범위의 commit/push 금지는 이후 사용자의 별도 업로드 지시로 변경되었다. 당시 라이선스는 미결정이었다; 이후 사용자는 직접 작성분에 MIT를 선택했다.
- 공개용 `docs/MASTER_PLAN.md`는 초기 로컬 원본을 복사한 뒤 개인·기업 관련 맥락을 제거해 수정했다. 과거 원본 SHA-256은 현재 공개판의 해시가 아니다.

- 2026-11-04: [M1 SDD](specs/M1-android-foundation.md) 및 [ADR-0002](adr/ADR-0002-android-foundation-stack.md)에서 공식 호환성 근거와 후보 버전을 기록했다. M1-001은 문서 작업이고 M1-002부터 실제 앱을 생성한다.

- M1-002 source CI build/lint/unit PASS; M1-004 API 35 emulator navigation PASS (PR #14 / main Run #41755701493). Physical-device and broader lifecycle verification NOT_RUN.

- 2026-11-04: M1-005 extends the existing one-test emulator smoke with ActivityScenario recreation on the detail route (ADR-0004). **NOT_RUN** until PR CI proves it; rotation/real process-death are separate NOT_RUN blockers.

- 2026-11-04: M1-005 [PR #16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16) merged as `e22bb4042173d6b0160b9d46c50837cbfde6bb56`; [main Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) completed PASS (unit 7/7, Compose Activity recreation/navigation 1/1). Actual rotation, process death, physical device and M1 Gate audit remain NOT_RUN/NOT VERIFIED.

- 2026-11-04 M1-006: independently audited MASTER_PLAN M1 Gate against current source (three-module dependency graph, Compose navigation/ViewModel), prior main [Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) original job logs and artifact metadata, and latest PR/Actions list (no open PR/active workflows at audit start). Gate remains **BLOCKED**, primarily developer-local explicit clean build NOT_RUN; see [audit](reviews/M1-006-exit-gate-audit.md). Documentation-only PR; no new CI/Codex invocation, no device/live/performance claim.

- 2026-11-04 M1-007: local clean build, lint and 7 unit tests PASS; wrapper mode fixed from 100644 to 100755 after initial permission-denied failure. See [build-log](../harness/build-log.md). M1 overall approval remains pending.

- 2026-11-04 Fast Track v1: M0~M8 사용자 사전 승인, P0/P1 차단 및 P2 이월·M8 재평가를 [ADR-0005](adr/ADR-0005-fast-track.md)에 기록. M1 핵심 Gate와 미실행 항목은 위에 분리 기록.

- 2026-11-04 M2: 사용자가 최초 foreground 자동재생, background pause와 수동 재개, 화면 이탈 해제, PIP/백그라운드 오디오 제외 및 제한된 재시도 정책을 제시했다. [ADR-0006](adr/ADR-0006-m2-playback-lifecycle.md) 참조.

- 2026-11-04 M2 실기기 Gate: API 36 실제 단말 합성 VOD·LIVE/lifecycle 및 반복 해제 증거를 검토해 위 범위에서 VERIFIED. 원격 LIVE·실제 LMK·전체 heap 누수 부재는 검증하지 않았다.

## UX-001 v0.2 작업 중 (2026-11-05)

- [SDD](specs/UX-001-visual-foundation.md), `core:designsystem`, Discover 홈 및 기존 LIVE 연결을 작업 트리에 추가했다. M8 Gate 상태는 위와 동일하다.
- API 36 에뮬레이터 [실제 홈 캡처](evidence/ux-001/home.png). 최종 코드 기준 `:app:assembleDebug`, `:app:lintDebug`, `DiscoverTest` 2건 PASS; 기존 `:feature:live:testDebugUnitTest` 51건 PASS. 기존 `RoomNavigationSmokeTest#roomListOpensDetailAndReturns`는 최종 소규모 UI/취소 처리 수정 전 PASS.
- NOT_RUN: 최종 코드에서 전체 계측 테스트·벤치마크, 실기기 검증, Preview 렌더링 확인. 시작 화면이 바뀌므로 기존 시작 벤치마크 결과는 새 화면의 수치가 아니다.
- 미완료: 커뮤니티와 추천 콘텐츠는 화면에 고정된 예시이며 별도 가상 아티스트/추천 모델과 이미지 카드가 부족하다. Replay는 준비 중 표시이며 재생 미구현이다. 이 상태를 UX-001 완료로 표시하지 않는다.

- 2026-11-05: 사용자 지시에 따라 `codex/ux-continuous-batch` 브랜치에서 v0.3 UX 확장 트랙을 시작했다. UX-001 SDD 범위를 갱신했으며 구현·단계 완료는 아직 아니다. M8 Gate 상태는 변경하지 않는다.
- UX-001 v0.3: 가상 아티스트 ID 및 카드 이미지 보강 진행. 빌드·lint·기존 live 단위 테스트 PASS. 새로운 UI 캡처, Replay 연결 및 나머지 UX 단계는 미완료다.
- UX-001 v0.3: Discover와 LIVE/Replay 연결의 관련 검증 PASS; 커뮤니티 연결은 UX-003에서 구현 예정. 다음 작업 UX-002 공통 탐색. 전체 통합 검증 NOT_RUN_DEFERRED.
- UX-002: 네 탭 탐색과 Discover 스크롤 복원 계측 PASS. 탭 내용·공통 TopBar 및 전체 상세 백스택은 UX-003~006에서 완성·검증한다. 다음 UX-003 Community.
- UX-003: Community Fake 피드·상세·좋아요·댓글 및 Discover 연결 구현. API 36 관련 5건과 빌드·lint PASS. 공지 빈 상태이며 오류·빈 상태 주입과 전체 통합은 후속 검증. 다음 UX-004 Media.
- UX-004: Media 그리드·필터 및 기존 LIVE/Replay 이동 구현, API 36 관련 6건·빌드·lint PASS. LIVE 상세 재디자인과 전체 서버 회귀는 미완료. 다음 UX-005 Messages.
- UX-005: 메모리 로컬 대화 목록·상세·입력과 로컬 저장 표시 구현, API 36 관련 7건·빌드·lint PASS. 실제 DM 전송 및 읽음 상태는 미구현. 다음 UX-006 통합 및 UX-004 상세 보완.
- 2026-11-05 일시 중단: 사용자 요청으로 안전 지점에서 중단. `codex/ux-continuous-batch` HEAD `6f7eb6a`. LIVE 상세에 Player/Chat 카드 표현 보완은 미커밋이다. 재개 시 UX-004 상세 화면 검토·캡처, UX-003 상태/공지 및 UX-005 읽음 표시 등 잔여 범위를 보완한 뒤 UX-006 전체 통합 검증을 수행한다. PR/CI/병합은 아직 하지 않았다.
- UX-006 로컬 통합: 공통 TopBar, 공지·로컬 읽음·필터 복원·취소 경합 방어 및 LIVE 상세 표현 보완. 최종 작업 트리 전체 Android 계측 14/14(실제 서버 case 포함), 서버 통합·장애 주입, Debug/Release/Benchmark 빌드·lint·unit PASS. [통합 SDD](specs/UX-006-integration.md). 실기기 성능 A/B·전 화면 접근성·독립 리뷰·최종 CI는 NOT_RUN. 기존 M6 미검증 항목은 유지하며 v0.1 포트폴리오 준비 완료와 구분한다.
- PR #36 최초 head API 35 CI는 새 홈의 첫 LIVE 카드가 초기 화면 밖이어서 기존 테스트의 스크롤 없는 대기가 실패했다. 테스트를 실제 스크롤 흐름으로 수정했으며 작은 화면 API 36에서 기존 4/4·신규 9/9 PASS; 수정 head 원격 CI 확인 필요. 로컬 테스트 시작 중 뒤늦게 연결된 실기기에서도 테스트가 일부 시작되어 즉시 중단했고, 결과는 사용하지 않았다. 상세는 build-log 참조.
- PR #36 최종 head `38ec99d`: Android build/lint/unit·API 35 탐색 [CI PASS](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42040756558), PostgreSQL·실제 WebSocket [CI PASS](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42040756538). Codex 독립 리뷰의 Messages P2(IME 입력창 가림·신규 메시지 미스크롤) 2건을 수정해 최신 head 재리뷰에서 추가 지적 없음·스레드 Resolved. [PR #36](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/36)을 `7b5fb4cd523c3bab70cbe2f9d6d530a087dde296`로 Squash Merge. 실기기 성능 A/B·전 화면 접근성은 NOT_RUN.

## UX-007~011 / ARCH-001~003 별도 확장 (2026-11-05)
- 기준 `5bd022b`에서 새 브랜치 `codex/ux-premium-modular-batch`를 생성했다. 로컬 `.gitignore`의 plans 제외 규칙은 사용자 미커밋 변경으로 보존한다.
- UX-007: 그룹별 원본·파생 자산 9개와 토큰을 추가하고 빠른 빌드·app 단위 테스트 PASS, 별도 API 36 에뮬레이터의 Hero 렌더 확인. [SDD](specs/UX-007-premium-visual.md), [자산 근거](portfolio/visual-assets.md). 기존 에뮬레이터와 개인 기기 데이터는 변경하지 않았다. 다음 UX-008. 전체 계측·서버·성능 NOT_RUN_DEFERRED.
- UX-008 (2026-11-06): 16:9 Hero·가로 미디어·벡터 탐색·데모 LIVE 표시 적용, 빠른 빌드/lint/unit PASS, 전용 에뮬레이터 수동 LIVE 왕복·Community 이동 및 [캡처](evidence/ux-008/discover.png) 확인. 전체 connected 테스트는 두 AVD가 보이는 환경이라 NOT_RUN_DEFERRED. 다음 UX-009.
- UX-009 (2026-11-06): 커버·프로필·피드·상세 이미지 계층과 뒤로가기 문구 개선. 빌드/lint/unit PASS, 전용 에뮬레이터에서 가상 댓글·좋아요의 상세 왕복 보존을 수동 확인. [캡처](evidence/ux-009/artist.png). 자동 connected 테스트는 다중 AVD로 NOT_RUN_DEFERRED. 다음 UX-010.
- UX-010 (2026-11-06): Media 이미지 크롭·데모 LIVE 라벨·Replay 상세 정리. 관련 빌드/lint/단위 PASS, 전용 API 36에서 합성 LIVE/Replay 재생·복귀 및 각 Player Init/Release 한 쌍 확인. [캡처](evidence/ux-010/media.png). 전체 계측·서버 회귀 NOT_RUN_DEFERRED. 다음 UX-011.
- UX-011 (2026-11-06): 메시지 시각·아바타 개선, IME와 축소 viewport에서 마지막 메시지 및 과거 읽기 위치 수동 검증, 빈 입력·긴 메시지 단위 테스트 PASS. [대화 캡처](evidence/ux-011/conversation.png). connected 전체 검증은 ADB 다중 AVD로 NOT_RUN_DEFERRED. UX-007~011 실제 화면 증거 확보 후 다음 ARCH-001로 진행한다.
- ARCH-001 (2026-11-06): `core:model` 순수 모델/계약, `core:data` 세션 Fake, `core:artwork` resolver/리소스 및 AppContainer 도입. 관련 모듈·앱·계측 APK 빌드, 데이터/앱 단위와 lint PASS; 전용 AVD 수동 화면·좋아요 왕복 확인. [SDD](specs/ARCH-001-feature-modularization.md). 자동 connected 전체는 NOT_RUN_DEFERRED. 다음 ARCH-002 Discover 이동.
- ARCH-002 Discover (2026-11-06): `:feature:discover`로 화면·VM 이동, 관련 빌드·단위 및 계측 APK 컴파일 PASS; 전용 AVD의 Home→LIVE 수동 확인. connected 자동 실행 NOT_RUN_DEFERRED. 다음 Community.
- ARCH-002 Community: `:feature:community` 이동 및 관련 빌드/단위/계측 APK 컴파일 PASS; 전용 AVD에서 아티스트·Post 좋아요 왕복 확인. connected 자동 검증 NOT_RUN_DEFERRED. 다음 Media.
- ARCH-002 Media: `:feature:media`와 독립 ViewModel 분리, feature 간 직접 의존 제거. 관련 빌드/단위·계측 APK 컴파일 PASS, 전용 AVD에서 Replay 필터·재생 확인. 전체 connected NOT_RUN_DEFERRED. 다음 Messages.
- ARCH-002 Messages: `:feature:messages`로 이동, 기존 IME/스크롤 코드 유지. 관련 빌드·단위·계측 APK 컴파일 PASS, 전용 AVD에서 IME 입력·로컬 전송 확인. connected 전체 NOT_RUN_DEFERRED. 다음 ARCH-003 통합.
- ARCH-003 진행: Replay 화면을 기존 `feature:live`로 이동, app에는 MainActivity/AppContainer만 남김. 경계 스크립트 PASS. 별도 ADB 서버에 전용 AVD 한 대만 연결해 기존 LIVE·새 화면 계측 13/13 PASS. 실제 서버 포함 전체 최종 검증은 진행 전.
- ARCH-003 로컬 통합 (2026-11-06): 11개 모듈·경계 검사 PASS, 별도 ADB 서버의 전용 API 36 AVD 및 격리 서버에서 전체 Android 계측 14/14·서버/장애 회귀 PASS. 이후 Activity 재생성 및 IME 접근 검사를 추가한 DiscoverTest 9/9 PASS. Debug/Release/Benchmark 빌드·lint·unit 및 core:data 테스트 PASS. [ARCH 기록](specs/ARCH-001-feature-modularization.md). 독립 리뷰·실기기 성능 A/B·전 화면 접근성·최종 CI는 NOT_RUN. 다음 통합 PR 및 CI 확인.
- ARCH-003 CI: core:data 테스트와 경계 검사를 Android gate에 추가, 로컬 PASS. PR #38의 수정 head 원격 CI 및 독립 리뷰 확인 중.
- ARCH-003 추가 감사: Discover 오류가 다른 탭 카탈로그를 비우지 않도록 공통 Fake 카탈로그를 독립 주입, 회귀 단위 테스트와 DiscoverTest 9/9 PASS. rcLocal 빌드·M4/M6 manifest 경계 PASS. 최종 head PR CI 별도 확인.

## UX-012 (2026-11-06)
- 그리드/카드 두 줄·말줄임 및 높이 통일, 상세 상단 뒤로가기 아이콘, LIVE/DM 입력창 안 전송 버튼·IME Send, 영상 TextureView 전환 및 앱 아이콘 적용. [SDD](specs/UX-012-layout-navigation-chat-player.md), [영상 surface ADR](adr/ADR-0008-compose-video-surface.md).
- 에뮬레이터 전체 Android 계측 18/18·서버 통합/장애 회귀 PASS, 이후 UI 계측 5/5 및 일반 Debug/Release/rcLocal/Benchmark 빌드·lint·unit·manifest·모듈 경계 PASS. 키보드 전송 및 영상 전환 프레임 수동 확인. 실기기는 사용자 요청으로 에뮬레이터로 전환해 NOT_RUN. 기존 일반 앱은 사용자 승인에 따라 에뮬레이터에 `install -r`로 갱신했다. 기존 `.gitignore` 변경은 보존한다.

## UDF-001~004 (2026-11-06)
- UDF-001 진행: Community 액션 및 Post Detail Route/Screen 분리, 관련 컴파일·단위 테스트 PASS. 전체 통합·계측·CI NOT_RUN. [SDD](specs/UDF-001-004-ui-contracts.md).
- UDF-002: Messages 상태·행동 및 목록/대화 Route·Screen 분리. 관련 컴파일·단위 PASS, 계측·전체 통합 NOT_RUN.
- UDF-003: Discover/Media 재시도 행동과 Media 필터 경계 정리. 관련 컴파일·취소 회귀 PASS, 전체 통합 NOT_RUN.
- UDF-004 로컬 통합: Android 빌드/lint/unit, 모듈·manifest 검사, 전용 API 36 기존 계측 18/18 및 상태 기반 UI 2/2, 격리 서버 통합·장애 테스트 PASS. 외부 MP4 7개·실기기·성능·전체 접근성 NOT_RUN. 최종 PR CI·독립 리뷰 대기.
- UDF-004 원격: 구현 head `c4fda55` Android 및 서버 CI PASS, 독립 Codex 리뷰 지적 0건. 기록 추가 후 최종 head 검사 확인 중: [PR #43](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/43). 원격 에뮬레이터는 기존 사용자 설정으로 SKIPPED.

## READABILITY-001~006 (2026-11-06)
- Community/Discover/Media/Messages 구조 분리와 로컬 빌드/lint/unit·모듈/manifest 검사 PASS. 전용 신규 API 36 AVD 계측 20건 중 2건 FAIL; 같은 두 실패가 변경 전 main에서도 재현됐다. 기존 테스트 변경과 live 코드 변경이 계획에서 금지되어 있어 원인 해결 전 통합 PR/병합은 차단. [SDD](specs/READABILITY-001-006-compose-structure.md).
- READABILITY 再検証: ユーザー承認の下、画面外コメントへのテストスクロールとIME移動後の座標安定待ちを追加。専用 API 36 で最終 UI 20/20、ビルド/lint/unit PASS。初回失敗と中間再発は SDD に保持。PR・CI・レビュー待ち。

## READABILITY 실기기 검증 (2026-11-06)
- 사용자 승인으로 SM-S908N/API 36에 일반 Debug 앱 갱신 후 별도 테스트 패키지로 계측. 최초 Activity 재생성 후 화면 밖 좋아요 테스트 1건 FAIL을 실제 목록 스크롤로 보완하고 전체 UI 회귀 **20/20 PASS**. 합성 HLS·LIVE/DM IME·Community 댓글/좋아요 포함. 서버·외부 MP4/오디오·장시간 성능·전체 접근성은 NOT_RUN. [상세](specs/READABILITY-001-006-compose-structure.md).
