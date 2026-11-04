# FanStage — Native Android LIVE & Realtime Fan Experience

Android와 AI 개발 하네스를 함께 검증하는 프로젝트다. M0와 M1 핵심 Gate는 완료됐고, M2는 룸 상세에 Media3 HLS 재생을 구현한다. 기본 화면은 합성 VOD fixture를 사용하며 공개 LIVE 방송으로 표시하지 않는다. M2 전체 완료에는 실기기 검증이 남아 있다.

- Android: Kotlin/Compose 룸 목록·상세, `:app` / `:feature:live` / `:core:model`, FakeRepository와 Media3 PlayerController.
- 검증: 로컬 clean build 및 CI 빌드·단위 테스트, 에뮬레이터 내 VOD·갱신 LIVE fixture 및 lifecycle 테스트. 정확한 대상과 실행 이력은 [build-log](harness/build-log.md)를 따른다.
- 미검증: 실기기 재생·회전, 시스템 프로세스 종료/복구, 실기기 성능. 채팅 transport는 M3~M5에서 구현한다.

현재 상태는 [STATUS](docs/STATUS.md), 전체 범위는 [MASTER_PLAN](docs/MASTER_PLAN.md), 재생 정책은 [M2 SDD](docs/specs/M2-hls-player.md), 빌드 방법은 [Android README](android/README.md)를 참조한다.
