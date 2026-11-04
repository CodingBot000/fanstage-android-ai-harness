# M3 — Fake Realtime Chat SDD

- 상태: VERIFIED — M3 핵심 Gate는 로컬 단위·에뮬레이터 검증 PASS; PR CI·코드 리뷰는 병합 전에 별도 확인한다.
- Goal: 외부 서버 없이 룸 채팅의 전송 상태·ACK·정렬·중복·누락 복구와 lifecycle을 재현한다.
- 사용자 승인 정책 (2026-11-04): 메모리에 보관하는 메시지는 최대 **1,000개**, 화면에는 그중 최대 **500개**를 표시한다. 화면과 같은 프로세스의 모든 활성 룸·Fake 서버가 참조하는 고유 논리 메시지는 합쳐서 1,000개를 넘지 않으며 공통 메시지는 하나의 슬롯을 공유한다. 메시지당 최대 **1,000 Unicode 코드포인트**이며 초과 입력은 자르지 않고 거절한다. 명백히 실패한 메시지는 사용자가 직접 제거할 수 있고 Pending 메시지는 자동 삭제하지 않는다. Fake는 화면별 임시 저장이며 룸을 떠나거나 전환하면 해당 화면의 가상 이력이 사라진다. 디스크 보존이나 실 서버 보존 정책은 확정하지 않는다.
- In Scope: Fake Transport와 교체 가능한 계약, Pending/Sent/Failed, 멱등 clientMessageId, serverSequence 정렬·중복 제거, 누락 탐지·페이지 보충, 룸 전환·취소, Compose LazyColumn. 합성 메시지와 사용자가 입력한 메시지를 구분한다.
- Out of Scope: WebSocket, 실 서버·DB, M4 이후 운영 정책, 실제 아티스트/사용자 데이터. Player 소유권·상태는 채팅과 독립한다.
- Contracts: 승인된 보관 한도 내에서 확인된 연속 sequence와 관측된 최대 sequence를 별도로 관리한다. 중간 누락을 최댓값으로 덮지 않는다. 누락 복구가 불가능하거나 실패하면 명시적으로 표시한다. 명백한 전송 거절만 Failed, 결과 불명은 Pending으로 유지한다. ACK와 에코는 clientMessageId로 하나의 항목으로 수렴한다. 범위 밖 event와 취소 후 지연 결과는 무시한다.
- Architecture: 기존 `:feature:live` 내부에 chat 패키지와 화면 단위 ViewModel을 두며 Player는 건드리지 않는다. Transport 인터페이스는 UI와 ViewModel이 Fake/실제 구현에 종속되지 않게 한다. 테스트 대체 구현으로 교체 가능성은 확인했지만 실제 WebSocket 구현과의 계약 검증은 M4에서 수행한다. 상세 화면에서 나가면 구독·복구·전송 job을 취소한다.
- Test Plan: 고정 seed와 가상 시간으로 정상·지연·역순·중복·누락·보충·실패·오래된 룸 응답·종료 취소·1,000/500 경계를 검증한다. Android build/lint/unit과 에뮬레이터 채팅 UI 및 기존 Player 회귀를 실행한다.
- Evidence: 결과와 미실행 항목은 [build-log](../../harness/build-log.md)에 기록한다. M2 원격 LIVE·실제 LMK·장시간 메모리 검증은 기존 NOT_RUN 상태를 유지한다.
- Open Questions: 장기 보관·복구 범위와 실 서버 정책은 M4에서 별도 결정한다. 이번 작업에서 구현하지 않는다.

Fake 서버의 중복 요청 멱등성은 공유 예산 안에 보관 중인 ID에 적용된다. 퇴출된 ID의 장기 중복 방지는 M4 서버의 영속 계약으로 남긴다. 개수·문자열 길이는 제한하지만 JVM 객체·비디오·런타임까지 포함한 프로세스 전체 바이트 상한을 주장하지 않는다.

- UI: 최신 메시지 근처에서만 자동으로 따라가며 사용자가 오래된 메시지를 읽을 때는 위치를 유지한다. 직접 전송하면 최신 메시지로 이동한다.
