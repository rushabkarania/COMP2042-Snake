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