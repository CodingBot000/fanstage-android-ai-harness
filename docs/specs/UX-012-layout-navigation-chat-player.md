# UX-012 — 카드 정렬·상세 탐색·채팅 입력·영상 전환

상태: 진행 중. 기준 `561afce`; 기존 사용자 `.gitignore` 미커밋 변경 보존.

## 요구사항
- 그리드와 가로 카드의 같은 용도 카드 높이를 통일하고 콘텐츠 텍스트는 최대 2줄, 초과는 말줄임 처리한다. 2배 글꼴에서도 접근 가능하게 확인한다.
- 모든 상세 화면은 제목 왼쪽에 접근성 이름이 있는 뒤로가기 아이콘을 둔다. 기존 뒤로가기·Navigation 및 테스트 태그를 유지한다.
- LIVE 채팅 및 로컬 대화 입력창의 끝에 전송 아이콘을 배치하고 키보드 전송 동작을 동일한 검증/전송 경로에 연결한다. 채팅 ACK·중복·복구 및 DM의 로컬 상태 계약은 변경하지 않는다.
- LIVE/Replay 이탈 시 영상 표면이 화면과 함께 사라지도록 수정하고 Player/Surface 생성·해제를 검증한다.
- 원본 앱 데이터와 기존 실기기 패키지를 보존하는 별도 디버그 테스트 패키지에서 실기기 검증한다. UI·IME·화면 전환 및 재생/채팅 확인, 실제 실행된 검증만 PASS로 기록한다.
- 내장 imagegen으로 FanStage 런처 아이콘을 만들고 Android 리소스로 적용한다. 원본·프롬프트·해시와 결과를 기록한다.

## 구현 영향과 검증
공통 UI는 디자인 시스템에서 제공하며 각 feature는 계약으로 소비한다. Surface 방식 변경 시 Media3의 SurfaceView 대비 소비전력·HDR/보안 출력 차이를 기록하고, 기존 합성 HLS 범위의 전환과 수명 테스트를 수행한다. 실기기에는 명시적으로 지정한 별도 applicationId만 설치하며 개인 앱 데이터 초기화나 기존 앱 삭제는 하지 않는다.

## Open Questions
없음. 의미 있는 기능 범위·데이터 정책 변경이 필요하면 결정 전 질문한다.

## 검증 결과 (2026-11-06)
- 카드 텍스트는 실제 글꼴의 두 줄 높이를 측정해 고정하고 2줄 초과 시 말줄임한다. 미디어 그리드 및 메시지 목록의 길이가 다른 카드 높이와 1×/2× 폰트, 말줄임을 에뮬레이터 계측으로 확인했다. 최초 2× 테스트에서 Android 비선형 글꼴 배율로 텍스트가 한 줄만 표시되는 결함을 재현했고, 두 줄 실측 방식으로 수정 후 PASS.
- Community/게시물/LIVE/Replay/대화의 상단 제목 옆에 공통 뒤로가기 아이콘을 적용했다. 각 경로의 접근성 레이블 및 대화 화면에서 아이콘이 제목 왼쪽에 있는 것을 계측으로 확인했다.
- LIVE·DM 입력창의 trailing icon과 IME Send를 같은 전송 경로에 연결했다. 에뮬레이터에서 실제 Gboard 전송 키 표시 및 DM/LIVE 각 합성 메시지 전송을 확인했다. 실기기 키보드 캡처는 수행하지 않았다.
- Media3 `PlayerView`의 surface를 TextureView로 지정했다. 에뮬레이터 전환 녹화 프레임에서 영상과 화면이 함께 사라지는 것을 확인하고 PlayerView 제거·기존 init/release 회귀를 실행했다. [전환 영상](../evidence/ux-012/video-exit.mp4), [ADR-0008](../adr/ADR-0008-compose-video-surface.md). DRM/HDR·전력·실기기 성능은 NOT_RUN.
- 전용 ADB 서버의 API 36 AVD에서 격리 로컬 PostgreSQL/WebSocket을 사용한 Android 전체 계측 18/18, 서버 통합·ACK 유실·단절 복구 PASS. 이후 UI 계측 5/5 재실행 PASS. Debug·Release·rcLocal·Benchmark 빌드, lint, 단위 테스트, manifest 및 모듈 경계 검사 PASS.
- 사용자가 테스트 도중 에뮬레이터로 전환을 요청해 실기기 검증은 **NOT_RUN**. 실기기에 설치한 별도 `devicecheck` 테스트 패키지 2개만 제거했고 기존 rclocal 패키지는 유지했다. 일반 Debug 앱은 사용자가 허용한 기존 에뮬레이터에 `install -r`로 갱신·실행했다.
- [Media 그리드](../evidence/ux-012/media-grid.png), [대화 화면](../evidence/ux-012/conversation.png), [아이콘 생성 기록](../portfolio/visual-assets.md). 현재 2배 글꼴 등 검증 범위는 자동·에뮬레이터 기반이며 TalkBack 사용자 검증은 NOT_RUN이다.
- PR 준비: 기존 API 35 LIVE 수명 검증은 그대로 선행하고, 뒤이어 UX-012 UI 5개 사례를 실행하도록 CI wrapper를 추가했다. 스크립트 구문 검사 PASS; API 35의 실제 실행 결과는 PR CI에서 확인한다.

- LIVE 입력을 콘텐츠 스크롤과 분리해 하단 및 키보드 위에 고정한다. API 36 입력·재생·뒤로가기 회귀 PASS. 전체 실제 서버 회귀는 NOT_RUN.
