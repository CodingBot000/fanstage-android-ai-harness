# PLAYER-001 독립 리뷰

- 범위: Draft PR #47 구현 head `85c9657c06221362e35b5ab45d177b922f9c0380`, base `92e4ae8cbd8354aed55198c5a76911f5019e28ee`. 별도 리뷰 컨텍스트에서 아키텍처·Lifecycle·Surface·Coroutine·상태·테스트를 정적으로 검토했다.
- 사전 리뷰의 공용 경로 덮어쓰기와 이전 Surface의 첫 프레임 상태를 새 화면의 증거로 사용하는 P1, 전체 화면 어두운 텍스트의 P2는 수정 후 재검토했다. 매 전환 Analytics output identity 검사와 이전 PlayerView 선행 분리, 고유 test cache 및 정리를 확인했다.
- PR head에서 추가 확정 P0/P1은 발견하지 못했다. 테스트 임시 디렉터리의 API 26 의존성 P2는 이후 API 24 호환 File API로 수정했다. LIVE/Replay 개별 경로의 태그 대기는 output identity를 직접 비교하는 반복 전환 검사와 구분한다. 태그 자체는 지연 렌더 콜백의 output을 식별하지 못한다.
- output 검사에는 Media3 내부 필드 반사에 따른 버전 의존성이 있으며, 전환의 모든 프레임·모든 기기, 장시간 heap 및 실제 미선언 CEA 방송을 검증한 것은 아니다. 별도 후속 검증으로 유지한다. 원격 CI는 PR head별로 확인한다.

- 후속 head `604fa44c6d20a54d9aca120dfc69ddb636b6c0f1` 전체 diff와 API 24 호환 수정의 결합을 별도 컨텍스트에서 재검토했다. 추가 P0/P1/P2 지적 0건, `git diff --check` PASS. 이는 정적 리뷰이며 테스트를 별도로 실행한 결과는 아니다.

- `f99c224a37e0be621c9fa1271aef7be805a9fd73` 문서 head 재검토에서 `604fa44`와 기존 전체 계측 결과의 연결이 모호하다는 P2를 발견했다. 기존 전체 테스트와 604 테스트 수정 후 전용 테스트를 위 SDD에 분리하고 `f99c224`에서 전체 41/41을 재실행해 SHA별 근거를 보강했다. 이후 문서 전용 head 재리뷰 및 CI는 PR에서 확인한다.
