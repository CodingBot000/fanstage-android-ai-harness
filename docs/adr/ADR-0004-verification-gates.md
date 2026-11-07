# ADR-0004 · CI and Device Verification Gates

**Decision: Accepted.** PR에는 재현 가능한 Android 빌드·Lint·단위 및 서버 통합·장애 주입 검사를 수행합니다. 비용과 환경 변동이 큰 Android 에뮬레이터/실기기 계측은 개발 환경에서 별도 수행하고 PR 증거와 연결합니다.

**Why:** 코드 생성 성공 또는 CI 초록색 결과만으로 Media3 Surface·IME·화면 전환·실기기 품질을 보증할 수 없습니다. 다만 모든 PR에 고비용 계측을 의무화하면 실행 비용과 소요 시간이 커집니다.

**Trade-off:** 원격 PR CI가 Android 기기 계측 전체를 자동 차단하지는 않습니다. 변경과 관련된 기기 테스트를 로컬에서 수행하고 실행 환경·PR·검증 수를 구분해 기록해야 합니다.

**Evidence:** [Test Matrix](../test-matrix.md) · [Android workflow](../../.github/workflows/android-ci.yml) · [Server workflow](../../.github/workflows/m4-integration.yml).
