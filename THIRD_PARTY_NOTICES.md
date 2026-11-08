# 제3자 소프트웨어 및 저작물 안내

FanStage가 직접 작성한 코드·문서는 [MIT 라이선스](LICENSE)를 따릅니다. 외부 라이브러리·도구·이미지·시스템 UI에는 각 저작물의 원래 라이선스와 권리 조건이 적용됩니다.

## 저장소에 포함된 Gradle Wrapper

`android/`와 `backend/chat-server/`에는 Gradle Wrapper 스크립트·JAR가 포함됩니다. 해당 파일의 저작권 고지와 **Apache License 2.0** 조건을 유지합니다. [Apache-2.0 라이선스 전문](third_party/licenses/Apache-2.0.txt)을 함께 제공합니다. Wrapper JAR의 SHA-256은 Gradle 9.5.0의 공개 값과 대조한 기록이 있습니다.

## 테스트 영상과 이미지

앱과 계측 테스트용 합성 HLS 영상은 FFmpeg `testsrc2`로 생성했습니다. [Media3 검증 자료](docs/specs/media3-player.md)에 사용 목적을 정리했습니다.

앱의 가상 아티스트 이미지는 생성형 도구로 제작했습니다. 화면 녹화에는 Android 시스템 UI·글꼴 등 별도 권리가 적용될 수 있습니다. 공개된 실기기 MP4 시연에는 프로젝트 사용자가 공개 표시 권한을 확인한 영상 자료를 사용했으며 원본 MP4 파일 자체는 저장소에 포함하지 않습니다.

## 빌드 시 사용하는 의존성

AndroidX·Media3·Jetpack Compose, Spring Boot, PostgreSQL JDBC(BSD-2-Clause), Logback(EPL-2.0 또는 LGPL-2.1), Jakarta 계열 구성요소, Haze(Apache-2.0, © 2024 Chris Banes) 등 외부 의존성은 각 구성요소의 라이선스를 따릅니다. 일부는 복수 라이선스 중 선택 가능한 조건이 있습니다.

현재 저장소는 소스 중심으로 공개하며 APK·서버 JAR·컨테이너 이미지는 공개 배포하지 않습니다. 추후 바이너리를 배포할 경우 실제 포함된 의존성·미디어·NOTICE·저작권 고지와 컨테이너/JRE 구성요소를 해당 배포물 기준으로 다시 확인해야 합니다.
