# FanStage 개발 현황

**최신 구조 변경 기준:** 2026-11-23 [PR #81](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/81) 병합 커밋 [`4de92ad`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/4de92adfa1b3bca4a3a8dabef342221643f8ce6d). 아래의 11월 16~18일 제출·미병합 표현은 당시 작업 이력입니다.

## 현재 구현

- **UI:** Discover·Community·Media·Messages·LIVE 화면. Kotlin·Compose 기반 11개 Gradle 모듈에서 `core:designsystem`을 재사용합니다. Media 카드 3종은 `MediaArtworkCard`로 묶고, 앱의 `app`의 `FanStageAppShell`·`FanStageNavHost`가 공통 화면 틀과 루트 그래프를 담당하고, 각 Feature가 자기 목적지·인자를 관리합니다. [UI 설계](specs/ui-designsystem-refactoring.md).
- **미디어:** Media3를 이용한 HLS·로컬 MP4 재생, 전체 화면·화질·자막·배속 설정과 플레이어 생명주기 관리.
- **채팅:** Fake·WebSocket/HTTP 채팅. `LocalChatConnectionCoordinator`가 연결 검증을, `ChatViewModel`이 룸·Job·Transport 수명을, `ChatTimelineStore`가 ACK·순번·중복·충돌·메모리 임대를 담당합니다. 실제 누락과 프로토콜 경고·수신 오류를 분리했습니다.
- **상태 관리:** Hilt로 Repository·일반 ViewModel 네 개를 Activity 범위에서 사용합니다. `ChatViewModel`은 룸별 Navigation 항목에, DM·댓글 초안 ViewModel은 해당 대화·게시물 BackStackEntry에 속합니다. 초안은 목적지 최종 제거 시 해제하며 프로세스 종료 후 복원하지 않습니다.

## 2026-11-23 Android 멀티모듈 리팩토링 완료

기존 11개 모듈을 유지하면서 **기능별 Navigation 목적지·인자와 단위 테스트를 각 Feature로 이동**했습니다. `app`에는 공통 화면 틀·기능 간 이동·Hilt DI 구성을 남기고, 카탈로그 조회와 아티스트 조회 계약을 분리했습니다. Gradle이 평가한 의존성과 공개 API 경계를 검사하도록 CI를 강화했습니다.

[PR #81](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/81)을 Squash Merge했습니다. Android·서버·문서 CI와 필수 품질 게이트가 통과했고, API 36 에뮬레이터에서 선택한 화면·Hilt 계측을 실행했습니다. **실기기·정량 성능·실제 채팅 서버 연동은 이번 리팩토링에서 재실행하지 않았습니다.** [단계별 결과와 미실시 범위](verification.md#2026-11-23-android-멀티모듈-리팩토링)

## 2026-11-18 리팩토링 최종 완료

| PR | 구현 범위 | 최종 상태 |
|---|---|---|
| [#74](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/74) | 채팅 연결 검증·메시지 정합성 책임 분리 | `623feb4`로 병합 |
| [#75](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/75) | Media 누락 아티스트 대체·미사용 RoomList 정리 | `3fdfaef`로 병합 |
| [#76](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/76) | 실제 누락 복구와 프로토콜·수신 종료 경고 구분 | `71644df`로 병합 |
| [#77](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/77) | 목적지별 DM·댓글 초안, 접수 성공 후 초기화 | `cf4b731`로 병합 |

네 PR 모두 `main`에 반영됐습니다. [#76 최종 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43922328335)와 [#77 최신 main 통합 PR CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43926318804)는 통과했습니다. 통합 CI의 Android 계측 APK **컴파일**과 기기에서의 실제 계측 **실행**은 다른 검증입니다. [세부 실행 결과·미실시 범위](verification.md)를 참조하세요.

## 이전 변경과 검증 (2026-11-09까지)

| 변경 | 확인할 내용 | 근거 |
|---|---|---|
| PR #69 · Android 검증 보강 | 실기기 회전 진단, 격리 서버 통합, 테스트 원본·무결성 검사와 PR 품질 게이트 | [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69) · [감사 보고](evidence/verification/audit-20261108/audit-report.md) |
| PR #70 · UI 구조 정리 | Media 카드 레이아웃 재사용과 앱 화면 조립 책임 분리. API 36 에뮬레이터·실기기 계측 각 43/43, Hilt 3/3 기록 | [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70) · [검증 문서](verification.md) |
| PR #70 병합 후 CI | Android 빌드·Lint·단위 테스트 및 계측 APK 컴파일, 서버 통합, 문서·증거 검사 | [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381222) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381250) · [문서 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381240) |

`main`에는 GitHub Actions의 `required-quality`가 필수 체크로 설정돼 있습니다. 다만 관리자는 이 정책을 우회할 수 있으며, **실패한 PR이 실제로 병합 차단되는지 확인하는 실험은 수행하지 않았습니다**. [정책 기록](specs/ci-required-quality-policy.md). Android CI의 계측 APK **컴파일**과 로컬 기기에서의 테스트 **실행**도 구분합니다.

## 남아 있는 확인 사항

- 최초 회전 테스트의 간헐 실패 원인은 재현되지 않아 확정하지 못했습니다. 후속 진단 결과는 [검증 증거](evidence/verification/audit-20261108/README.md)에 남겼습니다.
- PR #70에서는 변경 전후 스크린샷 비교와 실제 서버·장시간 누수 검사를 다시 실행하지 않았습니다. 과거 실기기 서버 통합 기록과는 별개입니다.
- 장시간 Player/Socket 누수 부재, 프로세스 강제 종료 후 복원, 전 기기 호환성을 입증한 것은 아닙니다. R-02의 프로세스 종료 정책은 새로운 ViewModel 소유자·저장 코드 부재로 확인했으며 실제 OS 강제 종료 후 복원 계측은 미실시입니다.

이전 Hilt 도입 단계의 PASS/FAIL/NOT_RUN은 [PR #67 검증 기록](specs/hilt-di-migration-verification.md)과 [후속 감사](evidence/verification/audit-20261108/audit-report.md)에 날짜·커밋별로 보존했습니다.

## 2026-11-16 Android 리뷰·당시 제출 과정 (이력)

로컬 `main` `5d335ec2f26d4655f713083ecded85b06b44806e`를 기준으로 상태 소유권·채팅 정합성·자원 수명과 테스트를 검토했다. [상세 리팩토링 계획](specs/android-refactoring-plan.md)에 확인한 문제, 단계별 변경 범위·수용 기준과 미정 제품 동작을 보존했다. Media 카드 공통화와 AppShell/NavHost 분리는 이미 적용된 현재 구조로 취급한다.

사용자 지시에 따라 **작업 A: 채팅 책임 분리(R-03·R-04) → 작업 B: 누락 데이터 처리·잔존 코드 정리(R-05·R-06)** 순으로 먼저 진행하도록 작업 문서를 갱신했다. 각 작업의 구현·검증 후 **각각 커밋·푸시·PR 생성·해당 CI 확인**을 수행하고, 두 작업이 완료되면 **후속 작업을 시작하지 않고 일시 정지·종합 보고**한다. 내부 단계들을 두 작업의 제출 단위로 묶으며 PR은 작업별 하나다.

**당시 상태: 지정 두 작업 제출 완료 · 후속 작업 일시 정지.** 작업 A는 구현 커밋 `31dac148`과 [PR #74](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/74), 작업 B는 구현 커밋 `2bf7554d`와 [PR #75](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/75)로 각각 커밋·푸시·제출했다. [작업 A 구현 head CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43861023966)와 [작업 B 구현 head CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43866791104)가 통과했다. 최종 보고 문서 변경 이후의 최신 head 상태는 각 PR Checks를 기준으로 확인한다.

당시에는 제품 변경을 두 PR로 분리해 제출하고 병합하지 않았다. 로컬 실행의 명령·결과·초기 실패·재실행·한계는 [작업 A 검증](verification.md#android-refactor-a-20261116)과 [작업 B 검증](verification.md#android-refactor-b-20261116)을 따른다. 누락 아티스트는 사용자 선택 D-03에 따라 대체 이미지·한국어/영어 이름으로 표시하며 카드와 영상 클릭을 유지한다. 구형 룸 목록의 의미 있는 테스트는 현재 Discover 경로로 이관했다.

채팅 오류 상태 변경(R-01)과 초안 보존 변경(R-02), 추가 기능·정리는 시작하지 않고 사용자 재개 지시를 기다린다. 이후 2026-11-17의 정책 결정은 아래에 구분한다.

리뷰에서 실행한 검사와 초기 실패·재실행의 범위는 [검증 기록](verification.md#android-review-20261116)을 따른다. 과거 Hilt·UI 검증과 이번 리뷰의 소스·환경을 합산하지 않는다. 실제 진행 상태·커밋·PR·CI 및 일시 정지 보고 기준은 [상세 계획의 8절](specs/android-refactoring-plan.md#8-변경-단위-진행-상태와-되돌리기)을 따른다.

## 2026-11-17 R-01·R-02 정책 확정

사용자가 제시된 권장 조합 전체를 A로 선택했다. [확정 기준](specs/android-refactoring-plan.md#4-구현-전에-결정할-제품-동작)에 기록했으며, 정책 확정 후 사용자 구현 지시를 받아 아래 후속 구현 단계로 진행했다.

- **R-01:** 오류 유형을 구분하고 실제 누락만 재조회한다. 충돌 메시지는 제외하면서 정상 채팅을 계속하고, 충돌 경고는 룸 세션 동안 유지한다. 기존 확정 메시지·미확정 전송·초안을 자동 삭제하지 않는다.
- **R-02:** DM·댓글 초안은 목적지 수명 동안 메모리에 보관한다. 회전·Activity 재생성과 살아 있는 저장 목적지에서는 유지하고 최종 제거 시 삭제하며, 프로세스 종료 후에는 복원하지 않는다. 대상별 초안을 분리하고 전송 접수 성공 때만 초기화하며 거절·오류 때는 유지한다.

## 개발 기준

설계와 구현 근거는 [아키텍처](architecture.md)·[ADR](adr/)·[SDD](specs/)에서 확인할 수 있습니다. 변경별 테스트는 [테스트 매트릭스](test-matrix.md), 코딩 에이전트 작업 규칙은 [AGENTS.md](../AGENTS.md)를 따릅니다.

## 2026-11-17 R-01·R-02 구현 제출 (당시 이력)

사용자가 승인한 A 기준으로 두 과제를 각각 구현·로컬 검증·커밋·푸시했다. R-01은 `e940d0c`와 [PR #76](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/76), R-02는 `a4aa026`과 [PR #77](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/77)로 제출했다. 당시 제출 head CI는 각각의 PR Checks로 확인했고, 그 제출 단계에서는 PR 병합을 수행하지 않았다. 이후 두 PR 모두 병합됐으며 최신 상태는 상단 완료 표를 따른다.

실제 누락 복구와 프로토콜·수신·인증 오류를 구분했고, 목적지 메모리 초안은 실제 접수 성공 때만 초기화한다. 초안은 회전·재생성·저장 목적지 복원에서 유지하고 최종 pop·새 프로세스 소유자에서는 비운다. 기존 일반 ViewModel·Player·Socket 소유권과 채팅 보호 규칙을 유지한다. [전체 명령·결과·제한](verification.md) · [현재 실행 계획](specs/android-refactoring-plan.md#13-2026-11-17-r-01r-02-구현제출).


