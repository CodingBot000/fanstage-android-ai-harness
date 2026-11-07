# UX-LIVE-002 — LIVE 채팅 메신저형 비주얼 정렬

상태: DRAFT / PR 검증 대기

## 배경과 기준
사용자가 승인한 대화 내 LIVE 디자인 시안(2026-11-07)을 기준으로, 기존 UX-LIVE-001의 기능 중심 구현을 메신저형 화면으로 개선한다. 원격 `main` 기준 커밋은 `328cf13`. 사용자 로컬에만 존재하는 `9ede13f27939bfd9b182490e3ce532f896b00c97` 커밋은 원격에서 조회되지 않아 이 PR에 포함됐다고 주장하지 않는다.

## 시각적 수용 기준
- 채팅 헤더는 제목을 왼쪽에, 현재 모드/연결 상태를 작은 점·텍스트로 오른쪽에 배치한다.
- 두 개의 `FilterChip` 대신 상호 배타적인 Material 3 `SingleChoiceSegmentedButtonRow`를 사용한다. 선택 상태는 실제 `ChatUiState.localServer`에서 읽으며, 연결 성공 전에 로컬 서버 선택이 확정된 것처럼 보이지 않는다.
- 우측 `개발 설정` 버튼의 기본 상태는 접힘이다. 서버 주소·토큰·연결 버튼은 접힘 상태에서 자리를 차지하지 않는다. 펼친 패널의 입력·오류·토큰 마스킹 기능은 유지한다.
- 발신자 아바타는 왼쪽, 발신자명은 말풍선 위, 본문은 폭 최대 78%의 **내용 크기 기반** 말풍선으로 표시한다. 본문이 짧으면 말풍선도 짧아진다. 나의 메시지는 오른쪽에 테마 컬러 말풍선으로 표시한다.
- 전송 시각 및 Pending/Sent/Failed는 말풍선 바깥의 작은 메타데이터로 표시한다. 서버가 시각을 주지 않을 때 임의의 시간을 생성하지 않는다.
- 실패한 메시지의 재전송·삭제 버튼, 서버 재연결·누락 복구 경고는 그대로 유지한다.
- LIVE 영상 및 하단 고정 채팅 입력의 기능을 유지하며, 중복되는 설명성 문구는 제거한다.
- 플레이어 설정 버튼을 우측 정렬하고 시각적 content padding을 4dp로 축소한다.

## 데이터 및 안전 계약
- `ChatViewModel`, `ChatTransport`, ACK, 서버 시퀀스, 중복 제거, 메시지 재전송 정책은 변경하지 않는다.
- 서버 모드를 누르면 연결 설정이 펼쳐지고, 인증된 연결이 성공했을 때만 모드가 서버로 전환된다.
- 비확정 전송이 있을 때 모드 변경 차단, 토큰 비보관 및 연결 검증 중 취소 동작을 보존한다.
- `chat_mode`, `chat_use_fake`, `chat_select_real`, `chat_settings_toggle`, `chat_use_real`, `chat_bubble_own`, `chat_bubble_other`, `chat_input` 테스트 태그를 유지한다.

## 검증 계획과 한계
- `assembleDebug`, `lintDebug`, `feature:live:testDebugUnitTest` 및 관련 Android UI 계측 실행.
- 기존 `chatSettingsStartCollapsedAndInvalidConnectionKeepsDemoSelected`, `liveChatKeyboardSendAndBack`, `RealChatIntegrationTest`, `developmentFieldsStayAboveImeWithoutMovingVideo`를 포함해 점검.
- 추가: 수신자 아바타 존재·짧은 메시지의 말풍선 가변 너비 검증.
- 320dp 및 시스템 글꼴 2배에서 Segmented Control과 설정 버튼의 내용 겹침/말줄임, IME 노출 시 동작 및 실제 화면 캡처 검사.
- 실제 기기, TalkBack, 장시간 메시지 스트레스 및 실제 렌더 픽셀 확인은 실행되기 전까지 NOT_RUN이다.
- 이 변경은 GitHub 원격 API에서 수행했다. 로컬 Android SDK/에뮬레이터를 통한 컴파일·계측은 아직 실행하지 못했다. CI 결과를 별도로 확인할 것.
