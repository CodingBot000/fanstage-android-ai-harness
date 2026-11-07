# AI-assisted Engineering Workflow

FanStage에서 AI 코딩 에이전트는 **구현 도구**, 요구사항·설계·검증 근거는 **개발 프로세스와 실행형 검사**가 담당합니다.

~~~text
요구사항·SDD(정상/오류/수용 조건)
  → Codex 구현·리팩토링
  → 변경 영향·코드 리뷰
  → Gradle build / Lint / Unit·Integration·UI
  → GitHub Actions + 로컬 Android 계측
  → PR별 증거 확인, 검증 결과 보고
~~~

## 실행 원칙

- **Spec:** 변경할 화면·상태·네트워크 계약, 정상 및 실패 조건과 테스트 계획을 해당 기능 단위로 정의합니다.
- **Scoped implementation:** Codex가 관련 모듈만 변경하고 API/모듈 경계·수명주기 영향이 있으면 기술 결정을 남깁니다.
- **Quality gates:** 프로젝트 규칙인 [AGENTS](../AGENTS.md)는 행동 지침이고, [Gradle/CI](test-matrix.md)는 실제 통과 여부를 판단합니다. 지침의 존재만으로 검증되었다고 간주하지 않습니다.
- **Regression:** 오류를 재현하고 수정한 뒤 관련 테스트·정적 검사와 Android 로컬 계측을 실행합니다. 변동 환경·커밋별 결과를 분리합니다.
- **Review responsibility:** AI 리뷰는 보조 수단입니다. Android Lifecycle·Coroutine cancellation·오래된 응답·시크릿 등을 사람이 판단할 수 있는 근거로 남깁니다.

## 구체적 적용

| 실제 PR | 구현·검증 연결 |
|---|---|
| [#38 Compose 모듈화](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) | 기능/공통 모듈 분리 후 의존 방향·회귀 검사 |
| [#47 Media3](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) | TrackGroup 설정, 전체 화면·Surface/lifecycle, 56개 단위 및 41개 로컬 계측 |
| [#53 LIVE 채팅](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) | 연결 인증 확인 후 transport 교체, Pending 보존·UI 회귀 |
| [#59 IME](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) | 키보드/화면 이동 계약, 로컬 AVD 및 실기기 회귀 |

**도구 범위:** Codex 실행과 GitHub Actions 기록은 저장소에 남아 있습니다. CLAUDE.md는 다른 에이전트의 공통 규칙 호환 진입점이며, 이 저장소가 Claude Code로 독립 검증까지 완료됐다는 주장에는 사용하지 않습니다.

현재 공개 증거 인덱스는 [harness/build-log](../harness/build-log.md), 아키텍처 선택은 [ADR](adr/)에 있습니다.
