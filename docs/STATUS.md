# FanStage 작업 현황

- Current milestone: M1 — Android Native Foundation
- Current task: M1-007 developer-local clean build — results under review
- State: REVIEW / M1 EXIT GATE BLOCKED (local clean build PASS; android/README.md drift remains)
- Last approved main code commit: `e22bb4042173d6b0160b9d46c50837cbfde6bb56` (PR #16 squash merge)
- Human approval: M0 전체 진행 및 결과에 대한 사용자 포괄 승인 (2026-11-04); M0-002의 성공 증거 기반 Exit Gate 충족
- Latest prior main CI verification: [PR #16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16) squash merged; [post-merge main CI Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) **PASS**, Gradle build/lint, JUnit 7/7 and API35 Compose Activity recreation/navigation 1/1. Both job logs and two raw ZIP artifacts independently checked against GitHub SHA-256; see build-log.
- Latest local verification: M1-007 clean/assemble/lint/unit **PASS** on macOS arm64, JDK 17 and Android SDK 37.0; 7/7 unit tests and 0 lint errors / 13 warnings. Source base `7fbcc7108d87ecc03951de168c5b3aeb4c29ff52` plus Gradle wrapper executable-bit fix; details in [build-log](../harness/build-log.md).
- Current blockers: `android/README.md` still describes verified build/device checks as NOT_RUN and versions as unverified; correct separately. Real rotation, process death/relaunch and physical-device install remain NOT_RUN. LIVE and physical performance belong to later milestones. Existing lint and CI deprecation warnings remain.
- Next action: correct `android/README.md` build and device verification drift in a separately reviewed M1 task. M1 remains incomplete; no M2 work.

## Milestone Checklist

- [x] M0 Harness Core
- [ ] M1 Android Foundation
- [ ] M2 HLS Player
- [ ] M3 Fake Chat
- [ ] M4 Real Chat Server
- [ ] M5 Fault & Recovery
- [ ] M6 Performance
- [ ] M7 Globalization/Polish
- [ ] M8 Portfolio Release

## M1-008 문서 현행화 (리뷰 대기)

- Android README의 기존 CI·기기 검증 설명을 현행화했다. M1-007 로컬 clean build는 [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)의 별도 미병합 증거다. 이 문서 변경과 해당 PR의 검토·병합 전에는 M1을 완료로 표시하지 않는다.

## 결정 및 진입 조건

- 2026-11-04: 사용자가 M0-001 보완 및 후속 작업 실행을 포괄 승인했다. [PR #1](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/1)을 Squash Merge하여 M0-001 문서 기반 산출물은 승인 처리했다.
- 2026-11-04: Codex가 주 구현·검증 에이전트다. Claude Code 파일은 선택 호환성 지침이며 실제 import는 M0 필수 Gate에서 제외한다. [M0 SDD](specs/M0-harness-core.md) 및 [ADR-0001](adr/ADR-0001-development-workflow.md) 참고.
- M0 Exit Gate: Codex plan-only attempt 2 PASS, JSON 스키마 검증 PASS, 후속 단계 파일 미생성 PASS. [Raw Codex 결과](../harness/reviews/M0-002-plan-only-result.json), [Actions Run](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41692832207), [build-log](../harness/build-log.md) 참조. M1 상세 SDD/호환성 조사가 다음 선행 작업.
- Repository `CodingBot000/fanstage-android-ai-harness`는 Public으로 생성되어 `main`에 초기 커밋 `93304b079cb20ccb470afd7dcd1737a41eaa9a7a`가 업로드되었다. 최초 M0-001 범위의 commit/push 금지는 이후 사용자의 별도 업로드 지시로 변경되었다. 라이선스는 미결정이다.
- 공개용 `docs/MASTER_PLAN.md`는 초기 로컬 원본을 복사한 뒤 개인·기업 관련 맥락을 제거해 수정했다. 과거 원본 SHA-256은 현재 공개판의 해시가 아니다.

- 2026-11-04: [M1 SDD](specs/M1-android-foundation.md) 및 [ADR-0002](adr/ADR-0002-android-foundation-stack.md)에서 공식 호환성 근거와 후보 버전을 기록했다. M1-001은 문서 작업이고 M1-002부터 실제 앱을 생성한다.

- M1-002 source CI build/lint/unit PASS; M1-004 API 35 emulator navigation PASS (PR #14 / main Run #41755701493). Physical-device and broader lifecycle verification NOT_RUN.

- 2026-11-04: M1-005 extends the existing one-test emulator smoke with ActivityScenario recreation on the detail route (ADR-0004). **NOT_RUN** until PR CI proves it; rotation/real process-death are separate NOT_RUN blockers.

- 2026-11-04: M1-005 [PR #16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16) merged as `e22bb4042173d6b0160b9d46c50837cbfde6bb56`; [main Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) completed PASS (unit 7/7, Compose Activity recreation/navigation 1/1). Actual rotation, process death, physical device and M1 Gate audit remain NOT_RUN/NOT VERIFIED.

- 2026-11-04 M1-006: independently audited MASTER_PLAN M1 Gate against current source (three-module dependency graph, Compose navigation/ViewModel), prior main [Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) original job logs and artifact metadata, and latest PR/Actions list (no open PR/active workflows at audit start). Gate remains **BLOCKED**, primarily developer-local explicit clean build NOT_RUN; see [audit](reviews/M1-006-exit-gate-audit.md). Documentation-only PR; no new CI/Codex invocation, no device/live/performance claim.

- 2026-11-04 M1-007: local clean build, lint and 7 unit tests PASS; wrapper mode fixed from 100644 to 100755 after initial permission-denied failure. See [build-log](../harness/build-log.md). M1 overall approval remains pending.
