# Third-party notices and distribution scope

FanStage's [MIT license](LICENSE) applies to the project's original code and documentation. It does **not** relicense included third-party code, tools, assets or user-interface components. Preserve each upstream copyright and applicable license.

## Included Gradle Wrapper

This source repository contains the Gradle-generated scripts and Wrapper JARs under android/ and backend/chat-server/. The Wrapper is licensed under **Apache License 2.0**, with its original copyright notices retained. See the [Apache-2.0 license text](third_party/licenses/Apache-2.0.txt). Wrapper JARs were compared to the published Gradle 9.5.0 SHA-256 values; upstream notices within the JARs are preserved.

## Project media and images

The Android synthetic HLS VOD and test media are generated with FFmpeg testsrc2; see the [playback specification](docs/specs/media3-player.md). FanStage illustrations are fictional/generated assets. Portfolio captures show FanStage running on Android hardware or emulator; embedded system/keyboard/UI fonts and any separately supplied video remain subject to their own rights. The current physical-device MP4 demo uses media the project owner confirmed was authorized for public display; the original supplied MP4 files are not committed here.

## Dependencies resolved during build

Android and server dependencies are downloaded through Gradle and follow their respective upstream licenses. The source repository does not redistribute a public APK, server JAR or container image.

Examples of third-party runtime/test dependencies include AndroidX, Media3, Jetpack Compose, Spring Boot, PostgreSQL JDBC (BSD-2-Clause), Logback (EPL-2.0 / LGPL-2.1), Jakarta components with separately declared licenses and Haze (Apache-2.0, copyright © 2024 Chris Banes). See the upstream project notices and license texts for each dependency. Some components have dual-license choices.

**When distributing binaries:** Resolve actual dependency artifacts and embedded licenses/NOTICE entries for the exact build variant; preserve required copyright and notices, evaluate transitive dependencies, embedded media and container/JRE layers separately. Existing source-level review is not a substitute for checking the packaged distribution.
