# 테스트 매트릭스 · 변경 영향 기반 검증

FanStage는 **PR 자동 검사**와 **로컬 Android 기기 계측**을 역할에 맞게 나누어 실행합니다.

| 검증 계층 | 주요 대상 | 실행 방식 |
|---|---|---|
| Android CI | Debug/Release/RC/Benchmark 빌드, Lint, 단위 테스트, Manifest·모듈 의존성 | [GitHub Actions](../.github/workflows/android-ci.yml) |
| 서버 CI | Spring Boot 테스트, 격리 PostgreSQL, WebSocket 중복·순서·재연결, ACK 유실 주입 | [GitHub Actions](../.github/workflows/m4-integration.yml) |
| Android 단위 테스트 | ViewModel 상태·취소, 채팅 ACK/정렬/누락, Player 설정 | 관련 모듈의 Gradle test |
| Android 계측 테스트 | Compose 화면 이동·IME·회전, Media3 재생·전체 화면·로컬 서버 연결 | 로컬 AVD 또는 Android 실기기 |
| 장애 주입 | ACK/echo 손실, 지연 snapshot, HLS segment 오류, 재연결 | 테스트 전용 프록시·fixture·서버 |
| 성능 계측 | Macrobenchmark·Perfetto 기준선과 변경 전후 비교 | 동일 기기·빌드·시나리오로 별도 실행 |
| 코드 리뷰 | Compose/UDF, Coroutine 취소, Player/Socket 자원, 메시지 정합성 | 변경 단위의 검토·PR 근거 |

## 검사 기준

- **자동 검사:** 컴파일·핵심 단위/통합 테스트가 실패하면 CI가 통과하지 못하도록 구성
- **코드 리뷰:** 시크릿 노출, 재현된 메시지 정합성 오류, Player/Socket 리소스 오류는 수정·재검증 후 변경 수용
- **기기·성능 검증:** 화면·영상·키보드·수명주기 변경 시 관련 로컬 계측과 실행 환경을 PR에 기록합니다. 성능 비교는 기기·빌드·시나리오를 고정한 경우에만 결과로 제시
- **자동화 운영:** 원격 에뮬레이터는 기본 PR CI에서 제외하고, Android 계측은 로컬 환경의 실행 결과를 별도로 보관

개별 작업의 테스트 수와 실제 결과는 [검증 기록](verification.md) 및 해당 PR을 참조합니다.
