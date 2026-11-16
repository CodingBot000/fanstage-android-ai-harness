# FanStage 개발 현황

**구현 기준:** [PR #70 병합 커밋 `5d335ec`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/5d335ec2f26d4655f713083ecded85b06b44806e) (2026-11-09).

## 현재 구현

- **UI:** Discover·Community·Media·Messages·LIVE 화면. Kotlin·Compose 기반 11개 Gradle 모듈에서 `core:designsystem`을 재사용합니다. Media 카드 3종은 `MediaArtworkCard`로 묶고, 앱의 Scaffold와 Navigation 구성은 `FanStageAppShell`·`FanStageNavHost`로 나눴습니다. [UI 설계](specs/ui-designsystem-refactoring.md).
- **미디어:** Media3를 이용한 HLS·로컬 MP4 재생, 전체 화면·화질·자막·배속 설정과 플레이어 생명주기 관리.
- **채팅:** Fake 채팅과 로컬 WebSocket·PostgreSQL 연동. ACK 유실 후 재전송, 중복·역순 메시지 처리, HTTP 누락 조회.
- **상태 관리:** Hilt로 Repository·ViewModel을 주입합니다. 일반 ViewModel은 Activity 범위에서 공유하고 `ChatViewModel`은 룸별 Navigation 항목에 연결합니다.

## 최근 변경과 검증

| 변경 | 확인할 내용 | 근거 |
|---|---|---|
| PR #69 · Android 검증 보강 | 실기기 회전 진단, 격리 서버 통합, 테스트 원본·무결성 검사와 PR 품질 게이트 | [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69) · [감사 보고](evidence/verification/audit-20261108/audit-report.md) |
| PR #70 · UI 구조 정리 | Media 카드 레이아웃 재사용과 앱 화면 조립 책임 분리. API 36 에뮬레이터·실기기 계측 각 43/43, Hilt 3/3 기록 | [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70) · [검증 문서](verification.md) |
| PR #70 병합 후 CI | Android 빌드·Lint·단위 테스트 및 계측 APK 컴파일, 서버 통합, 문서·증거 검사 | [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381222) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381250) · [문서 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42503381240) |

`main`에는 GitHub Actions의 `required-quality`가 필수 체크로 설정돼 있습니다. 다만 관리자는 이 정책을 우회할 수 있으며, **실패한 PR이 실제로 병합 차단되는지 확인하는 실험은 수행하지 않았습니다**. [정책 기록](specs/ci-required-quality-policy.md). Android CI의 계측 APK **컴파일**과 로컬 기기에서의 테스트 **실행**도 구분합니다.

## 남아 있는 확인 사항

- 최초 회전 테스트의 간헐 실패 원인은 재현되지 않아 확정하지 못했습니다. 후속 진단 결과는 [검증 증거](evidence/verification/audit-20261108/README.md)에 남겼습니다.
- PR #70에서는 변경 전후 스크린샷 비교와 실제 서버·장시간 누수 검사를 다시 실행하지 않았습니다. 과거 실기기 서버 통합 기록과는 별개입니다.
- 장시간 Player/Socket 누수 부재, 프로세스 강제 종료 후 복원, 전 기기 호환성을 입증한 것은 아닙니다.

이전 Hilt 도입 단계의 PASS/FAIL/NOT_RUN은 [PR #67 검증 기록](specs/hilt-di-migration-verification.md)과 [후속 감사](evidence/verification/audit-20261108/audit-report.md)에 날짜·커밋별로 보존했습니다.

## 2026-11-16 Android 리뷰와 리팩토링 계획

로컬 `main` `5d335ec2f26d4655f713083ecded85b06b44806e`를 기준으로 상태 소유권·채팅 정합성·자원 수명과 테스트를 검토했다. [상세 리팩토링 계획](specs/android-refactoring-plan.md)에 확인한 문제, 단계별 변경 범위·수용 기준과 미정 제품 동작을 보존했다. Media 카드 공통화와 AppShell/NavHost 분리는 이미 적용된 현재 구조로 취급한다.

사용자 지시에 따라 **작업 A: 채팅 책임 분리(R-03·R-04) → 작업 B: 누락 데이터 처리·잔존 코드 정리(R-05·R-06)** 순으로 먼저 진행하도록 작업 문서를 갱신했다. 각 작업의 구현·검증 후 **각각 커밋·푸시·PR 생성·해당 CI 확인**을 수행하고, 두 작업이 완료되면 **후속 작업을 시작하지 않고 일시 정지·종합 보고**한다. 내부 단계들을 두 작업의 제출 단위로 묶으며 PR은 작업별 하나다.

**지정 두 작업 완료 · 후속 작업 일시 정지.** 작업 A는 구현 커밋 `31dac148`과 [PR #74](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/74), 작업 B는 구현 커밋 `2bf7554d`와 [PR #75](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/75)로 각각 커밋·푸시·제출했다. [작업 A 구현 head CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43861023966)와 [작업 B 구현 head CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/43866791104)가 통과했다. 최종 보고 문서 변경 이후의 최신 head 상태는 각 PR Checks를 기준으로 확인한다.

제품 변경은 두 PR로 분리했으며 병합하지 않았다. 로컬 실행의 명령·결과·초기 실패·재실행·한계는 [작업 A 검증](verification.md#android-refactor-a-20261116)과 [작업 B 검증](verification.md#android-refactor-b-20261116)을 따른다. 누락 아티스트는 사용자 선택 D-03에 따라 대체 이미지·한국어/영어 이름으로 표시하며 카드와 영상 클릭을 유지한다. 구형 룸 목록의 의미 있는 테스트는 현재 Discover 경로로 이관했다.

채팅 오류 상태 변경(R-01)과 초안 보존 변경(R-02), 추가 기능·정리는 시작하지 않고 사용자 재개 지시를 기다린다. 이후 2026-11-17의 정책 결정은 아래에 구분한다.

리뷰에서 실행한 검사와 초기 실패·재실행의 범위는 [검증 기록](verification.md#android-review-20261116)을 따른다. 과거 Hilt·UI 검증과 이번 리뷰의 소스·환경을 합산하지 않는다. 실제 진행 상태·커밋·PR·CI 및 일시 정지 보고 기준은 [상세 계획의 8절](specs/android-refactoring-plan.md#8-변경-단위-진행-상태와-되돌리기)을 따른다.

## 2026-11-17 R-01·R-02 정책 확정

사용자가 제시된 권장 조합 전체를 A로 선택했다. [확정 기준](specs/android-refactoring-plan.md#4-구현-전에-결정할-제품-동작)에 기록했으며, 현재는 결정 문서 반영 단계로 제품 구현은 미착수다.

- **R-01:** 오류 유형을 구분하고 실제 누락만 재조회한다. 충돌 메시지는 제외하면서 정상 채팅을 계속하고, 충돌 경고는 룸 세션 동안 유지한다. 기존 확정 메시지·미확정 전송·초안을 자동 삭제하지 않는다.
- **R-02:** DM·댓글 초안은 목적지 수명 동안 메모리에 보관한다. 회전·Activity 재생성과 살아 있는 저장 목적지에서는 유지하고 최종 제거 시 삭제하며, 프로세스 종료 후에는 복원하지 않는다. 대상별 초안을 분리하고 전송 접수 성공 때만 초기화하며 거절·오류 때는 유지한다.

## 개발 기준

설계와 구현 근거는 [아키텍처](architecture.md)·[ADR](adr/)·[SDD](specs/)에서 확인할 수 있습니다. 변경별 테스트는 [테스트 매트릭스](test-matrix.md), 코딩 에이전트 작업 규칙은 [AGENTS.md](../AGENTS.md)를 따릅니다.
