# AI 코딩 에이전트 활용과 검증

FanStage는 **사람이 기술적 요구사항과 검증 기준을 정하고, Codex가 구현·수정을 수행한 Android 프로젝트**입니다. 개발자는 설계 선택·코드 변경·실행한 테스트 결과를 검토한 뒤 변경을 수용했습니다. 이 문서는 'AI를 사용했다'는 설명보다 **어떤 결정을 내리고 무엇으로 확인했는지**에 초점을 맞춥니다.

## 담당 범위와 협업 방식

| 단계 | 개발자의 판단·책임 | 구현·검증 근거 |
|---|---|---|
| 요구사항·설계 | 기능 범위, 실패 시 동작, 모듈 경계, 완료 조건을 정하고 ADR/SDD로 기록 | [채팅 결정](adr/ADR-0003-chat-consistency.md) · [플레이어 결정](adr/ADR-0002-player-lifecycle.md) |
| 코드 생성·변경 | Codex에 작업 범위·금지 조건을 전달하고 생성된 차이를 검토 | [AGENTS.md](../AGENTS.md) · 개별 PR |
| 기술 검토 | Player/Surface 생명주기, Coroutine 취소, 채팅 메시지의 정합성, DI 소유권을 확인 | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) |
| 실행 검증 | Gradle·서버 통합 테스트·로컬 기기 계측 결과를 구분하고 회귀를 재검증 | [검증 결과](verification.md) · [테스트 매트릭스](test-matrix.md) |
| 변경 수용 | PR의 변경 범위와 `required-quality` 체크를 확인 | [검증 정책](specs/ci-required-quality-policy.md) |

**운영 원칙:** `AGENTS.md`는 에이전트의 작업 방향을 지정하는 지침입니다. 품질 판단은 코드 리뷰와 **실제로 실행된** 빌드·테스트·계측 결과로 합니다.

## 대표 판단 1. 전체 화면 전환의 재생 성공 판정 보완

- **발견한 문제:** 이전 PlayerView에서 발생한 첫 프레임 이벤트를 새 출력의 성공으로 판단할 수 있었습니다.
- **선택한 수정:** 이전 View의 Player 연결을 먼저 해제하고 새로운 TextureView에 연결했습니다. 출력 세대를 구별해 화면 전환 후 새 Surface의 이벤트인지 확인하도록 테스트를 보강했습니다.
- **확인 경로:** [Media3PlayerController.attach/detach](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/Media3PlayerController.kt) → [PlayerSettingsTest](../android/app/src/androidTest/kotlin/dev/fanstage/android/PlayerSettingsTest.kt) → [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)

## 대표 판단 2. 결과가 불확실한 채팅 전송의 보호

- **문제:** 서버 저장 이후 ACK가 유실되면 클라이언트는 성공과 실패를 구분할 수 없습니다. 서버 전환 도중 Pending 메시지가 존재해도 이전 연결의 응답을 일관되게 처리해야 합니다.
- **선택한 정책:** 미확정 메시지는 Pending으로 유지하고 동일한 `clientMessageId`로 재시도합니다. `ChatViewModel.replaceTransport()`는 미확정 메시지가 남아 있으면 전환을 차단하고, 새 서버 인증이 성공한 뒤에만 전송 경로를 교체합니다.
- **확인 경로:** [ChatViewModel](../android/feature/live/src/main/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModel.kt) → [불확실한 전송 재시도 테스트](../android/feature/live/src/test/kotlin/dev/fanstage/android/feature/live/chat/ChatViewModelTest.kt) → [ACK 유실 장애 주입](../backend/chat-server/tests/m5_faults.py) → [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53)

## 대표 판단 3. Hilt 전환 중 실패 원인을 분리한 회귀 분석

- **관찰:** LIVE 채팅 전송 후 재생 상태를 확인하는 실기기 테스트가 Hilt 전환 과정에서 실패했습니다.
- **검증 판단:** 같은 테스트를 전환 이전 커밋에서도 재현해 DI 변경의 영향이라고 단정하지 않았습니다. 키보드 표시로 UI 테스트 태그가 사라진 경우를 실제 Media3 Player의 상태와 구분했습니다.
- **수정:** 테스트의 관찰 기준을 실제 Player 상태·동일성으로 보강하고, 별도로 발견한 회전 후 일시정지 버튼 표시 문제를 수정했습니다.
- **확인 경로:** [Hilt 검증 기록](specs/hilt-di-migration-verification.md) → [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67)

## 구조적 품질 기준: 모듈·UI 공통화

기능별 모듈이 다른 기능의 내부 구현이나 `app`을 직접 참조하지 않도록 [모듈 경계 검사](../scripts/check-ux-module-boundaries.py)를 Android CI에 연결했습니다. UI 리팩토링에서는 Media 카드 세 종류의 공통 표현만 `feature:media` 내부로 추출하고, Navigation·ViewModel·Player 소유권은 기존 모듈에 유지했습니다.

[UI 설계 결정](adr/ADR-0005-ui-component-reuse.md) · [구현 PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70)

## 검증 결과와 원본

테스트 수치, 실행한 기기·커밋, CI 결과와 증거 파일은 [검증 결과](verification.md)에 모았습니다. 실제 기기 계측과 GitHub Actions의 계측 APK 컴파일은 서로 다른 검증으로 구분합니다.
