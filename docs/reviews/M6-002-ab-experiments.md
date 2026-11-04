# M6-002 — 실기기 A/B 실험 기록

## 범위와 판정

동일 SM-G975N / Android 12 / 60 Hz, AC 전원·배터리 100%, non-debuggable/profileable benchmark, `CompilationMode.None`, 각 10회. 동일한 test APK SHA-256 `f65d301b8467c6786037c8a42d5d695d74872331a8162976bd93b2d55bc4aecb`. 배경 작업·OS 캐시 등은 통제하지 못했다. n=10의 nearest-rank p95/p99는 최대값이며 모든 반복을 보존했다. 실제 실험에 사용된 소스 SHA와 APK 해시는 아래 로컬 manifest에 있다.

| 실험 | 변경 전 → 변경 후 | 관찰된 변화 | 판정 |
|---|---|---|---|
| A: 500개 수신 이벤트의 동기 emit→ViewModel 수용 구간, 반복별 평균의 중앙값 | 0.585614 → 0.504111 ms | −13.92% | 수용 시간은 감소했으나 프레임 개선은 확인되지 않았다. 초기 100개 평균 중앙값은 0.434756→0.464862 ms (+6.92%, 범위 중첩), 마지막 100개는 0.682356→0.511180 ms (−25.09%). |
| A: 재생 중 정적 스크롤 프레임 CPU 중앙값의 중앙값 | 7.4097 → 7.2036 ms | −2.78% | 정적 화면은 수신이 없어 해당 변경 효과라고 볼 수 없다. 일시정지 대조군은 7.8409→12.1808 ms (+55.35%)였고 앞선 재시험에서도 11.5914 ms였다. 원인은 미확정이며 회귀 가능성을 배제할 수 없어 A를 되돌렸다. |
| B: cold startup TTID 중앙값 | 523.08 → 567.00 / 재시험 593.15 ms | +8.40% / +13.40% | 두 변경 후 실행 모두 중앙값이 느렸다. 분포가 겹치고 원인은 미확정이다. 유익함을 확인하지 못해 B를 되돌렸다. |
| B: 시작 요청→UIAutomator의 방 목록 텍스트 탐지 proxy 중앙값 | 1,169.36 → 1,419.36 / 재시험 1,687.50 ms | +21.38% / +44.31% | 관찰·폴링 비용이 포함되고 실제 첫 프레임/TTFD가 아니다. 범위가 넓어 수치를 인과관계로 해석하지 않는다. |

A 수신 부하의 측정 구간에서 대상 앱 스레드 scheduler CPU 시간 중앙값은 7.475→7.387초 (−1.18%), 구간 길이는 10.454→10.479초였다. 기기 전체 CPU 비율이나 에너지 소비가 아니다. 양수 frame-overrun 비율의 반복별 중앙값은 89.33%→94.84%였다. 반복별 분포는 변경 전 77.51–96.77%, 변경 후 58.12–96.92%로 불안정하며 화면 프레임 개선을 주장하지 않는다. Player 재생/일시정지 대조군 양쪽에서 Buffer Stuffing이 발생했다. 초기 트레이스의 지연 구간에서는 Compose 재구성이 거의 없었으며, Player 또는 특정 앱 코드의 원인은 확정되지 않았다.

## 가설과 변경

- A: `ChatViewModel.publish`가 표시 최대 500개인데 확인 메시지 최대 1,000개를 모두 화면 모델로 만든 뒤 잘라내는 점을 관찰했다. 먼저 뒤쪽 500개만 변환하고 반복자 할당을 줄였다. 두 변형의 별도 측정은 보존하며 확인/Pending의 순서와 한도를 추가 단위 테스트로 확인했다. 일시정지 대조군의 반복적 악화로 두 변경을 모두 되돌렸다. 동기 emit trace는 서버 지연 또는 실제 픽셀 표시 시간이 아니다.
- B: FakeRoomRepository의 무조건적인 `yield()`가 불필요한 scheduler turn을 만든다는 가설이다. 변경 전 즉시 완료 테스트는 실패했고 `yield()` 제거 후 통과했다. 이 코드는 별도 작업 공간에서 A 없이 측정했다. TTID와 콘텐츠 탐지 양쪽의 중앙값이 느려져 제품 변경을 되돌렸다. 지연의 정확한 원인은 확정하지 않았다.

