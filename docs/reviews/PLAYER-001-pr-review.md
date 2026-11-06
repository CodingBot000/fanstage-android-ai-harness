# PLAYER-001 독립 리뷰

- 범위: Draft PR #47 구현 head `85c9657c06221362e35b5ab45d177b922f9c0380`, base `92e4ae8cbd8354aed55198c5a76911f5019e28ee`. 별도 리뷰 컨텍스트에서 아키텍처·Lifecycle·Surface·Coroutine·상태·테스트를 정적으로 검토했다.
- 사전 리뷰의 공용 경로 덮어쓰기와 이전 Surface의 첫 프레임 상태를 새 화면의 증거로 사용하는 P1, 전체 화면 어두운 텍스트의 P2는 수정 후 재검토했다. 매 전환 Analytics output identity 검사와 이전 PlayerView 선행 분리, 고유 test cache 및 정리를 확인했다.
- PR head에서 추가 확정 P0/P1은 발견하지 못했다. 테스트 임시 디렉터리의 API 26 의존성 P2는 이후 API 24 호환 File API로 수정했다. LIVE/Replay 개별 경로의 태그 대기는 output identity를 직접 비교하는 반복 전환 검사와 구분한다. 태그 자체는 지연 렌더 콜백의 output을 식별하지 못한다.
- output 검사에는 Media3 내부 필드 반사에 따른 버전 의존성이 있으며, 전환의 모든 프레임·모든 기기, 장시간 heap 및 실제 미선언 CEA 방송을 검증한 것은 아니다. 별도 후속 검증으로 유지한다. 원격 CI는 PR head별로 확인한다.
