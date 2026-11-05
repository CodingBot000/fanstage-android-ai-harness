# M8-001 — Release Candidate 검증

- 상태: M8-001 완료 ([PR #30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30), 사용자 승인 일회성 위험 수용 예외 적용). M8 전체 완료는 M8-002 이후 별도 판단한다.
- 목표: v0.1 오프라인 데모의 릴리스 빌드, 핵심 흐름과 공개 적합성을 증거로 확인한다.
- 범위: 합성 HLS VOD, Fake 채팅, 한국어·영어, 릴리스 보안 경계와 격리된 개발 서버 검증. 신규 기능 및 장시간 성능 측정은 제외한다.
- 계약: 일반 릴리스는 cleartext를 허용하지 않는다. 실제 서버 데모는 Debug와 임시 토큰을 이용하는 로컬 개발 전용이다. 기존 서버와 프로토콜을 변경하지 않는다.
- 검증: 정확한 소스 SHA의 기존 CI를 재사용하고 릴리스 APK 자체의 설치·동작은 별도 검증한다. [검증 보고서](../reviews/M8-001-release-candidate.md)에 실행 사실과 한계를 기록한다.
- 완료 기준: 로컬 검증 전용 RC 설치·핵심 흐름, 공개 산출물 감사 범위 기록, 관련 P0/P1 검증·리뷰와 필수 CI. 최초 WebSocket 타임아웃의 미해결 P1에는 아래 승인 예외를 적용했다. 공개용 서명 자격 증명은 별도 승인 없이 만들지 않는다.
- Open Questions: 공개 배포용 서명 방식 및 저장소 라이선스 미결정. v0.1은 공개 APK 대신 소스와 로컬 재현 절차를 제공한다.

## 로컬 검증 전용 변형

`rcLocal`은 Release를 상속하고 기존 Debug 키가 있을 때만 재사용한다. 별도 ID `dev.fanstage.android.rclocal`, non-debuggable, cleartext 금지이며 benchmark 전용 Activity와 profileable을 포함하지 않는다. 일반 Release에 서명을 추가하지 않는다. 공개 배포용 APK가 아니며 GitHub Release에 올리지 않는다. 키가 없으면 생성하지 않고 서명·설치는 NOT_RUN이다. 실제 검증 결과와 남은 차단 사항은 보고서에 기록한다.

2026-11-05 사용자 결정: GitHub CI 빌드·테스트용 일회성 Debug 키 자동 생성은 허용한다. 공개 배포용 Release 키의 생성·등록은 별도 승인 전 금지한다. `fanstageRcInstrumentation`은 별도 테스트 APK의 대상만 rcLocal로 바꾸며 제품 APK에 오류 주입 화면을 추가하지 않는다.

2026-11-05: 사용자가 최초 WebSocket CI 20초 타임아웃의 미확정 위험에 대해 PR #30 한정 일회성 예외를 승인했다. 범위와 재조사 조건은 [승인 기록](../reviews/M8-001-risk-exception-proposal.md)에 있다. 원인 해결이나 제품 결함 부재는 입증되지 않았다. 향후 P1 실패·공개 APK·운영 검증에는 적용하지 않는다.
