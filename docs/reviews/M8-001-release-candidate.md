# M8-001 릴리스 후보 검증 기록

## 상태: M8-001 완료 — 사용자 승인 일회성 예외 적용

[PR #30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30)은 head `44bd3ccaa7e412bbca6bac29e80bed46c7a3c256`의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42003790103)와 [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42003790152) PASS 후 `74e417ad604fab227a9fbca0f7e28f1728130859`로 Squash Merge됐다. 최초 타임아웃은 미해결 위험으로 남는다. M8 전체는 M8-002에서 별도 판단한다.

2026-11-05 사용자가 최초 WebSocket 타임아웃의 미확정 위험에 대해 PR #30과 M8-002 진입에 한정한 [일회성 예외](M8-001-risk-exception-proposal.md)를 승인했다. 최초 원인과 제품 영향은 미확정이며 향후 P1 실패·공개 APK·운영 검증에는 예외가 적용되지 않는다.

PR [#30](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30) head `d85568924b9b7929abe0ea92939a1f1d368fa382`: [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41991999002) PASS (빌드·단위 및 API 35 계측 3건). 자동 리뷰의 테스트 수 불일치 P1은 수정 후 CI PASS로 해결 처리했다. [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41991999118)는 attempt 1에서 `RealChatIntegrationTest` 20초 타임아웃으로 FAIL, attempt 2에서 PASS. 최초 실패 로그에는 어느 20초 대기 지점인지 드러나지 않아 원인은 미확정이다. 단순 재시도 성공만으로 최초 실패 원인이 해결됐다고 주장하지 않아 당시 병합을 보류했다.

### WebSocket 타임아웃 추가 진단

- 기존 20초 대기마다 고유 단계 ID와 경과 시간·연결/Job 상태를 기록하고, ACK 프록시의 accept·업스트림 연결·요청/응답 핸드셰이크 전달 단계와 비동기 프록시/observer 예외 유형을 추적한다. 테스트 로그에는 토큰·인증 헤더·본문을 출력하지 않는다. CI는 `M8Rca` 태그만 수집해 기존 비밀값 치환 절차를 거쳐 artifact로 보존한다. 제한 시간은 늘리지 않았다.
- 동일 Android WebSocket 계측을 격리된 로컬 서버와 읽기 전용 API 36 에뮬레이터에서 수정 후 3회 단독 실행해 **3/3 PASS**. `ack.both_connected`는 각각 657/911/761ms, 단계 실패 0건이다. 예비 실행도 PASS였으나 최종 코드와 분리해 집계한다. 로그 `/tmp/fanstage-m8-rca-final-{1,2,3}.log`, `/tmp/fanstage-m8-rca-phases.log`는 로컬에만 보관한다. 테스트용 임시 격리 서버·에뮬레이터만 종료·제거했다.
- **의도된 실패 재현:** 서버를 꺼 둔 격리 에뮬레이터에서 동일 테스트는 예상대로 FAIL(exit 1), 419ms에 `ack.both_connected`, sender CONNECTING, observer RECONNECTING, proxy accepted 이후 `ConnectException`을 테스트 결과와 태그 로그에 기록했다. 테스트 자격값은 두 로그에서 검색되지 않았다. 이는 진단 경로를 확인한 인위적 연결 실패이며 최초 CI 타임아웃의 재현이나 원인 증명이 아니다. 로컬 로그 `/tmp/fanstage-m8-rca-offline.log`, `/tmp/fanstage-m8-rca-offline-phases.log`.
- 진단 변경 head `e0b9aeb4475e96017adc521555e0342948c7a400`의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41996401282) 및 [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41996401209)는 PASS. 서버 CI artifact의 `ack.both_connected`는 50ms, 단계 실패 0건이며 제한적 비밀 패턴 검사도 0건이다. 인위적인 값으로 sanitizer의 진단 로그 보존과 자격값 치환을 별도 검증했다. 당시 독립 리뷰는 미완료였다.
- `e0b9aeb` 독립 Codex 리뷰 완료: 원래 예외의 스택·cause가 누락되는 P2 지적을 확인했다. 예외 메시지·파일명을 배제하고 허용된 코드 위치·줄 번호와 원인 유형 체인을 복사하도록 수정했다. 민감한 합성 문자열과 중첩 cause를 이용한 계측 회귀 PASS(1/1); 서버 미기동 인위적 실패에서 단계 ID, 원인 유형, 코드 줄 번호가 보이고 테스트 자격값은 로그에 없음을 확인했다. 이는 최초 타임아웃 재현이 아니다. 후속 수정 head의 CI는 [PR 검사](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/30/checks)에서 확인한다.
- **Gate 판단 및 예외:** 최초 실패는 채팅 연결·복구에 관련된 차단 검증의 실제 실패다. 로컬 3회와 이후 CI PASS는 현재 실행 조건에서의 성공 근거지만 최초 실패가 제품 결함·테스트 경합·CI 환경 중 무엇인지 배제하지 못한다. 위험은 P1로 남으며 사용자가 이번 PR에 한해 일회성 예외를 승인했다. 이후 동일 문제가 재발하면 진단 증거를 보존해 다시 조사한다.
- 최초 CI 실패는 단계 계측 추가 이전이라 해당 실패의 정확한 지점과 원인을 복원할 수 없다. 양측 CONNECTED 대기는 경과 시간상 후보이지만, 테스트 경합·CI 환경·제품 결함 중 확정 가능한 원인은 없다. 반복 PASS는 관측 범위의 비재현을 뜻한다. 스택 보존 수정 후 별도의 신규 독립 리뷰는 확인되지 않았으며 기존 독립 리뷰의 지적 수정과 로컬·CI 검증 범위를 구분해 기록한다.

### 로컬 RC 추가 검증 (2026-11-05)

- **PASS — 빌드·서명·설치:** 기존 Debug 키로 서명한 `app-rcLocal.apk` SHA-256 `7103dfef4df9d3268afb8be581d24fad1e22514f81c3e51c5f839886aaad79b1`. `apksigner verify --verbose` v2 서명 확인. Android API 36 실기기에 `dev.fanstage.android.rclocal` 신규 설치 성공. 기존 `dev.fanstage.android` 패키지는 설치 상태를 유지했다. RC는 로컬 검증 전용이며 배포용 서명이 아니다.
- **PASS — 빌드 및 경계:** `:app:assembleRcLocal :app:assembleRelease :app:assembleBenchmark :app:lintDebug testDebugUnitTest` 성공 (253 tasks 중 174 up-to-date); m4/m6 매니페스트 스크립트 성공. 실제 APK 매니페스트에서도 cleartext와 backup false, debuggable/profileable 및 benchmark Activity 부재를 확인했다.
- **PASS — RC 정상 흐름 (API 36 실기기):** 방 목록→상세→뒤로가기, 합성 VOD 영상 표시·재생→일시정지→재생, Fake 메시지 전송과 `전송됨` ACK를 확인했다. 한국어 및 앱별 영어 설정에서 목록·상세·Player·채팅 주요 문구를 확인하고 앱별 언어 설정을 원복했다. RC 앱 종료·재실행 후 목록과 재입장 시 기본 Fake 채팅·재생 상태를 확인했다. 세 차례 Player Init/Release 로그 쌍을 관찰했으나 전체 리소스 누수 부재의 증명은 아니다.
- **PASS — RC 장애 흐름 (API 36):** 기존 키로 서명한 별도 androidTest APK를 설치해 기존 RC APK (위 SHA-256 변경 없음)에 계측했다. 미존재 HLS 오류 표시·Retry·재오류, Fake 전송 거절·Failed·Retry·재실패 및 Player 지속 재생, 알 수 없는 전송 결과가 확정되지 않고 Pending/uncertain으로 유지됨을 확인했다. 2개 계측 테스트 PASS. 장애 fixture는 테스트 APK에만 있고 일반 Release에 주입 화면은 추가하지 않았다.
- **리소스 정리:** 위 세 번의 실제 화면 Player Init/Release 쌍과 별도 RC 계측의 해제된 Player controller·listener 약한 참조 회수, Chat ViewModel 종료 시 이벤트 Flow 취소와 transport.close 호출을 확인했다. 코드상 Player는 listener 제거·surface clear·release, AndroidView는 player 연결 해제, 화면 ViewModel은 Job 취소와 transport 종료를 수행한다. 기본 RC Fake 경로에는 소켓이 없다. 실제 WebSocket 종료 코드는 취소·socket cancel·pool eviction을 포함하지만 이번 RC에서 실기기 소켓 종료를 관측하지 않았다. 장시간 heap, 전체 누수 부재, Surface 회수 자체는 NOT_RUN이다.
- **공개물 감사 진행 중:** 기존 제한적 Git 객체·CI 산출물 패턴 점검은 아래 기록 참조. 현재 추적 파일명과 과거 커밋 파일명에서 원본 trace·keystore·개인키 확장자는 발견되지 않았으며 trace 분석 스크립트는 존재한다. 로컬 RC APK 175개 항목 중 HLS fixture와 AndroidX 라이선스 파일이 확인됐고 이름 기준 trace·키 파일은 발견되지 않았다. 이는 모든 개인정보·비밀 부재의 증명이 아니다. 의존성별 라이선스, 콘텐츠 권리, CI 로그·artifact 추가 감사와 최종 공개물 검증은 미완료다. 루트 LICENSE도 없고 라이선스는 미결정이다. 공개 APK는 준비되지 않았다.
- README의 실기기 성능 미실행 문구는 실제 M6 실험 이력과 충돌해 바로잡았다. 실험 변경은 되돌렸으며 개선 효과는 입증되지 않았다. 전체 TalkBack, 장시간 재측정, 실제 LMK, 원격 LIVE는 NOT_RUN이다.
- 사용자는 CI 전용 일회성 Debug 키 생성을 허용했다. 공개 Release 키 생성·등록은 승인되지 않았으며 일반 Release에는 서명을 적용하지 않는다. PR 리뷰·CI 결과는 위 상태 항목 참조.

### 추가 감사 (2026-11-05)

- Git 전체 도달 객체 1,375개 중 blob, 현재 추적·미추적 파일, rcLocal APK 모든 엔트리 및 기존 Android/서버 CI 두 실행의 로그에 개인키 헤더와 일부 GitHub·AWS·OpenAI 키 패턴을 검사해 일치 0건. CI 로그에서 macOS 사용자 홈 경로 패턴도 0건. 제한된 패턴이므로 모든 시크릿·개인정보 부재의 증명은 아니다. 기존 두 실행의 artifact 33개 제한 검사는 아래 이력 참조. 원본 trace와 실기기 개인정보는 Git 이력의 파일명 및 로그 패턴 검사 범위로만 확인했다.
- `:app:dependencies --configuration rcLocalRuntimeClasspath` 출력에서 식별한 좌표 122개의 POM 라이선스 표기를 로컬 캐시 및 Google/Maven Central 저장소에서 확인했다. 직접 표기가 없는 Guava 3개는 해당 parent POM의 Apache 2.0 표기를 확인했으며 나머지 119개도 Apache 2.0 계열로 표기됐다. 이는 제약·중복 좌표를 포함할 수 있는 메타데이터 조사이며 실제 포함 바이너리별 NOTICE, 서버 의존성 및 제3자 고지 의무 전체 검토는 미완료다. 합성 영상 생성 명령과 출처는 M2 SDD 참조. 루트 LICENSE 미결정이며 임의로 선택하지 않았다.
- v0.1 제공 경로는 사용자 결정에 따라 공개 APK 없이 소스와 로컬 빌드 절차다. rcLocal APK 및 테스트 APK는 내부 검증 전용이다. 라이선스 확인 및 공개물 최종 검토 잔여분은 M8-002에서 추적한다.

계측 명령: `cd android && ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew :app:assembleRcLocalAndroidTest -PfanstageRcInstrumentation=true --no-daemon`; `adb install -r app/build/outputs/apk/androidTest/rcLocal/app-rcLocal-androidTest.apk`; `adb shell am instrument -w -r -e class 'dev.fanstage.android.RoomNavigationSmokeTest#rcFaultUiAndReleasedListener,dev.fanstage.android.RoomNavigationSmokeTest#rcUncertainMessageAndChatCleanup' dev.fanstage.android.rclocal.test/androidx.test.runner.AndroidJUnitRunner`. 첫 설치는 `-r` 없이 수행했다. 로그는 로컬 `/tmp/fanstage-m8-rc-instrument-test2.log`.

아래는 기존 검사 당시 소스 `abdaf6ba94131357ca34da7803b5d5de64afe076` (PR #29 squash merge)의 이력이다. 이후 위의 로컬 RC 빌드 설정과 검증을 추가했다. 일반 사용자 데모는 로컬 합성 VOD와 Fake 채팅이다. 실제 서버는 Docker Compose와 Debug 앱, 임시 토큰을 사용하는 개발 전용이며 공개 서버나 운영 인증이 아니다.

- **PASS — 로컬 빌드:** JDK 17, SDK 37, `:app:assembleRelease :app:assembleDebug :app:assembleBenchmark :app:lintDebug testDebugUnitTest`. 첫 시도는 SDK 경로 미설정으로 실패했고 `ANDROID_HOME` 설정 후 성공했다. 일부 작업은 기존 Gradle 출력 재사용.
- **PASS — 변형 경계:** `scripts/m4-check-manifests.py`, `scripts/m6-check-manifests.py`. Release는 cleartext false, non-debuggable, profileable 및 benchmark Activity 없음. Debug만 로컬 cleartext 허용. Benchmark는 별도 applicationId와 debug 서명, shell profileable이다. APK 매니페스트에서도 cleartext false와 backup false를 확인했다.
- **PASS — 해당 SHA 기존 CI:** [Android build/unit 및 API 35 emulator](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41984975533), [격리된 PostgreSQL 및 실제 WebSocket](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41984975625). 후자는 ACK 유실·연결 단절과 서버 연동을 포함한다. 구체 범위는 각 실행 로그와 기존 SDD를 따른다. 이는 Debug 검증이지 Release APK 설치 검증이 아니다.
- **기존 기기 증거:** [M7 build log](../../harness/build-log.md)의 API 31 물리 기기 및 API 36 에뮬레이터 결과는 당시 소스와 조건에 한정한다. 한국어·영어, 큰 글자와 Player/채팅 회귀를 포함한다. 최종 PR 수정 후의 새 Release 실기기 테스트는 아니다.
- **서명 및 설치 NOT_RUN:** `app-release-unsigned.apk` SHA-256 `3fa398fb9a745db4b570bf0dca0bcd724e41512e57d7a4155c584f002a0837d2`의 `apksigner verify`는 서명이 없어 실패했다. 배포 가능한 APK가 아니다. 기존 실기기에는 같은 패키지가 설치되어 있어 데이터를 덮어쓰지 않았다. 공개 서명 키를 생성하지 않았다.
- **제한적 보안 점검:** 전체 Git 객체, 위 두 CI의 다운로드된 artifact 33개와 APK 175개 엔트리에 대해 개인키 헤더, 일부 GitHub/OpenAI/AWS 키 형식의 패턴을 검색하여 일치 0건. 제한된 패턴 검사이며 모든 비밀·개인정보·과거 artifact에 대한 부재 증명은 아니다. 앱 메인 코드의 명시적 로깅 호출 검색에서 민감 로그 호출을 발견하지 못했다. APK에 개발 토큰이 없다는 포괄적 증명과 모든 공개 artifact 감사는 미완료다.
- 합성 영상은 [M2 SDD](../specs/M2-hls-player.md)에 생성 출처가 기록되어 있다. 저장소 라이선스는 미결정이며 의존성 및 전체 콘텐츠의 배포 권리 검토는 미완료다.

## 재현

JDK 17과 Android SDK platform 37/build-tools 36 및 기존 Debug 키가 준비된 환경에서만 실행한다. 아래 Debug·Benchmark 빌드는 키가 없으면 Android 도구가 새 키를 만들 수 있으므로 실행하지 않는다.

```sh
export ANDROID_HOME="$HOME/Library/Android/sdk" # 환경에 맞게 변경
test -f "$HOME/.android/debug.keystore" || { echo 'Existing debug key required'; exit 1; }
cd android
./gradlew :app:assembleRelease :app:assembleDebug :app:assembleRcLocal :app:assembleBenchmark :app:lintDebug testDebugUnitTest --no-daemon
python3 ../scripts/m4-check-manifests.py
python3 ../scripts/m6-check-manifests.py
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify app/build/outputs/apk/release/app-release-unsigned.apk
```

마지막 명령은 현재 서명되지 않은 APK에 대해 실패한다. 별도 승인된 배포 서명 방식이 정해지기 전에는 설치 명령이나 공개 다운로드를 제시할 수 없다. 로컬 서버 절차는 [서버 README](../../backend/chat-server/README.md)를 참조한다.

## 미완료 및 초안 릴리스 노트

- NOT_RUN: 실제 배포용 Release APK 설치, 실기기 WebSocket 종료 및 장시간 heap, 포괄적인 공개물/라이선스 감사. 로컬 RC의 통과 범위는 위에 별도 기록한다.
- 기존 M6 실기기 baseline과 두 A/B 실험은 [보고서](M6-002-ab-experiments.md)에 있다. 결과가 불확실하거나 악화되어 두 변경을 되돌렸으며 성능 개선을 주장하지 않는다. M7 이후 UI에 이전 baseline을 적용하지 않는다. 추가 장시간 측정, 실제 LMK, 원격 LIVE는 NOT_RUN.
- 릴리스 노트 초안: “합성 VOD 재생과 가상 채팅을 제공하는 한국어·영어 오프라인 Android 데모. 실제 WebSocket은 별도의 로컬 개발 환경에서 검증. 서명·설치 및 공개 검토 완료 전 배포 불가.”
