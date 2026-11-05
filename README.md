# COMP2042 Snake

A JavaFX Snake game maintained and extended for COMP2042 coursework,
based on the supplied starter project.

**Status:** Initial maintenance. The Maven Wrapper configuration and
direction-reversal bug have been fixed. Direction validation has
20 passing JUnit cases. Further maintenance and extensions are pending.

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
| Obstacles | Collecting a silver coin introduces obstacles. |
| Game over | A game-over message and score are displayed. |
| Visible reset | Reset restores the displayed score and snake and clears obstacles. |

These checks cover basic behaviour. Known edge cases and defects
are listed below.

No new gameplay extensions have been completed.

- Direction validation prevents direct reversal into the snake's body.
  Verified with 20 passing JUnit cases and manual gameplay checks.

## Implemented but Not Working Properly

### Observed during gameplay

- **Collection audio:** The bundled sound plays when a coin is collected,
    including after reset and returning from the menu.
- **Snake rendering:** Body sections occasionally disappear briefly.
  The cause is still under investigation.
- **Obstacle density:** Obstacle spawning can feel excessive.
  The number and arrangement require gameplay-balancing review.

### Additional source-review findings

- Rainbow coin selection contains an unreachable branch.
- Obstacle generation can produce duplicate or out-of-bounds positions.
- Reset does not consistently restore all internal game state.
- Returning to the menu does not explicitly stop the previous timeline.
- A rendering calculation applies the scale twice.
- Coin placement has no explicit exit when no valid cell remains.

These findings require targeted tests and verification as fixes
are implemented.

## Features Not Implemented

| Planned feature | Status / reason pending |
| --- | --- |
| Selectable difficulty | Pending review of the existing difficulty logic. |
| Playable levels | Pending fixes to movement, collisions and obstacle placement. |
| Power-up improvements and a new mechanic | Pending repairs to existing power-up behaviour. |
| Saved high scores and play statistics | Persistence has not yet been implemented. |
| Background and obstacle assets | Visual improvements are planned after core gameplay fixes. |
| Audio settings | Pending working sound playback. |

These are development targets rather than completed features.
Any features omitted from the final version will be listed with
the reasons for omission.

## New Java Classes

- `src/main/java/com/comp2042/CoinSound.java`: Loads and reuses the collection sound; handles missing resources and synchronous audio failures.
- `src/test/java/com/comp2042/CoinSoundTest.java`: Checks the WAV resource, repeat playback requests and failure handling without audio hardware.

- `src/test/java/com/comp2042/GameControllerTest.java`:
  Parameterised JUnit tests covering opposite directions, perpendicular
  turns, repeated directions and non-arrow keys.

## Modified Java Classes

- `src/main/java/com/comp2042/GameController.java`:
  Extracted direction validation into a directly testable method and
  corrected the DOWN condition to reject movement opposite to UP.

## Unexpected Problems

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

Run automated tests with `.\mvnw.cmd test`.

Direction validation currently has 20 passing parameterised test cases.
Manual checks also confirmed reversal prevention, pause/resume and reset.

See [test cases and results](Docs/Testing.md) for evidence and limitations.

Compilation success alone does not verify gameplay correctness.