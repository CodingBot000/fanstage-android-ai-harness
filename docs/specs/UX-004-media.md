# UX-004 — LIVE / Media

状態: 進行中。既存 Discover Fake コンテンツと合成 VOD を使い、Media グリッドと LIVE/Replay フィルターを提供する。既存 PlayerPanel と LIVE Chat を再利用し、再生・WebSocket・ACK/復旧契約を変更しない。詳細の表示変更は既存ライフサイクルとテストを維持して行う。

検証: フィルター、LIVE・Replay 移動と戻り、Player 初回描画、既存 LIVE/Chat 回帰。全体 suite・サーバ回帰は統合段階まで NOT_RUN_DEFERRED。

Open Questions: なし。

## 실행 기록
그리드·필터 및 기존 LIVE/Replay 진입을 추가했다. API 36 필터·Replay 첫 프레임/복귀를 포함한 DiscoverTest 6/6과 빌드·lint PASS. [캡처](../evidence/ux-004/media.png). 기존 LIVE 상세는 코어 보존을 위해 아직 재디자인하지 않았으며, 실제 서버 회귀와 전체 suite는 NOT_RUN_DEFERRED다.
- UX-006에서 LIVE Player/Chat 카드 표현을 보완하고 기존 회귀를 실행했다. [통합 SDD](UX-006-integration.md) 참조.
