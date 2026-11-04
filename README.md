# FanStage — Native Android LIVE & Realtime Fan Experience

공개 Android 기술 검증 및 AI 개발 하네스 프로젝트. `M0`와 `M1` Android Foundation의 핵심 Gate는 검토·완료했으며, 실기기 설치와 실제 회전·프로세스 종료 복구는 아직 검증하지 않았다. HLS 재생(M2), 채팅(M3~M5), 실기기 성능(M6)은 아직 구현/검증 완료로 주장하지 않는다.

- Android 현재 구현: Kotlin/Compose 룸 목록·상세 자리 표시 화면, `:app` / `:feature:live` / `:core:model`, FakeRepository, ViewModel/StateFlow 및 오류·빈 상태 분기.
- 실제 CI 증거: [main Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) — Gradle build/lint, Unit 7/7, API 35 Emulator Compose 목록→상세→Activity 재생성→뒤로가기 1/1 PASS. Lint 경고 13건은 별도 부채다.
- **미충족 Gate:** 개발자 로컬 환경의 명시적인 clean build는 NOT_RUN, 본격적인 실기기/화면 회전/프로세스 종료 검증은 NOT_RUN. LIVE·성능 검증은 M2/M6 Gate에서 수행한다. 세부 판단은 [M1 독립 Gate 감사](docs/reviews/M1-006-exit-gate-audit.md)를 확인한다.

작업 상태·다음 행동은 [STATUS](docs/STATUS.md), 범위는 [MASTER_PLAN](docs/MASTER_PLAN.md), 설계는 [M1 SDD](docs/specs/M1-android-foundation.md), 정확한 명령과 증거는 [build-log](harness/build-log.md)를 기준으로 한다. Android 빌드 준비는 [android/README](android/README.md)를 참고하되 그 문서의 과거 생성 시점 검증 상태 문구는 최신 사실이 아니므로 STATUS와 build-log를 우선한다. 새로운 구현/검증은 각 Gate 조건을 충족한 경우에만 통과로 기록한다.
