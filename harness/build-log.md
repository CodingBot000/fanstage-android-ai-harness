# 작업 및 검증 기록


## M1-008 — Android README evidence reconciliation (2026-11-04)

- Scope: update `android/README.md` to describe the existing wrapper, pinned versions, actual main CI and emulator evidence, and the separate pending M1-007 local clean build PR. Preserve the distinction between verified scenarios and NOT_RUN device/lifecycle cases; no Android behavior or dependencies change.
- Base: `main` `7fbcc7108d87ecc03951de168c5b3aeb4c29ff52`; [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19) remains unmerged and its local result is attributed to its source and mode fix, not to unchanged main. This entry is proposed on a separate branch; commit SHA is available from Git history.
- Evidence: prior [main CI run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) documented in the M1-006 audit; [M1-007 PR](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19) and local clean-build log referenced there. GitHub API `gh run view 41790821988 --json status,conclusion,jobs` returned completed/success for both build-lint-unit and emulator jobs; this is job-status verification, not a reinspection of their raw test reports.
- Verification: `git diff --check`; local Markdown file link check and README source/evidence review. No Gradle or new local device test is claimed for this documentation task; CI status is attached to the PR.
- M1 remains incomplete pending review; physical device, actual rotation and process death remain NOT_RUN. No M2 work.

> **공개 기록 주의:** 아래 초기 M0-001의 '원본 바이트 동일성' 및 SHA-256은 공개판 수정 **이전** 로컬 검증 당시의 기록이다. 현재 GitHub의 `docs/MASTER_PLAN.md`와 같은 내용·해시라는 뜻이 아니다. 비공개 원본 파일은 저장소에 게시하지 않았다.

## M0-001 — Harness Core 초기화 (2026-11-04)

- Spec: [M0 SDD](../docs/specs/M0-harness-core.md) (DRAFT)
- 승인 범위: M0-001 문서 작성·검증 및 로컬 Git 초기화.
- Agent/Tool: Codex desktop (버전 확인하지 않음); Git 2.50.1 (Apple Git-155); Python 3.14.6.
- 변경 파일: README.md, .gitignore, AGENTS.md, CLAUDE.md, docs/MASTER_PLAN.md, docs/STATUS.md, docs/specs/M0-harness-core.md, docs/adr/ADR-0001-development-workflow.md, docs/test-matrix.md, harness/build/M0-harness-core.md, harness/build-log.md. 기존 원본과 .DS_Store 보존.
- Commit: UNCOMMITTED
- Commands: `git init`; `cp docs/FanStage_Android_AI_Harness_Master_Plan_v0.1.md docs/MASTER_PLAN.md`; `git --version`; `python3 --version`; `shasum -a 256 docs/FanStage_Android_AI_Harness_Master_Plan_v0.1.md`; Python 표준 라이브러리로 필수 파일·원본 바이트·상대 Markdown 링크·파일 목록·지침·상태·기본 시크릿 패턴 확인; `git status --short --untracked-files=all`; `git diff --check`; Python으로 미추적 파일 공백·민감정보 패턴 재검사.
- PASS: 필수 파일 및 문서 참조, 원본 v0.1 바이트 동일성, CLAUDE 첫 줄, 범위 내 파일 목록 및 상태 확인. 원본 SHA-256: `6adbd5e8b0607b171069f50ba41259c25e020ef951cef9032c172994b9c9931f`. 기본 민감정보 패턴 검사에서 일치 없음 (포괄적인 보안 감사는 아님).
- FAIL: 없음.
- NOT_RUN: Claude Code의 실제 import 검증 (실행하지 않음); Android Gradle Build, Android Test, Emulator Test, Android CI (프로젝트 및 CI 미구현); G1~G5 (해당 실행 환경·구현 없음). M0 전체 Exit Gate의 별도 계획-only 작업도 미실행.
- Evidence paths: 이 기록, [상태](../docs/STATUS.md), `docs/FanStage_Android_AI_Harness_Master_Plan_v0.1.md` (당시 로컬 원본, 공개 저장소에 없음) 및 [당시 사본의 공개판](../docs/MASTER_PLAN.md). 별도 로그 파일은 생성하지 않음; 명령 결과는 작업 세션에 출력됨.
- 문제점 / 미해결: 문서 지침 자체는 자동 차단 장치가 아니다. M0 전체와 SDD/ADR 승인, Claude import 검증은 대기 중이다. 추후 푸시가 필요한 경우 공개 저장소 사용은 사용자 지정; 이번 작업의 푸시는 범위 밖이다.
- Next action: M0-001 사용자 리뷰 및 승인.
- Human approval: PENDING

## M0-001 — 독립 리뷰 보완 재검증 (2026-11-04)

이 항목은 위 초기 작업 기록과 별개의 재검증이다. 초기 검증의 상세 출력과 종료 코드는 소급하여 재구성하지 않았다. 변경 범위는 AGENTS.md, .gitignore, docs/specs/M0-harness-core.md, docs/STATUS.md, harness/build-log.md이다. Commit: UNCOMMITTED. Human approval: PENDING.

### 실행 명령과 결과

- 필수 파일 확인 (종료 코드 **0**, `missing: []`):

  ```sh
  python3 -c 'from pathlib import Path; fs="README.md .gitignore AGENTS.md CLAUDE.md docs/MASTER_PLAN.md docs/STATUS.md docs/specs/M0-harness-core.md docs/adr/ADR-0001-development-workflow.md docs/test-matrix.md harness/build/M0-harness-core.md harness/build-log.md".split(); missing=[f for f in fs if not Path(f).is_file()]; print("missing:", missing); raise SystemExit(bool(missing))'
  ```

- 상대 Markdown 파일 링크 확인 (종료 코드 **0**, `missing links: []`). 원본과 동일 사본인 마스터 플랜은 계획상의 미생성 경로와 예제 링크를 포함하므로 검사에서 제외했다. 링크의 앵커 유효성은 검사하지 않았다.

  ```sh
  python3 -c 'from pathlib import Path; import re; fs=[p for p in Path(".").rglob("*.md") if ".git" not in p.parts and p.name not in ("MASTER_PLAN.md", "FanStage_Android_AI_Harness_Master_Plan_v0.1.md")]; missing=[(str(p),t) for p in fs for t in re.findall(r"\]\(([^)]+)\)",p.read_text()) if "://" not in t and not t.startswith("#") and not (p.parent/t.split("#")[0]).exists()]; print("missing links:",missing); raise SystemExit(bool(missing))'
  ```

- `git check-ignore -v build/example android/app/build/example android/core/model/build/example` — 종료 코드 **0**; 세 경로 모두 `.gitignore:11:build/`에 일치.
- `git check-ignore -q harness/build/M0-harness-core.md` — 종료 코드 **1**; 해당 지침은 무시되지 않음 (의도한 결과).
- `git status --short --untracked-files=all` — 종료 코드 **0**; 최초 저장소의 파일 12개 모두 미추적이며 `harness/build/M0-harness-core.md`가 목록에 포함됨. `.DS_Store`는 무시됨. 미추적 파일은 일반 `git diff`에 표시되지 않는다.
- `git diff --check` — 종료 코드 **0**, 출력 없음. 추적된 변경만 검사하므로 미추적 파일의 공백 검사 근거는 아니다.
- `cmp -s docs/MASTER_PLAN.md docs/FanStage_Android_AI_Harness_Master_Plan_v0.1.md` — 종료 코드 **0**, 원본과 사본 동일.

### 결과와 한계

- **PASS:** 위 명령의 필수 파일·상대 파일 링크·ignore 경로·미추적 파일 목록·원본 동일성 확인.
- **FAIL:** 없음. 보완 전 `/build/`는 루트에만 적용되는 기존 결함으로 확인되었고 `build/`와 Harness 예외로 수정했다.
- **NOT_RUN:** Claude Code 실제 import, plan-only 리허설(M0-002 미승인), Android 빌드·테스트·에뮬레이터·CI(미구현), 마스터 플랜 내부 예제 링크와 앵커 검증. M0 전체 Exit Gate 및 사람의 승인도 미완료.
- Next action: M0-001 사용자 리뷰 및 승인. M0-002와 M1은 시작하지 않는다.

## M0-001 — 초기 공개 커밋과 원격 리뷰 (2026-11-04)

