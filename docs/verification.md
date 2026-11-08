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

기기 없음: Hilt 바인딩 대체 테스트 실행, 일반 UI·Activity 재생성·룸 재진입, 실제 서버 및 Media3/Socket 수명주기 검증은 **NOT_RUN**. 단위 테스트나 컴파일은 이 기기 검증을 대신하지 않는다. 원격 CI 및 성능·장시간 누수 측정도 NOT_RUN이다.

기기 연결 시 Hilt 테스트는 기존 일반 테스트와 분리해 다음 명령으로 실행한다. 전용 러너는 HiltTestApplication을 사용하므로 일반 계측 테스트를 이 명령으로 함께 실행하지 않는다.

```sh
./gradlew :app:connectedDebugAndroidTest -PfanstageHiltInstrumentation=true -Pandroid.testInstrumentationRunnerArguments.class=dev.fanstage.android.RepositoryBindingsTest
```

일반 계측은 위 속성 없이 기본 AndroidJUnitRunner 및 프로덕션 Application으로 실행한다. Phase별 상세는 각 [Phase 1](specs/hilt-di-migration-phase1.md), [Phase 2A](specs/hilt-di-migration-phase2a.md), [Phase 2B](specs/hilt-di-migration-phase2b.md), [Phase 3](specs/hilt-di-migration-phase3.md) SDD 참조.
