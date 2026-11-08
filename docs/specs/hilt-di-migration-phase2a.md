# Hilt DI Migration — Phase 2A

상태: 구현 및 정적 게이트 PASS; 기기 검증 NOT_RUN. 기준: Phase 1 커밋 1c234c6.

Discover, Community, Media, Messages ViewModel을 생성자 주입과 `@HiltViewModel`로 전환한다. 네 ViewModel은 기존과 같이 Activity의 ViewModelStore에 속하고 NavHost 바깥에서 생성해 탭·상세 화면 간 공유 및 재생성 시 보존한다. Repository 계약과 공개 상태·행동을 유지하며 Chat 및 Media3 소유권은 변경하지 않는다. 기존 AppContainer의 일반 ViewModel Factory는 제거한다. JVM 단위 테스트 및 전체 Phase 1과 같은 빌드·lint·경계·manifest 게이트, 가용 기기 UI 및 재생성 검증을 수행한다. 기기가 없으면 NOT_RUN으로 기록한다. AndroidX Hilt 1.3.0 Compose API는 https://developer.android.com/jetpack/androidx/releases/hilt 참조.

Open Questions: 없음. 기기 계측은 가용 기기가 없으면 미완료로 남긴다.

## 결과 (2026-11-08)

네 ViewModel은 NavHost 밖의 Activity 소유자로 유지한다. 기존 AppContainer와 Factory를 제거했다. 무상태 Discover Fake는 각 ViewModel에 별도 인스턴스, 가변 Community/Messages Fake는 각각 ViewModel 수명에 속한다. Chat/Player는 변경하지 않았다. JDK 21·로컬 SDK에서 Phase 1 SDD의 전체 Gradle 명령 PASS (819 tasks), 모듈 경계·manifest·benchmark 서명·diff 검사 PASS. 기기 미연결로 UI·Activity 재생성 NOT_RUN.
