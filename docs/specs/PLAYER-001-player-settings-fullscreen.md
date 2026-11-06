# PLAYER-001 — 플레이어 설정과 전체 화면

상태: [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) Squash Merge `760ae1ec8e6106f09945ad7228dc062937701c1e`. 로컬 단위 56/56·전용 AVD 전체 계측 41/41, PR 최종 head `edc70e5` 활성 원격 CI PASS. 원격 에뮬레이터 SKIPPED; 별도 독립 정적 리뷰 추가 P0/P1/P2 0건, 공식 GitHub 승인 리뷰 없음. 병합 커밋 `760ae1e`의 main Android·서버 CI도 PASS했다.

## 목표와 범위
LIVE와 Replay의 기존 화면 소유 재생 세션에서 실제 트랙 기반 화질·자막 선택, 배속, 전체 화면을 제공한다. PIP, 백그라운드 연속 재생, 자막 다운로드·크기 변경, DRM은 제외한다.

## 상태·이벤트 계약
UI는 읽기 전용 재생 상태와 지원되는 트랙, 선택 정책, 실제 배속, 탐색 가능 여부를 관찰하고 명시적 액션을 전달한다. 비디오 수동 선택은 현재 지원되는 특정 트랙 고정이며 Auto는 비디오 override를 해제한다. Auto 정책과 실제 선택된 포맷을 구분한다. 트랙 변경 시 유효하지 않은 override를 정리한다. 단일 트랙에서는 선택 불가능한 해상도를 만들지 않는다. 자막은 실제 텍스트 트랙 및 플레이어 선택 상태를 따르며 미지원 시 알린다. 새 세션 배속은 1.0x이고 변경은 실제 playback parameters에 동기화한다. 탐색은 확장자가 아닌 timeline seekability를 따른다.

## Lifecycle과 화면
기존 ADR-0006의 foreground 자동재생, background pause·수동 재개, 화면 이탈 시 release를 유지한다. ADR-0008의 TextureView와 PlayerView를 유지한다. 전체 화면 전환은 같은 세션과 단일 surface를 유지하며 생성·prepare·release를 유발해서는 안 된다. 영상은 FIT으로 비율을 보존한다. Back 우선순위는 설정 닫기, 전체 화면 종료, 상세 이탈이다. 시스템 UI 변경은 복원한다. 회전 및 Activity 재생성은 기존 lifecycle 정책과 별도로 검증한다.

## 예외와 수용 기준
지원하지 않는 트랙과 명령은 선택하지 못하게 한다. 소스 변경과 TrackGroup 교체 시 무효 선택을 표시하지 않는다. 한글·영어 문자열, 접근성 설명과 터치 영역을 제공한다. 실제 영상 및 자막 렌더링과 첫 프레임을 확인한다. 기존 HLS·채팅·오류 복구와 로컬 미디어 비공개 정책을 유지한다.

## 테스트 매트릭스
| 범위 | 실행 결과와 한계 |
|---|---|
| 단위/Fake | **PASS:** live 단위 56/56. 트랙 그룹 교체·무효 override·지원 여부, 자막 선택 상태, 속도 명령과 lifecycle 유지 검사. 가능한 모든 Media3 조합의 단위 전수는 NOT_RUN. |
| Media3 통합 | **PASS:** 합성 다중 화질 HLS·WebVTT에서 트랙 선택과 Auto, 소스 교체, 자막 cue·끄기, 속도·첫 프레임 검사. 실제 미선언 CEA 방송은 NOT_RUN. |
| 전체 화면·Lifecycle | **PASS:** 전용 API 36에서 동일 player output 교체·첫 프레임, 일시정지 위치·설정, Back·HOME·회전·이탈 후 잔상 검사. 전체 전환 프레임·모든 기기의 Surface 중복 부재와 장시간 누수는 NOT_RUN. |
| 회귀·접근성 | **PASS:** 전체 계측 41/41 중 플레이어 12/12, 실제 채팅 1/1, 로컬 미디어 3/3(일곱 영상). Compose 2배 fontScale 레이블·Back 검사. 시스템 글꼴 설정·TalkBack 전수·오디오 청취는 NOT_RUN. |
| 원격·리뷰 | **PASS:** `edc70e5`의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984158) 및 [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227). 원격 에뮬레이터 Job 및 M4 Android 단계는 **SKIPPED**. 별도 정적 리뷰 추가 P0/P1/P2 0건; 공식 GitHub 승인 리뷰 없음. |

