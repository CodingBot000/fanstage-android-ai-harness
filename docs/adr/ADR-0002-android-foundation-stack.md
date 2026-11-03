# ADR-0002 — Android Foundation stack and module boundaries

- Status: ACCEPTED AS CANDIDATE (2026-11-04); actual version set remains conditional on green CI.
- Milestone: M1
- Related: [M1 SDD](../specs/M1-android-foundation.md)

## Decision

Use native Kotlin + Jetpack Compose UDF/ViewModel/Flow. Start with only useful modules: `:app`, `:core:model`, `:feature:live`, optionally `:core:testing` when justified. No backend/media/socket until separate milestones. Persist stable versions in `gradle/libs.versions.toml` after real resolution and build.

Candidate environment JDK 17, AGP 9.3.3, Gradle 9.5, Kotlin/Compose compiler 2.4.21, Compose BOM 2026.09.00, compileSdk 37, minSdk 24, targetSdk 36. These are not claims of confirmed interoperability.

## Rationale and alternative

AGP 9.x enables built-in Kotlin; applying `org.jetbrains.kotlin.android` on top of it is an error. Compose compiler plugin is still needed for Compose modules. Kotlin's published KGP compatibility table covers AGP through 9.3.1 at Kotlin 2.4.21; 9.3.3 fixes lint/other issues but is beyond that explicit version range. Therefore **do not claim compatibility from version numbers alone**; verify in CI. Fall back to an official fully supported pairing or adjust the stack after recording a reproducible failure.

Avoid a large multi-module empty structure, Hilt/Room/Network/Media3 before they have real responsibilities. Favor simple Fake Room data and deterministic state tests.

## Trade-offs

Newer AGP toolchain can break plugin integrations; actual Gradle and Android SDK downloads take CI resources. Version pinning is stable for reproduction but requires maintenance. A small initial module graph may need refinement when chat/media arrives.

## Official evidence

- https://developer.android.com/build/releases/agp-9-3-0-release-notes
- https://developer.android.com/build/releases/about-agp
- https://developer.android.com/build/migrate-to-built-in-kotlin
- https://kotlinlang.org/docs/gradle-configure-project.html
- https://kotlinlang.org/docs/compose-compiler-migration-guide.html
- https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler

## Review gate

Version selection remains `PROPOSED` until CI resolves dependencies and runs compile/lint/tests. If this fails, create a focused ADR amendment; never silence tasks.
