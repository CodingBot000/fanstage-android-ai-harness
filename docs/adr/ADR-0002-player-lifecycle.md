# ADR-0002 · Media3 플레이어 소유권과 비디오 Surface

## 결정

Media3 ExoPlayer, Listener, Surface는 화면의 재생 세션이 관리합니다. ViewModel은 Player 인스턴스를 직접 보유하지 않고, 전체 화면 전환에서는 동일한 재생 세션을 유지합니다. 화면을 이탈하면 연결된 리소스를 정리합니다.

## Compose 화면 전환

일반적으로 SurfaceView는 영상 출력의 성능·전력 효율 측면에서 유리합니다. FanStage에서는 Compose 화면 전환 중 영상 잔상을 줄이기 위해 `PlayerView`의 비디오 출력을 **TextureView**로 설정했습니다. 현재 데모에서는 화면 전환과 영상 합성의 일관성을 우선했습니다.

## 재생 정책 및 고려 사항

포그라운드에서 재생하고, 백그라운드 진입 시 일시정지하며, 복귀 시 사용자 조작으로 재개합니다. 로드 오류에는 제한된 재시도와 수동 재시도를 제공합니다. DRM/HDR 또는 전력 효율이 중요한 환경에는 Surface 선택을 별도로 검토해야 합니다.

**구현·검증:** [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) · [플레이어 검증 계약](../specs/media3-player.md) · [Media3 Surface 안내](https://developer.android.com/media/media3/ui/surface).
