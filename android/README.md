# Android foundation

The Android project contains `:app`, `:feature:live`, and the Kotlin/JVM
`:core:model` module. The app owns navigation and constructs the fake repository
and ViewModel factory. The feature exposes the model dependency because its public
state and factory use the model contract; the model has no Android, UI, storage,
or networking dependency.

The room list supports loading, content, empty, and error with retry. The default
fake returns three fictional rooms. Detail contains video and chat placeholders;
no HLS player or chat transport is implemented.

## Build and test

Use JDK 17 and Android SDK package `platforms;android-37.0` with build-tools
`36.0.0`. Set `ANDROID_HOME` to the SDK location or configure an untracked
`local.properties`. The committed wrapper uses Gradle 9.5.0; no system Gradle or
wrapper regeneration is required. From `android/`:

```sh
bash ./gradlew clean :app:assembleDebug :app:lintDebug testDebugUnitTest --no-daemon --stacktrace
```

`bash` also works on the current `main` where the wrapper lacks its executable
bit. [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)
proposes fixing that mode; its local validation used `./gradlew` after the fix.

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
- **Developer-local clean build PASS, pending merge:** [PR #19](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/19)
  records a macOS arm64 JDK 17 build on base `7fbcc7108d87ecc03951de168c5b3aeb4c29ff52`
  with the wrapper mode fixed. The command in that PR passed clean, assemble,
  lint and seven unit tests; lint reported 13 warnings. Its local log and SHA-256
  are recorded in the PR branch's build log.
- **NOT_RUN:** physical-device installation, actual orientation change and system
  process-death/relaunch. Activity recreation is not proof of those scenarios.
  LIVE playback and performance measurements belong to later milestones.

CI and local results are evidence for their recorded source revisions and
conditions, not a guarantee for every environment. M1 remains open pending
review and its remaining Exit Gate requirements. See [STATUS](../docs/STATUS.md)
and [build log](../harness/build-log.md) for the current project state.
