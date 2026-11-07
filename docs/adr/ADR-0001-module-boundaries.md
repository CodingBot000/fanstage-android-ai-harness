# ADR-0001 · Compose 기능별 모듈 분리

## 결정

`app`은 Navigation과 의존성 조립을 맡고, Discover·Community·Media·Messages·LIVE는 각각 기능 모듈로 분리했습니다. 공통 도메인 모델·Fake 데이터·이미지 매핑·디자인 시스템은 실제 재사용 범위에 따라 `core`에 배치했습니다.

## 선택 이유

기능 화면이 다른 기능의 구현이나 `app`에 직접 의존하지 않도록 구성하면 변경 영향을 확인하기 쉽습니다. 공통 데이터와 아트워크가 여러 화면에서 재사용되어도 의존 방향을 일관되게 유지할 수 있습니다.

## 고려 사항

모듈이 늘어나면 Gradle 설정과 API 경계 관리 비용이 생깁니다. 따라서 추상화를 무조건 추가하기보다 역할이 분명한 공통 책임만 분리하고, [모듈 의존성 검사](../../scripts/check-ux-module-boundaries.py)로 실제 경계를 확인합니다.

**구현·검증:** [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) · [아키텍처](../architecture.md).
