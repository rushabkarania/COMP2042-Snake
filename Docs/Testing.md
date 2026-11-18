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

## Coin transparency — 5 November 2026

Branch: `fix/coin-transparency`

Updated the blue, gold, rainbow, red and silver coin PNGs to remove
their opaque backgrounds.

Manual verification:
- The red coin displays without the pale square during gameplay.
- Other coin colours remain pending visual verification.

No Java code changed.

## Coin collection audio — 5 November 2026

Branch: `fix/coin-audio`

### Cause and change

The bundled `coinSound.wav` existed, but GameController did not load or
play it. Unused Media/MediaPlayer imports and a sound path remained.

CoinSound now loads the resource through a classpath URL and reuses one
AudioClip per controller. Successful collection triggers playback before
the win check. Tab reset retains this sound object. Missing resources and
synchronous loading/playback failures disable audio with a diagnostic
instead of interrupting gameplay. Asynchronous device failures may still
be reported by the JavaFX media system.

### Automated verification

Five new CoinSoundTest cases cover:
- A readable, non-empty bundled WAV resource.
- One load and one playback request per call, including repeated calls.
- Missing resource handling.
- Loading failure handling.
- Playback failure handling without repeated attempts or diagnostics.

Assistant verification: all 101 tests passed using an ECJ compiler with
Java 17 and JavaFX 17. This was not the project's configured Maven stack.
The tests use replacement playback callbacks; they do not establish
that sound is audible or that controller events fire correctly in-game.

Windows Maven verification: 101 tests passed with zero failures, errors or skips during the coin audio fix.

### Pending manual checks

- Collect several coins: one audible sound for each successful collection.
- Collect available power-up coins: the same collection sound plays.
- Ordinary movement and collisions do not trigger collection audio.
- Tab reset, then collect again: audio still works.
- Escape to the menu, start again and collect: audio still works.
- Check the console for audio errors and confirm gameplay remains responsive.

## Obstacle spawning — 10 November 2026

The first placement fix passed all 106 tests on Windows. In gameplay the silver coin still produced a crowded wave, and obstacles overlapped the game-over label.

The revised generator shuffles available cells and places at most one block in each board quarter, up to four total. It keeps the outer columns, top three rows and bottom two rows clear. The minimum straight-line distance from the head and between blocks is four grid cells. A crowded board can receive fewer blocks. The win target uses the actual count added.

Revised suite: 106 tests passed with Java 17 and JavaFX 17. The revised placement cases cover 100 standard layouts, a rectangular board with an existing block, and a board with no suitable cells. Windows Maven and visual verification of this revision are pending.

Manual check: collect a silver coin, inspect spacing and label clearance in windowed and fullscreen modes, then press Tab and verify the obstacles clear.


## Coin selection — 10 November 2026

Playtesting confirmed the revised obstacle spacing felt right. A separate round reached 130 points without a reported silver coin. Previously silver depended on a random draw and became ineligible while obstacles existed. Rainbow shared the same draw as gold and was unreachable.

CoinSequenceTest checks the contents of 20 consecutive groups and verifies that resetting during a group restarts the silver countdown. The original red coin is placed before the sequence begins. Windows verification of repeat silver and rainbow pickups is pending.

## Repeat silver pickups — 10 November 2026

Playtesting found that a second silver pickup gave points but left the four blocks in the same positions. Later pickups now relocate one block when a safe cell is available. The other three stay in place on a full wave. If relocation is impossible, the existing layout is preserved. The win target changes only for additional blocks.

Two regression cases check repeated relocation and the no-space fallback. Windows gameplay check pending: compare layouts before and after collecting a second silver, then reset with Tab.

## Coin placement and winning — 10 November 2026

Coin placement scans the board once. On a four-cell board with two snake cells and one obstacle, the test selects the only free cell. A second test collects the final coin, checks the awarded points and win target, and confirms the full board has no spawn position.

112 tests passed using Java 17 and JavaFX 17. Windows Maven verification and visual verification of the winning frame are pending.

## Buffered turns and movement interpolation

