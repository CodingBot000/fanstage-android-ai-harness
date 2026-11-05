# M8-002 — Portfolio Packaging

- 상태: 산출물 작성·검증·독립 리뷰 완료, [PR #31](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/31) Squash Merge `3a1f0d79d1d3a9939a715d3ddd7ad6e1c07838ce`. M8 전체 Gate는 미완료이며 별도 확인한다.
- 목표: 외부 검토자가 기능, 기술적 선택, 실제 검증과 한계를 빠르게 이해하고 소스로 데모를 재현할 수 있게 한다.
- 범위: README, 아키텍처와 데이터 흐름, 측정 결과 및 근거 링크, 합성 데모 시연 자료, 장애·복구 검증과 AI 작업 방식. 기존 코드 계약과 정책을 변경하지 않는다.
- 배포: v0.1은 소스와 로컬 빌드 절차로 제공하며 공개 APK는 제공하지 않는다. 루트 라이선스 및 배포 서명은 미결정이다. 제3자 고지 의무 검토는 미완료로 표시한다.
- 근거: [M8-001 보고서](../reviews/M8-001-release-candidate.md), [일회성 예외](../reviews/M8-001-risk-exception-proposal.md), [M5 장애 검증](M5-fault-recovery.md), [M6 실험](../reviews/M6-002-ab-experiments.md), [검증 기록](../../harness/build-log.md).
- 계약: 기본 데모는 합성 VOD·Fake 채팅이며 원격 LIVE 방송이나 운영 서비스가 아니다. 실제 WebSocket은 별도 로컬 개발 환경이다. 제품 성능 개선은 입증되지 않았고 기존 M6 변경은 되돌렸다. 원격 LIVE·실제 LMK·장시간 heap·실기기 네트워크 전환은 NOT_RUN이다.
- 시연: 화면 영상은 실제로 캡처한 동작만 표시하고 장애·복구는 별도 실행 증거와 구분한다. 개인정보 및 실기기 원본 추적 데이터는 공개하지 않는다.
- 완료 조건: 문서 링크·빌드 재현 절차·주장과 증거 정합성, 콘텐츠 권리 및 포트폴리오 검토를 확인한다. 미검증 또는 미결정 항목은 숨기지 않는다.
