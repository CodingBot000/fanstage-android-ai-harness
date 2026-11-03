# M0 — Harness Core SDD

- Status: VERIFIED — M0-001 및 M0-002 증거 검토 후 M0 Gate 충족 (2026-11-04).

## Goal

승인된 작업 단위를 관리하고 실행과 검증 증거를 추적하는 최소 문서 기반을 마련한다.

## In Scope / Out of Scope

M0-001은 Git 초기화와 지정된 Harness 문서 작성·검증이다. Android/Backend 구현, 의존성, CI 구축, 유료 서비스와 M0-002 실행은 제외한다. 최초 지시의 커밋·push 제한은 이후 사용자의 별도 승인으로 초기 공개 커밋에 한해 해제되었으며, 이것이 M0-001의 완료 승인을 뜻하지는 않는다. M0 전체의 나머지 Exit Gate는 별도 승인된 작업에서 다룬다.

## Input / Output / Contracts

입력은 [마스터 플랜](../MASTER_PLAN.md), 승인된 작업 지시와 현재 저장소 상태다. 출력은 상태, 지침, ADR, 테스트 매트릭스 및 [작업 로그](../../harness/build-log.md)다. 상태는 실제 실행과 사람의 승인을 구분한다. 미실행은 NOT_RUN으로 기록하며 한 작업의 승인으로 다음 작업을 허용하지 않는다.

## Architecture Impact

개발 프로세스와 문서 책임을 정의한다. 앱 구조나 네트워크·저장 계약은 구현하거나 변경하지 않는다. [ADR-0001](../adr/ADR-0001-development-workflow.md)은 초안이다.

## Normal Path

현재 상태와 SDD를 확인하고 승인 범위 내 변경 후 검증 결과와 증거를 기록한다. 사람의 리뷰 전 REVIEW를 유지한다.

## Failure / Recovery

원본 누락, 기존 파일 충돌 또는 승인 범위 확대가 필요하면 해당 변경을 중단하고 보고한다. 실패를 숨기지 않고 FAIL과 원인·후속 조치를 기록한다. 미실행 검증은 사유와 함께 NOT_RUN으로 남긴다.

## Test Plan

필수 파일, 원본 동일성, 참조, 지침, 범위 및 민감정보를 명령으로 점검한다. Git 미추적 파일도 별도로 확인한다. Codex API GitHub Action의 읽기 전용 계획 리허설을 실제 실행한 후에만 통과로 기록한다. Claude import는 선택 호환성 검사이며 Android 빌드·테스트·CI는 아직 대상이 아니다.

## Acceptance Criteria

### M0-001 — Repository 및 Harness 문서 초기화

- [x] 지정된 문서가 존재하고 공개판 마스터 플랜의 버전·변경 이유·진실의 원천이 명확하다. 초기 원본과 바이트 동일성은 공개판 수정 이전 로컬 검증 이력으로만 취급한다.
- [x] 지침·상태·참조와 검증 결과가 일관되고 승인 대기 상태로 리뷰되었다.
- [x] 허용 범위 밖 구현이 없고 검증 결과와 미실행 사유가 기록된다.
- [x] 사용자가 M0-001 결과를 리뷰하고 승인했다.

### M0-002 — Codex API 읽기 전용 계획 리허설

- [x] GitHub Actions `openai/codex-action@v1`이 Repository Secret `OPENAI_API_KEY`로 실제 실행됐다.
- [x] Codex가 현재 M0, M1 구현 금지, 승인 게이트, Android 테스트 NOT_RUN을 구분한다.
- [x] `.github/codex/schemas/m0-002-result.schema.json`에 따른 결과를 출력하고 계약 검사가 통과한다.
- [x] `android/`·`backend/`를 생성하거나 소스 파일을 변경하지 않았다 (Actions 검증 단계 PASS).
- [x] Actions Run URL, 결과 JSON, 실행 결과를 build-log에 기록했다.
- [x] 최초 인증 실패 후 자동 단계 전환 없이 중단했으며, 사용자 키 교체 후 명시적 재실행을 수행했다.

M0-002 재실행 attempt 2는 최종 SUCCESS이며 [Actions Run](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41692832207)과 [검증된 구조화 결과](../../harness/reviews/M0-002-plan-only-result.json)를 참조한다. 최초 attempt 1 실패는 이력으로 유지한다.

### M0 전체 Exit Gate

- [x] [마스터 플랜 4.8절](../MASTER_PLAN.md)의 모든 Exit Gate를 Codex 중심 검증 정책에 따라 충족하고 사람의 사전 포괄 승인을 확인했다.

검증되지 않은 항목은 미완료로 유지한다. M0-001 승인과 M0 전체 Exit Gate 통과는 별개다.

## Evidence / Commands

실제 명령과 결과는 [build-log](../../harness/build-log.md)에 기록한다. 검증 계층은 [test-matrix](../test-matrix.md)를 따른다.

## Open Questions / ADRs

- Repository `CodingBot000/fanstage-android-ai-harness`는 Public으로 생성되었다. 라이선스는 미결정이다.
- Codex plan-only GitHub Actions attempt 2가 검증을 통과했다. Claude Code import는 선택 호환성 검사로 NOT_RUN 상태를 유지한다.
- [ADR-0001](../adr/ADR-0001-development-workflow.md): ACCEPTED.
