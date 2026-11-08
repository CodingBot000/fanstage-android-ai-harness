# PR 필수 품질 게이트 제안

상태: **워크플로 구현·검증 중 / GitHub 보호 정책 적용 승인 대기**. GitHub 설정은 변경하지 않는다.

## 제안 구조

모든 main 대상 PR에서 경로 필터 없이 `PR quality gate`가 시작된다. `classify-quality-changes`가 PR의 base·head SHA와 변경 경로를 확인하고 Android·서버·문서 검사를 선택한다. 기존 세 워크플로는 PR에서는 재사용 워크플로로 한 번만 실행하고, main push에서는 기존 경로 필터를 유지한다. 분류기·집계기나 게이트 워크플로 자체가 바뀌거나 분류되지 않은 경로가 있으면 세 검사를 모두 실행한다. 이름이 `required-quality`인 마지막 작업은 변경 분류 성공과 **선택된 모든 검사 성공**을 요구한다. 선택된 작업의 실패·취소·누락·예상치 못한 skipped는 실패다. 선택되지 않은 작업만 명시적 `NOT_APPLICABLE`로 처리한다.

권장 Required Check 후보는 GitHub Actions 출처의 **`required-quality`** 하나다. PR #69의 `c8f1a98`에서 이름 `required-quality`, 출처 `github-actions`(App ID 15368), 성공 결과가 실제 생성됐음을 조회했다. 이는 필수 정책이 적용됐다는 뜻은 아니다. 실제 GitHub PR에서 표시되는 이름과 출처, 성공·실패·누락 처리까지 확인한 뒤에만 설정한다. 기존 `build-lint-unit`, 서버, 문서 체크를 경로 필터가 있는 workflow 상태 그대로 필수로 지정하면 실행되지 않는 PR이 Pending에 머물 수 있다. [GitHub 공식 설명](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks).

## 적용 전 사용자 검토 사항

- 대상: `main`을 대상으로 하는 Ruleset 또는 Branch Protection, PR 필수, 직접 push 제한, `required-quality` 성공 필수, 누락·진행 중에는 병합 불가. base가 전진한 뒤 오래된 결과로 병합하지 않도록 **Require branches to be up to date**(strict)를 권장한다.
- 우회: 일반 관리자·자동화 계정의 상시 우회는 허용하지 않는 안을 권장한다. 긴급 복구 경로와 담당자는 사용자가 결정한다.
- 영향: 문서 전용 PR은 Android·서버 검사를 제외하고 문서 검사와 두 경량 작업만 실행한다. Android 변경은 Android·서버, 서버 변경은 서버, 정책 변경은 세 검사를 실행한다. 각 무거운 검사 중복 실행은 피하지만 추가 경량 runner 두 개와 전체 Git 이력 checkout 비용은 발생한다. 계정의 실제 Actions 비용·한도는 확인하지 않았다.
- 기존 PR: 기존 체크 이름과 새 재사용 workflow의 표시 이름이 달라진다. 진행 중 PR의 최신 head에서 새 체크 결과를 확인해야 한다. 필수 정책은 이 체크가 실제 보고된 뒤 적용한다. 현재 workflow는 `merge_group`을 지원하지 않으므로 merge queue의 필수 체크로 바로 설정하지 않는다.
- 복구: 정책을 적용한다면 권한 있는 관리자가 Ruleset/보호 규칙을 수정·비활성화할 수 있다. workflow 오류는 수정 PR로 되돌리며 실패한 검사를 성공으로 위장하지 않는다.

## 실험 및 한계

분류·집계 로직은 임시 입력으로 실패·취소·미실행·정당한 제외를 검증한다. 별도 PR의 실제 Actions 실행은 로컬 테스트와 구분한다. **GitHub 보안 설정, main 직접 push 및 고의 실패 PR의 병합 차단 실험은 사용자 승인 없이 수행하지 않는다.** 설정을 적용하지 않은 동안에는 CI 통과가 병합 강제를 의미하지 않는다. 검사 코드와 정책을 함께 수정할 권한이 있는 행위자에 대한 보호는 별도 리뷰·권한 정책이 필요하다.
