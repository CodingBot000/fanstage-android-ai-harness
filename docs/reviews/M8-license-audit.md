# M8 — 제3자 라이선스·NOTICE 감사

## 상태와 결론

- 조사 기준: 공개 GitHub `main` `b92521b218f2b96edea5ec4232f9005954c8242c` (2026-11-05). 소스 저장소는 이미 공개되어 있으며 v0.1에서 공개 APK 또는 서버 바이너리를 제공하지 않는다. 기존 로컬 작업 트리는 보존하고 별도 worktree에서 조사했다.
- 사용자는 이번 대화에서 **직접 작성한 프로젝트 코드·문서에 MIT**를 선택했고 저작권 표기는 **2026 FanStage contributors**로 결정했다. `LICENSE`와 [제3자 고지](../../THIRD_PARTY_NOTICES.md)를 추가했다. 각 제3자 저작물의 원래 조건은 유지한다.
- **M8 Gate 미완료:** 기존에 공개된 시연 MP4의 Google 키보드 구간은 현재 파일에서 제외했지만 기존 Git 이력에 남으며, 포트폴리오에서 해당 UI를 캡처·공개하는 구체적 권한은 확인하지 못했다. 외부 독립 개발자의 소스 재현도 NOT_RUN이다. 이 감사는 위 사항과 전체 코드의 저작권 출처를 모두 해결했다고 주장하지 않는다.

## 현재 공개 소스에 포함된 자산

