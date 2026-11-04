# M6-001 — 성능 기준선 SDD

- 상태: 실기기 기준선 측정 준비 및 진행 중. M6 전체 Gate는 두 건의 변경 전후 비교가 필요하며 이 작업에서 완료하지 않는다.
- 측정 대상: 별도 `dev.fanstage.android.benchmark` 패키지, release 설정을 상속하고 non-debuggable·shell profileable, debug 서명. 기준선과 후속 측정은 동일 변형·기기·컴파일 조건을 사용한다. 보통 release는 별도 패키지이므로 비교 시 이 차이를 명시한다. 이 측정용 APK만 profileinstaller 및 합성 화면을 추가한다.
- 방식: AndroidX Macrobenchmark 1.5.0, `CompilationMode.None`, 각 10회. API 34 미만에서는 프레임워크가 컴파일 상태 초기화를 위해 측정용 패키지를 재설치할 수 있다. 기존 앱과 별도 패키지로 격리한다. 앱의 중요한 동작과 release 보안 설정은 바꾸지 않는다. 결과 원본과 Perfetto trace는 개인 기기 정보가 포함될 수 있어 로컬에만 저장한다.
- Cold startup: `StartupTimingMetric`의 TTID. 적절한 fully-drawn 신호를 앱이 내지 않으므로 TTFD는 NOT_RUN. Debug 수치와 비교하지 않는다.
- 500개 스크롤: 측정 전용 Activity에 실제 ChatPanel·PlayerPanel과 고정된 합성 500개를 주입하고 Player playing 확인 후 `FrameTimingMetric`; 화면·fixture는 일반 룸 내비게이션과 동일하지 않다. 500행 입력은 일반 네트워크 서버의 용량이나 실제 LIVE를 뜻하지 않는다. 이 별도 합성 구성의 계측 영향을 결과와 함께 기록한다.
- `scripts/m6-run-baseline.py`는 명시된 물리 기기만 사용하고 측정 패키지의 기존 설치가 있으면 중단한다. 변경 없는 Git SHA와 전후 모델·OS·표시 주사율·배터리·온도·전원 상태, Gradle 종료 코드 및 원본 SHA-256을 로컬에 기록한다. 디바이스 식별자는 저장하지 않는다. 사전 열·배터리·네트워크 조건을 고정하고, 원본에서 임의 outlier를 제거하지 않는다.
- `scripts/m6-summarize.py`는 AndroidX JSON의 iteration별 값을 보존하고 median, nearest-rank p95/p99, 범위·표본 표준편차를 출력한다. 10회 p95/p99는 최대값이므로 tail을 정밀하게 추정하지 못한다. sampled frame 값은 반복별 요약으로 분리하여 frame들을 독립 반복으로 취급하지 않는다.
- 룸 진입부터 첫 프레임, 10/50/100 수신→화면 적용, 재생 동시 프레임, 반복 입장 메모리/GC/소켓·리스너, CPU·발열·배터리 세부 계측은 해당 시나리오와 증거가 확보되기 전 NOT_RUN. ViewModel 상태 변경과 화면 표시 완료를 혼동하지 않는다.
- [Android 공식 Macrobenchmark 문서](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview), [Benchmark 안정 버전](https://developer.android.com/jetpack/androidx/releases/benchmark), [AndroidX 결과 구조](https://android.googlesource.com/platform/frameworks/support/+/e49915ae87445a97d9e3d3915214eae59ce92150/development/ab-benchmarking/app/src/main/kotlin/androidx/abbenchmarking/macrobenchmarking/MacroBenchmarkResultFiles.kt).
