# UX-LIVE-001 — LIVE 채팅 UI

상태: [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) 병합 완료 (`dd3302a`). PR Android·서버 CI PASS; 원격 에뮬레이터 SKIPPED.

LIVE 채팅에 실제 transport 상태를 따르는 데모/로컬 서버 단일 선택, 기본 접힘 개발 설정, 좌우 말풍선을 제공한다. 서버 선택 시 주소·토큰이 없으면 설정을 열고, 임시 연결의 인증을 확인한 뒤 기존 `replaceTransport`를 호출한다. 실패·인증 오류는 데모 상태를 유지하고 표시하며 미확정 메시지의 전환 차단은 기존 ViewModel 계약을 따른다. 검증 중 임시 연결과 화면 이탈 시 작업은 취소되며 토큰은 저장 상태나 로그에 남기지 않는다. 연결이 이후 끊기면 기존 재연결·오류 상태를 표시한다.

기존 메시지 순서·키·전송/ACK·실패 재시도와 최신 메시지 추적 로직을 유지한다. 상대·내 메시지는 각 80% 폭 이하의 좌우 말풍선이며 실제 서버 발신자 이름은 기존 모델이 제공하지 않아 '참여자'로 표시한다. 플레이어 크기·전송 로직은 변경하지 않았다.

## 검증 및 한계
- Debug 빌드·lint·live 단위 56/56 PASS. 전용 API 36 AVD에서 최종 PlayerSettingsTest·DeviceUiRegressionTest·UiContractTest·LocalMediaTest·RealChatIntegrationTest 28/28 PASS(기존 자격 재사용, 별도 빈 DB, 기존 MP4 읽기 전용). 실제 서버 인증 실패 후 데모 유지, 정상 연결, 메시지·ACK·재연결과 영상 회귀 포함. 서버에서 데모로 전환하는 별도 검사 1/1 PASS.
- 설정 접힘·빈 자격 오류·모드 선택, 채팅 말풍선 좌우 정렬, IME·영상 위치 검사 PASS. Activity 재생성과 실제 화면 회전 후 설정 유지, 320dp 폭·2배 글꼴에서 모드 컨트롤 비겹침 별도 2/2 PASS. Pending 메시지 차단은 기존 ViewModel 단위 테스트로 확인했다.
- 합성 영상·데모 데이터로 캡처한 [변경 전](../evidence/ux-live-001/before.png) 및 [변경 후](../evidence/ux-live-001/after.png). 화면의 모든 기기·언어·2배 글꼴 픽셀 전수, TalkBack 및 실제 운영 방송은 NOT_RUN. 2배 글꼴에서는 모드 텍스트가 화면 폭에 따라 말줄임될 수 있다.