| 대상 | 조사 결과 | 조치·한계 |
|---|---|---|
| Gradle Wrapper 스크립트 4개, JAR 2개 | 스크립트에 원저작권과 Apache-2.0 헤더. 두 JAR의 `META-INF/LICENSE`에 Apache-2.0 전문이 있고 별도 NOTICE 항목은 발견하지 못했다. JAR SHA-256 `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`은 [Gradle 9.5.0 공식 값](https://gradle.org/release-checksums/)과 일치. | [고지](../../THIRD_PARTY_NOTICES.md)와 [Apache-2.0 전문](../../third_party/licenses/Apache-2.0.txt) 추가. 기존 저작권 헤더 보존. 해당 버전에서 생성한 스크립트와 비교 시 3개는 바이트 일치, 서버 `gradlew.bat`는 줄바꿈 정규화 후 일치했다. 줄바꿈만 다른 경우 파일 내 변경 고지가 추가로 필요한지는 미확인이다. 최초 비교 시 잘못된 디렉터리에서 Gradle을 실행해 실패했으며 올바른 임시 디렉터리에서 다시 생성해 확인했다. |
| `android/feature/live/src/main/assets/hls/*.ts`, `vod.m3u8` | FFmpeg `testsrc2`로 생성한 무음 합성 콘텐츠. [M2 생성 명령](../specs/M2-hls-player.md)에 출처 기록. FFmpeg/인코더 소프트웨어 자체는 이 파일에 포함해 배포하는 것으로 확인되지 않았다. | 도구 라이선스와 출력물·코덱 권리 검토를 구분한다. 배포 지역의 특허·인코딩 관련 조건은 이 감사로 확정하지 않았다. [FFmpeg 안내](https://ffmpeg.org/legal.html). |
| `docs/portfolio/media/offline-demo.mp4`·PNG | 격리 API 36 에뮬레이터에서 앱·합성 영상을 캡처. 기존 MP4에는 `com.google.android.inputmethod.latin` 키보드 UI가 일시적으로 나타나며 AVD 기본 입력기로 이를 확인했다. 현재 MP4는 해당 구간을 잘라내고 샘플 프레임과 전환 구간을 시각 검토했다. PNG에는 키보드가 보이지 않는다. | 기존 MP4는 공개 Git 이력에 남는다. Google의 [브랜드 사용 안내](https://about.google/brand-resource-center/guidance/)는 일부 교육 목적 스크린샷 사용을 설명하지만 본 포트폴리오 목적의 해당 UI 사용 권한은 확정하지 못했다. Google 키보드·OS UI·폰트를 프로젝트 MIT로 재라이선스하지 않는다. 다른 OS UI·폰트와 과거 이력의 권리 검토는 미완료이며 무단 사용이라고 단정하지 않는다. |
| 기타 원본 코드·문서 | `git ls-files` 146개와 명시적 copyright/SPDX/license 표기를 검색했다. Gradle Wrapper 외 명시적으로 확인된 복사 코드 헤더는 없었다. | 헤더 검색만으로 모든 코드·문서의 원저작자 또는 복사 부재를 증명할 수 없다. 기존 기여분에 대한 권리 확인은 별도 검토가 필요하다. |

## Gradle로 해석한 의존성

[상세 TSV](M8-dependency-inventory.tsv)는 아래 각 설정에서 실제 해석된 외부 artifact 좌표·SHA-256·POM 라이선스 표기·라이선스가 선언된 POM·아카이브 직접 엔트리의 LICENSE/NOTICE 경로를 보존한다. 같은 artifact가 여러 설정에 들어가 중복될 수 있으며 총 482행, 고유 좌표 190개다. POM에 표기가 없는 행은 0개였지만, 이는 모든 라이선스 조건과 재배포 의무를 완전히 검증했다는 뜻이 아니다.

| 범위 | 설정 | artifact 수 | 조사 결과 |
|---|---|---:|---|
| Android 실행 | `:app:releaseRuntimeClasspath` | 82 | POM의 선언은 모두 Apache-2.0 계열. 아카이브 직접 엔트리에 LICENSE/NOTICE 계열 파일이 있는 artifact 48개. |
| Android Debug 실행 | `:app:debugRuntimeClasspath` | 83 | POM의 선언은 Apache-2.0 계열. 직접 엔트리의 LICENSE/NOTICE 계열 파일이 있는 artifact 49개. |
| Android 테스트 | `:app:debugAndroidTestRuntimeClasspath` / `:app:debugUnitTestRuntimeClasspath` | 87 / 83 | Apache 계열 외 EPL-1.0·BSD 표기 포함. 테스트 도구는 APK 실행 의존성과 구분한다. |
| 서버 실행 | `runtimeClasspath` | 59 | Apache 계열 외 Logback EPL-2.0/LGPL-2.1, Jakarta Annotations EPL-2.0/GPL-2.0 with Classpath Exception, PostgreSQL JDBC BSD-2-Clause, SLF4J 및 Checker MIT 표기. 53개 아카이브에서 직접 엔트리의 LICENSE/NOTICE 계열 파일 확인. |
| 서버 테스트 | `testRuntimeClasspath` | 88 | 위 실행 의존성 외 EPL-2.0, EDL-1.0, BSD-3-Clause 및 MIT 등 테스트 의존성 표기. |

의존성 목록은 라이선스가 없으면 부모 POM을 따라 확인했다. POM의 두 표기만으로 선택 관계를 추정하지 않았고, [Logback 원저작자의 이중 라이선스 설명](https://logback.qos.ch/license.html), Jakarta artifact의 `META-INF/LICENSE.md`·`NOTICE.md`, [PostgreSQL JDBC 원문](https://jdbc.postgresql.org/license/)을 별도로 확인했다. 예를 들어 서버 artifact에는 Apache Tomcat·Jackson·Spring·Spring Boot 등의 NOTICE, Jakarta NOTICE, PostgreSQL JDBC의 BSD 전문 및 그 JAR 내부 추가 구성요소의 라이선스가 있다. 아카이브 직접 엔트리 이름 검사만으로 AAR 내부 중첩 JAR, 빌드 플러그인, 모든 번들 구성요소까지 조사한 것은 아니다.

Gradle 플러그인/배포판, 별도 실행 Python 도구(Perfetto 포함), GitHub Actions, Docker build/runtime의 Eclipse Temurin JRE·OS 계층과 `postgres:17-alpine` 이미지의 전체 구성요소는 이번 설정별 inventory 밖이다. 저장소는 해당 도구·이미지에 대한 참조와 설정을 제공하며, 실제 도구나 이미지를 이 소스 저장소에 포함했다고 취급하지 않는다. 각 이미지나 도구를 별도로 재배포한다면 해당 최종 산출물과 공급자 조건을 조사해야 한다.

## 소스와 향후 바이너리 배포의 차이

- **현재 공개 소스:** 저장소에 실제 포함된 Gradle Wrapper는 Apache-2.0 원문·기존 저작권 표시를 함께 보존하고, 수정된 파일은 변경 표시를 한다. MIT는 프로젝트 직접 작성분의 사용 조건이며 Wrapper나 제3자 매체의 조건을 대체하지 않는다. 선언된 외부 의존성의 다운로드와 소스 저장소에 그 바이너리를 묶어 재배포하는 행위를 구분한다.
- **APK 배포 시:** 출시 변형에 실제로 포함된 AAR/JAR 및 중첩 라이브러리를 다시 수집하고 라이선스 원문·저작권 및 해당 NOTICE의 적용 여부를 확인해 이용자가 읽을 수 있는 고지를 제공해야 한다. 현재 공개 APK는 없다. 지금의 POM 목록이나 로컬 검증용 RC만으로 출시 APK 고지 완료를 주장하지 않는다.
- **서버 실행 파일/이미지 배포 시:** 실제 boot JAR의 내장 라이브러리와 컨테이너 계층을 기준으로 모든 관련 원문·NOTICE·저작권을 확인한다. Apache-2.0의 라이선스 사본·기존 고지 보존·변경 표시와 NOTICE 조건은 [Apache 원문 §4](https://www.apache.org/licenses/LICENSE-2.0.html)를 따른다. BSD/MIT의 저작권·조건·면책 고지, EPL/LGPL 및 GPL Classpath Exception 등 선택 가능한 조건과 소스 제공·변경 여부는 해당 배포 구성에 맞춰 별도로 검토한다. dual license 표기를 본 프로젝트의 MIT 선택으로 대체하지 않는다.

## 재현·한계 및 다음 행동

- 실행: Android `releaseRuntimeClasspath`, `debugRuntimeClasspath`, `debugAndroidTestRuntimeClasspath`, `debugUnitTestRuntimeClasspath`, 서버 `runtimeClasspath`, `testRuntimeClasspath`에 대해 Gradle `artifactView`로 외부 artifact를 해석하고 각 아카이브 직접 엔트리의 법적 고지 경로를 검색했다. 별도 dependency report도 조사했다. 목록의 라이선스 메타데이터 POM URL 184개는 HEAD 요청으로 응답 200을 확인했다. 테스트·장애·성능 시나리오는 재실행하지 않았다. 임시 원본 `/tmp/fanstage-m8-*-artifacts.tsv`, `/tmp/fanstage-m8-license-inventory.json`은 로컬 파일이며 공개 산출물은 경로·사용자명을 제외한 TSV다.
- 직접 다운로드되는 의존성의 POM 표기와 실제 최종 APK/JAR·이미지에 포함된 모든 텍스트의 법적 적합성은 동일하지 않다. 저작권 귀속·dual license 선택 및 상업적 배포 판단은 관련 원문과 구체적 배포물을 기준으로 추가 검토가 필요하다.
- M8 종료 전 남은 항목: 기존 Git 이력의 Google 키보드 및 기타 에뮬레이터 UI·폰트의 영상 사용 권한 확인, 외부 독립 개발자 재현, 소스의 제3자 출처를 최종 검토한다. 향후 배포용 APK·서버 바이너리·이미지의 NOTICE 전수는 해당 배포를 실제로 추진할 때 별도 선행 조건이다.
- PR #30 한정 WebSocket 타임아웃 예외와 M6 성능 개선 미입증은 그대로 유지한다. M8 전체 VERIFIED를 선언하지 않는다.
