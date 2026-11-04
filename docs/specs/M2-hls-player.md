# M2 — Media3 HLS Player SDD

- 상태: 구현·기능 PR 검증 중. 실기기 M2 전체 Gate는 검증 전 NOT_RUN이다.
- Goal: Compose 룸 상세에서 HLS를 실제 재생하고 생명주기·오류·리소스 소유권을 검증한다.
- In Scope: PlayerController, Media3 ExoPlayer/HLS, 테스트 Fake, VOD 및 별도 갱신 LIVE fixture, loading/buffering/playing/paused/error, 제한된 자동·수동 재시도, lifecycle과 표면·listener·player 해제.
- Out of Scope: 채팅 transport, PIP·백그라운드 오디오, 상시 과금 인프라, 정밀 영상·채팅 동기화. 기본 화면은 합성 VOD fixture이며 LIVE 서비스라고 표기하지 않는다.
- Contract / Architecture: 상세 화면이 player를 소유하며 ViewModel은 소유하지 않는다. 최초 foreground 진입에서 자동 재생, background 진입 시 pause, 복귀 시 사용자 조작으로 재생한다. 이탈·재생성 시 release; 재생성 후 자동 재개하지 않는다. [ADR-0006](../adr/ADR-0006-m2-playback-lifecycle.md)에 이유와 트레이드오프를 기록한다.
- Failure / Recovery: Media3 로드 오류 자동 재시도 최대 2회, 최종 오류 상태와 수동 재시도. 네트워크 제한을 무한 재시도로 숨기지 않는다. 실제 서비스 수준의 네트워크 장애 주입은 M5에서 별도 검증한다.
- Fixture provenance: 앱에 포함된 VOD는 FFmpeg 8.0.1의 `testsrc2`로 직접 생성한 무음 합성 영상이다. `ffmpeg -f lavfi -i 'testsrc2=size=160x90:rate=15' -t 6 -c:v libx264 -pix_fmt yuv420p -g 30 -sc_threshold 0 -an -hls_time 2 -hls_playlist_type vod -hls_segment_filename segment%02d.ts vod.m3u8`. 외부 실존 영상·아티스트 콘텐츠가 아니다. 테스트는 별도 임시 LIVE playlist에서 ENDLIST 없이 재생 중 새 segment를 추가해 timeline refresh와 재생을 검증한다. 이는 로컬 합성 LIVE 검증이지 공개 방송·네트워크 서버 검증이 아니다.
- Test Plan: 세션 자동재생/복귀/해제 단위 테스트, 실제 Media3 VOD 재생과 첫 프레임 렌더링, 상태·background/수동 재개·Activity recreation, 반복 진입 시 인스턴스 해제, 오류·수동 재시도, 갱신 LIVE timeline과 재생을 에뮬레이터에서 검증한다. Release ID 로그 짝을 검사하며 이를 메모리 누수 전체의 증명으로 과장하지 않는다. 실기기 LIVE/VOD, 실제 화면 회전, 시스템 process death는 별도 NOT_RUN.
- Evidence / Commands: [build-log](../../harness/build-log.md)의 실행 환경·명령·결과 및 PR CI. M2 Gate는 [마스터 플랜](../MASTER_PLAN.md)대로 실기기 확인까지 필요하다.
- Open Questions: 실기기 접근과 실제 네트워크 환경에서의 LIVE 검증은 미완료. 동작하는 LIVE 소스와 외부 라이선스는 별도 확인한다. PIP·백그라운드 오디오는 사용자 정책상 제외.
