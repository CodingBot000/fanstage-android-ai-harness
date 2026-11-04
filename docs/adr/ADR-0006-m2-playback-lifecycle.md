# ADR-0006: M2 플레이어 소유권과 재생 정책

- 상태: 2026-11-04 사용자 제안에 따라 구현; 검증·M2 Exit Gate는 별도.

## 결정

상세 화면이 PlayerController와 Media3 ExoPlayer를 소유하고 ViewModel은 이를 보유하지 않는다. 최초 상세 진입 시 포그라운드 자동 재생, 일시중지 후 복귀 시 사용자 조작으로 재개한다. 화면 이탈·재생성에서는 lifecycle observer를 제거하고 listener·surface·player를 해제한다. 화면 재생성 후 자동 재생을 반복하지 않는다. PIP와 백그라운드 오디오는 v0.1에서 제외한다. HLS 로드 실패의 자동 재시도는 최대 2회로 제한하고 사용자에게 수동 재시도를 제공한다.

## 근거와 대안

화면별 소유권은 백그라운드 서비스보다 자원 수명과 경계를 단순하게 한다. 재생 중단을 명시적으로 유지하므로 복귀 시 예상치 못한 오디오가 재생되지 않지만 자동 연속 재생은 제공하지 않는다. 화면 재생성 시 이전 위치를 복원하지 않으며, 장기 재생이나 PIP가 필요하면 별도 설계가 필요하다.

Media3 1.11.1의 ExoPlayer/HLS/UI 모듈을 동일 버전으로 사용한다. [Android Media3 릴리스](https://developer.android.com/jetpack/androidx/releases/media3), [HLS 지원](https://developer.android.com/media/media3/exoplayer/hls), [플레이어 수명 관리](https://developer.android.com/media/implement/playback-app)를 참조했다. 네트워크 재생을 위한 INTERNET 권한을 추가하고 테스트는 외부 서비스 없이 로컬 fixture를 사용한다.
