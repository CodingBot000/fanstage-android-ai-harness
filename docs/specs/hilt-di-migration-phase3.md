# Hilt DI Migration — Phase 3

## 최종 판정 (2026-11-08 / PR #67)

- **구현·정적 검사:** PASS. 기본 Android CI와 별도 로컬 Hilt 테스트 APK 컴파일 근거를 구분한다.
- **기기 회귀:** Galaxy S22 Ultra/API 36 선택 일반 계측 39/39 및 Hilt 바인딩 1/1 PASS(로컬 기록).
- **실제 프로토콜 연결:** API 36 에뮬레이터의 격리 PostgreSQL·WebSocket Android 통합 1/1 PASS(로컬 기록).
- **당시 미실행:** PR #67 시점의 실기기 실제 서버 연결, 장시간 누수·정량 성능과 전 기기 검증은 `NOT_RUN`이었다. 후속 감사 결과는 아래를 참조한다.
- **추적:** [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) · [명령·실패·수정 이력](hilt-di-migration-verification.md). 아래 기록은 수정 전 실패를 포함한 시간순 이력이다.


상태: 빌드·정적 게이트, 선택 실기기 계측 및 격리 서버 Android 통합 PASS. 후속 감사에서 실기기 서버 연결과 30회 Collector 완료·소켓 참조 해제 검사도 통과했으며 장시간 누수 부재는 미확인이다. Hilt 테스트 전용 선택적 계측 구성에서 Repository 바인딩 대체를 검증하고 기본 일반 계측 러너 및 애플리케이션은 유지한다. Hilt 테스트 러너는 전용 Gradle 속성으로만 활성화하고 해당 테스트 클래스만 실행한다. 전체 빌드·lint·단위·모듈/manifest·benchmark 서명과 기기가 있으면 계측 및 재생/채팅 수명주기 회귀를 수행한다. 기기가 없으면 계측은 NOT_RUN. 테스트용 명시적 Transport Factory는 유지하고 프로덕션 중복 조립을 제거한다. Media3 소유권 변경은 범위 밖이다.

Open Questions: 없음.

## 실행 결과 (2026-11-08)

선택적 Hilt 계측 소스·러너 및 Repository 대체 바인딩 테스트를 추가했다. 전용 APK 빌드와 이후 일반 APK 빌드 PASS. 기본 러너와 프로덕션 Application은 유지한다. 전체 Gradle 빌드·lint·단위 PASS (825 tasks); app 6, live 58, core:data 4 tests PASS; 모듈·manifest·서명 검사 PASS. 기기 미연결로 대체 바인딩 테스트, UI 및 자원 수명주기 계측은 NOT_RUN. 상세 명령과 전환 중 실패 이력은 [검증 기록](../verification.md).

## 실기기 후속 확인

Hilt 대체 바인딩 및 별도 룸 재생성/재진입 각 1/1 PASS. 일반 회귀 36/37 PASS, LIVE 재생 상태 한 건은 기준 커밋에서도 재현된 FAIL로 미해결. 서버 통합·장시간 누수 NOT_RUN. 상세는 [검증 기록](../verification.md).

실기기 실패 원인은 키보드 compact 모드에서 상태 테스트 태그가 사라지는 것이다. 동일 Player가 채팅 전송 후 재생 중임을 별도 계측으로 확인했으나 기존 테스트는 아직 수정·재실행 전이다. [상세](../verification.md).

## 회귀 수정 후 재검증

실제 Player로 재생 상태를 확인하고 회전 후 일시정지 재생 버튼 표시를 복구했다. 수정된 LIVE 통합 테스트와 선택 실기기 회귀 39/39, 별도 Hilt 바인딩 1/1 및 빌드·lint·단위·경계 검사 PASS. 서버·장시간 검증은 NOT_RUN. [결과](../verification.md).

## 격리 서버 Android 통합 후속 검증

API 36 전용 에뮬레이터와 격리된 PostgreSQL·WebSocket 서버에서 `RealChatIntegrationTest` 1/1 PASS. ACK 유실·재전송, 지연 응답, 재연결과 재생 분리 시나리오를 실행했다. 실기기의 실제 서버 연결과 장시간 누수·성능은 NOT_RUN. 상세는 [작업 검증 기록](hilt-di-migration-verification.md).

## 최종 검증 감사

후속 감사에서 실기기 Hilt 생성 경로와 실제 서버 연결, 룸별 반복 연결에서 Collector 완료와 소켓 참조 해제를 확인했다. 초기 회전 실패와 재실행, 성능·정책 한계는 [감사 보고](../evidence/verification/audit-20261108/audit-report.md)에 분리했다.
