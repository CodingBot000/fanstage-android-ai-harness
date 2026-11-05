# 포트폴리오 소개 문안

## 한 줄 소개

합성 HLS 영상과 실시간 채팅을 결합하고, 장애 복구와 lifecycle을 테스트한 Android 데모.

## 상세 소개

- Kotlin·Jetpack Compose와 Media3로 룸 목록·상세 및 HLS 재생 화면을 구성하고, Fake 채팅과 로컬 WebSocket/HTTP 서버를 같은 화면 계약에 연결했습니다.
- ACK·echo 유실, 중복·역순 이벤트와 지연 snapshot에 대해 Pending 상태와 복구 경로를 구현하고, 격리된 PostgreSQL·프록시·Android 에뮬레이터에서 시나리오별로 검증했습니다.
- Player와 채팅의 독립성, 화면 이탈 시 자원 해제, 한국어·영어 문구를 테스트하고 로컬 검증용 Release-like APK를 실기기에 설치해 핵심 흐름을 확인했습니다.
- 실기기 성능 A/B 실험을 각 10회씩 수행했습니다. 일부 구간의 개선 관찰에도 대조군 회귀 가능성과 시작 시간 악화가 있어 변경을 되돌렸으며, 제품 성능 향상으로 주장하지 않습니다.
- SDD·ADR, CI 결과와 PASS/FAIL/NOT_RUN을 연결해 구현 과정과 남은 위험을 추적했습니다. 최초 WebSocket CI 타임아웃은 원인 미확정으로 남겼고 해당 PR에 한해 사용자가 위험 수용 예외를 승인했습니다.

## 사용 시 지켜야 할 범위

원격 LIVE·운영 WebSocket 서비스가 아닙니다. 실제 네트워크 부하·실기기 네트워크 전환·실제 LMK·장시간 heap은 검증하지 않았습니다. 합성 트래픽을 운영 동시접속 수로 바꾸어 쓰지 않습니다. 원본 실기기 trace는 공개하지 않으며 로컬 manifest와 해시만 [실험 보고서](../reviews/M6-002-ab-experiments.md)에 기록했습니다.
