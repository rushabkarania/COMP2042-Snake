# Testing Record

## Direction validation — 4 November 2026

Branch: `fix/direction-validation`

Implementation:
`src/main/java/com/comp2042/GameController.java`

Tests:
`src/test/java/com/comp2042/GameControllerTest.java`

### Purpose

Prevent direct reversal into the snake's body while allowing
perpendicular turns and continued movement in the same direction.

### Automated checks

20 parameterised cases:

- Four opposite-direction requests are rejected.
- Eight perpendicular turns are accepted.
- Four same-direction requests are accepted.
- Four non-arrow keys are rejected as movement directions.

Command: `.\mvnw.cmd test`

Before the fix:
20 tests ran, with two failures and no errors.
Up-to-down was incorrectly accepted, and down-to-down was rejected.

Change:
The DOWN case now checks that the current direction is not UP.

After the fix:
Maven reported BUILD SUCCESS.

Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.131 s -- in com.comp2042.GameControllerTest

### Manual checks

- Passed: rapid up/down inputs no longer cause the previously
  observed reversal into the snake's body.
- Passed: left/right reversal checks during gameplay.
- Passed: pause stops movement and an arrow key resumes gameplay.
- Passed: reset restarts the game successfully.

### Additional observations

- A visible gap between snake segments was captured in a screenshot
  at a bend on the game-over screen. Cause not yet established.
- Top-to-bottom border wrapping was observed. The existing blue-coin
  power-up enables this behaviour; whether it was active during that
  particular occurrence was not recorded.

### Limitations

These unit tests verify the direction-validation method.
They do not verify keyboard-event timing, rendering or collision
handling during actual gameplay.


![Visible gap at a snake bend](images/snake-body-gap.png)

## Snake rendering — 4 November 2026

Branch: `fix/snake-rendering`

### Changes

- Replaced stored corner history with sprite selection based on
  neighbouring snake positions.
- Removed turnArray and turnDir.
- Made the controller's movement direction instance-specific.
- Corrected double application of fullscreen scaling.
- Handled the duplicate head/body position during fatal blocked moves
  when preparing the drawing positions.

### Automated checks

Before renderer integration, all 42 tests passed:
20 direction-validation cases and 22 sprite-selection cases.

After renderer integration, `.\mvnw.cmd test` completed successfully:
42 tests run, 0 failures, 0 errors and 0 skipped.

Manual gameplay testing covered 10 rounds. The previously observed
snake-body gap did not recur during these runs.

`git diff --check` reported no whitespace errors.

### Manual checks

Played 10 rounds after integration. The previously observed body gaps
did not recur. A screenshot captures connected segments through
multiple bends on the game-over screen.

### Remaining verification

Explicit checks of border wrapping, fullscreen scaling, and
Esc → Start are still to be recorded if not covered during these rounds.

### Additional regression tests — 5 November 2026

Moved tail rotation and preparation of drawing positions into
SnakeSpriteSelector so these calculations can be tested directly.

Added 15 test cases covering:
- Tail rotation in all four directions, including wrapped borders.
- Rejection of invalid tail neighbours.
- Removal of the duplicate final head position after a blocked fatal move.
- Preservation of the original body list and interior self-collision positions.
- Living snakes and empty drawing lists.

Command: `.\mvnw.cmd clean test`

Result: BUILD SUCCESS.
57 tests run, 0 failures, 0 errors and 0 skipped:
20 direction-validation cases and 37 rendering-helper cases.

These tests check rendering calculations, not JavaFX visual output.
The earlier 10-round manual gameplay check found no recurrence
of the snake-body gap.

## Fullscreen controls — 5 November 2026

### Problem

The keyboard reported EQUALS when the fullscreen shortcut was pressed,
but the controller only accepted PLUS.

### Change

Fullscreen entry now accepts EQUALS, PLUS and ADD.
Returning to windowed mode accepts MINUS and SUBTRACT.

### Manual verification

- Passed: = enters fullscreen without requiring Shift.
- Passed: - returns to windowed mode.
- Passed: repeated switching between the two modes.
- Snake segments appeared connected in both modes.

Number-pad shortcuts have not been explicitly verified.
Dedicated automated key-mapping tests remain pending.

### Follow-up

Removed the temporary key-event diagnostic output left in PR #5.


## Snake collision state — 5 November 2026

Branch: `fix/snake-collision-state`

### Changes

Movement now checks the destination before changing the snake's
positions. Fatal moves preserve the last valid head, body and direction.
Movement requests after death leave the snake unchanged.

### Automated checks

Command: `.\mvnw.cmd clean test`

96 tests passed, with zero failures, errors or skipped tests:
- 20 direction-validation cases.
- 39 movement and collision cases.
- 37 sprite-selection cases.

### Manual checks

- Self-collision: game over triggered.
- Wall collision: game over triggered.
- Obstacle collision: game over triggered.
- Arrow keys after death: no movement or growth.
- Tab reset: a new round starts successfully.
- Snake remained visually connected in the captured collision cases.

### Remaining issues

- A pale square background is visible around the red coin.
- Obstacles can overlap the game-over title.

These visual issues remain outside this collision fix.