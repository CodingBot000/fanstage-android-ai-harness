# Hilt DI Migration — Phase 3

상태: 빌드·정적 게이트 및 선택 실기기 계측 PASS; 실제 서버·장시간 검증 NOT_RUN. Hilt 테스트 전용 선택적 계측 구성에서 Repository 바인딩 대체를 검증하고 기본 일반 계측 러너 및 애플리케이션은 유지한다. Hilt 테스트 러너는 전용 Gradle 속성으로만 활성화하고 해당 테스트 클래스만 실행한다. 전체 빌드·lint·단위·모듈/manifest·benchmark 서명과 기기가 있으면 계측 및 재생/채팅 수명주기 회귀를 수행한다. 기기가 없으면 계측은 NOT_RUN. 테스트용 명시적 Transport Factory는 유지하고 프로덕션 중복 조립을 제거한다. Media3 소유권 변경은 범위 밖이다.

Open Questions: 없음.

## 실행 결과 (2026-11-08)

선택적 Hilt 계측 소스·러너 및 Repository 대체 바인딩 테스트를 추가했다. 전용 APK 빌드와 이후 일반 APK 빌드 PASS. 기본 러너와 프로덕션 Application은 유지한다. 전체 Gradle 빌드·lint·단위 PASS (825 tasks); app 6, live 58, core:data 4 tests PASS; 모듈·manifest·서명 검사 PASS. 기기 미연결로 대체 바인딩 테스트, UI 및 자원 수명주기 계측은 NOT_RUN. 상세 명령과 전환 중 실패 이력은 [검증 기록](../verification.md).

## 실기기 후속 확인

Hilt 대체 바인딩 및 별도 룸 재생성/재진입 각 1/1 PASS. 일반 회귀 36/37 PASS, LIVE 재생 상태 한 건은 기준 커밋에서도 재현된 FAIL로 미해결. 서버 통합·장시간 누수 NOT_RUN. 상세는 [검증 기록](../verification.md).

실기기 실패 원인은 키보드 compact 모드에서 상태 테스트 태그가 사라지는 것이다. 동일 Player가 채팅 전송 후 재생 중임을 별도 계측으로 확인했으나 기존 테스트는 아직 수정·재실행 전이다. [상세](../verification.md).

## 회귀 수정 후 재검증

실제 Player로 재생 상태를 확인하고 회전 후 일시정지 재생 버튼 표시를 복구했다. 수정된 LIVE 통합 테스트와 선택 실기기 회귀 39/39, 별도 Hilt 바인딩 1/1 및 빌드·lint·단위·경계 검사 PASS. 서버·장시간 검증은 NOT_RUN. [결과](../verification.md).
