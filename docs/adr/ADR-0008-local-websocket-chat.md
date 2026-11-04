# ADR-0008: M4 로컬 WebSocket·PostgreSQL 계약

- 상태: 채택; 로컬 검증 완료, PR CI 별도 확인. 로컬 개발/테스트 전용이며 공개 서비스 운영 승인이 아니다.

## 선택

Spring Boot 4.0.9 / Java 17 / Gradle 9.5, PostgreSQL 17.9 (고정 이미지 digest), Flyway 및 Android OkHttp 5.5.1을 사용한다. [Spring Boot 호환성](https://docs.spring.io/spring-boot/4.0/system-requirements.html), [WebSocket 지원](https://docs.spring.io/spring-boot/4.0/reference/messaging/websockets.html), [PostgreSQL 행 잠금](https://www.postgresql.org/docs/current/explicit-locking.html), [OkHttp 릴리스](https://github.com/square/okhttp/blob/master/CHANGELOG.md)를 구현 직전 확인했다. Gradle 배포 ZIP 체크섬과 컨테이너 digest를 고정한다.

room counter 행을 먼저 잠그고 멱등 판별·증가·저장을 짧은 DB 트랜잭션으로 수행한다. 커밋 반환 이후에만 ACK/신규 broadcast한다. subscription 등록 후 head를 얻고 Android는 HTTP로 head까지 보충하면서 live 이벤트를 수신한다. 서버 재연결 시 같은 경로를 반복하며 누락이 있으면 숨기지 않는다.

## 보안과 보존

두 독립 임시 토큰을 환경에서 받아 고정된 합성 사용자로 식별한다. 타인이 보낸 메시지의 client ID는 제공하지 않는다. 기본 바인딩은 loopback이며 cleartext는 Android debug에서만 허용한다. 개발 중 LAN 바인딩을 명시적으로 변경할 수 있지만 테스트 토큰과 합성 데이터만 사용한다. 실제 서비스 인증·암호화·권한·악용 방지는 별도 설계가 필요하다. 로컬 DB는 일반 종료 시 보존하고 자동 삭제하지 않는다. CI는 자체 프로젝트의 임시 합성 DB만 삭제한다.

## 대안과 트레이드오프

메모리 synchronized 카운터는 다중 프로세스·재시작에서 안전하지 않아 배제한다. 트랜잭션 잠금은 룸별 직렬화를 수반하므로 처리량은 실측 전 주장하지 않는다. 0부터 페이지를 재생하는 단순 snapshot은 재접속 데이터량이 커질 수 있으며 M5/M6에서 개선을 검토한다. 무한 영속 저장은 로컬 데모에 한정하며 운영 보존·비용 정책은 별도 결정한다.