- 별도 사용자 지시로 최초 M0-001의 commit/push 제외 조건이 초기 공개 업로드에 한해 변경되었다.
- Repository: [CodingBot000/fanstage-android-ai-harness](https://github.com/CodingBot000/fanstage-android-ai-harness), visibility: Public, branch: `main`.
- 초기 공개 Commit: [`93304b079cb20ccb470afd7dcd1737a41eaa9a7a`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/93304b079cb20ccb470afd7dcd1737a41eaa9a7a).
- 검증 주체/방법: 로컬 Codex의 push 완료 보고 + ChatGPT의 GitHub 연결을 통한 읽기 전용 원격 확인. 로컬 CLI 검증과 구분한다.
- PASS (원격): Public 저장소, main HEAD, 초기 커밋 및 지정된 11개 파일 확인. `AGENTS.md` 공통 참조와 `CLAUDE.md` 첫 줄 확인. 지정된 회사명 키워드에 대한 11개 공개 파일 검사에서 일치 항목 없음(패턴 기반 검사의 한계가 있음).
- 검토 발견 사항: 공개판 수정 전 SHA-256은 현재 공개 문서의 해시가 아님; 로컬 전용 원본의 상대 링크가 공개 저장소에서 깨짐; Repository 이름 및 초기 커밋 상태 기록이 오래됨. 본 문서 수정으로 공개판 기록을 정리하고, 공개용 마스터 계획의 개인 경력 중심 문구를 정비한다.
- NOT_RUN: Claude Code 실제 import, plan-only 리허설, Android 빌드·테스트·CI, 로컬 재실행 검증.
- Human approval: PENDING. M0-001은 REVIEW 상태이며 M0-002 및 M1은 시작하지 않는다.

## M0-001 — 승인·Merge 및 M0-002 실행 준비 (2026-11-04)

- 사용자 승인: M0-001 리뷰 보완 및 후속 단계의 실행을 포괄 승인. 이는 **검증이 되지 않은 결과의 PASS 처리**를 의미하지 않는다.
- PR #1: [공개 문서 정합성 보완](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/1), Squash Merge 성공.
- Merge SHA: `e7dc155c70e46929650d38541827fc2c8232c29d`.
- M0-001: APPROVED (문서 기반 범위); M0 전체: NOT_VERIFIED.
- 현재 작업 M0-002: Codex GitHub Action을 이용한 읽기 전용 plan-only 실동작 검증.
- Changes (구성 예정): `.github/workflows/m0-codex-smoke.yml`, `.github/codex/prompts/m0-002-plan-only.md`, `.github/codex/schemas/m0-002-result.schema.json`, 관련 SDD·ADR·상태·로그.
- M0-002 검증: NOT_RUN (워크플로 병합 전); API 사용량 과금 가능, job 시간 20분 제한, `contents: read`, 에이전트 read-only.
- 실패 시 Exit Gate 미통과, 자동 재시도/다음 단계 전환 금지.

## M0-002 — GitHub Actions Codex plan-only 최초 실행 FAIL (2026-11-04)

- Workflow: [M0 Codex plan-only rehearsal, Run #41692832207](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41692832207)
- 실행 SHA: `1a22b55afa5cdc9d9d21f3ed52006fdbd453dae4` (PR #2 Squash Merge)
- Trigger: `push` to `main` (M0-002 워크플로 추가)
- Agent/Tool: `openai/codex-action@v1`, OpenAI Codex CLI v0.166.0 (GitHub Actions 로그에서 확인)
- PASS: GitHub-hosted runner 시작, 저장소 체크아웃, Codex CLI 설치 및 실행 시작
- FAIL: 첫 Responses API 호출에서 `401 Unauthorized: Incorrect API key provided`. GitHub Secret의 실제 원문은 출력·검토하지 않음. 등록 값이 유효한 OpenAI API secret인지 사용자 재확인 필요.
- NOT_RUN: Codex 출력 JSON 생성과 schema assertion, 변경 없음 검증 단계(선행 API 인증 실패로 skip), Android 빌드·테스트·실기기·CI(미구현).
- Artifacts: 결과 JSON 생성 실패로 업로드 항목 없음. 근거는 해당 Actions Run 및 Job 로그.
- Risk/Recovery: Secret 키 교체는 사용자만 수행. 유효한 키 재등록 후 실패 Job을 재실행하여 별도 로그로 기록. 무한 자동 재시도 금지.
- Human approval: 작업 실행은 사전 승인됨. **M0-002 Exit Gate PENDING / M0 전체 미완료**.

## M0-002 — GitHub Actions Codex plan-only 재실행 PASS (2026-11-04)

- Run: [#41692832207, attempt 2](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41692832207)
- GitHub Actions: 결론 **success**, Codex 실행·JSON 검증·범위 검사·Artifact 업로드 단계 모두 SUCCESS (job `126120208006`).
- Artifact: `m0-002-codex-plan-only` ID `13259075926`, SHA-256 `f059a39c696758d0e64ef57e5fb6f077c72be2e4496ffc02b24e285f186238d1` (ZIP digest, GitHub 제공), 자동 보관 30일. JSON 추출본은 [harness/reviews/M0-002-plan-only-result.json](reviews/M0-002-plan-only-result.json)에 복사해 지속 보관.
- PASS: OpenAI API 키 변경 후 인증/실행, Codex `PLAN_ONLY` 응답, structured JSON 스키마 계약 체크, `android/`·`backend/` 미생성, Git 변경 없음 검사, 결과 Artifact 업로드.
- 검토: Codex가 `task_id=M0-002`, `current_milestone=M0`, `mode=PLAN_ONLY`, `m1_permitted=false`, `human_gate=REQUIRED`, `android_tests=NOT_RUN`을 반환함. M1 실행을 시작하지 않았으며 다음 단계는 계획 후보만 작성.
- FAIL/복구: 최초 attempt 1은 401 Unauthorized; 해당 이력은 별도 섹션으로 보존. attempt 2는 성공. Secret 원문은 취급하지 않음.
- NOT_RUN: Android 빌드·Lint·Unit/UI 테스트(프로젝트 미생성), Claude Code import(선택 호환성), Android 실기기 성능(해당 단계 아님).
- M0 Exit Gate: 문서 기반 초기화, 승인·기록 정책, 실제 계획-only Codex 호출과 보고 계약, 범위 검증을 충족. M0은 VERIFIED. 후속 M1 구현은 M1 SDD 및 공식 호환성 확인 후 별도 작업으로 진행.
- Human approval: 사용자 포괄 승인 + 검증 증거에 따른 M0 Gate 적용.

## M1-001 — Android Native Foundation SDD·호환성 조사 (2026-11-04)

- Approved scope: M1 SDD/ADR 및 현재 상태 기록. Android 프로젝트 생성은 후속 M1-002로 분리.
- Source of truth: [M1 SDD](../docs/specs/M1-android-foundation.md), [ADR-0002](../docs/adr/ADR-0002-android-foundation-stack.md).
- Research: Android AGP 9.3 release compatibility, Kotlin KGP 2.4.21 compatibility table, Compose compiler migration, built-in Kotlin, Compose BOM official documents (URLs in ADR/SDD).
- Result: AGP 9.3.3 / Kotlin 2.4.21 is an **unverified candidate**; Kotlin's documented max fully supported AGP is 9.3.1, so CI verification is mandatory before fixating. AGP 9.3.3 includes fixes absent in 9.3.1. Do not infer compatibility.
- PASS: Evidence-backed version *candidate* and SDD contracts/Out of Scope recorded.
- NOT_RUN: Android project, Gradle, lint, unit tests, device install, UI tests (no project yet).
- Next: M1-002 minimal Gradle/Compose multi-module skeleton and clean build/test in Github Actions, with API key not exposed to build tests.

## M1-002 — First generation PASS, SDK bootstrap FAIL (2026-11-04)

- Workflow: [Run #41696126585](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41696126585), commit `eeb33249e0f8b2db727ce55169270cbeb3468559`.
- Generate job: PASS. Codex created 3 Android modules and test sources, with JSON report verified and no out-of-scope writes. Artifact `m1-002-generated` (ID `13260921238`) is preserved for 14 days.
- Verify job: FAIL at `android-actions/setup-android@v3`: its SDK manager invocation tried to install an unavailable legacy package named `tools` and returned exit code 1.
- NOT_RUN: Android SDK 37 installation, Gradle wrapper, assembleDebug, lintDebug, unit tests, device installation and UI tests.
- Publish: SKIPPED because CI verify failed; no Android source PR was published.
- Repair: use preinstalled GitHub runner Android SDK manager directly, then run actual Gradle gates. Do not claim source compile success before test results.

## M1-002 — SDK package ID mismatch and artifact reuse (2026-11-04)

- Second run: [#41696988901](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41696988901), commit `7413145fb00cab842bc60575570711fcf40a71d1`.
- PASS: Codex source generation job; SDK manager discovery and initialization.
- FAIL: SDK Manager install of `platforms;android-37` returned 'Failed to find package'; Android 17 SDK is published as `platforms;android-37.0` (see https://developer.android.com/about/versions/17/setup-sdk and package identifier notes in upstream https://github.com/flutter/flutter/issues/192478).
- NOT_RUN: Gradle wrapper, assembleDebug, lintDebug, unit tests, device.
- Source Artifact: `m1-002-generated` ID `13260927048`; published no code PR because build gate failed.
- Repair: add verify-only GitHub Actions job using preserved Artifact without calling OPENAI_API_KEY again; update SDK package identifier to `platforms;android-37.0`. Disable auto-push trigger on code generation workflow, leaving manual dispatch only, so infrastructure fixes do not repeat paid Codex runs.
- Next: inspect verify-only CI build and publish first M1 source PR only if green.

## M1-002 — Gradle 검증 PASS, 소스 게시 단계 FAIL (2026-11-04)

- Run: [#41698095765](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41698095765) (기존 Codex artifact 재사용, API 키 없이 검증).
- Verify job 126133860860 **PASS**: Android SDK 37.0, Gradle Wrapper 생성, `assembleDebug`, `lintDebug`, `testDebugUnitTest`; 실제 `:feature:live:testDebugUnitTest` 수행, `:app:testDebugUnitTest`는 NO-SOURCE.
- Publish job 126136197146 **FAIL**: `git diff --cached --check`가 공식 Gradle Wrapper에서 생성한 Windows `android/gradlew.bat`의 CRLF를 trailing whitespace로 탐지하고 exit code 2. Git push/PR은 실행되지 않음.
- NOT_RUN: Emulator/실기기 설치, UI instrumentation, M1 전체 Gate.
- Scope-limited fix: `git -c core.whitespace=cr-at-eol diff --cached --check`로 CR at EOL을 유효한 줄끝으로 처리하며 다른 공백 검사 유지. 별도 로컬 재현에서 기본 diff --check는 오류, `cr-at-eol` 적용 검사는 PASS.
- Recovery: 수정된 CI로 보존된 소스 artifact를 다시 검증한 뒤 새 소스 PR 생성. API 비용 발생하는 Codex 재생성은 금지.

## M1-002 — Verified Android source ready for review (2026-11-04)

- Build source: preserved Codex artifact from Run #41696988901; no additional Codex API invocation.
- CI PASS: https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41699243951 verify job #126137862506: assembleDebug/lintDebug/testDebugUnitTest.
- Publish source: validated artifact from that run; Android .gitignore extra blank newline removed without changing source code.
- Original plan/report: harness/reviews/M1-002-codex-report.json.
- NOT_RUN: emulator/device install and manual navigation UX; M1 Gate pending.
- Human review: requested via PR.

## M1-002 — Verified source branch pushed and independent PR created (2026-11-04)

- GitHub publishing attempt [Run #41699908603](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41699908603).
- PASS: 이전 [Run #41699243951](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41699243951)의 Verify job 성공 재검증, 검증된 Artifact 다운로드, `android/.gitignore`의 불필요한 마지막 빈 줄 한 개 정리, `git -c core.whitespace=cr-at-eol diff --cached --check`, Git commit `c542bd220f5a18fb814c274bafc9dd78c3acf201`, 브랜치 `feat/m1-002-android-foundation-verified` 원격 Push.
- FAIL: `gh pr create`는 GitHub Repository의 Actions 봇 PR 생성 금지 정책으로 실패. 빌드/테스트 실패가 아니다.
- Recovery: GitHub 연결의 별도 사용자 권한으로 [PR #11](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/11)을 생성. GitHub Actions의 PR 생성 제한 설정은 변경하지 않음.
- Code review: 범위 `app/core:model/feature:live`를 점검. ViewModel load cancel/late response 방어, Error/Empty/Loading/Content, UI navigation placeholder, fake repository unit tests를 확인. 출시용 media/socket은 없으며 아직 M1 단계에 맞는 구조.
- NOT_RUN: Emulator/UI instrumentation 및 실기기 동작, 장기 PR 자동 검증 (M1-003 예정). 따라서 M1 전체 Exit Gate는 미완료.

## M1-003 — Persistent Android PR CI setup (2026-11-04)

- Change scope: `.github/workflows/android-ci.yml` for `pull_request` / `push(main)` / manual execution. No app code change.
- Gates: JDK 17, Gradle 9.5.0, Android SDK `platforms;android-37.0` via preinstalled SDK manager, `:app:assembleDebug`, `:app:lintDebug`, `testDebugUnitTest` plus log/test XML artifact.
- Security: `permissions: contents:read`, checkout credentials not persisted, `OPENAI_API_KEY` never injected; untrusted PR may run code but cannot access project secrets through this job.
- NOT_RUN: New workflow validation pending first GitHub Actions Run; Emulator/manual UI/physical device tests are M1-004.
- Next: Review PR and capture actual CI result, then start instrumentation SDD/test work.

## M1-003 — Persistent Android PR CI evidence/review (2026-11-04)

- Scope: [PR #12](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/12), `.github/workflows/android-ci.yml`, source commit `424b1c4d3917f1fa2175364808743bb0c555c3b4`. Existing Android source is from merged [PR #11](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/11) (`65d4aefb3b2d08a778b710c1938fbd2883286abb`). No new code generation or OpenAI API-key request.
- Actual CI: [Run #41700593263](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41700593263), job [build-lint-unit #126142531399](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41700593263/job/126142531399), conclusion `success`. Environment: ubuntu-24.04; Temurin JDK 17, Gradle 9.5.0, Android SDK `platforms;android-37.0` installed using preinstalled SDK Manager, AGP 9.3.3 reported by lint.
- Actual commands (working directory `android/`): `chmod +x gradlew`; `./gradlew --version`; `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace` (stdout/stderr piped to `tee "$GITHUB_WORKSPACE/android-ci.log"`; shell `set -euo pipefail` preserves failures).
- PASS: `:app:assembleDebug`, `:app:lintDebug`, `:feature:live:testDebugUnitTest`; Gradle `BUILD SUCCESSFUL in 1m 13s` / `93 actionable tasks: 93 executed`. XML report `FakeRoomRepositoryTest`: 2 tests, `RoomListViewModelTest`: 5 tests. **Total 7 tests; failures=0, errors=0, skipped=0**. `:app:testDebugUnitTest` is explicitly `NO-SOURCE`, not counted as an executed test.
- Lint result **PASS task with 13 nonfatal WARNINGS**, not clean of warnings: targetSdk lower than newest (1), obsolete AGP (3), outdated Gradle dependencies (5), newer library version (2), missing data extraction rules (1), missing application icon (1). Review these before final release; no lint gate disabled or suppressed. Node 20 action deprecation warning occurred for v4 actions, but did not fail this run.
- Artifact verified: `android-pr-gate-evidence`, ID `13262019517`, expires 2026-11-17 19:11 UTC, GitHub digest `sha256:b57910dcb1814466595a3f25f4ba3fd33ee7fe106e3e568259092245758bac17`; downloaded ZIP hash matches GitHub digest. ZIP contains raw `android-ci.log`, app lint HTML, and the two JUnit XML suites. Original job log was reviewed independently; all verify/upload steps concluded success.
- Security/review: PR-only and main-push gates use `contents: read`, checkout `persist-credentials: false`, no `OPENAI_API_KEY`/publishing token. `workflow_dispatch` is manual but this Android gate has no API billing. Same workflow source revision was verified before this documentation-only commit. No unreviewed Android source modifications in this M1-003 change.
- FAIL: none in M1-003 CI. NOT_RUN: Android Emulator/device install, Compose UI instrumentation, navigation/rotation/manual UX, performance/live HLS, M1 overall Gate. Do **not** mark M1 VERIFIED on this result. Next authorized unit: M1-004 device/emulator test proof.

## M1-003 — PR #12 squash merge and main push quality gate (2026-11-04)

- Scope: Merge [PR #12](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/12) after reviewing workflow diff, security boundaries, previous PR evidence, and the latest complete PR CI [Run #41705829627](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41705829627). Merge method: squash with expected PR head SHA `0943b75de12b3e7b8904c35962bdfcdf3de615ca`; confirmed `main` merge commit `ecbd717ef97e78cdf1bd8f68b58f2100c3f21ec8`. No additional Codex or API-key invocation.
- Verified pre-merge artifact: Run #41705829627, ID `13263729427`, downloaded ZIP SHA-256 `cbc28a6e717b990e3d4cbd7d91f4620bab94042a86672258b3f8b97540a321d6` matched GitHub digest; 7 unit tests, 0 failures/errors/skipped, 13 lint warnings.
- Main after merge: [Run #41715334883](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41715334883), `push(main)`, head `ecbd717ef97e78cdf1bd8f68b58f2100c3f21ec8`, job [#126193724321](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41715334883/job/126193724321); workflow/job conclusion **success**.
- Actual command (GitHub Actions `ubuntu-24.04`, JDK 17, Gradle 9.5.0, SDK `platforms;android-37.0`): from `android/`, `chmod +x gradlew`, `./gradlew --version`, and `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/android-ci.log"` under `set -euo pipefail`.
- **PASS**: Android `:app:assembleDebug`, `:app:lintDebug`, `:feature:live:testDebugUnitTest`. Original job log: `BUILD SUCCESSFUL in 55s`, 93 tasks executed; Android `:app:testDebugUnitTest` is `NO-SOURCE`, not an executed test. Downloaded XML suites: `FakeRoomRepositoryTest` 2 tests, `RoomListViewModelTest` 5 tests; total **7 tests, 0 failures, 0 errors, 0 skipped**. Downloaded lint HTML: **13 nonfatal warnings**, not warning-free.
- Main artifact: `android-pr-gate-evidence`, ID `13269957072`, ZIP SHA-256 `eee5760ab3c8e25d9157b882322f5d05500bfcdd858dbf85b077a4af376e9ca6`, downloaded hash matches GitHub digest. ZIP contains `android-ci.log`, lint HTML, and 2 test XML files. Original job log also inspected.
- **FAIL**: none in merge or main CI. **NOT_RUN**: emulator/physical device install and launch, Compose instrumentation/navigation, rotation/process recovery, HLS LIVE and physical-device performance measurements. M1 milestone Gate **not** VERIFIED; M1-004 remains pending. Existing lint warnings and GitHub Actions Node20 deprecation warning are technical debt; tests/lint gates were not disabled.
- Documentation-only evidence follow-up updates `docs/STATUS.md` and `harness/build-log.md`; no additional workflow execution intended. Next small unit: M1-004 device/emulator test design and real execution, under a separate approved work PR.

## M1-004 — Emulator Compose instrumentation PR proposal (2026-11-04)

- Base: `a4d06a0246e3c2b865ef6d4d45fd3ef97ae9fad7` (main, PR #13); before work checked MASTER_PLAN, STATUS, M1 SDD, this log, latest PRs and Actions. Open PRs: none; latest completed Android main CI [#41715334883](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41715334883) success; no in-progress Codex Action observed.
- Scope: one proposed M1-004 Compose list → detail → back smoke, on API 35 emulator, with instrumentation-runner Gradle dependency wiring. Android feature implementation unchanged. Workflow job only runs after existing build/lint/unit gate; checkout/token read-only, no OPENAI_API_KEY.
- Files: `.github/workflows/android-ci.yml`, `android/app/build.gradle.kts`, `android/gradle/libs.versions.toml`, `android/app/src/androidTest/kotlin/dev/fanstage/android/RoomNavigationSmokeTest.kt`, `docs/specs/M1-android-foundation.md`, `docs/adr/ADR-0003-emulator-compose-smoke.md`, `docs/STATUS.md`, `harness/build-log.md`.
- Intended CI command (not yet executed at proposal time): `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace` from `android/`, plus `adb devices -l`, `adb shell getprop ro.build.version.sdk`, `adb shell pm path dev.fanstage.android`; see exact workflow for shell and selected logcat collection.
- PASS: static read-only GitHub inspection of closed previous PRs and completed main CI; no new Android/Gradle task run locally in this proposal.
- FAIL: none from this proposed change **yet**, not a prediction of future CI success.
- NOT_RUN: new instrumentation build, emulator boot, APK installation, navigation assertions, rotation, real physical device, LIVE playback, device performance, M1 full Gate.
- Run URL / artifact / source commit: **PENDING until PR-triggered workflow actually runs**, then inspect GitHub job logs and downloaded test XML; do not retroactively mislabel this proposal as executed.
- Security: GitHub Actions generation/build/publish roles remain separated; this PR invokes no Codex generation and does not touch API key values.
- Next: verify the single PR-triggered workflow and record true PASS/FAIL with original artifact references; merge only after review and risk assessment.


## M1-004 — confirmed first CI failure and smallest shell repair (2026-11-04)

- Prior [PR #14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14) source `b67cd7dfc523434eec213445ddd83d8575573c3e`, [Run #41724518589](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41724518589) **FAIL**. Original [build job #126225684613](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41724518589/job/126225684613) log **PASS** (`BUILD SUCCESSFUL in 2m 9s`, existing `:app:assembleDebug :app:lintDebug testDebugUnitTest`); build evidence artifact id `13274595433`, GitHub-provided `sha256:8357a13b647fede372aaa0c145ce33fd8373c387450176c86c209c617b88c920` (metadata only, local checksum not independently verified).
- Original [emulator job #126227097645](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41724518589/job/126227097645) log showed API 35 emulator **boot PASS** then `/usr/bin/sh -c set -euo pipefail` -> `/usr/bin/sh: 1: set: Illegal option -o pipefail`, process exit 2. **FAIL** workflow shell. No connected test XML, no emulator evidence artifact. **NOT_RUN** `:app:connectedDebugAndroidTest`, actual APK installation, list→detail→back UI checks, physical-device install, rotation, LIVE/performance and M1 Gate.
- Independent review: Codex PR #14 P1 thread `4460818454` reproduced the action's per-line `sh -c` behavior and warned that deleting `pipefail` could mask errors behind `tee`. Compared review with original job log before changing anything; leave thread unresolved until CI evidence.
- One bounded fix: `.github/workflows/android-ci.yml` switches emulator runner to one command `bash ../scripts/m1-004-emulator-smoke.sh`; new `scripts/m1-004-emulator-smoke.sh` executes in one Bash process, `set -euo pipefail`, SDK 35 assertion, adb logcat EXIT trap, Gradle connected test with `tee`, and installed package check. Additional evidence documents: `docs/STATUS.md`, `docs/specs/M1-android-foundation.md`, `docs/adr/ADR-0003-emulator-compose-smoke.md`, this log. No test or gate disabled.
- Actual commands reviewed in existing GitHub job logs: `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace` **PASS**; emulator runner `sh -c 'set -euo pipefail'` **FAIL**. Repair command `bash ../scripts/m1-004-emulator-smoke.sh` and `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace`: **NOT_RUN** until repair PR CI, not claimed PASS. Run URL and repair commit: **PENDING on branch update**, then record in PR.
- No Codex generation call, no secret access, no external server, no new milestone functionality. Next: inspect **one** repair-triggered Android CI workflow and original test XML/logs. Never merge merely because emulator boots.


## M1-004 — second PR run FAIL at AndroidTest import and one-line repair (2026-11-04)

- Existing [PR #14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14), source `1687a88b97733ae0c4e73af0e5579ca8c8584df8`, [Run #41734088258](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41734088258), emulator job [#126259783403](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41734088258/job/126259783403). Before editing, read MASTER_PLAN, STATUS, M1 SDD/ADR, prior build-log, all open PRs and latest Actions; only PR #14 open; this run completed in **failure**, no overlapping generation or active workflow observed.
- Original job logs reviewed: `build-lint-unit` **PASS**; emulator boot/API35 and Bash execution **PASS**; `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/android-emulator-test.log"` exits **FAIL** during `:app:compileDebugAndroidTestKotlin` at `RoomNavigationSmokeTest.kt:3:33 Unresolved reference 'assertExists'`. Gradle `BUILD FAILED in 1m 32s`; AndroidTest compilation and workflow **FAIL**.
- Original artifact metadata: `android-emulator-compose-smoke-evidence` id `13278516563`, GitHub SHA-256 `e25d5d0716c216a6da2a53a8196c95eecb01a6edaf5b03a6915545d0f22c6e5e`; `android-pr-gate-evidence` id `13278042808`, GitHub SHA-256 `92af6934c2f391c924665aef22ca317449e2752937601ce50d3be7ab0f8e7ea7`. These are **GitHub metadata only**; ZIP contents/checksums not independently downloaded in this work unit. Job raw log conclusively shows compile failure, not failed Compose assertion.
- One-line patch: remove invalid `import androidx.compose.ui.test.assertExists` from `android/app/src/androidTest/kotlin/dev/fanstage/android/RoomNavigationSmokeTest.kt`; retain its `...assertExists()` method call, assertion coverage and original failure gates. SDD/ADR/STATUS/evidence log updated; no application runtime changes, no API key accessed, no new Codex call.
- PASS: static source/API analysis of import mismatch and review of raw existing job log. FAIL: prior emulator CI run (see above). NOT_RUN: post-patch compilation, APK installs, Compose list/detail/back, physical device, rotation, LIVE/performance, M1 Gate. New Run URL and result **PENDING** until PR-triggered CI executes; verify source commit and independent test XML before merge.


## M1-004 — third CI run: instrumentation PASS, post-test shell FAIL (2026-11-04)

- [PR #14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14) code commit `1099d9d812e07efdca23fe5b1228956f620ccea0`; [Run #41741160963](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41741160963) **workflow FAIL**. No Codex generation/API key usage and exactly one PR Android CI run triggered by this unit. Docs-only evidence follow-up does not match Android CI `paths` filters.
- [build-lint-unit job #126282723465](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41741160963/job/126282723465) **PASS**; command `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace` from `android/`. Downloaded original artifact `android-pr-gate-evidence` id `13281490747`: ZIP SHA-256 `7f57a06215c117a98512817e6f3858fbb69d910b16a006ee206fd6ea4ac190da` independently matches GitHub digest; XML FakeRoomRepositoryTest 2 and RoomListViewModelTest 5, total **7 tests / 0 errors / 0 failures / 0 skipped**, Gradle BUILD SUCCESSFUL.
- [Emulator job #126283801264](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41741160963/job/126283801264) **FAIL**. Original raw job log proves API35 emulator boot, then single Bash script (`bash ../scripts/m1-004-emulator-smoke.sh`) executed command `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace 2>&1 | tee "$GITHUB_WORKSPACE/android-emulator-test.log"` and Gradle **BUILD SUCCESSFUL in 3m 3s**, 88 tasks executed. `:app:compileDebugAndroidTestKotlin` compiled (one deprecation warning for Compose test v1 API); test APK packaging and connected test ran.
- Downloaded original `android-emulator-compose-smoke-evidence` id `13281616538`: ZIP SHA-256 `8b1c623c9105f3b9fd92aa32df0cdb8c82e417da58815e8bb1258e9da394b2fb` independently matches GitHub digest. Connected XML `TEST-test(AVD) - 15-_app-.xml` shows `tests=1, failures=0, errors=0, skipped=0` for `dev.fanstage.android.RoomNavigationSmokeTest.roomListOpensDetailAndReturns`; raw AndroidJUnitRunner `OK (1 test)` and `test-result-exit-code.txt = 0`. Logcat `ActivityTaskManager` records actual MainActivity `START` and `Displayed` on emulator. Thus **AndroidTest code, app launch, tested Compose navigation PASS**.
- After Gradle returned success, workflow's one Bash script exited 1 at the final `adb shell pm path dev.fanstage.android | grep '^package:'` assertion. Original job reports `The process '/usr/bin/sh' failed with exit code 1`. Neither stdout nor artifact proves whether the package was uninstalled by test teardown or an adb/package query problem occurred; the late probe is the identified brittle gate. **Overall workflow FAIL**, not eligible for PR merge despite passing test XML.
- PASS: build/lint/7 unit tests, API35 emulator boot, AndroidTest APK build and 1/1 connected test, MainActivity start/display; artifact ZIP SHA verification. FAIL: post-test package-path shell check / overall CI. NOT_RUN: real physical device, rotation/process recreation, LIVE video, performance baselines, M1 full Gate.
- Next small work unit (new CI run only in next execution): replace/reposition final post-test package-path check with proof from actual XML and launch result without masking Gradle or test failures. Leave PR #14 open, preserve original artifacts and do **not** disable any tests. Files changed in this documentation-only follow-up: `docs/STATUS.md`, M1 SDD, ADR-0003 and this log; code revision remains `1099d9d812e07efdca23fe5b1228956f620ccea0`.

## M1-004 — fourth CI evidence and narrow post-test repair (2026-11-04)

- Before edit: inspected MASTER_PLAN, branch STATUS, current M1 SDD/ADR, this log, open PR #14 and recent Actions. No active Codex action or Android CI. Docs-only PR commit 9970e56ef81a7a4b4105afe358b167c9e45e2cd7 unexpectedly triggered Run https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212 because PR paths are calculated over its full diff.
- Raw original [build-lint-unit job #126286995066](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212/job/126286995066) PASS: command ./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace; BUILD SUCCESSFUL in 2m 21s, 93 tasks.
- Raw original [emulator job #126288426133](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212/job/126288426133): API35 emulator boot PASS, command ./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace PASS, BUILD SUCCESSFUL in 2m 51s (88 tasks), one test finished. Post-test adb shell pm path dev.fanstage.android | grep ^package: exited 1, whole workflow FAIL. Package-path failure's exact cause unproven.
- Original [artifact #13281658056](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41742429212/artifacts/13281658056) ZIP downloaded and SHA-256 independently checked: 0d929058d51fcd17f12600eaefe6e31f0b62fb7473b070f30dbacae8fd6786d9 (matches GitHub). XML: one RoomNavigationSmokeTest.roomListOpensDetailAndReturns, failures/errors/skips=0. Runner exit file=0; logcat includes MainActivity START/Displayed and TestRunner finished. Build artifact #13282631214 GitHub digest df62caf5f1e36d58b7ea54b33a2acab43f590e99219cb3826f89e27f012ba559 (metadata only, not independently downloaded).
- One small change: scripts/m1-004-emulator-smoke.sh replaces only the post-test package query with Python3 XML and runner-exit assertions. Unchanged: Gradle command, shell pipefail, test, CI workflow/jobs, logcat and artifacts. Offline replay command python3 /mnt/data/check_m1_xml.py on extracted original ZIP: PASS; mutated XML tests=0: exit 1, positive negative-control PASS. Local scratch path is not a repository file.
- PASS: original Gradle/connected UI and offline validator checks. FAIL: prior whole Run #41742429212. NOT_RUN: new patch-triggered Actions result, physical device, rotation/process recovery, LIVE, performance and M1 Gate. Commit/Run URL pending branch update; document actual new verification in PR comment, not a second doc-only PR commit (which would trigger another Actions run).

## M1-004 — PR #14 squash merge, independent main CI proof (2026-11-04)

- **Scope/PR:** [#14](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/14) squash merged after code diff, resolved Codex P1 thread, and successful [PR Run #41748827365](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41748827365) were reviewed. Merge commit `ede28dc8ef8416986a586d3c41ca7a5617b845fb`, PR head `953aba5fb4e419fcd266ce9018164e860d847c77`.
- **Post-merge verification:** [main push Run #41755701493](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493), head `ede28dc8ef8416986a586d3c41ca7a5617b845fb`, completed SUCCESS; build job [#126331170470](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493/job/126331170470) and emulator job [#126332262156](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493/job/126332262156) both PASS.
- **Actual commands:** from `android/`: `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace` under Bash `set -euo pipefail` with `tee`; emulator runner invokes `bash ../scripts/m1-004-emulator-smoke.sh`, which runs `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace` plus strict connected XML and instrumentation exit-code verification. Ubuntu 24.04, JDK 17, Gradle 9.5.0, compilation SDK package `platforms;android-37.0`, emulator API 35 x86_64 Google APIs.
- **PASS:** main Android Gradle build/lint/unit `BUILD SUCCESSFUL in 2m 13s`, 93 tasks; two unit XML suites total 7/7 tests, failures=0/errors=0/skipped=0. Emulator boot, `:app:connectedDebugAndroidTest` `BUILD SUCCESSFUL in 1m 48s`; `RoomNavigationSmokeTest.roomListOpensDetailAndReturns` 1/1, failures=0/errors=0/skipped=0; result-exit-code file `0`; original TestRunner logcat shows 1 completed, 0 failed.
- **Artifact integrity:** unit `android-pr-gate-evidence` ID `13288307072` ZIP SHA-256 `bae7921d913d1b6f76ace26bef660f673587288c8f62913af50496e840d5f7ec`; emulator `android-emulator-compose-smoke-evidence` ID `13287838197` ZIP SHA-256 `6ad1fedf5d8d7479be1577f5901389ac8afd238e7cec164c36e4765279a01cec`. Both independently downloaded/hashed; match GitHub artifact digests. Original job logs/XML/exit file inspected, not just green badges.
- **Warnings/limitations:** Android Lint task PASS with **13 nonfatal warnings**; GitHub Actions Node20 deprecation and `setup-java@v4` warning remain. **FAIL:** none in this main CI. **NOT_RUN/blockers:** clean build on local developer machine, physical-device install, rotation/process recreation, LIVE streaming, release-like device performance; M1 full Exit Gate **NOT VERIFIED**. No Codex generation, API key reads or paid infrastructure in this work unit. Only one Android Actions run was triggered by merging PR #14; evidence-only follow-up is outside Android path filters.


## M1-005 — Activity recreation in emulator navigation smoke (2026-11-04)

- Starting point: `main` `3172bfb715d911d0fb0b2c8c5c16c5a69ec25459`. Read `docs/MASTER_PLAN.md`, `docs/STATUS.md`, M1 SDD, build-log, open PR list and latest Actions; no open PR or running workflows at inspection. Previous [main Run #41755701493](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41755701493) finished SUCCESS; independently re-read original two job logs, jobs and artifact metadata. Build/unit/emulator jobs PASS; artifacts 13288307072/13287838197 report matching previously verified SHA-256 digests. No new download/hash performed in this work unit.
- Scope: append **one lifecycle probe** to existing `RoomNavigationSmokeTest.roomListOpensDetailAndReturns` (test source commit `694ac1bcf147a27213c3c15f2e523f65c652e5d5`). `ActivityScenario.recreate()` creates a new Activity; retained detail route and list Back behavior must still pass. No tests disabled, no new test count, no Android implementation, Gradle dependency or workflow modification.
- Files: `android/app/src/androidTest/kotlin/dev/fanstage/android/RoomNavigationSmokeTest.kt`; `docs/adr/ADR-0004-m1-activity-recreation.md`; `docs/specs/M1-android-foundation.md`; `docs/STATUS.md`; `harness/build-log.md`.
- Existing CI command and expected validation (workflow unchanged): `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace`; `bash ../scripts/m1-004-emulator-smoke.sh` invokes `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace` and strict one-case XML/runner-exit verification.
- **PASS**: source-only review of module direction (`:app → :feature:live → :core:model`), test assertion path, existing CI gate and raw prior main CI jobs. **FAIL**: no M1-005 failure known before execution. **NOT_RUN**: new lifecycle instrumentation and new PR CI (pending), local clean build, physical device, explicit rotation, process death/relaunch, HLS LIVE, release-like performance; M1 overall Exit Gate remains **NOT VERIFIED**.
- Security/cost: `OPENAI_API_KEY` not read or passed; no Codex invocation; Android PR CI is read-only. One PR-triggered Android Actions run maximum; never rerun automatically after a failure in this work unit.
- New M1-005 PR / Run URL / final branch SHA: **PENDING PR creation and GitHub Actions**; complete actual outcome in PR conversation without pushing another evidence-only commit onto an Android-containing PR (which would retrigger CI).

## M1-005 — PR #16 squash merge and post-merge main CI proof (2026-11-04)

- Before merge, read `docs/MASTER_PLAN.md`, `docs/STATUS.md`, `docs/specs/M1-android-foundation.md`, `harness/build-log.md`; inspected the sole open PR #16, source patch (five files), review status and [PR CI #41763099229](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41763099229). Codex review summary for exact head `dc49a4a54f6aad3848b159140f26938b414ae764` was completed with no findings; no unresolved threads. PR CI original build/emulator logs independently inspected. PR artifact ZIPs **downloaded and SHA-256 checked in this work unit**: unit artifact `13291522529` = `90e46205fae700940b8d83e60de826975a9c110dbf5c5df705c9f1c57b0e8e90` (JUnit 2+5 tests), emulator artifact `13291438088` = `a154cfd1d5030456901645034ddc50234ce3aa2ef03fbc18c1b4222ff5e4d4ff` (Compose 1 test, instrumentation exit 0). All match GitHub digests; no failure/skip.
- **Merge action:** `merge_pull_request` PR #16 with `merge_method=squash` and exact expected PR head SHA; GitHub reports merged=true, merge SHA `e22bb4042173d6b0160b9d46c50837cbfde6bb56`. No new Codex code generation or external server. New post-merge [main push Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) is the **only new Android Actions run** triggered in this bounded work unit, attempt 1, completed **SUCCESS**. No retry.
- **Actual runner commands** (unchanged, from `android/`): `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --max-workers=2 --stacktrace`; emulator runner calls `bash ../scripts/m1-004-emulator-smoke.sh`, executing `./gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2 --stacktrace` and strict connected XML and runner exit-code checks. Runner ubuntu-24.04/JDK17, Gradle9.5, compile SDK package `platforms;android-37.0`, API35 x86_64 emulator.
- **PASS:** main build-lint-unit job [#126372936584](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235/job/126372936584): Gradle `BUILD SUCCESSFUL in 1m 11s`, 93 tasks, unit test XML `FakeRoomRepositoryTest` 2/2 + `RoomListViewModelTest` 5/5 = **7/7**; failures/errors/skips=0. Original log read. Artifact `android-pr-gate-evidence` ID `13294470605`, downloaded ZIP SHA-256 `59785a4877a10b4b0a403cf4dbc45b197d651c1f52be5cc32cbaf940f8870d0f`, matches GitHub digest.
- **PASS:** main emulator job [#126373527054](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235/job/126373527054): API35 emulator connected instrumentation `BUILD SUCCESSFUL in 1m 15s`, 88 tasks; log `Finished 1 tests` and strict `PASS: verified connected Compose XML and runner exit`. Downloaded artifact `android-emulator-compose-smoke-evidence` ID `13294461031`, SHA-256 `60dad5ab02b811bb36a654d278f6a358d1b68b2c0f9376d1a8c06af0a616ef70` matches GitHub. Raw connected `TEST-test(AVD) - 15-_app-.xml`: 1 test, failures/errors/skips 0; `test-result-exit-code.txt` = `0`. Original job log read.
- **Changed code (already merged):** `android/app/src/androidTest/kotlin/dev/fanstage/android/RoomNavigationSmokeTest.kt` added explicit `ActivityScenario.recreate()`, changed Activity identity and restored detail/Back checks. Original PR also changed M1 SDD, ADR-0004, STATUS, build-log. This evidence-only PR changes those four docs (no Android/workflow paths); Android CI path filters apply only to `android/**` and workflow YAML.
- **FAIL:** none in PR or post-merge main workflow. **NOT_RUN/blockers:** clean local workstation build, physical device installation, explicit rotation, low-memory process-death/relaunch, real HLS LIVE playback, release-like performance, separate full M1 Exit Gate audit. Deprecated Compose rule factory and Node20/setup-java@v4 warnings remain; do not suppress them or claim 0 warnings. M1 Gate **NOT VERIFIED**, M2 not started. No `OPENAI_API_KEY` read, logged or passed to build jobs.

## M1-006 — Independent Exit Gate audit (2026-11-04)

- Base main commit: `fcb6144448c111422b979c621c69e5f17ed7caa3` (last Android code merge `e22bb4042173d6b0160b9d46c50837cbfde6bb56`). See [M1 audit](../docs/reviews/M1-006-exit-gate-audit.md) for source, test and blocker matrix. Documentation-only proposal changes `README.md`, `docs/test-matrix.md`, `docs/STATUS.md`, `docs/specs/M1-android-foundation.md`, audit report and this log; no Android source/workflow changes.
- **Actual inspection commands/actions:** GitHub repository `fetch_file` (MASTER_PLAN, STATUS, M1 SDD, log, README, android/settings/Gradle/Compose/ViewModel/test source), `search_prs is:open` (0), REST GET `actions/runs?per_page=15` (latest main #41768660235 completed SUCCESS; no running Codex/Android run), `fetch_workflow_run_jobs`, `fetch_workflow_run_artifacts` and raw `fetch_workflow_job_logs` for jobs 126372936584 and 126373527054. Local `command -v sdkmanager` and SDK path preflight: Android SDK not installed in automation runtime; did NOT launch Gradle. No API key read.
- **PASS (previous CI independently re-read):** main [Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235), Gradle assemble/lint/unit `BUILD SUCCESSFUL`, 7 JUnit tests from previously verified XML, emulator connected test `Finished 1 tests`/`BUILD SUCCESSFUL`, strict connected XML/exit validator PASS. Artifact IDs 13294470605 and 13294461031 remain available, matching previously recorded metadata digest; current run did **NOT** re-download or re-hash bytes.
- **PASS (new audit):** static three-module project dependency graph check, inspection of MainActivity/Compose/ViewModel/FakeRepository/test source and PR review history. **FAIL (new execution):** none, no tests newly attempted. **NOT_RUN / blockers:** developer-local explicit `./gradlew clean ...`; this environment has no Android SDK, physical-device install, real orientation change, process-death/relaunch. HLS LIVE M2 and release-like performance M6 not yet executed. Known 13 nonfatal Lint warnings and compatibility caveat remain.
- MASTER_PLAN M1 Exit Gate requires local clean **AND** CI, while old M1 SDD claimed `local or CI`; reconciled SDD to stricter rule. Local clean still missing and android/README remains stale (separate Android-path PR needed). **Overall M1 Gate: BLOCKED / NOT VERIFIED**, M2 prohibited. No new GitHub Actions Run (Run URL **NOT_RUN**, latest reviewed main Run above); no Codex generation or paid API invocation. Proposed audit commit SHA to be captured on PR (base `fcb6144448c111422b979c621c69e5f17ed7caa3`).

## M1-007 — developer-local clean build (2026-11-04)

- Scope: perform the requested local clean/assemble/lint/unit verification, repair a blocking wrapper permission issue, and record evidence. M2 remains out of scope.
- Source tested: clean `origin/main` base `7fbcc7108d87ecc03951de168c5b3aeb4c29ff52` with `android/gradlew` mode changed from `100644` to `100755` (script bytes unchanged). This record and the wrapper mode change are committed after the tested base; do not mistake the base SHA for the evidence commit.
- Environment: developer-local macOS 26.5.1, arm64; JetBrains Runtime OpenJDK 17.0.14+1-b1367.22; Gradle wrapper 9.5.0; AGP 9.3.3; Kotlin 2.4.21; compile SDK 37.0 and build-tools 36.0.0. Existing SDK lacked API 37; installed `platforms;android-37.0` using SDK manager (Android CLI 1.0.15985488); existing build-tools 36.0.0 and platform-tools reused. No version or dependency changed.
- First attempt: requested `./gradlew` command exited **126**, `permission denied`, before Gradle started because the tracked wrapper mode was 100644. `chmod +x android/gradlew` repaired the mode; no checks were disabled.
- Command: from `android/`, with `JAVA_HOME` set to the installed JDK 17 and `ANDROID_HOME` set to the local SDK, `./gradlew clean :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --stacktrace`.
- **PASS:** rerun exit **0**, `BUILD SUCCESSFUL in 1m 13s`, 96 actionable tasks (93 executed, 3 up-to-date); JUnit XML: `FakeRoomRepositoryTest` 2/2 and `RoomListViewModelTest` 5/5, zero errors/failures/skips. `:app:testDebugUnitTest` was NO-SOURCE. Lint passed with **0 errors, 13 warnings**; native library symbol stripping emitted a warning and packaged the library unchanged.
- Local evidence: `~/Library/Logs/FanStage/M1-007/clean-build.log`, SHA-256 `ecc46550faa9955b56032d317f67f1a69336108b500c7e778e5bd0e86e65e7ec`; SDK installation log `~/Library/Logs/FanStage/M1-007/sdk-install.log`. Local generated JUnit XML under `android/feature/live/build/test-results/testDebugUnitTest/`; lint report under `android/app/build/reports/`. These local artifacts are not committed and may be removed by future clean builds; the raw log may contain local paths.
- **FAIL:** initial command permission failure, fixed and successfully rerun as above. **NOT_RUN:** new emulator/device tests, actual rotation, process death, physical install, LIVE/HLS and performance. Existing CI evidence is distinct; no new CI result is asserted here.
- M1 overall remains BLOCKED / human approval pending: `android/README.md` documentation drift remains a separate task. No M2 work.


## Fast Track v1 정책 전환 및 M1 Gate 검토 (2026-11-04)

- 사용자 승인 범위: M0~M8 개발·commit/push/PR/merge 사전 승인, M9 제외. 이 작업에서는 M2 계획만 작성하고 기능 코드는 작성하지 않는다. 상시 과금·신규 보안 자격 증명·비가역 외부 작업은 별도 승인 대상이다.
- PR #19: head `591b562bbfd48fda9e9d87f65da03945d64ae443`, [CI #41790821988](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41790821988) 두 job PASS; squash merge `468065ae0f8fff0f41e48d5f8a07a99b94bcdf52`. 로컬 build-log SHA-256을 실제 재확인했다.
- PR #20: 최신 head `f8b3751b142e80d995942fca227439a8d3fcdb21`, [CI #41792416136](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41792416136) 빌드 및 에뮬레이터 job PASS; squash merge `d189e258e84537d9e2bfeb1a9eda72402171f69b`. PR #19 반영 후 README의 미병합 표현을 고치고 해당 head CI를 재검증했다.
- M1 핵심 Gate: 개발자 로컬 clean/assemble/lint/unit PASS; CI fresh runner에서 assemble/lint/unit PASS (CI에서 명시적 Gradle `clean`은 NOT_RUN). 소스 의존 방향 및 제한된 리뷰 PASS, API 35 emulator의 list/detail/recreate/back 1/1 PASS, README 및 환경 증거 정리. M1 핵심 완료로 평가하며 사용자가 Fast Track에서 별도 단계 승인을 면제했다.
- 여전히 NOT_RUN: 실제 회전, 시스템 프로세스 종료/복구, 실기기 설치, 로컬 외 환경 성능 및 LIVE 검증. 일부는 후속 M2/M5/M6 필수 Gate이며 이월 시 M8에서 재평가한다. 13개 Lint 경고 및 GitHub Actions deprecation 경고는 P2. 과거 검증·실패 기록은 유지한다.
- 정책: [ADR-0005](../docs/adr/ADR-0005-fast-track.md), [테스트 매트릭스](../docs/test-matrix.md); [M2 계획](../docs/specs/M2-hls-player.md). 기존 정책 문구의 현재 효력을 수정하고 역사적 초기 지시문은 과거 기록으로 표시했다.
- 수행: PR diff와 GitHub 체크 상태 검토, `shasum -a 256` 로컬 clean-build 원본 해시 확인, 문서 링크/범위 및 `git diff --check`. 본 정책 작업에서는 Gradle·에뮬레이터를 새로 실행하지 않았다. PR #19/#20의 CI 결과는 별도 실행의 결과다.

## M2 — Media3 HLS Player 기능 구현 (2026-11-04)

- Scope: [M2 SDD](../docs/specs/M2-hls-player.md) 및 [ADR-0006](../docs/adr/ADR-0006-m2-playback-lifecycle.md). Source base `e5adba83ea6b20db23372d30930f0d67e6f7c6c7`; actual feature commit is identified by the PR/Git history. No M3 transport.
- Environment: macOS 26.5.1 arm64, JDK 17.0.14, Gradle 9.5.0, Android compile SDK 37.0, local API 36 arm64 emulator, Media3 1.11.1. CI uses separate API 35 x86_64; its result is recorded on the PR and must not be inferred from local success.
- Command (from `android/`): `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :app:connectedDebugAndroidTest --no-daemon --stacktrace` with JDK 17 and local Android SDK. Exit 0, `BUILD SUCCESSFUL`, 124 actionable tasks (13 executed, 111 up-to-date). Unit XML 9/9 (Fake repository 2, ViewModel 5, playback session 2), no failures/errors/skips; app unit task NO-SOURCE. Lint 0 errors, 13 warnings.
- Emulator instrumentation 1/1 PASS: actual synthetic VOD reached playing; lifecycle background/pause, manual resume, Activity recreation and three repeated enter/exit cycles; missing-file error and manual retry; separate synthetic LIVE manifest without ENDLIST updated from two to three segments and Media3 played/refreshed its timeline. The local LIVE test uses a file-backed dynamic playlist, not a network broadcast. These are assertions within one instrumentation case, not multiple counted tests.
- Lifecycle evidence: after clearing logcat, Media3 `ExoPlayerImpl` logs showed seven unique `Init` and matching seven `Release` IDs. This supports release in tested flows; it is not a complete heap/Surface leak proof. `bash -n scripts/m1-004-emulator-smoke.sh` and `git diff --check` passed. CI smoke now retains and checks player ID pairing without removing the existing XML/exit checks.
- Local raw artifacts (not committed): `~/Library/Logs/FanStage/M2/verify.log` SHA-256 `8413583b259df5126839c5f6bf71bd23d9403473533d698bed88c0833bcb2795`; `~/Library/Logs/FanStage/M2/player-lifecycle.log` SHA-256 `2daffe81a32e5c07f0e29b07afc2a5a58ef4b8ff88378284df1900ed0904323c`. Log output may contain local paths; generated test XML/lint reports reside under ignored build directories.
- FAIL: initial attempted `./gradlew` preparation from incorrect working directory prevented a temporary test-file edit (no source loss); corrected and all final commands above passed. NOT_RUN: real physical-device playback, actual screen rotation, system process death, actual remote LIVE/network behavior, full heap leak profiling, release performance. Emulator Activity recreation is not real rotation/process death. M2 overall Gate remains NOT VERIFIED until physical-device evidence. P2: pre-existing 13 lint warnings and deprecated Compose test factory.
- PR CI first run [#41795720944](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41795720944), head `dfab2a2da61fbb350ef54dac4e6534d2b6858ec4`: build/lint/unit PASS and API 35 instrumentation Gradle PASS, but overall emulator job **FAIL** in the lifecycle-log validator. Downloaded original artifact `13305692423` shows five unmatched initializations from unrelated emulator processes (Media3 1.3/1.4) and seven matched 1.11.1 init/release pairs in instrumentation PID 4194. The original validator compared all emulator processes.
- Fix: correlate the exact instrumentation test's TestRunner process ID with Media3 log lines, preserving the minimum seven creations and exact init/release matching for that PID. Replay against the original CI artifact PASS; negative control removing one test-process release exited 1 as intended; `bash -n` PASS. The full updated CI rerun must pass before merge; no test or quality gate is disabled.
- Additional rendering assertion: `isPlaying` alone does not establish that a video frame reached the surface. The instrumentation now waits for Media3 `onRenderedFirstFrame` for the VOD UI. After that change, the same full local Gradle build/lint/unit/connected command passed on API 36 (exit 0, 124 tasks: 32 executed, 92 up-to-date), with seven matching player init/release IDs. Local raw logs: `~/Library/Logs/FanStage/M2/frame-verify.log` SHA-256 `6191907ecf6c368df19e684009bb99865c76649539c668a491f92a8bbf5e3ec5`; `~/Library/Logs/FanStage/M2/frame-release.log` SHA-256 `bc789ecacbc6f70e593d5e496a8012aaf49449d35bd3824327a195f3c8d756ff`. This strengthens VOD surface evidence; it does not claim real-device or remote LIVE validation.
- PR review identified that the non-scrollable detail screen could clip the manual playback control in a short landscape window. The detail content now scrolls. On local API 36, instrumentation changed the emulator from portrait to landscape and back, scrolled to the playback button and asserted it was displayed; the full build/lint/unit/connected command passed (exit 0, 124 tasks: 30 executed, 94 up-to-date). Nine player Init/Release IDs matched. Raw logs: `~/Library/Logs/FanStage/M2/rotation-verify.log` SHA-256 `7d48ed2378b9b8b6a88faa8bbb2c3363624f736af2ae70b7532d58d3e50197b0`; `~/Library/Logs/FanStage/M2/rotation-release.log` SHA-256 `5c719d48c308b3a3ef7e5af4c9cc2f01a030b40443ea36fc5c87a53fa4002d84`. This supersedes the earlier local-emulator rotation NOT_RUN for this tested case only; real-device rotation remains NOT_RUN.

## M2 — physical-device Exit Gate (2026-11-04)

- Scope: one connected USB Android physical device (SM-S908N, Android 16 / API 36); no device serial or personal data recorded in this repository. App source base `982d3293b3faf07524c4893617f3088e39b85554` plus this PR's PlayerPanel injectable source and instrumentation-only changes. No existing FanStage installation was present before testing. The default source remains the local synthetic VOD; test LIVE is a separate file-backed updating playlist, not a remote broadcast.
- **PASS (physical device):** JDK 17 `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :app:connectedDebugAndroidTest --no-daemon --stacktrace`; exit 0, unit 9/9, instrumentation 1/1, lint 0 errors / 13 warnings. The instrumentation case asserts VOD first rendered frame, lifecycle pause/manual resume, Activity recreation, real portrait→landscape→portrait, reachable controls, six room entries/exits, actual Media3 missing-source error and manual retry, the visible Retry button and separate synthetic LIVE timeline refresh/playing. The UI error test uses an injected missing source; production's default URI is unchanged.
- **PASS (actual HOME/process kill):** `python3 scripts/m2-physical-process-death.py` after installing the debug APK: actual HOME background paused, manual Play resumed, `adb shell am kill` terminated the stopped background app, and a different process restored the same detail in Paused state. This is controlled background process termination, not an actual system low-memory-killer event.
- **PASS (bounded resource evidence):** `python3 scripts/m2-physical-log-check.py "$HOME/Library/Logs/FanStage/M2-device/player-test-ui.txt" "$HOME/Library/Logs/FanStage/M2-device/surface-test-ui.txt"` verified 13 matched Media3 Init/Release IDs and 30 matched codec Surface connect/disconnect addresses from the instrumentation process. Separate actual Media3 controller/listener weak references were collected after release and main queue drain on this physical device; the resulting instrumentation case passed repeatedly. Log-check negative controls removing one player release or one Surface disconnect exited nonzero. These are evidence for tested paths, not a whole-heap or long-duration no-leak guarantee.
- **FAIL then retest:** initial added GC/reachability test failed `released controller retained` before the test drained the main queue and avoided holding weak-reference temporaries during GC. The exact cause was not conclusively established. After revising only the diagnostic test to drain pending work and avoid retained test-thread temporaries, the case passed twice on the device, then passed again with the actual error/Retry UI check. No production gate or test was disabled. An initial manual launch after Gradle test cleanup also reported missing activity because Gradle had uninstalled the app; manually installed the debug APK and successfully reran. An initial ADB date command failed due to shell quoting; corrected before evidence capture.
- Evidence (local, not committed): `~/Library/Logs/FanStage/M2-device/ui-verify.log` SHA-256 `95db2120257d9edfeda2c77e2593b269d910bd061252d9beab6a0a51649eeefb`; `player-test-ui.txt` `68811979722dd65280bc9f60e8abc74451669c2df699456d78ba98f94140c6f5`; `surface-test-ui.txt` `0e9b369a1ff7dd7f84d02422dc2ab3ceac0b577b7cd9155f7d45bb1e18a476a8`; `process-death-current.log` `ce951d7ec8e17281ce9d6f439d328e6e5d93f7d34fc7c4d208d20f596db03b51`; initial failing run `initial-gc-check-failure.log` `42ccae2ee15f1b6eefaeed73365220b5b10b1246106095bd823ebada18595656`. Logs are local and may contain local paths. The debug app was installed on the device as requested.
- **Prior main CI PASS:** [run #41799161431](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41799161431), source `982d3293b3faf07524c4893617f3088e39b85554`, build/lint/unit and API 35 emulator jobs success. This PR changes the test and adds PlayerPanel's source URI argument; its own CI is separate and must pass before merge.
- **NOT_RUN:** real remote-network LIVE, a visible LIVE first frame, actual system LMK (as opposed to `am kill`), prolonged or multi-device heap and performance profiling. M2 Gate is VERIFIED for the tested physical-device and synthetic-fixture criteria; those limitations are not PASS claims. Remaining P2: 13 lint warnings and Compose test API deprecation.

## M3 — Fake Realtime Chat (2026-11-04)

- 사용자 결정: 전체 활성 룸의 화면·Fake 서버를 합쳐 고유 논리 메시지 최대 1,000개, 표시 최대 500개, 메시지당 최대 1,000 Unicode 코드포인트. 공통 메시지는 슬롯을 공유하고 Pending은 고정한다. 이 한도는 프로세스 전체 바이트 상한이 아니다. 사용자 선호에 따라 후속 선택은 권장안을 적용한다.
- 범위: [M3 SDD](../docs/specs/M3-fake-chat.md), [ADR-0007](../docs/adr/ADR-0007-fake-chat-contract.md). Fake만 구현; 실제 WebSocket·Backend는 M4. 로컬 메모리에서 합성 fan과 사용자의 입력을 구분하고 입력 텍스트는 로그·디스크에 기록하지 않는다.
- 소스 기준 `7fe45f58131d1f21546b04d23368813bfd6050f4` 및 이 PR의 변경. 환경 macOS arm64, JDK 17.0.14, Gradle 9.5.0, Android SDK 37, API 36 arm64 emulator.
- **PASS:** `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :app:connectedDebugAndroidTest --no-daemon --stacktrace` (android 디렉터리), 종료 코드 0, `BUILD SUCCESSFUL`, 124 tasks (27 executed / 97 up-to-date). Unit XML 총 36/36 (M3 27, 기존 9), errors/failures/skips 0; app unit task NO-SOURCE. Lint 0 errors / 13 warnings. 에뮬레이터 instrumentation 1/1은 Chat 입력→ACK Sent 및 동시에 Player 재생 유지, 기존 VOD 첫 프레임·lifecycle·release 회귀를 검증. 로컬 Media3 Init/Release 14쌍 일치.
- **FAIL → PASS:** 취소를 오류로 바꾸는 비협조 transport가 종료된 ViewModel의 상태를 바꾸는 실패를 단위 테스트로 재현했고 취소 상태 검사를 추가한 뒤 통과했다. 1,000개 슬롯 전체가 Pending으로 고정된 상태에서 새 Fake 룸 생성이 충돌하는 실패도 재현했고 기존 메시지 보존 및 안전한 빈 룸 처리로 수정했다. 실패 기록은 아래 로컬 로그에 보존한다.
- Local evidence (not committed): `~/Library/Logs/FanStage/M3/verify-draft.log` SHA-256 `49d59fdef5d6596f92a23b8a42678ac2e3371da5edfae255a55eb1bf18fff91c`; `lifecycle-draft.log` `0670d29f35aa7355a64d48d0d3e669a2d48f7dc6e05933aeb`; `cancellation-failure.log` `91c3ab621f069b60500e8e08d524651eb0e519643e3e96465addc2603b8170c4`; `full-budget-failure.log` `ebaba39d1f91a538bd276faf527e23af4e432a5c3733b7062042bc291cbfe83f`. 로컬 로그에는 경로가 포함될 수 있다.
- **NOT_RUN:** M3 실기기, 실제 서버/WebSocket·네트워크 장애 및 서버 영속 멱등성. M2 원격 LIVE, 실기기 LIVE 첫 프레임, 실제 LMK 및 장시간 heap/profile은 여전히 NOT_RUN. Fake ID 멱등성은 공유 예산의 보관 범위에 한정된다. PR CI 및 리뷰 결과는 해당 PR에서 확인한다.

- **에뮬레이터 실행 재시도:** 최종 통합 테스트의 첫 시도는 0개 테스트 후 패키지 설치·삭제 중 앱 프로세스가 종료되어 FAIL했다. 이벤트 로그에 `installPackageLI` 및 `deletePackageX`가 기록됐으나 정확한 원인은 미확정이다. 부팅 완료와 단일 실행 상태 확인 후 동일 명령은 PASS했다. 로컬 원본: `~/Library/Logs/FanStage/M3/emulator-interruption.log` SHA-256 `834e263147f8fe2a7abc395887841178c641126cd673d230855d05c931768623`, `emulator-interruption-events.log` `4047cbf5863be80697af95f40e924b590f690016b1ec3205be9c8a0c989f4093`.

- 추가 계약 검증: 앱 내 이력을 갖지 않는 대체 Transport에서도 중첩 화면이 프로세스 공통 메시지 예산을 공유하도록 수정했으며 단위 테스트와 위 전체 검증을 재실행했다. 실제 WebSocket 구현과의 호환성은 여전히 M4 NOT_RUN이다.

- 입력 초안은 시스템 saved state에 넣지 않고 화면 ViewModel 메모리에서만 관리한다. 단위 테스트로 길이 제한 및 룸 전환 초기화를, 에뮬레이터에서 Activity 재생성 시 초안 보존을 검증했다. 프로세스 종료 후 초안 보존은 제공하지 않는다.

## M4 — local PostgreSQL/WebSocket and Android integration (2026-11-04)

- Source base `7eef606991b4c2c1f58a2116745e7de11cf71caf` plus this feature PR. Scope, exact protocol, non-production constraints and chosen limits: [M4 SDD](../docs/specs/M4-real-chat.md), [ADR-0008](../docs/adr/ADR-0008-local-websocket-chat.md). Local development data persists across ordinary Compose shutdown; no actual account or credential was committed. CI creates and removes its own disposable synthetic DB.
- Versions/environment: macOS arm64, Docker Desktop 4.55 / Engine 29.1.3 / Compose 2.40.3, PostgreSQL 17.9, Spring Boot 4.0.9, Flyway 11.14.1 (Boot BOM), JDK 17.0.14, Gradle 9.5.0, Android API 36 arm64 emulator, OkHttp 5.5.1. Official compatibility references and image/checksum pins are in ADR.
- **PASS:** dedicated Compose `up --build -d`; Flyway version 1 marked successful. `python3 backend/chat-server/tests/integration.py` (exit 0) tested authenticated HTTP/WS, identity-scoped isOwn/client ID, subscription-before-snapshot boundary, 12 concurrent identical-ID retries, 20 concurrent distinct sends with contiguous unique sequences, duplicate/content conflict, valid/invalid Unicode/length/ID and history ranges. A deferred DB trigger forced COMMIT failure; no ACK or broadcast was observed and the next valid sequence was gap-free. Restarting both DB and server preserved message ID, timestamp, sequence and idempotent result. Tokens were generated only for the isolated local test and were not printed or committed.
- **PASS:** Android command from `android/`: `./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug testDebugUnitTest :app:connectedDebugAndroidTest` with class filter `dev.fanstage.android.RealChatIntegrationTest`, temporary instrumentation token arguments, `--no-daemon --stacktrace`; exit 0, 209 tasks (43 executed / 166 up-to-date), unit XML 41/41 (chat 32 plus existing 9), lint 0 errors / 13 warnings, real integration 1/1. The emulator connected to the actual Docker server, tested distinct authenticated users, idempotent resend/conflict, forced socket disconnect, a message committed while disconnected and subsequent HTTP snapshot recovery, old-room event isolation, debug Fake/Real selection, authentication error vs uncertain outcome, actual UI send, socket cleanup and independent Player during chat error/disconnect. Token values were checked absent from captured stdout/XML.
- **PASS:** filtered `RoomNavigationSmokeTest` ran separately (1/1, exit 0) for Fake/chat/Player regression; 14 Media3 Init/Release IDs matched. Merged manifests were parsed: debug cleartext true, release explicitly false. Compose config, CI YAML and shell/Python syntax passed; CI XML validator passed real XML and rejected zero tests; sanitizer negative/positive check removed all synthetic test credential values.
- **FAIL → PASS:** first Python integration assumed `subscribed` must arrive before concurrent broadcasts; real server delivered a broadcast first. Test now handles valid interleaving, and separate subscription/snapshot boundary test verifies no loss. First Android integration compile had an invalid top-level Compose `assertExists` import; removed it and reran successfully. No checks were disabled. Local logs retain both failures.
- Evidence (local, not committed; paths may appear): `~/Library/Logs/FanStage/M4/server-final.log` SHA-256 `5c9b15572c02199bcf7239b730d0cf58093e3c87ce3fabec677a68b560c7a82d`; `android-connection-final.log` `e9bde1ad4d26f831990167667af83e6b089b2427a5eec9cd7d10788dae42b590`, `real-connection-final.xml` `9f96694d5bca53e6596f4797206e38b233ac78280fc1598c3e7bf5b89626743e`; Fake/player regression `fake-connection-final.log` `5c2f7642687a8df25ab8a663f17f8844685699a6ddc232bddc6967fadc22438e`, `fake-connection-player.log` `e349100dfe2330c7c32ba0af69112234ff1aa22727388fbbaba178d385ad8f61`.

- **NOT_RUN:** physical-device LAN connection, real production authentication/TLS, external deployment, M5 broad fault injection, M6 performance, M2 remote LIVE / visible synthetic LIVE first frame / actual LMK / long-duration heap profiling. The local Android emulator test and server test used disposable synthetic users and data. PR CI and formal review are separate from the local results.

- **Graceful-close regression FAIL → PASS:** a synthetic local WebSocket peer sent a normal server-initiated close. The first Android instrumentation run failed `client did not acknowledge server close` (exit 1); `RealChatTransport.onClosing` now acknowledges the close and transitions to reconnecting. The same complete Android build/release/lint/unit/RealChatIntegrationTest command above then passed (exit 0, 209 tasks: 43 executed / 166 up-to-date; 41 unit tests, 1 instrumentation case). This case also reran the real Docker server integration. `RoomNavigationSmokeTest` separately passed (exit 0, 1/1) with 14 matched Player Init/Release IDs. The manifest script initially failed when invoked from the repository root; `cd android && python3 ../scripts/m4-check-manifests.py` passed (debug true, release false).
- Local sanitized evidence (not committed): `~/Library/Logs/FanStage/M4/graceful-close-failure.log` SHA-256 `5c3f2b632bebe746d3d9d2e57d445f2e072a53a9bad6e8ae68c94162144d5b74`; `android-graceful-final.log` `5ed6c2f3b92071b79ff481f0d7f40fcf6e40aa6d8c839228b1f2de0f1e5781ee`; `fake-graceful-final.log` `cdecf7fede496e5c5982bcd282160a84793fb811bb1f667fa3c093ffba4ca01f`; `fake-graceful-player.log` `bbc046789828df7f639ea13eca3a5f0480b24a93d0a0d0553d908e5809baefea`. The final real-test XML was overwritten by the separate Fake regression run; the real-test stdout was retained, and the earlier real-test XML above remains available.

- **PR CI PASS:** [Android run #41820156197](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41820156197) build/lint/unit and API 35 Fake/Player smoke, and [M4 run #41820156491](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41820156491) PostgreSQL concurrency/rollback/restart and Android real WebSocket emulator passed on source head `dbe807f1ebdec04b4bab67eef08ff9379da64557`. The latter log confirms one instrumentation case and runner exit. This documentation update follows the tested source; final PR head CI must also pass before merge. GitHub Actions reported existing Node/setup-java deprecation warnings.
