# AI 코딩 도구를 활용한 개발·검증 과정

FanStage는 Codex를 코드 구현·리팩토링에 활용하면서, **요구사항 정의와 품질 검증을 개발자가 관리하는 방식**으로 진행했습니다.

~~~text
기능별 요구사항·수용 기준(SDD)
    → Codex로 구현·리팩토링
    → 변경 영향 분석·코드 리뷰
    → 빌드·Lint·단위/통합/계측 테스트
    → GitHub Actions 확인
    → 검증 결과를 PR과 연결
~~~

## 적용한 원칙

- **작업 범위 정의:** 변경할 화면·모듈, 정상 흐름과 실패 조건, 완료 기준을 기능 단위로 정리합니다.
- **구현과 설계 판단 분리:** Codex로 코드를 작성하되 아키텍처·네트워크·수명주기 변경은 영향 범위를 검토하고 결정 이유를 남깁니다.
- **실행 결과 중심 검증:** [AGENTS.md](../AGENTS.md)로 공통 규칙을 제공하고, Gradle·테스트·CI 결과로 완료 여부를 확인합니다.
- **재현과 회귀 확인:** 실패한 상태 전이·화면 전환·네트워크 이벤트를 테스트로 재현하고 수정 후 관련 경로를 재검증합니다.
- **코드 리뷰:** Compose 책임, Coroutine 취소, Player/Socket 리소스, 데이터 정합성에 집중해 변경을 검토합니다.

## 실제 작업 사례

| 변경 | 개발·검증 포인트 |
|---|---|
| [PR #38 · Compose 모듈화](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) | 기능/공통 모듈 책임 분리 후 의존성 검사와 회귀 검증 |
| [PR #47 · Media3](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) | 트랙 설정·전체 화면·Surface 수명주기와 계측 테스트 |
| [PR #53 · LIVE 채팅](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) | 서버 인증 확인 후 전송 경로 변경, 미확정 메시지 보존 |
| [PR #59 · 키보드 대응](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) | IME 표시 중 영상 위치와 입력창 배치, AVD·실기기 회귀 |

구체적인 PASS 범위와 실행 환경은 [검증 결과](verification.md), 자동화 대상과 실행 정책은 [테스트 매트릭스](test-matrix.md)에 정리했습니다.
