# 검증 매트릭스 — 실행 사실과 Gate 분리

이 문서는 단계별 정책이다. 특정 검증의 실제 PASS/FAIL/NOT_RUN은 [STATUS](STATUS.md), [M1 독립 감사](reviews/M1-006-exit-gate-audit.md), [build-log](../harness/build-log.md)의 해당 commit/Run 증거를 따른다.

| 계층 | 대상 | 환경 / 도입 | 현재 검증 증거·제한 |
|---|---|---|---|
| G0 | 범위·SDD·문서·시크릿 정책 | M0, 이후 매 변경 | M0 종료 증거 존재. 시크릿 노출 여부는 매 작업 별도 검사 |
| G1 | Android assembleDebug, lintDebug | M1-003, PR/main Android CI | main [#41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) PASS; Lint 13 warning 부채; M1-007 로컬 clean PASS는 build-log 참조 |
| G2 | FakeRepository/ViewModel JUnit | M1-003, PR/main Android CI | main Run의 XML 7/7 PASS; `:app:testDebugUnitTest` NO-SOURCE |
| G3 | API 35 Emulator Compose / Activity 재생성 | M1-004/005, 연결된 Android CI | main Run XML 1/1 PASS; 실제 회전·프로세스 종료/재실행·실기기 검증은 NOT_RUN |
| G4 | LIVE/채팅 회복 | M2~M5 각 SDD | M3 Fake 및 M4 실제 WS/HTTP 복구 PASS; M5 제한된 로컬 장애 주입 PASS, 실제 장치 네트워크 전환·장시간 부하 NOT_RUN. 상세는 M5 SDD 및 build-log |
| G5 | 실기기 성능/benchmark | M6 | NOT_RUN; raw 결과 없이는 성능 개선 주장 금지 |
| G6 | 독립 코드·품질 리뷰 | 각 변경 PR 및 단계 Gate | PR 리뷰·소스 감사 별도 확인; CI 초록색 배지는 리뷰 대체 불가 |

모든 게이트는 **실제 실행한 테스트와 실행 환경**을 기준으로 판단한다. PR의 README/문서 변경만으로 새 Android CI PASS를 주장할 수 없고, 원본 ZIP을 이 실행에서 새로 해시하지 않았다면 과거 검증 값과 이번 확인을 구분한다. 장기 실행 서버/유료 인프라 없이 M0~M8 범위 내에서 재현한다.

## Fast Track v1 차단 기준

- **P0 — 차단:** 컴파일 또는 핵심 테스트 실패, 시크릿 노출·중대한 보안 취약점, 데이터 정합성 오류, 명백한 리소스 누수, 승인 범위 밖 구현.
- **P1 — 관련 기능 완료 전 차단:** Player/Surface/Listener 해제, coroutine 취소·오래된 응답, 채팅 ACK·중복·역순·누락, 네트워크 복구·재진입 및 핵심 사용자 흐름. 해당 기능에 실제 영향이 있다면 미검증 또는 실패도 차단한다.
- **P2 — 추적 후 이월 가능:** 경미한 Lint 경고, 비핵심 README 문구, 미관·스타일, 비차단 리팩터링, 해당 단계와 무관한 선택 검증. M8 릴리스 전에 이월된 중요한 검증을 재평가한다.

G 계층은 검증 종류, P 등급은 영향과 차단 여부를 뜻한다. 변경 영향에 맞게 검증을 선택하고 기존 결과는 대상·환경·SHA를 명시해 재사용한다. 필수 Gate를 임의로 생략하거나 미실행을 PASS로 기록하지 않는다.


## 원격 에뮬레이터 임시 중지 (2026-11-06)
사용자 요청으로 GitHub Actions의 Android 에뮬레이터 실행만 임시 중지한다. 워크플로와 테스트 코드는 보존하고 false 조건 제거 시 복원한다. Android 빌드·lint·단위·계측 APK 컴파일과 서버 통합·장애 주입은 원격에서 계속한다. 영향을 받는 에뮬레이터/실제 서버 Android 검증은 병합 전 로컬 전용 에뮬레이터에서 실행하고 결과를 기록하며 미실행은 NOT_RUN이다. 원격 에뮬레이터 검증은 OFF로 기록한다.

## Hilt 마이그레이션 (2026-11-08)

로컬 빌드·lint·단위 및 모듈/manifest PASS; Hilt 전용 계측 APK 컴파일 PASS. 실행 기기 미연결로 Hilt 바인딩 교체, UI, Activity 재생성 및 Chat/Media3 수명주기 계측은 NOT_RUN. [실행 명령·상세](verification.md).

실기기 후속 실행: Hilt 대체 바인딩 1/1, 별도 룸 수명주기 1/1 PASS; 일반 회귀 36/37 PASS, LIVE 재생 상태 1건 FAIL(기준 커밋에서도 재현). 실제 서버·장시간 누수 NOT_RUN. [상세](verification.md).

후속 수정 후 SM-S908N/API 36 선택 회귀 39/39 및 별도 Hilt 바인딩 1/1 PASS. 기존 36/37 실패는 위에 보존하며 후속 통과로 구분한다. 실제 서버·장시간 검증은 NOT_RUN. [상세](verification.md).
