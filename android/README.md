# Android foundation

The Android project contains `:app`, feature modules for discover, community, media, messages and live, and shared core modules including Kotlin/JVM `:core:model`. The app owns navigation and constructs the fake repository
and ViewModel factory. The feature exposes the model dependency because its public
state and factory use the model contract; the model has no Android, UI, storage,
or networking dependency.

The room list supports loading, content, empty, and error with retry. The default
fake returns three fictional rooms. Detail uses a Media3 HLS player with a locally
generated synthetic VOD fixture; an in-memory Fake chat transport exercises ACK, ordering and gap recovery. A debug-only selector can connect the same chat contract to the local WebSocket/HTTP server. The
separate instrumentation test exercises a changing local LIVE playlist.

For the local real-server setup, emulator host address and optional physical-device LAN configuration, see [server README](../backend/chat-server/README.md). Cleartext is explicitly disabled in release and enabled only in debug; use disposable test credentials only.

## Build and test

Use JDK 17 and Android SDK package `platforms;android-37.0` with build-tools
`36.0.0`. Set `ANDROID_HOME` to the SDK location or configure an untracked
`local.properties`. The committed wrapper uses Gradle 9.5.0; no system Gradle or
wrapper regeneration is required. From `android/`:

```sh
bash ./gradlew clean :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --stacktrace
```

The wrapper executable bit was corrected in [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19); its local validation used `./gradlew` after the fix.

The version catalog pins AGP 9.3.3, Kotlin/Compose compiler 2.4.21 and Compose
BOM 2026.09.00. The project uses AGP built-in Kotlin for Android modules and
the Kotlin JVM plugin for `:core:model`. The Kotlin compatibility table cited
in [ADR-0002](../docs/adr/ADR-0002-android-foundation-stack.md) explicitly
lists AGP only through 9.3.1 for this Kotlin version; successful builds of this
particular combination do not extend that published support range.

## Verification evidence

- **Main CI PASS:** [run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235)
  on `e22bb4042173d6b0160b9d46c50837cbfde6bb56` compiled, ran lint and passed
  seven unit tests. Lint reported 13 warnings. The same run passed one API 35
  emulator instrumentation test covering list → detail → Activity recreation →
  back; it installed and launched the app on the emulator.
- **Developer-local clean build PASS:** [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)
  records a macOS arm64 JDK 17 build on base `7fbcc7108d87ecc03951de168c5b3aeb4c29ff52`
  with the wrapper mode fixed. The command in that PR passed clean, assemble,
  lint and seven unit tests; lint reported 13 warnings. Its local log and SHA-256
  are recorded in the [build log](../harness/build-log.md).
- **Physical device:** see [build log](../harness/build-log.md) for API 36 VOD, rotation, background/resume, synthetic LIVE timeline and explicit `am kill` process-death evidence. `am kill` is not a low-memory-killer test.
- **NOT_RUN:** remote-network LIVE, on-screen first frame for synthetic LIVE, actual low-memory process death and prolonged memory profiling. Physical-device performance experiments were performed, but did not establish an improvement; see the [M6 report](../docs/reviews/M6-002-ab-experiments.md).

CI and local results are evidence for their recorded source revisions and
conditions, not a guarantee for every environment. M1 core gates have been
reviewed; the unrun scenarios above remain unverified. See [STATUS](../docs/STATUS.md)
and [build log](../harness/build-log.md) for the current project state.

M2 player behavior and fixture provenance are documented in the [M2 SDD](../docs/specs/M2-hls-player.md). M2 physical-device validation is recorded with the stated limitations.

### PLAYER-001 test fixture and validation

`scripts/generate-player001-fixture.sh` (run from the repository root) uses FFmpeg's synthetic `testsrc2` source to generate a six-second 360p/720p HLS playlist, a 9:16 MP4, and a synthetic English WebVTT caption under `android/app/src/androidTest/assets/player001`. It contains no third-party footage or audio; the audio is a synthetic 440 Hz tone. The generated fixture is approximately 5.0 MiB with FFmpeg 8.0.1; encoder versions can change its bytes. PLAYER-001 is merged. Its final local run passed 56 live unit tests and 41 API 36 AVD instrumentation tests; the remote emulator job was skipped. See the [PLAYER-001 SDD](../docs/specs/PLAYER-001-player-settings-fullscreen.md) for the exact source revision, test scope and remaining limitations.

The bundled captionless HLS advertises `CLOSED-CAPTIONS=NONE`. For HLS with no caption declaration, an inferred CEA-608 choice is labeled as availability unknown; it may contain no captions.

일반 ViewModel 네 개는 Activity 범위의 Hilt 주입을 사용한다. Chat은 Navigation 항목별 Hilt ViewModel을 사용하며 검증 범위와 미실행 기기 검증은 [SDD](../docs/specs/hilt-di-migration-phase2a.md)를 참조한다.
