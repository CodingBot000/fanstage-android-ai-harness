# M2 — HLS Player 계획

- 상태: 계획 초안. 이번 정책 전환에서는 기능 코드나 Player 의존성을 추가하지 않는다.
- 목표: Media3 기반 HLS 재생과 화면 lifecycle·오류 복구를 실제 동작 및 증거로 검증한다.
- 범위: PlayerController 계약과 테스트 가능한 구현, 재생·버퍼링·오류 상태, 화면 이탈·재진입, 제한된 복구, 허가된 VOD fixture 및 별도의 시간에 따라 갱신되는 LIVE playlist 검증. Media3/Android 호환성은 구현 직전에 공식 자료와 기존 버전 조합으로 확인한다.
- 제외: 실시간 채팅 구현, 상시 과금 송출 인프라, 정밀 영상·채팅 동기화. 기존 M2의 실기기 및 리소스 해제 Gate를 보존한다.
- 순서: 기존 화면과 소유권을 조사 → 해결되지 않은 사용자 정책 확정 → SDD/ADR 구체화 → Player와 deterministic fake/테스트를 기능 PR에 함께 구현 → 관련 빌드·단위·기기 검증 및 증거 기록 → P0/관련 P1을 해결한 뒤 병합한다.
- 검증: 정상/오류/재시도·취소, 중복 생성 방지, listener/surface/player 해제와 반복 진입·이탈, 백그라운드 및 복귀, HLS 유형과 재생 증거를 기록한다. 실제 LIVE playlist와 VOD는 구분하고 실기기 Gate는 수행 전 NOT_RUN이다.
- Open Questions: 백그라운드 오디오·PIP·화면 밖 재생 등 사용자에게 영향을 주는 재생 정책은 미결정이며 사용자 결정을 받기 전 확정하지 않는다. 재현 가능한 적법한 LIVE fixture와 실기기 검증 환경도 구현 전 확인한다. 외부 상시 비용이 필요하면 별도 승인받는다.
- 근거: [마스터 플랜 M2](../MASTER_PLAN.md)와 [Fast Track ADR](../adr/ADR-0005-fast-track.md).
