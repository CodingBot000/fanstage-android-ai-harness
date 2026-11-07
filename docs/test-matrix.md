# Test Matrix · 변경 영향 기반 검증

테스트·품질 게이트는 **실행형 검사**와 **기기별 수동/로컬 계측**으로 분리합니다.

| 계층 | 대상 | 실행 환경·판정 |
|---|---|---|
| Android CI | assembleDebug/Release/RC/benchmark, Lint, Unit, 모듈 경계·Manifest 검사 | GitHub Actions의 [android-ci](../.github/workflows/android-ci.yml), 실패 시 PR에서 수정 |
| Server CI | Spring Boot 테스트, 격리 PostgreSQL, WebSocket 중복·순서·재연결, ACK 유실 주입 | GitHub Actions의 [m4-integration](../.github/workflows/m4-integration.yml) |
| Unit | ViewModel의 상태·취소, 메시지 순서/중복/누락, 플레이어 명령 | 관련 모듈의 Gradle test |
| Android instrumentation | Compose 탐색, IME, 회전/복귀, Media3 재생·트랙·첫 프레임, 로컬 서버 연결 | 전용 로컬 AVD 또는 실제 Android 단말; 테스트 결과와 소스 SHA 연결 |
| Fault injection | TCP 프록시 ACK/echo 소실, 지연 snapshot, 네트워크 오류, HLS segment 오류 | 격리된 테스트 환경·서버 |
| Performance | AndroidX Macrobenchmark, Perfetto, 반복 측정 및 통계 비교 | 동일 실기기·동일 빌드/측정 조건으로 실행; PR 자동 차단 수치로 사용하지 않음 |
| Review | Compose/Coroutine/Player/Socket 경계와 테스트·라이선스·시크릿 확인 | AI 보조 리뷰와 개발자 판단; 테스트 실행을 대신하지 않음 |

**CI 정책:** 원격 에뮬레이터 작업은 기본 PR CI에 포함하지 않습니다. 로컬 계측은 필요 시 실행하고 결과를 별도 기록하며, CI PASS와 계측 PASS를 혼동하지 않습니다. 상세 사례는 [verification](verification.md), [워크플로](ai-development-workflow.md)를 참조합니다.

**변경 승인 기준:** 필수 빌드/테스트 실패, 시크릿 노출, 재현 가능한 메시지 정합성·Player 리소스 오류는 해결 전 완료로 선언하지 않습니다. 실행하지 않은 범위를 통과했다고 표현하지 않습니다.
