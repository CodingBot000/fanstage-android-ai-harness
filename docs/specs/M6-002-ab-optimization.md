# M6-002 — 성능 가설 및 전후 검증

- 상태: 진행 중. M6-001은 PR #27로 병합됐고 M6 전체는 두 사례와 회귀 검증이 충족되기 전 미완료다.
- 기준: 실기기 SM-G975N, Android 12/API 31, 60Hz, AC 전원, non-debuggable·shell-profileable benchmark APK, `CompilationMode.None`, 원본 JSON·Perfetto trace 각 10회. OS 캐시 초기화는 하지 않는다. 온도·충전 및 다른 앱 상태 차이는 기록하고 불안정한 분포에서 개선 효과를 확정하지 않는다.
- 기존 기준선 `66fa029497fac181caf54818c19ab38238260925`의 원본 21개(트레이스 20개) 해시 확인 PASS. `58ff2edb8b07c6bf771ef2c51a0f3cbb7c7fd255`와 `android/` 차이 없음. 충전 상태 차이를 고려해 현재 main에서 다시 기준선을 실행한다. 화면 절전 상태에서 실패한 실행은 보존하고 기준선으로 사용하지 않는다.
- A 분석: 기존 스크롤 10회 중 지연이 일부 반복의 마지막 구간에 집중됐고, 해당 구간에서 Compose 재구성은 거의 없었다. Player/Surface, 입력·렌더 및 환경 영향을 분리하기 위해 같은 500행 스크롤의 Player 일시정지 대조군을 추가한다. 정적 스크롤 외에도 500개의 실제 `ChatViewModel` 메시지 처리 구간을 동일 속도의 합성 수신 중 trace하는 보조 시나리오를 사용한다. 테스트 전용 `Trace.beginSection`은 fixture가 동기 emit에 소비하는 시간이며 실제 서버 또는 화면 표시 지연이 아니다. 대상은 1,000 보관·500 표시와 사용자 상태를 바꾸지 않는다.
- A 가설: 기존 `publish`는 표시가 최대 500개인데 1,000개까지의 확인 메시지 전부를 화면 모델로 변환한 뒤 마지막 500개만 선택한다. 수신이 있는 상황에서 불필요한 객체 생성을 줄이면 이벤트 처리 비용이 줄 가능성이 있다. 정적 스크롤 Buffer Stuffing의 원인이라고 단정하지 않으며 정적/동적 양쪽을 전후 측정한다. 현재 확인 메시지와 Pending 순서·중복·상한의 기존 동작을 회귀 테스트한다.
- B 가설: FakeRoomRepository의 무조건적인 `yield()`가 가짜 데이터 준비를 별도 scheduler turn으로 미룬다. 없애면 불필요한 대기를 줄일 가능성이 있지만, TTID와 콘텐츠 표시 시점에 대한 실제 영향은 미확인이다. 앱 주요 기능이나 콘텐츠를 지연시키지 않는다. 냉시작 TTID와 test-process의 UIAutomator `Studio session` 탐지 시간을 함께 측정한다. 후자는 표시 관찰 proxy이며 fully-drawn 또는 실제 프레임 완료가 아니다.
- 기존 측정과 test-only harness가 달라지는 경우 새 baseline을 먼저 확보한다. A/B 변경은 별도 commit으로 만들며 B는 A와 독립된 기준 소스에서 비교한다. 실행 실패·불안정·회귀·인과관계 미확인은 그대로 기록하고 임의 outlier를 제거하지 않는다. 두 개선 모두 성공할 필요는 없다.
- 기존 시나리오의 Play/Pause·리소스 해제, 메시지 순서/ACK/취소 및 Android CI/server CI를 그대로 유지한다. 실기기 네트워크 전환, 원격 LIVE, TTFD, 실제 LMK 및 M7은 수행하지 않는다.
