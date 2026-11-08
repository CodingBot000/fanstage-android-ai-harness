# Android 최종 검증 감사 — 2026-11-08

## 범위와 판정

**PARTIAL.** 기준 main은 `4191a1dd1adf9debd95a3cc0c4366943c751f67e`이며, 후속 실행의 당시 HEAD와 커밋 전 변경을 포함한 관련 코드 스냅샷은 [manifest](manifest.json)에 구분해 기록했다. 확인된 미해결 사항은 High 1건, Medium 1건이다. Critical·Low 확정 항목은 없다. 등급은 이 감사의 운영·검증 위험이며 애플리케이션 보안 취약점의 발견을 의미하지 않는다.

- **High — main 병합 차단 정책 부재:** 읽기 전용 GitHub API는 `protected=false`, 필수 검사 `off`, 적용 Ruleset `[]`를 반환했다. 현재 계정은 push 권한을 갖지만 main 직접 push나 실패 PR 병합은 시도하지 않았다. 따라서 특정 시도의 성공 여부를 단정하지 않는다. [관찰 원본 요약](branch-policy.json).
- **Medium — 회전 계측 간헐 실패:** 실기기 선택 회귀 첫 실행은 38/39였고, `fullscreenRestoresAfterActualRotation`에서 일시정지 태그 대기가 시간 초과됐다. 단독 재실행, 두 차례 전체 39/39 재실행, 별도 단독 5회는 통과했다. 최초 원인은 미확정이며 실패를 환경 문제로 단정하거나 통과 결과로 덮어쓰지 않는다. [첫 결과](ui-physical-initial.xml) · [전체 재실행](ui-physical-retry.xml) · [manifest](manifest.json).

## 실행 결과

| 범위 | 실제 결과와 한계 |
|---|---|
| Hilt | API 36 에뮬레이터의 기존 두 테스트 2/2, Activity Hilt ViewModel 경로 추가 후 에뮬레이터·SM-S908N 실기기 각 3/3 PASS. ViewModelStore 종료 처리 후 실기기 3/3 재검증. [원본](hilt-physical-cleanup.xml) |
| Android 회귀 | JDK 17 빌드·Lint·단위 및 모듈·Manifest 검사; 선택 실기기 회귀 최초 38/39, 단독 재시도 1/1 및 전체 재시도 39/39 PASS. Activity 재생성과 실제 회전을 포함한다. 실제 프로세스 종료 후 복원은 이 감사에서 NOT_RUN. |
| 실제 채팅 | SM-S908N에서 일회성 격리 PostgreSQL·WebSocket 서버 및 USB reverse 사용. Android 통합 및 30회 별도 룸 연결에서 Collector 완료, close 호출 및 소켓 참조 해제 검사 2/2 PASS. 서버 측 연결 종료나 OS 소켓 해제 완료는 별도 측정하지 않았다. 서버 동시성·ACK 유실·DB 재시작·연결 단절 테스트 PASS. [Android 원본](real-chat-30-physical.xml) · [서버 출력](backend-regression.json) |
| 자원 반복 | 실기기 Debug 격리 패키지에서 LIVE 30회, Replay 10회, 전체 화면 및 실제 HOME 복귀 각 10회 PASS. 30개의 이전 Player 약한 참조는 최종 관찰에서 0개였으며 PSS·Java heap·Java thread·GC 관찰값은 [측정값](resource-observations.json)에 기록했다. 이는 Fake 채팅 경로이며 모든 Listener·Surface·Coroutine의 누수 부재나 장시간 안정성의 증명이 아니다. 판정 **INCONCLUSIVE**. 실제 WebSocket 30회 테스트는 별도 소켓 필드와 Collector 종료 확인이다. |
| 레거시 DI | 런처는 Discover 경로를 사용한다. `RoomListViewModel`·`FakeRoomRepository`는 직접 런처에서 쓰이지 않지만 기존 단위 테스트와 독립 룸 UI 계측 경로에 연관된다. 테스트를 삭제하며 제거하지 않고 유지한다. `ChatViewModel.Factory`는 명시적 Transport를 사용하는 테스트·벤치마크용이다. |
| CI | Android CI는 일반/Hilt 계측 APK를 각각 컴파일하고 일반 구성을 다시 빌드하지만 원격 기기 실행은 하지 않는다. 서버 workflow는 서버 테스트만 실행하므로 명칭을 서버 범위에 맞게 정정했다. 문서 검사에 모든 Markdown 경로를 추가했다. 보호 정책 변경은 하지 않았다. |

## 성능: 측정 사실과 한계

