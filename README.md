# Snake

An endless, progressively harder JavaFX Snake game maintained from the COMP2042 starter project.

- Project repository: https://github.com/rushabkarania/COMP2042-Snake
- Starter repository: https://github.com/kooitt/CW2026

![Gameplay](Docs/images/snake-play.png)

## Build and run

Install **JDK 25 or newer**, set `JAVA_HOME`, and open a terminal in this project directory. JavaFX 25.0.1 and Maven 3.9.9 are downloaded automatically by Maven/the wrapper on the first build. Gameplay is offline.

Windows PowerShell:

```powershell
java -version
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

Linux/macOS:

```sh
java -version
./mvnw clean test
./mvnw javafx:run
```

If an archive extractor removes executable permissions, use `chmod +x mvnw` or `sh mvnw javafx:run`. The Unix launcher has been fixed to use LF line endings.

To build a runnable JAR **on the computer/OS where it will be used**:

```sh
./mvnw clean package
java --enable-native-access=ALL-UNNAMED -jar target/snake-javafx.jar
```

Use `mvnw.cmd` on Windows. A Linux-built JAR contains Linux JavaFX native libraries and is not a Windows build. `Launcher` provides the ordinary Java entry point required for the shaded JAR. JDK 25+ is still needed to run it.

## Implemented and working

### Progressive difficulty; no mode selector

Everyone starts the same run. There is one Play button, one board and one saved personal best. There is no Easy/Normal/Hard menu. Play reveals the board and a small arrow-key hint; the snake stays still until an arrow is pressed. The first arrow dismisses the hint and moves immediately. Play again follows the same waiting flow.

| Stage | Trigger | Challenge |
| --- | --- | --- |
| 1 | Start | Collect coins; avoid walls and your own tail. |
| 2 | 40 total points | Two short hedge borders are previewed before becoming solid. |
| 3 | 80 total points | Corner-shaped hedges introduce more navigation choices. |
| Later layouts | 160, 240, then every 80 points | Staggered sections and lane layouts rotate, while speed continues to rise. |

Speed begins at 8.5 steps/second, increases to 9.5 at stage 2 and 10.5 at stage 3. After 80 points it continues increasing by 0.5 for each additional 80 points, capped at 14. A temporary rush can add 3, with a total cap of 17.

The game continues as a score-beating run. Filling the board awards 25 bonus points and releases tail cells before spawning another coin; it does not end the game or throw a full-board spawn error.

### Hedge changes and silver gates

New layouts show translucent outlines for **12 successful moves** before activation. Old hedges remain solid until their replacement is safe. At both proposal and activation, the game rejects walls on the head/body/pickup or newly appearing within three cells of the head. It also checks that the garden remains connected and the current pickup still has a route around the live body. If a change becomes unsafe during its preview, it waits; it does not force walls onto the snake. A crowded board can keep its current layout rather than forcing a new one.

Silver opens two adjacent cells through a hedge, preferring an internal opening. It lasts for 48 successful moves. If no hedge exists yet, the effect is banked and opens a gate if a layout arrives before it expires. A refreshed silver keeps the existing gap open.

After the effect expires, the opening warns for **eight moves** before closing. Closure is deferred while the snake/coin occupies the gap, the head is too close, or closing would remove the route to the pickup. All warnings/effects stop while paused or waiting after life loss. Hedgerows do not move around the board.

### Three hearts

Every fatal collision consumes one heart. After the first two, a short snake respawns on the nearest clear lane to the centre. No dialog or dimming is shown: the board stays visible with a small arrow-key hint, without darkening the board. Any arrow orients the fresh snake safely and resumes it. Enter/Space do not bypass this recovery wait. Score, stage, hedges and the current coin are preserved; temporary effects are cleared. The third collision displays Game over, score, best, and a Play again button.

### Pickups

| Pickup | Points | Effect |
| --- | --- | --- |
| Red | 5 | Grow one cell. |
| Gold | 15 | Grow one cell. |
| Blue | 5 | Grow; wrap through edges for 48 successful moves. |
| Rainbow | 5 | Grow; +3 speed for 40 successful moves. |
| Silver | 5 | Grow; open a two-cell gap through a hedge for 48 successful moves. |
| Mint scissors | 5 | Trim up to three tail cells without growing first; minimum length two. |

Effects coexist. Recollecting a type refreshes its duration. Pausing and waiting after life loss do not consume moves. Help defines one step as moving one square. The footer uses Edge crossing, Speed boost and Open passage, with remaining steps; pickup instructions have explanatory tooltips. Active effects show remaining steps unobtrusively below the board. A blue border indicates wrapping. An open hedge gap is outlined in pale cyan; an amber outline warns before it closes.

The first coin is red. Every subsequent group of eight contains three reds, one gold, one blue, one rainbow, one scissors and a final silver. The first seven are shuffled. Shrinking and life loss never deduct earned points.

### Simple interface

- One slim top bar: neutral points star, trophy best, three hearts, pause, fullscreen, sound and close.
- A large green checkerboard, original snake sprites, and stationary illustrated hedge sections.
- Compact blue start/pause/game-over panels with a single primary action. Life loss stays on the board without a panel.
- Pickup explanations and keyboard help are behind **How to play**.
- A small level indicator; no dashboard cards, slogans, persistent pickup legend or mode selector.
- Square board geometry in windowed/fullscreen mode; state panels stay inside the board on shorter displays.
- Changing focus pauses the run automatically.

The checkerboard/compact-panel presentation takes inspiration from the supplied Google Snake reference. No Google code or artwork was copied. New original SVG sources and PNG exports are under `src/main/resources/resources/views/res/garden/`. The supplied `SnakeLogo.png` remains the window/taskbar icon (16/32/64 px). The menu now draws a large side-on snake from the original head, patterned body and tapered tail sprites through the same renderer as gameplay. Its body enters from the left edge, faces right and sits directly above the taller checkerboard strip, following the supplied menu-layout reference. There is no separate mascot in the menu. Coins appear only as collectibles and in pickup instructions.

| Input | Action |
| --- | --- |
| Arrow keys | Start/resume and steer; direct reversal is rejected. |
| P / Space | Pause/resume. |
| Enter | Reveal the ready board; resume when paused; reset from game over. Fresh/recovered snakes wait for an arrow. |
| R / Tab | Reset to the start panel. |
| M | Mute/unmute collection sounds. |
| F / F11 | Toggle fullscreen. |
| = / + / keypad + | Enter fullscreen. |
| - / keypad - | Leave fullscreen. |
| Escape | Leave fullscreen first; otherwise return to start. |
| Header X | Close the game, saving the personal best. |

### Saved best

Records save on game over, reset and clean exit in:

```text
<user home>/.comp2042-snake/best.txt
```

For a portable packaged run:

```sh
java -Dsnake.data.dir=./scores --enable-native-access=ALL-UNNAMED -jar target/snake-javafx.jar
```

The HUD updates immediately; disk saving uses temporary-file replacement. Corrupt records recover to zero. A file-access failure retains the best in memory and is explained on the game-over panel. An abrupt process kill can lose the current run's unsaved score.

## Design and changes

`GameSession` owns rules/state independently of the window. `GameController` owns the input queue and animation clock. `GameView` owns the board, minimal HUD and panels. `ViewManager` uses one stage for both the start panel and gameplay.

New production classes in `src/main/java/com/comp2042/`:

- `GameSession`: endless play, pickups, progression, recovery and saving.
- `PowerUpState`: independent effect durations in successful moves.
- `HighScoreStore`: persistent best with a memory fallback.
- `ShrinkCoin`: scissors collectible.
- `GardenAssets`: cached decorations and small coin thumbnails.
- `HedgeLayouts`: progressively structured patterns, connectivity and live-body route checks.
- `SnakeRenderer`: shared sprite compositing for the actual game and menu snake.
- `UiIcons`: font-independent vector toolbar icons.
- `Launcher`: shaded-JAR entry point.

Modified existing classes:

- `Game`: removed cumulative pickup-count victory; manages score, lives and board settings.
- `Snake`: optional pickup growth, bounded tail trimming and defensive copying of body points; existing collision rules remain.
- `Coin`/`CoinSequence`: scissors collectible and guaranteed special-pickup cycle.
- `Obstacle`: stationary layout previews, guarded activation and temporary two-cell gates, replacing scattered stones/patrols.
- `GameController`/`GameView`: session-driven controls and simplified interface; existing buffered turns and sprite-path interpolation retained.
- `Main`/`ViewManager`: one-window lifecycle. The obsolete separate `MainMenuController`/FXML difficulty menu is removed.
- `pom.xml`: JDK 25 target, ordinary shaded-JAR entry point, native-access options; unused FXML dependency removed.
- `mvnw`: Unix line-ending fix.

See `Design.pdf`, `Design.vpp`, `Docs/Design.md`, `Javadoc/`, and `Docs/GardenRevision.md` for the final design and rationale. Historical maintenance evidence remains in `Docs/Testing.md`.

## Tests and limitations

Validated on Linux amd64 using JDK 25, Maven 3.9.9 and JavaFX 25.0.1. **158 checks passed** with native UI testing enabled: 157 model/helper cases and one native JavaFX scenario. It checks the supplied window icon, combined-sprite menu snake, hedge previews/solid layouts/open gates, neutral score star, unobstructed centre respawn with an arrow-only wait, immediate keyboard start/resume and consecutive buffered turns, Play waiting for an arrow, immediate prompt dismissal, on-demand help, no mode selector, pause/start, a scripted pickup crossing 40 points, life loss, saved best, game over, Play again, reset, mute, actual 1280 × 1100 fullscreen, and short-window board/button layout.

The default test command skips the opt-in native scenario so it works without a display. To include it on a machine with a desktop:

```sh
./mvnw -Dsnake.ui.tests=true test
```

Linux fullscreen testing requires an X display **and a window manager**. Screenshots default to `target/ui-screenshots`; the level-boundary test uses a scripted starting score/coin to exercise the transition quickly.

Windows rendering and audible playback of this revision still need a local check. The virtual display has no working sound output; native checks mute before pickup. Unit tests verify the WAV, playback requests/reuse and failure handling, not audible sound. Some JDK/Maven/JavaFX deprecation/configuration warnings remain separate from test results.

### Input responsiveness

Pressing Play does not start the movement clock. The first movement arrow starts/resumes and processes the first logical move inside its key handler. Continuous turns apply at the next grid boundary with a maximum two-turn buffer; repeated keys do not add delay or accelerate the clock. Native checks verify two rapid turns on consecutive ticks, partial-frame head movement before a full tick, and no extra move on the first partial pulse. Rendering is issued once per animation pulse. These checks establish no added start/resume or extra-tick input wait; they do not claim zero hardware/OS/display latency on Windows.

## Features not implemented

Online leaderboards, extra maps, remappable keys and a volume slider are outside this revision. Mute is available. The existing three stages evolve into continued speed progression rather than an untested fourth map.
