# M4 — 실제 WebSocket·PostgreSQL 채팅 SDD

- 상태: 로컬 핵심 Gate VERIFIED; PR CI와 리뷰는 병합 전에 별도로 확인한다. 실제로 실행하지 않은 검증은 NOT_RUN이다.
- Goal: 기존 ChatTransport/UI 계약으로 로컬 Spring Boot WebSocket과 HTTP 누락 보충을 사용하고, PostgreSQL에서 순번·멱등성·재시작 후 영속성을 검증한다.
- In Scope: 인증된 합성 테스트 사용자 두 명, 룸별 순번과 원자적 저장, commit 이후 ACK/broadcast, 충돌 거절, HTTP 페이지 조회, 등록 후 snapshot 보충과 재접속, debug Fake/Real 선택. Player와 WebSocket 소유권은 분리한다.
- Out of Scope: 실제 사용자 인증, TLS 배포, 외부 공개 서버, Redis/Kafka, 상시 과금 인프라, M5 대규모 장애 주입 및 M6 성능 주장. 데이터는 로컬 개발용이며 실제 개인 정보를 입력하지 않는다.
- 사용자 결정: 클라이언트 활성 룸과 Fake 합계 1,000개, 화면 500개, 메시지 최대 1,000 Unicode 코드포인트를 유지한다. 추천 기본안을 적용하며 임의의 실제 credential을 생성·보관하지 않는다.
- 로컬 개발 기본안: DB는 자동 삭제 없이 Compose volume에 유지하고 일반 `down`은 데이터를 보존한다. 사용자가 별도 삭제하기 전까지 보관하며 실서비스 보존 정책은 미정이다. CI의 별도 임시 프로젝트만 테스트 종료 후 합성 데이터를 제거한다. 테스트 요청 수는 성능/용량 약속이 아니다.
- 인증·환경: 독립적으로 생성한 두 임시 토큰으로 합성 사용자 `alice`/`bob`을 서버에서 식별한다. 동일 토큰·짧은 토큰은 시작 시 거절한다. 서버의 DB 비밀번호·토큰은 환경변수로 전달한다. Android debug 토큰 입력은 메모리에서만 유지하고 저장소·로그·배포 산출물에 넣지 않는다. 기본 호스트 바인딩은 127.0.0.1:18765이며 실기기 LAN 바인딩은 개발자가 명시적으로 선택한다. HTTP/WS 평문은 Android debug 매니페스트에만 허용하며 외부 공개나 실제 credential에 사용하지 않는다.
- 데이터 계약: WebSocket 경로의 roomId, UUID clientMessageId와 text를 검증한다. `(room,user,clientMessageId)`와 `(room,serverSequence)`는 DB UNIQUE; room counter 행 잠금 후 조회·증가·저장을 하나의 트랜잭션으로 처리한다. 같은 ID/본문은 기존 결과, 다른 본문은 충돌. commit이 성공한 후 ACK 및 신규 메시지만 broadcast한다. `isOwn`은 수신자의 인증 사용자에서 계산하고 타인의 client ID는 제공하지 않는다. `sentAt`은 DB 생성 시각이며 commit 순서 기준이 아니다.
- 연결·오류: 서버는 subscription을 등록한 뒤 커밋된 head를 보낸다. Android는 등록된 실시간 스트림과 head까지 HTTP snapshot을 함께 소비해 경계의 누락을 방지하며 sequence로 중복·역순을 처리한다. 연결이 끊기면 0.5~30초 backoff로 재연결하고 snapshot을 다시 받는다. 10초 ACK 불명은 Pending/uncertain이며 동일 ID로 수동 재시도한다. 명시적 invalid/conflict는 Failed; 인증 실패와 연결·재연결 상태는 결과 불명 전송과 분리해 표시한다. 화면 종료·룸 변경은 socket과 요청을 취소한다.
- 기술 제한: history 페이지 최대 1,000개, WebSocket text frame 8,192자, HTTP 응답 최대 8 MiB, 서버 느린 소켓 전송 버퍼 1 MiB/5초 제한. 이는 로컬 데모의 방어적 기본값이지 운영 용량 약속이 아니다. 기존 transport UI 계약은 유지하며 프로토콜 시각을 선택적 필드로 확장한다.
- Test Plan: 실제 Postgres/WS/HTTP에서 순차·동시 중복/충돌, 서로 다른 사용자 동일 ID, 순번 연속성, 잘못된 입력·유니코드, 지연 COMMIT 실패 후 ACK/broadcast 부재, 구독/snapshot 경계, DB·서버 재시작을 확인한다. Android emulator에서 두 인증 사용자, 실제 송수신·재접속 중 누락 보충·룸 교체·debug UI·Player 격리를 실행한다. Fake/Player 기존 회귀와 release 평문 미허용 설정을 확인한다.
- Evidence: [build-log](../../harness/build-log.md), 서버 통합 스크립트 및 PR CI. M2 원격 LIVE, 실제 LMK, 장시간 메모리·성능과 M4 물리 LAN 검증은 실행하지 않았다면 NOT_RUN을 유지한다.
