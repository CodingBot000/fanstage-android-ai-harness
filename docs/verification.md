# Hilt 마이그레이션 검증 기록

2026-11-08, 로컬 macOS, JDK 21.0.10, 로컬 Android SDK, Gradle 9.5.0. Phase 3 작업 트리에서 아래 명령을 실행했다.

```sh
cd android
ANDROID_HOME="$ANDROID_HOME" ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:assembleRelease :app:assembleRcLocal :app:assembleBenchmark :macrobenchmark:assembleBenchmark :app:lintDebug testDebugUnitTest :core:data:test --no-daemon --max-workers=2 --stacktrace
python3 ../scripts/check-ux-module-boundaries.py
python3 ../scripts/m4-check-manifests.py
python3 ../scripts/m6-check-manifests.py
```

결과: Gradle PASS (825 tasks), 경계·manifest PASS, 앱 및 macrobenchmark APK 서명 검사 PASS. JUnit 결과 app 6, live 58, core:data 4건 모두 PASS. Hilt 전용 계측 APK `:app:assembleDebugAndroidTest -PfanstageHiltInstrumentation=true` PASS. 전용 빌드 다음 일반 계측 APK를 다시 빌드해 전환 PASS. 최초 전용 소스 등록 및 그 후 일반 빌드에서 생성 코드 잔존으로 실패했고 소스 등록과 KSP 설정을 수정해 재실행했다.

최초 검증 시 기기가 없어 Hilt 바인딩 대체 테스트, 일반 UI·Activity 재생성·룸 재진입 및 Media3/Socket 계측은 **NOT_RUN**이었다. 이후 실기기 실행 결과는 아래 참조. 실제 서버 검증은 계속 NOT_RUN이다. 단위 테스트나 컴파일은 이 기기 검증을 대신하지 않는다. 원격 CI 및 성능·장시간 누수 측정도 NOT_RUN이다.

기기 연결 시 Hilt 테스트는 기존 일반 테스트와 분리해 다음 명령으로 실행한다. 전용 러너는 HiltTestApplication을 사용하므로 일반 계측 테스트를 이 명령으로 함께 실행하지 않는다.

```sh
./gradlew :app:connectedDebugAndroidTest -PfanstageHiltInstrumentation=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest
```

일반 계측은 위 속성 없이 기본 AndroidJUnitRunner 및 프로덕션 Application으로 실행한다. Phase별 상세는 각 [Phase 1](specs/hilt-di-migration-phase1.md), [Phase 2A](specs/hilt-di-migration-phase2a.md), [Phase 2B](specs/hilt-di-migration-phase2b.md), [Phase 3](specs/hilt-di-migration-phase3.md) SDD 참조.

## 실기기 계측 (2026-11-08)

- 대상 Galaxy S22 Ultra SM-S908N / API 36, ADB 서버 포트 5041. 기존 앱과 분리한 `.devicecheck` 테스트 패키지 사용. 기준 코드 `7dd2422`에 추가한 `HiltRoomLifecycleTest` 포함.
- 전용 Hilt `RepositoryBindingsTest` 1/1 PASS: 테스트 Repository 바인딩 대체를 실제 기기에서 주입했다.
- 일반 러너 37건 중 36 PASS, 1 FAIL, 0 skipped: Discover 9/9, DeviceUiRegression 9/9, PlayerSettings 13/13, UiContract 2/2, RoomNavigationSmoke 3/4.
- 실패: `roomListOpensDetailAndReturns`가 채팅 전송 후 `player_state_playing` 확인(127행)에서 실패. 단독 재실행도 같은 실패. Hilt 도입 전 기준 `0451c02`의 동일 기기·동일 테스트에서도 같은 위치에서 실패했다. 원인은 미확정이며 단순 재시도 성공으로 덮어쓰지 않는다. 실패 이후 해당 테스트의 재생성·반복 입장·추가 리소스 검사는 실행되지 않았다.
- 별도 `HiltRoomLifecycleTest` 1/1 PASS: 룸 초안의 Activity 재생성 보존과 퇴장 후 재진입 시 초기화. 기존 Discover 계측의 메시지 및 Community 상태 재생성 검증도 PASS. 이는 실패한 LIVE 통합 테스트 전체를 대체하지 않는다.
- 해당 실패 테스트의 benchmark 룸은 내장 HLS 및 Fake 채팅을 사용한다. 실제 백엔드 연동 테스트는 에뮬레이터 전용 주소와 별도 격리 서버·토큰이 필요해 이번 실기기에서 NOT_RUN. 장시간 누수·성능 및 실제 네트워크 전환도 NOT_RUN.
- 명령: `:app:connectedDebugAndroidTest -PfanstageDeviceTest=true`에 대상 클래스 필터를 지정하고 Hilt 테스트에만 `-PfanstageHiltInstrumentation=true`를 추가했다. ADB는 포트 5041 사용. 원본 테스트 결과는 로컬 임시 증거로 보존했다.

