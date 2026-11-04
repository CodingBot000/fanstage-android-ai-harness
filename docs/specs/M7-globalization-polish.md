# M7 — 언어·접근성 및 오류 UX SDD

- 상태: 로컬 핵심 UX 검증 PASS, PR CI·리뷰 대기. 이전 M6의 두 최적화 실험은 모두 되돌렸고 개선 효과는 입증되지 않았다. M6 추가 미측정 항목은 그대로 이월한다.
- 기본 영어와 한국어 Android 리소스를 사용하고 시스템 구성 locale 변경에 따라 다시 선택한다. 지원하지 않는 언어는 기본 영어로 대체한다. 사용자 입력·실제 서버 메시지·알 수 없는 방 이름은 번역하거나 수정하지 않는다. 데모의 고정된 가상 방과 Fake 환영 메시지만 로컬로 번역한다. 실존 인물·브랜드 및 무허가 콘텐츠를 추가하지 않는다.
- 서버의 `sentAt`은 UTC `Z` 형식의 절대 시각이다. 확인된 메시지에만 서버 시각을 보존해 현재 locale과 기기 시간대로 짧은 날짜·시간을 표시하며, 정렬은 계속 serverSequence 기준이다. Fake·Pending 및 값이 없거나 유효하지 않은 시각에는 시간을 추정하지 않는다. 초 이하 정밀도는 표시하지 않으며 상대 시간은 사용하지 않는다.
- 방 목록 오류·채팅 누락/전송·WebSocket 재연결과 Player 오류는 별도 사용자 메시지와 Retry를 유지한다. 불확실 전송을 실패나 성공으로 확정하지 않으며 중복·순서·메시지 예산·Player lifecycle 계약을 유지한다.
- 큰 글꼴에서 입력 및 복수의 액션이 겹치지 않도록 세로로 배치하며, Player에 지역화된 접근성 설명을 제공한다. 오류 및 Retry 텍스트는 실제 동작을 구별한다. 접근성 자동 검증은 TalkBack 실사용 전체를 대체하지 않는다.
- Test Plan: 시간대/잘못된 날짜 단위 테스트, 상태 timestamp 보존 및 기존 채팅·Player 테스트, 영어↔한국어 구성 변경과 2배 글꼴에서 화면·오류·입력 및 플레이어 설명의 연결 테스트, 실제 시스템 언어 변경은 별도 검증한다. 로컬 기기 결과·CI·미실행 항목은 [build-log](../../harness/build-log.md)에 기록한다.
- References: [Android localization](https://developer.android.com/guide/topics/resources/localization), [Compose accessibility](https://developer.android.com/codelabs/jetpack-compose-accessibility).
