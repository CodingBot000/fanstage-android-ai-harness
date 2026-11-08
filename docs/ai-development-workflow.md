# AI 코딩 에이전트 제어·검증 워크플로

FanStage에서는 Codex를 코드 작성과 리팩토링에 사용했습니다. 개발자는 **작업 범위를 정의하고, 설계·동작의 적합성을 검토하며, 테스트 결과를 확인한 후 변경을 수용**했습니다.

## 1. 작업을 어떻게 제어했나

| 단계 | 적용한 통제 방법 | 확인할 근거 |
|---|---|---|
| 요구사항 정의 | 기능별 정상·오류 흐름, 변경 모듈, 완료 기준을 먼저 정리 | [기능 계약](specs/) · [설계 결정](adr/) |
| 에이전트 작업 | [AGENTS.md](../AGENTS.md)로 변경 범위, 아키텍처 경계, 보고 형식을 안내 | 실제 코드 변경·PR 차이 |
| 코드 리뷰 | Compose 상태 관리, Coroutine 취소, Player·Surface·Socket 소유권, 채팅 정합성 검사 | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 자동 검증 | Android 빌드·Lint·단위 테스트, 모듈 경계 검사, 실제 PostgreSQL·WebSocket 서버 테스트 | [Android CI](../.github/workflows/android-ci.yml) · [서버 CI](../.github/workflows/m4-integration.yml) |
| 기기 검증 | 화면 전환·영상 재생·IME 변경은 관련 로컬 에뮬레이터 또는 실기기 계측 실행 | [검증 결과](verification.md) |
| 결과 기록 | 수행한 테스트·소스 버전·CI 링크를 PR에 연결하고 검토 후 병합 | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) · [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) |

**핵심은 지침과 검증을 구분하는 것입니다.** AGENTS.md는 에이전트의 작업 방향을 지정하지만 코드의 정확성을 보장하지 않습니다. 실제 통과 여부는 테스트·CI 결과와 변경 코드 리뷰로 판단했습니다.

## 2. 사례: Media3 전체 화면 전환 검증 보완

**문제:** 전체 화면으로 전환할 때 이전 PlayerView에서 발생한 첫 프레임 이벤트를 새 화면의 출력 성공으로 잘못 판단할 수 있었습니다. 코드 리뷰에서 이 잘못된 성공 판정 가능성을 발견했습니다.

**수정:** 이전 PlayerView를 먼저 분리한 뒤 새 TextureView에 재생 화면을 연결하도록 순서를 조정했습니다. 테스트에서는 출력 객체의 동일성을 확인해 **이전 화면의 이벤트와 새 화면의 이벤트를 구별**했습니다.

**재검증:** [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)에서 플레이어를 포함한 단위 테스트 **56/56**, API 36 로컬 계측 **41/41** 통과를 기록했습니다. 해당 변경의 Android 빌드·Lint·단위 테스트와 서버 통합 CI 결과도 PR에 연결했습니다.

이 사례는 에이전트가 코드를 생성했다는 사실보다 **검증 기준을 구체화하고, 잘못된 성공 판정 가능성을 제거한 과정**을 보여줍니다.

## 3. 사례: 메시지 전송 중 연결 전환 제어

**문제:** 채팅 서버를 바꿀 때 전송이 확정되지 않은 메시지가 있다면 이전 연결의 결과를 잃거나 화면 상태를 잘못 확정할 수 있습니다.

**처리:** `ChatViewModel.replaceTransport()`는 미확정 메시지가 남아 있으면 전송 경로 변경을 차단합니다. 로컬 서버 연결은 임시 연결에서 인증을 확인한 뒤 적용하며, 인증 실패 시 기존 데모 모드를 유지합니다.

**재검증:** [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)에서 관련 단위 테스트 **56/56**, API 36 로컬 계측 **28/28** 통과를 기록했습니다. 테스트에는 인증 실패와 미확정 메시지 상태에서의 전환 차단이 포함됩니다.

## 4. 사례: Hilt 전환 과정의 회귀 실패 원인 분리

**문제:** Hilt 전환을 진행하면서 실기기의 LIVE 채팅 전송 후 재생 상태를 검사하는 기존 테스트가 실패했습니다.

**원인 분리:** 전환 전 커밋에서도 같은 실패를 재현해 DI 변경으로 인한 결함이라고 단정하지 않았습니다. 키보드 표시 시 compact UI에서 재생 상태 테스트 태그가 사라졌지만 실제 Media3 Player는 재생 중이었습니다.

**수정과 재검증:** 원래 테스트를 실제 Player 동일성·재생 상태 확인으로 보강했고, 별도로 발견한 회전 후 일시정지 화면의 재생 버튼 표시 문제를 수정했습니다. 선택 실기기 계측 39/39, Hilt 바인딩 테스트 1/1을 통과했습니다. 초기 FAIL과 최종 PASS의 실행 시점은 [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67)과 [검증 기록](specs/hilt-di-migration-verification.md)에 분리해 남겼습니다.

## 5. 사례: 모듈 경계의 자동 검사

**목표:** 기능 모듈이 `app`이나 다른 기능의 내부 구현에 직접 의존하지 않도록 관리합니다.

**검증:** [`check-ux-module-boundaries.py`](../scripts/check-ux-module-boundaries.py)는 기능·공통 모듈의 허용 의존 관계와 순수 Kotlin 모델의 Android UI 참조 여부를 검사합니다. 이 스크립트를 Android GitHub Actions에서 실행합니다. 기능별 분리와 검증 이력은 [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38)에 남겼습니다.

## 최종 검증 감사의 범위

별도 에이전트 세션이 코드와 증거를 재검토했고, Hilt ViewModel의 실제 Activity 생성 경로와 실기기 서버 연결을 추가로 검증했습니다. 같은 모델 계열의 별도 리뷰이며 다른 모델의 평가로 주장하지 않습니다. 실패·미실행과 GitHub 병합 정책의 한계는 [감사 보고](evidence/verification/audit-20261108/audit-report.md)에 기록했습니다.

## 검증 결과를 읽는 기준

GitHub Actions는 Android 빌드·Lint·단위 테스트와 서버 통합·장애 주입 테스트를 실행합니다. Android 계측은 로컬 에뮬레이터·실기기에서 수행해 해당 PR에 연결했습니다. **테스트 수와 통과 여부는 각 PR에 표시된 코드 버전 기준**입니다.

[검증 결과 모음](verification.md) · [테스트 매트릭스](test-matrix.md)
