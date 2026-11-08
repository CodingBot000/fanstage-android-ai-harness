# 2026-11-08 감사 실행 증거

이 폴더의 XML은 실제 Android 계측 실행에서 생성된 JUnit 결과로, 저장소 공백 검사에 맞춰 CRLF를 LF로 변환했고 단위 테스트 XML의 개인 호스트명은 삭제했다. 변환 전 원본과 보관본의 SHA-256을 각각 기록한다. `manifest.json`은 각 파일의 SHA-256, 실행 당시 HEAD, 관련 테스트 코드의 커밋 스냅샷, 기기·변형·건수와 실행 목적을 연결한다. 일부 테스트는 커밋 전 변경된 작업 트리에서 실행했고 해당 커밋은 사후에 생성됐다. 이 경우 전체 저장소가 그 커밋과 일치하는 클린 체크아웃이었다는 뜻이 아니다. 첫 전체 UI 실행의 실패와 별도 재시도 및 전체 재시도를 모두 보존한다. backend JSON은 격리 서버 테스트의 민감정보를 제거한 실제 출력과 종료 코드이며 JUnit 원본을 가장하지 않는다. resource JSON은 해당 계측 실행의 필터링된 관찰값이다. 원본 계측 결과를 생성하거나 과거 실행의 결과로 대체하지 않았다.

`android/`에서 실행하고, 기기가 여러 개면 `ANDROID_SERIAL`과 해당 ADB 서버 포트를 명시한다. 기기 실행 시 기존 앱과 분리된 `.devicecheck` Debug 변형을 사용했다. 실제 서버 통합은 일회성 로컬 컨테이너와 테스트 인증값, USB 역방향 포트로 실행했고 실행 후 임시 리소스만 정리했다. 개인 기기 식별자·인증값은 이 폴더에 포함하지 않는다. 성능·자원 관찰의 한계는 [검증 문서](../../../verification.md)를 참조한다.

대표 실행 명령 (`ANDROID_SERIAL`은 선택된 기기로 설정):

```sh
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -PfanstageHiltInstrumentation=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest --no-daemon --max-workers=2
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.DiscoverTest,dev.fanstage.android.RoomNavigationSmokeTest,dev.fanstage.android.DeviceUiRegressionTest,dev.fanstage.android.PlayerSettingsTest,dev.fanstage.android.UiContractTest,dev.fanstage.android.HiltRoomLifecycleTest --no-daemon --max-workers=2
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.ResourceLifecycleAuditTest -Pandroid.testInstrumentationRunnerArguments.fanAuditRunId=audit20261108a --no-daemon --max-workers=2
```

실제 서버 Android 통합은 `RealChatIntegrationTest`, `fanServerHost=127.0.0.1`, 임시 포트와 USB reverse를 사용했다. 일회용 토큰은 명령 증거에 기록하지 않는다. 서버의 `integration.py`와 `m5_faults.py`는 기본 포트 대신 동일한 격리 포트로 실행했다.

`real-chat-30-physical.xml`은 실제 서버 통합과 룸별 WebSocket 30회 연결에서 Collector 완료, close 호출 및 소켓 참조 해제 확인 결과다. OS 소켓 해제 완료를 직접 관찰한 결과는 아니다. `cold-start-observations.json`은 실기기 Macrobenchmark 원본 JSON에서 측정 반복값을 추출한 파생 자료다. 원본 JSON과 각 10개의 Perfetto trace는 기기 시스템 정보를 포함할 수 있어 로컬에 두고 원본 해시만 기록했다. 단일 순차 실행과 10회 표본으로 Hilt의 인과적 성능 차이를 주장하지 않는다.

回전 테스트는 초기 전체 실행의 실패와 별도로 단독 1회 및 연속 5회 재실행을 모두 보존했다. 모두 통과했지만 초기 실패 원인은 미확정이다. `branch-policy.json`은 GitHub API 읽기 결과이며 직접 push 또는 실패 PR 병합 실험 결과가 아니다.

실기기 서버 검증을 재현할 때는 기존 데이터가 없는 별도 Compose 프로젝트와 미사용 포트를 선택하고 일회용 토큰을 환경 변수로 준비한다. 예를 들어 분리 서버가 호스트 포트 18767에 있을 때 Android 쪽 명령은 다음과 같다. 테스트 토큰을 로그나 Git에 남기지 않는다.

```sh
adb -s "$ANDROID_SERIAL" reverse tcp:18767 tcp:18767
cd android
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RealChatIntegrationTest \
  -Pandroid.testInstrumentationRunnerArguments.fanServerHost=127.0.0.1 \
  -Pandroid.testInstrumentationRunnerArguments.fanServerPort=18767 \
  "-Pandroid.testInstrumentationRunnerArguments.fanTokenA=$FANSTAGE_TOKEN_ALICE" \
  "-Pandroid.testInstrumentationRunnerArguments.fanTokenB=$FANSTAGE_TOKEN_BOB" \
  --no-daemon --max-workers=2
adb -s "$ANDROID_SERIAL" reverse --remove tcp:18767
```

서버 테스트의 기본 포트는 18765이며, 격리 포트 실행 시 Compose의 `FANSTAGE_PORT`와 Python 테스트의 `FANSTAGE_TEST_PORT`를 동일하게 설정한다. Python 서버 회귀는 DB 재시작을 수행하며 허용된 `fanstage-m4-validation` 또는 `fanstage-m5-validation` 프로젝트를 사용한다. 실행 전에 해당 프로젝트에 기존 컨테이너나 볼륨이 없는지 확인한다. Android 계측만 실행할 때는 기존 데이터와 격리된 별도 Compose 프로젝트를 사용할 수 있다.

`unit/`은 JDK 17에서 캐시 재사용 없이 실행한 단위 테스트 XML이다. `historical-manifest-check.json`은 과거 로컬 원본의 해시를 재검사한 파생 요약이다. 각 실행의 현재 공개 파일 해시는 manifest에서 확인할 수 있다.