## 미검증 항목과 결정
미선언 CEA-608은 사용자의 결정에 따라 선택 가능하게 유지하며 지원 여부 미확인으로 표시한다. 실제 미선언 CEA 방송, 장시간 heap/GC·성능, 전 제조사 기기와 TalkBack 전수는 별도 NOT_RUN이다. 관측하지 않은 전환 프레임의 절대적인 Surface 중복 부재를 주장하지 않는다. PIP는 PLAYER-002 범위로 남긴다.

## 이전 부분 구현 기록 (2026-11-06, 이후 갱신됨)
배속 메뉴는 Media3 playbackParameters 이벤트에 동기화하고 명령을 사용할 수 없으면 진입을 비활성화한다. 새 controller 기본값은 1.0x다. 아직 UI 상태 계약이 adapter에서 완전히 분리되지 않았으며 배속 전용 테스트와 실제 재생 검증은 NOT_RUN이다. 화질·자막·전체 화면, fixture, 구조 ADR은 미구현이다. 위 수용 기준은 충족되지 않았고 병합을 차단한다.

## 이력: 2026-11-06 후속 구현과 검증 (당시 상태)

- `PlayerUiState`에 지원 트랙, 현재 선택 정책 및 실제 배속을 노출하고 controller에서 Media3 override를 적용한다. 그룹마다 세션 내 식별자를 부여하고 그룹 이탈 또는 지원 중단 시 override를 정리한다. PlayerView 연결과 탐색은 화면 소유 adapter가 처리한다. 배속 메뉴는 설정 시트로 확장되었다. 위치 polling은 STARTED에서만 수행된다. 재생 상태와 fullscreen을 UI 상태에 포함한다. 탐색 polling과 Surface 연결은 현재 adapter 경계에 남아 있어 완전한 UI/adapter 분리는 미완료다.
- Dialog 화면 전환 시 controller는 유지하고 TextureView 기반 PlayerView를 교체한다. 명시적 FIT과 안전 영역을 사용하며 시스템 막대 상태나 방향을 바꾸지 않는다. 회전으로 Activity가 실제 재생성되면 ADR-0006에 따라 이전 플레이어는 해제된다. [ADR-0010](../adr/ADR-0010-player-settings-fullscreen.md).
- `scripts/generate-player001-fixture.sh`: FFmpeg `testsrc2` 360p/720p, 15fps, 6초, x264 ultrafast/yuv420p/GOP 30 및 AAC 440Hz 합성음을 사용한다. 직접 작성한 영어 WebVTT, HLS master, 9:16 합성 MP4를 포함하며 약 5.0 MiB다. 외부 영상·개인 데이터는 없다.
- 전용 API 36 AVD `127.0.0.1:5563`, 현재 작업 트리에서 PLAYER-001 계측 6/6 PASS: 실제 다중 화질 트랙 선택·Auto, WebVTT cue 및 표시 중인 SubtitleView 연결·끄기, 배속, 새 Surface 첫 프레임, Back 및 LIVE/Replay 전환, 실제 HOME 후 수동 재개, 실제 화면 회전·복원, 9:16 첫 프레임 및 새 세션 기본 배속. 전체 화면 반복 테스트 로그에서 ExoPlayer Init/Release 각 1회, 동일 ID 확인. [자막](../evidence/player-001/caption.png)과 [세로 영상](../evidence/player-001/portrait.png)은 합성 fixture 화면을 확인했다. 이 검사는 모든 전환 프레임의 잔상 부재나 전 기기에서의 Surface 동작을 증명하지 않는다.
- Media3가 HLS에서 암묵적으로 노출하는 CEA-608 후보는 실제 데이터 존재 여부를 사전에 확정할 수 없다. 합성 fixture는 `CLOSED-CAPTIONS=NONE`을 명시한다. 해당 후보 노출 정책은 추가 검토가 필요하다.
- 트랙 그룹 식별자 교체 단위 테스트 PASS. 나머지 전용 단위/Fake 매트릭스, 실제 소스 교체 시 무효 override, 큰 글꼴·전체 전환 프레임의 잔상, 오디오 청취, 로컬 7개 MP4, 실제 서버 통합, 최종 CI는 NOT_RUN. 기존 unit 및 UI 회귀 범위와 실패는 build-log에 별도로 기록한다. 이 미검증 항목 때문에 수용 기준은 충족되지 않았다.

