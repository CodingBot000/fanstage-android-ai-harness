# PR 필수 품질 게이트 — 적용 상태와 설계 근거

상태: **2026-11-09 사용자 승인 후 main Branch Protection 적용**. 설정 조회 결과 `required-quality` (GitHub Actions App ID 15368), strict 최신 base, PR 필수, 관리자 우회 허용이다. 승인 리뷰 수는 0으로 설정되어 사람의 승인을 강제하지 않는다. 실제 병합 차단 실험은 별도 승인이 없어 NOT_RUN이다.

## 구현 구조

모든 main 대상 PR에서 경로 필터 없이 `PR quality gate`가 시작된다. `classify-quality-changes`가 PR의 base·head SHA와 변경 경로를 확인하고 Android·서버·문서 검사를 선택한다. 기존 세 워크플로는 PR에서는 재사용 워크플로로 한 번만 실행하고, main push에서는 기존 경로 필터를 유지한다. 분류기·집계기나 게이트 워크플로 자체가 바뀌거나 분류되지 않은 경로가 있으면 세 검사를 모두 실행한다. 이름이 `required-quality`인 마지막 작업은 변경 분류 성공과 **선택된 모든 검사 성공**을 요구한다. 선택된 작업의 실패·취소·누락·예상치 못한 skipped는 실패다. 선택되지 않은 작업만 명시적 `NOT_APPLICABLE`로 처리한다.

설계 단계에서 제안한 필수 체크는 GitHub Actions 출처의 **`required-quality`** 하나다. PR #69의 `c8f1a98`에서 동일한 이름과 출처(`github-actions`, App ID 15368)의 체크 결과가 생성된 것을 확인한 뒤, **2026-11-09 사용자 승인으로 `main`에 필수 체크로 적용했다**. [PR #70의 실제 게이트 PASS](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42502757479)는 확인되지만, 실패 PR 병합 차단 자체는 실험하지 않았다. 기존 `build-lint-unit`, 서버, 문서 체크를 경로 필터가 있는 workflow 상태 그대로 필수로 지정하면 실행되지 않는 PR이 Pending에 머물 수 있다. [GitHub 공식 설명](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks).

## 설계 시 고려한 적용 내용과 운영상의 주의

- 대상: `main`을 대상으로 하는 Ruleset 또는 Branch Protection, PR 필수, 직접 push 제한, `required-quality` 성공 필수, 누락·진행 중에는 병합 불가. base가 전진한 뒤 오래된 결과로 병합하지 않도록 **Require branches to be up to date**(strict)를 권장한다.
- 우회: 사용자의 결정에 따라 관리자 상시 우회를 허용한다 (`enforce_admins: false`). 관리자는 PR·필수 검사 제한을 우회할 수 있으므로 이 정책만으로 관리자 작업까지 강제되지는 않는다. 우회 문제는 보고한다.
- 영향: `edited` 이벤트도 base 변경을 포착하도록 수신하므로 PR 제목·본문 수정에도 재실행될 수 있다. 같은 head에서 재실행이 시작되며 기존 작업이 취소된 사례가 있었고, 게이트는 취소를 실패 처리했다. 추가 runner·재실행 비용을 고려한다. 변경 파일이 `docs/**`나 루트 Markdown에 한정되면 Android·서버 검사를 제외하고 문서 검사와 경량 집계 작업만 실행한다. **예외:** 현재 경로 분류기는 `android/README.md`도 `android/` 변경으로 간주하므로 해당 문서만 수정해도 Android·서버 검사가 함께 실행된다. 이는 보수적인 분류이며 CI 비용 증가 가능성을 별도로 감수한다. Android 변경은 Android·서버, 서버 변경은 서버, 정책 변경은 세 검사를 실행한다. 각 무거운 검사 중복 실행은 피하지만 추가 경량 runner 두 개와 전체 Git 이력 checkout 비용은 발생한다. 계정의 실제 Actions 비용·한도는 확인하지 않았다.
- 기존 PR: 기존 체크 이름과 새 재사용 workflow의 표시 이름이 달라진다. 진행 중 PR의 최신 HEAD에서 새 체크 결과를 확인해야 한다. 정책은 실제 체크 결과가 생성된 뒤 적용했다. 현재 workflow는 `merge_group`을 지원하지 않으므로 merge queue의 필수 체크로 바로 설정하지 않는다.
- 복구: 정책을 적용한다면 권한 있는 관리자가 Ruleset/보호 규칙을 수정·비활성화할 수 있다. workflow 오류는 수정 PR로 되돌리며 실패한 검사를 성공으로 위장하지 않는다.

## 실험 및 한계

분류·집계 로직은 임시 입력으로 실패·취소·미실행·정당한 제외를 검증한다. 별도 PR의 실제 Actions 실행은 로컬 테스트와 구분한다. **설정 전에는 GitHub 보안 정책을 사용자 승인 없이 변경하지 않았다.** 2026-11-09 승인 후 정책을 적용했다. `main` 직접 push나 고의 실패 PR의 실제 차단 실험은 시행하지 않았다. CI 통과만으로 관리자 우회가 봉쇄됐거나 실패 PR이 실제로 차단됐다고 단정하지 않는다. 검사 코드와 정책을 함께 수정할 권한이 있는 행위자에 대한 보호는 별도 리뷰·권한 정책이 필요하다.

## 적용 후 확인 및 남은 한계

2026-11-09 GitHub API 재조회에서 main 보호 활성화, strict `required-quality`와 GitHub Actions App ID 15368, PR 필수, 관리자 강제 비활성화, force push·삭제 비허용을 확인했다. 관리자에게는 우회가 가능하므로 이는 일반 기여자에게 적용되는 보호다. 실제 정상·실패·누락 체크의 병합 차단 실험은 별도 승인 전 `NOT_RUN`이다.

**병합 상태 갱신:** [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69)가 `main`에 병합되면서 [`.github/workflows/required-quality.yml`](../../.github/workflows/required-quality.yml)이 기본 브랜치에 반영됐다. 후속 [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)에서도 `required-quality`와 선택된 Android·서버·문서 검사가 PASS했다. 따라서 '게이트가 PR에만 있다'는 초기 제약은 현재 적용되지 않는다. 다만 관리자는 정책을 우회할 수 있고 실제 실패·누락 체크에 따른 병합 거부는 `NOT_RUN`이다. 문제가 생기면 권한 있는 관리자가 정책을 변경·해제해 복구할 수 있다.
