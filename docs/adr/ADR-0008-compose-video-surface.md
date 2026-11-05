# ADR-0008 — Compose 상세 전환의 비디오 Surface

상태: 검증 중, 2026-11-06. UX-012의 사용자 보고: 뒤로가기로 상세 화면이 사라진 뒤 영상이 따로 남는다.

## 결정
기존 Media3 `PlayerView`를 Compose `AndroidView` 안에서 유지하되 video surface를 `TextureView`로 지정한다. Player/Session 소유권, lifecycle observer, player detach/release, 서버·채팅 계약을 변경하지 않는다. TextureView는 일반 View 합성에 참여하므로 상세 화면의 opacity/전환과 영상이 함께 그려지도록 한다. 전환 프레임과 `Init`/`Release`, 회귀 테스트를 확인한다.

## 대안과 한계
Media3는 전력·프레임 타이밍·HDR·보안 DRM 출력 등의 이유로 일반적으로 SurfaceView를 권장한다. 현재 합성 HLS 데모에는 보안 DRM 요구가 없고 Compose 화면 전환의 잔상 문제가 있어 TextureView를 선택한다. 기기별 전력·HDR·보안 출력 및 Android TV 전체 해상도는 검증하지 않았다. 향후 DRM/HDR 등 요구가 생기면 surface 정책을 다시 결정해야 한다. [Media3 공식 surface 안내](https://developer.android.com/media/media3/ui/surface).
