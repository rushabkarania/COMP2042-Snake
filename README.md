# Snake — COMP2042 Coursework

An endless JavaFX Snake game maintained and extended from the supplied coursework project. The aim is to keep playing and beat the saved best score. Difficulty increases with the score rather than through a selectable mode.

- **Student:** Rushab Rasik Karania
- **GitHub repository:** https://github.com/rushabkarania/COMP2042-Snake
- **Starter repository:** https://github.com/kooitt/CW2026
- **Generated API documentation:** [Javadoc folder](https://github.com/rushabkarania/COMP2042-Snake/tree/main/Javadoc). To read the formatted pages, open `Javadoc/index.html` locally after cloning or downloading the repository.

## Compilation instructions

### Requirements

- JDK **25 or newer**. The Windows IntelliJ project uses JDK 27; `pom.xml` sets the compiler release target to Java 25.
- Maven **3.9.9**, supplied through the included Maven wrapper.
- JavaFX **25.0.1**, downloaded as a Maven dependency.
- Internet access for the first dependency download, and a desktop environment for the game. Gameplay itself is offline.

For the Moodle source archive, extract `RushabRasikKarania_IntelliJ_27.zip` and open its `COMP2042-Snake` folder. For GitHub, clone this repository. Run commands from the folder containing `pom.xml`.

### Windows PowerShell

Set `JAVA_HOME` to the installed JDK and ensure `java -version` reports a compatible version. Import `pom.xml` as a Maven project in IntelliJ and select the same JDK for the project and Maven runner.

```powershell
java -version
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

### Linux/macOS

```sh
java -version
chmod +x mvnw
./mvnw clean test
./mvnw javafx:run
```

The Unix wrapper uses LF line endings, enforced by `.gitattributes`.

### Build and run a packaged JAR

On Windows:

```powershell
.\mvnw.cmd clean package
java --enable-native-access=ALL-UNNAMED -jar .\target\snake-javafx.jar
```

On Linux/macOS:

```sh
./mvnw clean package
java --enable-native-access=ALL-UNNAMED -jar target/snake-javafx.jar
```

Build the JAR on the operating system where it will run. Maven selects that platform's JavaFX native libraries; a Linux-built JAR is not a Windows distribution. The shaded JAR uses `Launcher` as its entry point and still requires JDK 25 or newer.

## Implemented and working properly

### Progressive difficulty and endless play

Every run starts at the same difficulty. Play opens the board with an arrow-key hint, but the snake stays still until an arrow is pressed. Play again follows the same flow.

| Stage | Score | Behaviour |
| --- | --- | --- |
| 1 | Below 40 | Collect pickups and avoid walls and the snake's own body. |
| 2 | 40–79 | Two short hedge sections are introduced after a warning preview. |
| 3 | 80 onward | Corner-shaped hedges appear. Further patterns are requested at 160, 240 and every additional 80 points. |

The normal movement rate starts at 8.5 steps per second, becomes 9.5 at stage 2 and 10.5 at stage 3, and then rises by 0.5 for each additional 80 points, capped at 14. Rush adds 3 steps per second, with a total cap of 17.

There is no fixed score-based victory. If the snake fills the available board, the session awards 25 bonus points and releases tail cells before spawning another pickup so the run can continue.

**Implementation:** `Game.level()`, `GameSession.speed()` and `HedgeLayouts.revision()`.

### Safe hedge changes and silver passages

A proposed layout is shown as translucent outlines for **12 successful movement steps**. Existing hedges remain solid until the replacement can be installed safely.

At proposal and activation, the checks reject hedges on the snake or pickup, and newly appearing cells within three squares of the head. They also require the garden to remain connected and preserve a route to the pickup around the live body. If activation becomes unsafe during the preview, it waits. A crowded board can retain its current layout rather than forcing a change.

Silver opens two adjacent hedge cells for **48 successful steps**, preferring a passage through the middle of a hedge. Collecting silver before hedges exist banks the effect until a layout appears or its timer expires. Recollecting silver refreshes the passage.

After expiry, the passage warns for **eight steps** before closing. Closure waits if the snake or pickup occupies the opening, the head is too close, or closure would remove the route to the pickup. Hedges remain stationary between safe layout changes.

**Implementation:** `Obstacle` manages previews, activation and passages; `HedgeLayouts.safeTransition()` checks occupancy, connectivity and reachability.

### Three lives and recovery

A fatal wall, body or hedge collision consumes one heart. After either of the first two losses, a short snake appears on a clear lane near the centre. The board stays visible with an arrow-key hint and no recovery dialog. An arrow orients the fresh snake and resumes play; Enter and Space do not bypass this wait.

The score, stage, hedges and current pickup are preserved. Temporary effects are cleared. After the third loss, the game-over screen displays the final score, best score and Play again button.

**Implementation:** `Game.loseHeart()`, `GameSession.step()` and `Snake.findRespawn()`.

### Pickups and movement-based timers

| Pickup | Points | Effect |
| --- | ---: | --- |
| Red | 5 | Grow by one cell. |
| Gold | 15 | Grow by one cell. |
| Blue | 5 | Grow and cross board edges for 48 successful steps. |
| Rainbow | 5 | Grow and gain a temporary speed increase for 40 successful steps. |
| Silver | 5 | Grow and open a two-cell hedge passage for 48 successful steps. |
| Mint scissors | 5 | Remove up to three tail cells without first growing; retain a head and at least one body cell. |

Effects can coexist. Collecting the same type refreshes its duration. A step means moving one board square; timers and hedge warnings do not advance while paused or waiting for an arrow.

The first pickup is red. Each subsequent group of eight contains three reds, one gold, one blue, one rainbow, one scissors and a final silver. The first seven are shuffled. Shrinking and losing a life do not deduct earned points.

**Implementation:** `CoinSequence`, `PowerUpState`, the collectible classes and `GameSession`.

### Interface, controls and sound

The header shows a neutral score star, best-score trophy, three hearts, pause, fullscreen, mute and close controls. The board uses a green checkerboard, the supplied snake sprites and illustrated hedge tiles. The menu snake is drawn from the same sprites as gameplay.

Help explains the pickups and controls. The footer displays the level and remaining effect steps. A blue border indicates edge crossing, pale cyan marks an open passage, and amber warns before a passage closes. Changing window focus pauses the run.

Collection reuses a loaded sound clip. Mute affects collection sounds and can be changed through the toolbar or `M`.

| Input | Action |
| --- | --- |
| Arrow keys | Start or resume the ready/recovered snake, and steer. Direct reversal is rejected. |
| P / Space | Pause or resume an active run. A fresh/recovered snake still requires an arrow. |
| Enter | Reveal the ready board, resume a paused run, or reset from game over. |
| R / Tab | Reset to the start panel. |
| M | Mute/unmute collection sounds. |
| F / F11 | Toggle fullscreen. |
| = / + / keypad + | Enter fullscreen. |
| - / keypad - | Leave fullscreen. |
| Escape | Leave fullscreen first; otherwise return to the start panel. |
| Header X | Save the best score and close the game. |

**Implementation:** `GameView`, `GameController`, `UiIcons`, `GardenAssets` and `CoinSound`.

### Persistent best score

The best score is saved on game over, reset and clean exit to:

```text
<user home>/.comp2042-snake/best.txt
```

Saving uses temporary-file replacement. Corrupt records recover to zero. If file access fails, the best score remains available in memory and the game-over panel explains the failure. An abrupt process termination can lose the current unsaved score.

A different save directory can be selected when launching the packaged game:

```sh
java -Dsnake.data.dir=./scores --enable-native-access=ALL-UNNAMED -jar target/snake-javafx.jar
```

**Implementation:** `HighScoreStore` and `GameSession`.

## Refactoring and design rationale

The main structural improvement separates gameplay rules from input, timing and presentation:

- `GameSession` owns the snake, pickups, score/life transitions, effects, progression and persistence decisions. Rule tests can exercise it without opening a window.
- `GameController` handles key events, a bounded turn buffer, the animation clock and collection-sound requests. `GameView` handles rendering and UI state.
- `SnakeRenderer` delegates body geometry to `SnakeBodyPainter`. Its nested `Surface` interface supplies drawing operations, allowing geometry to be tested independently of JavaFX drawing and reused by the menu.
- `ViewManager` manages one stage for the application lifecycle.

This applies single-responsibility separation to the session, controller and view. The shared painter uses a drawing abstraction rather than depending directly on the window. These choices reduce duplicated rendering logic and make rule and geometry regressions easier to test.

The final UML diagram is supplied as **Design.pdf in the Moodle submission**. The editable **Design.vpp is retained locally** as its Visual Paradigm authoring source. Design files and the demonstration video are intentionally excluded from the GitHub repository; generated API documentation is included under [Javadoc/](https://github.com/rushabkarania/COMP2042-Snake/tree/main/Javadoc).

## New Java classes

All production classes below are in `src/main/java/com/comp2042/`.

| New class | Purpose |
| --- | --- |
| `CoinSequence.java` | Selects balanced, shuffled pickup cycles. |
| `CoinSound.java` | Reuses collection audio and handles mute/load/playback failures. |
| `GameSession.java` | Owns run state, rules, pickups, progression, recovery and saving. |
| `PowerUpState.java` | Tracks independent effect durations in successful moves. |
| `HighScoreStore.java` | Reads/writes the best score with an in-memory fallback. |
| `ShrinkCoin.java` | Adds the tail-shortening pickup. |
| `HedgeLayouts.java` | Proposes progressive hedge patterns and validates safe transitions. |
| `GardenAssets.java` | Caches garden artwork, pickup thumbnails and the window logo. |
| `SnakeSpriteSelector.java` | Selects body sprites and prepares rendering paths. |
| `SnakeBodyPainter.java` | Shares body geometry through its nested `Surface` drawing interface. |
| `SnakeRenderer.java` | Draws the supplied sprites for gameplay and the menu. |
| `UiIcons.java` | Draws toolbar icons without depending on installed symbol fonts. |
| `Launcher.java` | Provides the ordinary Java entry point for the shaded JAR. |

## Modified Java classes and build files

Java files in this table are also in `src/main/java/com/comp2042/`.

| Modified file(s) | Changes and reason |
| --- | --- |
| `Game.java` | Manages score, lives and stage thresholds; replaces the cumulative pickup-count victory with endless session behaviour. |
| `Snake.java` | Adds optional pickup growth, bounded trimming, defensive position copies and safe respawn placement; preserves valid state on fatal moves. |
| `Coin.java` | Places pickups only in free cells and caches their sprites, avoiding full-board spawn failures. |
| `BlueCoin.java`, `GoldCoin.java`, `RainbowCoin.java`, `SilverCoin.java` | Integrates existing collectible types with session-managed scoring and effects. |
| `Obstacle.java` | Replaces scattered/patrolling obstacles with stationary layouts, warning previews, guarded activation and temporary passages. |
| `GameController.java` | Delegates rules to the session while retaining input/timing, buffered turns and animation updates. |
| `GameView.java` | Renders the session, compact HUD and panels, and interpolates movement. The final cleanup removes an uncalled private `paintSprite` helper. |
| `Main.java`, `ViewManager.java` | Use a single-window lifecycle; remove dependence on the obsolete separate FXML difficulty menu. |
| `pom.xml` (project root) | Pins the Java/JavaFX build, enables strict Javadoc generation and configures the shaded-JAR launcher; removes unused FXML dependencies. |
| `mvnw`, `.gitattributes` (project root) | Preserve Unix-wrapper LF line endings and Windows command-wrapper CRLF line endings. |

## Testing and verification

Tests are under `src/test/java/com/comp2042/`. They cover ordinary behaviour, edge cases and failure handling.

| Area | Test evidence |
| --- | --- |
| Controls and movement | `GameControllerTest`, `MovementTest`, `SnakeCollisionTest`: valid turns, rejected reversal, movement/collision state and dead-snake behaviour. |
| Rendering geometry | `SnakeSpriteSelectorTest`, `SnakeBodyPainterTest`: sprite orientation, bends, wrapped neighbours and body continuity calculations. |
| Pickups and progression | `CoinImageTest`, `CoinPlacementTest`, `CoinSequenceTest`, `PowerUpStateTest`, `ProgressionTest`: decoded assets, free-cell placement, pickup groups, move counters and score thresholds. |
| Session, reset and persistence | `GameSessionTest`, `GameResetTest`, `HighScoreStoreTest`: recovery, score/life state, reset, corrupt records and storage behaviour. |
| Hedge safety | `ObstacleSpawningTest`: previews, deferred activation, occupied cells, connected routes, banked/refreshed gates and safe closure. |
| Audio requests | `CoinSoundTest`: a real WAV resource, reused playback callbacks and safe handling of load/playback failures. |
| Native UI | `GardenUiTest`: actual JavaFX menus, arrow-key readiness, rapid turns, recovery, game over, fullscreen, small-window layout, help and toolbar behaviour. |

### Run the tests

The normal command works without enabling the desktop-only scenario:

```powershell
.\mvnw.cmd clean test
```

Expected summary: **158 reported, 157 passed, one optional UI test skipped, zero failures/errors**.

To include the native JavaFX scenario on a desktop:

```powershell
.\mvnw.cmd -Dsnake.ui.tests=true test
```

On Linux/macOS, replace `.\mvnw.cmd` with `./mvnw`. Linux native UI tests require an X display and a window manager. Test screenshots are temporary output under `target/ui-screenshots` and are not required runtime assets.

### Recorded results

| Verification | Result |
| --- | --- |
| Historical direction fix | The original 20-case direction test changed from two failures to all passing; reversal into the body is rejected. |
| Historical rendering/collision fixes | Coverage increased to 96 passing cases for valid state after collisions and sprite geometry. |
| Windows tests, 25 November 2026 | `mvnw.cmd clean test`: BUILD SUCCESS; 157 passed, one optional UI test skipped, zero failures/errors. |
| Windows documentation and packaged-game check, 25 November 2026 | Javadoc generation and opening succeeded; packaged launch/gameplay confirmed. |
| Separate Linux cleanup verification, 25 November 2026 | All 158 passed with native UI enabled; zero failures/errors/skips. JDK 25, Maven 3.9.9, JavaFX 25.0.1, Xvfb and xfwm4. |
| Submission documentation/build verification | Strict Javadoc generation succeeded, all 25 production-class pages were present and local documentation links resolved. The shaded JAR built and opened. |

Automated audio tests verify the resource and playback requests, not sound heard through speakers. Earlier recorded Windows gameplay checks on 19 November included audible collection sounds and saved-best persistence. The optional native scenario was skipped in the latest Windows default test run; it is not reported as a Windows native-test pass.

The first arrow processes a logical move in its event handler. Continuous turns use a maximum two-turn buffer and apply at grid boundaries. Native tests check rapid consecutive turns and partial-frame movement; this verifies the implemented timing behaviour rather than claiming zero hardware, OS or display latency.

## Implemented but not working properly

No outstanding gameplay defect was observed in the documented test scenarios. The following limitations remain:

- A run can lose its current unsaved best score if the process is terminated abruptly.
- File-access failures retain the best score only in memory until storage becomes available.
- JavaFX module/native-access and Maven Shade warnings can appear on some JDK/build configurations. They did not prevent the recorded successful tests and launches. Use the documented native-access launch option.
- The native UI test requires a desktop; it is intentionally skipped by default.

## Features not implemented

Online leaderboards, additional selectable maps, remappable controls and a volume slider were not implemented. The revision focuses on progressive layouts, power-ups, safe recovery and maintainability. Mute is available. Later hedge patterns continue within the third score stage rather than introducing a separate fourth map or mode.

## Unexpected problems and maintenance responses

| Problem | Location and response |
| --- | --- |
| Incorrect direction validation allowed reversal | `GameController` direction checks were corrected and covered by parameterised tests. |
| Snake bends, wrapped edges and fullscreen scaling produced gaps | `SnakeSpriteSelector`, `SnakeBodyPainter`, `SnakeRenderer` and `GameView` share geometry and interpolation rules, with regression coverage. |
| Gameplay rules were coupled to controls | `GameSession` separates rules/state from controller input and timing, allowing isolated model tests. |
| New obstacles could overlap or trap the snake | `Obstacle` previews transitions and `HedgeLayouts` validates occupancy, nearby head space, connectivity and a route to the pickup. |
| The existing sound resource was not being played | `CoinSound` loads a classpath resource once, reuses playback and handles failures without stopping gameplay. |
| A packaged JAR needs an ordinary launcher and platform natives | `Launcher` supplies the entry point; packaging instructions require building for the target OS. |
| Best-score files can be corrupt or unwritable | `HighScoreStore` validates input, uses temporary-file replacement and retains an in-memory fallback. |
| Old UI assets and documentation accumulated | Removed 14 unreferenced resource files, including the duplicate scissors export, and the uncalled drawing helper; consolidated supporting notes into this README. All 17 used runtime PNGs and useful editable SVGs remain. |

The detailed historical records remain available in Git history. Runtime files load from the classpath, so the project has no dependency on the author's Desktop or Downloads paths.

## Artwork and attribution

The supplied project provides the snake sprites, collectible artwork and window logo. Original garden SVGs and runtime PNGs are bundled under `src/main/resources/resources/views/res/garden/`; the scissors runtime export is `src/main/resources/resources/views/res/shrinkcoin.png`.

The checkerboard and compact arcade presentation were inspired by the supplied Google Snake reference. No Google code or artwork was copied. The supplied `SnakeLogo.png` remains the window icon, and the menu combines the same head/body/tail sprites used in gameplay. Unused menu buttons/fonts/styles, alternate logo and leaf/rock artwork were removed.

## Generate and publish Javadoc

The pinned Maven Javadoc plugin includes package-private helpers, uses UTF-8 and enables all DocLint checks. Warnings fail generation.

From Windows PowerShell in the project folder:

```powershell
.\mvnw.cmd clean javadoc:javadoc
if ($LASTEXITCODE -ne 0) { throw 'Javadoc generation failed; keep the existing published copy.' }

if (Test-Path .\Javadoc) { Remove-Item .\Javadoc -Recurse -Force }
New-Item -ItemType Directory -Path .\Javadoc | Out-Null
Copy-Item .\target\reports\apidocs\* .\Javadoc\ -Recurse -Force
Start-Process .\Javadoc\index.html
```

Generation writes to `target/reports/apidocs/`. Only after a successful build should its complete contents replace the published `Javadoc/` folder. This avoids leaving pages for removed classes behind. Keep all supporting files and the generated `legal/` notices.

On Linux/macOS, run `./mvnw clean javadoc:javadoc`, then copy the complete generated site's contents into `Javadoc/` after generation succeeds.

Commit the complete published `Javadoc/` folder to GitHub. It must not be excluded by `.gitignore`. `mvn clean` removes `target/`, but does not remove the separate published site.

## GitHub and Moodle files

| Item | GitHub repository | Moodle submission |
| --- | --- | --- |
| Source, tests, resources, Maven files and README | Included | Included in the project ZIP; README also at the submission root. |
| Published `Javadoc/` | Included | A complete copy at the submission root. |
| `Design.pdf` | Excluded; kept locally. | Required at the submission root. |
| `Demo.mp4` | Excluded; kept locally. | Required at the submission root; final video is 2 minutes 45.48 seconds. |
| `Design.vpp` | Excluded; retained locally as editable source. | Optional authoring source; the required diagram is the PDF. |
| `target/`, `dependency-reduced-pom.xml`, IDE caches and `.git/` | Build/IDE output is ignored; Git history remains in the working repository. | Excluded from the source ZIP. |

The repository ignore rules exclude `/Design.pdf`, `/Design.vpp` and `/Demo.mp4`, but **not `/Javadoc/`**. Previously tracked design/video files must also be removed from the Git index using `git rm --cached`; this keeps the local copies. This changes the latest repository contents without rewriting historical commits.

The Moodle submission is one ZIP containing:

```text
COMP2042RushabRasikKarania/
    README.md
    Design.pdf
    Javadoc/
    Demo.mp4
    RushabRasikKarania_IntelliJ_27.zip
```

The inner ZIP contains the buildable project. The outer `Javadoc/` must be a complete generated site, rather than a folder containing another `apidocs` folder. Preserve Git history in the working repository used for development and the final push; do not include Git metadata, build output, IDE caches or intermediate archives in the submission source ZIP. The whole Moodle archive must stay within the brief's 250 MB limit.