## 이력: Release Gate 재검증 (2026-11-06, 당시 상태)

- **기능 결함 / P1 FAIL (사용자 결정 전; 아래 수정 참조):** 자막이 없는 기본 합성 HLS 미디어 playlist에서 `captionlessDefaultHlsHasNoSubtitleChoices`가 FAIL. Media3 기본 extractor가 선언이 빠진 MPEG-TS에 CEA-608 트랙을 추론해 선택지에 노출한다. 선언 누락 상태의 실제 CEA-608을 지원할지, 추론을 끄고 빈 선택지를 방지할지는 상충하여 당시 사용자 결정이 필요했다. [Media3 공식 API](https://developer.android.com/reference/androidx/media3/exoplayer/hls/DefaultHlsExtractorFactory), [Media3 이슈 #432](https://github.com/androidx/media/issues/432). 실패 테스트는 유지한다. 화질·자막 선택의 시각적 체크 표시 누락은 수정 후 전용 계측 PASS.
- **테스트 환경:** 기존 18765 서버는 사용자 미디어 및 DB를 보존하기 위해 읽기 전용 미디어에만 사용했다. 기존 실행 컨테이너에서 기존 토큰을 재사용하여 별도 18766 포트·빈 DB 스택을 기동했고 신규 자격 증명은 만들지 않았다. 채팅 계측의 명시 포트 옵션을 추가했다. 첫 격리 실행에서 테스트 UI 엔드포인트가 18765를 사용해 FAIL했으며 테스트가 같은 격리 주소를 사용하도록 수정했다. 초기 수정의 테스트 컴파일 오류는 수정 후 재실행했다.
- **PASS (위 FAIL 테스트 추가 전 코드):** 전용 API 36 AVD에서 전체 계측 39/39, 격리 실제 채팅 1/1, 기존 로컬 MP4 7개 첫 프레임·오디오 트랙·대표 탐색과 로컬 영상/채팅 UI 포함. 새로운 source 교체 검사는 실제 override와 낡은 ID 무효화를 확인한다. 2배 Compose 글꼴의 기본 컨트롤 레이블, 설정 시트와 Back, 전체 화면 반복 후 플레이어 제거 화면 픽셀을 검사했다. 선택한 720p·자막·배속은 전체 화면 왕복 후 선택 상태와 시각 표시를 유지했다.
- **미완료 필수 검증:** 위 P1 및 정책 결정, 전체 단위/Fake 매트릭스, 최종 테스트 집합의 전체 회귀, Dialog 전환 중 전체 프레임의 Surface 중복·잔상 여부 및 전 기기 접근성, 최종 CI/독립 리뷰. 현재 PR·병합 불가. 자동 선택과 WebVTT 등은 실기기 테스트가 아닌 전용 AVD에서 검증했다.
- **선택적 장기 검증:** 다기기·장시간 heap/GC·성능과 실제 음성 청취는 이 작업에서 아직 NOT_RUN이다. 비디오가 재생되는 동안 오디오 트랙 존재는 검증했지만 소리의 청취 품질은 확인하지 않았다. PIP는 PLAYER-002로 남긴다.

### 미선언 CEA-608 사용자 결정과 수정

사용자는 2026-11-06 실제 CEA 자막 호환 유지를 선택했다. 선언이 없고 Media3가 추론한 CEA-608 후보는 선택할 수 있게 유지하며 한국어·영어로 지원 여부 미확인을 표시한다. 실제 자막의 존재가 입증됐다는 뜻이 아니다. 자막이 없는 기본 합성 HLS는 `captionless.m3u8`에 `CLOSED-CAPTIONS=NONE`을 선언하고 기존 미디어 playlist를 참조한다. 선언이 없는 구 fixture를 직접 재생할 경우 미확인 후보가 표시되는 것은 승인된 정책이다. 이전 실패는 위 기록에 보존하며, 사용자 결정 후 회귀는 명시적 무자막과 미선언 후보의 라벨·선택 가능성을 각각 검사한다. 전용 API 36 계측 12/12, Debug 빌드·lint·기존 unit PASS; 최종 전체 회귀 진행 중.

### Release Gate 최신 결과
- 앞선 39/39 전체 PASS 뒤 의도적으로 추가한 무자막 HLS 회귀가 FAIL하여 39 PASS/1 FAIL을 기록했다. 사용자 결정 이후 무자막 master와 미선언 CEA 미확인 표시를 적용하고 두 별도 회귀로 검증했다. 전용 12/12 및 추가 전 전체 41/41 PASS. 이후 회전·상세 이탈 검사에 복귀 화면 잔상 확인을 추가했고 해당 검사 PASS; 이를 포함한 최종 전체 회귀는 별도로 기록한다.
- 전체 화면 반복 동안 동일 ExoPlayer의 Init/Release 각 1회 및 새 TextureView의 첫 프레임, 화면 제거 후 배경 픽셀을 확인했다. 실제 회전 후 상세를 빠져나간 [복귀 화면](../evidence/player-001/post-rotation.png)에 합성 영상 잔상이 없었다. 순간적인 Surface 중복과 모든 전환 프레임을 계측한 결과는 아니며 해당 항목은 NOT_RUN이다.
- **필수 잔여:** 전용 Fake/단위 매트릭스의 모든 후보·선택·명령 불가 경로 및 전환 중 Surface 중복 전수 검증, 원격 CI·독립 리뷰는 미완료다. 실제 HLS/MP4와 UI 계측 PASS를 전용 단위 테스트 전체 PASS로 대체하지 않는다. 초기 구현의 선언되지 않은 실제 CEA 자막(미선언일 경우 미확인으로 표시)의 실방송 데이터 검증도 없다. 최종 테스트 결과가 나오더라도 이 미검증 항목의 영향 판단 전 병합하지 않는다.
- **환경 관련:** 기존 실행 M4 서비스의 토큰을 값 노출 없이 재사용하고 격리 DB를 사용해 실제 채팅 회귀를 실행했다. 기존 로컬 MP4는 읽기만 했다. CI 에뮬레이터 설정은 변경하지 않았다.

### 최종 로컬 실행 결과

- **PASS:** 최종 작업 트리의 전체 Android 계측 41/41, 실패·skip 0. 이 중 PLAYER-001 12/12, 로컬 MP4 3/3(일곱 파일 및 대표 탐색·오디오 트랙), 격리 실제 채팅 1/1. 전용 API 36 AVD, serial `127.0.0.1:5563`. 새 Fake 생명주기 설정 유지 테스트를 추가한 뒤 live JUnit 53/53 PASS. 단위 테스트의 나머지 선정·명령 불가 매트릭스를 모두 실행했다는 뜻은 아니다.
- **PASS:** 최종 Debug/Release/rcLocal/Benchmark/macrobenchmark 빌드, Debug lint, 모듈 방향·M4/M6 manifest 및 `git diff --check`. 선택지 시각 표시 결함을 수정했고 CEA 정책 사용자 결정을 반영했다. 전환 반복 테스트에서 동일 player `ad8577d`의 Init 1회와 이탈 후 Release 1회를 확인했다. Surface 재연결 후 첫 프레임, 회전·복귀 뒤 합성 화면 잔상 및 종료 후 배경 픽셀을 확인했다.
- **FAIL 이력:** 미선언 HLS의 CEA 후보를 무조건 자막 없음으로 가정한 최초 회귀는 FAIL. 사용자 선택 후 명시적 무자막과 추론 후보의 미확인 표기·선택 가능성 두 계약으로 분리해 통과했다. 격리 포트 UI 미설정, 테스트 컴파일 및 선택 표시 assertion의 초기 실패도 build-log에 남겼다.
- **NOT_RUN / 차단:** 후보 도출·선택·명령 불가 전용 단위/Fake 전체 매트릭스, 전체 전환 프레임의 Surface 중복 여부, 원격 CI·독립 리뷰. 해당 필수 검증 전 PR 생성·병합 판단은 보류한다. 실제 미선언 CEA 방송, 장시간 heap/GC·성능, 실기기 전반의 접근성·오디오 청취는 별도 검증이며 PASS로 추정하지 않는다.
- 이번에 만든 18766 격리 컨테이너·합성 테스트 DB는 정리했고 기존 18765 서버의 health 200을 확인했다. 신규 자격 증명은 생성하지 않았고 테스트 산출물에서 기존 토큰 값이 검출되지 않았다.

- 2배 글꼴 검사는 Compose `LocalDensity`의 fontScale=2 환경에서 레이블·시트 Back을 확인한 것이다. 기기 시스템 글꼴 설정 변경과 TalkBack 전체 탐색은 NOT_RUN이다.

## 이력: Draft PR 준비 검증 (2026-11-06)

- 최신 `origin/main` 92e4ae8 기반 분리 작업 트리에서 PLAYER-001만 검증한다. 기존 작업 트리는 보존한다. 독립 사전 리뷰에서 발견한 공용 Download 고정 파일 덮어쓰기, Surface 첫 프레임 증거의 이전 세대 재사용, 전체 화면 텍스트 대비 문제를 수정했다. 테스트 fixture는 매번 고유 cache 디렉터리를 만들고 해당 디렉터리만 정리한다.
- View 전환 시 이전 PlayerView를 먼저 분리한 후 새 View를 연결하고 Surface 세대별 상태를 사용한다. 반복 전환 계측은 AnalyticsListener의 첫 프레임 output 객체가 ExoPlayer의 현재 videoOutput과 동일하고 직전 output과 다른지 검사한다. 일시정지 상태·탐색 위치·속도 유지와 화면 제거 후 픽셀도 검사한다. output identity 검사는 Media3 내부 필드 리플렉션에 의존하므로 버전 변경 시 테스트 재검토가 필요하다. 모든 전환 프레임이나 모든 기기에서 중복 Surface 부재를 증명한다는 의미는 아니다.
- 추출한 선택 규칙 단위 테스트는 이전 그룹·범위를 벗어난 인덱스·미지원 트랙의 override, 자막 선택 상태, 사용 불가능한 명령·해제 후 및 지원하지 않는 속도를 검사한다. 실제 트랙 교체 및 설정 유지도 Media3 계측에서 별도 검증한다.
- 장시간 메모리·전체 제조사·실제 미선언 CEA 방송·TalkBack 전수는 기능 결함이 확인된 항목과 구분해 NOT_RUN으로 유지한다. 원격 에뮬레이터 Job은 사용자 요청에 따라 OFF다.

### 이력: Draft PR용 최종 로컬 Gate

- `origin/main` 92e4ae8 기반 분리 트리에서 Debug/Release/rcLocal/Benchmark/macrobenchmark 빌드·lint, live 단위 56/56 및 전체 Android 계측 41/41 PASS. 이 중 전용 플레이어 12/12, 실제 채팅 1/1 및 로컬 미디어 3/3(7개 영상)이 포함된다. 별도 API 36 AVD 명시적 serial을 사용했고 원격 에뮬레이터는 OFF 유지.
- 이전 View detach→새 View attach 및 세대별 첫 프레임, 실제 Media3 Analytics output과 현재 videoOutput identity, pause·재생 위치·속도 유지, 화면 제거 후 픽셀과 실제 회전 후 복귀 화면을 확인했다. Init/Release 로그는 동일 player 1쌍. 결과는 전용 AVD의 관측 범위에 한정되며 모든 전환 프레임 또는 모든 기기에서의 완전한 중복 부재를 증명하지 않는다.
- 단위 규칙 검사는 그룹 교체·범위 이탈·미지원 트랙의 override, 자막 선택 상태, 속도 지원 값·명령 불가·해제 후 동작을 다루며, 실제 Media3 트랙 교체/Auto/자막·속도 선택은 계측으로 확인했다. 후보 도출의 모든 가능한 Media3 구현 조합은 전수하지 않았다.
- 별도 사전 독립 리뷰의 공용 파일 덮어쓰기·첫 프레임 근거 P1 및 전체 화면 대비 P2를 수정하고 재검토에서 추가 확정 P0/P1 없음. PR 생성 후 해당 head의 독립 리뷰와 원격 CI는 별도로 확인한다. 실제 미선언 CEA 방송, 장시간 메모리·성능, 전 제조사 및 TalkBack 전수, 소리 청취는 후속 NOT_RUN이다.

### 이력: PR 독립 리뷰

Draft [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47)의 최초 head `85c9657`을 별도 컨텍스트에서 검토해 추가 확정 P0/P1은 발견하지 못했다. 테스트 임시 디렉터리의 API 26 의존성 P2는 API 24 호환 File API로 수정하고 전용 검사를 재실행했다. LIVE/Replay 경로의 태그 대기는 실제 output identity를 직접 비교한 반복 전환 검사와 구별한다. 태그만으로 모든 경로의 새 output 첫 프레임을 증명하지 않는다. [독립 리뷰](../reviews/PLAYER-001-pr-review.md). 최종 head 원격 CI는 별도 확인하며 원격 에뮬레이터는 OFF다.

### PR 코드 head 원격 검증

구현 head `604fa44c6d20a54d9aca120dfc69ddb636b6c0f1` 기준 [Android 빌드·lint·단위 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42231894078)와 [PostgreSQL·서버 장애 주입 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42231894057)가 PASS했다. 원격 API 35 에뮬레이터 Job과 M4 Android 에뮬레이터 단계는 기존 사용자 설정에 따라 SKIPPED다. 로컬 API 36 계측 41/41과 구분한다. 해당 코드 head의 별도 독립 리뷰는 추가 P0/P1/P2 지적 0건이다. 문서 후속 커밋의 CI는 PR에서 별도 확인한다.

### SHA별 최종 재검증

독립 리뷰에서 `604fa44`의 로컬 전체 계측 기준이 모호하다는 P2를 확인했다. 실제 기존 전체 41/41은 `85c9657` 구현 트리에서 실행했고, `604fa44`의 API 호환 테스트 수정 뒤에는 전용 플레이어 12/12 및 빌드·단위를 재실행했다. 이후 문서 head `f99c224a37e0be621c9fa1271aef7be805a9fd73`에서 Debug/Release/rcLocal/Benchmark/macrobenchmark 빌드, lint, 단위 56/56과 실제 채팅·일곱 로컬 MP4를 포함한 전체 계측 41/41을 새로 PASS했다. 재사용한 기존 자격 증명은 산출물에서 검출되지 않았고 격리 DB만 정리했다. 문서 전용 후속 head의 원격 CI·독립 리뷰는 PR에서 확인한다.
