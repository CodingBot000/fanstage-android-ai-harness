# 2026-11-08 감사 실행 증거

이 폴더의 XML은 실제 Android 계측 실행에서 생성된 JUnit 결과로, 저장소 공백 검사에 맞춰 CRLF를 LF로만 변환했다. 변환 전 원본과 보관본의 SHA-256을 각각 기록한다. `manifest.json`은 각 파일의 SHA-256, 실행한 소스 커밋, 기기·변형·건수와 실행 목적을 연결한다. 첫 전체 UI 실행의 실패와 별도 재시도 및 전체 재시도를 모두 보존한다. backend JSON은 격리 서버 테스트의 민감정보를 제거한 실제 출력과 종료 코드이며 JUnit 원본을 가장하지 않는다. resource JSON은 해당 계측 실행의 필터링된 관찰값이다. 원본 계측 결과를 생성하거나 과거 실행의 결과로 대체하지 않았다.

. `android/`에서 실행하고, 기기가 여러 개면 `ANDROID_SERIAL`과 해당 ADB 서버 포트를 명시한다. 기기 실행 시 기존 앱과 분리된 `.devicecheck` Debug 변형을 사용했다. 실제 서버 통합은 일회성 로컬 컨테이너와 테스트 인증값, USB 역방향 포트로 실행했고 실행 후 임시 리소스만 정리했다. 개인 기기 식별자·인증값은 이 폴더에 포함하지 않는다. 성능·자원 관찰의 한계는 [검증 문서](../../../verification.md)를 참조한다.

대표 실행 명령 (`ANDROID_SERIAL`은 선택된 기기로 설정):

```sh
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -PfanstageHiltInstrumentation=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest --no-daemon --max-workers=2
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.DiscoverTest,dev.fanstage.android.RoomNavigationSmokeTest,dev.fanstage.android.DeviceUiRegressionTest,dev.fanstage.android.PlayerSettingsTest,dev.fanstage.android.UiContractTest,dev.fanstage.android.HiltRoomLifecycleTest --no-daemon --max-workers=2
./gradlew :app:connectedDebugAndroidTest -PfanstageDeviceTest=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.ResourceLifecycleAuditTest -Pandroid.testInstrumentationRunnerArguments.fanAuditRunId=audit20261108a --no-daemon --max-workers=2
```

실제 서버 Android 통합은 `RealChatIntegrationTest`, `fanServerHost=127.0.0.1`, 임시 포트와 USB reverse를 사용했다. 일회용 토큰은 명령 증거에 기록하지 않는다. 서버의 `integration.py`와 `m5_faults.py`는 기본 포트 대신 동일한 격리 포트로 실행했다.
