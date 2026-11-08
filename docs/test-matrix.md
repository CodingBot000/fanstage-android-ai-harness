# 변경 영향 기반 테스트 매트릭스

FanStage는 변경된 코드의 역할에 따라 테스트를 선택합니다. GitHub Actions가 실행하는 검사와 로컬 Android 기기에서 실행한 계측 결과를 구분해 기록합니다.

## 변경별 검증

| 변경 대상 | 우선 실행할 검사 | 추가 확인 |
|---|---|---|
| Compose 화면·ViewModel | 빌드·Lint·관련 단위 테스트 | 탐색·IME·회전 등 변경 화면의 로컬 계측 |
| Media3·Player | 빌드·Lint·Player 단위 테스트 | 실제 첫 프레임, 전체 화면 전환, Listener·Surface 해제 |
| ChatTransport·메시지 모델 | 관련 단위 테스트·서버 통합·장애 주입 | ACK 유실, 중복·역순·누락 및 Android 상태 전이 |
| Hilt 주입·ViewModel 범위 | 빌드·단위 테스트·모듈 경계 검사 | 바인딩 교체, 화면 재생성 및 룸 재진입 계측 |
| 모듈·Gradle 의존성 | 빌드·단위 테스트·모듈 경계 검사 | 의존 방향과 공개 API 영향 |
| 문서·설계 결정 | Markdown 링크·ADR 번호 검사 | 실제 구현·PR 근거와 내용 일치 여부 |
| 성능 계측 | 동일 기기·빌드·측정 시나리오의 반복 실행 | Macrobenchmark·Perfetto 결과 비교 |

## 실제 자동 검사

- **[Android CI](../.github/workflows/android-ci.yml):** Debug/Release 계열 빌드, Lint, 단위 테스트, Manifest·모듈 의존성 검사, 일반/Hilt 전용 계측 APK 각각 컴파일(기기 실행 아님)
- **[서버 CI](../.github/workflows/m4-integration.yml):** 격리 PostgreSQL·WebSocket 통합과 ACK 유실·연결 단절 테스트
- **[문서 CI](../.github/workflows/docs-check.yml):** Markdown 내부 링크와 ADR 번호 중복 검사

Hilt 전용 `RepositoryBindingsTest`는 `-PfanstageHiltInstrumentation=true`에서만 테스트 러너와 소스가 구성됩니다. [기기 실행 결과](specs/hilt-di-migration-verification.md)는 별도 기록으로 구분합니다.

자동 검사가 실패하면 해당 GitHub Actions 작업이 실패 상태로 표시됩니다. 변경 수용 시 코드 리뷰와 관련 기기 검증 기록을 함께 확인합니다. **에이전트의 작업 보고만으로 테스트 통과를 판단하지 않습니다.**

[실제 실행 결과](verification.md) · [AI 개발·검증 절차](ai-development-workflow.md)
