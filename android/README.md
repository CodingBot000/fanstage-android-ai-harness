# FanStage Android · Build & Test

## Project

**Kotlin / Jetpack Compose / Coroutines / Flow / MVVM-UDF / Media3**, 11 Gradle modules:

- **app**: Navigation, app assembly
- **feature**: discover, community, media, messages, live
- **core**: model, data, artwork, designsystem
- **macrobenchmark**: separate release-like performance fixtures

The default Debug application runs without a server using fictional content, synthetic HLS VOD and Fake chat. A Debug-only transport selector can connect to the local Spring Boot WebSocket/HTTP server.

## Requirements

- JDK 17 and Android Studio or command-line Android SDK
- Android SDK platform **37**, Build Tools **36.0.0**
- Gradle wrapper **9.5.0** (included)
- Android **minSdk 24**, **targetSdk 36**, **compileSdk 37**

Set ANDROID_HOME or a local.properties SDK path.

~~~bash
cd android
./gradlew :app:assembleDebug :app:lintDebug testDebugUnitTest :core:data:test --no-daemon
~~~

Open the **android/** directory in Android Studio and install the Debug variant. Test-specific synthetic HLS/MP4/WebVTT fixtures are bundled under androidTest assets; the ordinary app contains a separate synthetic HLS VOD.

## Test scope and reference

- [PR #47](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/47): Media3 source revision — Unit 56/56, local API 36 instrumentation 41/41.
- [PR #53](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/53): chat/transport source revision — Unit 56/56, local API 36 instrumentation 28/28.
- [PR #59](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/59): IME/UI source revision — Unit 62/62, local API 36 instrumentation 24/24, physical Galaxy instrumentation 3/3.

Android CI performs compile, lint, unit, module-boundary and manifest checks. The Android emulator jobs are **not part of the automatic PR gate**; device instrumentation results above were produced locally. See [verification](../docs/verification.md) for the distinction.

## Video / live / local server

- In-app synthetic HLS is **VOD**, even when opened from a LIVE-themed demo room.
- The optional private local MP4 service is HTTP **Progressive VOD**, not a live-streaming server.
- Local chat integration uses WebSocket events and HTTP snapshots with PostgreSQL; no production credentials or public deployment are required.

To start the isolated chat or optional MP4 server, see the [backend README](../backend/chat-server/README.md). Playback and transport contracts: [Media3](../docs/specs/media3-player.md) · [Chat](../docs/specs/realtime-chat.md).
