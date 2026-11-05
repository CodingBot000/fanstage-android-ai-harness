# M8 — 새 clone 소스 재현 기록

## 범위와 환경

- 수행일: 2026-11-05. 동일 개발 환경의 Codex가 수행한 검증이며 **외부 독립 개발자의 재현이 아니다**.
- GitHub `main`을 별도 임시 디렉터리에 새로 clone한 SHA `e09beb205dcf1ae12510225dff710e3e05682cb8`. 기존 작업 트리와 기존 단말 앱 데이터는 변경하지 않았다.
- macOS arm64, JDK 17.0.14, 기존 Android SDK platform 37/build-tools 36.0.0, 기존 Debug 키와 로컬 Gradle 캐시. 임시 API 36 arm64 에뮬레이터는 새 AVD로 만들었다. 빈 Gradle 캐시나 신규 개발자 기기는 검증하지 않았다.

## 실행 결과

- **PASS:** README 빠른 시작의 `./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon`, 새 clone에서 95 tasks executed, JUnit 51 tests / 0 failures / 0 errors / 0 skipped. 로컬 원본 로그 `/tmp/fanstage-m8-repro.6XLDTx/build.log`. Debug APK SHA-256 `7659d75b301b4f6bb1688bbfeb1ee85dd0937b458519848e9974d6ace96eacb2`.
- **PASS:** 별도 에뮬레이터에 Debug APK를 새로 설치하고 `dev.fanstage.android/.MainActivity` 시작. 방 목록→상세, 합성 VOD 영상 표시·Playing→Paused→Playing, Fake 입력 전송 후 `Sent`, 뒤로가기로 목록 복귀를 확인했다. 화면은 합성 데이터만 사용했다. Player 캡처 `/tmp/fanstage-m8-repro.6XLDTx/player.png`는 로컬 증거다.
- **문서 보완:** 기존 README는 CLI 빌드 다음 Android Studio 실행을 안내했으나 터미널만 사용하는 설치·실행 명령은 없었다. 새 에뮬레이터 선택, 기존 패키지를 교체하지 않는 `adb install`, Activity 시작과 Debug 개발 설정 아래에 있는 Fake 입력 안내를 추가했다. 사용한 `adb -s emulator-5556 install ...` 및 `adb -s emulator-5556 shell am start ...`가 위 PASS의 실행 경로다.
- **NOT_RUN:** Android Studio UI를 통한 실행, 외부 독립 개발자 재현, 빈 Gradle 캐시 환경, 실제 원격 LIVE, 실기기 네트워크 전환·실제 LMK·장시간 heap. 기존 성능·WebSocket 장애 CI는 이 작업에서 반복하지 않았다.

## 남은 M8 Gate

외부 개발자 독립 재현과 제3자 라이선스·NOTICE 전수 검토는 미완료다. 루트 라이선스와 공개 배포용 서명 방식은 미결정이며 v0.1 공개 APK는 제공하지 않는다. PR #30의 일회성 위험 수용 대상인 최초 WebSocket 타임아웃 원인은 미확정으로 남는다. M8 전체 VERIFIED를 선언하지 않는다.
