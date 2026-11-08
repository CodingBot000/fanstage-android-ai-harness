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

`unit/`과 `unit-rotation-final/`은 각각 해당 시점에 JDK 17에서 캐시 재사용 없이 실행한 단위 테스트 XML이다. `historical-manifest-check.json`은 과거 로컬 원본의 해시를 재검사한 파생 요약이다. 각 실행의 현재 공개 파일 해시는 manifest에서 확인할 수 있다.

`additional-ui-physical.xml`은 별도 탐색바·상단바 5건의 결과다. `synthetic-media-physical.xml`은 실제 Hilt 화면·채팅 전환을 포함한 로컬 미디어 3건이다. 테스트에는 24초 무음 합성 MP4 한 개를 일곱 경로에 복사해 사용했으며 사용자 원본 영상은 사용하지 않았다. 인증값은 XML에 포함되지 않았음을 실행 직후 확인했다.

합성 미디어 경로 검증은 `compose.yaml`과 `compose.media.yaml`을 함께 사용한 별도 Compose 프로젝트에서 실행했다. 테스트 영상은 아래처럼 생성한 24초 무음 합성 MP4 한 개를 `demo-01.mp4`부터 `demo-07.mp4`까지 일곱 이름으로 복사했다. 생성 환경에 따라 파일 바이트는 달라질 수 있다.

```sh
ffmpeg -f lavfi -i 'testsrc2=size=320x180:rate=15' \
  -f lavfi -i 'anullsrc=r=44100:cl=stereo' -t 24 \
  -c:v libx264 -preset ultrafast -g 30 -c:a aac -shortest \
  -movflags +faststart demo-01.mp4
```

실기기의 앱은 18765 포트를 사용하므로, 예를 들어 격리 서버의 호스트 포트가 18767이라면 기기 측 18765를 호스트 측 18767로 reverse한다. 실제 계측은 `LocalMediaTest`, `fanstageLocalMedia=true`, `fanServerHost=127.0.0.1`, `fanServerPort=18765`와 일회용 `fanTokenA`를 지정했다. 기존 reverse와 Compose 볼륨의 존재를 먼저 확인하고, 종료 시 이 테스트가 만든 리소스만 정리한다.

합성 미디어 실행에서는 테스트 종료 후 자동 Compose 정리가 작업 디렉터리 기준 파일 경로 문제로 실패했다. 즉시 Compose 파일의 절대 경로로 이번 임시 프로젝트의 컨테이너·볼륨을 정리하고 reverse 제거 및 기존 서버 유지 상태를 확인했다.

## 자동 무결성 검사

`scripts/check-verification-evidence.py`는 공개 파일의 해시·필수 파일·JUnit 집계와 실패 이력, 소스 커밋의 존재 및 실행 시점 설명을 검사한다. Manifest v2는 XML 결과와 실제 실패 테스트를 명시한다. v1 이력은 고정된 실패 파일·테스트 정책으로 읽을 수 있다. `original_sha256`과 비공개 Perfetto 해시는 형식만 검사하며 비공개 원본 바이트의 일치를 주장하지 않는다. 사후 스냅샷 커밋과 실행 당시 작업 트리의 동일성도 해시나 Git 이력만으로 증명할 수 없다.

```sh
python3 -m unittest discover -s scripts/tests -p 'test_verification_evidence.py' -v
python3 scripts/check-verification-evidence.py
```

해시 검사는 실수나 예상 밖 변경을 탐지한다. 증거와 검사 코드·정책을 함께 변경할 수 있는 주체의 고의적 위변조까지 방지하지는 않는다. GitHub의 병합 통제와 코드 리뷰는 별도로 필요하다.

`rotation-intentional-negative.xml`은 로그 보존 검증을 위해 의도적으로 실패시킨 별도 probe이며 실제 회전 결함 기록이 아니다. 실행 당시 관련 fixture·runner는 미커밋이었고 사후 커밋을 기록했다. 두 파일의 실행 시점 해시를 수집하지 못했으므로 사후 커밋과의 바이트 동일성을 독립적으로 증명하지 못한다.

## 2026-11-09 회전 진단 후속

