# ADR-0002 · Media3 Ownership and Compose Video Surface

**Decision: Accepted.** Media3 ExoPlayer/Listener/Surface는 화면의 재생 세션이 소유하고, ViewModel은 Player 인스턴스를 직접 보유하지 않습니다. 화면 이탈에서는 리소스를 정리하며, 전체 화면 전환 중에는 동일 재생 세션을 사용합니다.

**Compose surface:** 화면 전환 시 영상 잔상을 줄이기 위해 PlayerView의 video surface를 TextureView로 선택했습니다. SurfaceView가 일반적으로 전력·성능·HDR/보안 출력에 유리할 수 있다는 점을 인지하고, 현재 DRM 없는 데모 UI의 전환 요구를 우선했습니다.

**Playback policy:** 포그라운드에서 재생, 백그라운드 일시정지와 복귀 시 명시적인 재개, 제한된 오류 재시도 및 사용자 조치 제공.

**Trade-off:** DRM/HDR·고해상도·기기별 전력 특성에는 별도 surface 선택과 검증이 필요합니다.

**Evidence:** [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [플레이어 기술 계약](../specs/media3-player.md) · [Media3 surface guide](https://developer.android.com/media/media3/ui/surface).
