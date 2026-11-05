# FanStage — Native Android LIVE & Realtime Fan Experience

Android와 AI 개발 하네스를 함께 검증하는 프로젝트다. M0와 M1 핵심 Gate는 완료됐고, M2는 룸 상세의 Media3 HLS 재생과 실기기 합성 fixture 검증을 완료했으며 M3는 Fake 채팅을 구현했고 M4는 로컬 PostgreSQL·WebSocket 채팅을 추가한다. 기본 화면은 합성 VOD fixture를 사용하며 공개 LIVE 방송으로 표시하지 않는다. 원격 네트워크 LIVE와 실제 저메모리 종료는 아직 검증하지 않았다.

- Android: Kotlin/Compose 룸 목록·상세, `:app` / `:feature:live` / `:core:model`, FakeRepository와 Media3 PlayerController.
- 검증: 로컬 clean build 및 CI 빌드·단위 테스트, 에뮬레이터 내 VOD·갱신 LIVE fixture 및 lifecycle 테스트. 정확한 대상과 실행 이력은 [build-log](harness/build-log.md)를 따른다.
- 미검증: 원격 LIVE 재생, 실제 저메모리 종료, 장시간 메모리 및 M6 성능 개선. 제한된 실기기 성능 실험은 수행했지만 개선을 입증하지 못했다. 실서비스 인증·원격 배포와 M5 대규모 장애 주입도 아직 검증하지 않았다.

현재 상태는 [STATUS](docs/STATUS.md), 전체 범위는 [MASTER_PLAN](docs/MASTER_PLAN.md), 서버와 연결 방법은 [서버 README](backend/chat-server/README.md), 재생 정책은 [M2 SDD](docs/specs/M2-hls-player.md), 빌드 방법은 [Android README](android/README.md)를 참조한다.
