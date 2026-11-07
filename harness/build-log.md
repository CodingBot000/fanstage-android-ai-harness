# FanStage · Verification Index

AI 코딩 에이전트의 수행 결과는 개별 PR·커밋·GitHub Actions와 테스트 코드에 연결합니다. 이 파일은 **현재 포트폴리오의 핵심 증거 인덱스**입니다. 과거 세부 작업 로그는 Git 이력에서 확인할 수 있습니다.

| 작업·변경 | 검증 내용 | 원본 근거 |
|---|---|---|
| Compose 모듈화 | app/feature/core 11개 모듈, 경계 검사 | [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) |
| Media3 플레이어 | 단위 56/56, 로컬 API 36 계측 41/41, Android·서버 CI | [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) |
| LIVE 채팅 UX·transport | 단위 56/56, 로컬 AVD 28/28, PR CI | [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| IME·대화 입력 UI | 단위 62/62, 로컬 AVD 관련 24/24, 실기기 3/3 | [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) |
| 실기기 데모 | 로컬 MP4 재생·화면 녹화, 문서 링크와 영상 디코딩 검사 | [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) |
| 서버·장애 주입 | 격리 DB/WS/HTTP, 재전송·누락·ACK 유실 | [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) · [검증 시나리오](../docs/specs/failure-recovery.md) |
| Macrobenchmark | 별도 오래된 소스 버전에서 실기기 기준선 수집·가설 검증 | [성능 측정 방법](../docs/performance.md) |

**해석:** PR별 테스트 수는 서로 다른 코드 버전·테스트 범위의 결과이며 서로 합산하지 않습니다. GitHub Actions의 Android 빌드/단위 PASS는 원격 Android 에뮬레이터 통과를 뜻하지 않습니다. 현재 UI 시연은 사전 녹화 영상과 가상 데이터로 촬영했습니다.
