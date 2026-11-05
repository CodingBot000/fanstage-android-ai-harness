# M8-001 일회성 위험 수용 예외 — 승인

## 요청 범위

- 대상: [PR #30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30)의 M8-001 종료와 Squash Merge, 이후 M8-002 Portfolio Packaging 진입.
- 기준 head: `582ad33cec9c1d2c0cbdb56ae9f884873d3667cd`.
- 상태: **승인**. 2026-11-05 사용자가 이 문서에 명시된 최초 타임아웃의 미확정 위험을 PR #30에 한해 명시적으로 수용했다.
- 범위는 최초 WebSocket CI 타임아웃에 관한 아래 P1 차단의 **이번 한 번의 예외**다. 원인 해결이나 제품 결함 부재를 선언하지 않는다. 다른 P0/P1 실패와 향후 변경의 Gate에는 적용하지 않는다.

## 알려진 위험과 근거

- [서버 CI #41991999118](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41991999118) attempt 1에서 실제 Android WebSocket 통합 테스트가 20초 타임아웃으로 실패했다. 계측 추가 전이므로 정확한 대기 지점과 원인은 알 수 없다. 양측 CONNECTED 대기는 후보일 뿐이다. 제품 코드·테스트 경합·CI 환경 중 어느 쪽인지 판별하거나 제품 영향을 배제할 근거는 없다.
- 같은 실행의 attempt 2와 이후 계측 추가 후 로컬 동일 테스트 3회는 PASS였다. 기준 head의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41999435328)는 빌드·단위 및 API 35 계측 4건 PASS, [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41999435309)는 PASS였다. 후속 PASS는 그 환경에서의 결과이며 최초 실패의 원인 해결을 증명하지 않는다.
- RC의 오프라인 합성 VOD·Fake 채팅 정상/장애 흐름은 실기기에서 별도 검증했다. 실제 WebSocket은 별도 로컬 개발 환경에서 검증하며 운영 인증·TLS와 실기기 소켓 종료는 미검증이다. v0.1은 소스와 로컬 빌드 절차를 제공하고 공개 APK는 제공하지 않는다.
- 리뷰의 CI 테스트 수 불일치 P1과 진단 스택 보존 P2는 수정·검증 후 스레드가 해결됐다. 진단 스택 수정 후 최신 코드에 대한 별도의 신규 독립 리뷰는 확인되지 않았다. 마지막 코드·로그 검토에서 현재 재현 가능한 새로운 P0/P1은 확인하지 못했지만 제품 결함이 없다는 뜻은 아니다.

## 승인할 경우와 재조사 조건

승인에 따라 위 알려진 미확정 위험을 유지한 채 이 PR에 한해 P1 차단 예외를 적용했다. 라이선스 결정, 공개 배포용 서명 키 생성, 공개 APK 배포, 운영 환경 검증 또는 향후 P1 실패의 면제는 승인 범위가 아니다. PR #30은 `74e417ad604fab227a9fbca0f7e28f1728130859`로 Squash Merge됐다. [검증 보고서](M8-001-release-candidate.md) 참조.

동일한 타임아웃이나 채팅 연결·ACK·누락·복구 관련 실패가 다시 발생하면 단계 ID·연결 상태·프록시 상태가 남는 현재 진단 증거를 보존해 원인과 제품 영향을 재조사한다. 해당 변경의 차단 검증은 통과나 별도 결정 전까지 병합하지 않는다. 민감정보는 로그에 기록하지 않는다.

## 결정

사용자는 위 한정된 범위에서 PR #30 병합과 M8-002 진입을 승인했다. 최초 원인과 제품 영향은 미확정으로 남는다. 동일 문제가 재발하면 보존된 진단 증거로 다시 조사한다.
