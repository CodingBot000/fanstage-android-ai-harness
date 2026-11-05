# FanStage v0.1 — Portfolio Ready 최종 마감 기록

- **판정일:** 2026-11-05
- **최종 판정:** `PORTFOLIO_READY` — Android 채용 지원용 **소스 공개 포트폴리오 제출 준비 완료**
- **제공 범위:** 공개 GitHub 소스, README 로컬 빌드·설치 안내, 합성 VOD/Fake 채팅 시연 영상, 아키텍처·CI·장애 주입·성능 실험의 재현 근거
- **제외 범위:** 공개 APK 또는 서명된 Release, 운영용 WebSocket·TLS·인증, 실제 원격 방송, 상용 서비스 및 바이너리·컨테이너 재배포
- **정확한 의미:** 제출 목적의 산출물 마감이다. 모든 배포·법적·성능·운영 위험에 대한 무제한 `VERIFIED` 선언이 아니다.

## 확보된 증거

1. **핵심 구현:** Kotlin·Compose, Media3 HLS 합성 VOD, Fake 및 실제 로컬 개발용 WebSocket/HTTP 채팅, Spring Boot·PostgreSQL, ACK/순서·중복·누락 대응. [아키텍처와 증거](../portfolio/architecture-and-evidence.md).
2. **클린 체크아웃 재현:** 기존 macOS 개발 환경에서 `e09beb205dcf1ae12510225dff710e3e05682cb8` 새 clone, JDK 17/기존 SDK·키·Gradle 캐시로 빌드·lint·단위 51/51 PASS, 신규 API 36 에뮬레이터에 설치하여 룸 목록→VOD 재생/일시정지/재개→Fake 전송 확인→목록 복귀 PASS. [재현 기록](M8-clean-clone-reproduction.md), [PR #33](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/33).
3. **CI 및 실제 서버 장애 테스트:** [Android main CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42004905245), [PostgreSQL·WebSocket Android 통합 main CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42004905195) PASS. 테스트의 상세 범위는 원본 로그와 [M5 기록](../specs/M5-fault-recovery.md)에 따른다.
4. **RC 및 시연:** rcLocal 실기기 정상·오류·Retry 경로의 제한된 검증([M8-001 보고서](M8-001-release-candidate.md), [PR #30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30)). 공개 영상은 합성 VOD/Fake 채팅의 화면 흐름이며 실제 네트워크 장애 복구 장면이 아니다. [시연 설명](../portfolio/demo-and-faults.md), [PR #31](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/31).
5. **저작권 및 고지:** 프로젝트 직접 작성 코드·문서에 [MIT 라이선스](../../LICENSE), Gradle Wrapper의 Apache-2.0 원문과 [제3자 고지](../../THIRD_PARTY_NOTICES.md), Android·서버 선택 구성의 고유 의존성 190개 조사([감사](M8-license-audit.md), [PR #34](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/34)).

## 제출 범위에서 명시할 제한과 미해결 위험

- **기존 WebSocket CI 타임아웃:** 최초 20초 실패의 원인과 제품 영향은 미확정. [PR #30 한정 사용자 승인 예외](M8-001-risk-exception-proposal.md)는 향후 실패에 적용하지 않는다.
- **성능:** M6 A/B 두 변경은 효과를 입증하지 못하거나 악화가 관찰되어 되돌렸다. 현재 제품의 성능 개선 성공을 주장하지 않는다. [M6 실험](M6-002-ab-experiments.md).
- **촬영 자산과 권리:** 현재 MP4에서 Google 키보드 장면은 제외했다. 과거 Git 이력의 해당 영상과 일부 에뮬레이터 UI·폰트의 구체적인 포트폴리오 사용 권리 판단은 확정되지 않았다. 이력이 제거되거나 모든 법적 위험이 해소됐다고 주장하지 않는다.
- **소스와 배포의 차이:** 소스에 포함된 Wrapper·제3자 고지는 기록했지만, 미래에 APK·서버 JAR·컨테이너를 배포한다면 실제 산출물의 내장 의존성·NOTICE·라이선스·운영 보안을 다시 검토해야 한다.
- **검증 경계:** 원격 LIVE 첫 프레임, 실제 LMK, 장시간 heap, 운영 자격증명/TLS, 실기기 물리 네트워크 전환과 대규모 운영 부하는 본 소스 포트폴리오의 제출 근거에 포함하지 않는다.

## M8 종료 결정

M8-001 RC, M8-002 포트폴리오 문서·시연, 추가 재현 기록 및 소스 대상 라이선스 감사가 병합됐다. README 빌드·실행, 기본 시연, CI 링크, 아키텍처, 실패·회귀 기록, 실험의 미입증 결과가 제출 목적에 맞게 연결된다. 따라서 **v0.1 소스 포트폴리오의 준비 작업을 마감**하고 지원서에 [저장소](https://github.com/CodingBot000/fanstage-android-ai-harness)를 사용한다.

이 마감은 신규 테스트를 실행하거나 기존 `NOT_RUN`을 `PASS`로 변경하지 않는다. 추가 기능·공개 바이너리·운영 배포는 별도 후속 범위다.
