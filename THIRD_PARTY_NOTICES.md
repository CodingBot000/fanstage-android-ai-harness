# Third-party notices and distribution scope

This file identifies third-party material in the current source repository and records dependencies used to build or run it. It does **not** declare that every dependency or media item is licensed under the project's MIT license. Preserve the original notices and follow each component's own license.

## Material included in this source repository

### Gradle Wrapper

The following Gradle-generated scripts and Wrapper JARs are included in the source tree:

- `android/gradlew`, `android/gradlew.bat`, `android/gradle/wrapper/gradle-wrapper.jar`
- `backend/chat-server/gradlew`, `backend/chat-server/gradlew.bat`, `backend/chat-server/gradle/wrapper/gradle-wrapper.jar`

Copyright © 2015 the original author or authors, as stated in the scripts. The scripts and Wrapper JARs are distributed under the [Apache License, Version 2.0](third_party/licenses/Apache-2.0.txt). Each JAR also contains `META-INF/LICENSE`. Both JARs match the published Gradle 9.5.0 Wrapper JAR SHA-256; the server batch script has LF rather than CRLF line endings. Other checked script content matches a Gradle 9.5.0 generated wrapper. Whether this newline-only difference requires an additional notice in the file remains to be confirmed.

### Synthetic media and UI captures

The `android/feature/live/src/main/assets/hls/` VOD was generated locally with FFmpeg `testsrc2`; its generation command is in the [M2 specification](docs/specs/M2-hls-player.md). The portfolio MP4 and PNG were captured from a test emulator displaying the project demo, not a real artist or broadcast. A previously published revision of the MP4 briefly showed the emulator's `com.google.android.inputmethod.latin` keyboard. That segment has been removed from the current video; the prior revision remains in Git history. Permission and attribution for that third-party interface in this portfolio context have **not** been established; see the [audit](docs/reviews/M8-license-audit.md). Do not infer that the keyboard or other third-party UI elements are relicensed under MIT.

## Dependencies resolved locally, not bundled in the source tree

The [resolved dependency inventory](docs/reviews/M8-dependency-inventory.tsv) lists the audited Android and server runtime/test artifacts, artifact hashes, declared POM licenses, license-bearing POMs, and top-level license/notice entry paths. It is a snapshot of selected Gradle configurations, not an exhaustive bill of materials for every build tool, container image or future distribution. Dependencies retrieved separately by a developer remain subject to their own licenses.

No APK or server binary is presently offered as the v0.1 public release. If distributing either later, regenerate an inventory from the **actual** artifacts and variants, review their full upstream LICENSE/NOTICE and bundled subcomponents, preserve required notices and license texts, and satisfy the applicable license conditions. The server runtime includes Logback (EPL-2.0 or LGPL-2.1), Jakarta Annotations (EPL-2.0 or GPL-2.0 with Classpath Exception), PostgreSQL JDBC (BSD-2-Clause), MIT-licensed SLF4J and other components; these do not inherit the project MIT license. Any future image distribution also needs a separate audit of its JRE, OS and PostgreSQL image layers.
