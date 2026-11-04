# 검증 매트릭스 — 실행 사실과 Gate 분리

이 문서는 단계별 정책이다. 특정 검증의 실제 PASS/FAIL/NOT_RUN은 [STATUS](STATUS.md), [M1 독립 감사](reviews/M1-006-exit-gate-audit.md), [build-log](../harness/build-log.md)의 해당 commit/Run 증거를 따른다.

| 계층 | 대상 | 환경 / 도입 | 현재 검증 증거·제한 |
|---|---|---|---|
| G0 | 범위·SDD·문서·시크릿 정책 | M0, 이후 매 변경 | M0 종료 증거 존재. 시크릿 노출 여부는 매 작업 별도 검사 |
| G1 | Android assembleDebug, lintDebug | M1-003, PR/main Android CI | main [#41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) PASS; Lint 13 warning 부채, 로컬 `clean`은 NOT_RUN |
| G2 | FakeRepository/ViewModel JUnit | M1-003, PR/main Android CI | main Run의 XML 7/7 PASS; `:app:testDebugUnitTest` NO-SOURCE |
| G3 | API 35 Emulator Compose / Activity 재생성 | M1-004/005, 연결된 Android CI | main Run XML 1/1 PASS; 실제 회전·프로세스 종료/재실행·실기기 검증은 NOT_RUN |
| G4 | LIVE/채팅 회복 | M2~M5 각 SDD | NOT_RUN; M1 placeholder를 기능 완료로 표시 금지 |
| G5 | 실기기 성능/benchmark | M6 | NOT_RUN; raw 결과 없이는 성능 개선 주장 금지 |
| G6 | 독립 코드·품질 리뷰 | 각 변경 PR 및 단계 Gate | PR 리뷰·소스 감사 별도 확인; CI 초록색 배지는 리뷰 대체 불가 |

모든 게이트는 **실제 실행한 테스트와 실행 환경**을 기준으로 판단한다. PR의 README/문서 변경만으로 새 Android CI PASS를 주장할 수 없고, 원본 ZIP을 이 실행에서 새로 해시하지 않았다면 과거 검증 값과 이번 확인을 구분한다. 장기 실행 서버/유료 인프라 없이 M0~M8 범위 내에서 재현한다.