`rotation-instrumented-initial-01.xml`~`20.xml`과 `rotation-instrumented-final-01.xml`~`20.xml`은 동일 Galaxy S22 Ultra/API 36, 격리 Debug 패키지에서 각각 독립 실행한 원본 JUnit 결과를 줄바꿈만 정규화한 것이다. 두 배치 모두 20/20이며, 최초 38/39 실행의 회전 실패 원인은 재현되지 않아 미확정으로 유지한다. JUnit XML에는 실행 ID가 포함되지 않아 각 XML과 trace의 대응은 수집 스크립트의 순번에 의존하며 파일만으로 동일 실행을 암호학적으로 증명하지 못한다. 각 배치의 실행 당시 HEAD와 이후 코드 스냅샷, 테스트 시작 시 일곱 소스 파일 해시 및 커밋과의 대조 결과는 `rotation-source-fingerprints.json`과 `manifest.json`에 구분했다.

`rotation-initial-trace.json`과 `rotation-final-trace.json`은 각 실행 ID에 해당하는 진단 태그만 실제 logcat 스트림에서 추출한 파생 자료다. `ns`는 Android 단조 시계이며, Activity·Player·Surface·Compose·Dialog 이벤트를 같은 실행 ID로 연결한다. 사용자 입력·URL·토큰은 기록하지 않는다. 필터링 전 진단 태그 스트림의 SHA-256을 기록했지만 CI는 로컬 원본을 재검증하지 않는다. 최종 배치의 각 실행에서 회전 전 재생, 전체 화면 Surface, 회전 후 일시정지와 화면 이탈을 확인했다. 진단 로깅은 실행 타이밍에 영향을 줄 수 있다.

`rotation-diagnostic-preflight.xml`은 오류 첨부 검사·실제 회전·명시적 전체 화면 Activity 재생성 3건의 결과다. `rotation-intentional-negative.xml`은 재현 가능한 격리 probe가 **의도적으로 실패**한 결과이며 Player/Surface 상태와 단조 시간 로그가 AndroidJUnitRunner 실패 XML에 보존되는지 검증한다. 실제 제품 실패가 아니다. API 24–28의 Player snapshot은 Activity decorView만 탐색하므로 별도 Dialog window를 찾지 못할 수 있고, Release/RcLocal 진단은 no-op이다.

`rotation-negative-fresh.xml`은 재사용된 오래된 결과 XML을 잘못 성공으로 판정하지 않도록 실행 시작·종료와 파일 시각을 확인한 뒤 수집한 두 번째 **의도적 실패 probe**다. 이 실행의 테스트 fixture와 Player 진단은 당시 커밋에 있었고 probe runner의 최신성 확인 변경만 미커밋 상태였다. 실제 결함으로 집계하지 않는다. `rotation-final-regression.xml`과 `rotation-final-hilt.xml`은 최종 코드의 실기기 선택 회귀 42/42 및 Hilt 3/3 원본이다. `unit-rotation-final/`은 최종 Android 코드의 JDK 17 단위 테스트 원본으로 호스트명을 가렸다.

단독 회전 반복의 대표 명령은 다음과 같다. 기기 두 대 이상이 같은 ADB 서버에 보이면 대상 분리를 먼저 확인한다.

```sh
cd android
for run in $(seq -w 1 20); do
  ./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true \
    '-Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.PlayerSettingsTest#fullscreenRestoresAfterActualRotation' \
    "-Pandroid.testInstrumentationRunnerArguments.fanRotationRunId=finalRotation$run" \
    --no-daemon --max-workers=2
done
```

실행마다 생성되는 JUnit 결과를 다음 실행 전에 별도 보관해야 한다. 진단 이벤트는 `FSRotationAudit` 태그만 수집해 실행 ID로 걸러낸다. `scripts/verify-rotation-diagnostic-failure.sh`는 선택된 기기만 ADB 서버에 보이는지 확인하고 임시 fixture의 의도적 실패 XML에 진단이 있는지 확인한 뒤 fixture를 삭제한다.

`rotation-negative-unique-run.xml`은 추가 보완된 runner로 실제 기기에서 실행한 의도적 실패다. runner는 매 실행의 임의 ID에 속한 이벤트만 수용하고 단조 시각과 복구 전 Player/Surface snapshot → checkpoint → 의도적 실패 → 복구 후 checkpoint 순서를 검사한다. 미래 시각의 오래된 XML, 서로 다른 실행의 이벤트 혼합, 잘못된 이벤트 순서와 다중 기기 노출을 격리 fixture로 거부했다. 당시 실행 HEAD와 코드 스냅샷은 같고 임시 probe 소스는 추적된 fixture에서 복사됐다.
