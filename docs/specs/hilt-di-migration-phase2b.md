# Hilt DI Migration — Phase 2B

상태: 구현 및 정적 게이트 PASS; Phase 2B 종료 당시 기기 검증 `NOT_RUN`. 후속 선택 기기·격리 서버 검증은 [Phase 3 결과](hilt-di-migration-phase3.md)에 별도 기록. ChatViewModel을 Hilt로 생성하되 roomId는 Navigation 인자가 제공하는 SavedStateHandle에서 읽고 기존 NavBackStackEntry 소유자를 명시한다. 각 ViewModel마다 FakeChatTransport를 별도 생성하고 연결·교체·종료 로직은 보존한다. 기존 Factory는 계측·벤치마크·단위 테스트가 명시적인 Transport를 주입하는 용도로만 유지한다. 누락 인자는 명시적으로 실패한다. Chat 복구·중복·재전송·교체·자원 회귀 테스트 및 Phase 1과 동일한 빌드·정적 게이트를 실행하며 기기 없으면 계측은 NOT_RUN이다. 토큰은 SavedStateHandle에 저장하지 않는다.

Open Questions: 없음.

## 결과 (2026-11-08)

Navigation BackStackEntry를 소유자로 명시하고 SavedStateHandle에서 roomId를 전달한다. 기존 재전송·복구·교체·정리 구현은 유지했다. 누락 인자 및 독립 Transport 테스트 추가, live 단위 테스트 PASS. JDK 21 및 로컬 SDK에서 Phase 1 SDD 전체 Gradle 명령 PASS (823 tasks), 모듈·manifest·benchmark 서명·diff 검사 PASS. 실제 서버·Activity 재생성·룸 재진입 계측은 기기 미연결로 NOT_RUN.

## 최종 검증 연결 (Phase 2B 이후)

후속 작업에서 룸 재생성/재진입 및 채팅+Player 상태 확인을 포함한 실기기 선택 회귀 39/39, 격리 PostgreSQL·WebSocket의 API 36 Android 통합 1/1 PASS가 기록되었다. 실제 백엔드의 실기기 연결 및 장시간 Socket 누수 검증은 `NOT_RUN`이다. [원인 분석·실행 증거](hilt-di-migration-verification.md).
