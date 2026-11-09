# FanStage 개발 현황

**구현 기준:** [PR #70 병합 커밋 `5d335ec`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/5d335ec2f26d4655f713083ecded85b06b44806e) (2026-11-09). 이 문서의 변경은 Android 앱 코드를 수정하지 않습니다.

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

## 개발 기준

설계와 구현 근거는 [아키텍처](architecture.md)·[ADR](adr/)·[SDD](specs/)에서 확인할 수 있습니다. 변경별 테스트는 [테스트 매트릭스](test-matrix.md), 코딩 에이전트 작업 규칙은 [AGENTS.md](../AGENTS.md)를 따릅니다.
