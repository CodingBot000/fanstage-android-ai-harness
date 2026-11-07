# Performance Measurement · 실기기 계측

이 저장소는 AndroidX Macrobenchmark와 Perfetto 기반의 **기준선 수집·가설 비교·회귀 판단 과정**을 포함합니다. 측정 당시 버전과 현재 최신 UI가 다르므로 과거 수치를 현재 앱의 성능 성과로 일반화하지 않습니다.

## 기준선 예시

2026-11-05, **Galaxy S10+ 계열 SM-G975N / Android 12 / 60Hz**, 기준 소스 **66fa029**, benchmark 전용 non-debuggable·profileable 변형, 각 시나리오 10회 실행.

| 지표 | 반복 결과의 중앙값 | 조건 |
|---|---:|---|
| Cold startup TTID | **402.84 ms** | 앱 프로세스 cold start, OS 파일 캐시 별도 초기화 없음 |
| 500행 스크롤 frameDurationCpuMs | **7.25 ms** | 합성 화면·로컬 VOD 재생 중 측정 |
| 500행 스크롤 frameOverrunMs | **−7.82 ms** | 각 반복의 요약을 10회 다시 집계 |

검증은 수치만 보는 방식이 아니라 **대상 앱/테스트 APK, 기기·OS, 커밋, 빌드 변형, 반복 조건을 고정하고 Perfetto로 증상을 비교**하는 방식입니다.

## 실험에서 적용한 판단 원칙

- 단위 메서드 수용 시간과 실제 화면 렌더링 완료 시각을 구별합니다.
- 개선 후보 A(채팅 이벤트 모델 변환)와 B(Fake repository 스케줄링)를 독립적으로 비교했습니다.
- 전체 성능 개선을 재현했다고 확인되지 않은 변경은 제품 코드에 유지하지 않았습니다. **현재 앱의 성능 향상률로 기재하지 않습니다.**
- 이전 UI·벤치마크 결과를 리팩토링 이후 현재 UI 결과로 재사용하지 않습니다.

측정 구현은 [macrobenchmark 모듈](../android/macrobenchmark/)과 [계측·분석 스크립트](../scripts/)에서 확인할 수 있습니다. 원본 Perfetto trace는 장치 식별 정보 노출 가능성 때문에 이 공개 저장소에 포함하지 않았으며, 위 측정값은 해당 과거 리비전의 기록입니다.
