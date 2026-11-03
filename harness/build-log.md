# 작업 및 검증 기록

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
