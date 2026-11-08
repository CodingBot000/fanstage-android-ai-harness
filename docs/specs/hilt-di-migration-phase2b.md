# Hilt DI Migration — Phase 2B

상태: 구현 및 정적 게이트 PASS; 기기 검증 NOT_RUN. ChatViewModel을 Hilt로 생성하되 roomId는 Navigation 인자가 제공하는 SavedStateHandle에서 읽고 기존 NavBackStackEntry 소유자를 명시한다. 각 ViewModel마다 FakeChatTransport를 별도 생성하고 연결·교체·종료 로직은 보존한다. 기존 Factory는 계측·벤치마크·단위 테스트가 명시적인 Transport를 주입하는 용도로만 유지한다. 누락 인자는 명시적으로 실패한다. Chat 복구·중복·재전송·교체·자원 회귀 테스트 및 Phase 1과 동일한 빌드·정적 게이트를 실행하며 기기 없으면 계측은 NOT_RUN이다. 토큰은 SavedStateHandle에 저장하지 않는다.

Open Questions: 없음.

## 결과 (2026-11-08)

Navigation BackStackEntry를 소유자로 명시하고 SavedStateHandle에서 roomId를 전달한다. 기존 재전송·복구·교체·정리 구현은 유지했다. 누락 인자 및 독립 Transport 테스트 추가, live 단위 테스트 PASS. JDK 21 및 로컬 SDK에서 Phase 1 SDD 전체 Gradle 명령 PASS (823 tasks), 모듈·manifest·benchmark 서명·diff 검사 PASS. 실제 서버·Activity 재생성·룸 재진입 계측은 기기 미연결로 NOT_RUN.
