# Android 최종 검증 감사

- 착수 기준 코드: `4191a1dd1adf9debd95a3cc0c4366943c751f67e` (PR #68 이후 `main`). 후속 검증은 [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69)에서 진행했고, 해당 PR은 [병합 커밋 `3299c9d`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/3299c9d97cd8a43a3360f4c3c77beac10ce4b22d)로 `main`에 반영됐다. 이 문서의 테스트 결과는 당시의 코드/실행 환경에 한정하며 최신 `main`의 상태는 [STATUS](../STATUS.md)를 따른다.
- 상태: **PARTIAL** — 주요 기기·서버 검증과 PR 통합 품질 게이트는 확인했다. 2026-11-09 `main` Branch Protection 및 `required-quality` 필수 체크를 적용했으나 관리자 우회를 허용한다. 실제 GitHub 병합 차단 실험은 `NOT_RUN`이다. 최초 회전 실패 원인 및 장시간 누수 부재는 미확정이며, 미실행 검증을 PASS로 추정하지 않는다.
- 범위: Hilt 계측, Android 빌드·UI·생명주기, 격리 서버 연결·복구, 자원 반복 관찰, 기존 성능 근거, 레거시 DI, CI 정책, 원본 증거·문서 정합성.
- 제외: 상시 인프라, 실제 사용자 데이터, PR 자동 병합. GitHub 보안 설정 변경은 착수 당시 제외했으나, 이후 사용자 승인을 받아 `main` 보호 정책만 별도로 적용했다.

## 수용 기준

실제 실행한 명령과 환경·소스 버전·결과를 기록하고 기계 판독 가능한 테스트 결과를 민감정보 검사 후 보존한다. 실패는 재현·원인·후속 결과를 보존한다. 기기 회전, Activity 재생성, 프로세스 종료는 구분한다. 30회 이상 반복 관찰의 메모리·자원 수치는 누수 부재의 증명으로 해석하지 않는다. 기존 및 신규 성능 결과를 동일 조건 여부와 함께 제시하고 근거 없는 개선율을 주장하지 않는다. 독립 리뷰 관점의 지적과 해결·미해결을 기록한다.

## Open Questions / 제약

- 실기기가 연결되지 않으면 격리 에뮬레이터 결과와 실기기 NOT_RUN을 분리한다.
- 착수 당시 Branch Protection/Ruleset은 읽기 전용 점검 대상이었다. 이후 사용자 승인 하에 `main` Branch Protection을 적용했다. 관리자 우회 여부와 실제 차단 실험 미실행을 [정책 기록](ci-required-quality-policy.md)에 분리한다.
- 성능·누수 합격 수치나 데이터 보존 정책은 임의로 확정하지 않는다.

## 결과

실제 실행·기기·소스 SHA·실패와 후속 결과는 [감사 보고](../evidence/verification/audit-20261108/audit-report.md)와 [원본 증거](../evidence/verification/audit-20261108/README.md)를 참조한다. 최초 감사 당시 GitHub 설정 변경은 없었지만, 2026-11-09 사용자 승인으로 `main` Branch Protection을 적용했다. `required-quality`의 PR CI 성공은 확인했으며, 정책에 따른 실제 병합 차단 실험은 수행하지 않았다.

## 증거 무결성 검사 보완

공개 evidence Manifest v2의 파일·해시·JUnit 집계·실패 이력·커밋 출처를 검증하고, v1 이력의 고정 실패 정책을 지원한다. 비공개 원본 해시와 실행 당시 미커밋 작업 트리 일치는 검증했다고 주장하지 않는다. 임시 fixture에 의도적 오류를 주입해 검사 실패를 확인하고 문서 CI에 연결한다. 상세는 [증거 안내](../evidence/verification/audit-20261108/README.md).

## 회전 진단 및 통합 게이트 후속

실기기 회전은 두 진단 버전에서 각 20/20, 관련 최종 회귀 42/42 및 Hilt 3/3을 실행했다. 기존 최초 실패는 재현되지 않아 원인 미확정으로 보존한다. Debug 전용 단조 시간 진단과 격리 실패 probe·원본은 [증거 안내](../evidence/verification/audit-20261108/README.md)에 있다. GitHub 필수 체크는 사용자 승인 후 `main`에 적용되었다. 현재 `required-quality`는 GitHub Actions 출처의 필수 검사로 설정되어 있지만, 관리자는 우회할 수 있다. 별도의 실패 PR 병합 차단 실험은 `NOT_RUN`이다. 상세 내용은 [정책 적용 기록](ci-required-quality-policy.md)을 참조한다.
