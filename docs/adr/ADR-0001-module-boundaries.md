# ADR-0001 · Compose Feature / Core Module Boundaries

**Decision: Accepted.** app은 Navigation과 의존성 조립을 담당하고, Discover/Community/Media/Messages/LIVE는 기능별 모듈로 둡니다. 도메인 모델, Fake 데이터, 공통 아트워크와 디자인 시스템만 core로 공유합니다.

**Why:** 기능별 UI 책임을 분리하면서 여러 화면의 동일한 모델·리소스 재사용이 app 역참조를 만들지 않도록 합니다. 모듈 수 증가 자체보다 **경계의 명확성과 변경 영향 검증**을 우선합니다.

**Trade-off:** 11개 모듈의 Gradle 설정과 API 경계 유지 비용이 증가합니다. 단순 전달용 추상화나 feature 간 무분별한 참조는 만들지 않습니다.

**Evidence:** [PR #38](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/38) · [경계 검사](../../scripts/check-ux-module-boundaries.py) · [아키텍처](../architecture.md).
