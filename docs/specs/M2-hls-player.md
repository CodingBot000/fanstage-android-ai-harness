# M2 — Media3 HLS Player SDD

- 상태: VERIFIED — 아래에 기록된 실기기 합성 fixture 및 lifecycle 범위에서 M2 Exit Gate 확인. 후속 테스트 PR CI는 별도 확인하며 검증 범위 밖 항목은 NOT_RUN이다.
- Goal: Compose 룸 상세에서 HLS를 실제 재생하고 생명주기·오류·리소스 소유권을 검증한다.
- In Scope: PlayerController, Media3 ExoPlayer/HLS, 테스트 Fake, VOD 및 별도 갱신 LIVE fixture, loading/buffering/playing/paused/error, 제한된 자동·수동 재시도, lifecycle과 표면·listener·player 해제.
- Out of Scope: 채팅 transport, PIP·백그라운드 오디오, 상시 과금 인프라, 정밀 영상·채팅 동기화. 기본 화면은 합성 VOD fixture이며 LIVE 서비스라고 표기하지 않는다.
- Contract / Architecture: 상세 화면이 player를 소유하며 ViewModel은 소유하지 않는다. 최초 foreground 진입에서 자동 재생, background 진입 시 pause, 복귀 시 사용자 조작으로 재생한다. 이탈·재생성 시 release; 재생성 후 자동 재개하지 않는다. [ADR-0006](../adr/ADR-0006-m2-playback-lifecycle.md)에 이유와 트레이드오프를 기록한다.
- Failure / Recovery: Media3 로드 오류 자동 재시도 최대 2회, 최종 오류 상태와 수동 재시도. 네트워크 제한을 무한 재시도로 숨기지 않는다. 실제 서비스 수준의 네트워크 장애 주입은 M5에서 별도 검증한다.
- Fixture provenance: 앱에 포함된 VOD는 FFmpeg 8.0.1의 `testsrc2`로 직접 생성한 무음 합성 영상이다. `ffmpeg -f lavfi -i 'testsrc2=size=160x90:rate=15' -t 6 -c:v libx264 -pix_fmt yuv420p -g 30 -sc_threshold 0 -an -hls_time 2 -hls_playlist_type vod -hls_segment_filename segment%02d.ts vod.m3u8`. 외부 실존 영상·아티스트 콘텐츠가 아니다. 테스트는 별도 임시 LIVE playlist에서 ENDLIST 없이 재생 중 새 segment를 추가해 timeline refresh와 재생을 검증한다. 이는 로컬 합성 LIVE 검증이지 공개 방송·네트워크 서버 검증이 아니다.
- Test Plan: 세션 자동재생/복귀/해제 단위 테스트, 실제 Media3 VOD 재생과 첫 프레임 렌더링, 상태·background/수동 재개·Activity recreation, 반복 진입 시 인스턴스 해제, 오류·수동 재시도, 갱신 LIVE timeline과 재생을 에뮬레이터에서 검증한다. Release ID 로그 짝을 검사하며 이를 메모리 누수 전체의 증명으로 과장하지 않는다. 에뮬레이터와 실기기의 실제 가로·세로 전환 및 스크롤 후 재생 버튼 접근을 검증한다. 실기기 합성 VOD 첫 프레임, 합성 LIVE timeline/playing 및 `am kill` 기반 process death 복원 결과를 기록한다.
- Evidence / Commands: [build-log](../../harness/build-log.md)의 실기기 실행 환경·명령·결과 및 PR CI. 실기기에서 VOD 첫 프레임, 실제 HOME background/수동 재개, 가로·세로 전환, 6회 진입/이탈, 오류 화면 Retry, 갱신 LIVE playlist의 timeline/playing, `am kill` 이후 복구를 확인했다. player 13쌍·codec Surface 30쌍의 로그와 별도 controller/listener 수거 검사를 확인했으나 전체 heap 무누수를 보장하지 않는다.
- Open Questions: 원격 네트워크 LIVE 및 합성 LIVE의 실기기 첫 프레임 렌더링, 실제 저메모리 종료(LMK), 장시간·다기기 heap/profile 검증은 NOT_RUN이다. 실제 LIVE 소스와 외부 라이선스는 별도 확인한다. PIP·백그라운드 오디오는 사용자 정책상 제외. 최초 listener 수거 검사 실패의 정확한 원인은 확인되지 않았으며 메인 큐 정리 후 재검증은 연속 통과했다.

## 플레이어 컨트롤 표현
영상 렌더링 높이와 기존 생명주기·오류·재시도 계약을 유지하며 재생/일시정지를 영상 중앙의 아이콘 버튼으로 제공한다. MP4 탐색은 터치 영역을 유지하면서 얇은 코랄색 트랙으로 표현한다.
- 검증: Debug 빌드·lint·단위 PASS. API 36 전용 AVD에서 실제 MP4 첫 프레임·7개 영상, 아이콘의 영상 내부 위치·일시정지/재생·탐색, 합성 HLS·수명주기·화면 회전 및 기존 탐색/채팅 포함 25/25 PASS. 화면 회전 직후 애니메이션 중 버튼 검사 두 차례 FAIL, 전환 완료 후에도 실제 표시되는지 기다려 검사하도록 수정 후 PASS. 실기기·장시간 성능 NOT_RUN.
