# Hilt DI Migration — Phase 3

상태: 빌드·정적 게이트 PASS, 기기 검증 NOT_RUN. Hilt 테스트 전용 선택적 계측 구성에서 Repository 바인딩 대체를 검증하고 기본 일반 계측 러너 및 애플리케이션은 유지한다. Hilt 테스트 러너는 전용 Gradle 속성으로만 활성화하고 해당 테스트 클래스만 실행한다. 전체 빌드·lint·단위·모듈/manifest·benchmark 서명과 기기가 있으면 계측 및 재생/채팅 수명주기 회귀를 수행한다. 기기가 없으면 계측은 NOT_RUN. 테스트용 명시적 Transport Factory는 유지하고 프로덕션 중복 조립을 제거한다. Media3 소유권 변경은 범위 밖이다.

Open Questions: 없음.

## 실행 결과 (2026-11-08)

선택적 Hilt 계측 소스·러너 및 Repository 대체 바인딩 테스트를 추가했다. 전용 APK 빌드와 이후 일반 APK 빌드 PASS. 기본 러너와 프로덕션 Application은 유지한다. 전체 Gradle 빌드·lint·단위 PASS (825 tasks); app 6, live 58, core:data 4 tests PASS; 모듈·manifest·서명 검사 PASS. 기기 미연결로 대체 바인딩 테스트, UI 및 자원 수명주기 계측은 NOT_RUN. 상세 명령과 전환 중 실패 이력은 [검증 기록](../verification.md).
