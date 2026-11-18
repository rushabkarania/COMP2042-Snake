# Validation results · arrow-key readiness and clear effects

JDK 25 · Maven 3.9.9 · JavaFX 25.0.1 · Linux amd64

Build/test command: `./mvnw -Dsnake.ui.tests=true package` (with virtual display and window manager).

| Test class | Cases | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| com.comp2042.CoinImageTest | 1 | 0 | 0 | 0 |
| com.comp2042.CoinPlacementTest | 2 | 0 | 0 | 0 |
| com.comp2042.CoinSequenceTest | 2 | 0 | 0 | 0 |
| com.comp2042.CoinSoundTest | 5 | 0 | 0 | 0 |
| com.comp2042.GameControllerTest | 20 | 0 | 0 | 0 |
| com.comp2042.GameResetTest | 3 | 0 | 0 | 0 |
| com.comp2042.GameSessionTest | 15 | 0 | 0 | 0 |
| com.comp2042.GardenUiTest | 1 | 0 | 0 | 0 |
| com.comp2042.HighScoreStoreTest | 4 | 0 | 0 | 0 |
| com.comp2042.MovementTest | 4 | 0 | 0 | 0 |
| com.comp2042.ObstacleSpawningTest | 15 | 0 | 0 | 0 |
| com.comp2042.PowerUpStateTest | 2 | 0 | 0 | 0 |
| com.comp2042.ProgressionTest | 4 | 0 | 0 | 0 |
| com.comp2042.SnakeBodyPainterTest | 4 | 0 | 0 | 0 |
| com.comp2042.SnakeCollisionTest | 39 | 0 | 0 | 0 |
| com.comp2042.SnakeSpriteSelectorTest | 37 | 0 | 0 | 0 |

**Total: 158 checks; zero failures/errors/skips. Build succeeded.**

The latest menu enlarges the existing sprite snake, clips its tail outside the left edge, faces it right, aligns visible sprite edges directly above the taller checkerboard strip, and clips the panel to rounded corners. Score/best remain above. Native screenshots and the illustration/grass geometry check verify the placement, including the existing small-window game-over checks. Gameplay rules and the normal gameplay-renderer transform remain unchanged.

Hedge preview/gate/route checks, immediate-input/rapid-turn checks, panel-free recovery, persistence, collision and sprite-continuity regressions remain. The wall-collision fixtures disable random follow-up powers so an unrelated blue/wrap pickup cannot mask an intended test collision; the production game still enables all power-ups.

Native fullscreen was 1280 × 1100, and the short outer window was 560 × 700. The native scenario uses scripted score/layout fixtures. These checks do not establish zero Windows hardware/OS/display latency or audible audio on this virtual display.

## Visual Paradigm design update

The final source-aligned UML model was saved and reopened in Visual Paradigm 18.1. Design.pdf contains three native landscape A3 exports, with all 25 production classes plus the nested turn buffer and rendering interface. No attributes or operations are included. Native model integrity and PDF class-name coverage passed. See Design.md for scope, notation and the evaluation-export watermark limitation. This documentation update does not change game code.

## Arrow-key readiness update

158 checks passed again with native UI enabled. The scenario checks Play and Play again remain READY, the arrow prompt is visible, the first arrow dismisses it immediately, recovery retains its arrow-only wait, and existing pause/reset/fullscreen/small-window checks still pass. The injected clock advances 50 movement intervals while READY and verifies no movement, including after Enter/Space/P; it then verifies the first arrow moves within its handler and rapid queued turns still execute on consecutive boundaries. Native screenshots cover the ready prompt, revised help, recovery hint and step-based passage countdown.

## Windows validation - 18 November 2026

- Commit tested: 42dd918.
- Command: `.\mvnw.cmd clean test`
- Result: BUILD SUCCESS.
- Tests reported: 158; passed: 157; skipped: 1; failures: 0; errors: 0.
- The optional GardenUiTest was skipped because native UI testing was not enabled.
- This run verifies the automated tests that executed; it does not establish a native UI test pass on Windows.
