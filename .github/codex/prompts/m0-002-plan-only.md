# FanStage M0-002 — Codex 계획 전용 리허설

이 프롬프트는 GitHub Actions의 **읽기 전용** Codex 실행으로 처리한다.
시스템과 리포지토리의 AGENTS.md는 해당 규칙대로 따른다.

## 작업 목표
1. `docs/STATUS.md`, 현재 M0 SDD, `AGENTS.md`, 직접 관련된 마스터 플랜의 M0 및 M1 설명만 읽는다.
2. 현재 작업이 `M0-002`이고 `M1` 구현 승인이 아님을 확인한다.
3. 가상 요청 "M1 Android Native Foundation 개발을 위한 첫 작은 작업을 계획하라"에 대해 **계획만** 작성한다.
4. 변경 영향, 검증 항목, 미검증 항목, 다음 승인 게이트를 포함한다.

## 금지
- 파일 생성/수정/삭제, 명령으로 코드 변경, 의존성 설치, 네트워크 접속 금지
- Android 프로젝트/Gradle/Backend/CI 기능 구현 금지
- 테스트를 실행했다고 주장하지 말 것
- M0 완료나 M1 구현 승인을 가정하지 말 것

## 반환 계약
`.github/codex/schemas/m0-002-result.schema.json`의 JSON 스키마를 따른다.
- task_id: M0-002
- current_milestone: M0
- mode: PLAN_ONLY
- m1_permitted: false
- human_gate: REQUIRED
- android_tests: NOT_RUN
- proposed_next_steps: M1 작업의 계획 후보만 나열. 실제 구현하지 않는다.
- open_issues: 현재 남아있는 검증/환경 이슈만 명시한다.

이는 **Codex 런타임/지침 준수 리허설**이다. 결과가 성공해도 Android 빌드 성공이나 M0 전체 완료를 주장하지 않는다.
