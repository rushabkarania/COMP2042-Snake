# Progressive Snake · implementation record

## Final behaviour

The game is an endless progressive run with one slim HUD, a square checkerboard, three hearts, saved best and six pickup types. The supplied SnakeLogo.png remains the native window icon. The menu uses a large side-on combined-sprite snake, painted by the same SnakeRenderer as gameplay, with the original head, body markings and tapered tail. It has no separate mascot or mode selector.

Play and Play again reveal a stationary board with a compact arrow-key hint, without a full-board scrim. The first arrow dismisses it and moves immediately. Losing a life resets a short snake near the centre, leaves the board unobstructed and waits for any arrow. Points, pickup and hedges remain. The third collision shows game over. Start/resume processes the first move directly in the key handler; continuous turns apply at grid boundaries through the existing two-turn buffer.

## Hedge mechanism

The former four stones/moving-rock patrol is replaced by structured stationary layouts. Stage 1 is clear. At 40 points short borders are proposed; 80 points introduces corners. At 160/240 points the patterns become staggered sections and lanes, then rotated variations continue every additional 80 points. Base movement speed still increases gradually and is capped.

HedgeLayouts creates bounded templates, rotates them and checks candidates. Obstacle holds the current collision cells, pending preview, gate opening and warning counters. A layout is visible as translucent/dashed cells for twelve successful moves before it can become solid. The old layout persists until replacement. No timer runs during pause, ready or recovery.

At proposal and activation, a candidate cannot cover head/body/pickup. Newly added cells must be more than three grid steps from the head. A breadth-first traversal verifies that all non-hedge cells remain connected. A second traversal treats the current body as blocked and verifies a route from head to the existing pickup. The replacement pickup is created before these checks. This is a conservative snapshot route check, not a guarantee that the player cannot subsequently trap themselves.

If a warned layout becomes unsafe, it waits at zero. It never reappears elsewhere without a fresh warning. A later score threshold can request a new preview. If no candidate is safe, retries are bounded/spaced and the existing layout remains. Small fixture boards below the supported layout size stay clear.

Silver awards five points and opens two adjacent hedge cells, preferring a genuine internal opening. It stays open for 48 successful moves, with the remaining duration in the HUD. Before hedges exist the effect can be banked. On expiry the opening stays clear during an eight-move closing warning. Closing reuses the placement/route checks, so it cannot restore a wall under the snake/coin or close a route the snake is currently using. Silver refresh cancels closing and keeps the same opening. Reset discards all layout and gate warnings.

## Design choices

GameSession owns round rules independently of JavaFX. PowerUpState has independent WRAP, RUSH and GAP durations. HighScoreStore owns persistence. GameController owns keyboard dispatch, queued turns and a testable animation pulse. GameView owns presentation. SnakeRenderer extracts the previous game renderer so the menu and game genuinely share compositing and sprite art.

The original Coin subclasses and fixed six-type effect switch are retained. The existing package is kept to avoid unrelated restructuring. New hedge SVG/PNG assets load from the classpath without network or machine-specific paths. The old patrol/freeze behaviour is removed rather than left running invisibly.

## Validation

Configured stack: Linux amd64, JDK 25, Maven Wrapper 3.9.9 and JavaFX 25.0.1. The native display uses Xvfb and a window manager for actual 1280 × 1100 fullscreen checks. Local proxy/cache flags are test-environment configuration, not application dependencies.

Model coverage includes the twelve-move noncolliding preview, later replacement, activation deferred by snake/coin/head proximity, connected garden, route blocked by the live body, tiny-board fallback, stable warning countdown, two-cell openings, eight-move closure warning, occupied-gap deferral, silver refresh, banked silver and reset. Existing collision, growth/shrink, score/lives, persistence, independent-effect, sprite-continuity and input-queue regression cases remain.

The native UI scenario checks Play revealing a stationary board, an arrow-key prompt, help, the original window icon, a Canvas menu snake, neutral score symbol, preview/solid/open-gap screenshots, recovery without a panel, all life losses, saved best, game over, reset, fullscreen and short-window geometry. An injected clock verifies immediate start/resume, first-partial-frame rendered movement, no extra early step and two rapid turns at consecutive legal boundaries. Screenshots use scripted score/layout fixtures to exercise states promptly, not claimed naturally earned scores.

See Validation.md for the final test count/results. Audio is muted during scripted native collection because the virtual display has no sound device. Audible playback and Windows hardware/OS/display input latency are not established by these tests.

## Coursework and delivery

The supplied brief does not explicitly require a finite win condition. The evolving layouts, gate pickup and progressive speed implement the requested navigation challenge while keeping the endless score objective. Full-board completion releases tail cells and awards a bonus instead of producing an invalid coin spawn.

The working branch is feature/endless-garden-hud. The supplied uncommitted work and Git history are preserved; no remote commits, PRs or pushes were fabricated. The delivery ZIP excludes IDE state, compiled output and downloaded validation tools.

This revision was implemented with AI assistance. This record identifies the assisted design and validation for the user's coursework declaration and code understanding.

## Arrow-key readiness and clearer instructions

The Play action is separate from starting the clock. READY retains the visible board until a direction is chosen. RECOVERY uses the same noninteractive hint; it cannot intercept keyboard events. Reset clears view readiness even when already READY. Help defines a step as one square, explains waiting/paused countdowns, and gives each pickup a detailed tooltip. The footer replaces Wrap/Rush/Gap with Edge crossing/Speed boost/Open passage; warning countdowns also use steps. The original menu head remains aligned on the grass, using the supplied sprite rather than changing the artwork.
