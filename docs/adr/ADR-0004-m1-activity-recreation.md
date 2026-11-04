# ADR-0004 — Verify M1 navigation restoration with ActivityScenario recreation

- Date: 2026-11-04
- Status: ACCEPTED FOR M1-005 (post-merge main CI verified; M1 overall Gate remains open)
- Milestone: M1; scope M1-005 only

## Decision

Extend the existing Android API 35 Compose navigation instrumentation smoke with one explicit `ActivityScenario.recreate()` while the detail route is visible. Assert a **different Activity instance** returns, the same detail route remains visible, and back navigation restores the original list. Keep the current `RoomNavigationSmokeTest.roomListOpensDetailAndReturns` name and the existing strict test XML/exit-code validator, so the one-test gate cannot silently report success on zero tests.

## Why and alternatives

The current navigation smoke proves list/detail/back only, not restoration of the Navigation Compose back stack after Activity recreation. An isolated recreation is a deterministic Android lifecycle probe using the existing `createAndroidComposeRule<MainActivity>()` ActivityScenario and requires no external services. It does **not** simulate device orientation changes, low-memory process death, or playback restoration. Those are independent NOT_RUN conditions; no PASS claim is allowed for them.

Alternatives: use `requestedOrientation` on the emulator (separate device configuration test with orientation change verification); use `am force-stop` or process kill (separate process-state/relaunch contract, distinct lifecycle). Neither is implemented in M1-005.

## Evidence required

Run the original workflow on the new PR commit, verify `:app:assembleDebug :app:lintDebug testDebugUnitTest` and the connected instrumentation job. Inspect the raw emulator job log and strict `TEST-*.xml`: exactly 1 test; failures/errors/skips 0; AndroidTest exit code 0. If the recreation fails, do not lower the assertion or override `pipefail`. Preserve original logs/artifacts for the next bounded repair.

## Security/cost

No Android production code, server, API key, or permissions change. CI uses read-only token without `OPENAI_API_KEY`. No Codex generation, no extra paid resources.


## Verification and limits (2026-11-04)

- [PR #16](https://github.com/CodingBot000/fanstage-android-ai-harness/pull/16) squash merged into `main` at `e22bb4042173d6b0160b9d46c50837cbfde6bb56` after PR Code Review (no findings) and [PR Run #41763099229](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41763099229) passed.
- [Post-merge main Run #41768660235](https://github.com/CodingBot000/fanstage-android-ai-harness/actions/runs/41768660235) PASS: build/lint and 7/7 JUnit; API35 emulator actual AndroidTest 1/1, XML failures/errors/skips=0, exit 0. Original job logs, test XML and ZIP SHA-256 verified. This validates Activity recreation/state restoration **only**.
- Explicit rotation, system process death/relaunch, real device install, LIVE and release-like performance remain **NOT_RUN**. Do not infer them from ActivityScenario recreation. M1 Exit Gate stays open pending independent review.
