# LOCAL-MEDIA-001 검증 기록

기준: PR #40 병합 후 `580f420`. 전용 API 36 에뮬레이터만 사용했다. 별도 ADB 서버에서도 다른 기기가 보였으므로 모든 설치·계측은 `emulator-5560`을 명시했다. 실기기 NOT_RUN.

- PASS: 원본 7개와 저장소 밖 복사본의 SHA-256 일치. 각 파일에 H.264 영상과 AAC 오디오 트랙이 존재하며, 원본은 변경하지 않았다. 매핑·해시는 로컬 매니페스트에만 둔다.
- PASS: 로컬 격리 Compose의 읽기 전용 mount와 7개 HEAD 200·정상 Range 206 및 정확한 응답 크기. 미등록 ID와 경로 순회는 404. 미설정·파일 부재는 합성 fixture 서버 단위 테스트 PASS. 서버 소스 최종 버전의 7개 Range 재확인 PASS. Compose 2.40.3 config에서 기본 바인딩을 0.0.0.0으로 지정해도 미디어 override가 loopback만 남기는 것 PASS.
- PASS: 전용 에뮬레이터에서 7개 카드를 각각 선택해 첫 프레임과 지원 오디오 트랙을 확인, 대표 영상의 UI 일시정지·탐색·재개 및 뒤로가기 확인. 기존 HLS·탐색·UI 18개 계측 PASS는 슬라이더 추가 전 결과다. 이후 최종 앱에서 기존 Discover·HLS/플레이어 회귀 13/13 PASS.
- PASS: 서버 단위 테스트, Android Debug/Release/rcLocal 빌드·lint·단위, 모듈 경계·cleartext manifest 검사. 실제 서버 WebSocket ACK·history와 MP4 Range 공존 확인. 동일 VOD 화면에서 실제 서버 연결·메시지 전송 완료 및 영상 첫 프레임 확인 PASS. 기존 실제 서버 Android 계측 1/1 PASS.
- NOT_RUN: 실제 스피커 청취, 시연 캡처, 독립 리뷰, 최종 PR CI. 이 항목들을 통과한 것으로 간주하지 않는다. 서버 부재 전용 화면/자동 fallback, 비정상 Range 세부 처리는 NOT_RUN_DEFERRED.
- 최초 빌드는 SDK 경로 미설정으로 실패했고 설정 후 PASS. UI 탐색 테스트는 버퍼링과 슬라이더 표시를 기다리도록 수정 후 PASS. 영상·채팅 계측은 연결 완료 전 전송한 첫 시도 실패 후 연결 완료를 기다리도록 수정하여 PASS. 최종 코드의 기존 회귀 재실행 중 두 계측 명령을 겹쳐 이전 프로세스가 중단되었으며 그 결과는 사용하지 않는다.
