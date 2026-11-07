# UX-VIDEO-003 — 영상 화면 다크 테마 및 LIVE/VOD 콘텐츠 분기

## 목표와 승인된 동작
- LIVE 룸 카드(`studio-session`, `sketch-break`, `tabletop-night`)는 `rooms/{roomId}`로 이동하고 **영상 + 채팅**을 제공한다.
- 디버그 환경의 LIVE 데모는 로컬 서버에서 사전 녹화 MP4를 재생할 수 있지만 실제 방송처럼 설명하지 않는다. 화면에 "LIVE 데모 · 로컬 서버 녹화 영상으로 채팅 시연"을 표기한다.
- `local-video/{videoId}` 카드(Video 01–07)는 **모두 VOD**로 처리하여 `ReplayScreen`으로 이동한다. 기존 'Video 01~03은 채팅, 04~07은 채팅 없음'이라는 번호 기반 분기를 제거한다.
- 다시보기 카드와 벤치마크 다시보기 역시 `ReplayScreen`을 유지하고, "다시보기 영상에서는 채팅을 제공하지 않습니다."를 표시한다. 이것은 오류 메시지가 아니다.
- `rooms/benchmark`와 같은 LIVE 룸 벤치마크는 기존 룸/채팅 검증 경로를 보존한다.

## 디자인 기준
- 영상이 표시되는 LIVE, Replay 화면 전체를 검정(`#000000`) 배경으로 한다. 영상 검정 레터박스, 상단 제목, 플레이어, 채팅 패널, 입력창 배경까지 일관되게 구성한다.
- 화면 전용 `VideoPlaybackTheme`에서 흰색/회색 텍스트, 중립적인 짙은 회색 채팅 카드·말풍선, 밝은 아이콘·모드 컨트롤을 사용한다. 보라색 표면을 배제한다.
- Android edge-to-edge 상태바/내비게이션바 아이콘도 어두운 배경에서 읽히는 색상으로 전환하고, 화면 이탈 시 이전 밝은 화면 설정을 복구한다.
- 다른 탭(Discover, Media Grid, Community, Messages)은 기존 밝은 테마를 바꾸지 않는다.
- 콘텐츠 주변의 불필요한 여백을 늘리지 않으며, 기본 플레이어 높이와 회전·IME 복구 정책은 유지한다.

## 아키텍처/변경 위험
- 라우팅은 카드가 대표하는 LIVE/VOD 의미로 결정한다. 재생 파일 확장자나 01~03 인덱스로 채팅 존재 여부를 결정하지 않는다.
- `ChatViewModel`, `ChatTransport`, ACK/순서/재연결 로직을 변경하지 않는다.
- LIVE 데모의 MP4 대응은 영상을 교체하는 테스트 입력일 뿐, 이 앱이 실제 송출 서비스라는 주장으로 사용하지 않는다.
- M2 재생 화면 Player/Surface 수명과 Debug 로컬 동영상 서버의 URL 계약을 보존한다.
- 외부 LIVE 실서비스, DRM/HDR, TalkBack/실기기 픽셀 시각 검증은 본 변경으로 완료되었다고 주장하지 않는다.

## 검증
- Debug/Release build, lint, unit test, Android instrumentation 컴파일, PR의 실제 WebSocket 통합 CI를 수행한다.
- Android 에뮬레이터: LIVE 카드는 `chat_panel` 있고 VOD 안내 없음; VOD 01–07은 `vod_chat_unavailable`가 있고 `chat_panel`이 없음; 모든 영상 상세가 `video_playback_dark_screen` 포함.
- 테스트가 접근 가능한 경우 실제 화면 캡처로 검정/무채색 스타일과 Android 상태바·하단 IME 대비, 회전, 영상→목록 복귀를 수동 확인한다.
- 본 문서 작성 시 PR CI 및 에뮬레이터/실기기 결과는 **NOT_RUN**, 이후 실제 실행 결과에 맞게 갱신한다.
