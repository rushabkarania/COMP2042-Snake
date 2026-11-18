# COMP2042 Snake

A JavaFX Snake game maintained and extended for COMP2042 coursework,
based on the supplied starter project.

**Status:** Progressive levels, three lives and a patrolling obstacle are implemented. Windows playtesting of these additions is pending.

## GitHub

- [Project repository](https://github.com/rushabkarania/COMP2042-Snake)
- [Starter repository](https://github.com/kooitt/CW2026)

## Compilation Instructions

### Tested environment

| Component | Version |
| --- | --- |
| Operating system | Windows 11 |
| JDK | OpenJDK 27 |
| Maven | 3.9.9 |
| JavaFX | 25.0.1 |

Maven is provided through the Maven Wrapper. JavaFX dependencies are
configured in `pom.xml`. A separate Maven installation is not required.

### Setup

1. Install a JDK. The project has been tested with OpenJDK 27.
2. Set `JAVA_HOME` to the JDK installation directory and ensure
   `java` is available in the terminal.
3. Open PowerShell in the project directory containing `pom.xml`.
4. Check the environment:

```powershell
java -version
.\mvnw.cmd -version
```

Internet access is required to download Maven and dependencies
during the initial setup.

### Compile

```powershell
.\mvnw.cmd clean compile
```

Successful compilation ends with `BUILD SUCCESS`.

### Run

```powershell
.\mvnw.cmd javafx:run
```

For IntelliJ IDEA, open `pom.xml` as a project or import it as an
existing Maven project.

Verification on another computer is pending.

## Implemented and Working Properly

The following features are inherited from the starter project and
have been checked manually during ordinary gameplay:

| Feature | Observed behaviour |
| --- | --- |
| Main menu | The menu appears and Start launches the game. |
| Basic movement | Arrow keys control the snake. |
| Coin collection | Collecting coins increases the score and snake length. |
| Obstacles | Level 2 introduces up to four obstacles, one per board quarter. Positions vary each round. |
| Game over | A game-over message and score are displayed. |
| Visible reset | Reset restores the displayed score and snake and clears obstacles. |

These checks cover basic behaviour. Known edge cases and defects
are listed below.

### Progressive levels and lives

The HUD shows the level, score, next points threshold and remaining lives. At 40 points the board gains up to four spaced blocks. At 80 points one existing block patrols a horizontal lane. Each level increases base movement speed by one step per second. A silver pickup in Level 2 can relocate one block. In Level 3 it freezes the patrol for twelve movement steps. The original board-fill win condition still applies.

The first two fatal collisions each cost one life. The snake returns to its starting length on a clear lane, keeping the score, obstacles and current coin. Press Left, Up or Down to resume. The third collision ends the run. Tab starts again with three lives.

Game manages lives and score thresholds. GameController handles transitions, Snake selects a safe respawn, Obstacle updates the patrol and GameView displays progress. ProgressionTest covers these rules.

- Direction validation prevents direct reversal into the snake's body.
  Verified with 20 passing JUnit cases and manual gameplay checks.

### Coin spawning

Each round starts with a red coin. With power-ups enabled, the next seven spawns contain four red coins and one each of gold, blue and rainbow in shuffled order. The eighth spawn is silver. This sequence repeats and restarts on Tab. Silver awards five points. In Level 2, later pickups move one existing block within its quarter when a safe replacement cell is available, then fill eligible empty quarters up to four blocks. With power-ups disabled, all coins are red.

## Implemented but Not Working Properly

JavaFX runtime warnings remain documented under Unexpected Problems. The winning frame still needs a visual check on Windows.

## Features Not Implemented

| Planned feature | Status / reason pending |
| --- | --- |
| Saved high scores and play statistics | Persistence has not yet been implemented. |
| Audio settings | Collection audio works. Volume and mute controls remain planned. |

These are development targets rather than completed features.
Any features omitted from the final version will be listed with
the reasons for omission.

## New Java Classes


- `src/main/java/com/comp2042/CoinSound.java`: Loads and reuses the collection sound; handles missing resources and synchronous audio failures.
- `src/test/java/com/comp2042/CoinSoundTest.java`: Checks the WAV resource, repeat playback requests and failure handling without audio hardware.

- `src/test/java/com/comp2042/GameControllerTest.java`:
  Parameterised JUnit tests covering opposite directions, perpendicular
  turns, repeated directions and non-arrow keys.

- `SnakeSpriteSelector` selects sprites from neighbouring snake positions.
- `CoinSequence` controls the order of coin types across pickups.
- Regression tests include `SnakeSpriteSelectorTest`, `SnakeCollisionTest`, `GameResetTest`, `ObstacleSpawningTest`, `CoinSequenceTest` and `CoinPlacementTest` in `src/test/java/com/comp2042/`. Production classes are in `src/main/java/com/comp2042/`.

## Modified Java Classes

- `src/main/java/com/comp2042/GameController.java`:
  Extracted direction validation into a directly testable method and
  corrected the DOWN condition to reject movement opposite to UP.

- `Snake.java` checks the destination before applying movement so a fatal collision preserves the last valid position.
- `GameController.java` handles fullscreen shortcuts, stops finished rounds and resets the coin sequence for a new round.
- `Game.java` restores the original win target on reset.
- `Obstacle.java` spaces blocks across board quarters and relocates a block on repeat silver pickups when a suitable cell exists.
- `Coin.java` selects a free cell. `GameView.java` clears the coin sprite when the player wins.

- `GameController.java` buffers two valid turns and advances movement on JavaFX animation pulses. `GameView.java` interpolates positions between moves and rotates sprites directly on the canvas.
- `MovementTest.java` in the test package checks turn order, clearing input and wrap interpolation.

## Unexpected Problems

Quick cornering felt delayed in playtesting. The controller accepted one arrow per tick, including repeated directions, and discarded a second turn. Two distinct legal turns can now be queued. Movement starts on the next animation pulse. Collision and collection still occur at grid positions.


The entries below distinguish problems seen during play from faults identified by inspecting the code. The [testing record](Docs/Testing.md) gives the individual runs and their results.

| What revealed the problem | Cause and change | Evidence |
| --- | --- | --- |
| The snake could reverse vertically into its own body. | The DOWN check used the wrong direction. `GameController.isDirectionAllowed` now rejects each opposite pair. | 20 controller cases cover reversals, legal turns, repeated keys and non-arrow input. |
| A screenshot showed a gap at a bend. | Stored turn history could disagree with the occupied cells. `SnakeSpriteSelector` derives each sprite from neighbouring positions. `GameView` also applied the fullscreen scale twice. | Ten rounds without the observed gap. Wrapped-edge rendering was checked during play. |
| The fullscreen shortcut failed on the Windows keyboard. | Key logging showed EQUALS on the shared equals/plus key. `GameController` accepts EQUALS, PLUS and keypad ADD, plus MINUS and keypad SUBTRACT for windowed mode. | The player verified equals and minus in-game. |
| Collision review found that a fatal move could change the snake’s final position. | `Snake` validates the destination before moving and ignores later movement requests after death. | Body, wall and obstacle collisions, arrow input after death and reset were checked in-game. |
| A pale square surrounded the red coin. | The background was part of the PNG. The five coin assets were edited to use transparency. | The red coin was visually checked against the board. |
| Collecting coins was silent. | Source inspection found a bundled WAV with no playback call. `CoinSound` loads the resource and `GameController` requests playback on collection. | Five audio tests cover the resource, repeated requests and failures. Playback was also reported working in-game. |
| Round-end and reset review found a running timer after the snake stopped and a reduced win target carrying into the next round. | `GameController` stops the timeline on round completion or exit. Reset clears obstacles and `Game.resetScore` restores the original target. | `GameResetTest` checks score and target after a reset. See [PR #10](https://github.com/rushabkarania/COMP2042-Snake/pull/10). |
| Playtesting showed crowded blocks across the board and behind the labels even after 106 tests passed. | `Obstacle` spaces blocks across quarters and reserves rows near the labels. The win target uses the number actually placed. | Revised spacing was confirmed during play. |
| A player reached 130 points without seeing silver. Reviewing the selection branches also revealed that rainbow shared gold’s draw and could never be selected. | Silver had depended on chance and was ineligible while obstacles existed. `CoinSequence` now includes gold, blue and rainbow in each shuffled group and schedules silver eighth. | `CoinSequenceTest` checks 20 consecutive groups and the reset behaviour. |
| A later silver pickup awarded points but the obstacles looked identical. | Repeat pickups now move one block if a safe replacement cell exists. | Regression cases cover relocation and no available space. Repeat pickup behaviour was confirmed during play. |
| Coin-placement review identified two end-of-round cases: one free cell and a final coin still drawn after winning. | `Coin` chooses from free cells and `GameView` skips drawing the coin once the round is won. | Two placement tests cover the last free cell and the win target. Visual verification of the winning frame is pending. |

### Maven Wrapper download failure

The initial Maven Wrapper invocation failed with
`FileNotFoundException` because its distribution URL referenced
an unavailable Maven archive.

The `distributionUrl` in
`.mvn/wrapper/maven-wrapper.properties` was updated to reference
Maven 3.9.9. The wrapper then started successfully, and compilation
and application launch were verified on Windows.

Related change: [Pull request #1](https://github.com/rushabkarania/COMP2042-Snake/pull/1).

### Runtime warnings

The tested environment reports Java/native-access warnings and a
mismatch between the FXML JavaFX version declaration and the runtime
dependency. These did not prevent the observed build or launch.
Compatibility cleanup remains pending.

## Controls

| Key | Action |
| --- | --- |
| Arrow keys | Start movement and change direction |
| P | Pause |
| Arrow key while paused | Resume |
| Tab | Reset the game |
| Escape | Return to the main menu |
| = / + / number-pad + | Enter fullscreen |
| - / number-pad - | Return to windowed mode |

Known limitations affecting these controls are listed above.

## Testing

Completed checks:

- Maven Wrapper version check.
- Successful clean compilation.
- JavaFX application launch.
- Manual checks of the baseline gameplay features.

Obstacle placement leaves the top three and bottom two rows clear for labels. Each block starts at least four grid cells from the head and other blocks.

Run automated tests with `.\mvnw.cmd test`.

Direction validation currently has 20 passing parameterised test cases.
Manual checks also confirmed reversal prevention, pause/resume and reset.

See [test cases and results](Docs/Testing.md) for evidence and limitations.

Compilation success alone does not verify gameplay correctness.

### Short-snake turning animation

Windows playtesting found smooth straight movement but a disconnected-looking tail during a turn at score zero. Independently translating rotated segment sprites could leave a diagonal gap. GameView now draws a continuous rounded body through the occupied bend, keeping the head sprite and advancing the endpoints along grid edges. MovementTest checks the two-cell snake halfway through a right-angle turn. Windows visual confirmation of this revision is pending.

### Restoring the body artwork

A fullscreen playtest at score 55 showed a plain green line behind the head. The continuous-line renderer bypassed the straight, corner and tail images. GameView now draws the original body and corner sprites on their occupied cells and slides the pointed tail and head along the path. Visual confirmation on Windows is pending.

A Windows playtest exposed gaps at the neck and a flickering tail during successive turns. The renderer now draws a continuous connection under the body sprites and tapers the tail along the same path. See Docs/Testing.md for the reproduction steps.

A later fullscreen playtest at score 5 showed a square tail even while travelling straight. The earlier renderer painted a fixed body tile over the moving taper. SnakeBodyPainter now paints the body, markings and taper together from the interpolated path. The head uses its original sprite.

Fullscreen uses nearest-neighbour scaling for the snake body so the edges and scale markings retain their pixel-art appearance.

### Smooth movement and original snake art

A fullscreen playtest showed a coarse, jittering tail after movement interpolation was introduced. The renderer now draws the original body and tail PNGs directly at the board's display scale. Each section follows the same interpolated path as the head. The game clock carries a late frame's remaining time into the next step.

### Coin pickup performance

Playtesting revealed a brief pause when collecting coins. Each respawn decoded a 1254 × 1254 PNG on the game thread, and special coins also decoded the red image. The game now loads the five coin sprites when it creates the first coin and shares them across pickups and resets.

The windowed score display in GameView uses a smaller font and tighter spacing so the two-digit level target leaves room for the remaining lives.