## LIVE 계측 실패 원인 조사

실패는 IME가 표시되면 `RoomDetailScreen`이 `compact = true`를 전달하고 `PlayerPanel`의 `if (!compact)` 내부에 있는 `player_state_playing` 태그가 제거되기 때문이다. 실기기 진단은 같은 입력·전송 순서에서 IME 표시, 태그 부재, 동일 Media3 Player 인스턴스 및 `isPlaying == true`를 확인했다 (1/1 PASS). 이후 진단을 재생 지속 회귀 테스트로 정리해 `HiltRoomLifecycleTest` 2/2 PASS. Hilt 이전 커밋에서도 원래 테스트 실패는 동일하게 재현되었다. 이는 해당 한 번의 실제 재생 지속 근거이며 모든 기기·장시간 재생을 입증하지 않는다.

판정: 제품 재생 버그 근거는 확인되지 않았으나 기존 통합 테스트의 검증 지점이 현재 UI 구성과 맞지 않으므로 방치하지 않는다. 재생 지속을 실제 Player 상태로 검증하거나 compact 여부와 무관한 상태 표시를 제공하고, 기존 테스트에서 미실행된 재생성·반복 입장·정리까지 재실행한다. 기존 실패 결과는 수정 전 FAIL로 유지한다.

## LIVE 통합 테스트 및 재생 버튼 수정

2026-11-08, 위 실패 이후 기존 테스트의 채팅 전송 후 상태 확인을 같은 Media3 Player의 `isPlaying` 검사로 변경했다. IME를 닫고 기존 일시정지·수동 재개·회전 단계를 실행하며, 고정된 Player의 재생 버튼에는 스크롤 대신 화면 표시를 확인한다. 첫 수정 후에는 compact 상태 태그와 고정 레이아웃의 스크롤 전제가 각각 재차 실패해 수정했다.

추가로 화면 회전 후 실제 플레이어는 READY·일시정지 상태인데 재생 버튼이 보이지 않는 별도 문제를 발견했다. 새 PlayerPanel의 `hasPlayed`가 false라 기존 표시 조건이 차단한 것이다. 일시정지 상태에서는 재생 버튼을 표시하도록 수정했다. 자동 재생, 수동 재개 및 Player 소유권은 변경하지 않았다.

SM-S908N/API 36의 수정된 `roomListOpensDetailAndReturns` 단독 PASS 후 일반 계측 선택 39/39 PASS (Discover 9, RoomNavigationSmoke 4, DeviceUiRegression 9, PlayerSettings 13, UiContract 2, HiltRoomLifecycle 2; skipped 0). 마지막 코드 기준 Hilt RepositoryBindingsTest 1/1 PASS. 전체 Gradle 빌드·lint·단위 (824 tasks), 모듈·manifest 및 두 benchmark APK 서명 검사 PASS. 초기 실패와 재현 이력은 위에 유지한다. 실제 백엔드 연동·장시간 누수·성능·전 기기 검증은 NOT_RUN.
