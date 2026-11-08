# ADR-0004 · Hilt 기반 의존성 주입으로의 점진적 전환

- Status: PHASE_1_IMPLEMENTED / DEVICE_VERIFICATION_NOT_RUN
- Date: 2026-11-08
- Related SDD: `../specs/hilt-di-migration-phase1.md`

## Context

FanStage는 11개 Android Gradle 모듈을 사용하며, 현재 `app/AppContainer.kt`가 Repository 인스턴스를 직접 생성하고 `ViewModelProvider.Factory`를 수동 구성한다. `MainActivity`는 Composable에서 `AppContainer`를 `remember`한다. 모듈/도메인 경계는 이미 분리되어 있으므로 이를 유지한 채 Android 표준 DI인 Dagger Hilt를 도입한다. LIVE 룸 채팅, Media3의 자원 소유권과 기기 회귀를 함께 고려해야 한다.

## Decision

1. 애플리케이션 진입점은 `@HiltAndroidApp Application`, Activity는 `@AndroidEntryPoint`로 구성한다.
2. 현재 Fake Repository 3개 (`DiscoverRepository`, `CommunityRepository`, `MessageRepository`)의 Hilt 바인딩은 우선 `app` 모듈의 `@Module`/`@Provides`/`@InstallIn(SingletonComponent::class)`에서 제공한다. `core:data`에 DI 프레임워크 의존성을 추가하지 않으며, 구현 선택 자체를 제품 기능의 변경으로 간주하지 않는다.
3. **`SingletonComponent`에 설치하는 것과 객체를 `@Singleton`으로 스코프 지정하는 것은 다르다.** 가변 Fake Repository는 우선 unscoped로 구성해 기존 Activity별 생성 의미를 보존한다. 추후 Repository 상태 소유권이 명확해지면 별도 ADR로 scope를 조정한다.
4. `AppContainer`를 Hilt가 주입 가능한 조립 보조 객체로 전환하고 `MainActivity`에 필드 주입한다. 기존 수동 ViewModel Factory는 Phase 1에서 보존한다. 즉 이번 변경은 완성형 Hilt 전환이 아닌 동작 가능한 첫 조각이다.
5. Hilt/KSP를 기본 설정에 도입하되 버전은 실제 문서·빌드 호환성으로 확정한다. 현재 AGP 9.3.3/Kotlin 2.4.21은 Kotlin 공식 문서의 완전 지원 범위와 차이가 있으므로 작업 기록에 검증 결과를 남긴다.
6. `ChatTransport`/WebSocket/`Media3PlayerController`를 전역 스코프로 승격하지 않는다. 기존 리소스 생성·해제/테스트는 유지한다.
7. 이후 Phase 2A/2B에서 ViewModel 전환, Phase 3에서 Hilt 테스트 바인딩/계측 구성과 최종 정리를 수행한다. 모든 Phase를 진행할 계획이지만 각각 독립 검증·승인을 갖는다.

## Options considered

- 수동 DI 유지: 테스트하기 쉽고 의존성 비용이 낮지만 멀티모듈 확장 시 수작업 Factory 관리가 늘어난다.
- 전체 일괄 Hilt 전환: 코드 중복 제거는 빠르지만 채팅 룸·미디어 자원 수명주기와 계측 테스트를 동시에 변경해 실패 원인 분리가 어렵다.
- **선택: 단계적 Hilt 적용**: 일시적인 수동+Hilt 혼용이 생기지만 회귀 위험을 격리하고 원인 추적·리뷰를 쉽게 한다.

## Consequences and tradeoffs

- 장점: DI 그래프를 실행 가능한 앱에 적용하고 Repository 조립을 중앙화하며, 향후 `@HiltViewModel`과 테스트 대체 바인딩으로 확장할 기반을 확보한다.
- 비용/위험: KSP/Hilt 코드 생성에 따른 빌드 비용·플러그인 호환성, 단계 전환 기간의 Factory 중복, Activity 재생성 시 Repository 수명 범위 확인 필요.
- 검증 의무: 모든 빌드 변형, Lint, 단위 테스트, 모듈 경계 검사, 가용한 기기의 UI/재생·채팅 회귀를 확인한다. 실패 또는 미실행은 PASS로 기재하지 않는다.

## Rollback

Hilt 전환의 Stage 1 작업 diff만 되돌려 수동 `AppContainer` 조립으로 복귀할 수 있도록 서버·데이터 계약과 Feature 코드 변경을 금지한다. 실패 시 `main` CI를 통과하지 않은 상태로 병합하지 않는다.

## Sources

- https://dagger.dev/hilt/gradle-setup.html
- https://dagger.dev/dev-guide/ksp.html
- https://kotlinlang.org/docs/ksp-quickstart.html
- https://kotlinlang.org/docs/gradle-configure-project.html
- https://developer.android.com/training/dependency-injection/hilt-android

## Phase 1 실행 결과

Hilt 2.60.1과 KSP 2.3.10으로 Repository를 unscoped 제공하고 Activity의 AppContainer에 주입했다. 순수 JVM core:data 및 기존 Factory, Chat/Player 수명주기는 유지한다. 빌드 및 정적 게이트는 PASS, 연결 기기가 없어 기기 검증은 NOT_RUN. 상세 명령·호환성 위험은 Phase 1 SDD 참조.

## Phase 2A 실행 결과

일반 ViewModel 네 개를 Activity 소유 `@HiltViewModel`로 전환하고 AppContainer의 수동 Factory를 제거했다. Discover Fake는 무상태이며 가변 Fake는 해당 ViewModel 수명에 묶인다. 기기 회귀는 NOT_RUN.

## Phase 2B 실행 결과

ChatViewModel은 Navigation BackStackEntry와 SavedStateHandle을 사용하고 룸별 Transport를 생성한다. 테스트·벤치마크의 명시적 Transport Factory는 보존한다. 기기 회귀는 NOT_RUN.
