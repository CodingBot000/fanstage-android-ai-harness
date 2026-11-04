# M5 — 장애 주입 및 복구 SDD

- 상태: 핵심 Gate VERIFIED (로컬과 PR 소스 CI PASS). 이 기록 후 최종 PR head의 CI도 병합 전에 확인한다. 개별 증거가 없는 항목은 NOT_RUN이다.
- 범위: 기존 Real/Fake ChatTransport, Android Media3, Spring Boot 및 PostgreSQL. 합성 데이터만 사용하며 M6 벤치마크나 운영 인프라를 추가하지 않는다.
- 격리: 테스트 전용 프록시가 ACK 프레임을 제거한다. 연결 오류 시 자동 재접속은 최대 6회이며 30초 이상 지속된 연결에서 실패 횟수를 초기화하고, 소진되면 명시적 사용자 재연결을 기다린다. 서버에 원격 조작 가능한 장애 API를 추가하지 않는다. 독립적인 Compose 프로젝트와 새 볼륨만 사용하며 기존 개발 DB를 삭제하지 않는다.
- 재현성: 시나리오 ID `m5-net01-seed-73`, `m5-net02-seed-73`; 고정 메시지 UUID 및 명시적 10초 대기. 고유 테스트 룸만 실행마다 달라진다. 일시 토큰을 로그·저장소에 기록하지 않는다.
- M3 사용자 결정: 전체 활성 룸 및 Fake 서버 공유 1,000개 보관, 화면 500개, 메시지 최대 1,000 Unicode 코드포인트를 유지한다.

| ID | 주입 / 증거 | 현재 상태 및 한계 |
|---|---|---|
| NET-01 | 테스트 서버의 10초 단절 후 재구독·HTTP snapshot, Android 에뮬레이터의 소켓 취소·10초 backoff, 503 장애 시 자동 재시도 6회 제한·UI 수동 재연결, 가상시간 Pending 불명·동일 ID 재전송 | 부분 PASS; Android에서는 ACK와 발신자 에코 유실을 별도로 재현해 불명 상태·동일 ID 재전송을 확인. 같은 실행에서 정확히 10초 물리 네트워크 단절과 결합 및 30초 안정 연결 후 실패 카운터 초기화 분기는 NOT_RUN |
| NET-02 | 로컬 TCP 프록시가 실제 서버의 최초 ACK 프레임을 폐기한 뒤 동일 ID 재전송, DB 조회 및 다른 구독자의 방송 확인 | 서버/프록시 및 Android ViewModel Pending·재전송 PASS (발신자 에코도 함께 유실) |
| NET-03 | 기존 결정적 단위 테스트에서 역순, 중복, 누락 및 ACK 처리 | 단위 및 실제 snapshot 지연으로 2번 live 이벤트가 1번보다 먼저 도착하는 Android 통합 PASS |
| NET-04 | 실제 Android 소켓을 재접속 backoff 중 룸 변경; 기존 단위 테스트의 늦은 이전 룸 snapshot 응답 | PASS, 실기기 전환 NOT_RUN |
| NET-05 | 단위 테스트에서 과거 snapshot을 보류하는 동안 최신 이벤트와 cursor를 진행한 후 늦게 응답 | 단위 및 실제 서버 HTTP 응답 지연 프록시·Android 통합 PASS |
| NET-06 | 가상시간 10/50/100 msg/s 각 12초 분량, 1,000 보관·500 표시 및 cursor 확인 | 가상시간 논리 상한 및 실제 Compose의 목표 속도 3초씩·메인 heartbeat·상한 PASS; 실제 네트워크 부하·전체 메모리 계측 NOT_RUN, M6 성능 주장 아님 |
| MEDIA-01 | 에뮬레이터 로컬 HTTP fixture의 segment 404 후 사용자 retry, 12초 지연 및 연결 절단 후 복구 | PASS; 제어된 loopback fixture 범위, 실제 외부 네트워크 NOT_RUN |
| LIFE-01 | 기존 에뮬레이터 background/foreground 재생 상태 및 채팅 테스트 | 실제 채팅 연결과 Player background/foreground 에뮬레이터 PASS; 실기기 네트워크 전환 NOT_RUN |
| LIFE-02 | 기존 Activity 재생성 및 에뮬레이터 회전 | 실제 채팅 Activity 재생성 및 에뮬레이터 am kill 후 Fake 기본 화면 복구 PASS; 실제 시스템 LMK NOT_RUN |
| LIFE-03 | 기존 반복 입장·퇴장 Player Init/Release 검사 | Player 생성/해제 일치 및 실제 WebSocket 5회 구독 취소 후 소켓·Job 정리 PASS; 장시간 heap NOT_RUN |

이 표는 부분 증거를 전체 시나리오 통과로 확대하지 않는다. 실제 네트워크 부하·장시간 리소스 계측과 실기기 네트워크 전환은 NOT_RUN이다. [검증 기록](../../harness/build-log.md)을 확인한다.
