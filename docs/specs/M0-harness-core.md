# M0 — Harness Core SDD

- Status: DRAFT — M0-001 실행 승인은 SDD 또는 M0 완료 승인이 아니다.

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

필수 파일, 원본 동일성, 참조, 지침, 범위 및 민감정보를 명령으로 점검한다. Git 미추적 파일도 별도로 확인한다. Claude import는 실제 실행 시에만 검증한다. Android 빌드·테스트·CI는 아직 대상이 아니다.

## Acceptance Criteria

### M0-001 — Repository 및 Harness 문서 초기화

- [ ] 지정된 문서가 존재하고 공개판 마스터 플랜의 버전·변경 이유·진실의 원천이 명확하다. 초기 원본과 바이트 동일성은 공개판 수정 이전 로컬 검증 이력으로만 취급한다.
- [ ] 지침·상태·참조와 검증 결과가 일관되고 승인 대기 상태다.
- [ ] 허용 범위 밖 구현이 없고 검증 결과와 미실행 사유가 기록된다.
- [ ] 사용자가 M0-001 결과를 리뷰하고 승인한다.

### M0-002 — 별도 작업 후보

- [ ] Claude Code에서 `@AGENTS.md` import를 실제 검증한다.
- [ ] 별도 plan-only 리허설을 실제 수행하고 보고 형식을 검토한다.

M0-002는 아직 승인되지 않았으며 실행되지 않았다.

### M0 전체 Exit Gate

- [ ] [마스터 플랜 4.8절](../MASTER_PLAN.md)의 모든 Exit Gate를 충족하고 사람의 승인을 받는다.

검증되지 않은 항목은 미완료로 유지한다. 위 M0-001 체크 항목도 사용자 승인 전 확정하지 않는다.

## Evidence / Commands

실제 명령과 결과는 [build-log](../../harness/build-log.md)에 기록한다. 검증 계층은 [test-matrix](../test-matrix.md)를 따른다.

## Open Questions / ADRs

- Repository `CodingBot000/fanstage-android-ai-harness`는 Public으로 생성되었다. 라이선스는 미결정이다.
- Claude Code import 실동작과 M0의 별도 계획-only 작업은 미검증이다.
- [ADR-0001](../adr/ADR-0001-development-workflow.md): 사용자 리뷰 대기 중인 초안.
