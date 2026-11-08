# Media3 재생 · 구현과 검증

## 구현한 기능

- 합성 HLS VOD와 로컬 서버의 녹화 MP4 재생
- Media3 `TrackGroup`을 이용한 화질·자막 선택, 자동 화질 복원, 배속 설정
- 재생·일시정지·버퍼링·오류 상태와 재시도
- 전체 화면 전환 중 동일 재생 세션 유지, 화면 이탈 시 Player·Listener·Surface 정리
- Compose 화면 전환에서 `TextureView`를 사용해 영상과 화면 전환을 함께 처리

## 주요 검증 시나리오

| 시나리오 | 검증 항목 |
|---|---|
| 영상 재생 | HLS 재생·일시정지·버퍼링·오류 및 재시도 |
| 재생 설정 | 화질·자막·배속 변경, 자동 화질 복원, 소스 교체 |
| 전체 화면 | 재생 위치·설정 유지, 이전 PlayerView 분리와 새 출력 연결 |
| 생명주기 | 뒤로가기·회전·재진입·백그라운드 복귀 시 Player/Listener 정리 |
| 장애 처리 | HLS 세그먼트 오류와 채팅 상태의 독립성 |

[PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)에서 단위 테스트 **56/56**, 로컬 API 36 계측 테스트 **41/41** 통과를 기록했습니다. 동일 PR의 코드 리뷰에서 이전 Surface의 첫 프레임을 새 화면 출력으로 잘못 판정할 위험을 확인하고 테스트를 보완했습니다.

**시연 영상:** 기본 Replay 화면은 앱에 포함된 합성 HLS VOD를 재생합니다. Debug의 LIVE 룸 세 곳과 로컬 영상 항목은 로컬 서버에서 녹화 MP4를 받아 재생합니다. [실기기 시연 PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61)

[플레이어 설계 결정](../adr/ADR-0002-player-lifecycle.md) · [Android 실행 안내](../../android/README.md)

## 일시정지 화면의 재생 버튼

Activity 재생성이나 회전으로 새 화면이 만들어진 뒤에도 플레이어가 일시정지 상태라면 재생 버튼을 표시합니다. 기존 자동 재생·수동 재개와 Player 소유권 정책은 유지합니다. 실기기 회귀 범위와 이전 실패는 [검증 결과](../verification.md)를 참조합니다.


## 회전 진단

회전 계측은 Debug 전용 진단으로 Lifecycle·Player·Surface·Compose 상태를 기록한다. Release/RcLocal에서는 진단이 비활성이다. 실제 기기 반복 결과와 기존 미확정 실패는 [검증 증거](../evidence/verification/audit-20261108/README.md)를 참조한다.
