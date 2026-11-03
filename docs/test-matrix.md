# 검증 매트릭스

M0-001에서 실제 실행 가능한 것은 문서 기반 G0 수동/명령 검증뿐이다. 다음은 계획이며 자동 게이트가 이미 구축되었다는 뜻이 아니다.

| 계층 | 내용 | 도입 시점 | 실행 환경 | 자동 차단 계획 |
|---|---|---|---|---|
| G0 | 문서·범위·시크릿 | M0 수동 검증; 자동화 시점 미정 | 로컬 / 향후 PR | 가능하면 차단, 현재 미구현 |
| G1 | Android assembleDebug · lintDebug | M1-003 | GitHub Actions PR/main | 실제 워크플로가 PASS이면 해당 빌드 차단 기준 충족 (설정만으로 PASS 아님) |
| G2 | JUnit/Coroutine 단위 테스트 | M1-003 | GitHub Actions PR/main | 실제 `testDebugUnitTest` PASS; NO-SOURCE 모듈 구분 |
| G3 | Emulator / UI | M1 이후 | 에뮬레이터 / 별도 CI job | 안정화 후 결정 |
| G4 | 실기기 성능 | M6 | 실기기, release-like 빌드 | PR 자동 차단 대신 측정·릴리스 증거 |
| G5 | AI 리뷰 | 도입 시점 미정 | 로컬 또는 PR 도구 | 보조 증거, 자동 승인 대체 불가 |

실행 결과와 구체적인 미실행 사유는 [build-log](../harness/build-log.md)를 따른다. 정책은 [마스터 플랜](MASTER_PLAN.md) 4.6절을 따른다.
