# FanStage 개발 현황

**기준:** 2026-11-08 · Hilt 전환 병합 커밋 [`898dabebf`](https://github.com/CodingBot000/fanstage-android-ai-harness/commit/898dabebff410795791efbb4ffbc3f0146468e94)

## 현재 구현

- **UI:** Discover·Community·Media·Messages·LIVE, Compose 기반 11개 모듈
- **미디어:** Media3 HLS·로컬 MP4, 화질·자막·배속·전체 화면과 플레이어 수명주기
- **채팅:** Fake 및 로컬 WebSocket·PostgreSQL, ACK 재전송·메시지 순서·누락 복구
- **DI:** Hilt 기반 Repository·ViewModel, 일반 ViewModel의 Activity 공유와 채팅의 룸별 수명주기
- **검증:** Android 빌드·단위 테스트, 선택된 실기기 계측, 격리 서버 통합·장애 주입

## 검증 근거와 적용 범위

- **Hilt 이력:** [PR #67](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/67) · [실기기/서버 검증 기록](specs/hilt-di-migration-verification.md)
- **병합 main의 CI:** [Android 빌드·Lint·Unit](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874845) · [서버 통합](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874806) · [문서 검사](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42474874813) — 해당 커밋에서 모두 PASS
- **로컬 기록:** Galaxy S22 Ultra/API 36 선택 일반 계측 39/39, Hilt 바인딩 1/1 PASS. API 36 격리 서버 Android 통합 1/1 PASS
- **명확한 경계:** GitHub Actions의 Android 빌드는 Android 기기 계측 실행을 의미하지 않는다. [CI 워크플로](../.github/workflows/android-ci.yml)에 Hilt 전용 테스트 APK **컴파일**을 추가했으며 실제 계측 결과와 구분한다.

## 남은 확인과 다음 행동

- 실제 백엔드의 실기기 연결, 장시간 Player/Socket 누수, 정량 성능·전 기기 검증은 `NOT_RUN`
- 로컬 계측 원본 JUnit XML/logcat 등 공개 가능한 증거는 저장소에 첨부 여부가 확인되지 않았다. 보존된 로그가 있다면 민감정보 제거 후 테스트명·기기·커밋 SHA와 연결
- GitHub Ruleset 조회에서 등록된 Ruleset은 없었다. Branch Protection 정보는 API 접근 제한으로 **CI 실패 시 merge 차단 여부를 확인하지 못했다**. GitHub Settings에서 별도 점검
- `RoomListViewModel`/독립 룸 목록 경로는 현재 `MainActivity` Navigation에서 직접 사용하지 않는다. 해당 단위 테스트가 있어 삭제하지 않고 별도 코드 정리의 영향 검토 대상으로 둔다
- **다음 검수:** Hilt 전용 CI 컴파일·문서 정합성 보완 PR을 CI 결과까지 확인해 병합 여부 판단

## 개발·검증 기준

- 관련 기능 계약과 [설계 결정](adr/)을 확인한 뒤 변경합니다.
- 검사 선택은 [테스트 매트릭스](test-matrix.md), 실행 결과는 [검증 문서](verification.md)와 해당 PR을 기준으로 합니다.
- Codex 작업 제어·리뷰·검증 절차는 [AI 개발 워크플로](ai-development-workflow.md)를 따릅니다.
