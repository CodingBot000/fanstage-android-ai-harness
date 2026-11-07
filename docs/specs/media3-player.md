# Media3 Playback · 기능 및 검증 계약

## Implemented

- AndroidX Media3 ExoPlayer 기반 **앱 내 합성 HLS VOD** 재생과 Debug 로컬 HTTP Progressive MP4 VOD.
- 실제 Media3 TrackGroup 기준 화질 수동 선택 및 Auto 복원, 지원되는 자막·재생 배속·전체 화면 설정.
- 버퍼링/오류/제한된 재시도 및 화면 복귀/재진입 시 Player/Listener/Surface의 소유권 관리.
- 화면 전환의 영상 Surface 잔상에 대응하기 위해 Compose 전환에서 TextureView 선택. 렌더링·DRM/HDR/전력 트레이드오프는 [ADR](../adr/ADR-0002-player-lifecycle.md)에 기록.

## Acceptance cases

1. 합성 HLS 재생, Playing·Paused·Buffering·Error의 상태 표시와 사용자 재시도.
2. 화질/자막/배속 선택·Auto 복원 및 무효한 트랙 선택 제거.
3. 전체 화면 전환 중 재생 세션 유지와 화면 종료 시 Listener/Surface/Player 정리.
4. 뒤로가기·회전·백그라운드 복귀·재진입에서 재생 세션과 채팅 수명 경계 유지.
5. Error fixture의 segment 오류를 주입해 Player와 채팅이 서로 독립적인지 확인.

## Recorded validation

[PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)에서 로컬 단위 **56/56**, API 36 로컬 계측 **41/41** PASS 및 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) PASS를 기록했습니다. [실기기 시연 PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61)은 로컬 서버의 사전 녹화 MP4를 사용합니다.

**의미 구분:** 앱 화면의 LIVE는 데모 콘텐츠 종류이며, 기본 합성 HLS는 VOD입니다. 실제 원격 생방송 송출·지연을 검증한 결과로 표현하지 않습니다.

Fixture는 FFmpeg testsrc2 합성 영상과 테스트용 다중 화질 HLS/WebVTT 자막으로 구성됩니다. [Android 실행 가이드](../../android/README.md).
