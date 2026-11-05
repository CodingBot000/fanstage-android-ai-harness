# FanStage 작업 현황

- Current milestone: M7 — Globalization & UI/Product Polish
- Current task: M7 한국어·영어, 접근성 및 오류 UX
- State: M7 핵심 UX 검증 및 PR 소스 CI PASS; 최종 PR head CI 확인 후 병합한다. M6 PR #28의 두 실험은 되돌렸고 개선 효과와 미측정 항목은 그대로 유지한다.
- Human approval: M0~M8 개발과 commit/push/PR/merge 사전 승인; M9 제외. 상시 과금·신규 보안 자격 증명·비가역 외부 작업은 별도 승인.
- 사용자 결정: 모든 활성 룸의 화면·Fake 서버 합계 1,000개, 표시 500개, 메시지당 1,000 Unicode 코드포인트. 이후 선택은 권장안을 적용해 질문 없이 진행한다.
- Latest verification: SM-G975N/API 31 실기기에서 A/B 각 10회 이상 측정. A는 수신 처리 시간이 감소했지만 대조군 프레임 회귀 가능성이 있어 되돌렸고, B는 시작·콘텐츠 탐지 중앙값 악화로 되돌렸다. [실험 보고서](reviews/M6-002-ab-experiments.md) 참조.
- NOT_RUN: M4 실기기 LAN 연결·운영 인증/TLS, M5 실기기 네트워크 전환·실제 네트워크 부하·장시간 메모리 검증; M6 TTFD·실제 룸 첫 프레임·수신→UI 지연·반복 입장 메모리/GC 및 실제 WebSocket 측정. M2 원격 LIVE, 실기기 LIVE 첫 프레임, 실제 LMK, 장시간 heap/성능도 계속 NOT_RUN.
- Next action: M7 PR의 최종 CI·리뷰를 확인하고 통과 시 병합한 뒤 M8을 준비한다.

## Milestone Checklist

- [x] M0 Harness Core
- [x] M1 Android Foundation
- [x] M2 HLS Player
- [x] M3 Fake Chat
- [x] M4 Real Chat Server (PR #25 병합; main CI PASS)
- [x] M5 Fault & Recovery (PR #26 병합; main CI PASS)
- [ ] M6 Performance
- [x] M7 Globalization/Polish (핵심 검증 PASS; 최종 PR head CI가 병합 조건)
- [ ] M8 Portfolio Release

## M1-008 문서 현행화

- Android README의 기존 CI·기기 검증 설명을 현행화했다. M1-007 로컬 clean build는 [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)에서 병합된 별도 증거다. README 수정은 [PR #20](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/20)에서 검증·병합되었다.

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

- 2026-11-04 Fast Track v1: M0~M8 사용자 사전 승인, P0/P1 차단 및 P2 이월·M8 재평가를 [ADR-0005](adr/ADR-0005-fast-track.md)에 기록. M1 핵심 Gate와 미실행 항목은 위에 분리 기록.

- 2026-11-04 M2: 사용자가 최초 foreground 자동재생, background pause와 수동 재개, 화면 이탈 해제, PIP/백그라운드 오디오 제외 및 제한된 재시도 정책을 제시했다. [ADR-0006](adr/ADR-0006-m2-playback-lifecycle.md) 참조.

- 2026-11-04 M2 실기기 Gate: API 36 실제 단말 합성 VOD·LIVE/lifecycle 및 반복 해제 증거를 검토해 위 범위에서 VERIFIED. 원격 LIVE·실제 LMK·전체 heap 누수 부재는 검증하지 않았다.
