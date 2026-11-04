# FanStage 작업 현황

- Current milestone: M1 — Android Native Foundation
- Current task: M1 Exit Gate independent audit after M1-005 Activity recreation verification
- State: IN_PROGRESS (M1-005 PR #16 squash merged; post-merge main CI PASS; M1 overall Exit Gate NOT VERIFIED)
- Last approved main code commit: `e22bb4042173d6b0160b9d46c50837cbfde6bb56` (PR #16 squash merge)
- Human approval: M0 전체 진행 및 결과에 대한 사용자 포괄 승인 (2026-11-04); M0-002의 성공 증거 기반 Exit Gate 충족
- Latest verification: [PR #16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16) squash merged; [post-merge main CI Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) **PASS**, Gradle build/lint, JUnit 7/7 and API35 Compose Activity recreation/navigation 1/1. Both job logs and two raw ZIP artifacts independently checked against GitHub SHA-256; see build-log.
- Current blockers: M1 clean **local** build remains NOT_RUN (CI fresh-runner build PASS). M1 Exit Gate's independent checklist audit remains pending. Explicit device rotation, low-memory process death/relaunch, physical-device install, LIVE playback, release-like device performance remain NOT_RUN (some are later milestones). Existing Android Lint warnings (13 previously observed), deprecated Compose UI test factory, and Actions Node20/setup-java@v4 warnings remain; no gate suppressed.
- Next action: independently audit every M1 Exit Gate criterion against source/CI evidence and distinguish later milestone gates. Do not start M2 until a separate evidence-backed M1 gate decision.

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