## 원본과 재현

`ANDROID_SERIAL=<selected physical device> python3 scripts/m6-run-baseline.py --classes <comma-separated test classes>`; 기기와 기존 측정 패키지 설치 상태, 깨끗한 source SHA 확인 후 실행한다. `python3 scripts/m6-summarize.py <benchmarkData.json> --output <new-dir>`로 반복별 통계를 재생성한다. `perfetto==0.16.0`을 `/tmp/fanstage-m6-tools`에 설치한 뒤 `PYTHONPATH=/tmp/fanstage-m6-tools python3 scripts/m6-analyze-active-traces.py <raw-dir> --output <new.csv>` 및 `PYTHONPATH=/tmp/fanstage-m6-tools python3 scripts/m6-analyze-active-cpu.py <raw-dir> <new.csv>`로 해당 trace를 로컬 분석한다. 결과 JSON·각 10개씩의 Perfetto trace와 각 SHA-256은 아래 로컬 manifest에 보관한다. 원본은 시스템 정보를 포함할 수 있어 공개하지 않는다.

| 실행 | 소스 SHA | 로컬 디렉터리 (`~/Library/Logs/FanStage/M6/`) | manifest SHA-256 |
|---|---|---|---|
| 수신/시작 기준 | `30c8068312d05942c38170ebac0e77cdac3f61c2` | `20261105-014714` | `6b0b2be40d59050c10cc7a41993b318ff7e77530afbf13fb888ba51b0982ef5f` |
| 재생/일시정지 기준 | `30c8068312d05942c38170ebac0e77cdac3f61c2` | `20261105-015351` | `ce0a2849218fdda8102b37d1ac8ae449c71d6e64cee435560f1a492abce2778d` |
| A 첫 변형 수신/스크롤 | `1f7401721a5678ba3e5d4a4acc8ba2dc0604ec51` | `20261105-020333` | `97a386db1165b80d2c5a67c2ad294155758afadbbf2d047b7aff8cf9bff07a0c` |
| A 첫 변형 같은 순서 재시험 | `1f7401721a5678ba3e5d4a4acc8ba2dc0604ec51` | `20261105-021245` | `898acfadbb1a1ee1196d970f1ce16015abbe0e609926cb52f2930b8e1881ab80` |
| B 독립 실행 / 재시험 | `7e38fa5f06055877cb1749053421bfa95799572a` | `20261105-022842` / `20261105-024047` | `a3ea675791a58aad9be279bcebb0905057b9febff4fdce632472938798acf877` / `04dae5f856d5250039be80986edf549d32148c19fc55fa7591fac97b76ddd27d` |
| A 보완안 수신/스크롤 및 같은 순서 재시험 | `b3c9c996d89b65802bc37f14be497dff77c5f260` | `20261105-025115` / `20261105-025949` | `720554e486a3c47a3f99bc0b39f0da86a02fb4fe430ee71540dfdf5b97027e32` / `2aa5a51d3f29b9a653ef09452fc806cb1175425d7ac20727f9291773c1f706b9` |

실기기 화면이 잠겨 재기준선 한 차례가 실패했고, 잠금 해제 후 성공했다. 두 변경을 되돌린 뒤의 추가 실기기 재측정은 요청에 따라 중단되어 0/2 실행 상태이며 **NOT_RUN**이다. 로컬 로그 `~/Library/Logs/FanStage/M6/20261105-030830/gradle.log`는 부분 기록으로만 보관한다.

## 남은 검증

되돌림 후 실기기 성능 재측정, 전체 회귀·PR CI 및 리뷰는 별도 결과를 확인해야 한다. M6 전체는 아직 VERIFIED가 아니며 M7에 진입하지 않는다. TTFD, 실제 원격 LIVE, 실제 LMK 및 실기기 네트워크 전환은 기존 NOT_RUN을 유지한다.
