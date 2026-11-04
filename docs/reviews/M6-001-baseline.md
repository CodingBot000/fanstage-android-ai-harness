# M6-001 — 실기기 성능 기준선

## 조건과 증거

- 2026-11-05 KST, Samsung Galaxy S10+ 계열 SM-G975N, Android 12/API 31, 약 60 Hz. 사용자가 구형·저속 기기라고 설명했다. 다른 기기와 일반 성능을 대표하지 않는다.
- 소스 `66fa029497fac181caf54818c19ab38238260925`, 버전 0.1.0, JDK 17, Gradle 9.5.0, AndroidX Macrobenchmark 1.5.0. `benchmark` 변형은 release 설정을 상속하고 non-debuggable·shell profileable, 로컬 서명, 고유 applicationId, benchmark 전용 ProfileInstaller 및 fixture Activity가 추가된다. 이는 일반 release와 동일 APK가 아니다. 두 APK SHA-256은 실행 후 로컬 빌드 산출물에서 확인했다: target `c03d09f492dac449f2e4a8e5cd6383df3ed4bd240c85f876e37c77c6f5dcc2e6`, test `87d3db843a0c8df6985c5d50963a8e9a0e3eb469b5db51f066814c0cec1b5dd2`.
- 두 시나리오 모두 `CompilationMode.None`, 각 10회, 실행 중 추가 ADB 진단 조회 없음. AC 전원 유지, 배터리 64→70%, 배터리 온도 32.7→33.4 °C; 배터리 충전과 기기 온도는 완전히 고정되지 않았다. UI fixture는 합성 메시지·로컬 VOD만 사용하며 서버 요청은 없다. 다른 앱의 백그라운드 상태 및 실제 네트워크 연결 상태는 기록하지 못했다.
- 원본 JSON SHA-256 `b769c91b345b5026cafd84dcca12463bfd78a1069c0013fe035c68f9b4c2c01c`; 20개 Perfetto trace 및 개별 SHA-256을 포함한 로컬 `~/Library/Logs/FanStage/M6/20261105-001656/manifest.json` SHA-256 `1dc9ef9093a8e9480c15df73ec22ed5410316100bbd3ed69d2fdede8e08b24bd`. 원본은 개인 기기 정보가 포함될 수 있으므로 저장소에 올리지 않는다. 분석 CSV와 세부 절차는 [build-log](../../harness/build-log.md) 참고.

## 결과

| 시나리오 | 측정 구간 | n | median | p95 | 범위 | 표본 표준편차 |
|---|---|---:|---:|---:|---:|---:|
| Cold startup TTID | 앱 프로세스 cold 시작→시스템 첫 표시 | 10 | 402.84 ms | 522.16 ms | 378.38–522.16 ms | 42.56 ms |
| 500행 스크롤 `frameDurationCpuMs` | 실제 이동이 검증된 스크롤, Player playing | 10 | 7.25 ms | 8.35 ms | 6.90–8.35 ms | 0.40 ms |
| 500행 스크롤 `frameOverrunMs` | 위와 같음 | 10 | −7.82 ms | −5.53 ms | −8.13–−5.53 ms | 0.84 ms |

Perfetto scheduler에서 측정 구간에 겹치는 대상 앱 스레드 CPU 시간은 반복별 중앙값 4.56초(범위 4.38–5.06초), 구간 경과 시간 중앙값 10.65초(10.60–11.26초), 평균 0.427 CPU 코어 상당(0.413–0.450)이었다. 기기 전체 CPU 사용률이나 전력 소비가 아니다.

스크롤의 숫자는 **각 반복의 프레임 중앙값을 다시 10회 요약한 값**이다. 각 반복의 프레임 p95를 다시 요약하면 CPU 시간 중앙값 10.03 ms, 가장 큰 반복 p95 11.99 ms; overrun 중앙값 −4.83 ms, 가장 큰 반복 p95 +1.62 ms다. 반복당 프레임 수는 374–394개였다. 각 반복의 양수 overrun 비율은 36.34%, 0.26%, 0.53%, 0.26%, 0.26%, 0.25%, 0%, 0%, 0%, 32.35%로 불균일하다. 프레임들을 독립 실험으로 합치지 않는다. p95/p99는 nearest-rank 방식이며 반복이 10회일 때 둘 다 최대값이어서 tail 추정이 거칠다. 원본 값은 제거하지 않았다.

초기 실행의 스크롤은 실제 이동이 검증되지 않았고 반복별 프레임 수가 10~222개여서 **INVALID**다. 진단용 ADB 조회와 더 큰 온도 변화도 있었으므로 초기 실행과 위 실행을 개선 전후로 비교하지 않는다. 두 실행 모두 최적화 전이다.

## 후속 검증 후보 — 원인과 개선 효과는 미확정

1. **동영상 재생 중 채팅 프레임 전달 경로:** 유효 스크롤의 첫째·열째 반복에서 양수 overrun이 각각 141/388, 121/374 프레임으로 집중됐다. Perfetto FrameTimeline에서 해당 반복의 `Buffer Stuffing`은 측정 구간 기준 각각 137/388, 122/375개였고 나머지 반복은 0~2개였다. FrameTimeline 집계는 `measureBlock`과 겹치는 slice를 대상으로 하며 Macrobenchmark 프레임 경계와 완전히 같지 않을 수 있다. 화면 업데이트·Surface/렌더링 상호작용과 측정 도구 영향을 분리해 원인을 검증한다. 이것만으로 Player나 앱 코드의 원인을 확정하지 않는다. [Perfetto의 FrameTimeline 설명](https://perfetto.dev/docs/data-sources/frametimeline)에 따르면 Buffer Stuffing은 표시 대기 버퍼로 인한 지연 상태이며, SurfaceView 분석에는 제한이 있다.
2. **Cold startup Compose 초기 작업:** 대상 프로세스의 각 startup trace에서 가장 긴 `Compose:recompose`는 32.94–34.64 ms, 중앙값 34.11 ms였다. 동일 trace의 `bindApplication` 최댓값은 반복별 중앙값 91.78 ms, `OpenDexFilesFromOat`는 56.00 ms였다. 구간은 중첩·병렬일 수 있고 TTID에 대한 인과관계가 확인된 것은 아니다. 초기 화면에서 불필요한 작업이 있는지 확인하고 동일 컴파일 조건에서 한 가지 가설씩 검증한다.

## NOT_RUN / 제한

TTFD는 fully-drawn 신호가 없어 NOT_RUN. 실제 룸 클릭→Media3 첫 프레임, 10/50/100 msg/s 수신→UI 적용 지연, 동일 프로세스 반복 룸 진입·이탈의 메모리/GC/socket/listener 추이, 기기 전체 CPU·발열·배터리 소비량, 실제 WebSocket 및 원격 LIVE는 이번 측정에서 실행하지 않았다. 측정 UI의 `Playing`은 실제 첫 프레임 시점의 증거가 아니다. 모델별 비교, 최적화, 개선율 및 M6 전체 VERIFIED는 아직 없다.
