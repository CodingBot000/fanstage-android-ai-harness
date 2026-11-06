# ADR-0010: 플레이어 설정과 전체 화면 호스트

상태: ACCEPTED — 로컬 API 36 및 활성 원격 CI에서 검증한 범위. [Draft PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47), 코드·문서 head `78c91cd` 기준. 원격 에뮬레이터는 SKIPPED이고 공식 GitHub 승인 리뷰는 없다. ADR-0006의 화면 소유와 background pause·수동 재개 정책 및 ADR-0008의 TextureView 선택을 유지한다.

설정은 Media3 이벤트에서 읽은 지원 트랙과 playback parameters를 `PlayerUiState`로 노출하고, 선택 명령은 화면 소유 controller가 수행한다. 화질 수동 선택은 현재 TrackGroup의 특정 트랙 override이고 Auto는 비디오 override를 제거한다. 자막 끄기는 텍스트 타입을 비활성화한다. 플레이어의 자동 캡션 설정은 사용자의 명시적 자막 선택 전까지 유지한다. Group이 바뀌어 더 이상 유효하지 않은 override는 제거한다. 라이브러리나 모듈 의존성은 추가하지 않는다.

전체 화면은 기존 상세 컴포지션이 소유하는 controller/session을 유지하면서 Compose Dialog에 PlayerView를 다시 연결한다. 기존 PlayerView를 먼저 detach한 뒤 새 TextureView를 연결하며 재생 세션은 유지된다. 이 전환은 player의 생성, prepare 또는 release를 요구하지 않는다. FIT을 유지하고 화면 방향은 강제하지 않으며 시스템 안전 영역에 컨트롤을 둔다. 시스템 막대의 가시성이나 Activity 방향은 변경하지 않는다. Activity 자체 재생성 시에는 ADR-0006에 따라 이전 player를 해제하며 새 세션을 시작한다. 전용 AVD에서 output identity·첫 프레임·회전 및 이탈 후 잔상을 확인했다. 모든 전환 프레임과 기기에서의 중복 Surface 부재는 검증하지 않았다.

대안인 별도 Activity는 세션 소유권과 생명주기 복잡성을 높인다. 앱 전역 서비스는 PIP/백그라운드 재생을 전제하므로 현재 범위를 넘는다. Dialog 전환은 Surface 재연결이 필요하므로 순간 검은 프레임이나 기기별 잔상 가능성을 검증해야 한다. PIP는 PLAYER-002에서 별도 결정한다.

## 미선언 CEA-608 정책 (2026-11-06 사용자 결정)

HLS manifest에 자막 선언이 없을 때 Media3가 호환성을 위해 추론한 CEA-608 후보는 유지한다. 실제로는 자막이 없을 수도 있으므로 UI에 지원 여부가 미확인임을 명시한다. 이 정책은 선언되지 않은 실제 CEA-608 스트림의 선택 가능성을 보존하는 대신, 미확인 후보가 메뉴에 보일 수 있다는 한계를 수용한다. 자막이 없음을 알고 있는 기본 합성 영상은 multivariant playlist에 `CLOSED-CAPTIONS=NONE`을 명시한다. 사용자 결정 전의 미선언 무자막 fixture 실패와 정책 선택 이유는 SDD와 build-log에 보존한다. 선언된 CEA와 WebVTT는 추론 후보로 취급하지 않는다. 관련 API: [Media3 DefaultHlsExtractorFactory](https://developer.android.com/reference/androidx/media3/exoplayer/hls/DefaultHlsExtractorFactory).
