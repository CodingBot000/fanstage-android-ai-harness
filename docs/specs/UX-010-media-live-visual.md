# UX-010 — Media·LIVE·Replay 시각 개선

상태: 진행 중. Media 그리드에 그룹별 피드 이미지를 사용하고 카드 비율 및 합성 LIVE 룸 라벨을 명확히 한다. LIVE는 기존 PlayerPanel과 ChatPanel의 ViewModel·Transport·resource lifecycle을 유지하며 제목/표현만 정리한다. Replay는 기존 합성 HLS를 재사용한다. 기존 `room_*`, `player_*`, `chat_*` 테스트 태그를 유지한다. 컴파일·단위 검증 및 전용 에뮬레이터 수동 재생·복귀 확인 후, 전체 계측·서버 회귀는 격리 환경의 최종 통합에서 수행한다.

## 결과 (2026-11-06)
Media에 그룹별 피드 썸네일을 사용하고 상단 크롭을 조정했다. 합성 룸 라벨·제목을 표시하고 Replay의 뒤로가기 문구를 경로에 무관하게 수정했다. [Media](../evidence/ux-010/media.png), [LIVE](../evidence/ux-010/live.png), [Replay](../evidence/ux-010/replay.png). 전용 API 36 에뮬레이터에서 LIVE/Replay `Playing` 및 복귀를 수동 확인하고 logcat에서 각 Player Init/Release 한 쌍을 확인했다. `:app:assembleDebug :app:lintDebug :feature:live:testDebugUnitTest :app:testDebugUnitTest` PASS. 전체 계측·실제 서버 회귀 NOT_RUN_DEFERRED; 단일 수동 사례는 장시간 누수 부재를 입증하지 않는다.

- Replay 카드 높이 정렬 진행: 영상 및 기존 Replay 카드는 동일한 이미지 비율과 3개의 두 줄 고정 텍스트 영역을 사용한다. 글꼴 크기에 맞춰 높이는 변하지만 같은 그리드의 카드들은 동일한 높이를 유지한다.
- Debug 빌드·lint PASS. 전용 API 36 에뮬레이터 Replay 필터에서 기존 Replay와 로컬 VOD 카드의 높이가 같은 것을 확인하고 기존 합성 Replay 진입·첫 프레임·복귀 PASS.
