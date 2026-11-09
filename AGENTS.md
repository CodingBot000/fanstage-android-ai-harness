# FanStage · 코딩 에이전트 작업 규칙

## 1. 시작 전에 확인할 사항

- [현재 상태](docs/STATUS.md)와 이번 변경에 관련된 [기능 계약](docs/specs/)·[설계 결정](docs/adr/)만 확인한다.
- 변경할 기능, 허용 모듈, 정상·오류 시나리오, 완료 기준을 먼저 정리한다.
- 승인된 범위 밖의 기능·의존성·서버 설정을 임의로 추가하거나 변경하지 않는다.

## 2. 구현과 리뷰

- Compose의 상태 소유권, ViewModel과 Repository 경계, Coroutine 취소를 확인한다.
- Player·Listener·Surface·Socket의 생성·해제 책임을 확인한다.
- 채팅의 ACK 유실, 중복·역순·누락, 이전 룸의 늦은 응답을 점검한다.
- 기존 테스트를 비활성화하거나 검사 조건을 완화해 통과시키지 않는다.
- 시크릿, 실제 사용자 데이터, 권리가 확인되지 않은 미디어를 소스·로그에 추가하지 않는다.

## 3. UI 작성·리팩토링 규칙

- 신규 Compose UI 작성 전 `core:designsystem`의 기존 Theme·spacing·컴포넌트와 [ADR-0005](docs/adr/ADR-0005-ui-component-reuse.md)의 재사용 기준을 확인한다.
- 여러 Feature에서 표현과 상호작용 **계약이 실제로 동일하고 모델 독립적인 경우**에만 `core:designsystem`으로 승격한다. 한 Feature 안의 중복 레이아웃은 먼저 그 Feature 내부에 둔다.
- 공통 컴포넌트는 표현 값·이벤트·필요한 슬롯만 받고, 기능별 모델·Navigation·ViewModel·Media3 Player·Socket 상태를 소유하지 않는다.
- Material3 Button/Card/TextField 등을 근거 없이 일대일 래핑하지 않고, 같은 숫자라는 이유만으로 padding·색·shape를 일괄 통일하지 않는다.
- 공통 UI 변경 시 사용처·접근성 설명·semantics/testTag·IME·탭 복원·Player/Surface 영향도를 확인하고 관련 Compose 계측을 수행한다. 시각적 동등성은 **별도 실제 캡처 비교** 없이 PASS로 기재하지 않는다.

## 4. 검증과 작업 종료

- 변경 영향에 맞는 Gradle 빌드·Lint·단위/통합 테스트를 실제 실행한다. 화면·재생·IME 변경은 관련 Android 계측 테스트도 확인한다.
- 테스트를 실행하지 못했으면 해당 항목과 사유를 결과에 기록한다. 실행 전에는 성공을 선언하지 않는다.
- 완료 보고에는 **변경 파일과 이유 / 실행 명령과 결과 / PR·CI 링크 / 남은 확인 항목**을 포함한다.
- 외부 비용, 자격 증명 변경, 데이터 삭제, 공개 배포 등 영향이 큰 작업은 별도 승인을 받는다.

## 5. 공개 문서 작성

- 설명은 한국어 문장으로 작성한다. 클래스·메서드·필드·프로토콜 이름은 실제 코드 명칭을 사용한다.
- 무엇을 구현했는지, 어떻게 테스트했는지, 어떤 환경에서 확인했는지를 구분한다.
- 같은 테스트 수치를 여러 문서에 반복하지 않고 [검증 결과](docs/verification.md) 또는 해당 PR로 연결한다.

[AI 개발·검증 워크플로](docs/ai-development-workflow.md) · [테스트 매트릭스](docs/test-matrix.md)