The progressive-levels branch starts from the merged game. The selectable-difficulty experiment is archived separately. The player reported delayed steering. Inspection found that a second legal turn between movement ticks was discarded.

The local JUnit run passed all 115 tests (112 baseline and three MovementTest cases). They cover rapid turns, repeats, opposite inputs, queue capacity, clearing input, and fractional positions across both wrapped edges. Windows checks pending: tap Up then Right while travelling left, pause and resume, reset during a turn, and cross a boundary with the blue power-up. Watch the head and tail at bends. Input-to-display latency has not been measured.

### Short-snake turning animation

Windows playtesting found smooth straight movement but a disconnected-looking tail during a turn at score zero. Independently translating rotated segment sprites could leave a diagonal gap. GameView now draws a continuous rounded body through the occupied bend, keeping the head sprite and advancing the endpoints along grid edges. MovementTest checks the two-cell snake halfway through a right-angle turn. Windows visual confirmation of this revision is pending.

### Restoring the body artwork

A fullscreen playtest at score 55 showed a plain green line behind the head. The continuous-line renderer bypassed the straight, corner and tail images. GameView now draws the original body and corner sprites on their occupied cells and slides the pointed tail and head along the path. Visual confirmation on Windows is pending.

### Turn rendering follow-up

Windows recording: during rapid alternating turns, the head briefly separates from a corner and the tail changes orientation abruptly. The renderer now joins the animated endpoints along the occupied grid edges and tapers the tail along that path. Manual retest pending: repeat a right/down staircase, then reverse the staircase, in windowed and fullscreen play. Check the neck for background-coloured gaps and the tail for a detached triangle.

### Square tail during straight movement

The Windows screenshot at score 5 exposed a straight, square-cut tail. A fixed sprite could cover the previous tapered layer as the tail crossed its cell. The new body renderer produces one patterned silhouette. SnakeBodyPainterTest inspects the actual body pixels at 33 positions across a straight step, checks continuity through a staircase turn at 33 positions, and checks both sides of a wrapped corner. 119 JUnit tests passed. Windows fullscreen playback and input latency remain unverified.

### Fullscreen body sharpness

The score-35 screenshot showed softened body markings beside a sharp head sprite. The body bitmap had antialiased edges and JavaFX filtered it again during enlargement. Body rasterization now uses hard pixel edges and the body draw disables image smoothing. Manual Windows check: compare the head, markings and tail at window size and fullscreen. Frame rate and fullscreen appearance still require a local playtest.

### Fullscreen tail regression after movement smoothing

Compared the submitted working tree with the committed sprite renderer. The animated build generated a low-resolution body bitmap and uploaded the entire board every pulse. The input queue was separate from this rendering path. Restored the original body and tail artwork using cropped sprite strips along the animated path. Replaced timer rounding on ordinary late pulses with a fixed step boundary.

The pixel checks use the original PNGs to exercise straight movement and staircase turns at 33 positions, plus a wrapped corner. A clock test covers both a 16 ms late pulse and a long stall. Manual Windows verification is pending for fullscreen motion and frame pacing.

### Coin pickup pause

CoinImageTest checks that separate red and gold spawns reuse their respective decoded images. All 121 JUnit tests passed. Windows playtest pending: collect several coins while moving straight and check that the head continues moving through each pickup. The tail stays in place for one movement step during growth.

## Progressive levels and recovery

The full JUnit suite passed locally using Java 21, ECJ and JUnit Console: 125 tests, zero failures or skips. ProgressionTest covers threshold crossings, exhausting lives, reset, patrol freeze and reversal, and a safe first step after respawn. Windows gameplay checks are pending.

Run the Maven suite, then check the HUD at 40 and 80 points. In level 3 collect silver and watch the patrol resume after twelve steps. Hit a wall three times, checking that each collision consumes only one life, that movement waits for input after recovery, and that Tab restores level one and three lives.

Windows playtesting at level 2, score 50/80, showed the lives count truncated to an ellipsis. Level 3 showed Lives 0 at game over. Reduced the windowed HUD font size and spacing. Repeat the level 2 collision check at normal window size to confirm Lives 2 is fully visible.
