# 테스트·검증 결과

테스트 결과는 **해당 기능의 PR, 실행한 환경, 통과 건수**를 기준으로 정리했습니다.

| 기능 | 실행 환경 | 결과·근거 |
|---|---|---|
| Media3 설정·전체 화면·수명주기 | Android 단위 테스트 / 로컬 API 36 에뮬레이터 | **56/56, 41/41 통과** · [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47) |
| 채팅 UI·전송 경로 변경 | Android 단위 테스트 / 로컬 API 36 에뮬레이터 | **56/56, 28/28 통과** · [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53) |
| 키보드·입력창 위치 | Android 단위 / 로컬 API 36 에뮬레이터 / Galaxy 실기기 | **62/62, 24/24, 3/3 통과** · [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59) |
| Compose 기능별 모듈 분리 | Android 빌드·단위/기기 회귀 / 모듈 경계 검사 | **통과** · [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) |
| PostgreSQL·WebSocket·장애 주입 | GitHub Actions의 격리 서버 | **통과** · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42240984227) |
| 실기기 시연 | Galaxy S22 Ultra / 로컬 MP4 | 화면 탐색·재생 영상 검증 · [PR #61](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/61) |
| Hilt DI·룸 수명주기 전환 | Galaxy S22 Ultra/API 36 및 격리 API 36 에뮬레이터 | 선택 일반 39/39, Hilt 바인딩 1/1, 격리 서버 Android 통합 1/1 PASS · [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) · [실행 기록](specs/hilt-di-migration-verification.md) |
| Android 증거·회전 진단 및 통합 게이트 | Galaxy S22 Ultra/API 36 / GitHub Actions | 회전 진단 두 배치 각 20/20, 최종 선택 회귀 42/42, Hilt 3/3, 실제 서버 Android 2/2 기록. 최초 실패 원인 미확정 · [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69) · [공개 증거](evidence/verification/audit-20261108/README.md) |
| UI 공통화·화면 조립 분리 | API 36 에뮬레이터·Galaxy S22 Ultra / GitHub Actions | 변경 PR에 선택 UI 회귀 각 43/43, Hilt 3/3 등이 기록됨. 스크린샷 전후 비교·해당 PR 실제 서버 재실행은 `NOT_RUN` · [PR #70](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/70) · [SDD](specs/ui-designsystem-refactoring.md) |

위 수치는 각 PR에서 기록한 **해당 코드 버전의 실행 결과**입니다.

## Hilt 전환 및 플레이어 실기기 회귀

로컬 JDK 21과 JDK 17·Android SDK에서 Debug·Release 계열 빌드, Lint, 단위 테스트와 모듈·Manifest 검사를 실행했습니다. Galaxy S22 Ultra / API 36에서 선택한 화면·플레이어·채팅 계측 39/39와 Hilt 테스트 바인딩 교체 1/1이 통과했습니다. 별도 API 36 에뮬레이터와 격리된 PostgreSQL·WebSocket 서버를 사용한 Android 연결·복구 통합 테스트도 1/1 통과했습니다. PR #67 당시 실제 백엔드의 실기기 연결, 장시간 누수와 성능 검증은 **NOT_RUN**이었습니다. 이후 실기기 서버 연결과 성능 관찰은 아래 최종 감사 결과로 구분합니다.

PR #67 검증 당시에는 기기 계측 후 포트폴리오 문서를 통합했으며, 그 단계의 Android 코드·테스트는 계측 시점과 동일하게 유지했습니다. 최초 LIVE 테스트 실패, 원인 조사, 일시정지 버튼 수정과 재검증의 명령·범위는 [작업 검증 기록](specs/hilt-di-migration-verification.md)에 남겼습니다. GitHub [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67)과 병합 후 `main`의 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874845) · [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874806) · [문서 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874813)가 성공했습니다. CI의 성공은 로컬 기기 테스트 재실행을 의미하지 않으며, PR #67 당시 원본 로컬 계측 로그의 공개 첨부 여부는 별도 확인되지 않았습니다. Hilt 전용 테스트 APK를 별도 컴파일하는 CI 단계는 [워크플로](../.github/workflows/android-ci.yml)에서 확인할 수 있습니다.


## Android 최종 감사 실행

PR #68 이후의 소스에서 Hilt 실제 Activity 생성 경로, 실기기 화면·플레이어 회귀와 격리 PostgreSQL·WebSocket 연결을 다시 검증했습니다. 추가 로컬 미디어는 무음 합성 영상으로 확인했습니다. 초기 회전 계측 실패와 재실행, 반복 자원 관찰, 성능 원본 해시 및 미해결 위험은 [감사 보고](evidence/verification/audit-20261108/audit-report.md)와 [기계 판독 증거](evidence/verification/audit-20261108/README.md)에 구분해 기록했습니다. **PR #69의 초기 감사 시점에는** `main`에 필수 검사 강제 규칙이 없었습니다. 이후 승인받아 보호 정책을 적용했으며 현재 설정과 차단 실험의 한계는 [정책 기록](specs/ci-required-quality-policy.md)을 참조합니다.

## 증거 무결성과 회전 진단 후속

공개 증거 파일의 해시·JUnit 집계·실패 이력과 출처를 검사하는 CI를 추가했습니다. 의도적 오류 fixture로 검사 실패도 확인했습니다. Galaxy S22 Ultra에서 회전 테스트는 두 진단 버전에서 각 20/20 통과했지만, 이전에 한 차례 발생한 실패의 원인은 확인되지 않았습니다. [후속 근거와 제한](evidence/verification/audit-20261108/README.md) · [적용된 필수 체크 정책](specs/ci-required-quality-policy.md). 2026-11-09 사용자 승인으로 GitHub `main` 보호 정책을 적용했고 관리자 우회는 허용했습니다. 실제 병합 차단 실험은 `NOT_RUN`입니다. 게이트 워크플로는 [PR #69](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/69)를 통해 이미 `main`에 병합됐고, [PR #70의 `required-quality`](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42502757479)가 성공했습니다.

<a id="android-review-20261116"></a>

## 2026-11-16 Android 코드 리뷰 검증

**소스:** 로컬 `main` `5d335ec2f26d4655f713083ecded85b06b44806e`. 제품 코드는 변경하지 않았고 추가 진단 테스트는 로컬 임시 소스셋에서 실행했다. **환경:** macOS arm64, JBR 17.0.14, Android SDK, Pixel 9 에뮬레이터/API 36. 실제 기기·실제 서버 검증과 구분한다. [상세 리팩토링 계획](specs/android-refactoring-plan.md).

| 검사 | 실제 결과와 범위 |
|---|---|
| Debug·Release 빌드 | PASS |
| Android Lint | 오류 0개, 경고 44개. 미사용 리소스 29개 외 버전 알림·플랫폼 관련 경고 포함 |
| 기존 단위 테스트 | 강제 재실행 포함 68/68 PASS |
| 임시 채팅 진단 단위 테스트 | 1/1 PASS. 충돌 뒤 복구 경고가 유지되면서 조회가 실행되지 않는 **현재 동작**을 확인한 결과이며, 문제가 수정됐다는 의미는 아님 |
| 일반 계측 최초 실행 | 선택한 8개 클래스의 45건 중 41 PASS, 4 FAIL, skip 없음 |
| 임시 화면 진단 | 4/4 PASS. DM·댓글 초안의 재생성 후 유실, Compose 시계 진행 후 컨트롤 숨김, IME 화면·좌표 관찰을 수행. 유실 확인의 PASS는 초안 보존 기능의 PASS가 아님 |
| 최초 실패 항목 재검증 | 아래 환경 보정 후 4/4 PASS. 전체 45건을 한 묶음으로 다시 실행한 결과는 아님 |
| 모듈 경계·문서 링크 | PASS. 문서 검사는 상대 링크와 ADR 식별자 범위이며 내용 전체의 최신성을 보장하지 않음 |

### 실행 명령과 초기 실행 오류

첫 Gradle 시도는 SDK 경로 미설정으로 작업 시작 전에 실패했다. 설치된 SDK와 JDK 17을 환경 변수로 지정한 후 다시 실행했다. 아래 Android 명령의 작업 디렉터리는 `android/`다.

```bash
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug \
  testDebugUnitTest :core:data:test --no-daemon --max-workers=2 --console=plain
```

최초 일반 계측의 클래스 선택은 다음과 같다. 실행 기기는 `ANDROID_SERIAL=emulator-5554`로 지정했다.

```bash
review_test_classes='dev.fanstage.android.FloatingNavigationTest,dev.fanstage.android.CollapsingTitleBarTest,dev.fanstage.android.UiContractTest,dev.fanstage.android.DiscoverTest,dev.fanstage.android.DeviceUiRegressionTest,dev.fanstage.android.PlayerSettingsTest,dev.fanstage.android.RoomNavigationSmokeTest,dev.fanstage.android.HiltRoomLifecycleTest'

./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
  "-Pandroid.testInstrumentationRunnerArguments.class=$review_test_classes" \
  --no-daemon --max-workers=2 --console=plain
```

기존 단위 테스트의 강제 재실행과 임시 진단에는 로컬 init script를 사용했다. 해당 script는 `Test` 작업의 up-to-date 처리를 해제하고 임시 Kotlin 테스트 디렉터리를 추가했다. 최초에는 Android의 Java 소스 경로로 잘못 지정해 진단 클래스 로드 오류가 발생했으며, `.kotlin.srcDir`로 수정한 뒤 다시 실행했다. 클래스 로드 오류 실행은 제품 통과 집계에 포함하지 않는다.

```bash
./gradlew -I /tmp/fanstage-review-20261116/review.init.gradle \
  :app:testDebugUnitTest :feature:live:testDebugUnitTest :core:data:test \
  :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
  '-Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.ReviewDraftObservationTest,dev.fanstage.android.DeviceUiRegressionTest#developmentFieldsStayAboveImeWithoutMovingVideo,dev.fanstage.android.DeviceUiRegressionTest#detailHeadersUseBackIconAndKeyboardSendWorks,dev.fanstage.android.DeviceUiRegressionTest#liveChatKeyboardSendAndBack,dev.fanstage.android.PlayerSettingsTest#playbackOverlayShowsOnTapAndHidesOnlyWhilePlaying' \
  --no-daemon --max-workers=2 --console=plain
```

`ReviewGapObservationTest`는 임시 단위 테스트, `ReviewDraftObservationTest`는 임시 계측 테스트다. 둘과 init script는 제품 저장소에 추가하지 않았다. 임시 경로가 없어지면 해당 명령을 그대로 재현할 수 없으며, 절차를 정식 테스트로 옮기는 작업은 계획서 R-00에 남겨 두었다.

저장소 루트에서 실행한 정적 검사는 다음과 같다.

```bash
python3 scripts/check-ux-module-boundaries.py
python3 scripts/check-portfolio-docs.py
git diff --check
```

### 최초 계측 실패와 환경 보정

| 최초 실패 테스트 | 조사·재실행 결과 |
|---|---|
| `DeviceUiRegressionTest.developmentFieldsStayAboveImeWithoutMovingVideo` | IME 표시 이후 입력창 위치 검증에서 시간 초과. 개별 재실행에서도 반복 |
| `DeviceUiRegressionTest.detailHeadersUseBackIconAndKeyboardSendWorks` | 같은 위치 검증에서 시간 초과. 개별 재실행에서도 반복 |
| `DeviceUiRegressionTest.liveChatKeyboardSendAndBack` | 같은 위치 검증에서 시간 초과. 개별 재실행에서도 반복 |
| `PlayerSettingsTest.playbackOverlayShowsOnTapAndHidesOnlyWhilePlaying` | 자동 숨김 대기에서 시간 초과. 개별 재실행과 Compose 시계 진단에서는 통과. 최초 실패 원인은 미확정 |

화면 캡처에서 Google 키보드의 **Try out your stylus** 초기 안내가 앱 입력 영역을 가리는 것을 확인했다. 좌표 진단에서는 IME 표시를 관측한 뒤 안내 화면에서 IME inset이 사라졌다. 이를 확인한 후 테스트 기기의 `secure stylus_handwriting_enabled`를 임시로 `0`으로 바꾸고 해당 실패 항목들을 재실행했다. 제품 코드나 assertion은 수정하지 않았다.

```bash
adb -s emulator-5554 shell settings get secure stylus_handwriting_enabled
adb -s emulator-5554 shell settings put secure stylus_handwriting_enabled 0

./gradlew -I /tmp/fanstage-review-20261116/review.init.gradle \
  :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
  '-Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.DeviceUiRegressionTest#developmentFieldsStayAboveImeWithoutMovingVideo,dev.fanstage.android.DeviceUiRegressionTest#detailHeadersUseBackIconAndKeyboardSendWorks,dev.fanstage.android.DeviceUiRegressionTest#liveChatKeyboardSendAndBack,dev.fanstage.android.PlayerSettingsTest#playbackOverlayShowsOnTapAndHidesOnlyWhilePlaying' \
  --no-daemon --max-workers=2 --console=plain

adb -s emulator-5554 shell settings delete secure stylus_handwriting_enabled
adb -s emulator-5554 shell settings get secure stylus_handwriting_enabled
```

이 기기의 원래 값은 미설정(`null`)이었으며 종료 후 미설정으로 복원된 것을 확인했다. 위 `delete`는 당시 원래 상태를 복원한 명령이다. 다른 기기에서도 원래 값이 미설정이라고 가정하거나 설정을 일괄 삭제하면 안 된다.

### 근거 보존과 한계

- 원본 Gradle 로그, JUnit XML, 화면 캡처와 임시 테스트는 당시 로컬 `/tmp/fanstage-review-20261116/` 및 관련 실행 로그에 보관했다. 이 절은 실행 당시 결과 요약을 저장소에 보존한 것이며 원본 전체를 공개 첨부한 증거 묶음은 아니다.
- 환경 보정 후 전체 최초 계측 묶음의 단일 재실행은 **NOT_RUN**이다. 최초 실패와 재실행 통과를 합쳐 전체 묶음이 한 번에 통과했다고 주장하지 않는다.
- 실제 기기, 실제 백엔드 연결, 별도 Hilt 바인딩 교체 계측, 장시간 누수 분석, Macrobenchmark는 이번 리뷰에서 **NOT_RUN**이다. 일반 계측의 `HiltRoomLifecycleTest`와 전용 러너의 바인딩 교체 검증을 구분한다.
- 재생 자동 숨김의 최초 실패 원인은 미확정이며, 기존 회전 감사의 미확정 원인·장시간 누수 판단도 변경하지 않는다.
- 신규 PR·CI 실행은 없다. 위 결과는 해당 SHA와 로컬 환경의 리뷰 근거이며 리팩토링 구현 후 검증을 대신하지 않는다.

계획서 보존 작업에서는 `check-portfolio-docs.py`, `check-ux-module-boundaries.py`, `git diff --check`와 문서 Bash 예제의 `bash -n` 구문 검사가 PASS였다. 이때 변경한 것은 계획서·현황·검증 문서이며 Android 빌드·계측은 다시 실행하지 않았다.

<a id="android-refactor-a-20261116"></a>

## 2026-11-16 작업 A · 채팅 책임 분리 검증

`origin/main` `deb6be0`와 실행 계획 기준 커밋 `46fa30b`에서 시작했다. `ChatConnectionSettings`·`LocalChatConnectionCoordinator`·`ChatTimelineStore`로 책임을 나누고, 룸의 Job·generation·활성 Transport는 `ChatViewModel`에 유지했다. 검증은 구현 작업 트리에서 실행했으며 제출 커밋과 CI는 해당 PR에서 확인한다.

| 검사 | 결과·범위 |
|---|---|
| 분리 전 기준선 | `feature:live` 단위 59/59 PASS. 기존 충돌·무동작 복구 동작의 재현 사례 포함 |
| 분리 후 단위 | 최종 결과 XML 합계 83/83 PASS. 추가한 인계 직전 취소 사례 뒤 `feature:live`의 73개를 다시 실행했고 app·core:data 결과도 확인 |
| 빌드·Lint | Debug·Release 빌드, 일반 계측 APK 컴파일, `lintDebug` PASS. 오류 0개·경고 44개 |
| Hilt | 전용 계측 APK 컴파일과 바인딩 기기 실행 3/3 PASS. 이후 일반 소스셋으로 전환해 다시 컴파일·실행 |
| 일반 기기 회귀 | API 36 전용 AVD에서 `RoomNavigationSmokeTest`, `DeviceUiRegressionTest`, `HiltRoomLifecycleTest`, `PlayerSettingsTest` 29/29 PASS |
| 실제 서버 기기 계측 | `RealChatIntegrationTest` 2/2 PASS. ACK 유실·조회 지연·재연결·인증·전송·재생 독립성·재생성·반복 구독 종료 검증 |
| 격리 서버 | PostgreSQL·WebSocket의 `integration.py`, `m5_faults.py` PASS. 합성 메시지와 일회용 자격값만 사용 |
| 정적 게이트 | 모듈 경계·문서 링크·공개 증거 무결성 검사 PASS. 품질·증거·회전 진단 스크립트의 기존 단위 테스트 51/51 PASS |

기기 환경은 `FanStage_FloatingNav_20261106` / Android API 36, JBR 17.0.14와 Android SDK다. 기본 ADB 연결에서 첫 일반 계측 실행이 `Connection refused`로 중단되어 완료 결과를 얻지 못했다. 이 실행은 통과 수에 포함하지 않았다. 기본 소켓과 분리한 ADB 포트 5039에서 Hilt·일반·실제 서버 계측을 순서대로 재실행해 위 결과를 확인했다. 첫 환경 실패를 assertion 완화로 처리하지 않았다.

로컬 주요 명령의 작업 디렉터리는 `android/`다.

```bash
./gradlew :feature:live:testDebugUnitTest :app:assembleDebug   :app:assembleDebugAndroidTest --no-daemon --max-workers=2 --console=plain
./gradlew :app:assembleRelease :app:lintDebug testDebugUnitTest   :core:data:test --no-daemon --max-workers=2 --console=plain
./gradlew :feature:live:testDebugUnitTest --no-daemon --max-workers=2 --console=plain
./gradlew :app:assembleDebugAndroidTest -PfanstageDeviceTest=true   -PfanstageHiltInstrumentation=true --no-daemon --max-workers=2 --console=plain
```

격리 ADB와 기기를 준비한 후 아래 클래스 필터로 각각 실행했다. 실제 서버 계측은 일회용 토큰을 별도 환경에서 전달하며 값은 문서에 보존하지 않는다.

```bash
review_a_classes='dev.fanstage.android.RoomNavigationSmokeTest,dev.fanstage.android.DeviceUiRegressionTest,dev.fanstage.android.HiltRoomLifecycleTest,dev.fanstage.android.PlayerSettingsTest'
ANDROID_SERIAL=emulator-5560 ANDROID_ADB_SERVER_PORT=5039   ADB_SERVER_SOCKET=tcp:localhost:5039 ./gradlew :app:connectedDebugAndroidTest   -PfanstageDeviceTest=true   "-Pandroid.testInstrumentationRunnerArguments.class=$review_a_classes"   --no-daemon --max-workers=2 --console=plain

# 같은 기기·ADB 환경에서 Hilt 전용 러너와 실제 서버 테스트도 각각 실행
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true   -PfanstageHiltInstrumentation=true   -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest   --no-daemon --max-workers=2 --console=plain
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true   -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RealChatIntegrationTest   -Pandroid.testInstrumentationRunnerArguments.fanServerPort=18769   "-Pandroid.testInstrumentationRunnerArguments.fanTokenA=$FANSTAGE_TOKEN_ALICE"   "-Pandroid.testInstrumentationRunnerArguments.fanTokenB=$FANSTAGE_TOKEN_BOB"   --no-daemon --max-workers=2 --console=plain
```

서버는 기존 미디어 서버와 다른 포트 18769, 별도 `fanstage-m4-validation` Compose 프로젝트로 실행했다. `FANSTAGE_PORT=18769`, `FANSTAGE_TEST_PORT=18769`와 해당 프로젝트 이름을 테스트 환경에 지정한 뒤 기존 통합·장애 주입 스크립트를 실행했다. 테스트 서버는 검증 후 일반 `down`으로 종료했으며 기존 미디어 서버와 데이터 볼륨을 삭제하지 않았다.

추가 단위 테스트는 연결 성공·인증 실패·타임아웃·늦은 취소 응답·인계 거절·화면 종료·취소된 scope·인계 직전 취소와, 정렬·충돌·불확실 전송·복수 룸 메모리 경쟁·보호된 슬롯을 검사한다. 계측의 `confirmed` 반사 접근은 전용 `ChatMemoryBudget` 관찰로 바꾸고 종료 후 임대 해제 검증을 유지했다.

원본 로그·JUnit·일회용 실행 설정은 로컬 `/tmp/fanstage-refactor-execution-20261116/`에 있으며 공개 첨부 묶음으로 주장하지 않는다. 시각적 전후 캡처 비교, 장시간 누수, 실기기와 정량 성능 측정은 이번 작업에서 **NOT_RUN**이다. 반복 구독 종료나 배치 계측 통과를 장시간 누수 부재로 확대하지 않는다. R-01의 오류 안내·복구 변경과 R-02 초안 보존 변경은 수행하지 않았다.

## 자동화와 기기 검증의 역할

**GitHub Actions**
- [Android CI](../.github/workflows/android-ci.yml): Debug·Release 계열 빌드, Android Lint, 단위 테스트, 모듈 의존성·Manifest 검사
- [서버 CI](../.github/workflows/m4-integration.yml): PostgreSQL·WebSocket 통합, 순번·재전송, ACK 유실·서버 단절 테스트
- [문서 검사](../.github/workflows/docs-check.yml): Markdown 내부 링크·ADR 번호 및 공개 증거 SHA-256·JUnit·출처 무결성 검사

**로컬 Android 기기 검증**
- 에뮬레이터: 플레이어 전환·화질·자막, Compose 화면 복귀, 채팅 연결과 키보드 처리
- Galaxy 실기기: 화면·로컬 MP4 재생 시연, 관련 키보드·입력창 계측

Android 기기 계측 결과는 PR에 별도로 기록하며, GitHub Actions의 빌드·단위 테스트 결과와 구분합니다.

## 장애 재현과 회귀 테스트

메시지의 ACK 유실·재전송, 역순·누락 복구, 오래된 HTTP 응답, HLS 오류를 독립된 테스트 시나리오로 구성했습니다. 구현 계약과 재현 조건은 [장애 주입 테스트](specs/failure-recovery.md)에 정리했습니다.

[테스트 매트릭스](test-matrix.md) · [Android 실행 안내](../android/README.md) · [AI 개발·검증 과정](ai-development-workflow.md)