SM-S908N/API 36, non-debuggable `benchmark`, JDK 17, `CompilationMode.None`, 동일 `ColdStartupBenchmark` 10회씩을 순서대로 실행했다. 도입 전 `0451c02817f2ae48e8f7ae5d898c55984e0cf87e`의 TTID 중앙값은 618.19 ms, nearest-rank p95는 664.35 ms, 표본 표준편차는 32.18 ms였다. 이후 `dd7ef1bbf3fe23c76caa8424c7d8f159cb0e06a5`는 각각 587.42 ms, 609.13 ms, 12.60 ms였다. 테스트 APK 해시는 동일하며 앱 APK는 다르다. 한 번의 고정 순서 실행이고 OS 캐시·백그라운드 상태가 통제되지 않았으며 소스 변경이 Hilt만으로 제한되지 않아 인과적 개선·회귀를 판정하지 않는다. n=10의 nearest-rank p95는 최대값이며 TTFD는 측정하지 않았다. [반복값과 원본 해시](cold-start-observations.json).

각 실행의 원본 Macrobenchmark JSON과 Perfetto trace 10개는 기기 시스템 정보가 포함될 수 있어 비공개 로컬에 보존한다. 공개 JSON은 실제 원본에서 추출한 값과 원본 해시를 담은 **파생 자료**다. 이전 다른 모델 실기기의 M6 기준 원본도 로컬에 존재했다. 과거 manifest 9개와 각 manifest가 가리키는 원본 189개를 해시 검증했고, 10회 반복값의 중앙값·nearest-rank p95를 재계산해 과거 기록과 일치함을 확인했다. [검사 요약](historical-manifest-check.json). 기기가 달라 이번 결과와 개선율을 계산하지 않는다.

## GitHub 정책 권고 — 설정은 변경하지 않음

현재는 어떤 체크도 필수로 강제된다는 근거가 없다. 관리자에게 main 대상 Ruleset/Branch Protection, PR 필수, 관리자 우회 여부, 필수 검사의 출처를 검토하도록 제안한다. 현 상태의 `build-lint-unit`, `PostgreSQL and WebSocket server integration`, `markdown-links-and-adr-ids`는 workflow 경로 필터가 있어 그대로 필수로 지정하면 해당 workflow가 실행되지 않는 PR이 대기 상태에 남을 수 있다. 모든 PR에서 실행되는 안정적인 집계 체크를 먼저 설계하고 경로별 작업의 성공·실패·정당한 미실행을 명시한 뒤 그 체크를 필수화하는 것을 권장한다. [GitHub 필수 검사 문서](https://docs.github.com/en/pull-requests/how-tos/merge-and-close-pull-requests/troubleshooting-required-status-checks).

실제 direct push 및 고의 실패 PR의 병합 차단 실험, 보안 정책 설정 변경은 **NOT_RUN**이다. 기기 CI를 실제 실행할 것인지도 별도 운영 선택이다. Hilt 전용 테스트를 APK 컴파일만 하는 CI와 로컬 기기 실행 결과는 구분한다.

## 독립 재검토

별도 에이전트 세션이 Hilt 범위·테스트·CI·문서와 증거 해시를 읽기 전용으로 재검토했다. 실제 테스트가 각각 2건·9건인데 기존 스크립트가 1건·5건만 기대하던 두 에뮬레이터 검증기의 오류를 발견해 정확한 전체 케이스를 검사하도록 수정했다. 실제 결과에서 추출한 임시 fixture의 통과와 케이스 누락 거부를 각각 실행해 확인했다. 같은 모델 계열이므로 다른 모델의 독립 검토라고 주장하지 않는다. 실제 Activity ViewModel 경로 미검증, CI 명칭과 실제 범위 차이, 문서 기준 SHA와 Manifest 스크립트 디렉터리 오류, 오래된 테스트 주석은 각각 테스트·워크플로·문서·주석에서 보완했다. 새 코드는 이후 재검토했다. 검토는 동시성 및 전체 누수 부재를 보장하지 않는다.

## PR #68 기존 CI와 이번 실행의 구분

[PR #68](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/68)의 기존 [Android CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42478684566), [서버 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42478684559), [문서 CI](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/42478684603)는 당시 커밋의 결과다. 해당 Android CI는 계측 APK를 컴파일했으며 이번 실기기 실행을 의미하지 않는다. 이번 PR의 CI 결과는 별도로 확인한다.

### 추가 성능 범위

이번 전후 정량 비교는 Cold Startup TTID에 한정한다. LIVE 룸 첫 프레임 시간, 실제 채팅 이벤트 지연, 스크롤 Jank의 Hilt 전후 비교와 장시간 메모리·GC 추세는 NOT_RUN이다. 화면 첫 프레임의 존재나 기존 단위 테스트 통과를 이런 지연·성능 측정으로 대체하지 않는다. 성능 개선을 주장하거나 배포 결정을 내리려면 동일 기기에서 반복·순서 교차 측정을 추가한다.
