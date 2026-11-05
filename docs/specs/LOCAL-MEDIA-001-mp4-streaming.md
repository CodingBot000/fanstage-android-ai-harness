# LOCAL-MEDIA-001 — 로컬 Progressive MP4 VOD

상태: 진행 중. 기준 `580f420`; 에뮬레이터 전용 Debug 시연. 사용자가 직접 제작한 공개 가능한 영상이나 원본·복사본·변환본 파일은 Git/CI에 포함하지 않는다. 개인 파일명과 경로는 공개하지 않는다.

기존 Spring 서버에 고정 ID 7개의 읽기 전용 HTTP 리소스를 선택적으로 추가하고 정상 Range를 지원한다. Debug Media 카드와 기존 룸을 표시 순서로 연결한다. 기존 HLS와 채팅 경로 및 Release cleartext 정책을 보존한다. 서버 부재 화면/자동 fallback과 비정상 Range 상세 검증은 NOT_RUN_DEFERRED. 실기기는 사용하지 않는다.

검증 게이트: 원본 해시와 복사본, 7개 HTTP 및 정상 206, 7개 에뮬레이터 첫 프레임/오디오 트랙, 대표 재생·오디오·탐색·해제, 기존 HLS/채팅 회귀. 실행한 것만 PASS로 기록한다.
