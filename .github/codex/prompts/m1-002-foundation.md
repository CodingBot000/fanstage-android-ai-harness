# FanStage M1-002 — Android Native Foundation bootstrap

You are Codex running in a trusted GitHub Actions source-generation job. Follow `AGENTS.md`, `docs/STATUS.md`, `docs/specs/M1-android-foundation.md`, and `docs/adr/ADR-0002-android-foundation-stack.md`. No other milestone.

## Goal (exactly one work unit)

Create a **minimal, real, compilable Kotlin + Jetpack Compose Android multi-module project** in `android/`. This is the M1-002 foundation ONLY. Do not build Media3/HLS, WebSocket, chat transport, server, DB, billing, performance benchmark, or internet services.

## Allowed changes

Only create or edit files inside `android/`. Do not modify workflow YAML, source docs, AGENTS/CLAUDE, secrets, root scripts, or `.github/`. The final JSON output file is written by the tool and is exempt.

## Required structure

- `android/settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `android/.gitignore`.
- Modules: `:app`, `:core:model`, `:feature:live`. Add `:core:testing` only if it eliminates actual duplication; otherwise omit.
- `app` hosts a single MainActivity and navigation. `feature:live` hosts Compose room list and detail screens and ViewModel. `core:model` holds Room model and `RoomRepository` contract, with no dependency on `:app`.
- Use appropriate Kotlin/Compose source and manifest/resources. Namespace must not conflict with an existing third-party product; prefer `dev.fanstage.android` and submodule namespaces.
- FakeRoomRepository implements list fetch with deterministic fake room fixtures; do not add Retrofit, Room database, backend or real videos.
- Room list screen states: Loading, Content, Empty, Error with retry. Detail is a **placeholder**, not a video player. Navigation list -> detail -> back.
- Compose UDF; ViewModel exposes immutable StateFlow and handles loading/cancellation; no UI/Context/ExoPlayer/WebSocket object in ViewModel.
- Basic unit tests for Fake repository and ViewModel state including empty/error, using deterministic coroutine scheduler; if test infrastructure is too costly, clearly state omitted tests as NOT_RUN in output (but prefer implementation).

## Build toolchain candidates (NOT verified until CI)

- AGP 9.3.3 (note Kotlin 2.4.21 official fully-supported AGP range ends at 9.3.1; actual CI validation mandatory)
- Gradle 9.5.0, JDK 17, Kotlin/Compose compiler plugin 2.4.21
- Compose BOM 2026.09.00, compileSdk 37, minSdk 24, targetSdk 36
- AGP 9.x has built-in Kotlin. Do **not** also apply `org.jetbrains.kotlin.android`/kotlin-android; apply `org.jetbrains.kotlin.plugin.compose` to every Compose module.
- Prefer stable AndroidX versions documented in the SDD; pin all dependency versions in Version Catalog. Avoid dynamic versions and alpha/RC without documented need.
- For AGP 9 built-in Kotlin configuration use current `kotlin { compilerOptions { ... } }` DSL when appropriate, not stale `kotlinOptions` DSL.

## Mandatory safety and result

Do not call external paid servers or download binaries yourself; the subsequent separate job installs Gradle wrapper/Android SDK and builds. You may inspect files locally. Never output secret values. Do not claim PASS for builds/tests you did not execute. Your final JSON must conform to `.github/codex/schemas/m1-002-report.schema.json` and contain task_id=M1-002, scope=ANDROID_FOUNDATION, code_generated=true, and "NOT_RUN" in verification because the Gradle job hasn't run.

Stop immediately after creating the bounded project and reporting the expected build commands and known toolchain risks. Do not initiate M1-003/M2.
