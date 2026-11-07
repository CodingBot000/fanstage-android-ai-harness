# Media3 영상 재생 · 구현 및 검증

## 구현 범위

- AndroidX Media3 ExoPlayer로 앱 내 **합성 HLS VOD** 및 Debug 로컬 HTTP Progressive MP4 VOD 재생
- Media3 `TrackGroup`에서 확인한 영상 화질·자막 선택, Auto 설정 복원, 배속·전체 화면
- 재생·일시정지·버퍼링·오류 상태와 재시도 UI
- 화면 전환·회전·재진입에 따른 Player/Listener/Surface 관리
- Compose 전환 시 TextureView 사용 및 동일 재생 세션 유지

## 주요 검증 시나리오

1. 합성 HLS 재생 및 Playing·Paused·Buffering·Error 상태 전환
2. 화질/자막/배속 설정, Auto 복원, 소스 변경 후 유효하지 않은 트랙 선택 정리
3. 전체 화면 전환에서 재생 위치·설정 유지, 종료 시 Player와 Listener 정리
4. Back·회전·백그라운드 복귀 및 반복 진입에서 재생 상태와 리소스 관리
5. HLS 세그먼트 오류 발생 시 오류·재시도 UI와 채팅 동작 분리

## 검증 결과

[PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) 기준 단위 테스트 **56/56**, 로컬 API 36 계측 **41/41** PASS를 기록했습니다. 해당 변경의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158)에서도 빌드·정적 검사·단위 테스트를 확인했습니다.

실기기 화면 시연은 [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61)의 로컬 서버 사전 녹화 MP4를 사용합니다. 합성 HLS와 사전 녹화 MP4는 모두 **VOD**로 구분합니다.

테스트 자산에는 FFmpeg `testsrc2`로 만든 다중 화질 HLS·MP4와 WebVTT 자막이 포함되어 있습니다.

**설계 근거:** [플레이어 수명주기 ADR](../adr/ADR-0002-player-lifecycle.md) · [Android 실행 안내](../../android/README.md).
